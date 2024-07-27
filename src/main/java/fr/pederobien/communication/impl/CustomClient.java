package fr.pederobien.communication.impl;

import fr.pederobien.communication.interfaces.IClientConfig;
import fr.pederobien.communication.interfaces.IClientImpl;
import fr.pederobien.communication.interfaces.IConnection;

public class CustomClient extends Client {
	private IClientImpl implementation;
	
	/**
	 * Create a client ready to be connected to a remote.
	 * 
	 * @param config The object that holds the client configuration.
	 * @param implementation The client specific implementation to connect/disconnect from the server.
	 */
	protected CustomClient(IClientConfig config, IClientImpl implementation) {
		super(config);
		
		this.implementation = implementation;
	}

	@Override
	protected void connectImpl(String address, int port, int connectionTimeout) throws Exception {
		implementation.connectImpl(address, port, connectionTimeout);
	}

	@Override
	protected IConnection onConnectionComplete(IClientConfig config) {
		return implementation.onConnectionComplete(config);
	}
	
	@Override
	protected void postInitialise() {
		implementation.postInitialise();
	}
}
