package fr.pederobien.communication.interfaces;

public interface IMessage {
	
	/**
	 * @return The bytes to send to a remote. The identifier does not need to be present in the array
	 * as it is used for internal purpose.
	 */
	byte[] getBytes();

	/**
	 * @return True if this message shall be sent synchronously, false to send it asynchronously.
	 */
	boolean isSync();

	/**
	 * @return The callback to execute, if not null, when a response has been received from the remote.
	 */
	ICallback getCallback();
}
