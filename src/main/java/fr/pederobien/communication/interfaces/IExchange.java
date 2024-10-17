package fr.pederobien.communication.interfaces;

import fr.pederobien.communication.interfaces.IConnection.Mode;

public interface IExchange {
	
	/**
	 * Send asynchronously or not (depending on the isSync value) a request to the remote.
	 * 
	 * @param message the message to send to the remote.
	 */
	void send(IMessage message);

	/**
	 * Send asynchronously or not (depending on the isSync value) a request to the remote.
	 * 
	 * @param requestID The identifier of the request to be answered.
	 * @param message The response of the request.
	 */
	void answer(int identifier, IMessage message);

	/**
	 * Wait until data is received from the remote.
	 * 
	 * @return The event associated to the received data.
	 */
	void receive(IUnexpectedRequestHandler handler) throws InterruptedException;

	/**
	 * @return The mode of the underlying connection.
	 */
	Mode getMode();
}
