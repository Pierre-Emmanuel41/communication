package fr.pederobien.communication.testing.tools;

import java.util.function.Consumer;

import fr.pederobien.communication.event.NewClientEvent;
import fr.pederobien.communication.interfaces.server.IServer;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;

public class DoOnceConnected implements IEventListener {
	private IServer server;
	private Consumer<NewClientEvent> action;

	/**
	 * Creates a listener to handle a NewClientEvent.
	 * 
	 * @param server  The server from which a NewClientEvent should be handled.
	 * @param message The message to send back to the client.
	 * @param times   The number of time the message should be sent to the client.
	 */
	public DoOnceConnected(IServer server, Consumer<NewClientEvent> action) {
		this.server = server;
		this.action = action;
	}

	/**
	 * Start listening event bus to trigger {@link NewClientEvent}.
	 */
	public void start() {
		EventManager.registerListener(this);
	}

	/**
	 * Unregister this event listener.
	 */
	public void stop() {
		EventManager.unregisterListener(this);
	}

	@EventHandler
	private void onNewClientEvent(NewClientEvent event) throws InterruptedException {
		if (event.getServer() == server) {
			action.accept(event);
		}
	}
}
