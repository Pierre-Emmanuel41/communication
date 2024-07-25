package fr.pederobien.communication.impl;

public class UdpServer {
	/*
	private String name;
	private int port;
	private Supplier<IAnswersExtractor> extractor;
	private Thread reception;
	private IUdpConnection server;
	private boolean isConnected;
	*/

	/**
	 * Creates a not connected UDP server.
	 * 
	 * @param name      The server name.
	 * @param port      The server port.
	 * @param extractor The supplier use to handle requests received from the client.
	 */
	public UdpServer() {
		/*
		this.name = name;
		this.port = port;
		this.extractor = extractor;
		reception = new Thread(() -> server.connect(), String.format("[UdpServer] %s_*:%s", name, port));
		*/
	}

	/**
	 * Creates a server socket associated to the specified port number and starts an internal thread waiting for new clients. When a
	 * new client is connected, a {@link NewTcpClientEvent} is thrown.
	 */
	public void connect() {
		/*
		try {
			server = new UdpServerImpl(new InetSocketAddress(port), extractor);
			EventManager.callEvent(new LogEvent("Starting %s UDP server on *:%s", name, port));
			reception.start();
			isConnected = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
	}

	/**
	 * Interrupt the internal thread waiting for clients and close the server socket.
	 */
	public void disconnect() {
		/*
		EventManager.callEvent(new LogEvent("Stopping %s UDP Server on *:%s", name, port));
		server.dispose();
		reception.interrupt();
		isConnected = false;
		*/
	}
}
