package fr.pederobien.communication.interfaces.server;

public interface IEthernetServer extends IServer {

	/**
	 * @return The port number this server is using. -1 if the server is not yet opened or has been closed.
	 */
	int getPort();
}
