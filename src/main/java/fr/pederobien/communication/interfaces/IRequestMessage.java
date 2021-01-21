package fr.pederobien.communication.interfaces;

public interface IRequestMessage {

	/**
	 * @return The bytes to send to a device.
	 */
	byte[] getBytes();

	/**
	 * Implementation specific identifier that shall ensure uniqueness with most probability between the different requests of the
	 * implemented message.
	 * 
	 * @return The request unique identifier.
	 */
	int getUniqueIdentifier();
}
