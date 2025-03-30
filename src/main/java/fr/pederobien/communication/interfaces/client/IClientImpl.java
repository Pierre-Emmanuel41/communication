package fr.pederobien.communication.interfaces.client;

import fr.pederobien.communication.interfaces.connection.IConnectionImpl;

public interface IClientImpl<T> {

	/**
	 * Client implementation specific to connect to the remote.
	 * 
	 * @param config The configuration of the client.
	 * 
	 * @throws Exception If an exception is thrown, it will be caught and a
	 *                   reconnection will be attempted.
	 */
	IConnectionImpl connect(String name, T endPoint, int timeout) throws Exception;
}