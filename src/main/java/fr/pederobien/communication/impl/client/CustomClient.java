package fr.pederobien.communication.impl.client;

import fr.pederobien.communication.impl.client.state.Context;
import fr.pederobien.communication.impl.client.state.IContext;
import fr.pederobien.communication.interfaces.client.IClient;
import fr.pederobien.communication.interfaces.client.IClientConfig;
import fr.pederobien.communication.interfaces.client.IClientImpl;
import fr.pederobien.communication.interfaces.connection.IConnection;

public class CustomClient implements IClient {
	private IClientConfig config;
	private String name;
	private IContext context;

	/**
	 * Create a client ready to be connected to a remote.
	 * 
	 * @param config The object that holds the client configuration.
	 * @param impl   The client specific implementation to connect/disconnect from
	 *               the server.
	 */
	public CustomClient(IClientConfig config, IClientImpl impl) {
		this.config = config;

		name = String.format("[%s %s:%s]", config.getName(), config.getAddress(), config.getPort());
		context = new Context(this, impl);
	}

	@Override
	public void connect() {
		context.connect();
	}

	@Override
	public void disconnect() {
		context.disconnect();
	}

	@Override
	public void dispose() {
		context.dispose();
	}

	@Override
	public boolean isDisposed() {
		return context.isDisposed();
	}

	@Override
	public IConnection getConnection() {
		return context.getConnection();
	}

	@Override
	public IClientConfig getConfig() {
		return config;
	}

	@Override
	public String toString() {
		return name;
	}
}
