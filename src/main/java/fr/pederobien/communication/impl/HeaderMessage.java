package fr.pederobien.communication.impl;

import java.util.concurrent.atomic.AtomicInteger;

import fr.pederobien.communication.interfaces.IHeaderMessage;
import fr.pederobien.communication.interfaces.IMessage;

public class HeaderMessage implements IHeaderMessage {
	private static final AtomicInteger IDENTIFIER_GENERATOR = new AtomicInteger(1);
	private int ID;
	private int requestID;
	private IMessage message;
	
	/**
	 * Create a header of a message. In case the message is a response to another request,
	 * the requestID must be the original request identifier. It should be set to 0 when the message
	 * is not a response.
	 * 
	 * @param ID The identifier of this request.
	 * @param requestID The identifier of the request associated to this response.
	 * @param message The response of the request.
	 */
	public HeaderMessage(int ID, int requestID, IMessage message) {
		this.ID = ID;
		this.requestID = requestID;
		this.message = message;		
	}
	
	/**
	 * Create a header of a message. In case the message is a response to another request,
	 * the requestID must be the original request identifier. It should be set to 0 when the message
	 * is not a response.
	 * 
	 * @param ID The identifier of this request.
	 * @param requestID The identifier of the request associated to this response.
	 * @param bytes The bytes array of the message.
	 */
	public HeaderMessage(int ID, int requestID, byte[] bytes) {
		this(ID, requestID, new Message(bytes));
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
	
	/**
	 * Create a header of a message.
	 * 
	 * @param message The message that contains bytes.
	 */
	public HeaderMessage(IMessage message) {
		this(0, message);
	}
	
	/**
	 * Create a header of a message.
	 * 
	 * @param bytes The bytes array of the message.
	 */
	public HeaderMessage(byte[] bytes) {
		this(0, new Message(bytes));
	}
	
	@Override
	public int getID() {
		return ID;
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
