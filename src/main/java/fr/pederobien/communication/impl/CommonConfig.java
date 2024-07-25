package fr.pederobien.communication.impl;

import fr.pederobien.communication.interfaces.ICommonConfig;
import fr.pederobien.communication.interfaces.ILayer;

public class CommonConfig implements ICommonConfig {
	private int receivingBufferSize;
	private boolean allowUnexpectedRequest;
	private ILayer layer;
	
	/**
	 * Create an object that holds parameters that are common for a client and a server.
	 * 
	 * @param receivingBufferSize The size, in bytes, of the buffer used to receive data from the remote.
	 * @param allowUnexpectedRequest True if an unexpected request has been received and should be executed, false otherwise.
	 * @param layer The layer responsible to encode/decode data.
	 */
	public CommonConfig(int receivingBufferSize, boolean allowUnexpectedRequest, ILayer layer) {
		this.receivingBufferSize = receivingBufferSize;
		this.allowUnexpectedRequest = allowUnexpectedRequest;
		this.layer = layer;
	}

	@Override
	public int getReceivingBufferSize() {
		return receivingBufferSize;
	}

	@Override
	public boolean isAllowUnexpectedRequest() {
		return allowUnexpectedRequest;
	}

	@Override
	public ILayer getLayer() {
		return layer;
	}
}
