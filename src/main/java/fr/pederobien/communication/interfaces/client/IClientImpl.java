package fr.pederobien.communication.interfaces.client;

import fr.pederobien.communication.interfaces.connection.IConnectionImpl;

public interface IClientImpl<T> {

	/**
	 * Client implementation specific to connect to the remote.
	 * 
	 * @param name     The client name.
	 * @param endPoint The object that gather remote information.
	 * @param timeout  The value considered as a timeout in ms when the client tries
	 *                 to connect to a server
	 * 
	 * @throws Exception If an exception is thrown, it will be caught and a
	 *                   reconnection will be attempted.
	 */
	IConnectionImpl connect(String name, T endPoint, int timeout) throws Exception;
}