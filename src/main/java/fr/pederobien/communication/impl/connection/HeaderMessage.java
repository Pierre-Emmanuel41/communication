package fr.pederobien.communication.impl.connection;

import java.util.concurrent.atomic.AtomicInteger;

import fr.pederobien.communication.interfaces.connection.IHeaderMessage;
import fr.pederobien.communication.interfaces.connection.IMessage;

public class HeaderMessage implements IHeaderMessage {
	private static final AtomicInteger IDENTIFIER_GENERATOR = new AtomicInteger(1);
	private int identifier;
	private int requestID;
	private IMessage message;
	
	/**
	 * Create a header of a message. In case the message is a response to another request,
	 * the requestID must be the original request identifier. It should be set to 0 when the message
	 * is not a response.
	 * 
	 * @param identifier The identifier of this request.
	 * @param requestID The identifier of the request associated to this response.
	 * @param message The response of the request.
	 */
	private HeaderMessage(int identifier, int requestID, IMessage message) {
		this.identifier = identifier;
		this.requestID = requestID;
		this.message = message;		
	}
	
	/**
	 * Create a header of a message. In case the message is a response to another request,
	 * the requestID must be the original request identifier. It should be set to 0 when the message
	 * is not a response.
	 * 
	 * @param identifier The identifier of this request.
	 * @param requestID The identifier of the request associated to this response.
	 * @param bytes The bytes array of the message.
	 */
	public HeaderMessage(int identifier, int requestID, byte[] bytes) {
		this(identifier, requestID, new Message(bytes));
	}

	/**
	 * Create a header of a message.
	 * 
	 * @param requestID The identifier of the request associated to this response.
	 * @param message The response of the request.
	 */
	public HeaderMessage(int requestID, IMessage message) {
		this(IDENTIFIER_GENERATOR.getAndIncrement(), requestID, message);
	}
	
	@Override
	public int getIdentifier() {
		return identifier;
	}
	
	@Override
	public int getRequestID() {
		return requestID;
	}
	
	@Override
	public byte[] getBytes() {
		return getMessage().getBytes();
	}
	
	/**
	 * @return The wrapped message.
	 */
	public IMessage getMessage() {
		return message;
	}
}
