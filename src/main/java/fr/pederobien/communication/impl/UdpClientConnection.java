package fr.pederobien.communication.impl;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import fr.pederobien.communication.EConnectionState;
import fr.pederobien.communication.event.ConnectionCompleteEvent;
import fr.pederobien.communication.event.ConnectionDisposedEvent;
import fr.pederobien.communication.event.DataReceivedEvent;
import fr.pederobien.communication.event.LogEvent;
import fr.pederobien.communication.event.LogEvent.ELogLevel;
import fr.pederobien.communication.interfaces.IAnswersExtractor;
import fr.pederobien.communication.interfaces.IRequestMessage;
import fr.pederobien.communication.interfaces.IUdpConnection;
import fr.pederobien.utils.BlockingQueueTask;
import fr.pederobien.utils.ByteWrapper;
import fr.pederobien.utils.SimpleTimer;
import fr.pederobien.utils.event.EventManager;

public class UdpClientConnection implements IUdpConnection {
	private SimpleTimer timer;
	private TimerTask receiving;
	private IAnswersExtractor answersExtractor;
	private BlockingQueueTask<DatagramPacket> extractingQueue;
	private BlockingQueueTask<DataReceivedEvent> unexpectedQueue;
	private BlockingQueueTask<IRequestMessage> sendingQueue;
	private DatagramSocket socket;
	private EConnectionState connectionState;
	private AtomicBoolean isDisposed;
	private String remoteAddress;
	private int remotePort;
	private boolean isEnabled;
	private int receptionBufferSize;

	public UdpClientConnection(String remoteAddress, int remotePort, IAnswersExtractor answersExtractor, boolean isEnabled, int receptionBufferSize) {
		this.remoteAddress = remoteAddress;
		this.remotePort = remotePort;
		this.answersExtractor = answersExtractor;
		this.isEnabled = isEnabled;
		this.receptionBufferSize = receptionBufferSize;

		isDisposed = new AtomicBoolean(false);

		timer = new SimpleTimer("UdpClientConnectionTimer_".concat(remoteAddress), true);
		sendingQueue = new BlockingQueueTask<>("Sending_".concat(remoteAddress), message -> startSending(message));
		extractingQueue = new BlockingQueueTask<>("Extracting_".concat(remoteAddress), answer -> startExtracting(answer));
		unexpectedQueue = new BlockingQueueTask<>("UnexpectedData_".concat(remoteAddress), event -> EventManager.callEvent(event));

		connectionState = EConnectionState.DISCONNECTED;
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
		onLogEvent(ELogLevel.INFO, null, "Starting connection");

		startConnect();
	}

	@Override
	public void disconnect() {
		checkDisposed();

		connectionState = EConnectionState.DISCONNECTING;

		onLogEvent(ELogLevel.INFO, null, "Closing connection");
		closeSocket();

		if (receiving != null)
			receiving.cancel();

		connectionState = EConnectionState.DISCONNECTED;
		onLogEvent(ELogLevel.INFO, null, "Connection closed");
	}

	@Override
	public void dispose() {
		if (getState() != EConnectionState.DISCONNECTED)
			disconnect();

		if (!isDisposed.compareAndSet(false, true))
			return;

		onLogEvent(ELogLevel.INFO, null, "Disposing connection");

		timer.cancel();
		sendingQueue.dispose();
		extractingQueue.dispose();
		unexpectedQueue.dispose();

		onLogEvent(ELogLevel.INFO, null, "Connection disposed");
		EventManager.callEvent(new ConnectionDisposedEvent(this));
	}

	@Override
	public boolean isDisposed() {
		return isDisposed.get();
	}

	@Override
	public void send(IRequestMessage message) {
		checkDisposed();

		if (isEnabled && getState() == EConnectionState.CONNECTED)
			sendingQueue.add(message);
	}

	private void startConnect() {
		DatagramSocket socket = null;

		try {
			socket = new DatagramSocket();
			this.socket = socket;

			onConnectionCompleteEvent();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	private void startExtracting(DatagramPacket packet) {
		Map<Integer, byte[]> answers = answersExtractor.extract(packet.getData());
		for (Map.Entry<Integer, byte[]> entry : answers.entrySet())
			unexpectedQueue.add(new DataReceivedEvent(this, (InetSocketAddress) packet.getSocketAddress(), entry.getValue(), entry.getValue().length));
	}

	private void startReceiving() {
		while (connectionState == EConnectionState.CONNECTED) {
			try {
				byte[] buffer = new byte[receptionBufferSize];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet);

				byte[] data = ByteWrapper.wrap(packet.getData()).extract(0, packet.getLength());
				extractingQueue.add(new DatagramPacket(data, data.length, packet.getSocketAddress()));
			} catch (SocketException e) {
				// do nothing
			} catch (IOException e) {
				// do nothing
			}
		}
	}

	private void startSending(IRequestMessage message) {
		try {
			socket.send(new DatagramPacket(message.getBytes(), message.getBytes().length, InetAddress.getByName(remoteAddress), remotePort));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void closeSocket() {
		if (socket == null)
			return;

		socket.close();
	}

	private void checkDisposed() {
		if (isDisposed())
			throw new UnsupportedOperationException("Object disposed");
	}

	private void onConnectionCompleteEvent() {
		if (connectionState == EConnectionState.DISCONNECTING || connectionState == EConnectionState.DISCONNECTED)
			return;

		connectionState = EConnectionState.CONNECTED;
		receiving = timer.schedule(() -> startReceiving(), 0);
		sendingQueue.start();
		extractingQueue.start();
		unexpectedQueue.start();

		onLogEvent(ELogLevel.INFO, null, "Connection successfull");

		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			return;
		}

		EventManager.callEvent(new ConnectionCompleteEvent(this));
	}

	private void onLogEvent(ELogLevel level, Exception exception, String message) {
		EventManager.callEvent(new LogEvent(this, level, String.format("[TcpClient][%s:%s] %s", remoteAddress, remotePort, message), exception));
	}
}
