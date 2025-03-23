package fr.pederobien.communication.impl.server;

import fr.pederobien.communication.event.ConnectionDisposedEvent;
import fr.pederobien.communication.event.ConnectionLostEvent;
import fr.pederobien.communication.event.ConnectionUnstableEvent;
import fr.pederobien.communication.event.ServerCloseEvent;
import fr.pederobien.communication.interfaces.connection.IConnection;
import fr.pederobien.communication.interfaces.server.IClient;
import fr.pederobien.communication.interfaces.server.IServer;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;

public class Client<T> implements IClient, IEventListener {
	private IServer server;
	private IConnection connection;

	/**
	 * Creates a client connected to the remote.
	 * 
	 * @param server     The server from which this client has been created.
	 * @param connection The connection to send/receive data from the remote.
	 */
	public Client(IServer server, IConnection connection) {
		this.server = server;
		this.connection = connection;

		EventManager.registerListener(this);
	}

	@Override
	public IServer getServer() {
		return server;
	}

	@Override
	public IConnection getConnection() {
		return connection;
	}

	@Override
	public String toString() {
		return connection.toString();
	}

	@EventHandler
	private void onConnectionLost(ConnectionLostEvent event) {
		if (event.getConnection() == connection) {
			handleEvent();
		}
	}

	@EventHandler
	private void onConnectionUnstable(ConnectionUnstableEvent event) {
		if (event.getConnection() == connection) {
			handleEvent();
		}
	}

	@EventHandler
	private void onConnectionDisposed(ConnectionDisposedEvent event) {
		if (event.getConnection() == connection) {
			handleEvent();
		}
	}

	@EventHandler
	private void onServerClose(ServerCloseEvent event) {
		handleEvent();
	}

	private void handleEvent() {
		getConnection().setEnabled(false);
		getConnection().dispose();
		EventManager.unregisterListener(this);
	}
}
