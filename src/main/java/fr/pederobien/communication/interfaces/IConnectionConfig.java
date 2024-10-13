package fr.pederobien.communication.interfaces;

import fr.pederobien.communication.interfaces.IConnection.Mode;

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
	 * @return The direction of the communication.
	 */
	Mode getMode();

	/**
	 * @return The size of the bytes array used to receive data from the remote.
	 */
	int getReceivingBufferSize();

	/**
	 * Set the size, in bytes, of the buffer to receive data from the remote.
	 * 
	 * @param receivingBufferSize The size, in bytes, of the buffer.
	 */
	void setReceivingBufferSize(int receivingBufferSize);

	/**
	 * @return True if an unexpected request has been received and should be executed, false otherwise.
	 */
	boolean isAllowUnexpectedRequest();

	/**
	 * Handle or not a request when received from the remote.
	 * 
	 * @param isAllowUnexpectedRequest True to handle an unexpected request, false to ignore.
	 */
	void setAllowUnexpectedRequest(boolean isAllowUnexpectedRequest);

	/**
	 * @return The layer responsible to encode/decode data.
	 */
	ILayer getLayer();

	/**
	 * Set the layer to encode/decode data.
	 * 
	 * @param layer The layer to encode/decode data.
	 */
	void setLayer(ILayer layer);

	/**
	 * @return The handler to execute when an unexpected request has been received from the remote.
	 */
	IRequestReceivedHandler getRequestReceivedHandler();
}
