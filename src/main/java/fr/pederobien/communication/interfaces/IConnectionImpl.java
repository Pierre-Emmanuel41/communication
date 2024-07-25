package fr.pederobien.communication.interfaces;

public interface IConnectionImpl {

	/**
	 * Connection specific implementation to send a message to the remote.
	 * The bytes array is the result of the layer that has encapsulated the payload
	 * with other information in order to be received correctly.
	 * 
	 * @param data The byte array to send to the remote.
	 */
	void sendImpl(byte[] data) throws Exception;

	/**
	 * Connection specific implementation to receive bytes from the remote.
	 * 
	 * @param receivingBufferSize The size of the bytes array used to receive data from the remote.
	 */
	byte[] receiveImpl(int receivingBufferSize) throws Exception;
}
