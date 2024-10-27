package fr.pederobien.communication.interfaces.server;

import fr.pederobien.communication.interfaces.IConfiguration;
import fr.pederobien.communication.interfaces.connection.IConnection.Mode;

public interface IServerConfig extends IConfiguration {

	@Override
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
