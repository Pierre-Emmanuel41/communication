package fr.pederobien.communication.impl.server.state;

import java.util.ArrayList;
import java.util.List;

import fr.pederobien.communication.event.ConnectionDisposedEvent;
import fr.pederobien.communication.event.ConnectionLostEvent;
import fr.pederobien.communication.event.ConnectionUnstableEvent;
import fr.pederobien.communication.event.NewClientEvent;
import fr.pederobien.communication.impl.Communication;
import fr.pederobien.communication.interfaces.connection.IConnection;
import fr.pederobien.communication.interfaces.server.IClientInfo;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;
import fr.pederobien.utils.event.Logger;

public class Opened<T> extends State<T> implements IEventListener {
	private Thread waiter;
	private boolean closeRequested;
	private List<IConnection> connections;

	public Opened(Context<T> context) {
		super(context);

		connections = new ArrayList<IConnection>();
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		if (isEnabled) {
			EventManager.registerListener(this);
			try {

				// Server implementation specific to open the server
				getContext().getImpl().open(getConfig());

				closeRequested = false;

				String name = String.format("[%s %s - waitForClient]", getConfig().getName(), getConfig().getPoint());
				waiter = new Thread(() -> waitForClient(), name);
				waiter.setDaemon(true);
				waiter.start();

				info("Server opened");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean close() {
		info("Closing server");
		closeRequested = true;
		waiter.interrupt();

		EventManager.unregisterListener(this);

		for (IConnection connection : connections) {
			disposeConnection(connection);
		}

		connections.clear();
		getContext().setState(getContext().getClosed());
		return true;
	}

	private void waitForClient() {
		while (!closeRequested) {
			IClientInfo<T> info = null;

			try {

				// Server implementation specific to wait for a new client
				info = getImpl().waitForClient();

				// The client is not allowed to be connected with the server
				if (closeRequested || !getConfig().getClientValidator().isValid(info.getEndPoint())) {
					info.getImpl().dispose();
					continue;
				}
			} catch (Exception e) {
				if (getContext().getCounter().increment()) {
					break;
				}
			}

			if (info != null && !closeRequested) {
				boolean initialised = false;
				IConnection connection = null;

				try {
					connection = Communication.createConnection(getConfig(), info.getEndPoint(), info.getImpl());
					initialised = connection.initialise();
				} catch (Exception e) {
					if (getContext().getCounter().increment()) {
						break;
					}
				}

				if (!initialised || closeRequested) {
					if (!initialised) {
						Logger.warning("[%s] - Initialisation failure", getContext().getName());
					}

					disposeConnection(connection);
				} else {
					connections.add(connection);

					// Notifying observers that a client is connected
					EventManager.callEvent(new NewClientEvent(getContext().getServer(), connection));
				}
			}
		}
	}

	@EventHandler
	private void onConnectionLost(ConnectionLostEvent event) {
		if (connections.remove(event.getConnection())) {
			disposeConnection(event.getConnection());
		}
	}

	@EventHandler
	private void onConnectionUnstable(ConnectionUnstableEvent event) {
		if (connections.remove(event.getConnection())) {
			disposeConnection(event.getConnection());
		}
	}

	@EventHandler
	private void onConnectionDisposed(ConnectionDisposedEvent event) {
		connections.remove(event.getConnection());
	}

	/**
	 * Disable and dispose in given connection.
	 * 
	 * @param connection The connection to dispose.
	 */
	private void disposeConnection(IConnection connection) {
		connection.setEnabled(false);
		connection.dispose();
	}
}
