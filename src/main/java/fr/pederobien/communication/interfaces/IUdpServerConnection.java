package fr.pederobien.communication.interfaces;

public interface IUdpServerConnection extends IConnection {

	/**
	 * The implementation shall send the provided data **asynchronously**. That is to say that the method is not expecting to block
	 * any time. Error sending data may be reported with the event LogEvent.
	 * 
	 * @param message the message to send to the remote.
	 */
	void send(IAddressMessage message);

}
