package fr.pederobien.communication.event;

import java.util.StringJoiner;

import fr.pederobien.communication.interfaces.IClient;

public class ClientConnectedEvent extends ClientEvent {

	/**
	 * Creates a client connected event.
	 * 
	 * @param client The client connected to the remote.
	 */
	public ClientConnectedEvent(IClient client) {
		super(client);
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("client=" + getClient());
		return String.format("%s_%s", getName(), joiner);
	}
}
