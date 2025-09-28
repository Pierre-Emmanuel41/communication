package fr.pederobien.communication.event;

import fr.pederobien.communication.interfaces.connection.IConnection;

import java.util.StringJoiner;

public class ConnectionLostEvent extends ConnectionEvent {

	/**
	 * Creates a connection lost event.
	 *
	 * @param connection The connection which is no more connected with its remote.
	 */
	public ConnectionLostEvent(IConnection connection) {
		super(connection);
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("remote=" + getConnection());
		return String.format("%s_%s", getName(), joiner);
	}
}
