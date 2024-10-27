package fr.pederobien.communication.interfaces.server;

import fr.pederobien.communication.interfaces.connection.IConnection;

public interface IClient {

	/**
	 * @return The server from which this client has been created.
	 */
	IServer getServer();
	
	/**
	 * @return The connection to send/receive data from the remote.
	 */
	IConnection getConnection();
}
