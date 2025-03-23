package fr.pederobien.communication.example;

import fr.pederobien.communication.example.client.MyCustomTcpClient;
import fr.pederobien.communication.example.server.MyCustomTcpServer;
import fr.pederobien.utils.event.Logger;

public class Example {

	public static void main(String[] args) {
		// Do display events
		Logger.instance().timeStamp(true).newLine(true).register();

		MyCustomTcpServer server = new MyCustomTcpServer();
		server.open();

		MyCustomTcpClient client = new MyCustomTcpClient();
		client.connect();

		// Connection and data exchange is asynchronous
		// Waiting before closing server/client
		sleep(5000);

		server.close();
		server.dispose();

		sleep(2000);

		client.disconnect();
		client.dispose();
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
