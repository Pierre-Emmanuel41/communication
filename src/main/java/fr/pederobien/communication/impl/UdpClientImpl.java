package fr.pederobien.communication.impl;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.TimerTask;

import fr.pederobien.communication.EConnectionState;
import fr.pederobien.communication.event.ConnectionCompleteEvent;
import fr.pederobien.communication.event.ConnectionDisposedEvent;
import fr.pederobien.communication.event.ConnectionLogEvent.ELogLevel;
import fr.pederobien.communication.interfaces.IAnswersExtractor;
import fr.pederobien.utils.SimpleTimer;
import fr.pederobien.utils.event.EventManager;

public class UdpClientImpl extends UdpImpl {
	private SimpleTimer timer;
	private TimerTask receiving;

	public UdpClientImpl(String address, int port, IAnswersExtractor extractor) {
		super(Mode.CLIENT, address, port, extractor);
		timer = new SimpleTimer(String.format("UdpClientTimer_%s", address), true);
		setState(EConnectionState.DISCONNECTED);
	}

	@Override
	public void connect() {
		checkDisposed();

		if (!isState(EConnectionState.CONNECTION_LOST) && !isState(EConnectionState.DISCONNECTED))
			return;

		setState(EConnectionState.CONNECTING);
		onLogEvent(this, ELogLevel.INFO, "Starting connection");

		startConnect();
	}

	@Override
	public void disconnect() {
		checkDisposed();

		setState(EConnectionState.DISCONNECTING);

		onLogEvent(this, ELogLevel.INFO, "Closing connection");
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
		getUnexpectedQueue().dispose();

		onLogEvent(this, ELogLevel.INFO, "Connection disposed");
		EventManager.callEvent(new ConnectionDisposedEvent(this));
	}

	private void startConnect() {
		try {
			setSocket(new DatagramSocket());

			onConnectionCompleteEvent();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	private void onConnectionCompleteEvent() {
		if (isState(EConnectionState.DISCONNECTING) || isState(EConnectionState.DISCONNECTED))
			return;

		setState(EConnectionState.CONNECTED);
		receiving = timer.schedule(() -> startReceiving(), 0);
		getSendingQueue().start();
		getExtractingQueue().start();
		getUnexpectedQueue().start();

		onLogEvent(this, ELogLevel.INFO, "Connection successfull");

		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			return;
		}

		EventManager.callEvent(new ConnectionCompleteEvent(this));
	}
}
