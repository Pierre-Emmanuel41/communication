package fr.pederobien.communication.impl.connection;

import java.util.function.Consumer;

import fr.pederobien.communication.interfaces.ICallbackMessage;

public class CallbackMessage extends Message implements ICallbackMessage {
	private long timeout;
	private boolean isSync;
	private Consumer<CallbackArgs> callback;

	/**
	 * Creates a message that is expecting a response from the remote.
	 * 
	 * @param bytes The bytes of the message.
	 * @param timeout The timeout in ms of the message.
	 * @param isSync True if this message shall be sent synchronously, false to send it asynchronously.
	 * @param callback The callback to execute if a timeout occurred or a response has been received.
	 */
	public CallbackMessage(byte[] bytes, long timeout, boolean isSync, Consumer<CallbackArgs> callback) {
		super(bytes);
		this.timeout = timeout;
		this.isSync = isSync;
		this.callback = callback;
	}
	
	/**
	 * Creates a message that is expecting a response from the remote.
	 * 
	 * @param bytes The bytes of the message.
	 * @param timeout The timeout in ms of the message.
	 * @param callback The callback to execute if a timeout occurred or a response has been received.
	 */
	public CallbackMessage(byte[] bytes, long timeout, Consumer<CallbackArgs> callback) {
		this(bytes, timeout, false, callback);
	}

	@Override
	public long getTimeout() {
		return timeout;
	}

	@Override
	public Consumer<CallbackArgs> getCallback() {
		return callback;
	}
	
	@Override
	public boolean isSync() {
		return isSync;
	}
}
