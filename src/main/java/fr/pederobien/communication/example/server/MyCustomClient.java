package fr.pederobien.communication.example.server;

import fr.pederobien.communication.event.MessageEvent;
import fr.pederobien.communication.impl.connection.Message;
import fr.pederobien.communication.interfaces.connection.IConnection;
import fr.pederobien.utils.event.Logger;

public class MyCustomClient {
	private IConnection connection;

	public MyCustomClient(IConnection connection) {
		this.connection = connection;

		connection.setMessageHandler(event -> onMessageReceived(event));
	}

	/**
	 * @return The connection to the remote
	 */
	public IConnection getConnection() {
		return connection;
	}

	/**
	 * Handler when unexpected message has been received from the remote.
	 * 
	 * @param event The event that contains remote message
	 */
	private void onMessageReceived(MessageEvent event) {
		String received = new String(event.getData());
		Logger.info("[Server] Received %s", received);

		if (received.equals("I expect a response")) {
			String message = "Here is your response";
			Logger.info("[Server] Sending \"%s\" to client", message);
			Message response = new Message(message.getBytes());

			// event.getConnection() is equivalent to call connection class member directly
			event.getConnection().answer(event.getIdentifier(), response);
		}
	}
}
