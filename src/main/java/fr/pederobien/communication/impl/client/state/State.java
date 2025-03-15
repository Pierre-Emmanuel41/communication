package fr.pederobien.communication.impl.client.state;

import fr.pederobien.communication.interfaces.client.IClientConfig;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.LogEvent;
import fr.pederobien.utils.event.LogEvent.ELogLevel;

public abstract class State implements IState {
	private Context context;

	/**
	 * Creates a new state associated to this context.
	 * 
	 * @param context The context of this state.
	 */
	public State(Context context) {
		this.context = context;
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		// Do nothing
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
	protected Context getContext() {
		return context;
	}

	/**
	 * @return The client configuration.
	 */
	protected IClientConfig getConfig() {
		return context.getClient().getConfig();
	}

	/**
	 * Throw a LogEvent.
	 * 
	 * @param level   The level of the log.
	 * @param message The message of the log.
	 * @param args    The arguments of the message to display.
	 */
	protected void onLogEvent(ELogLevel level, String message, Object... args) {
		String log = String.format("%s - %s", context.getClient().toString(), String.format(message, args));
		EventManager.callEvent(new LogEvent(level, log));
	}

	/**
	 * Throw a LogEvent.
	 * 
	 * @param message The message of the event.
	 * @param args    The arguments of the message to display.
	 */
	protected void onLogEvent(String message, Object... args) {
		onLogEvent(ELogLevel.INFO, message, args);
	}
}
