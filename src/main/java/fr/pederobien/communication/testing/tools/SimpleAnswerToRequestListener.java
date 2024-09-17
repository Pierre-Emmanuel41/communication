package fr.pederobien.communication.testing.tools;

import fr.pederobien.communication.event.RequestReceivedEvent;
import fr.pederobien.communication.impl.connection.Message;
import fr.pederobien.communication.interfaces.IRequestReceivedHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.LogEvent;

public class SimpleAnswerToRequestListener implements IRequestReceivedHandler {
	private String message;
	
	/**
	 * Creates a listener that response to a request received event with the given message.
	 * 
	 * @param message The message to send back to the remote.
	 */
	public SimpleAnswerToRequestListener(String message) {
		this.message = message;
	}

	@Override
	public void onRequestReceivedEvent(RequestReceivedEvent event) {
		String received = new String(event.getData());
		EventManager.callEvent(new LogEvent("Message received: %s", received));
		event.getConnection().answer(event.getIdentifier(), new Message(message.getBytes()));
	}
}
