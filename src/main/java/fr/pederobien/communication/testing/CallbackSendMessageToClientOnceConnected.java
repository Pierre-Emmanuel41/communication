package fr.pederobien.communication.testing;

import java.util.function.Consumer;

import fr.pederobien.communication.event.NewClientEvent;
import fr.pederobien.communication.impl.connection.CallbackMessage;
import fr.pederobien.communication.interfaces.ICallbackMessage.CallbackArgs;
import fr.pederobien.communication.interfaces.IServer;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;

public class CallbackSendMessageToClientOnceConnected implements IEventListener {
	private IServer server;
	private Consumer<CallbackArgs> callback;
	
	public CallbackSendMessageToClientOnceConnected(IServer server, Consumer<CallbackArgs> callback) {
		this.server = server;
		this.callback = callback;
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
	private void onNewClientEvent(NewClientEvent event) {
		if (event.getServer() == server)
			event.getConnection().send(new CallbackMessage("You are connected".getBytes(), 1000, callback));
	}
}
