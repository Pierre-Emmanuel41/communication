package fr.pederobien.communication.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.function.Supplier;

import fr.pederobien.communication.event.NewTcpClientEvent;
import fr.pederobien.communication.interfaces.IAnswersExtractor;
import fr.pederobien.communication.interfaces.IUdpConnection;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.LogEvent;

public class UdpServer {
	private String name;
	private int port;
	private Supplier<IAnswersExtractor> extractor;
	private Thread reception;
	private IUdpConnection server;

	/**
	 * Creates a not connected UDP server.
	 * 
	 * @param name      The server name.
	 * @param port      The server port.
	 * @param extractor The supplier use to handle requests received from the client.
	 */
	public UdpServer(String name, int port, Supplier<IAnswersExtractor> extractor) {
		this.name = name;
		this.port = port;
		this.extractor = extractor;
		reception = new Thread(() -> server.connect(), String.format("[UdpServer] %s_*:%s", name, port));
	}

	/**
	 * Creates a server socket associated to the specified port number and starts an internal thread waiting for new clients. When a
	 * new client is connected, a {@link NewTcpClientEvent} is thrown.
	 */
	public void connect() {
		try {
			server = new UdpServerImpl(new InetSocketAddress(port), extractor);
			EventManager.callEvent(new LogEvent("Starting %s UDP server on *:%s", name, port));
			reception.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Interrupt the internal thread waiting for clients and close the server socket.
	 */
	public void disconnect() {
		EventManager.callEvent(new LogEvent("Stopping %s UDP Server on *:%s", name, port));
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
