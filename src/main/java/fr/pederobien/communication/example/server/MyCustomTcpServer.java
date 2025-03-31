package fr.pederobien.communication.example.server;

import java.util.HashMap;
import java.util.Map;

import fr.pederobien.communication.event.NewClientEvent;
import fr.pederobien.communication.impl.Communication;
import fr.pederobien.communication.impl.EthernetEndPoint;
import fr.pederobien.communication.impl.ServerConfig;
import fr.pederobien.communication.impl.layer.AesLayerInitializer;
import fr.pederobien.communication.interfaces.IEthernetEndPoint;
import fr.pederobien.communication.interfaces.connection.IConnection;
import fr.pederobien.communication.interfaces.server.IServer;
import fr.pederobien.communication.testing.tools.SimpleCertificate;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;

public class MyCustomTcpServer implements IEventListener {
	private IServer server;
	private Object lock;
	private Map<IConnection, MyCustomClient> clients;

	public MyCustomTcpServer() {
		IEthernetEndPoint endPoint = new EthernetEndPoint(12345);
		ServerConfig<IEthernetEndPoint> config = Communication.createServerConfig("My TCP server", endPoint);

		// Setting the layer to use to pack/unpack data.
		// A new layer is defined each time a new client is connected
		config.setLayerInitializer(() -> new AesLayerInitializer(new SimpleCertificate()));

		// If the unstable counter reach 10, the connection will be automatically closed
		config.setConnectionMaxUnstableCounter(10);

		// Decrement the value of the unstable counter each 100 ms
		config.setConnectionHealTime(100);

		// Validate or not if a client is allowed to be connected to the server
		config.setClientValidator(remoteEndPoint -> validateClient(remoteEndPoint));

		// If the server unstable counter reach 2, the server will be
		// closed automatically as well as each client currently connected.
		config.setServerMaxUnstableCounter(2);

		// Decrement the value of the server unstable counter each 5 ms
		config.setServerHealTime(5);

		// Creating the server
		server = Communication.createTcpServer(config);

		lock = new Object();
		clients = new HashMap<IConnection, MyCustomClient>();

		// To know when a new client is connected to the server
		EventManager.registerListener(this);
	}

	/**
	 * Start the server and wait for a client to be connected.
	 *
	 * @return true if the server is in correct state to be opened, false otherwise.
	 */
	public boolean open() {
		return server.open();
	}

	/**
	 * Stop the server, dispose the connection with each client.
	 *
	 * @return true if the server is in correct state to be closed, false otherwise.
	 */
	public boolean close() {
		return server.close();
	}

	/**
	 * Dispose this server. It cannot be used anymore.
	 *
	 * @return true if the has been disposed, false otherwise.
	 */
	public boolean dispose() {
		return server.dispose();
	}

	/**
	 * Indicates if the client defined by the given end point is allowed to connect
	 * with the server.
	 * 
	 * @param endPoint The remote end-point.
	 * 
	 * @return True if the client is allowed, false otherwise.
	 */
	private boolean validateClient(IEthernetEndPoint endPoint) {
		// Dummy criteria to check client end-point
		if (endPoint.getAddress() == "127.0.0.2") {
			return false;
		}

		return true;
	}

	@EventHandler
	private void onNewClient(NewClientEvent event) {
		if (event.getServer() != server) {
			return;
		}

		MyCustomClient client = new MyCustomClient(event.getConnection());
		synchronized (lock) {
			clients.put(client.getConnection(), client);
		}
	}
}
