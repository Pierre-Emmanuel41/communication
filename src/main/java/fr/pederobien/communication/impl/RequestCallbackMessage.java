package fr.pederobien.communication.impl;

import java.util.function.Consumer;

import fr.pederobien.communication.ResponseCallbackArgs;
import fr.pederobien.communication.interfaces.ICallbackRequestMessage;

public class RequestCallbackMessage extends RequestMessage implements ICallbackRequestMessage {
	private Consumer<ResponseCallbackArgs> callback;
	private long timeout;

	/**
	 * Create a request message to be send to a remote.
	 * 
	 * @param bytes            The byte array to send to the remote.
	 * @param uniqueIdentifier The request identifier.
	 * @param callback         The callback to run when a response has been received before the timeout.
	 * @param timeout          The request timeout.
	 */
	public RequestCallbackMessage(byte[] bytes, int uniqueIdentifier, Consumer<ResponseCallbackArgs> callback, long timeout) {
		super(bytes, uniqueIdentifier);
		this.callback = callback;
		this.timeout = timeout;
	}

	/**
	 * Create a request message to be send to a remote with the default timeout of 1000ms.
	 * 
	 * @param bytes            The byte array to send to the remote.
	 * @param uniqueIdentifier The request identifier.
	 * @param callback         The callback to run when a response has been received before the timeout.
	 */
	public RequestCallbackMessage(byte[] bytes, int uniqueIdentifier, Consumer<ResponseCallbackArgs> callback) {
		this(bytes, uniqueIdentifier, callback, 1000);
	}

	/**
	 * Create a request message to be send to a remote with the default timeout of 1000ms and without callback.
	 * 
	 * @param bytes            The byte array to send to the remote.
	 * @param uniqueIdentifier The request identifier.
	 */
	public RequestCallbackMessage(byte[] bytes, int uniqueIdentifier) {
		this(bytes, uniqueIdentifier, null);
	}

	@Override
	public Consumer<ResponseCallbackArgs> getCallback() {
		return callback;
	}

	@Override
	public long getTimeout() {
		return timeout;
	}
}
