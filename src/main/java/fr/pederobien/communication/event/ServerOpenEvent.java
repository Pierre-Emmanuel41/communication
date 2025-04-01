package fr.pederobien.communication.event;

import java.util.StringJoiner;

import fr.pederobien.communication.interfaces.server.IServer;

public class ServerOpenEvent extends ServerEvent {

	/**
	 * Creates a server open event.
	 * 
	 * @param server The opened server.
	 */
	public ServerOpenEvent(IServer server) {
		super(server);
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("server=" + getServer());
		return String.format("%s_%s", getName(), joiner);
	}
}
