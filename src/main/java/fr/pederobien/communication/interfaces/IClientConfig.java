package fr.pederobien.communication.interfaces;

import fr.pederobien.communication.interfaces.IConnection.Mode;

public interface IClientConfig extends IConfiguration {

	@Override
	default Mode getMode() {
		return Mode.CLIENT_TO_SERVER;
	}

	/**
	 * @return The IP address of the server.
	 */
	String getAddress();
	
	/**
	 * @return The port number of the server.
	 */
	int getPort();
	
	/**
	 * @return The value considered as a timeout in ms the client tries to connect to a server. The default value is
	 *         1000 ms.
	 */
	int getConnectionTimeout();
	
	/**
	 * @return True if the client should try to reconnect automatically with the server if an error occurred.
	 *         The default value is true.
	 */
	boolean isAutomaticReconnection();
	
	/**
	 * @return The delay in ms before trying to reconnect to the server. The default value is 1000 ms.
	 */
	int getReconnectionDelay();
	
	/**
	 * An unstable connection event is thrown if an exception is thrown 10 times in a row.
	 * It can be from the send, receive, extract, callback or dispatcher method. The maximum counter value
	 * corresponds to the maximum number of time a connection unstable event is thrown before stopping
	 * the automatic reconnection if is is enabled. The default value is 5, which allowing up to 50 exceptions
	 * in a row to be thrown before stopping automatic reconnection.
	 * 
	 * @return The maximum number of time a connection unstable before stopping the automatic reconnection.
	 */
	int getMaxUnstableCounterValue();
}