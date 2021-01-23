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
import fr.pederobien.communication.NonBlockingConsole;
import fr.pederobien.communication.event.DataReceivedEvent;
import fr.pederobien.communication.event.LogEvent;
import fr.pederobien.communication.event.LogEvent.ELogLevel;
import fr.pederobien.communication.interfaces.IAnswersExtractor;
import fr.pederobien.communication.interfaces.IObsConnection;
import fr.pederobien.communication.interfaces.IRequestMessage;
import fr.pederobien.communication.interfaces.IUdpConnection;
import fr.pederobien.utils.ByteWrapper;
import fr.pederobien.utils.Observable;
import fr.pederobien.utils.SimpleTimer;

public class UdpClientConnection implements IUdpConnection {
	private SimpleTimer timer;
	private TimerTask receiving;
	private IAnswersExtractor answersExtractor;
	private BlockingQueueTask<byte[]> extractingQueue;
	private BlockingQueueTask<DataReceivedEvent> unexpectedQueue;
	private BlockingQueueTask<IRequestMessage> sendingQueue;
	private DatagramSocket socket;
	private EConnectionState connectionState;
	private AtomicBoolean isDisposed;
	private String remoteAddress;
	private int remotePort;
	private boolean isEnabled;
	private Observable<IObsConnection> observers;

	public UdpClientConnection(String remoteAddress, int remotePort, IAnswersExtractor answersExtractor, boolean isEnabled) {
		this.remoteAddress = remoteAddress;
		this.remotePort = remotePort;
		this.answersExtractor = answersExtractor;
		this.isEnabled = isEnabled;

		isDisposed = new AtomicBoolean(false);
		observers = new Observable<IObsConnection>();

		sendingQueue = new BlockingQueueTask<>("Sending_".concat(remoteAddress), message -> startSending(message));
		extractingQueue = new BlockingQueueTask<>("Extracting_".concat(remoteAddress), answer -> startExtracting(answer));
		unexpectedQueue = new BlockingQueueTask<>("UnexpectedData_".concat(remoteAddress), event -> startReceivingUnexpectedData(event));

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
		onLogEvent(ELogLevel.INFO, null, "%s - Starting connection", remoteAddress);

		timer = new SimpleTimer("ClientConnection", true);
		startConnect();
	}

	@Override
	public void disconnect() {
		checkDisposed();

		connectionState = EConnectionState.DISCONNECTING;

		onLogEvent(ELogLevel.INFO, null, "%s - Closing connection", remoteAddress);
		closeSocket();

		if (receiving != null)
			receiving.cancel();

		connectionState = EConnectionState.DISCONNECTED;
		onLogEvent(ELogLevel.INFO, null, "%s - Connection closed", remoteAddress);
	}

	@Override
	public void dispose() {
		if (getState() != EConnectionState.DISCONNECTED)
			disconnect();

		if (!isDisposed.compareAndSet(false, true))
			return;

		onLogEvent(ELogLevel.INFO, null, "%s - Disposing connection", remoteAddress);

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
	public void send(IRequestMessage message) {
		checkDisposed();

		if (isEnabled || getState() == EConnectionState.CONNECTED)
			sendingQueue.add(message);
	}

	@Override
	public void addObserver(IObsConnection obs) {
		observers.addObserver(obs);
	}

	@Override
	public void removeObserver(IObsConnection obs) {
		observers.removeObserver(obs);
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

	private void startExtracting(byte[] answer) {
		Map<Integer, byte[]> answers = answersExtractor.extract(answer);
		for (Map.Entry<Integer, byte[]> entry : answers.entrySet())
			unexpectedQueue.add(new DataReceivedEvent(getAddress(), entry.getValue(), entry.getValue().length));
	}

	private void startReceiving() {
		while (!isDisposed()) {
			try {
				byte[] buffer = new byte[1024];
				DatagramPacket packet = new DatagramPacket(buffer, 1024);
				socket.receive(packet);

				extractingQueue.add(ByteWrapper.wrap(packet.getData()).extract(0, packet.getLength()));
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
	}

	private void startReceivingUnexpectedData(DataReceivedEvent event) {
		onDataReceivedEvent(event.getBuffer(), event.getLength());
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

		receiving = timer.schedule(() -> startReceiving(), 0);

		sendingQueue.start();
		extractingQueue.start();
		unexpectedQueue.start();

		connectionState = EConnectionState.CONNECTED;

		onLogEvent(ELogLevel.INFO, null, "%s - Connection successfull", remoteAddress);

		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			return;
		}

		observers.notifyObservers(obs -> obs.onConnectionComplete());
	}

	private void onLogEvent(ELogLevel level, Exception exception, String message, Object... parameters) {
		NonBlockingConsole.println(new LogEvent(level, String.format(message, parameters), exception));
	}

	private void onDataReceivedEvent(byte[] buffer, int length) {
		observers.notifyObservers(obs -> obs.onDataReceived(new DataReceivedEvent(getAddress(), buffer, length)));
	}
}
