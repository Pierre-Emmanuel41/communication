package fr.pederobien.communication.testing.tools;

import fr.pederobien.communication.event.NewClientEvent;
import fr.pederobien.communication.interfaces.IMessageHandler;
import fr.pederobien.communication.interfaces.server.IServer;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;

import java.util.function.Consumer;

public class ServerListener implements IEventListener {
	private final IServer server;
	private Consumer<NewClientEvent> onNewClientConnected;
	private IMessageHandler handler;

	/**
	 * Creates a listener to handle a NewClientEvent.
	 *
	 * @param server The server from which a NewClientEvent should be handled.
	 */
	public ServerListener(IServer server) {
		this.server = server;
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

	/**
	 * Set the action to apply when a new client is connected to the remote.
	 *
	 * @param onNewClientConnected The action to apply when a new client is connected.
	 */
	public void setActionOnNewClientConnected(Consumer<NewClientEvent> onNewClientConnected) {
		this.onNewClientConnected = onNewClientConnected;
	}

	/**
	 * Set the action to apply when an unexpected message has been received from the remote.
	 *
	 * @param handler The handler to apply.
	 */
	public void setMessageHandler(IMessageHandler handler) {
		this.handler = handler;
	}

	@EventHandler
	private void onNewClientEvent(NewClientEvent event) throws InterruptedException {
		if (event.getServer() == server) {
			if (onNewClientConnected != null)
				onNewClientConnected.accept(event);
			if (handler != null)
				event.getConnection().setMessageHandler(handler);
		}
	}
}
