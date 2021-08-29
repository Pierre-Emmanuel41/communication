package fr.pederobien.communication.event;

import fr.pederobien.communication.interfaces.IConnection;

public class ConnectionLostEvent extends ConnectionEvent {

	/**
	 * Creates a connection lost event.
	 * 
	 * @param connection The connection which is no more connected with its remote.
	 */
	public ConnectionLostEvent(IConnection<?> connection) {
		super(connection);
	}
}
