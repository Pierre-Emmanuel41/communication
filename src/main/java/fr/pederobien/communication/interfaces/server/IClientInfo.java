package fr.pederobien.communication.interfaces.server;

import fr.pederobien.communication.interfaces.connection.IConnectionImpl;

public interface IClientInfo<T> {

    /**
     * @return The remote end point.
     */
    T getEndPoint();

    /**
     * @return The connection implementation to use.
     */
    IConnectionImpl getImpl();
}
