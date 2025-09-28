package fr.pederobien.communication.interfaces.server;

public interface IServerImpl<T> {

	/**
	 * Server specific implementation for opening.
	 *
	 * @param config The server configuration that holds connection configuration parameters.
	 */
	void open(IServerConfig<T> config) throws Exception;

	/**
	 * Server specific implementation for closing.
	 */
	void close() throws Exception;

	/**
	 * Called in its own thread in order to create a connection with a client.
	 */
	IClientInfo<T> waitForClient() throws Exception;
}
