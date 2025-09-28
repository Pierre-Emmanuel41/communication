package fr.pederobien.communication.interfaces.layer;

import fr.pederobien.communication.interfaces.IToken;

public interface IStep {

	/**
	 * Execute this step of the initialisation sequence.
	 *
	 * @param token The token to use to send/receive data from the remote.
	 * @return The layer to use once this initialisation step is over.
	 */
	ILayer apply(IToken token);
}
