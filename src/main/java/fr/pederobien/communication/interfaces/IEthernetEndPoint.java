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
}
