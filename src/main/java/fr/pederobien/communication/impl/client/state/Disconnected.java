package fr.pederobien.communication.impl.client.state;

import fr.pederobien.communication.interfaces.connection.IConnection;
import fr.pederobien.utils.BlockingQueueTask;
import fr.pederobien.utils.event.LogEvent.ELogLevel;

public class Disconnected extends State {
	private BlockingQueueTask<Object> connectionQueue;
	private boolean attemptingConnection;
	private boolean disconnectionRequested;

	/**
	 * Create a state where the client is disconnected from the remote.
	 * 
	 * @param context The context of this state.
	 */
	public Disconnected(Context context) {
		super(context);

		String name = String.format("%s[reconnect]", getContext().getClient().toString(), getConfig().getPort());
		connectionQueue = new BlockingQueueTask<Object>(name, ignored -> connect(ignored));
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		if (isEnabled) {
			onLogEvent("Client disconnected");
		}
	}

	@Override
	public void connect() {
		onLogEvent("Connecting client");

		attemptingConnection = true;
		disconnectionRequested = false;
		connectionQueue.add(new Object());
		connectionQueue.start();
	}

	@Override
	public void disconnect() {
		if (attemptingConnection) {
			onLogEvent("Disconnecting client");
			disconnectionRequested = true;
			onLogEvent("Client disconnected");
		}
	}

	@Override
	public void dispose() {
		onLogEvent("Disposing client");

		disconnectionRequested = true;
		connectionQueue.dispose();
		getContext().getCounter().dispose();
		getContext().setState(getContext().getDisposed());
	}

	private void connect(Object ignored) {
		try {
			String address = getConfig().getAddress();
			int port = getConfig().getPort();
			int timeout = getConfig().getConnectionTimeout();

			// Attempting connection with the remote
			getContext().getImpl().connectImpl(address, port, timeout);

			if (!disconnectionRequested) {
				IConnection connection = getContext().getImpl().onConnectionComplete(getConfig());

				// Attempting connection initialization
				if (connection.initialise()) {
					if (disconnectionRequested) {
						connection.dispose();
					} else {
						getContext().setConnection(connection);
						getContext().setState(getContext().getConnected());
						attemptingConnection = false;
					}
				} else {
					onLogEvent(ELogLevel.WARNING, "Initialisation failure");
					connection.setEnabled(false);
					connection.dispose();

					if (!getContext().getCounter().increment()) {
						retry();
					}
				}
			}
		} catch (Exception e) {
			retry();
		}
	}

	private void retry() {
		try {
			// Wait before trying to reconnect to the remote
			Thread.sleep(getConfig().getReconnectionDelay());

			if (!disconnectionRequested && getConfig().isAutomaticReconnection()) {
				onLogEvent("Attempted connection aborted, retrying");
				connectionQueue.add(new Object());
			}
		} catch (InterruptedException e) {
			// Client is disposed
		}
	}
}
