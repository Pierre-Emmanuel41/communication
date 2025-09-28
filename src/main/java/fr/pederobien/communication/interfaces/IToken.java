package fr.pederobien.communication.interfaces;

import fr.pederobien.communication.event.MessageEvent;
import fr.pederobien.communication.interfaces.connection.IConnection.Mode;
import fr.pederobien.communication.interfaces.connection.IMessage;

public interface IToken {

	/**
	 * @return The direction of the communication
	 */
	Mode getMode();

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
	 * @return Block until unexpected data has been received from the remote.
	 */
	MessageEvent receive() throws InterruptedException;

	/**
	 * Close this token, it cannot be used anymore.
	 */
	void dispose();
}
