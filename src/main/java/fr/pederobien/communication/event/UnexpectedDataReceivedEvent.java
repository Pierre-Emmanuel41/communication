package fr.pederobien.communication.event;

import java.net.InetSocketAddress;

public class UnexpectedDataReceivedEvent extends DataEvent {
	private int identifier;
	private byte[] answer;

	public UnexpectedDataReceivedEvent(InetSocketAddress address, int identifier, byte[] answer) {
		super(address);
		this.identifier = identifier;
		this.answer = answer;
	}

	/**
	 * @return The identifier associated to the not expected received data.
	 */
	public int getIdentifier() {
		return identifier;
	}

	/**
	 * @return The byte array that correspond to a not expected data.
	 */
	public byte[] getAnswer() {
		return answer;
	}
}
