package fr.pederobien.communication.impl;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.TimerTask;

import fr.pederobien.communication.EConnectionState;
import fr.pederobien.communication.event.ConnectionDisposedEvent;
import fr.pederobien.communication.event.ConnectionLogEvent.ELogLevel;
import fr.pederobien.communication.interfaces.IAnswersExtractor;
import fr.pederobien.utils.SimpleTimer;
import fr.pederobien.utils.event.EventManager;

public class TcpClientServerImpl extends TcpImpl {
	private SimpleTimer timer;
	private TimerTask receiving;

	/**
	 * Creates a connection with a remote.
	 * 
	 * @param socket           The underlying socket created by a {@link ServerSocket} to send/receive data with the remote.
	 * @param answersExtractor An object responsible to extract several answers from a bytes buffer.
	 * @param ignoreTimeout    True in order to ignore remote answers whose the requests have thrown a timeout.
	 */
	public TcpClientServerImpl(Socket socket, IAnswersExtractor extractor, boolean ignoreTimeout) {
		super(socket.getLocalAddress().getHostAddress(), extractor, ignoreTimeout);
		setSocket(socket);
		setState(EConnectionState.CONNECTED);

		timer = new SimpleTimer("TcpServerTimer_".concat(socket.getInetAddress().getHostAddress()), true);
		receiving = timer.schedule(() -> startReceiving(), 0);

		getSendingQueue().start();
		getExtractingQueue().start();
	}

	@Override
	public void connect() {
		throw new IllegalStateException();
	}

	@Override
	public void disconnect() {
		checkDisposed();

		setState(EConnectionState.DISCONNECTING);

		onLogEvent(this, ELogLevel.INFO, "closing connection");
		closeSocket();

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
		return String.format("[%s %s:%s]", "TcpClientServer", getSocket().getLocalAddress().getHostAddress(), getSocket().getPort());
	}
}
