package fr.pederobien.communication.impl;

import fr.pederobien.communication.interfaces.IEthernetEndPoint;

public class EthernetEndPoint implements IEthernetEndPoint {
    private final String toString;
    private final String address;
    private final int port;

    /**
     * Create an end point for network communication.
     *
     * @param address The remote IP address.
     * @param port    The remote port number.
     */
    public EthernetEndPoint(String address, int port) {
        this.address = address;
        this.port = port;

        toString = String.format("%s:%s", address, port);
    }

    /**
     * Create an end point for ethernet communication.
     *
     * @param port The port number of the point
     */
    public EthernetEndPoint(int port) {
        this("*", port);
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return toString;
    }
}
