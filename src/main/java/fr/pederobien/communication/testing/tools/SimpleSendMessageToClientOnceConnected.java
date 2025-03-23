package fr.pederobien.communication.testing.tools;

import fr.pederobien.communication.event.NewClientEvent;
import fr.pederobien.communication.impl.connection.Message;
import fr.pederobien.communication.interfaces.server.IServer;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;

public class SimpleSendMessageToClientOnceConnected implements IEventListener {
	private IServer server;
	private String message;
	private int times;

	/**
	 * Creates a listener to handle a NewClientEvent.
	 * 
	 * @param server  The server from which a NewClientEvent should be handled.
	 * @param message The message to send back to the client.
	 * @param times   The number of time the message should be sent to the client.
	 */
	public SimpleSendMessageToClientOnceConnected(IServer server, String message, int times) {
		this.server = server;
		this.message = message;
		this.times = times;
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
			for (int i = 0; i < times; i++) {
				try {
					Thread.sleep(200);

					if (event.getClient().getConnection().isDisposed()) {
						break;
					}

					event.getClient().getConnection().send(new Message(message.getBytes()));
				} catch (InterruptedException e) {
					// Do nothing
				}
			}
		}
	}
}
