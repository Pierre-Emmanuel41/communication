package fr.pederobien.communication.interfaces;

public interface IExchange {
	
	/**
	 * The implementation shall send the provided data **asynchronously**.
	 * That is to say that the method is not expecting to block any time.
	 * Error sending data may be reported with the event LogEvent.
	 * 
	 * @param message the message to send to the remote.
	 */
	void send(ICallbackMessage message);

	/**
	 * Wait until data is received from the remote.
	 * 
	 * @return The event associated to the received data.
	 */
	void receive(IRequestReceivedHandler handler) throws InterruptedException;
}
