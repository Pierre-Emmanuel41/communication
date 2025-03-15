package fr.pederobien.communication.impl.client.state;

import fr.pederobien.communication.event.ClientUnstableEvent;
import fr.pederobien.communication.interfaces.client.IClient;
import fr.pederobien.communication.interfaces.client.IClientImpl;
import fr.pederobien.communication.interfaces.connection.IConnection;
import fr.pederobien.utils.HealedCounter;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.LogEvent;
import fr.pederobien.utils.event.LogEvent.ELogLevel;

public class Context implements IContext {
	private IClient client;
	private IClientImpl impl;
	private IState disconnected;
	private IState connected;
	private IState disposed;
	private IState state;

	private IConnection connection;
	private HealedCounter counter;

	/**
	 * Create a state context.
	 */
	public Context(IClient client, IClientImpl impl) {
		this.client = client;
		this.impl = impl;

		disconnected = new Disconnected(this);
		connected = new Connected(this);
		disposed = new Disposed(this);

		state = disconnected;

		int unstableCounter = client.getConfig().getClientMaxUnstableCounterValue();
		int healTime = client.getConfig().getClientHealTime();
		String counterName = String.format("%s unstable counter", getClient());
		counter = new HealedCounter(unstableCounter, healTime, () -> onClientUnstable(), counterName);
	}

	@Override
	public void connect() {
		state.connect();
	}

	@Override
	public void disconnect() {
		state.disconnect();
	}

	@Override
	public void dispose() {
		state.dispose();
	}

	@Override
	public boolean isDisposed() {
		return state == disposed;
	}

	@Override
	public IConnection getConnection() {
		return connection;
	}

	/**
	 * Set the connection of this context. The connection can be connected to the
	 * client or null.
	 * 
	 * @param connection The connection of the context.
	 */
	public void setConnection(IConnection connection) {
		this.connection = connection;
	}

	/**
	 * @return The client associated to this context.
	 */
	public IClient getClient() {
		return client;
	}

	/**
	 * @return The implementation specific to the network.
	 */
	public IClientImpl getImpl() {
		return impl;
	}

	/**
	 * @return The disconnected state.
	 */
	public IState getDisconnected() {
		return disconnected;
	}

	/**
	 * @return The connected state.
	 */
	public IState getConnected() {
		return connected;
	}

	/**
	 * @return The disposed state.
	 */
	public IState getDisposed() {
		return disposed;
	}

	/**
	 * @return The healed-counter associated to this context.
	 */
	public HealedCounter getCounter() {
		return counter;
	}

	/**
	 * Set the state of this context.
	 * 
	 * @param state The new state.
	 */
	public void setState(IState state) {
		this.state.setEnabled(false);
		this.state = state;
		this.state.setEnabled(true);
	}

	/**
	 * Function called when the healed counter reached its maximum value.
	 */
	private void onClientUnstable() {
		String log = String.format("%s - %s", client.toString(), "stopping automatic reconnection");
		EventManager.callEvent(new LogEvent(ELogLevel.ERROR, log));
		EventManager.callEvent(new ClientUnstableEvent(client));
	}
}
