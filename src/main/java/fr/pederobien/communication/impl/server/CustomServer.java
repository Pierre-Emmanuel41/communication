package fr.pederobien.communication.impl.server;

import fr.pederobien.communication.interfaces.IConnection;
import fr.pederobien.communication.interfaces.IServerConfig;
import fr.pederobien.communication.interfaces.IServerImpl;

public class CustomServer extends Server {
	private IServerImpl implementation;

	/**
	 * Creates a custom server.
	 * 
	 * @param config The object that holds the server configuration.
	 * @param implementation The server specific implementation to open/close the server.
	 */
	public CustomServer(IServerConfig config, IServerImpl implementation) {
		super(config);
		
		this.implementation = implementation;
	}

	@Override
	protected void openImpl(int port) throws Exception {
		implementation.openImpl(port);
	}

	@Override
	protected void closeImpl() throws Exception {
		implementation.closeImpl();
	}

	@Override
	protected IConnection waitForClientImpl(IServerConfig config) throws Exception {
		return implementation.waitForClientImpl(config);
	}
}
