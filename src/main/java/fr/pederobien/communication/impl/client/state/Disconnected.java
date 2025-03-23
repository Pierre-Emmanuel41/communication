package fr.pederobien.communication.impl.client.state;

import fr.pederobien.communication.interfaces.connection.IConnection;
import fr.pederobien.utils.BlockingQueueTask;
import fr.pederobien.utils.event.Logger;

public class Disconnected<T> extends State<T> {
	private BlockingQueueTask<Object> connectionQueue;
	private boolean attemptingConnection;
	private boolean disconnectionRequested;

	/**
	 * Create a state where the client is disconnected from the remote.
	 * 
	 * @param context The context of this state.
	 */
	public Disconnected(Context<T> context) {
		super(context);

		String name = String.format("%s[reconnect]", getContext().getClient());
		connectionQueue = new BlockingQueueTask<Object>(name, ignored -> connect(ignored));
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		if (isEnabled) {
			info("Client disconnected");
		}
	}

	@Override
	public void connect() {
		info("Connecting client");

		attemptingConnection = true;
		disconnectionRequested = false;
		connectionQueue.add(new Object());
		connectionQueue.start();
	}

	@Override
	public void disconnect() {
		if (attemptingConnection) {
			info("Disconnecting client");
			disconnectionRequested = true;
			info("Client disconnected");
		}
	}

	@Override
	public void dispose() {
		info("Disposing client");

		disconnectionRequested = true;
		connectionQueue.dispose();
		getContext().getCounter().dispose();
		getContext().setState(getContext().getDisposed());
	}

	private void connect(Object ignored) {
		IConnection connection = null;

		try {

			// Attempting connection with the remote
			connection = getContext().getImpl().connectImpl(getConfig());
		} catch (Exception e) {
			retry();
		}

		if (connection != null && !disconnectionRequested) {
			boolean initialised = false;

			try {
				initialised = connection.initialise();
			} catch (Exception e) {
				// Do nothing
			}

			if (!initialised) {
				Logger.warning(String.format("%s - %s", getContext().getClient(), "Initialisation failure"));
				connection.setEnabled(false);
				connection.dispose();

				if (!getContext().getCounter().increment()) {
					retry();
				}
			} else {
				if (disconnectionRequested) {
					connection.dispose();
				} else {
					getContext().setConnection(connection);
					getContext().setState(getContext().getConnected());
					attemptingConnection = false;
				}
			}
		}
	}

	private void retry() {
		try {
			// Wait before trying to reconnect to the remote
			Thread.sleep(getConfig().getReconnectionDelay());

			if (!disconnectionRequested && getConfig().isAutomaticReconnection()) {
				info("Attempted connection aborted, retrying");
				connectionQueue.add(new Object());
			}
		} catch (InterruptedException e) {
			// Client is disposed
		}
	}
}
