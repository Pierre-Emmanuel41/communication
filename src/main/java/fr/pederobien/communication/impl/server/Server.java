package fr.pederobien.communication.impl.server;

import fr.pederobien.communication.event.NewClientEvent;
import fr.pederobien.communication.event.ServerCloseEvent;
import fr.pederobien.communication.event.ServerUnstableEvent;
import fr.pederobien.communication.interfaces.connection.IConnection;
import fr.pederobien.communication.interfaces.server.IServer;
import fr.pederobien.communication.interfaces.server.IServerConfig;
import fr.pederobien.communication.interfaces.server.IServerImpl;
import fr.pederobien.utils.BlockingQueueTask;
import fr.pederobien.utils.Disposable;
import fr.pederobien.utils.IDisposable;
import fr.pederobien.utils.event.Logger;
import fr.pederobien.utils.event.EventManager;

public class Server<T> implements IServer<T> {
	private static final int MAX_EXCEPTION_NUMBER = 10;

	private IServerConfig<T> config;
	private IServerImpl<T> impl;
	private EState state;
	private BlockingQueueTask<Object> clientQueue;
	private IDisposable disposable;
	private int newClientExceptionCounter;

	/**
	 * Creates a server.
	 * 
	 * @param config         The object that holds the server configuration.
	 * @param implementation The server specific implementation to open/close the
	 *                       server.
	 */
	public Server(IServerConfig<T> config, IServerImpl<T> impl) {
		this.config = config;
		this.impl = impl;

		clientQueue = new BlockingQueueTask<Object>(String.format("%s[WaitForClient]", toString()),
				object -> waitForClient(object));
		disposable = new Disposable();

		newClientExceptionCounter = 0;
		state = EState.CLOSED;
	}

	@Override
	public boolean open() {
		disposable.checkDisposed();

		if (state != EState.CLOSED) {
			return false;
		}

		try {
			state = EState.OPENING;
			info("Opening server");

			impl.open(config);

			state = EState.OPENED;
			info("Server opened");

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

		if (state != EState.OPENED) {
			return false;
		}

		try {
			state = EState.CLOSING;
			info("Closing server");

			// First close the server and then close all connections with clients.
			impl.close();
			EventManager.callEvent(new ServerCloseEvent(this));

			state = EState.CLOSED;
			info("Server closed");
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@Override
	public boolean dispose() {
		if (state != EState.CLOSED || !disposable.dispose()) {
			return false;
		}

		clientQueue.dispose();
		info("Server disposed");
		return true;
	}

	@Override
	public EState getState() {
		return state;
	}

	@Override
	public IServerConfig<T> getConfig() {
		return config;
	}

	@Override
	public String toString() {
		return String.format("[%s %s]", config.getName(), config.getPoint());
	}

	/**
	 * Throw a LogEvent.
	 * 
	 * @param message The message of the event.
	 * @param args    The arguments of the message to display.
	 */
	protected void info(String message, Object... args) {
		Logger.info("%s - %s", this, String.format(message, args));
	}

	private void waitForClient(Object object) {
		IConnection connection = establishConnection(object);
		if (connection != null) {
			initialiseConnection(connection);
		}
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
			connection = impl.waitForClient(config);
			if (state != EState.OPENED) {
				connection.dispose();
				connection = null;
			}

			newClientExceptionCounter = 0;
		} catch (Exception e) {
			newClientExceptionCounter++;
			if (newClientExceptionCounter == MAX_EXCEPTION_NUMBER) {
				info("Too much exceptions in a row, closing server");
				EventManager.callEvent(new ServerUnstableEvent(this));
			}
		} finally {
			// Wait for another client if the server is opened.
			if (state == EState.OPENED) {
				clientQueue.add(object);
			}
		}

		return connection;
	}

	private void initialiseConnection(IConnection connection) {
		boolean initialised = false;

		try {
			initialised = connection.initialise();
		} catch (Exception e) {
			// Do nothing
		}

		if (state != EState.OPENED) {
			connection.dispose();
			return;
		}

		if (!initialised) {
			Logger.warning("%s - Initialisation failure", this);
			connection.setEnabled(false);
			connection.dispose();
		} else {

			// Notifying observers that a client is connected
			EventManager.callEvent(new NewClientEvent(new Client<T>(this, connection)));
		}
	}
}
