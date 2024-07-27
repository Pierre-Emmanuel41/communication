package fr.pederobien.communication.interfaces;

import fr.pederobien.communication.event.RequestReceivedEvent;

public interface IRequestReceivedHandler {

	/**
	 * Called when an unexpected request has been received from the remote.
	 * 
	 * @param event The event that contains the unexpected request.
	 */
	void onRequestReceivedEvent(RequestReceivedEvent event);
}
