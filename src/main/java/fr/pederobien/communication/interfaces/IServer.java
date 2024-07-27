package fr.pederobien.communication.interfaces;

public interface IServer {
	
	public enum EState {
		/**
		 * Trying to open the server following an open() command.
		 */
		OPENING,
		
		/**
		 * The server is opened.
		 */
		OPENED,
		
		/**
		 * Trying to close the server following a close() command.
		 */
		CLOSING,
		
		/**
		 * The server is closed.
		 */
		CLOSED
	}

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
	 * @return The current state of the server.
	 */
	EState getState();
}
