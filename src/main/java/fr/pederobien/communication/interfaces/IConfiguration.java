package fr.pederobien.communication.interfaces;

import fr.pederobien.communication.interfaces.connection.IConnection.Mode;
import fr.pederobien.communication.interfaces.layer.ILayerInitializer;

public interface IConfiguration {
	
	/**
	 * @return The direction of the communication.
	 */
	Mode getMode();
	
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
