package fr.pederobien.communication.interfaces;

import java.util.Map;

public interface IAnswersExtractor {

	/**
	 * When bytes are received from the remote, it is possible the array contains several answers. This extractor parse the received
	 * byte array in order to extract complete and not complete answers.
	 * 
	 * @param received the byte array received from the remote.
	 * @return A map that contains all complete answers and their unique identifier.
	 */
	Map<Integer, byte[]> extract(byte[] received);
}
