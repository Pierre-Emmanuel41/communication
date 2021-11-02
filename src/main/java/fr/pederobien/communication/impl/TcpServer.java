package fr.pederobien.communication.impl;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.function.Supplier;

import fr.pederobien.communication.event.NewTcpClientEvent;
import fr.pederobien.communication.interfaces.IAnswersExtractor;
import fr.pederobien.utils.event.EventManager;

public class TcpServer {
	private int port;
	private Supplier<IAnswersExtractor> extractor;
	private Thread reception;
	private ServerSocket server;

	public TcpServer(int port, Supplier<IAnswersExtractor> extractor) {
		this.port = port;
		this.extractor = extractor;
		reception = new Thread(() -> waitForClient(), String.format("TcpServer_%s", port));
	}

	/**
	 * Creates a server socket associated to the specified port number and starts an internal thread waiting for new clients. When a
	 * new client is connected, a {@link NewTcpClientEvent} is thrown.
	 */
	public void connect() {
		try {
			server = new ServerSocket(port);
			reception.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Interrupt the internal thread waiting for clients and close the server socket.
	 */
	public void disconnect() {
		try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		reception.interrupt();
	}

	private void waitForClient() {
		while (!server.isClosed()) {
			try {
				EventManager.callEvent(new NewTcpClientEvent(new TcpClientServerImpl(server.accept(), extractor.get()), this));
			} catch (IOException e) {
				break;
			}
		}
	}
}
