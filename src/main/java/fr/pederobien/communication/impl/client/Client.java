package fr.pederobien.communication.impl.client;

import fr.pederobien.communication.event.ClientConnectedEvent;
import fr.pederobien.communication.event.ClientUnstableEvent;
import fr.pederobien.communication.impl.connection.ConnectionListener;
import fr.pederobien.communication.interfaces.client.IClient;
import fr.pederobien.communication.interfaces.client.IClientConfig;
import fr.pederobien.communication.interfaces.connection.IConnection;
import fr.pederobien.utils.BlockingQueueTask;
import fr.pederobien.utils.Disposable;
import fr.pederobien.utils.IDisposable;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.LogEvent;
import fr.pederobien.utils.event.LogEvent.ELogLevel;

public abstract class Client implements IClient {
	private IClientConfig config;
	private EState state;
	private BlockingQueueTask<Object> connectionQueue;
	private IConnection connection;
	private ConnectionListener listener;
	private IDisposable disposable;
	private int unstableCounter;
	
	/**
	 * Create a client ready to be connected to a remote.
	 * 
	 * @param config The object that holds the client configuration.
	 * @param layer The layer responsible to encode/decode data.
	 */
	protected Client(IClientConfig config) {
		this.config = config;
		
		state = EState.DISCONNECTED;
		
		String name = String.format("%s[reconnect]", toString(), config.getPort());
		connectionQueue = new BlockingQueueTask<Object>(name, object -> startConnect(object));
		disposable = new Disposable();

		unstableCounter = 0;
	}
	
	@Override
	public boolean connect() {
		disposable.checkDisposed();

		if (state != EState.DISCONNECTED)
			return false;

		state = EState.CONNECTING;
		onLogEvent("Connecting to the remote");

		connectionQueue.add(new Object());
		connectionQueue.start();
		return true;
	}
	
	@Override
	public boolean disconnect() {
		disposable.checkDisposed();
		
		if (state != EState.CONNECTING && state != EState.CONNECTED)
			return false;

		state = EState.DISCONNECTING;
		onLogEvent("Disconnecting from the remote");

		if (getConnection() != null) {
			getConnection().setEnabled(false);
			getConnection().dispose();
		}

		state = EState.DISCONNECTED;
		onLogEvent("Disconnected from the remote");
		return true;
	}

	@Override
	public boolean dispose() {
		if (state != EState.DISCONNECTED || !disposable.dispose())
			return false;

		connectionQueue.dispose();

		state = EState.DISPOSED;
		onLogEvent("Client disposed");
		return true;
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
	 * Throw a LogEvent.
	 * 
	 * @param level The level of the log.
	 * @param message The message of the log.
	 * @param args The arguments of the message to display.
	 */
	protected void onLogEvent(ELogLevel level, String message, Object... args) {
		EventManager.callEvent(new LogEvent(level, "%s - %s", toString(), String.format(message, args)));
	}
	
	/**
	 * Throw a LogEvent.
	 * 
	 * @param message The message of the event.
	 * @param args The arguments of the message to display.
	 */
	protected void onLogEvent(String message, Object... args) {
		onLogEvent(ELogLevel.INFO, message, args);
	}

	/**
	 * Start to connect to the remote. If an error occurred, it will automatically try to reconnect unless
	 * the disconnect method is called.
	 * 
	 * @param socket The socket to use to connect to the remote.
	 */
	private void startConnect(Object object) {
		if (establishConnection(object))
			initializeConnection(object);
	}

	/**
	 * Try to establish the connection with the remote.
	 * 
	 * @param object The object used to retry asynchronously to establish to connection with the remote.
	 * 
	 * @return True if the connection is established, false otherwise.
	 */
	private boolean establishConnection(Object object) {
		try {
			// Attempting connection with the remote
			connectImpl(getAddress(), getPort(), config.getConnectionTimeout());
			return state == EState.CONNECTING;

		} catch (Exception e) {
			try {
				// Wait before trying to reconnect to the remote
				Thread.sleep(config.getReconnectionDelay());

				if (state == EState.CONNECTING) {
					onLogEvent("Connection timeout, retrying");
					connectionQueue.add(object);
				}
			} catch (InterruptedException e1) {
				// Exception occurs if client is disposed -> do nothing
			}
		}

		return false;
	}

	/**
	 * Initialise the connection with the remote.
	 * If the initialisation fails, no ClientConnected event is thrown.
	 * 
	 * @param object The object used to retry asynchronously to establish to connection with the remote.
	 */
	private void initializeConnection(Object object) {
		boolean initialized = false;
		IConnection connection = onConnectionComplete(config);

		try {
			// Attempting connection initialization
			initialized = connection.initialise();
		} catch (Exception e) {
			// Do nothing
		}
		
		if (state != EState.CONNECTING) {
			connection.dispose();
			return;
		}
		
		if (!initialized) {
			connection.dispose();
			onLogEvent(ELogLevel.WARNING, "Initialisation failure");
			reconnect();
		}
		else {
			this.connection = connection;

			// Starting the monitoring of the connection
			listener = new ConnectionListener(getConnection());
			listener.setOnConnectionLost(ignored -> reconnect());
			listener.setOnConnectionUnstable(ignored -> reconnect());
			listener.start();

			state = EState.CONNECTED;
			onLogEvent("Connected to the remote");

			// Notifying observers that the client is connected
			EventManager.callEvent(new ClientConnectedEvent(this));
		}
	}

	/**
	 * Re-attempt to connect with the remote.
	 */
	private void reconnect() {
		if (isDisposed())
			return;

		disconnect();

		if (unstableCounter == config.getMaxUnstableCounterValue()) {
			onLogEvent(ELogLevel.ERROR, "stopping automatic reconnection");
			EventManager.callEvent(new ClientUnstableEvent(this));
		}
		else if (config.isAutomaticReconnection()) {
			unstableCounter++;
			onLogEvent("Starting automatic reconnection");
			connect();
		}
	}
}
