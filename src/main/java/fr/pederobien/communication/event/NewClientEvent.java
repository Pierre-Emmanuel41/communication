package fr.pederobien.communication.event;

import java.util.StringJoiner;

import fr.pederobien.communication.interfaces.IConnection;
import fr.pederobien.communication.interfaces.IServer;

public class NewClientEvent extends ConnectionEvent {
	private IServer server;

	/**
	 * Creates an event thrown when a new TCP client is connected to a TCP server.
	 * 
	 * @param connection The client connection.
	 * @param server     The server to which the client is connected.
	 */
	public NewClientEvent(IConnection connection, IServer server) {
		super(connection);
		this.server = server;
	}

	/**
	 * @return The server to which the client is connected.
	 */
	public IServer getServer() {
		return server;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(", ", "{", "}");
		joiner.add("server=" + getServer());
		joiner.add("connection=" + getConnection());
		return String.format("%s_%s", getName(), joiner);
	}
}
