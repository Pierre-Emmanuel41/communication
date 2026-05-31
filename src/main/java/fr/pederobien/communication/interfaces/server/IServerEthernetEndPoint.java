package fr.pederobien.communication.interfaces.server;

public interface IServerEthernetEndPoint {

	/**
	 * @return The IP address of the remote.
	 */
	String getAddress();

	/**
	 * @return -1 if not defined, the server port number otherwise.
	 */
	int getPort();

	/**
	 * Set the port number of this end point. It can be set if and only if the previous value was less or equals 0.
	 *
	 * @param port The port number of the end point.
	 */
	void setPort(int port);

	/**
	 * @return -1 if not defined, the minimum value of the server port.
	 */
	int getMin();

	/**
	 * @return -1 if not defined, the maximum value of the server port.
	 */
	int getMax();
}
