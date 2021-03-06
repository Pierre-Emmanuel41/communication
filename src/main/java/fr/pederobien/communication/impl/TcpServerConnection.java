package fr.pederobien.communication.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import fr.pederobien.communication.EConnectionState;
import fr.pederobien.communication.event.DataReceivedEvent;
import fr.pederobien.communication.event.LogEvent;
import fr.pederobien.communication.event.LogEvent.ELogLevel;
import fr.pederobien.communication.event.UnexpectedDataReceivedEvent;
import fr.pederobien.communication.interfaces.IAnswersExtractor;
import fr.pederobien.communication.interfaces.ICallbackRequestMessage;
import fr.pederobien.communication.interfaces.IObsTcpConnection;
import fr.pederobien.communication.interfaces.ITcpConnection;
import fr.pederobien.utils.BlockingQueueTask;
import fr.pederobien.utils.ByteWrapper;
import fr.pederobien.utils.Observable;
import fr.pederobien.utils.SimpleTimer;

public class TcpServerConnection implements ITcpConnection {
	private IAnswersExtractor answersExtractor;
	private SimpleTimer timer;
	private TimerTask receiving;
	private BlockingQueueTask<byte[]> extractingQueue;
	private BlockingQueueTask<UnexpectedDataReceivedEvent> unexpectedQueue;
	private BlockingQueueTask<ICallbackRequestMessage> sendingQueue;
	private Socket socket;
	private EConnectionState connectionState;
	private AtomicBoolean isDisposed;
	private Observable<IObsTcpConnection> observers;
	private String remoteAddress;

	public TcpServerConnection(Socket socket, IAnswersExtractor answersExtractor) {
		this.socket = socket;
		this.answersExtractor = answersExtractor;

		remoteAddress = socket.getInetAddress().toString().substring(1);

		isDisposed = new AtomicBoolean(false);
		observers = new Observable<IObsTcpConnection>();

		timer = new SimpleTimer("TcpServerConnectionTimer_".concat(remoteAddress), true);

		sendingQueue = new BlockingQueueTask<>("Sending_".concat(remoteAddress), message -> startSending(message));
		extractingQueue = new BlockingQueueTask<>("Extracting_".concat(remoteAddress), answer -> startExtracting(answer));
		unexpectedQueue = new BlockingQueueTask<>("UnexpectedData_".concat(remoteAddress), event -> startReceivingUnexpectedData(event));

		connectionState = EConnectionState.CONNECTED;

		sendingQueue.start();
		extractingQueue.start();
		unexpectedQueue.start();
		receiving = timer.schedule(() -> startReceiving(), 0);
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

		cancelTimerTask(receiving);

		connectionState = EConnectionState.DISCONNECTED;
		onLogEvent(ELogLevel.INFO, null, "%s - Connection closed", remoteAddress);
	}

	@Override
	public void send(ICallbackRequestMessage message) {
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

		timer.cancel();
		sendingQueue.dispose();
		extractingQueue.dispose();

		onLogEvent(ELogLevel.INFO, null, "%s - Connection disposed", remoteAddress);

		observers.notifyObservers(obs -> obs.onConnectionDisposed());
	}

	@Override
	public boolean isDisposed() {
		return isDisposed.get();
	}

	@Override
	public void addObserver(IObsTcpConnection obs) {
		observers.addObserver(obs);
	}

	@Override
	public void removeObserver(IObsTcpConnection obs) {
		observers.removeObserver(obs);
	}

	private void startExtracting(byte[] answer) {
		onDataReceivedEvent(answer, answer.length);
		Map<Integer, byte[]> answers = answersExtractor.extract(answer);
		for (Map.Entry<Integer, byte[]> entry : answers.entrySet())
			unexpectedQueue.add(new UnexpectedDataReceivedEvent(getAddress(), entry.getKey(), entry.getValue()));
	}

	private void startReceiving() {
		while (connectionState == EConnectionState.CONNECTED) {
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
			}
		}
	}

	private void startReceivingUnexpectedData(UnexpectedDataReceivedEvent event) {
		observers.notifyObservers(obs -> obs.onUnexpectedDataReceived(event));
	}

	private void startSending(ICallbackRequestMessage message) {
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
		cancelTimerTask(receiving);

		observers.notifyObservers(obs -> obs.onConnectionLost());
	}

	private void onDataReceivedEvent(byte[] answer, int length) {
		observers.notifyObservers(obs -> obs.onDataReceived(new DataReceivedEvent(getAddress(), answer, length)));
	}

	private void onLogEvent(ELogLevel level, Exception exception, String message, Object... parameters) {
		observers.notifyObservers(obs -> obs.onLog(new LogEvent(level, String.format(message, parameters), exception)));
	}

	private void cancelTimerTask(TimerTask task) {
		if (task != null)
			task.cancel();
	}
}
