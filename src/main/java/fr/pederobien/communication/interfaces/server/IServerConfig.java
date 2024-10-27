package fr.pederobien.communication.interfaces.server;

import fr.pederobien.communication.interfaces.IConfiguration;

public interface IServerConfig extends IConfiguration {

	/**
	 * @return The name of the server.
	 */
	String getName();
	

	/**
	 * @return The port number of the server.
	 */
	int getPort();
}
