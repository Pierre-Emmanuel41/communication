package fr.pederobien.communication.impl.server;

import fr.pederobien.communication.event.ConnectionLostEvent;
import fr.pederobien.communication.event.ConnectionUnstableEvent;
import fr.pederobien.communication.event.NewClientEvent;
import fr.pederobien.communication.event.ServerCloseEvent;
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
import fr.pederobien.utils.event.LogEvent.ELogLevel;

public abstract class Server implements IServer {
	private static final int MAX_EXCEPTION_NUMBER = 10;

	private IServerConfig config;
	private EState state; 
	private BlockingQueueTask<Object> clientQueue;
	private IDisposable disposable;
	private int newClientExceptionCounter;
	
	/**
	 * Creates a server.
	 * 
	 * @param config The object that holds the server configuration.
	 */
	protected Server(IServerConfig config) {
		this.config = config;
		
		clientQueue = new BlockingQueueTask<Object>(String.format("%s[WaitForClient]", toString()), object -> waitForClient(object));
		disposable = new Disposable();
		
		newClientExceptionCounter = 0;
		state = EState.CLOSED;
	}

	@Override
	public boolean open() {
		disposable.checkDisposed();

		if (state != EState.CLOSED)
			return false;

		try {
			state = EState.OPENING;
			onLogEvent("Opening server");

			openImpl(config.getPort());

			state = EState.OPENED;
			onLogEvent("Server opened");

			clientQueue.add(new Object());
			clientQueue.start();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@Override
	public boolean close() {
		disposable.checkDisposed();

		if (state != EState.OPENED)
			return false;

		try {
			state = EState.CLOSING;
			onLogEvent("Closing server");

			// First close the server and then close all connections with clients.
			closeImpl();
			EventManager.callEvent(new ServerCloseEvent(this));

			state = EState.CLOSED;
			onLogEvent("Server closed");
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
	
	@Override
	public boolean dispose() {
		if (state != EState.CLOSED || !disposable.dispose())
			return false;

		clientQueue.dispose();
		onLogEvent("Server disposed");
		return true;
	}

	@Override
	public EState getState() {
		return state;
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
	protected void onLogEvent(ELogLevel level, String message) {
		EventManager.callEvent(new LogEvent(level, "%s - %s", toString(), message));
	}

	/**
	 * Throw a LogEvent.
	 * 
	 * @param message The message of the event.
	 */
	protected void onLogEvent(String message) {
		onLogEvent(ELogLevel.INFO, message);
	}
	
	private void waitForClient(Object object) {
		IConnection connection = establishConnection(object);
		if (connection != null)
			initialiseConnection(connection);
	}

	/**
	 * Wait for a client to connect.
	 * 
	 * @param object The object used to wait asynchronously for a client to connect.
	 * 
	 * @return Null if an error occurs, the connection with the client otherwise.
	 */
	private IConnection establishConnection(Object object) {
		IConnection connection = null;

		try {
			connection = waitForClientImpl(config);
			if (state != EState.OPENED) {
				connection.dispose();
				connection = null;
			}

			newClientExceptionCounter = 0;
		} catch (Exception e) {
			newClientExceptionCounter++;
			if (newClientExceptionCounter == MAX_EXCEPTION_NUMBER) {
				onLogEvent("Too much exceptions in a row, closing server");
				EventManager.callEvent(new ServerUnstableEvent(this));
			}
		} finally {
			// Wait for another client if the server is opened.
			if (state == EState.OPENED)
				clientQueue.add(object);
		}

		return connection;
	}

	private void initialiseConnection(IConnection connection) {
		boolean initialised = false;

		// Monitor the created connection
		ConnectionListener listener = new ConnectionListener(this, connection);

		try {
			initialised = connection.initialise();
		} catch (Exception e) {
			// Do nothing
		}

		if (!initialised) {
			onLogEvent(ELogLevel.ERROR, "Failure to initialise the connection with the remote");
			connection.dispose();
		}
		else {
			try {
				// Adding delay to be sure connection has not been lost
				Thread.sleep(100);

				if (listener.isAlive())
					// Notifying observers that a client is connected
					EventManager.callEvent(new NewClientEvent(connection, this));
			} catch (Exception e) {
				// Do nothing
			}
		}
	}
	
	private class ConnectionListener implements IEventListener {
		private IServer server;
		private IConnection connection;
		private boolean isAlive;

		/**
		 * Creates a listener to monitor the given connection.
		 *
		 * @param connection The connection to monitor.
		 */
		public ConnectionListener(IServer server, IConnection connection) {
			this.server = server;
			this.connection = connection;

			isAlive = true;

			EventManager.registerListener(this);
		}

		/**
		 * @return True if the connection is connected to the remote, false otherwise.
		 */
		public boolean isAlive() {
			return isAlive;
		}

		@EventHandler
		private void onConnectionLost(ConnectionLostEvent event) {
			if (connection == event.getConnection()) {
				isAlive = false;
				connection.dispose();
			}
		}
		
		@EventHandler
		private void onConnectionUnstable(ConnectionUnstableEvent event) {
			if (connection == event.getConnection()) {
				isAlive = false;
				connection.dispose();
			}
		}
		
		@EventHandler
		private void onServerClose(ServerCloseEvent event) {
			if (server == event.getServer()) {
				isAlive = false;
				connection.dispose();
				EventManager.unregisterListener(this);
			}
		}
	}
}
