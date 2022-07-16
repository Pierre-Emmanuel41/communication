package fr.pederobien.communication.impl;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Map;

import fr.pederobien.communication.EConnectionState;
import fr.pederobien.communication.event.DataReceivedEvent;
import fr.pederobien.communication.event.UnexpectedDataReceivedEvent;
import fr.pederobien.communication.interfaces.IAddressMessage;
import fr.pederobien.communication.interfaces.IAnswersExtractor;
import fr.pederobien.communication.interfaces.IUdpConnection;
import fr.pederobien.utils.BlockingQueueTask;
import fr.pederobien.utils.ByteWrapper;
import fr.pederobien.utils.event.EventManager;

public abstract class UdpImpl extends ConnectionOperation implements IUdpConnection {
	private Mode mode;
	private String address;
	private int port;
	private IAnswersExtractor extractor;
	private DatagramSocket socket;
	private boolean isEnable;
	private BlockingQueueTask<DatagramPacket> extractingQueue;
	private BlockingQueueTask<DataReceivedEvent> unexpectedQueue;
	private BlockingQueueTask<IAddressMessage> sendingQueue;

	protected UdpImpl(Mode mode, String address, int port, IAnswersExtractor extractor) {
		this.mode = mode;
		this.address = address;
		this.port = port;
		this.extractor = extractor;

		isEnable = true;
		sendingQueue = new BlockingQueueTask<IAddressMessage>("Sending_".concat(address), message -> startSending(message));
		extractingQueue = new BlockingQueueTask<DatagramPacket>("Extracting_".concat(address), answer -> startExtracting(answer));
		unexpectedQueue = new BlockingQueueTask<DataReceivedEvent>("UnexpectedData_".concat(address), event -> EventManager.callEvent(event));
	}

	@Override
	public InetSocketAddress getAddress() {
		return socket == null ? null : mode == Mode.CLIENT ? (InetSocketAddress) socket.getRemoteSocketAddress() : (InetSocketAddress) socket.getLocalSocketAddress();
	}

	@Override
	public boolean isEnable() {
		return isEnable;
	}

	@Override
	public void setEnable(boolean isEnable) {
		this.isEnable = isEnable;
	}

	@Override
	public void send(IAddressMessage message) {
		checkDisposed();

		if (isEnable() && isState(EConnectionState.CONNECTED))
			sendingQueue.add(message);
	}

	@Override
	public String toString() {
		return String.format("[%s %s:%s]", mode == Mode.CLIENT ? "UdpClient" : "UdpServer", address, port);
	}

	/**
	 * @return The queue that extracts answers from a raw bytes array.
	 */
	protected BlockingQueueTask<DatagramPacket> getExtractingQueue() {
		return extractingQueue;
	}

	/**
	 * @return The queue that sends data to the network.
	 */
	protected BlockingQueueTask<IAddressMessage> getSendingQueue() {
		return sendingQueue;
	}

	/**
	 * @return The queue that throws {@link UnexpectedDataReceivedEvent} when data are received.
	 */
	protected BlockingQueueTask<DataReceivedEvent> getUnexpectedQueue() {
		return unexpectedQueue;
	}

	protected void startReceiving() {
		while (isState(EConnectionState.CONNECTED)) {
			try {
				byte[] buffer = new byte[1024];
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

	/**
	 * @return The socket used to send data through the network.
	 */
	protected DatagramSocket getSocket() {
		return socket;
	}

	/**
	 * Set the socket associated to this UDP connection.
	 * 
	 * @param socket The socket used to send data through the network.
	 */
	protected void setSocket(DatagramSocket socket) {
		this.socket = socket;
	}

	protected void closeSocket() {
		if (socket == null)
			return;

		socket.close();
	}

	protected void startExtracting(DatagramPacket packet) {
		Map<Integer, byte[]> answers = extractor.extract(packet.getData());
		for (Map.Entry<Integer, byte[]> entry : answers.entrySet())
			unexpectedQueue.add(new DataReceivedEvent(this, (InetSocketAddress) packet.getSocketAddress(), entry.getValue()));
	}

	private void startSending(IAddressMessage message) {
		try {
			DatagramPacket packet = null;
			if (message.getAddress() != null)
				packet = new DatagramPacket(message.getBytes(), message.getBytes().length, message.getAddress().getAddress(), message.getAddress().getPort());
			else
				packet = new DatagramPacket(message.getBytes(), message.getBytes().length, InetAddress.getByName(address), port);
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
