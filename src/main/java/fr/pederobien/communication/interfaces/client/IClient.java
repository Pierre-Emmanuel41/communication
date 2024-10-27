package fr.pederobien.communication.interfaces.client;

import fr.pederobien.communication.interfaces.connection.IConnection;

public interface IClient {
	
	public enum EState {
		/**
		 * Trying to connect to remote following a Connect() command, or following a disconnection event.
		 **/
		CONNECTING,

		/**
		 * Connection is existing with remote.
		 **/
		CONNECTED,

		/**
		 * Trying to disconnect from remote following a Disconnect() command, or following a disconnection event.
		 **/
		DISCONNECTING,

		/**
		 * No connection is existing with remote. No request to perform any connection sent.
		 **/
		DISCONNECTED,

		/**
		 * The client is disposed and cannot be used anymore.
		 */
		DISPOSED
	}

	/**
	 * The implementation shall try establishing the connection only when this method is called. The class is expected to retry
	 * establishing the connection as long as Disconnected() is not called. Timeout may be reported in event LogEvent.
	 *
	 * @return true if the client is in correct state to start the connection to the remote.
	 */
	boolean connect();

	/**
	 * Close the connection to the remote.
	 *
	 * @return true if the client is in correct state to close the connection, false otherwise.
	 */
	boolean disconnect();

	/**
	 * Dispose this connection. After this, it is impossible to send data to the remote using this connection.
	 *
	 * @return true if the client has been disposed, false otherwise.
	 */
	boolean dispose();

	/**
	 * @return True if the connection is disposed and cannot be used any more.
	 */
	boolean isDisposed();
	
	/**
	 * @return The IP address of the server.
	 */
	String getAddress();
	
	/**
	 * @return The port number of the server.
	 */
	int getPort();

	/**
	 * @return The current state of the client.
	 */
	EState getState();
	
	/**
	 * @return The connection to send messages to the remote.
	 */
	IConnection getConnection();
}
