package fr.pederobien.communication.interfaces;

public interface IResponseMessage {

	/**
	 * @return The array of byte received from the remote.
	 */
	byte[] getBytes();

	/**
	 * Implementation specific identifier that shall return the same value for the instance of the IRequestMessage it is a response
	 * for.
	 * 
	 * @return The identifier of the request message associated to this response message.
	 */
	int getRequestIdentifier();
}
