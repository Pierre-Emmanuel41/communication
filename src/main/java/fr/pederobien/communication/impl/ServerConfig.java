package fr.pederobien.communication.impl;

import fr.pederobien.communication.interfaces.connection.IConnection.Mode;
import fr.pederobien.communication.interfaces.server.IServerConfig;

public class ServerConfig extends Configuration implements IServerConfig {
	private String name;
	private int port;

	/**
	 * Creates a configuration that holds the parameters for a server.
	 * 
	 * @param name The server's name.
	 * @param port The server port number.
	 */
	protected ServerConfig(String name, int port) {
		super(Mode.SERVER_TO_CLIENT);

		this.name = name;
		this.port = port;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getPort() {
		return port;
	}
}
