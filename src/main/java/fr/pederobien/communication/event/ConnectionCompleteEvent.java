package fr.pederobien.communication.event;

import java.util.StringJoiner;

import fr.pederobien.communication.interfaces.IConnection;

public class ConnectionCompleteEvent extends ConnectionEvent {

	/**
	 * Creates a connection complete event.
	 * 
	 * @param connection The connection that is now connection with its remote.
	 */
	public ConnectionCompleteEvent(IConnection<?> connection) {
		super(connection);
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("connection=" + getConnection());
		return "ConnectionCompleteEvent_" + joiner.toString();
	}
}
