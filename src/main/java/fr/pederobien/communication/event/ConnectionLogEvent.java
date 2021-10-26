package fr.pederobien.communication.event;

import java.util.StringJoiner;

import fr.pederobien.communication.interfaces.IConnection;

public class ConnectionLogEvent extends ConnectionEvent {

	public enum ELogLevel {
		DEBUG, INFO, WARNING, ERROR
	}

	private ELogLevel level;
	private String message;
	private Exception exception;

	/**
	 * Creates a log event.
	 * 
	 * @param connection The connection source involved in this event.
	 * @param level      The log level.
	 * @param message    The log message.
	 * @param exception  The exception source of the log message.
	 */
	public ConnectionLogEvent(IConnection<?> connection, ELogLevel level, String message, Exception exception) {
		super(connection);
		this.level = level;
		this.message = message;
		this.exception = exception;
	}

	/**
	 * @return The log level.
	 */
	public ELogLevel getLevel() {
		return level;
	}

	/**
	 * @return The log message.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return The log exception (can be null).
	 */
	public Exception getException() {
		return exception;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("connection=" + getConnection());
		joiner.add("level=" + getLevel());
		joiner.add("message=" + getMessage());
		joiner.add("Exception=" + getException());
		return String.format("%s_%s", getName(), joiner);
	}
}
