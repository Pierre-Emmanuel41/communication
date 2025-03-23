package fr.pederobien.communication.event;

import fr.pederobien.communication.interfaces.client.IClient;

public class ClientEvent extends CommunicationEvent {
	private IClient client;

	/**
	 * Creates a client event.
	 * 
	 * @param client The client involved in this event.
	 */
	public ClientEvent(IClient client) {
		this.client = client;
	}

	/**
	 * @return The client involved in this event.
	 */
	public IClient getClient() {
		return client;
	}
}
