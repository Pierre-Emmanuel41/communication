package fr.pederobien.communication.impl.connection;

import fr.pederobien.communication.interfaces.IMessage;

public class Message implements IMessage {
	private byte[] bytes;
	
	/**
	 * Creates a message to send to the remote.
	 * 
	 * @param bytes The bytes of the message.
	 */
	public Message(byte[] bytes) {
		this.bytes = bytes;
	}

	@Override
	public byte[] getBytes() {
		return bytes;
	}
}
