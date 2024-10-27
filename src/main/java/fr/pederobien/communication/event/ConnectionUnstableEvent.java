package fr.pederobien.communication.event;

import java.util.StringJoiner;

import fr.pederobien.communication.interfaces.connection.IConnection;

public class ConnectionUnstableEvent extends ConnectionEvent {

	/**
	 * Create a connection unstable event.
	 * 
	 * @param connection The unstable connection.
	 */
	public ConnectionUnstableEvent(IConnection connection) {
		super(connection);
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("connection=" + getConnection());
		return String.format("%s_%s", getName(), joiner);
	}
}
