package fr.pederobien.communication.interfaces;

import fr.pederobien.communication.impl.layer.SimpleLayer;
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
	 * @return The layer responsible to encode/decode data. The default layer is {@link SimpleLayer}.
	 */
	ILayer getLayer();
	
	/**
	 * @return The handler to execute when an unexpected request has been received from the remote.
	 *         The default handler do nothing, it is highly recommended to override it.
	 */
	IRequestReceivedHandler getRequestReceivedHandler();
}
