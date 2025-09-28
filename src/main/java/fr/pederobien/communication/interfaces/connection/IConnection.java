package fr.pederobien.communication.interfaces.connection;

import fr.pederobien.communication.interfaces.IMessageHandler;

public interface IConnection {

	/**
	 * First step for initializing this connection. This method is called before trying to send a message.
	 *
	 * @return True if this connection is successfully initialized, false otherwise.
	 */
	boolean initialise() throws Exception;

	/**
	 * Set the handler to call when an unexpected message is received from the remote.
	 *
	 * @param handler The handler to call.
	 */
	void setMessageHandler(IMessageHandler handler);

	/**
	 * Send asynchronously a request to the remote.
	 *
	 * @param message the message to send to the remote.
	 */
	void send(IMessage message);

	/**
	 * Send asynchronously a request to the remote.
	 *
	 * @param requestID The identifier of the request to be answered.
	 * @param message   The response of the request.
	 */
	void answer(int requestID, IMessage message);

	/**
	 * @return True if the connection can send data to the remote or not. It is independent of the connection state with the remote.
	 */
	boolean isEnabled();

	/**
	 * Set if this connection can send data to the remote or not. If this connection is not allowed to send data to the remote, then
	 * calling method {@link #send(IMessage)} will not stores the message.
	 *
	 * @param isEnabled True to allow this connection to send data to remote.
	 */
	void setEnabled(boolean isEnabled);

	/**
	 * Close this connection definitely. It cannot be used anymore.
	 */
	void dispose();

	/**
	 * @return true if this connection has been disposed and cannot be used anymore, false otherwise.
	 */
	boolean isDisposed();

	enum Mode {
		CLIENT_TO_SERVER, SERVER_TO_CLIENT
	}
}
