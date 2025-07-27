package fr.pederobien.communication.interfaces.layer;

import fr.pederobien.communication.interfaces.IToken;

public interface ILayerInitializer {

    /**
     * Perform some action to initialise the layer to use in normal state of the
     * connection.
     *
     * @param token The token to use when exchange with the remote shall be done.
     * @return True if the layer is successfully initialized, false otherwise.
     */
    boolean initialize(IToken token) throws Exception;

    /**
     * @return The layer to use to send/receive data from the remote.
     */
    ILayer getLayer();
}
