package fr.pederobien.communication.interfaces;

public interface ICommonConfig {
	
	/**
	 * @return The size, in bytes, of the buffer used to receive data from the remote.
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
}
