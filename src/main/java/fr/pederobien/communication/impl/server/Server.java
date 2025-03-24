package fr.pederobien.communication.impl.server;

import fr.pederobien.communication.impl.server.state.Context;
import fr.pederobien.communication.impl.server.state.IContext;
import fr.pederobien.communication.interfaces.server.IServer;
import fr.pederobien.communication.interfaces.server.IServerConfig;
import fr.pederobien.communication.interfaces.server.IServerImpl;

public class Server<T> implements IServer {
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
}
