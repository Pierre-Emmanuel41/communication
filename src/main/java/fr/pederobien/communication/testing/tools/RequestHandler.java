package fr.pederobien.communication.testing.tools;

import java.util.function.Consumer;

import fr.pederobien.communication.event.RequestReceivedEvent;
import fr.pederobien.communication.interfaces.IUnexpectedRequestHandler;

public class RequestHandler implements IUnexpectedRequestHandler {
	private Consumer<RequestReceivedEvent> action;

	/**
	 * Creates a request handler that run some code when a request has been received
	 * from the remote.
	 * 
	 * @param action The action to perform when a request has been received.
	 */
	public RequestHandler(Consumer<RequestReceivedEvent> action) {
		this.action = action;
	}

	@Override
	public void handle(RequestReceivedEvent event) {
		action.accept(event);
	}
}
