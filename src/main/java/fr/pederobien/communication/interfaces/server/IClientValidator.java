package fr.pederobien.communication.interfaces.server;

public interface IClientValidator<T> {

    /**
     * Check if the client is allowed to be connected to the server.
     *
     * @param endPoint The end point to check.
     * @return True if the client is allowed, false otherwise.
     */
    boolean isValid(T endPoint);
}
