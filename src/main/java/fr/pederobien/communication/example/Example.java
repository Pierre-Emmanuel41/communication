package fr.pederobien.communication.example;

import fr.pederobien.communication.example.client.MyCustomTcpClient;
import fr.pederobien.communication.example.server.MyCustomTcpServer;
import fr.pederobien.communication.impl.connection.Message;
import fr.pederobien.communication.interfaces.connection.IMessage;
import fr.pederobien.utils.event.Logger;

public class Example {

	public static void main(String[] args) {
		// Do display events
		Logger.instance().timeStamp(true).newLine(true);

		MyCustomTcpServer server = new MyCustomTcpServer();
		server.open();

		MyCustomTcpClient client = new MyCustomTcpClient();
		client.connect();

		// Waiting for the client to be connected with the server
		sleep(1000);

		// The content of this function shall be call when the ClientConnectedEvent is
		// thrown
		demoMessageExchange(client);

		sleep(2000);

		server.close();
		server.dispose();

		sleep(500);

		client.disconnect();
		client.dispose();

		sleep(1000);
	}

	private static void demoMessageExchange(MyCustomTcpClient client) {
		// Sending a simple message
		String message = "Hello World !";
		Logger.info("[Client] Sending %s", message);
		client.getConnection().send(new Message(message.getBytes()));

		try {
			Thread.sleep(1000);
		} catch (Exception e) {
			// Do nothing
		}

		message = "I expect a response";
		Logger.info("[Client] Sending %s", message);
		IMessage callback = new Message(message.getBytes(), arguments -> {
			if (!arguments.isTimeout()) {
				Logger.info("[Client] Received %s", new String(arguments.getResponse()));
			} else {
				Logger.error("Unexpected timeout occurs");
			}
		});
		client.getConnection().send(callback);
	}

	private static void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
