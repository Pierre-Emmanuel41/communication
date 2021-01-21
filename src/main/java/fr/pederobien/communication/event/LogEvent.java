package fr.pederobien.communication.event;

public class LogEvent {

	public enum ELogLevel {
		DEBUG, INFO, WARNING, ERROR
	}

	private ELogLevel level;
	private String message;
	private Exception exception;

	public LogEvent(ELogLevel level, String message, Exception exception) {
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
		return "[" + level + "] " + message;
	}
}
