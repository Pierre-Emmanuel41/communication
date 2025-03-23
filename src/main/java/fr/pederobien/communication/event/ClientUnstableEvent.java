package fr.pederobien.communication.event;

import java.util.StringJoiner;

import fr.pederobien.communication.interfaces.client.IClient;

public class ClientUnstableEvent extends ClientEvent {

	/**
	 * Create a connection initialisation failure event.
	 * 
	 * @param client The client involved in this event.
	 */
	public ClientUnstableEvent(IClient client) {
		super(client);
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("client=" + getClient());
		return String.format("%s_%s", getName(), joiner);
	}
}
