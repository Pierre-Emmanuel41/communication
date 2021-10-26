package fr.pederobien.communication.event;

import java.net.InetSocketAddress;

import fr.pederobien.communication.interfaces.IConnection;

public class DataEvent extends ConnectionEvent {
	private InetSocketAddress address;

	/**
	 * Creates a data event.
	 * 
	 * @param connection The connection that received data.
	 */
	public DataEvent(IConnection<?> connection) {
		super(connection);
		this.address = connection.getAddress();
	}

	/**
	 * Creates a data event.
	 * 
	 * @param connection The connection that received data.
	 * @param address    The address from which the data has been received.
	 */
	public DataEvent(IConnection<?> connection, InetSocketAddress address) {
		super(connection);
		this.address = address;
	}

	/**
	 * @return The address from which the data has been received.
	 */
	public InetSocketAddress getAddress() {
		return address;
	}
}
