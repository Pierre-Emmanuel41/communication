package fr.pederobien.communication.event;

public class UnexpectedDataReceivedEvent {
	private int identifier;
	private byte[] answer;

	public UnexpectedDataReceivedEvent(int identifier, byte[] answer) {
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
