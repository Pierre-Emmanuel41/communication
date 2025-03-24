package fr.pederobien.communication.impl.server.state;

import fr.pederobien.communication.interfaces.server.IServerConfig;
import fr.pederobien.communication.interfaces.server.IServerImpl;
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
	public boolean open() {
		return false;
	}
	
	@Override
	public boolean close() {
		return false;
	}
	
	@Override
	public boolean dispose() {
		return false;
	}
	
	/**
	 * @return The context associated to this state.
	 */
	public Context<T> getContext() {
		return context;
	}
	
	/**
	 * @return The server configuration
	 */
	public IServerConfig<T> getConfig() {
		return context.getConfig();
	}
	
	/**
	 * @return The server implementation.
	 */
	public IServerImpl<T> getImpl() {
		return context.getImpl();
	}
	
	/**
	 * Print a log using info debug level
	 * 
	 * @param message The message of the event.
	 * @param args    The arguments of the message to display.
	 */
	protected void info(String message, Object... args) {
		Logger.info("%s - %s", context.getServer(), String.format(message, args));
	}
}
