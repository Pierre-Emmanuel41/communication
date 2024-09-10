package fr.pederobien.communication.interfaces;

public interface IExchange {
	
	/**
	 * Send asynchronously a request to the remote.
	 * 
	 * @param message the message to send to the remote.
	 */
	void send(IMessage message);

	/**
	 * Send asynchronously or not (depending on the isSync value) a request to the remote.
	 * 
	 * @param message the message to send to the remote.
	 */
	void send(ICallbackMessage message);

	/**
	 * Send asynchronously or not (depending on the isSync value) a request to the remote.
	 * 
	 * @param requestID The identifier of the request to be answered.
	 * @param message The response of the request.
	 */
	void answer(int identifier, IMessage message);

	/**
	 * Send asynchronously or not (depending on the isSync value) a request to the remote.
	 * 
	 * @param identifier The identifier of the request to be answered.
	 * @param message The response of the request.
	 */
	void answer(int identifier, ICallbackMessage message);

	/**
	 * Wait until data is received from the remote.
	 * 
	 * @return The event associated to the received data.
	 */
	void receive(IRequestReceivedHandler handler) throws InterruptedException;
}
