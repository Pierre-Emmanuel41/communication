package fr.pederobien.communication.event;

import java.util.StringJoiner;

import fr.pederobien.communication.interfaces.server.IServer;

public class ServerDisposeEvent extends ServerEvent {

	/**
	 * Creates a server dispose event.
	 * 
	 * @param server The disposed server.
	 */
	public ServerDisposeEvent(IServer server) {
		super(server);
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("server=" + getServer());
		return String.format("%s_%s", getName(), joiner);
	}
}
