package fr.pederobien.communication.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import fr.pederobien.communication.EConnectionState;
import fr.pederobien.communication.event.DataReceivedEvent;
import fr.pederobien.communication.event.LogEvent;
import fr.pederobien.communication.event.LogEvent.ELogLevel;
import fr.pederobien.communication.event.UnexpectedDataReceivedEvent;
import fr.pederobien.communication.interfaces.IAnswersExtractor;
import fr.pederobien.communication.interfaces.IConnection;
import fr.pederobien.communication.interfaces.IObsConnection;
import fr.pederobien.communication.interfaces.IRequestMessage;
import fr.pederobien.utils.ByteWrapper;
import fr.pederobien.utils.Observable;

public class ServerConnection implements IConnection {
	private IAnswersExtractor answersExtractor;
	private ScheduledFuture<?> receiving;
	private ScheduledExecutorService executorService;
	private BlockingQueueTask<byte[]> extractingQueue;
	private BlockingQueueTask<UnexpectedDataReceivedEvent> unexpectedQueue;
	private BlockingQueueTask<IRequestMessage> sendingQueue;
	private Socket socket;
	private EConnectionState connectionState;
	private AtomicBoolean isDisposed;
	private Observable<IObsConnection> observers;
	private String remoteAddress;

	public ServerConnection(Socket socket, IAnswersExtractor answersExtractor) {
		this.socket = socket;
		this.answersExtractor = answersExtractor;

		remoteAddress = socket.getInetAddress().toString().substring(1);

		isDisposed = new AtomicBoolean(false);
		observers = new Observable<IObsConnection>();

		// Create a thread pool such as each created thread are daemon.
		executorService = Executors.newScheduledThreadPool(5, runnable -> {
			Thread thread = new Thread(runnable);
			thread.setDaemon(true);
			return thread;
		});

		receiving = executorService.schedule(() -> startReceiving(), 1, TimeUnit.MILLISECONDS);

		sendingQueue = new BlockingQueueTask<>("Sending_".concat(remoteAddress), message -> startSending(message));
		extractingQueue = new BlockingQueueTask<>("Extracting_".concat(remoteAddress), answer -> startExtracting(answer));
		unexpectedQueue = new BlockingQueueTask<>("UnexpectedData_".concat(remoteAddress), event -> startReceivingUnexpectedData(event));

		sendingQueue.start();
		extractingQueue.start();
		unexpectedQueue.start();

		connectionState = EConnectionState.CONNECTED;
	}

	@Override
	public InetSocketAddress getAddress() {
		return socket == null ? null : (InetSocketAddress) socket.getRemoteSocketAddress();
	}

	@Override
	public EConnectionState getState() {
		return connectionState;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void setIsEnabled(boolean isEnabled) {
	}

	@Override
	public void connect() {
		throw new UnsupportedOperationException("Cannot connect using a ServerConnection");
	}

	@Override
	public void disconnect() {
		checkDisposed();

		connectionState = EConnectionState.DISCONNECTING;

		onLogEvent(ELogLevel.INFO, null, "%s - closing connection", remoteAddress);
		closeSocket();

		cancelFuture(receiving);

		connectionState = EConnectionState.DISCONNECTED;
		onLogEvent(ELogLevel.INFO, null, "%s - Connection closed", remoteAddress);
	}

	@Override
	public void send(IRequestMessage message) {
		checkDisposed();

		if (getState() == EConnectionState.CONNECTED)
			sendingQueue.add(message);
	}

	@Override
	public void dispose() {
		if (getState() != EConnectionState.DISCONNECTED)
			disconnect();

		if (isDisposed.compareAndSet(false, true))
			return;

		onLogEvent(ELogLevel.INFO, null, "%s - Disposing connection", remoteAddress);

		sendingQueue.dispose();
		extractingQueue.dispose();
		try {
			executorService.awaitTermination(2000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		onLogEvent(ELogLevel.INFO, null, "%s - Connection disposed", remoteAddress);
	}

	@Override
	public boolean isDisposed() {
		return isDisposed.get();
	}

	@Override
	public void addObserver(IObsConnection obs) {
		observers.addObserver(obs);
	}

	@Override
	public void removeObserver(IObsConnection obs) {
		observers.removeObserver(obs);
	}

	private void notifyObservers(Consumer<IObsConnection> consumer) {
		observers.notifyObservers(consumer);
	}

	private void startExtracting(byte[] answer) {
		onDataReceivedEvent(answer, answer.length);
		Map<Integer, byte[]> answers = answersExtractor.extract(answer);
		for (Map.Entry<Integer, byte[]> entry : answers.entrySet())
			unexpectedQueue.add(new UnexpectedDataReceivedEvent(entry.getKey(), entry.getValue()));
	}

	private void startReceiving() {
		while (!isDisposed()) {
			try {
				byte[] buffer = new byte[1024];
				int read = socket.getInputStream().read(buffer);

				if (read == -1) {
					onConnectionLostEvent();
					break;
				}

				extractingQueue.add(ByteWrapper.wrap(buffer).extract(0, read));
			} catch (IOException e) {
				onLogEvent(ELogLevel.WARNING, e, "%s - Reception failure : %s", remoteAddress, e.getMessage());
				onConnectionLostEvent();
				break;
			}
		}
	}

	private void startReceivingUnexpectedData(UnexpectedDataReceivedEvent event) {
		notifyObservers(obs -> obs.onUnexpectedDataReceived(event));
	}

	private void startSending(IRequestMessage message) {
		try {
			socket.getOutputStream().write(message.getBytes());
			socket.getOutputStream().flush();
		} catch (IOException e) {
			onLogEvent(ELogLevel.WARNING, e, "%s - Send failure : %s", remoteAddress, e.getMessage());
			onConnectionLostEvent();
		}
	}

	private void closeSocket() {
		try {
			if (socket == null)
				return;

			socket.shutdownInput();
			socket.shutdownOutput();
			socket.close();
		} catch (IOException e) {
			// Do nothing
		}
	}

	private void checkDisposed() {
		if (isDisposed())
			throw new UnsupportedOperationException("Object disposed");
	}

	private void onConnectionLostEvent() {
		connectionState = EConnectionState.CONNECTION_LOST;
		onLogEvent(ELogLevel.INFO, null, "%s - Connection lost", remoteAddress);

		closeSocket();
		cancelFuture(receiving);

		notifyObservers(obs -> obs.onConnectionLost());
	}

	private void onDataReceivedEvent(byte[] answer, int length) {
		notifyObservers(obs -> obs.onDataReceived(new DataReceivedEvent(answer, length)));
	}

	private void onLogEvent(ELogLevel level, Exception exception, String message, Object... parameters) {
		notifyObservers(obs -> obs.onLog(new LogEvent(level, String.format(message, parameters), exception)));
	}

	private void cancelFuture(ScheduledFuture<?> future) {
		if (future != null)
			future.cancel(true);
	}
}
