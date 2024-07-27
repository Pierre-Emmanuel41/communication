package fr.pederobien.communication.interfaces;

public interface IClientConfig extends ICommonConfig {

	/**
	 * @return The IP address of the server.
	 */
	String getAddress();
	
	/**
	 * @return The port number of the server.
	 */
	int getPort();
	
	/**
	 * @return The value considered as a timeout in ms the client tries to connect to a server.
	 */
	int getConnectionTimeout();
	
	/**
	 * @return True if the client should try to reconnect automatically with the server if an error occurred.
	 */
	boolean isAutomaticReconnection();
	
	/**
	 * @return The delay in ms before trying to reconnect to the server.
	 */
	int getReconnectionDelay();
}