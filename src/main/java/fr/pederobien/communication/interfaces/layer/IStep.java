package fr.pederobien.communication.interfaces.layer;

import fr.pederobien.communication.interfaces.IToken;

public interface IStep {

	/**
	 * Execute this step of the initialisation sequence.
	 * 
	 * @param token The token to use to send/receive data from the remote.
	 * 
	 * @return The layer to use for the next step if there is one,
	 *         the layer to use once initialised otherwise.
	 */
	ILayer apply(IToken token);
	
	/**
	 * @return The next step of the initialisation, can be null.
	 */
	default IStep getNext() {
		return null;
	}
}
