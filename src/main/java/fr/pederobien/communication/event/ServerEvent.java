package fr.pederobien.communication.event;

import fr.pederobien.communication.interfaces.server.IServer;

public class ServerEvent extends CommunicationEvent {
	private IServer<?> server;

	/**
	 * Creates a server event.
	 * 
	 * @param server The server involved in this event.
	 */
	public ServerEvent(IServer<?> server) {
		this.server = server;
	}

	/**
	 * @return The server involved in this event.
	 */
	public IServer<?> getServer() {
		return server;
	}
}
