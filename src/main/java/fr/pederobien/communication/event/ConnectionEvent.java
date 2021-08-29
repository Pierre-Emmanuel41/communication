package fr.pederobien.communication.event;

import fr.pederobien.communication.interfaces.IConnection;
import fr.pederobien.utils.event.Event;

public class ConnectionEvent extends Event {
	private IConnection<?> connection;

	/**
	 * Creates a connection event.
	 * 
	 * @param connection The connection source involved in this event.
	 */
	public ConnectionEvent(IConnection<?> connection) {
		this.connection = connection;
	}

	/**
	 * @return The connection involved in this event.
	 */
	public IConnection<?> getConnection() {
		return connection;
	}
}
