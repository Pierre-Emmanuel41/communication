package fr.pederobien.communication.interfaces;

public interface IMessage {
	
	/**
	 * @return The bytes to send to a remote. The identifier does not need to be present in the array
	 * as it is used for internal purpose.
	 */
	byte[] getBytes();
}
