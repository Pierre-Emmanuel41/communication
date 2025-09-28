package fr.pederobien.communication.impl.client.state;

import fr.pederobien.communication.interfaces.client.IClientConfig;
import fr.pederobien.utils.event.Logger;

public abstract class State<T> implements IState {
	private final Context<T> context;

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
	 * Print a log using INFO level
	 *
	 * @param message The message to print.
	 * @param args    The arguments of the message.
	 */
	protected void info(String message, Object... args) {
		Logger.info("%s - %s", context.getClient(), String.format(message, args));
	}

	/**
	 * Print a log using DEBUG level.
	 *
	 * @param message The message to print.
	 * @param args    The arguments of the message.
	 */
	protected void debug(String message, Object... args) {
		Logger.debug("%s - %s", context.getClient(), String.format(message, args));
	}
}
