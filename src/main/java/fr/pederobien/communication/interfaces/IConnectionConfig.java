package fr.pederobien.communication.interfaces;

public interface IConnectionConfig {

	/**
	 * @return The IP address of the remote.
	 */
	String getAddress();
	
	/**
	 * @return The port number of the remote.
	 */
	int getPort();
	
	/**
	 * @return The size of the bytes array used to receive data from the remote.
	 */
	int getReceivingBufferSize();
	
	/**
	 * @return True if an unexpected request has been received and should be executed, false otherwise.
	 */
	boolean isAllowUnexpectedRequest();
	
	/**
	 * @return The layer responsible to encode/decode data.
	 */
	ILayer getLayer();
	
	/**
	 * @return The handler to execute when an unexpected request has been received from the remote.
	 */
	IRequestReceivedHandler getRequestReceivedHandler();
}
