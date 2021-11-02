package fr.pederobien.communication.event;

import java.net.InetSocketAddress;
import java.util.StringJoiner;

import fr.pederobien.communication.interfaces.IConnection;

public class UnexpectedDataReceivedEvent extends DataEvent {
	private int identifier;
	private byte[] answer;

	/**
	 * Creates a data received event.
	 * 
	 * @param connection The connection that received data.
	 * @param buffer     The buffer that contains the bytes of a response received from the remote.
	 * @param length     The length of the raw data received from the remote.
	 */
	public UnexpectedDataReceivedEvent(IConnection connection, int identifier, byte[] answer) {
		super(connection);
		this.identifier = identifier;
		this.answer = answer;
	}

	/**
	 * Creates a data received event.
	 * 
	 * @param connection The connection that received data.
	 * @param address    The address from which the data has been received.
	 * @param buffer     The buffer that contains the bytes of a response received from the remote.
	 * @param length     The length of the raw data received from the remote.
	 */
	public UnexpectedDataReceivedEvent(IConnection connection, InetSocketAddress address, int identifier, byte[] answer) {
		super(connection, address);
		this.identifier = identifier;
		this.answer = answer;
	}

	/**
	 * @return The identifier associated to the not expected received data.
	 */
	public int getIdentifier() {
		return identifier;
	}

	/**
	 * @return The byte array that correspond to a not expected data.
	 */
	public byte[] getAnswer() {
		return answer;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("connection=" + getConnection());
		joiner.add("identifier=" + getIdentifier());
		joiner.add("Length=" + getAnswer().length);
		return String.format("%s_%s", getName(), joiner);
	}
}
