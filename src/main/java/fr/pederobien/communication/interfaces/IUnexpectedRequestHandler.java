package fr.pederobien.communication.interfaces;

import fr.pederobien.communication.event.MessageEvent;

public interface IUnexpectedRequestHandler {

	/**
	 * Method called when an unexpected request is received from the remote.
	 * 
	 * @param event The event that contains the unexpected request.
	 */
	void handle(MessageEvent event);
}
