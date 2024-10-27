package fr.pederobien.communication.event;

import java.util.StringJoiner;

import fr.pederobien.communication.interfaces.connection.IConnection;

public class DataEvent extends ConnectionEvent {
	private byte[] data;

	/**
	 * Creates a data event.
	 * 
	 * @param data The bytes array received from the remote.
	 */
	public DataEvent(IConnection connection, byte[] data) {
		super(connection);
		this.data = data;
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
