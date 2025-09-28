package fr.pederobien.communication.impl.client.state;

import fr.pederobien.communication.interfaces.connection.IConnection;

public interface IContext {

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
	 * @return True if the context has been disposed, false otherwise.
	 */
	boolean isDisposed();

	/**
	 * @return The connection connected to the remote.
	 */
	IConnection getConnection();
}
