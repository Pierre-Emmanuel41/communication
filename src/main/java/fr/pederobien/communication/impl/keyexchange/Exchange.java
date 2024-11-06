package fr.pederobien.communication.impl.keyexchange;

import java.util.function.Consumer;

import fr.pederobien.communication.event.RequestReceivedEvent;
import fr.pederobien.communication.impl.connection.Message;
import fr.pederobien.communication.interfaces.IToken;
import fr.pederobien.communication.interfaces.connection.ICallback.CallbackArgs;
import fr.pederobien.communication.interfaces.connection.IConnection.Mode;
import fr.pederobien.utils.ByteWrapper;
import fr.pederobien.utils.ReadableByteWrapper;

public abstract class Exchange {
	public static final byte[] SUCCESS_PATTERN = "SUCCESS_PATTERN".getBytes();

	private IToken token;
	private String word;

	/**
	 * Creates an object to wrap a token.
	 * 
	 * @param token The token used to send/receive data from the remote.
	 * @param word The word to use when sending data to the remote.
	 */
	public Exchange(IToken token, String word) {
		this.token = token;
		this.word = word;
	}

	/**
	 * Perform the exchange with the remote.
	 * 
	 * @return True if the exchange succeed, false otherwise.
	 */
	public final boolean exchange() {
		try {
			if (token.getMode() == Mode.SERVER_TO_CLIENT)
				return doServerToClientExchange();
			else if (token.getMode() == Mode.CLIENT_TO_SERVER)
				return doClientToServerExchange();
		} catch (Exception e) {
			// Do nothing
		}

		return false;
	}

	/**
	 * Called if the token's mode is Server-to-Client.
	 * 
	 * @return True if the exchange is successful, false otherwise.
	 */
	protected abstract boolean doServerToClientExchange() throws Exception;

	/**
	 * Called if the token's mode is Client-to-Server.
	 * 
	 * @return True if the exchange is successful, false otherwise.
	 */
	protected abstract boolean doClientToServerExchange() throws Exception;

	/**
	 * Creates a message to send to the remote.
	 * 
	 * @param bytes The bytes of the message.
	 * @param timeout The maximum time, in ms, to wait for remote response.
	 * @param callback The code to execute once a response has been received or a timeout occurs.
	 */
	protected void send(byte[] bytes, int timeout, Consumer<CallbackArgs> callback) {
		token.send(new Message(pack(bytes), true, timeout, callback));
	}

	/**
	 * Creates a message to send to the remote.
	 * 
	 * @param bytes The bytes of the message.
	 */
	protected void send(byte[] bytes) {
		token.send(new Message(pack(bytes), true));
	}

	/**
	 * Creates a message to send to the remote.
	 * 
	 * @param requestID The identifier of the request to be answered.
	 * @param bytes The bytes of the message.
	 * @param timeout The maximum time, in ms, to wait for remote response.
	 * @param callback The code to execute once a response has been received or a timeout occurs.
	 */
	protected void answer(int requestID, byte[] bytes, int timeout, Consumer<CallbackArgs> callback) {
		token.answer(requestID, new Message(pack(bytes), true, timeout, callback));
	}

	/**
	 * Creates a message to send to the remote.
	 * 
	 * @param requestID The identifier of the request to be answered.
	 * @param bytes The bytes of the message.
	 */
	protected void answer(int requestID, byte[] bytes) {
		token.answer(requestID, new Message(pack(bytes), true));
	}

	/**
	 * @return Block until unexpected data has been received from the remote.
	 */
	protected RequestReceivedEvent receive() throws Exception {
		// Wait until data has been received from the remote
		RequestReceivedEvent event = token.receive();

		byte[] data = event.getData();
		if (data != null)
			data = unpack(data);

		return new RequestReceivedEvent(event.getConnection(), data, event.getIdentifier());
	}

	/**
	 * Create a bytes array that contains the payload.
	 * 
	 * @param data The raw data containing the keyword and the payload.
	 * @param consumer The code to execute if the keyword is correct.
	 * 
	 * @return Null if the given bytes array structure is wrong, the payload otherwise.
	 */
	protected void unpackAndDo(byte[] data, Consumer<byte[]> consumer) {
		byte[] payload = unpack(data);

		if (payload.length > 0)
			consumer.accept(payload);
	}

	/**
	 * Create a bytes array that contains the payload.
	 * 
	 * @param data The raw data containing the keyword and the payload.
	 * 
	 * @return Null if the given bytes array structure is wrong, the payload otherwise.
	 */
	private byte[] unpack(byte[] data) {
		ReadableByteWrapper wrapper = ReadableByteWrapper.wrap(data);

		// keyword's size
		int size = wrapper.nextInt();

		// Keyword
		String keyword = wrapper.nextString(size);

		return keyword.equals(word) ? wrapper.next(-1) : new byte[0];
	}

	/**
	 * Create a bytes array that contains the keyword first followed by the data.
	 * 
	 * @param data The data to pack.
	 * 
	 * @return A new bytes array.
	 */
	private byte[] pack(byte[] data) {
		ByteWrapper wrapper = ByteWrapper.create();
		wrapper.putString(word, true).put(data);
		return wrapper.get();
	}
}
