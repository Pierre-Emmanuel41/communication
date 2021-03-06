package fr.pederobien.communication.interfaces;

import java.util.function.Consumer;

import fr.pederobien.communication.ResponseCallbackArgs;

public interface ICallbackRequestMessage extends IRequestMessage {

	/**
	 * @return The timeout in milliseconds for this request.
	 */
	long getTimeout();

	/**
	 * @return Callback function to be called when response is received or when timeout occurs.
	 */
	Consumer<ResponseCallbackArgs> getCallback();
}
