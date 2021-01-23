package fr.pederobien.communication.event;

import java.net.InetSocketAddress;

public class DataReceivedEvent extends DataEvent {
	private byte[] buffer;
	private int length;

	public DataReceivedEvent(InetSocketAddress address, byte[] buffer, int length) {
		super(address);
		this.buffer = buffer;
		this.length = length;
	}

	/**
	 * @return The buffer filled in by the remote
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
