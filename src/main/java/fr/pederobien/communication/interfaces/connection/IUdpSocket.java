package fr.pederobien.communication.interfaces.connection;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;

public interface IUdpSocket {

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
     *
     * @return The packet received from the remote.
     */
    DatagramPacket receive() throws Exception;

    /**
     * Close definitively the connection with the remote.
     */
    void close();

    /**
     * Returns the address to which the socket is connected.
     * <p>
     * If the socket was connected prior to being {@link #close closed}, then this
     * method will continue to return the connected address after the socket is
     * closed.
     *
     * @return the remote IP address to which this socket is connected, or
     * {@code null} if the socket is not connected.
     */
    InetSocketAddress getInetAddress();
}
