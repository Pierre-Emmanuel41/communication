package fr.pederobien.communication.impl.server;

import fr.pederobien.communication.event.ServerCloseEvent;
import fr.pederobien.communication.impl.connection.ConnectionListener;
import fr.pederobien.communication.interfaces.connection.IConnection;
import fr.pederobien.communication.interfaces.server.IClient;
import fr.pederobien.communication.interfaces.server.IServer;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;

public class Client implements IClient, IEventListener {
	private IServer server;
	private IConnection connection;
	private ConnectionListener listener;
	
	/**
	 * Creates a client connected to the remote.
	 * 
	 * @param server The server from which this client has been created.
	 * @param connection The connection to send/receive data from the remote.
	 */
	public Client(IServer server, IConnection connection) {
		this.server = server;
		this.connection = connection;
		
		listener = new ConnectionListener(getConnection());
		listener.setOnConnectionUnstable(ignored -> getConnection().dispose());
		listener.setOnConnectionLost(ignored -> getConnection().dispose());
		listener.start();
		
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
	private void onServerClose(ServerCloseEvent event) {
		getConnection().dispose();
		
		listener.stop();
	}
}
