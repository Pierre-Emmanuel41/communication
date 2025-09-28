package fr.pederobien.communication.impl.server.state;

public interface IState {

	/**
	 * Set if this state is enabled or disabled.
	 *
	 * @param isEnabled True if enabled, false otherwise.
	 */
	void setEnabled(boolean isEnabled);

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
