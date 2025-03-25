package fr.pederobien.communication.interfaces.connection;

public interface IHeaderMessage {

	/**
	 * @return The identifier of the message.
	 */
	int getIdentifier();

	/**
	 * @return The identifier of the request associated to this response.
	 */
	int getRequestID();

	/**
	 * @return The bytes of the message.
	 */
	byte[] getBytes();
}
