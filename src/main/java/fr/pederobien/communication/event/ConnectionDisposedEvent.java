package fr.pederobien.communication.event;

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
}
