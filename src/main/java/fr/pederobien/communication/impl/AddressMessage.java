package fr.pederobien.communication.impl;

import java.net.InetSocketAddress;

import fr.pederobien.communication.interfaces.IAddressMessage;

public class AddressMessage extends RequestMessage implements IAddressMessage {
	private InetSocketAddress address;

	/**
	 * Creates a message to send at the given address.
	 * 
	 * @param bytes            The byte array to send to the remote.
	 * @param uniqueIdentifier The request identifier.
	 * @param address          The address at which the message should be sent.
	 */
	public AddressMessage(byte[] bytes, int uniqueIdentifier, InetSocketAddress address) {
		super(bytes, uniqueIdentifier);
		this.address = address;
	}

	/**
	 * Creates a message to send through the network.
	 * 
	 * @param bytes            The byte array to send to the remote.
	 * @param uniqueIdentifier The request identifier.
	 */
	public AddressMessage(byte[] bytes, int uniqueIdentifier) {
		this(bytes, uniqueIdentifier, null);
	}

	@Override
	public InetSocketAddress getAddress() {
		return address;
	}
}
