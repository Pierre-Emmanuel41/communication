package fr.pederobien.communication.interfaces.layer;

import fr.pederobien.communication.interfaces.connection.IHeaderMessage;

import java.util.List;

public interface ILayer {

	/**
	 * Generates tte bytes array associated to the given message.
	 *
	 * @param message The message that gather the information to send to the remote.
	 * @return The bytes array to send to the remote.
	 */
	byte[] pack(IHeaderMessage message) throws Exception;

	/**
	 * When bytes are received from the remote, it is possible the array contains several answers. This method parse the received byte
	 * array in order to extract complete and not complete answers.
	 *
	 * @param raw the bytes array received from the remote.
	 * @return A list that contains all complete answers.
	 */
	List<IHeaderMessage> unpack(byte[] raw) throws Exception;
}
