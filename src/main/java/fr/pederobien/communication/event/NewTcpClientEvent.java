package fr.pederobien.communication.event;

import fr.pederobien.communication.impl.TcpServer;
import fr.pederobien.communication.interfaces.ITcpConnection;

public class NewTcpClientEvent extends ConnectionEvent {
	private TcpServer server;

	/**
	 * Creates an event thrown when a new TCP client is connected to a TCP server.
	 * 
	 * @param connection The client connection.
	 * @param server     The server to which the client is connected.
	 */
	public NewTcpClientEvent(ITcpConnection connection, TcpServer server) {
		super(connection);
		this.server = server;
	}

	@Override
	public ITcpConnection getConnection() {
		return (ITcpConnection) super.getConnection();
	}

	/**
	 * @return The server to which the client is connected.
	 */
	public TcpServer getServer() {
		return server;
	}
}
