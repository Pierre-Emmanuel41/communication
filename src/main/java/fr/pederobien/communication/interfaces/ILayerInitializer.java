package fr.pederobien.communication.interfaces;

public interface ILayerInitializer {

	/**
	 * Perform some action to initialise the layer to use in normal state of the connection.
	 * 
	 * @param token The token to use when exchange with the remote shall be done.
	 * 
	 * @return True if the layer is successfully initialize, false otherwise.
	 */
	boolean initialize(IToken token) throws Exception ;
	
	/**
	 * @return The layer to use to send/receive data from the remote.
	 */
	ILayer getLayer();
	
	/**
	 * @return A copy of the original initializer.
	 *         The original will be left unmodified whereas the copy will
	 *         be specific for each connection.
	 */
	ILayerInitializer copy();
}
