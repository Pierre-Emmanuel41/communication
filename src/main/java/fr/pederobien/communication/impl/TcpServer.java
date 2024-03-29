package fr.pederobien.communication.impl;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.function.Supplier;

import fr.pederobien.communication.event.NewTcpClientEvent;
import fr.pederobien.communication.interfaces.IAnswersExtractor;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.LogEvent;

public class TcpServer {
	private String name;
	private int port;
	private Supplier<IAnswersExtractor> extractor;
	private Thread reception;
	private ServerSocket server;
	private boolean ignoreTimeout, isConnected;

	/**
	 * Creates a not connected UDP server.
	 * 
	 * @param name          The server name.
	 * @param port          The server port.
	 * @param extractor     The supplier use to handle requests received from the client.
	 * @param ignoreTimeout True in order to ignore remote answers whose the requests have thrown a timeout.
	 */
	public TcpServer(String name, int port, Supplier<IAnswersExtractor> extractor, boolean ignoreTimeout) {
		this.name = name;
		this.port = port;
		this.extractor = extractor;
		this.ignoreTimeout = ignoreTimeout;
		reception = new Thread(() -> waitForClient(), String.format("[TcpServer] %s_*:%s", name, port));
	}

	/**
	 * Creates a server socket associated to the specified port number and starts an internal thread waiting for new clients. When a
	 * new client is connected, a {@link NewTcpClientEvent} is thrown.
	 */
	public void connect() {
		try {
			server = new ServerSocket(port);
			EventManager.callEvent(new LogEvent("Starting %s TCP server on *:%s", name, port));
			reception.start();
			isConnected = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Interrupt the internal thread waiting for clients and close the server socket.
	 */
	public void disconnect() {
		try {
			EventManager.callEvent(new LogEvent("Stopping %s TCP Server on *:%s", name, port));
			server.close();
			isConnected = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
		reception.interrupt();
	}

	/**
	 * @return True if this server is waiting for client, false otherwise.
	 */
	public boolean isConnected() {
		return isConnected;
	}

	private void waitForClient() {
		while (!server.isClosed()) {
			try {
				EventManager.callEvent(new NewTcpClientEvent(new TcpClientServerImpl(server.accept(), extractor.get(), ignoreTimeout), this));
			} catch (IOException e) {
				break;
			}
		}
	}

	/**
	 * @return The server's name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return The server's port number.
	 */
	public int getPort() {
		return port;
	}
}
