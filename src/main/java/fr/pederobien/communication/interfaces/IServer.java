package fr.pederobien.communication.interfaces;

public interface IServer {

	/**
	 * Start the server and wait for a client to be connected.
	 */
	void open();
	
	/**
	 * Stop the server, dispose the connection with each client.
	 */
	void close();
	
	/**
	 * Dispose this server. It cannot be used anymore.
	 */
	void dispose();
	
	/**
	 * @return True if the server is opened and is waiting for a client.
	 */
	boolean isOpened();
}
