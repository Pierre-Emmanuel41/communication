package fr.pederobien.communication.interfaces;

import fr.pederobien.communication.event.RequestReceivedEvent;

public interface IUnexpectedRequestHandler {

	/**
	 * Method called when an unexpected request is received from the remote.
	 * 
	 * @param event The event that contains the unexpected request.
	 */
	void onUnexpectedRequestReceived(RequestReceivedEvent event);
}
