package fr.pederobien.communication.event;

import java.net.InetSocketAddress;

import fr.pederobien.communication.interfaces.IConnection;

public class DataReceivedEvent extends DataEvent {
	private byte[] buffer;
	private int length;

	/**
	 * Creates a data received event.
	 * 
	 * @param connection The connection that received data.
	 * @param buffer     The raw buffer that contains the bytes received from the remote.
	 * @param length     The length of the raw data received from the remote.
	 */
	public DataReceivedEvent(IConnection<?> connection, byte[] buffer, int length) {
		super(connection);
		this.buffer = buffer;
		this.length = length;
	}

	/**
	 * Creates a data received event.
	 * 
	 * @param connection The connection that received data.
	 * @param address    The address from which the data has been received.
	 * @param buffer     The raw buffer that contains the bytes received from the remote.
	 * @param length     The length of the raw data received from the remote.
	 */
	public DataReceivedEvent(IConnection<?> connection, InetSocketAddress address, byte[] buffer, int length) {
		super(connection, address);
		this.buffer = buffer;
		this.length = length;
	}

	/**
	 * @return The buffer filled in by the remote.
	 */
	public byte[] getBuffer() {
		return buffer;
	}

	/**
	 * @return The data length in the buffer.
	 */
	public int getLength() {
		return length;
	}
}
