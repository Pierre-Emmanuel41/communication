package fr.pederobien.communication.interfaces.connection;

import fr.pederobien.communication.interfaces.IUnexpectedRequestHandler;
import fr.pederobien.communication.interfaces.connection.IConnection.Mode;
import fr.pederobien.communication.interfaces.layer.ILayerInitializer;

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
	 * @return The layer responsible to encode/decode data.
	 */
	ILayerInitializer getLayerInitializer();

	/**
	 * @return The handler to execute when an unexpected request has been received from the remote.
	 */
	IUnexpectedRequestHandler getOnUnexpectedRequestReceived();
}
