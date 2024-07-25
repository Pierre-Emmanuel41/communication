package fr.pederobien.communication.event;

import java.util.StringJoiner;

import fr.pederobien.communication.interfaces.IConnection;

public class DataEvent extends ConnectionEvent {
	private String address;
	private int port;
	private byte[] data;

	/**
	 * Creates a data event.
	 * 
	 * @param connection The connection that received data.
	 * @param address    The address from which the data has been received.
	 * @param port       The port number of the remote.
	 * @param data       The array that contains the data.
	 */
	public DataEvent(IConnection connection, String address, int port, byte[] data) {
		super(connection);
		this.address = address;
		this.data = data;
	}
	
	/**
	 * @return The address from which the data has been received.
	 */
	public String getAddress() {
		return address;
	}
	
	/**
	 * @return The port number of the remote.
	 */
	public int getPort() {
		return port;
	}
	
	/**
	 * @return The bytes array that contains the data.
	 */
	public byte[] getData() {
		return data;
	}
	
	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("connection=" + getConnection());
		joiner.add("length=" + getData().length);
		return String.format("%s_%s", getName(), joiner);
	}
}
