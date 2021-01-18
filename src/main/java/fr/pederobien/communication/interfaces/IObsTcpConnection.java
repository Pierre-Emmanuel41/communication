package fr.pederobien.communication.interfaces;

import fr.pederobien.communication.event.UnexpectedDataReceivedEvent;

public interface IObsTcpConnection extends IObsConnection {

	/**
	 * This event shall be raised when the implementation detects the loss of the connection to the remote.
	 */
	void onConnectionLost();

	/**
	 * The implementation of shall raise this event each time a message has been received but was not expected by the connection. This
	 * means that the received data does not correspond to a pending request.
	 * 
	 * @param event The event that contains information about received data.
	 */
	void onUnexpectedDataReceived(UnexpectedDataReceivedEvent event);
}
