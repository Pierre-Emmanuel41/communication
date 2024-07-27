package fr.pederobien.communication.impl;

import java.util.ArrayList;
import java.util.List;

import fr.pederobien.communication.event.ConnectionUnstableEvent;
import fr.pederobien.communication.event.NewClientEvent;
import fr.pederobien.communication.event.ServerUnstableEvent;
import fr.pederobien.communication.interfaces.IConnection;
import fr.pederobien.communication.interfaces.IServer;
import fr.pederobien.communication.interfaces.IServerConfig;
import fr.pederobien.utils.BlockingQueueTask;
import fr.pederobien.utils.Disposable;
import fr.pederobien.utils.IDisposable;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;
import fr.pederobien.utils.event.LogEvent;

public abstract class Server implements IServer {
	private static final int MAX_EXCEPTION_NUMBER = 10;

	private IServerConfig config;
	private boolean isOpened; 
	private BlockingQueueTask<Object> clientQueue;
	private List<IConnection> connections;
	private IDisposable disposable;
	private ConnectionListener listener;
	private int newClientExceptionCounter;
	
	/**
	 * Creates a server.
	 * 
	 * @param config The object that holds the server configuration.
	 */
	protected Server(IServerConfig config) {
		this.config = config;
		
		clientQueue = new BlockingQueueTask<Object>(String.format("%s[WaitForClient]", toString()), object -> waitForClient(object));
		connections = new ArrayList<IConnection>();
		disposable = new Disposable();
		listener = new ConnectionListener();
		
		newClientExceptionCounter = 0;
		
	}

	@Override
	public void open() {
		disposable.checkDisposed();

		try {
			onLogEvent("Opening server");

			openImpl(config.getPort());
			
			isOpened = true;

			listener.start();
			clientQueue.add(new Object());
			clientQueue.start();

			onLogEvent("Server opened");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		disposable.checkDisposed();

		try {
			onLogEvent("Closing server");

			listener.stop();

			connections.forEach(connection -> connection.dispose());

			closeImpl();

			isOpened = false;
			
			onLogEvent("Server closed");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void dispose() {
		if (disposable.dispose()) {
			close();
			clientQueue.dispose();
		}
	}

	@Override
	public boolean isOpened() {
		return isOpened;
	}
	
	@Override
	public String toString() {
		return String.format("[%s *:%s]", config.getName(), config.getPort());
	}
	
	/**
	 * Server specific implementation for opening.
	 * 
	 * @param port The port number on which this server should listen for new clients.
	 */
	protected abstract void openImpl(int port) throws Exception;
	
	/**
	 * Server specific implementation for closing.
	 */
	protected abstract void closeImpl() throws Exception;
	
	/**
	 * Called in its own thread in order to create a connection with a client.
	 * 
	 * @param config The server configuration that holds connection configuration parameters.
	 */
	protected abstract IConnection waitForClientImpl(IServerConfig config) throws Exception;
	
	/**
	 * Throw a LogEvent.
	 * 
	 * @param message The message of the event.
	 */
	protected void onLogEvent(String message) {
		EventManager.callEvent(new LogEvent("%s - %s", toString(), message));
	}
	
	private void waitForClient(Object object) {
		try {
			IConnection connection = waitForClientImpl(config);

			if (!isOpened())
				return;
			
			connection.initialise();
			
			// Adding connections to a list in order to close the remaining opened connection when the server is closed.
			connections.add(connection);
			
			EventManager.callEvent(new NewClientEvent(connection, this));
			
			newClientExceptionCounter = 0;
		} catch (Exception e) {
			newClientExceptionCounter++;
			if (newClientExceptionCounter == MAX_EXCEPTION_NUMBER) {
				onLogEvent("Too much exceptions in a row, closing server");
				EventManager.callEvent(new ServerUnstableEvent(this));
			}
		} finally {
			// Wait for another client if the server is opened.
			if (isOpened())
				clientQueue.add(object);
		}
	}
	
	private class ConnectionListener implements IEventListener {
		
		/**
		 * Start monitoring the underlying connection.
		 */
		public void start() {
			EventManager.registerListener(this);
		}
		
		/**
		 * Stop monitoring the underlying connection.
		 */
		public void stop() {
			EventManager.unregisterListener(this);
		}
		
		@EventHandler
		private void onConnectionUnstable(ConnectionUnstableEvent event) {
			for (IConnection connection : connections)
				if (connection == event.getConnection())
					connection.dispose();
		}
	}
}
