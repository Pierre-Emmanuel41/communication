package fr.pederobien.communication.interfaces;

import fr.pederobien.communication.interfaces.IConnection.Mode;

public interface IServerConfig extends IConfiguration {
	
	/**
	 * @return The connection mode.
	 */
	default Mode getMode() {
		return Mode.SERVER_TO_CLIENT;
	}

	/**
	 * @return The name of the server.
	 */
	String getName();
	

	/**
	 * @return The port number of the server.
	 */
	int getPort();
}
