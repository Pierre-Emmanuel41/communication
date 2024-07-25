package fr.pederobien.communication.event;

import java.util.StringJoiner;

import fr.pederobien.communication.interfaces.IConnection;

public class ConnectionEnableChangedEvent extends ConnectionEvent {
	private boolean isEnabled;

	/**
	 * Creates a connection enable change event.
	 * 
	 * @param connection The connection associated to this event.
	 */
	public ConnectionEnableChangedEvent(IConnection connection, boolean isEnabled) {
		super(connection);
		this.isEnabled = isEnabled;
	}
	
	/**
	 * @return True if the connection is enabled, false otherwise.
	 */
	public boolean isEnabled() {
		return isEnabled;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("connection=" + getConnection());
		joiner.add("IsEnabled=" + isEnabled());
		return String.format("%s_%s", getName(), joiner);
	}
}
