package fr.pederobien.communication.interfaces;

public interface IClientImpl {

	/**
	 * Client implementation specific to connect to the remote.
	 * 
	 * @param address The address of the remote.
	 * @param port The port of the remote.
	 * @param connectionTimeout The time in ms before a connection timeout occurs.
	 * 
	 * @throws Exception If an exception is thrown, it will be caught and a reconnection will be attempted.
	 */
	void connectImpl(String address, int port, int connectionTimeout) throws Exception;

	/**
	 * Client implementation specific to disconnect from the remote.
	 */
	void disconnectImpl();

	/**
	 * A connected client does not mean it should open a connection with the remote.
	 * If while waiting for connection with the remote, the disconnect method is called, then the
	 * connection process must aborted.
	 * 
	 * @param config The configuration of the client.
	 */
	IConnection onConnectionComplete(IClientConfig config);
}
