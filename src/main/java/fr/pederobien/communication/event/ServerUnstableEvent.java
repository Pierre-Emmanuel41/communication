package fr.pederobien.communication.event;

import java.util.StringJoiner;

import fr.pederobien.communication.interfaces.server.IServer;

public class ServerUnstableEvent extends ServerEvent {

	/**
	 * Create a server unstable event.
	 * 
	 * @param server The server involved in this event.
	 */
	public ServerUnstableEvent(IServer server) {
		super(server);
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("server=" + getServer());
		return String.format("%s_%s", getName(), joiner);
	}
}
