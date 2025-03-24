package fr.pederobien.communication.impl.client.state;

import fr.pederobien.communication.interfaces.client.IClientConfig;
import fr.pederobien.utils.event.Logger;

public abstract class State<T> implements IState {
	private Context<T> context;

	/**
	 * Creates a new state associated to this context.
	 * 
	 * @param context The context of this state.
	 */
	public State(Context<T> context) {
		this.context = context;
	}

	@Override
	public void connect() {
		throw new IllegalStateException("Cannot connect to the remote, illegal state.");
	}

	@Override
	public void disconnect() {
		throw new IllegalStateException("Cannot disconnected from the remote, illegal state.");
	}

	/**
	 * @return The context associated to this state.
	 */
	protected Context<T> getContext() {
		return context;
	}

	/**
	 * @return The client configuration.
	 */
	protected IClientConfig<T> getConfig() {
		return context.getConfig();
	}

	/**
	 * Print a log using INFO debug level
	 * 
	 * @param message The message of the event.
	 */
	protected void info(String message) {
		Logger.info("%s - %s", context.getClient(), message);
	}
}
