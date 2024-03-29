package fr.pederobien.communication.impl;

import fr.pederobien.communication.interfaces.IRequestMessage;

public class RequestMessage implements IRequestMessage {
	private byte[] bytes;
	private int uniqueIdentifier;

	/**
	 * Create a request message to be send to a remote.
	 * 
	 * @param bytes            The byte array to send to the remote.
	 * @param uniqueIdentifier The request identifier.
	 */
	public RequestMessage(byte[] bytes, int uniqueIdentifier) {
		this.bytes = bytes;
		this.uniqueIdentifier = uniqueIdentifier;
	}

	@Override
	public byte[] getBytes() {
		return bytes;
	}

	@Override
	public int getUniqueIdentifier() {
		return uniqueIdentifier;
	}
}
