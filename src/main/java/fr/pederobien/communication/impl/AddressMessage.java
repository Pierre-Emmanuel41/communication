package fr.pederobien.communication.impl;

import java.net.InetSocketAddress;

import fr.pederobien.communication.interfaces.IAddressMessage;

public class AddressMessage extends RequestMessage implements IAddressMessage {
	private InetSocketAddress address;

	public AddressMessage(byte[] bytes, int uniqueIdentifier, InetSocketAddress address) {
		super(bytes, uniqueIdentifier);
		this.address = address;
	}

	@Override
	public InetSocketAddress getAddress() {
		return address;
	}
}
