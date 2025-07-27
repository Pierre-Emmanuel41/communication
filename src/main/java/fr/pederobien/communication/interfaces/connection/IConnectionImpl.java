package fr.pederobien.communication.interfaces.connection;

public interface IConnectionImpl {

    /**
     * Connection specific implementation to send a message to the remote. The bytes
     * array is the result of the layer that has encapsulated the payload with other
     * information in order to be received correctly.
     *
     * @param data The byte array to send to the remote.
     */
    void send(byte[] data) throws Exception;

    /**
     * Connection specific implementation to receive bytes from the remote.
     */
    byte[] receive() throws Exception;

    /**
     * Close definitively the connection with the remote.
     */
    void dispose();
}
