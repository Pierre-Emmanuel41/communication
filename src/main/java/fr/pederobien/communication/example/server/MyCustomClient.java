package fr.pederobien.communication.example.server;

import fr.pederobien.communication.event.MessageEvent;
import fr.pederobien.communication.impl.connection.Message;
import fr.pederobien.communication.interfaces.connection.IConnection;
import fr.pederobien.communication.interfaces.server.IClient;
import fr.pederobien.utils.event.Logger;

public class MyCustomClient {
	private IClient tcpClient;

	public MyCustomClient(IClient tcpClient) {
		this.tcpClient = tcpClient;
	}

	/**
	 * @return The connection to the remote
	 */
	public IConnection getConnection() {
		return tcpClient.getConnection();
	}

	/**
	 * Method called when the remote has sent an unexpected message
	 * 
	 * @param event The event that contains the message sent by the remote.
	 */
	public void handle(MessageEvent event) {
		String received = new String(event.getData());
		Logger.info("[Server] Received %s", received);

		if (received.equals("I expect a response")) {
			String message = "Here is your response";
			Logger.info("[Server] Sending \"%s\" to client", message);
			Message response = new Message(message.getBytes());
			tcpClient.getConnection().answer(event.getIdentifier(), response);
		}
	}
}
