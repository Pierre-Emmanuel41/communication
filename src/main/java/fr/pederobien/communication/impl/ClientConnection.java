package fr.pederobien.communication.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import fr.pederobien.communication.EConnectionState;
import fr.pederobien.communication.event.DataReceivedEvent;
import fr.pederobien.communication.event.LogEvent;
import fr.pederobien.communication.event.LogEvent.ELogLevel;
import fr.pederobien.communication.interfaces.IAnswersExtractor;
import fr.pederobien.communication.interfaces.IConnection;
import fr.pederobien.communication.interfaces.IObsConnection;
import fr.pederobien.communication.interfaces.IRequestMessage;
import fr.pederobien.utils.ByteWrapper;
import fr.pederobien.utils.Observable;
import fr.pederobien.utils.SimpleTimer;

public class ClientConnection implements IConnection {
	/**
	 * Time in ms to establish the connection to the remote.
	 */
	private static final int CONNECTION_TIMEOUT_MS = 100;

	/**
	 * Time in ms to wait before retrying to connect when the connection has been lost.
	 */
	private static final int RECONNECTION_IDLE_TIME = 300;

	/**
	 * Time in ms to retry and establish the connection to the remote when a SocketException has been thrown.
	 */
	private static final int SOCKET_ERROR_RETRY_MS = 1000;

	private SimpleTimer timer;
	private TimerTask receiving, connection;
	private BlockingQueueTask<byte[]> extractingQueue;
	private BlockingQueueTask<IRequestMessage> sendingQueue;
	private String remoteAddress;
	private int remotePort;
	private Socket socket;
	private boolean isEnabled;
	private RequestResponseManager requestResponseManager;
	private EConnectionState connectionState;
	private AtomicBoolean isDisposed;
	private Observable<IObsConnection> observers;

	public ClientConnection(String remoteAddress, int remotePort, IAnswersExtractor answersExtractor, boolean isEnabled) {
		this.remoteAddress = remoteAddress;
		this.remotePort = remotePort;
		this.isEnabled = isEnabled;

		isDisposed = new AtomicBoolean(false);

		requestResponseManager = new RequestResponseManager(this, remoteAddress, answersExtractor);
		observers = new Observable<IObsConnection>();

		sendingQueue = new BlockingQueueTask<IRequestMessage>("Sending_".concat(remoteAddress), message -> startSending(message));
		extractingQueue = new BlockingQueueTask<byte[]>("Extracting_".concat(remoteAddress), answer -> startExtracting(answer));

		connectionState = EConnectionState.DISCONNECTED;
	}

	public ClientConnection(String remoteAddress, int remotePort, IAnswersExtractor answerExtractor) {
		this(remoteAddress, remotePort, answerExtractor, true);
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
		return isEnabled;
	}

	@Override
	public void setIsEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	@Override
	public void connect() {
		checkDisposed();

		if (connectionState != EConnectionState.CONNECTION_LOST && connectionState != EConnectionState.DISCONNECTED)
			return;

		connectionState = EConnectionState.CONNECTING;
		onLogEvent(ELogLevel.INFO, null, "%s - Starting connection", remoteAddress);

		timer = new SimpleTimer("ClientConnection", true);
		connection = timer.scheduleAtFixedRate(() -> startConnect(), 0, RECONNECTION_IDLE_TIME);
	}

	@Override
	public void disconnect() {
		checkDisposed();

		connectionState = EConnectionState.DISCONNECTING;

		onLogEvent(ELogLevel.INFO, null, "%s - Closing connection", remoteAddress);
		closeSocket();
		cancelTimerTask(connection);
		cancelTimerTask(receiving);

		connectionState = EConnectionState.DISCONNECTED;
		onLogEvent(ELogLevel.INFO, null, "%s - Connection closed", remoteAddress);
	}

	@Override
	public void send(IRequestMessage message) {
		checkDisposed();

		if (isEnabled || getState() == EConnectionState.CONNECTED)
			sendingQueue.add(message);
	}

	@Override
	public void dispose() {
		if (getState() != EConnectionState.DISCONNECTED)
			disconnect();

		if (!isDisposed.compareAndSet(false, true))
			return;

		onLogEvent(ELogLevel.INFO, null, "%s - Disposing connection", remoteAddress);

		timer.cancel();
		sendingQueue.dispose();
		extractingQueue.dispose();
		requestResponseManager.dispose();

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

	public void notifyObservers(Consumer<IObsConnection> consumer) {
		observers.notifyObservers(consumer);
	}

	private void startConnect() {
		try {
			Socket socket = null;

			try {
				socket = new Socket();
				socket.connect(new InetSocketAddress(InetAddress.getByName(remoteAddress), remotePort), CONNECTION_TIMEOUT_MS);
				this.socket = socket;

				onConnectionCompleteEvent();
			} catch (SocketTimeoutException e) {
				socket.close();
				onLogEvent(ELogLevel.INFO, null, "%s - Connection timeout. Retry.", remoteAddress);
			} catch (IOException e) {
				connection.cancel();
				connection = timer.scheduleAtFixedRate(() -> startConnect(), SOCKET_ERROR_RETRY_MS, RECONNECTION_IDLE_TIME);
			}
		} catch (IOException e) {
			// do nothing
		}
	}

	private void startExtracting(byte[] answer) {
		// publish message
		onDataReceivedEvent(answer, answer.length);

		try {
			requestResponseManager.handleResponse(answer);
		} catch (UnsupportedOperationException e) {
			onLogEvent(ELogLevel.ERROR, e, "%s - Exception when retrieving answer : %s", remoteAddress, e.getMessage());
		}
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
				if (getState() == EConnectionState.DISCONNECTING || getState() == EConnectionState.DISCONNECTED)
					return;

				onLogEvent(ELogLevel.WARNING, e, "%s - Reception failure : %s", remoteAddress, e.getMessage());
				onConnectionLostEvent();
				break;
			}
		}
	}

	private void startSending(IRequestMessage message) {
		try {
			socket.getOutputStream().write(message.getBytes());
			socket.getOutputStream().flush();
			requestResponseManager.addRequest(message);
		} catch (SocketException e) {
			onLogEvent(ELogLevel.WARNING, e, "%s - Send failure : %s", remoteAddress, e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
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

	private void onConnectionCompleteEvent() {
		if (connectionState == EConnectionState.DISCONNECTING || connectionState == EConnectionState.DISCONNECTED)
			return;

		onLogEvent(ELogLevel.INFO, null, "%s - Connection successfull", remoteAddress);

		cancelTimerTask(connection);
		receiving = timer.schedule(() -> startReceiving(), 0);

		sendingQueue.start();
		extractingQueue.start();

		connectionState = EConnectionState.CONNECTED;

		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			return;
		}

		notifyObservers(obs -> obs.onConnectionComplete());
	}

	private void onConnectionLostEvent() {
		connectionState = EConnectionState.CONNECTION_LOST;
		onLogEvent(ELogLevel.INFO, null, "%s - Connection lost", remoteAddress);

		closeSocket();

		notifyObservers(obs -> obs.onConnectionLost());

		onLogEvent(ELogLevel.INFO, null, "%s - Starting automatic reconnection", remoteAddress);
		connect();
	}

	private void onLogEvent(ELogLevel level, Exception exception, String message, Object... parameters) {
		notifyObservers(obs -> obs.onLog(new LogEvent(level, String.format(message, parameters), exception)));
	}

	private void onDataReceivedEvent(byte[] buffer, int length) {
		notifyObservers(obs -> obs.onDataReceived(new DataReceivedEvent(buffer, length)));
	}

	private void cancelTimerTask(TimerTask task) {
		if (task != null)
			task.cancel();
	}
}
