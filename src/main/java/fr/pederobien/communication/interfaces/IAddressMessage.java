package fr.pederobien.communication.interfaces;

import java.net.InetSocketAddress;

public interface IAddressMessage extends IRequestMessage {

	/**
	 * @return The address used to send this message to the remote.
	 */
	InetSocketAddress getAddress();
}
