package fr.pederobien.communication.impl;

import fr.pederobien.communication.event.MessageEvent;
import fr.pederobien.communication.interfaces.IMessageHandler;
import fr.pederobien.communication.interfaces.client.IClientConfig;
import fr.pederobien.communication.interfaces.connection.IConnection.Mode;

public class ClientConfig<T> extends Configuration implements IClientConfig<T> {
	private final String name;
	private final T endPoint;
	private IMessageHandler messageHandler;
	private int connectionTimeout;
	private boolean automaticReconnection;
	private int reconnectionDelay;
	private int clientMaxUnstableCounter;
	private int clientHealTime;

	/**
	 * Creates a configuration that holds parameters for a client.
	 *
	 * @param name     The client's name. Essentially used for logging.
	 * @param endPoint The properties of the end point.
	 */
	protected ClientConfig(String name, T endPoint) {
		super(Mode.CLIENT_TO_SERVER);

		this.name = name;
		this.endPoint = endPoint;

		messageHandler = this::doNothing;
		connectionTimeout = 500;
		automaticReconnection = true;
		reconnectionDelay = 500;
		clientMaxUnstableCounter = 5;
		clientHealTime = 1000;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public T getEndPoint() {
		return endPoint;
	}

	@Override
	public IMessageHandler getMessageHandler() {
		return messageHandler;
	}

	/**
	 * Set the handler to execute when an unexpected request has been received from the remote. The default handler to nothing.
	 *
	 * @param messageHandler The handler to call.
	 */
	public void setMessageHandler(IMessageHandler messageHandler) {
		this.messageHandler = messageHandler;
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
	public int getClientMaxUnstableCounter() {
		return clientMaxUnstableCounter;
	}

	/**
	 * The connection to the remote is monitored so that if an error is happening, a counter is incremented automatically. The client
	 * max counter value is the maximum value the unstable counter can reach before throwing a client unstable event. This counter is
	 * incremented each time a connection unstable event is thrown.
	 *
	 * @param clientMaxUnstableCounter The maximum value the client's unstable counter can reach.
	 */
	public void setClientMaxUnstableCounter(int clientMaxUnstableCounter) {
		this.clientMaxUnstableCounter = clientMaxUnstableCounter;
	}

	@Override
	public int getClientHealTime() {
		return clientHealTime;
	}

	/**
	 * The connection to the remote is monitored so that if an error is happening, a counter is incremented automatically. During the
	 * connection lifetime, it is likely possible that the connection become unstable. However, if the connection is stable the
	 * counter value should be 0 as no error happened for a long time. The heal time, in milliseconds, is the time after which the
	 * client's error counter is decremented.
	 */
	public void setClientHealTime(int clientHealTime) {
		this.clientHealTime = clientHealTime;
	}

	private void doNothing(MessageEvent event) {

	}
}
