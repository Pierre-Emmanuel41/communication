package fr.pederobien.communication.interfaces;

import java.util.function.Consumer;

import fr.pederobien.communication.ResponseCallbackArgs;

public interface IRequestMessage {

	/**
	 * @return The bytes to send to a device.
	 */
	byte[] getBytes();

	/**
	 * @return Callback function to be called when response is received or when timeout occurs.
	 */
	Consumer<ResponseCallbackArgs> getCallback();

	/**
	 * @return The timeout in milliseconds for this request.
	 */
	long getTimeout();

	/**
	 * Implementation specific identifier that shall ensure uniqueness with most probability between the different requests of the
	 * implemented message.
	 * 
	 * @return The request unique identifier.
	 */
	int getUniqueIdentifier();
}
