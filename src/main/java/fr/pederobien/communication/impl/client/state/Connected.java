package fr.pederobien.communication.impl.client.state;

import fr.pederobien.communication.event.ClientConnectedEvent;
import fr.pederobien.communication.event.ConnectionLostEvent;
import fr.pederobien.communication.event.ConnectionUnstableEvent;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;

public class Connected<T> extends State<T> implements IEventListener {

	/**
	 * Create a state where the client is connected to the remote.
	 *
	 * @param context The context of this state.
	 */
	public Connected(Context<T> context) {
		super(context);
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		if (isEnabled) {
			info("Client connected");
			EventManager.callEvent(new ClientConnectedEvent(getContext().getClient()));
			EventManager.registerListener(this);
		} else {
			EventManager.unregisterListener(this);
		}
	}

	@Override
	public void disconnect() {
		info("Disconnecting client");

		getContext().getConnection().setEnabled(false);
		getContext().getConnection().dispose();
		getContext().setState(getContext().getDisconnected());
	}

	@Override
	public void dispose() {
		info("Disposing client");

		getContext().getConnection().setEnabled(false);
		getContext().getConnection().dispose();
		getContext().getCounter().dispose();
		getContext().setState(getContext().getDisposed());
	}

	@EventHandler
	private void onConnectionLost(ConnectionLostEvent event) {
		if (event.getConnection() != getContext().getConnection()) {
			return;
		}

		debug("Connection with the remote has been lost");
		reconnect();
	}

	@EventHandler
	private void onConnectionUnstable(ConnectionUnstableEvent event) {
		if (event.getConnection() != getContext().getConnection()) {
			return;
		}

		debug("Connection with the remote detected as unstable");
		reconnect();
	}

	private void reconnect() {
		getContext().getConnection().setEnabled(false);
		getContext().getConnection().dispose();

		if (!getContext().getCounter().increment()) {
			getContext().setState(getContext().getDisconnected());

			if (getConfig().isAutomaticReconnection()) {
				info("Starting automatic reconnection");
				getContext().connect();
			}
		}
	}
}
