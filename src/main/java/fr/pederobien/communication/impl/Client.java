package fr.pederobien.communication.impl;

import fr.pederobien.communication.event.ClientConnectedEvent;
import fr.pederobien.communication.event.ConnectionLostEvent;
import fr.pederobien.communication.event.ConnectionUnstableEvent;
import fr.pederobien.communication.interfaces.IClient;
import fr.pederobien.communication.interfaces.IClientConfig;
import fr.pederobien.communication.interfaces.IConnection;
import fr.pederobien.utils.BlockingQueueTask;
import fr.pederobien.utils.Disposable;
import fr.pederobien.utils.IDisposable;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;
import fr.pederobien.utils.event.LogEvent;
import fr.pederobien.utils.event.LogEvent.ELogLevel;

public abstract class Client implements IClient {
	private IClientConfig config;
	private EState state;
	private IDisposable disposable;
	private BlockingQueueTask<Object> connectionQueue;
	private IConnection connection;
	private ConnectionListener listener;
	
	/**
	 * Create a client ready to be connected to a remote.
	 * 
	 * @param config The object that holds the client configuration.
	 * @param layer The layer responsible to encode/decode data.
	 */
	protected Client(IClientConfig config) {
		this.config = config;
		
		state = EState.DISCONNECTED;

		disposable = new Disposable();
		
		String name = String.format("%s[reconnect]", toString(), config.getPort());
		connectionQueue = new BlockingQueueTask<Object>(name, object -> startConnect(object));
		
		listener = new ConnectionListener(config.getMaxUnstableCounterValue());
	}
	
	@Override
	public void connect() {
		disposable.checkDisposed();

		if (state == EState.CONNECTION_LOST || state == EState.DISCONNECTED) {
			onLogEvent("Connecting to the remote");

			state = EState.CONNECTING;			
			
			connectionQueue.add(new Object());
			connectionQueue.start();
		}
	}
	
	@Override
	public void disconnect() {
		disposable.checkDisposed();
		
		if (state != EState.DISCONNECTING && state != EState.DISCONNECTED) {
			state = EState.DISCONNECTING;
			onLogEvent("Disconnecting from the remote");
			
			if (getConnection() != null) {
				getConnection().setEnabled(false);
				getConnection().dispose();
			}
			
			state = EState.DISCONNECTED;

			onLogEvent("Disconnected from the remote");
		}
	}

	@Override
	public void dispose() {
		if (disposable.dispose()) {
			if (state != EState.DISCONNECTED)
				disconnect();

			connectionQueue.dispose();
		}
	}

	@Override
	public boolean isDisposed() {
		return disposable.isDisposed();
	}
	
	@Override
	public String getAddress() {
		return config.getAddress();
	}
	
	@Override
	public int getPort() {
		return config.getPort();
	}

	@Override
	public int getConnectionTimeout() {
		return config.getConnectionTimeout();
	}

	@Override
	public int getReconnectionDelay() {
		return config.getReconnectionDelay();
	}

	@Override
	public EState getState() {
		return state;
	}
	
	@Override
	public IConnection getConnection() {
		return connection;
	}
	
	@Override
	public String toString() {
		return String.format("[Client %s:%s]", config.getAddress(), config.getPort());
	}
	
	/**
	 * Set the new state of this client.
	 * 
	 * @param state The new client state.
	 */
	protected void setState(EState state) {
		this.state = state;
	}
	
	/**
	 * Client implementation specific to connect to the remote.
	 * 
	 * @param address The address of the remote.
	 * @param port The port of the remote.
	 * @param connectionTimeout The time in ms before a connection timeout occurs.
	 * 
	 * @throws Exception If an exception is thrown, it will be caught and a reconnection will be attempted.
	 */
	protected abstract void connectImpl(String address, int port, int connectionTimeout) throws Exception;
	
	/**
	 * A connected client does not mean it should open a connection with the remote.
	 * If while waiting for connection with the remote, the disconnect method is called, then the
	 * connection process must aborted.
	 * 
	 * @param config The configuration of the client.
	 */
	protected abstract IConnection onConnectionComplete(IClientConfig config);
	
	/**
	 * Method called after having the connection fully initialized but before throwing
	 * a connection complete event.
	 * 
	 * @param connection The initialized connection of this client.
	 */
	protected abstract boolean postInitialise(IConnection connection);
	
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
	
	/**
	 * Start to connect to the remote. If an error occurred, it will automatically try to reconnect unless
	 * the disconnect method is called.
	 * 
	 * @param socket The socket to use to connect to the remote.
	 */
	private void startConnect(Object object) {
		try {
			
			// Attempting connection with the remote
			connectImpl(getAddress(), getPort(), getConnectionTimeout());
			
			// But checking if connection has been canceled
			if (state == EState.DISCONNECTING || state == EState.DISCONNECTED)
				return;
			
			state = EState.CONNECTED;
			IConnection connection = onConnectionComplete(config);
			connection.initialise();
			listener.start();
			if (!postInitialise(connection))
				connection.dispose();
			else {
				this.connection = connection;
				onLogEvent("Connected to the remote");

				// Notifying observers that the client is connected
				EventManager.callEvent(new ClientConnectedEvent(this));
			}
				
		} catch (Exception e) {
			try {
				// Wait before trying to reconnect to the remote
				Thread.sleep(getReconnectionDelay());

				if (state == EState.CONNECTION_LOST || state == EState.CONNECTING) {
					onLogEvent("Connection timeout, retrying");
					connectionQueue.add(object);
				}
			} catch (InterruptedException e1) {
				// Exception occurs if client is disposed -> do nothing
			}
		}
	}
	
	private class ConnectionListener implements IEventListener {
		private int maxUnstableCounter;
		private int unstableCounter;
		
		/**
		 * Creates a connection listener to trigger a connection lost or an unstable connection.
		 * 
		 * @param maxUnstableCounter The maximum value of the unstable counter before stopping automatic reconnection.
		 */
		public ConnectionListener(int maxUnstableCounter) {
			this.maxUnstableCounter = maxUnstableCounter;
			unstableCounter = 0;
		}
		
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
			startReconnection(event.getConnection());
		}
		
		@EventHandler
		private void onConnectionLost(ConnectionLostEvent event) {
			startReconnection(event.getConnection());
		}
		
		/**
		 * Check if the connection is monitored by this event listener and start the automatic reconnection
		 * if it is enabled.
		 * 
		 * @param connection The connection involved in a ConnectionUnstableEvent or in a ConnectionLostEvent.
		 */
		private void startReconnection(IConnection connection) {
			if (connection == getConnection())
			{
				unstableCounter++;
				disconnect();
				stop();
				
				if (unstableCounter == maxUnstableCounter)
					onLogEvent(ELogLevel.ERROR, "Client unstable, stopping automatic reconnection");
				else if (config.isAutomaticReconnection())
					connect();
			}
		}
	}
}
