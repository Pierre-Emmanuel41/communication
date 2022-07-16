package fr.pederobien.communication.impl;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import fr.pederobien.communication.EConnectionState;
import fr.pederobien.communication.event.ConnectionDisposedEvent;
import fr.pederobien.communication.event.ConnectionLogEvent.ELogLevel;
import fr.pederobien.communication.event.DataReceivedEvent;
import fr.pederobien.communication.interfaces.IAnswersExtractor;
import fr.pederobien.utils.event.EventManager;

public class UdpServerImpl extends UdpImpl {
	private Supplier<IAnswersExtractor> extractor;
	private Map<SocketAddress, IAnswersExtractor> extractors;

	protected UdpServerImpl(InetSocketAddress address, Supplier<IAnswersExtractor> extractor) throws SocketException {
		super(Mode.SERVER, address.getAddress().getHostAddress(), address.getPort(), null);
		this.extractor = extractor;
		extractors = new HashMap<SocketAddress, IAnswersExtractor>();

		setSocket(new DatagramSocket(address.getPort()));
		setState(EConnectionState.CONNECTED);
		getSendingQueue().start();
		getExtractingQueue().start();
		getUnexpectedQueue().start();
	}

	@Override
	public void connect() {
		startReceiving();
	}

	@Override
	public void disconnect() {
		checkDisposed();

		setState(EConnectionState.DISCONNECTING);

		onLogEvent(this, ELogLevel.INFO, "Closing connection");
		closeSocket();

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

		getSendingQueue().dispose();
		getExtractingQueue().dispose();
		getUnexpectedQueue().dispose();

		onLogEvent(this, ELogLevel.INFO, "Connection disposed");
		EventManager.callEvent(new ConnectionDisposedEvent(this));
	}

	@Override
	protected void startExtracting(DatagramPacket packet) {
		IAnswersExtractor answerExtractor = extractors.get(packet.getSocketAddress());
		if (answerExtractor == null) {
			answerExtractor = extractor.get();
			extractors.put(packet.getSocketAddress(), answerExtractor);
		}

		Map<Integer, byte[]> answers = answerExtractor.extract(packet.getData());
		for (Map.Entry<Integer, byte[]> entry : answers.entrySet())
			getUnexpectedQueue().add(new DataReceivedEvent(this, (InetSocketAddress) packet.getSocketAddress(), entry.getValue()));
	}
}
