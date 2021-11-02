package fr.pederobien.communication.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.TimerTask;

import fr.pederobien.communication.EConnectionState;
import fr.pederobien.communication.event.ConnectionCompleteEvent;
import fr.pederobien.communication.event.ConnectionDisposedEvent;
import fr.pederobien.communication.event.ConnectionLogEvent.ELogLevel;
import fr.pederobien.communication.interfaces.IAnswersExtractor;
import fr.pederobien.communication.interfaces.ITcpConnection;
import fr.pederobien.utils.SimpleTimer;
import fr.pederobien.utils.event.EventManager;

public class TcpClientImpl extends TcpImpl implements ITcpConnection {
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

	private String address;
	private int port;

	private SimpleTimer timer;
	private TimerTask receiving, connection;

	public TcpClientImpl(String address, int port, IAnswersExtractor extractor) {
		super(Mode.CLIENT, address, extractor);
		this.address = address;
		this.port = port;

		timer = new SimpleTimer(String.format("TcpClientTimer_%s", address), true);
		setState(EConnectionState.DISCONNECTED);
	}

	@Override
	public void connect() {
		checkDisposed();

		if (!isState(EConnectionState.CONNECTION_LOST) && !isState(EConnectionState.DISCONNECTED))
			return;

		setState(EConnectionState.CONNECTING);
		onLogEvent(this, ELogLevel.INFO, "Starting connection");

		connection = timer.scheduleAtFixedRate(() -> startConnect(), 0, RECONNECTION_IDLE_TIME);
	}

	@Override
	public void disconnect() {
		checkDisposed();

		setState(EConnectionState.DISCONNECTING);

		onLogEvent(this, ELogLevel.INFO, "Closing connection");
		closeSocket();
		cancel(connection);
		cancel(receiving);

		setState(EConnectionState.DISCONNECTED);
		onLogEvent(this, ELogLevel.INFO, "Connection closed");
	}

	@Override
	public void dispose() {
		if (!isState(EConnectionState.DISCONNECTED))
			disconnect();

		if (!setDisposed(true))
			return;

		onLogEvent(this, ELogLevel.INFO, "Disposing connection");

		timer.cancel();
		getSendingQueue().dispose();
		getExtractingQueue().dispose();
		getRequestResponseManager().dispose();

		onLogEvent(this, ELogLevel.INFO, "Connection disposed");
		EventManager.callEvent(new ConnectionDisposedEvent(this));
	}

	@Override
	public String toString() {
		return String.format("[%s %s:%s]", "TcpClient", address, port);
	}

	@Override
	protected void onConnectionLost() {
		super.onConnectionLost();

		onLogEvent(this, ELogLevel.INFO, "Starting automatic reconnection");
		connect();
	}

	private void startConnect() {
		try {
			Socket socket = null;

			try {
				socket = new Socket();
				socket.connect(new InetSocketAddress(InetAddress.getByName(address), port), CONNECTION_TIMEOUT_MS);
				setSocket(socket);

				onConnectionCompleteEvent();
			} catch (SocketTimeoutException e) {
				socket.close();
				onLogEvent(this, ELogLevel.INFO, "Connection timeout. Retry.");
			} catch (IOException e) {
				connection.cancel();
				connection = timer.scheduleAtFixedRate(() -> startConnect(), SOCKET_ERROR_RETRY_MS, RECONNECTION_IDLE_TIME);
			}
		} catch (IOException e) {
			// do nothing
		}
	}

	private void onConnectionCompleteEvent() {
		if (isState(EConnectionState.DISCONNECTING) || isState(EConnectionState.DISCONNECTED))
			return;

		cancel(connection);

		setState(EConnectionState.CONNECTED);
		receiving = timer.schedule(() -> startReceiving(), 0);
		getSendingQueue().start();
		getExtractingQueue().start();

		onLogEvent(this, ELogLevel.INFO, "Connection successfull");

		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			return;
		}

		EventManager.callEvent(new ConnectionCompleteEvent(this));
	}
}
