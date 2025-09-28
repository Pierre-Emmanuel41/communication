package fr.pederobien.communication.event;

import fr.pederobien.communication.interfaces.connection.IConnection;
import fr.pederobien.communication.interfaces.server.IServer;

import java.util.StringJoiner;

public class NewClientEvent extends ServerEvent {
	private final IConnection connection;

	/**
	 * Creates an event thrown when a new client is connected to a server. The connection is already monitored by the server so that
	 * if the connection with the remote is lost or if it is unstable, The connection will be automatically closed.
	 *
	 * @param server     The server involved in this event.
	 * @param connection The connection with the remote.
	 */
	public NewClientEvent(IServer server, IConnection connection) {
		super(server);
		this.connection = connection;
	}

	/**
	 * @return The connection with the remote.
	 */
	public IConnection getConnection() {
		return connection;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(", ", "{", "}");
		joiner.add("server=" + getServer());
		joiner.add("remote=" + getConnection());
		return String.format("%s_%s", getName(), joiner);
	}
}
