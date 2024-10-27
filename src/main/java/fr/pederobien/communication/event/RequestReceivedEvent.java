package fr.pederobien.communication.event;

import java.util.StringJoiner;

import fr.pederobien.communication.interfaces.connection.IConnection;

public class RequestReceivedEvent extends DataEvent {
	private int identifier;

	/**
	 * Creates a request received event. It is possible to set a response to the request.
	 * 
	 * @param connection The connection on which the request has been received.
	 * @param request The request received from the remote.
	 * @param identifier The identifier of the unexpected request.
	 */
	public RequestReceivedEvent(IConnection connection, byte[] request, int identifier) {
		super(connection, request);
		this.identifier = identifier;
	}
	
	/**
	 * @return The identifier of the unexpected request.
	 */
	public int getIdentifier() {
		return identifier;
	}
	
	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("connection=" + getConnection());
		joiner.add("length=" + getData().length);
		joiner.add("identifier=" + getIdentifier());
		return String.format("%s_%s", getName(), joiner);
	}
}
