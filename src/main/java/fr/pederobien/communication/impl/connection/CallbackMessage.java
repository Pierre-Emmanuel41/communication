package fr.pederobien.communication.impl.connection;

import java.util.function.Consumer;

import fr.pederobien.communication.interfaces.ICallbackMessage;

public class CallbackMessage extends Message implements ICallbackMessage {
	private long timeout;
	private Consumer<CallbackArgs> callback;

	/**
	 * Creates a message that is expecting a response from the remote.
	 * 
	 * @param bytes The bytes of the message.
	 * @param timeout The timeout in ms of the message.
	 * @param callback The callback to execute if a timeout occurred or a response has been received.
	 */
	public CallbackMessage(byte[] bytes, long timeout, Consumer<CallbackArgs> callback) {
		super(bytes);
		this.timeout = timeout;
		this.callback = callback;
	}

	@Override
	public long getTimeout() {
		return timeout;
	}

	@Override
	public Consumer<CallbackArgs> getCallback() {
		return callback;
	}
}
