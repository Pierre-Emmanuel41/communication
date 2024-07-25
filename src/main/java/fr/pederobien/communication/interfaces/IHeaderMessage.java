package fr.pederobien.communication.interfaces;

public interface IHeaderMessage {

	/**
	 * @return The identifier of the request associated to this response.
	 */
	int getRequestID();
	
	/**
	 * @return The identifier of the message.
	 */
	int getID();
	
	/**
	 * @return The bytes of the message.
	 */
	byte[] getBytes();	
}
