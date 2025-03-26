package fr.pederobien.communication.interfaces.layer;

import java.util.List;

import fr.pederobien.communication.interfaces.connection.IHeaderMessage;

public interface ILayer {

	/**
	 * Pack the identifier and the payload in order to be sent to the remote.
	 * 
	 * @param identifier The identifier of the message to send.
	 * @param payload    The data to send.
	 * 
	 * @return The bytes array to send to the remote.
	 */
	byte[] pack(IHeaderMessage message) throws Exception;

	/**
	 * When bytes are received from the remote, it is possible the array contains
	 * several answers. This method parse the received byte array in order to
	 * extract complete and not complete answers.
	 * 
	 * @param raw the bytes array received from the remote.
	 * 
	 * @return A map that contains all complete answers and their unique identifier.
	 */
	List<IHeaderMessage> unpack(byte[] raw) throws Exception;
}
