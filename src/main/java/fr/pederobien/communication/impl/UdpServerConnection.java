package fr.pederobien.communication.impl;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import fr.pederobien.communication.EConnectionState;
import fr.pederobien.communication.NonBlockingConsole;
import fr.pederobien.communication.event.DataReceivedEvent;
import fr.pederobien.communication.event.LogEvent;
import fr.pederobien.communication.event.LogEvent.ELogLevel;
import fr.pederobien.communication.interfaces.IAddressMessage;
import fr.pederobien.communication.interfaces.IAnswersExtractor;
import fr.pederobien.communication.interfaces.IObsConnection;
import fr.pederobien.communication.interfaces.IUdpServerConnection;
import fr.pederobien.utils.Observable;
import fr.pederobien.utils.SimpleTimer;

public class UdpServerConnection implements IUdpServerConnection {
	private SimpleTimer timer;
	private TimerTask receiving;
	private Supplier<IAnswersExtractor> extractorSupplier;
	private Map<InetSocketAddress, IAnswersExtractor> extractors;
	private BlockingQueueTask<DatagramPacket> extractingQueue;
	private BlockingQueueTask<DataReceivedEvent> unexpectedQueue;
	private BlockingQueueTask<IAddressMessage> sendingQueue;
	private DatagramSocket socket;
	private EConnectionState connectionState;
	private AtomicBoolean isDisposed;
	private String remoteAddress;
	private Observable<IObsConnection> observers;

	public UdpServerConnection(InetSocketAddress address, Supplier<IAnswersExtractor> extractorSupplier) throws SocketException {
		socket = new DatagramSocket(address.getPort());

		this.extractorSupplier = extractorSupplier;

		extractors = new HashMap<InetSocketAddress, IAnswersExtractor>();

		remoteAddress = address.getAddress().toString().substring(1);

		isDisposed = new AtomicBoolean(false);
		observers = new Observable<IObsConnection>();

		timer = new SimpleTimer("ServerConnection", true);
		receiving = timer.schedule(() -> startReceiving(), 0);

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
	public void send(IAddressMessage message) {
		checkDisposed();

		if (getState() == EConnectionState.CONNECTED)
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

	private void startExtracting(DatagramPacket answer) {
		IAnswersExtractor extractor = extractors.get(answer.getSocketAddress());
		if (extractor == null) {
			extractor = extractorSupplier.get();
			extractors.put((InetSocketAddress) answer.getSocketAddress(), extractor);
		}
		Map<Integer, byte[]> answers = extractor.extract(answer.getData());
		for (Map.Entry<Integer, byte[]> entry : answers.entrySet())
			unexpectedQueue.add(new DataReceivedEvent((InetSocketAddress) answer.getSocketAddress(), entry.getValue(), entry.getValue().length));
	}

	private void startReceiving() {
		while (!isDisposed()) {
			try {
				byte[] buffer = new byte[1024];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet);

				extractingQueue.add(packet);
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
	}

	private void startReceivingUnexpectedData(DataReceivedEvent event) {
		onDataReceivedEvent(event.getBuffer(), event.getLength());
	}

	private void startSending(IAddressMessage message) {
		try {
			socket.send(new DatagramPacket(message.getBytes(), message.getBytes().length, message.getAddress().getAddress(), message.getAddress().getPort()));
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

	private void onLogEvent(ELogLevel level, Exception exception, String message, Object... parameters) {
		NonBlockingConsole.println(new LogEvent(level, String.format(message, parameters), exception));
	}

	private void onDataReceivedEvent(byte[] buffer, int length) {
		observers.notifyObservers(obs -> obs.onDataReceived(new DataReceivedEvent(getAddress(), buffer, length)));
	}
}
