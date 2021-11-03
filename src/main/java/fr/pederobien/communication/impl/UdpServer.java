package fr.pederobien.communication.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.function.Supplier;

import fr.pederobien.communication.event.NewTcpClientEvent;
import fr.pederobien.communication.interfaces.IAnswersExtractor;
import fr.pederobien.communication.interfaces.IUdpConnection;

public class UdpServer {
	private int port;
	private Supplier<IAnswersExtractor> extractor;
	private Thread reception;
	private IUdpConnection server;

	public UdpServer(int port, Supplier<IAnswersExtractor> extractor) {
		this.port = port;
		this.extractor = extractor;
		reception = new Thread(() -> server.connect(), String.format("UdpServer_%s", port));
	}

	/**
	 * Creates a server socket associated to the specified port number and starts an internal thread waiting for new clients. When a
	 * new client is connected, a {@link NewTcpClientEvent} is thrown.
	 */
	public void connect() {
		try {
			server = new UdpServerImpl(new InetSocketAddress(port), extractor);
			reception.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Interrupt the internal thread waiting for clients and close the server socket.
	 */
	public void disconnect() {
		server.dispose();
		reception.interrupt();
	}

	/**
	 * @return The UDP connection used to asynchronously send/receive data from the network.
	 */
	public IUdpConnection getConnection() {
		return server;
	}
}
