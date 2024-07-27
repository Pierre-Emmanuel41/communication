package fr.pederobien.communication.interfaces;

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
		 * The connection with the remote has been lost.
		 **/
		CONNECTION_LOST
	}

	/**
	 * The implementation shall try establishing the connection only when this method is called. The class is expected to retry
	 * establishing the connection as long as Disconnected() is not called. Timeout may be reported in event LogEvent.
	 */
	void connect();

	/**
	 * Abort the connection to the remote.
	 */
	void disconnect();

	/**
	 * Dispose this connection. After this, it is impossible to send data to the remote using this connection.
	 */
	void dispose();

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
	 * @return The value considered as a timeout in ms the client tries to connect to a server.
	 */
	int getConnectionTimeout();
	
	/**
	 * @return The delay in ms before trying to reconnect to the server.
	 */
	int getReconnectionDelay();

	/**
	 * @return The current state of the client.
	 */
	EState getState();
	
	/**
	 * @return The connection to send messages to the remote.
	 */
	IConnection getConnection();
}
