package fr.pederobien.communication.example.client;

import fr.pederobien.communication.event.MessageEvent;
import fr.pederobien.communication.impl.ClientConfig;
import fr.pederobien.communication.impl.Communication;
import fr.pederobien.communication.impl.EthernetEndPoint;
import fr.pederobien.communication.impl.layer.AesLayerInitializer;
import fr.pederobien.communication.interfaces.IEthernetEndPoint;
import fr.pederobien.communication.interfaces.client.IClient;
import fr.pederobien.communication.interfaces.connection.IConnection;
import fr.pederobien.communication.testing.tools.SimpleCertificate;

public class MyCustomTcpClient {
	private IClient tcpClient;

	public MyCustomTcpClient() {
		IEthernetEndPoint endPoint = new EthernetEndPoint("127.0.0.1", 12345);
		ClientConfig<IEthernetEndPoint> config = Communication.createClientConfig("My TCP client", endPoint);

		// Set the code to execute when a message has been received
		config.setMessageHandler(event -> onMessageReceived(event));

		// Setting the layer to use to pack/unpack data.
		// A new layer is defined each time a new client is connected
		config.setLayerInitializer(() -> new AesLayerInitializer(new SimpleCertificate()));

		// If the connection unstable counter reach 10, the connection will be
		// closed automatically
		config.setConnectionMaxUnstableCounter(10);

		// Decrement the value of the connection unstable counter each 100 ms
		config.setConnectionHealTime(100);

		// Time in ms after which a timeout occurs when trying to connect to the server
		config.setConnectionTimeout(5000);

		// The connection wait 1000 ms before retrying to connect with the server
		config.setReconnectionDelay(1000);

		// Value by default is true
		config.setAutomaticReconnection(false);

		// If the client unstable counter reach 2, the connection will be
		// closed automatically and the client will close it self.
		config.setClientMaxUnstableCounter(2);

		// Decrement the value of the client unstable counter each 5 ms
		config.setClientHealTime(5);

		// Creating the client
		tcpClient = Communication.createTcpClient(config);
	}

	/**
	 * Attempt the connection with the remote asynchronously
	 */
	public void connect() {
		tcpClient.connect();
	}

	/**
	 * Close the connection with the remote, by calling {@link #connect()} the
	 * client can be connected again with the server.
	 */
	public void disconnect() {
		tcpClient.disconnect();
	}

	/**
	 * Definitely close the connection with the server.
	 */
	public void dispose() {
		tcpClient.dispose();
	}

	/**
	 * @return The connection to the server.
	 */
	public IConnection getConnection() {
		return tcpClient.getConnection();
	}

	private void onMessageReceived(MessageEvent event) {
		if (event.getConnection() != tcpClient.getConnection()) {
			return;
		}

		// Write here what to do when an unexpected message is received from the server
	}
}
