package fr.pederobien.communication.interfaces;

public interface IServerConfig extends ICommonConfig {

	/**
	 * @return The name of the server.
	 */
	String getName();
	

	/**
	 * @return The port number of the server.
	 */
	int getPort();
}
