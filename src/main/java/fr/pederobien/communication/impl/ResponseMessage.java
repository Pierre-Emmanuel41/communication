package fr.pederobien.communication.impl;

import fr.pederobien.communication.interfaces.IResponseMessage;

public class ResponseMessage implements IResponseMessage {
	private int requestIdentifier;
	private byte[] bytes;

	/**
	 * Create a response based on the given parameter.
	 * 
	 * @param requestIdentifier The request identifier associated to this response.
	 * @param bytes             the bytes received from the remote as response to the request.
	 */
	public ResponseMessage(int requestIdentifier, byte[] bytes) {
		this.requestIdentifier = requestIdentifier;
		this.bytes = bytes;
	}

	@Override
	public byte[] getBytes() {
		return bytes;
	}

	@Override
	public int getRequestIdentifier() {
		return requestIdentifier;
	}
}
