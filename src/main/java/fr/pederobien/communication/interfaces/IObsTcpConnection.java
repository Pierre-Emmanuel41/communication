package fr.pederobien.communication.interfaces;

import fr.pederobien.communication.event.DataReceivedEvent;
import fr.pederobien.communication.event.LogEvent;
import fr.pederobien.communication.event.UnexpectedDataReceivedEvent;

public interface IObsTcpConnection {

	/**
	 * This event shall be raised when the implementation detects that the connection started by Connect() method is complete.
	 */
	void onConnectionComplete();

	/**
	 * This event shall be raised when the implementation detects the dispose of the connection to the remote.
	 */
	void onConnectionDisposed();

	/**
	 * This event shall be raised when the implementation detects the loss of the connection to the remote.
	 */
	void onConnectionLost();

	/**
	 * The implementation of shall raise this event each time a message has been received and transmit the buffer and its length as
	 * argument.
	 * 
	 * @param event The event that contains informations about received data.
	 */
	void onDataReceived(DataReceivedEvent event);

	/**
	 * This event may be used by implementation to report catched error, info or debug info.
	 * 
	 * @param event The log event.
	 */
	void onLog(LogEvent event);

	/**
	 * The implementation of shall raise this event each time a message has been received but was not expected by the connection. This
	 * means that the received data does not correspond to a pending request.
	 * 
	 * @param event The event that contains information about received data.
	 */
	void onUnexpectedDataReceived(UnexpectedDataReceivedEvent event);
}
