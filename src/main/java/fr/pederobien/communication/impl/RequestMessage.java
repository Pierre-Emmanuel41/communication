package fr.pederobien.communication.impl;

import java.util.function.Consumer;

import fr.pederobien.communication.ResponseCallbackArgs;
import fr.pederobien.communication.interfaces.IRequestMessage;

public class RequestMessage implements IRequestMessage {
	private byte[] bytes;
	private Consumer<ResponseCallbackArgs> callback;
	private long timeout;
	private int uniqueIdentifier;

	/**
	 * Create a request message to be send to a remote.
	 * 
	 * @param bytes            The byte array to send to the remote.
	 * @param uniqueIdentifier The request identifier.
	 * @param callback         The callback to run when a response has been received before the timeout.
	 * @param timeout          The request timeout.
	 */
	public RequestMessage(byte[] bytes, int uniqueIdentifier, Consumer<ResponseCallbackArgs> callback, long timeout) {
		this.bytes = bytes;
		this.uniqueIdentifier = uniqueIdentifier;
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
	public RequestMessage(byte[] bytes, int uniqueIdentifier, Consumer<ResponseCallbackArgs> callback) {
		this(bytes, uniqueIdentifier, callback, 1000);
	}

	@Override
	public byte[] getBytes() {
		return bytes;
	}

	@Override
	public Consumer<ResponseCallbackArgs> getCallback() {
		return callback;
	}

	@Override
	public long getTimeout() {
		return timeout;
	}

	@Override
	public int getUniqueIdentifier() {
		return uniqueIdentifier;
	}
}
