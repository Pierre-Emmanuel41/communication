package fr.pederobien.communication.impl;

import fr.pederobien.communication.interfaces.client.IClientConfig;
import fr.pederobien.communication.interfaces.connection.IConnection.Mode;

public class ClientConfig extends Configuration implements IClientConfig {
	private String address;
	private int port;
	private int connectionTimeout;
	private boolean automaticReconnection;
	private int reconnectionDelay;
	private int maxUnstableCounter;

	/**
	 * Creates a configuration that holds parameters for a client.
	 * 
	 * @param address The address of the remote.
	 * @param port The port number of the remote.
	 */
	protected ClientConfig(String address, int port) {
		super(Mode.CLIENT_TO_SERVER);

		this.address = address;
		this.port = port;

		connectionTimeout = 500;
		automaticReconnection = true;
		reconnectionDelay = 500;
		maxUnstableCounter = 5;
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
	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	/**
	 * Set the timeout value, in ms, when client attempt to connect to the remote. The default value 500ms
	 * 
	 * @param connectionTimeout The timeout in ms.
	 */
	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	@Override
	public boolean isAutomaticReconnection() {
		return automaticReconnection;
	}

	/**
	 * Set if the client should automatically reconnect if a network error occurs. The default value is true.
	 * 
	 * @param automaticReconnection True to automatically reconnect, false otherwise.
	 */
	public void setAutomaticReconnection(boolean automaticReconnection) {
		this.automaticReconnection = automaticReconnection;
	}

	@Override
	public int getReconnectionDelay() {
		return reconnectionDelay;
	}

	/**
	 * Set the time, in ms, to wait before the client should try to reconnect with the server. The default value is 500ms
	 * 
	 * @param reconnectionDelay The time in ms.
	 */
	public void setReconnectionDelay(int reconnectionDelay) {
		this.reconnectionDelay = reconnectionDelay;
	}

	@Override
	public int getMaxUnstableCounterValue() {
		return maxUnstableCounter;
	}

	/**
	 * An unstable connection event is thrown if an exception is thrown 10 times in a row.
	 * It can be from the send, receive, extract, callback or unexpected algorithm. The maximum counter value
	 * corresponds to the maximum number of time a connection unstable event is thrown before stopping
	 * the automatic reconnection if is is enabled. The default value is 5, which allowing up to 50 exceptions
	 * in a row to be thrown before stopping automatic reconnection.
	 * 
	 * @param maxUnstableCounter The maximum number of time a connection unstable before stopping
	 *       the automatic reconnection.
	 */
	public void setMaxUnstableCounter(int maxUnstableCounter) {
		this.maxUnstableCounter = maxUnstableCounter;
	}
}
