package fr.pederobien.communication.interfaces;

import fr.pederobien.communication.interfaces.IConnection.Mode;

public interface IConfiguration {
	
	/**
	 * @return The direction of the communication.
	 */
	Mode getMode();

	/**
	 * @return The size, in bytes, of the buffer used to receive data from the remote. The default value is 1024.
	 */
	int getReceivingBufferSize();
	
	/**
	 * @return An object that specify how a layer must be initialized.
	 */
	ILayerInitializer getLayerInitializer();
	
	/**
	 * @return The handler to execute when an unexpected request has been received from the remote.
	 *         The default handler do nothing, it is highly recommended to override it.
	 */
	IUnexpectedRequestHandler getOnUnexpectedRequestReceived();
}
