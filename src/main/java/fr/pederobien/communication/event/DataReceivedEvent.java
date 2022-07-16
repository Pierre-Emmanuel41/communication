package fr.pederobien.communication.event;

import java.net.InetSocketAddress;
import java.util.StringJoiner;

import fr.pederobien.communication.interfaces.IConnection;

public class DataReceivedEvent extends DataEvent {
	private byte[] buffer;

	/**
	 * Creates a data received event.
	 * 
	 * @param connection The connection that received data.
	 * @param buffer     The raw buffer that contains the bytes received from the remote.
	 */
	public DataReceivedEvent(IConnection connection, byte[] buffer) {
		super(connection);
		this.buffer = buffer;
	}

	/**
	 * Creates a data received event.
	 * 
	 * @param connection The connection that received data.
	 * @param address    The address from which the data has been received.
	 * @param buffer     The raw buffer that contains the bytes received from the remote.
	 */
	public DataReceivedEvent(IConnection connection, InetSocketAddress address, byte[] buffer) {
		super(connection, address);
		this.buffer = buffer;
	}

	/**
	 * @return The buffer filled in by the remote.
	 */
	public byte[] getBuffer() {
		return buffer;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("connection=" + getConnection());
		joiner.add("address=" + getAddress());
		joiner.add("length=" + getBuffer().length);
		return String.format("%s_%s", getName(), joiner);
	}
}
