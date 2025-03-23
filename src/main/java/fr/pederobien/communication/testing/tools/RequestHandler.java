package fr.pederobien.communication.testing.tools;

import java.util.function.Consumer;

import fr.pederobien.communication.event.MessageEvent;
import fr.pederobien.communication.interfaces.IMessageHandler;

public class RequestHandler implements IMessageHandler {
	private Consumer<MessageEvent> action;

	/**
	 * Creates a request handler that run some code when a request has been received
	 * from the remote.
	 * 
	 * @param action The action to perform when a request has been received.
	 */
	public RequestHandler(Consumer<MessageEvent> action) {
		this.action = action;
	}

	@Override
	public void handle(MessageEvent event) {
		action.accept(event);
	}
}
