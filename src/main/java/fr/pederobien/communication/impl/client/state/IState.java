package fr.pederobien.communication.impl.client.state;

public interface IState {

	/**
	 * Set if this state is enabled or disabled.
	 *
	 * @param isEnabled True if enabled, false otherwise.
	 */
	void setEnabled(boolean isEnabled);

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
}
