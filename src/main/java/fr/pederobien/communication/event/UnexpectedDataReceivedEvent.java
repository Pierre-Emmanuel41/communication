package fr.pederobien.communication.event;

import java.net.InetSocketAddress;
import java.util.StringJoiner;

import fr.pederobien.communication.interfaces.IConnection;

public class UnexpectedDataReceivedEvent extends DataReceivedEvent {
	private int identifier;

	/**
	 * Creates a data received event.
	 * 
	 * @param connection The connection that received data.
	 * @param buffer     The buffer that contains the bytes of a response received from the remote.
	 * @param identifier The identifier of the unexpected message.
	 */
	public UnexpectedDataReceivedEvent(IConnection connection, byte[] buffer, int identifier) {
		super(connection, buffer);
		this.identifier = identifier;
	}

	/**
	 * Creates a data received event.
	 * 
	 * @param connection The connection that received data.
	 * @param address    The address from which the data has been received.
	 * @param buffer     The buffer that contains the bytes of a response received from the remote.
	 * @param length     The length of the raw data received from the remote.
	 */
	public UnexpectedDataReceivedEvent(IConnection connection, InetSocketAddress address, byte[] buffer, int identifier) {
		super(connection, address, buffer);
		this.identifier = identifier;
	}

	/**
	 * @return The identifier associated to the not expected received data.
	 */
	public int getIdentifier() {
		return identifier;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("connection=" + getConnection());
		joiner.add("identifier=" + getIdentifier());
		joiner.add("Length=" + getBuffer().length);
		return String.format("%s_%s", getName(), joiner);
	}
}
