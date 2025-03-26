package fr.pederobien.communication.impl.server;

import fr.pederobien.communication.event.ServerUnstableEvent;
import fr.pederobien.communication.impl.server.state.Context;
import fr.pederobien.communication.impl.server.state.IContext;
import fr.pederobien.communication.interfaces.server.IServer;
import fr.pederobien.communication.interfaces.server.IServerConfig;
import fr.pederobien.communication.interfaces.server.IServerImpl;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;

public class Server<T> implements IServer, IEventListener {
	private IContext context;

	/**
	 * Creates a server.
	 * 
	 * @param config         The object that holds the server configuration.
	 * @param implementation The server specific implementation to open/close the
	 *                       server.
	 */
	public Server(IServerConfig<T> config, IServerImpl<T> impl) {
		context = new Context<T>(this, config, impl);

		EventManager.registerListener(this);
	}

	@Override
	public boolean open() {
		return context.open();
	}

	@Override
	public boolean close() {
		return context.close();
	}

	@Override
	public boolean dispose() {
		return context.dispose();
	}

	@Override
	public String toString() {
		return context.getName();
	}

	@EventHandler
	private void onUnstableServer(ServerUnstableEvent event) {
		if (event.getServer() != this) {
			return;
		}

		event.getServer().close();
	}
}
