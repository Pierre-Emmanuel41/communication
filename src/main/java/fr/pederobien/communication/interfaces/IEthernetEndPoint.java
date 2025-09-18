package fr.pederobien.communication.interfaces;

public interface IEthernetEndPoint {

    /**
     * @return The IP address of the remote.
     */
    String getAddress();

    /**
     * @return The port number of the remote.
     */
    int getPort();

    /**
     * Set the port number of this end point. It can be set if and only if the previous value was 0.
     *
     * @param port The port number of the end point.
     */
    void setPort(int port);
}
