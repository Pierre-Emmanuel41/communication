package fr.pederobien.communication.event;

import java.util.StringJoiner;

import fr.pederobien.communication.interfaces.server.IClient;

public class NewClientEvent extends ServerEvent {
	private IClient client;

	/**
	 * Creates an event thrown when a new client is connected to a server.
	 * 
	 * @param client The client involved in this event.
	 */
	public NewClientEvent(IClient client) {
		super(client.getServer());
		this.client = client;
	}

	/**
	 * @return The client involved in this event.
	 */
	public IClient getClient() {
		return client;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(", ", "{", "}");
		joiner.add("server=" + getServer());
		joiner.add("remote=" + getClient());
		return String.format("%s_%s", getName(), joiner);
	}
}
