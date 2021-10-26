package fr.pederobien.communication.event;

import java.util.StringJoiner;

import fr.pederobien.communication.interfaces.IConnection;

public class ConnectionDisposedEvent extends ConnectionEvent {

	/**
	 * Creates a connection disposed event.
	 * 
	 * @param connection The connection that is now disposed.
	 */
	public ConnectionDisposedEvent(IConnection<?> connection) {
		super(connection);
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("connection=" + getConnection());
		return String.format("%s_%s", getName(), joiner);
	}
}
