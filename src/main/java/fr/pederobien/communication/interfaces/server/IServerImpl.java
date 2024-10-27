package fr.pederobien.communication.interfaces.server;

import fr.pederobien.communication.interfaces.connection.IConnection;

public interface IServerImpl {

	/**
	 * Server specific implementation for opening.
	 * 
	 * @param port The port number on which this server should listen for new clients.
	 */
	void openImpl(int port) throws Exception;
	
	/**
	 * Server specific implementation for closing.
	 */
	void closeImpl() throws Exception;
	
	/**
	 * Called in its own thread in order to create a connection with a client.
	 * 
	 * @param config The server configuration that holds connection configuration parameters.
	 */
	IConnection waitForClientImpl(IServerConfig config) throws Exception;
}
