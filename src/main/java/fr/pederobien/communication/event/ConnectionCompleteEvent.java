package fr.pederobien.communication.event;

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
}
