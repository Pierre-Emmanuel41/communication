package fr.pederobien.communication.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import fr.pederobien.communication.EConnectionState;
import fr.pederobien.communication.event.DataReceivedEvent;
import fr.pederobien.communication.event.LogEvent;
import fr.pederobien.communication.event.LogEvent.ELogLevel;
import fr.pederobien.communication.interfaces.IAnswersExtractor;
import fr.pederobien.communication.interfaces.ICallbackRequestMessage;
import fr.pederobien.communication.interfaces.IObsTcpConnection;
import fr.pederobien.communication.interfaces.ITcpConnection;
import fr.pederobien.utils.ByteWrapper;
import fr.pederobien.utils.Observable;
import fr.pederobien.utils.SimpleTimer;

public class TcpClientConnection implements ITcpConnection {
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
	private BlockingQueueTask<ICallbackRequestMessage> sendingQueue;
	private String remoteAddress;
	private int remotePort;
	private Socket socket;
	private boolean isEnabled;
	private RequestResponseManager requestResponseManager;
	private EConnectionState connectionState;
	private AtomicBoolean isDisposed;
	private Observable<IObsTcpConnection> observers;

	public TcpClientConnection(String remoteAddress, int remotePort, IAnswersExtractor answersExtractor, boolean isEnabled) {
		this.remoteAddress = remoteAddress;
		this.remotePort = remotePort;
		this.isEnabled = isEnabled;

		isDisposed = new AtomicBoolean(false);

		requestResponseManager = new RequestResponseManager(this, remoteAddress, answersExtractor);
		observers = new Observable<IObsTcpConnection>();

		timer = new SimpleTimer("TcpClientConnectionTimer_".concat(remoteAddress), true);
		sendingQueue = new BlockingQueueTask<ICallbackRequestMessage>("Sending_".concat(remoteAddress), message -> startSending(message));
		extractingQueue = new BlockingQueueTask<byte[]>("Extracting_".concat(remoteAddress), answer -> startExtracting(answer));

		connectionState = EConnectionState.DISCONNECTED;
	}

	public TcpClientConnection(String remoteAddress, int remotePort, IAnswersExtractor answerExtractor) {
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
	public void send(ICallbackRequestMessage message) {
		checkDisposed();

		if (isEnabled && getState() == EConnectionState.CONNECTED)
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

	/**
	 * @return The observable object associated to this object.
	 */
	public Observable<IObsTcpConnection> getObservers() {
		return observers;
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
				if (getState() == EConnectionState.DISCONNECTING || getState() == EConnectionState.DISCONNECTED)
					return;

				onLogEvent(ELogLevel.WARNING, e, "%s - Reception failure : %s", remoteAddress, e.getMessage());
				onConnectionLostEvent();
				break;
			}
		}
	}

	private void startSending(ICallbackRequestMessage message) {
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

		cancelTimerTask(connection);
		receiving = timer.schedule(() -> startReceiving(), 0);

		sendingQueue.start();
		extractingQueue.start();

		connectionState = EConnectionState.CONNECTED;

		onLogEvent(ELogLevel.INFO, null, "%s - Connection successfull", remoteAddress);

		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			return;
		}

		observers.notifyObservers(obs -> obs.onConnectionComplete());
	}

	private void onConnectionLostEvent() {
		connectionState = EConnectionState.CONNECTION_LOST;
		onLogEvent(ELogLevel.INFO, null, "%s - Connection lost", remoteAddress);

		closeSocket();

		observers.notifyObservers(obs -> obs.onConnectionLost());

		onLogEvent(ELogLevel.INFO, null, "%s - Starting automatic reconnection", remoteAddress);
		connect();
	}

	private void onLogEvent(ELogLevel level, Exception exception, String message, Object... parameters) {
		observers.notifyObservers(obs -> obs.onLog(new LogEvent(level, String.format(message, parameters), exception)));
	}

	private void onDataReceivedEvent(byte[] buffer, int length) {
		observers.notifyObservers(obs -> obs.onDataReceived(new DataReceivedEvent(getAddress(), buffer, length)));
	}

	private void cancelTimerTask(TimerTask task) {
		if (task != null)
			task.cancel();
	}
}
