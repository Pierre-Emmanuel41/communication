package fr.pederobien.communication.event;

import fr.pederobien.communication.interfaces.IConnection;

public class ConnectionEvent extends CommunicationEvent {
	private IConnection connection;

	/**
	 * Creates a connection event.
	 * 
	 * @param connection The connection source involved in this event.
	 */
	public ConnectionEvent(IConnection connection) {
		this.connection = connection;
	}

	/**
	 * @return The connection involved in this event.
	 */
	public IConnection getConnection() {
		return connection;
	}
}
