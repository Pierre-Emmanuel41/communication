package fr.pederobien.communication.impl;

public abstract class UdpImpl {
	/*
	private Mode mode;
	private String address;
	private int port;
	private IAnswersExtractor extractor;
	private DatagramSocket socket;
	private boolean isEnable;
	private BlockingQueueTask<DatagramPacket> extractingQueue;
	private BlockingQueueTask<DataReceivedEvent> unexpectedQueue;
	private BlockingQueueTask<IAddressMessage> sendingQueue;
	*/

	protected UdpImpl() {
		/*
		this.mode = mode;
		this.address = address;
		this.port = port;
		this.extractor = extractor;

		isEnable = true;
		sendingQueue = new BlockingQueueTask<IAddressMessage>("Sending_".concat(address), message -> startSending(message));
		extractingQueue = new BlockingQueueTask<DatagramPacket>("Extracting_".concat(address), answer -> startExtracting(answer));
		unexpectedQueue = new BlockingQueueTask<DataReceivedEvent>("UnexpectedData_".concat(address), event -> EventManager.callEvent(event));
		*/
	}

	/*
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
*/

	/*
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
	*/
	
	/*

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
	*/
}
