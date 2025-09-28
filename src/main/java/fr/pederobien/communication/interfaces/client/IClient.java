package fr.pederobien.communication.interfaces.client;

import fr.pederobien.communication.interfaces.connection.IConnection;

public interface IClient {

	/**
	 * The implementation shall try establishing the connection only when this method is called. The class is expected to retry
	 * establishing the connection as long as Disconnected() is not called. Timeout may be reported in event LogEvent.
	 */
	void connect();

	/**
	 * Close the connection to the remote.
	 */
	void disconnect();

	/**
	 * Dispose this connection. After this, it is impossible to send data to the remote using this connection.
	 */
	void dispose();

	/**
	 * @return True if the connection is disposed and cannot be used anymore.
	 */
	boolean isDisposed();

	/**
	 * @return The connection to send messages to the remote.
	 */
	IConnection getConnection();
}
