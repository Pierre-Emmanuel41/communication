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
	 * @return The layer responsible to encode/decode data.
	 */
	ILayerInitializer getLayerInitializer();

	/**
	 * @return The handler to execute when an unexpected request has been received from the remote.
	 */
	IUnexpectedRequestHandler getOnUnexpectedRequestReceived();
}
