package fr.pederobien.communication.impl.client.state;

import fr.pederobien.communication.event.ClientConnectedEvent;
import fr.pederobien.communication.event.ConnectionLostEvent;
import fr.pederobien.communication.event.ConnectionUnstableEvent;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;

public class Connected extends State implements IEventListener {

	/**
	 * Create a state where the client is connected to the remote.
	 * 
	 * @param context The context of this state.
	 */
	public Connected(Context context) {
		super(context);
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		if (isEnabled) {
			onLogEvent("Client connected");
			EventManager.callEvent(new ClientConnectedEvent(getContext().getClient()));
			EventManager.registerListener(this);
		} else {
			EventManager.unregisterListener(this);
		}
	}

	@Override
	public void disconnect() {
		onLogEvent("Disconnecting client");

		getContext().getConnection().setEnabled(false);
		getContext().getConnection().dispose();
		getContext().setState(getContext().getDisconnected());
	}

	@Override
	public void dispose() {
		onLogEvent("Disposing client");

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

		reconnect();
	}

	@EventHandler
	private void onConnectionUnstable(ConnectionUnstableEvent event) {
		if (event.getConnection() != getContext().getConnection()) {
			return;
		}

		reconnect();
	}

	private void reconnect() {
		getContext().getConnection().setEnabled(false);
		getContext().getConnection().dispose();

		if (!getContext().getCounter().increment()) {
			getContext().setState(getContext().getDisconnected());

			if (getConfig().isAutomaticReconnection()) {
				onLogEvent("Starting automatic reconnection");
				getContext().connect();
			}
		}
	}
}
