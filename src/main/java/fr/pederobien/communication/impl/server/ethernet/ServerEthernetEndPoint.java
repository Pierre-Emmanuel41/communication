package fr.pederobien.communication.impl.server.ethernet;

import fr.pederobien.communication.interfaces.server.IServerEthernetEndPoint;

public class ServerEthernetEndPoint implements IServerEthernetEndPoint {
	private final String address;
	private final int min, max;
	private int port;

	/**
	 * Creates a end-point for a server.
	 * 
	 * @param address The IP address of the server.
	 * @param port    The port number of the server.
	 */
	public ServerEthernetEndPoint(String address, int port) {
		this.address = address;
		this.port = port;
		min = -1;
		max = -1;
	}

	/**
	 * Creates a end-point for a server that runs of all IP addresses.
	 * 
	 * @param port The port number of the server.
	 */
	public ServerEthernetEndPoint(int port) {
		this("*", port);
	}

	/**
	 * Creates a end-point for a server.
	 * 
	 * @param address The IP address of the server.
	 * @param min     The minimum value of the port number of the server.
	 * @param max     The maximum value of the port number of the server.
	 */
	public ServerEthernetEndPoint(String address, int min, int max) {
		this.address = address;
		this.min = min;
		this.max = max;

		port = -1;
	}

	/**
	 * Creates a end-point for a server that runs of all IP addresses.
	 * 
	 * @param min The minimum value of the port number of the server.
	 * @param max The maximum value of the port number of the server.
	 */
	public ServerEthernetEndPoint(int min, int max) {
		this("*", min, max);
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
	public void setPort(int port) {
		if (this.port <= 0)
			this.port = port;
	}

	@Override
	public int getMin() {
		return min;
	}

	@Override
	public int getMax() {
		return max;
	}

	@Override
	public String toString() {
		return String.format("%s:%s", address, port);
	}
}
