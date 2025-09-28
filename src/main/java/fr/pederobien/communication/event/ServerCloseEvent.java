package fr.pederobien.communication.event;

import fr.pederobien.communication.interfaces.server.IServer;

import java.util.StringJoiner;

public class ServerCloseEvent extends ServerEvent {

	/**
	 * Creates a server close event.
	 *
	 * @param server The closed server.
	 */
	public ServerCloseEvent(IServer server) {
		super(server);
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("server=" + getServer());
		return String.format("%s_%s", getName(), joiner);
	}
}
