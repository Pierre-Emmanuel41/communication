package fr.pederobien.communication.impl.connection;

import java.util.function.Consumer;

import fr.pederobien.communication.interfaces.connection.ICallback;
import fr.pederobien.communication.interfaces.connection.IMessage;
import fr.pederobien.communication.interfaces.connection.ICallback.CallbackArgs;

public class Message implements IMessage {
	private byte[] bytes;
	private boolean isSync;
	private ICallback callback;
	
	/**
	 * Creates a message to send to the remote.
	 * 
	 * @param bytes The bytes of the message.
	 * @param isSync True if this message shall be sent synchronously, false to send it asynchronously.
	 * @param timeout The maximum time, in ms, to wait for remote response.
	 * @param callback The code to execute once a response has been received or a timeout occurs.
	 */
	public Message(byte[] bytes, boolean isSync, int timeout, Consumer<CallbackArgs> callback) {
		this.bytes = bytes;
		this.isSync = isSync;

		this.callback = new Callback(timeout, callback);
	}
	
	/**
	 * Creates a message to send to the remote. The timeout has default value 1000 ms.
	 * 
	 * @param bytes The bytes of the message.
	 * @param isSync True if this message shall be sent synchronously, false to send it asynchronously.
	 * @param callback The code to execute once a response has been received or a timeout occurs.
	 */
	public Message(byte[] bytes, boolean isSync, Consumer<CallbackArgs> callback) {
		this(bytes, isSync, 1000, callback);
	}

	/**
	 * Creates a message to send to the remote asynchronously.
	 * 
	 * @param bytes The bytes of the message.
	 * @param timeout The maximum time, in ms, to wait for remote response.
	 * @param callback The code to execute once a response has been received or a timeout occurs.
	 */
	public Message(byte[] bytes, int timeout, Consumer<CallbackArgs> callback) {
		this(bytes, false, timeout, callback);
	}

	/**
	 * Creates a message to send to the remote asynchronously. The timeout has default value 1000 ms.
	 * 
	 * @param bytes The bytes of the message.
	 * @param callback The code to execute once a response has been received or a timeout occurs.
	 */
	public Message(byte[] bytes, Consumer<CallbackArgs> callback) {
		this(bytes, false, 1000, callback);
	}

	/**
	 * Creates a message to send to the remote. No callback.
	 * 
	 * @param bytes The bytes of the message.
	 * @param isSync True if this message shall be sent synchronously, false to send it asynchronously.
	 */
	public Message(byte[] bytes, boolean isSync) {
		this(bytes, isSync, -1, args -> {});
	}

	/**
	 * Creates a message to send to the remote. No callback.
	 * 
	 * @param bytes The bytes of the message.
	 */
	public Message(byte[] bytes) {
		this(bytes, false);
	}

	@Override
	public byte[] getBytes() {
		return bytes;
	}

	@Override
	public boolean isSync() {
		return isSync;
	}

	@Override
	public ICallback getCallback() {
		return callback;
	}

	private class Callback implements ICallback {
		private int timeout;
		private Consumer<CallbackArgs> callback;

		/**
		 * Creates a callback to be executed once a response has been received from the remote.
		 * 
		 * @param timeout The maximum time, in ms, to wait for remote response.
		 * @param callback The code to execute once a response has been received or a timeout occurs.
		 */
		public Callback(int timeout, Consumer<CallbackArgs> callback) {
			this.timeout = timeout;
			this.callback = callback;
		}

		@Override
		public int getTimeout() {
			return timeout;
		}

		@Override
		public void apply(CallbackArgs args) {
			if (callback != null)
				callback.accept(args);
		}
	}
}
