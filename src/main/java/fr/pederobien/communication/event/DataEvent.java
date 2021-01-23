package fr.pederobien.communication.event;

import java.net.InetSocketAddress;

public class DataEvent {
	private InetSocketAddress address;

	public DataEvent(InetSocketAddress address) {
		this.address = address;
	}

	/**
	 * @return The address from which the data has been received.
	 */
	public InetSocketAddress getAddress() {
		return address;
	}

}
