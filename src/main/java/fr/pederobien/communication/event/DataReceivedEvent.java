package fr.pederobien.communication.event;

public class DataReceivedEvent {
	private byte[] buffer;
	private int length;

	public DataReceivedEvent(byte[] buffer, int length) {
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
