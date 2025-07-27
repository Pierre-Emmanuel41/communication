package fr.pederobien.communication.impl.server;

import fr.pederobien.communication.interfaces.connection.IConnectionImpl;
import fr.pederobien.communication.interfaces.server.IClientInfo;

public class ClientInfo<T> implements IClientInfo<T> {
    private final T endPoint;
    private final IConnectionImpl implementation;

    /**
     * Creates a client info to be checked before creating a client.
     *
     * @param endPoint       The remote end point.
     * @param implementation The connection implementation.
     */
    public ClientInfo(T endPoint, IConnectionImpl implementation) {
        this.endPoint = endPoint;
        this.implementation = implementation;
    }

    @Override
    public T getEndPoint() {
        return endPoint;
    }

    @Override
    public IConnectionImpl getImpl() {
        return implementation;
    }
}
