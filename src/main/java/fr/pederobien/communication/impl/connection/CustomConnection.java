package fr.pederobien.communication.impl.connection;

import fr.pederobien.communication.interfaces.connection.IConnectionConfig;
import fr.pederobien.communication.interfaces.connection.IConnectionImpl;

public class CustomConnection extends Connection {
	private IConnectionImpl implementation;
	
	/**
	 * Create custom connection that send asynchronously messages to the remote.
	 * 
	 * @param config The object that holds the client configuration.
	 * @param implementation The connection specific implementation for sending/receiving data from the remote.
	 */
	public CustomConnection(IConnectionConfig config, IConnectionImpl implementation) {
		super(config);
		
		this.implementation = implementation;
	}

	@Override
	protected void sendImpl(byte[] data) throws Exception {
		implementation.sendImpl(data);
	}

	@Override
	protected byte[] receiveImpl() throws Exception {
		return implementation.receiveImpl();
	}
	
	@Override
	protected void disposeImpl() {
		implementation.disposeImpl();
	}
}
