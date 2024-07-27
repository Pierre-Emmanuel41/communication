package fr.pederobien.communication.impl;

import fr.pederobien.communication.interfaces.IConnectionConfig;
import fr.pederobien.communication.interfaces.IConnectionImpl;

public class CustomConnection extends Connection {
	private IConnectionImpl implementation;
	
	/**
	 * Create custom connection that send asynchronously messages to the remote.
	 * 
	 * @param config The object that holds the client configuration.
	 * @param implementation The connection specific implementation for sending/receiving data from the remote.
	 * @param mode Represent the direction of the connection.
	 */
	protected CustomConnection(IConnectionConfig config, IConnectionImpl implementation, Mode mode) {
		super(config, mode);
		
		this.implementation = implementation;
	}

	@Override
	protected void sendImpl(byte[] data) throws Exception {
		implementation.sendImpl(data);
	}

	@Override
	protected byte[] receiveImpl(int receivingBufferSize) throws Exception {
		return implementation.receiveImpl(receivingBufferSize);
	}
	
	@Override
	protected void disposeImpl() {
		implementation.disposeImpl();
	}
}
