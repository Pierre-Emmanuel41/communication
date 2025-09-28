package fr.pederobien.communication.interfaces.server;

public interface IServer {

	/**
	 * Start the server and wait for a client to be connected.
	 *
	 * @return true if the server is in correct state to be opened, false otherwise.
	 */
	boolean open();

	/**
	 * Stop the server, dispose the connection with each client.
	 *
	 * @return true if the server is in correct state to be closed, false otherwise.
	 */
	boolean close();

	/**
	 * Dispose this server. It cannot be used anymore.
	 *
	 * @return true if the has been disposed, false otherwise.
	 */
	boolean dispose();
}
