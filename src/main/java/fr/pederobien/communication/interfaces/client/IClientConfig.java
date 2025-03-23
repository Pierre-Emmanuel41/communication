package fr.pederobien.communication.interfaces.client;

import fr.pederobien.communication.interfaces.IConfiguration;

public interface IClientConfig<T> extends IConfiguration {

	/**
	 * @return The object that gather remote information.
	 */
	T getEndPoint();

	/**
	 * @return The client's name. Essentially used for logging.
	 */
	String getName();

	/**
	 * @return The value considered as a timeout in ms the client tries to connect
	 *         to a server. The default value is 1000 ms.
	 */
	int getConnectionTimeout();

	/**
	 * @return True if the client should try to reconnect automatically with the
	 *         server if an error occurred. The default value is true.
	 */
	boolean isAutomaticReconnection();

	/**
	 * @return The delay in ms before trying to reconnect to the server. The default
	 *         value is 1000 ms.
	 */
	int getReconnectionDelay();

	/**
	 * The connection to the remote is monitored so that if an error is happening, a
	 * counter is incremented automatically. The client max counter value is the
	 * maximum value the unstable counter can reach before throwing an client
	 * unstable event. This counter is incremented each time a connection unstable
	 * event is thrown.
	 * 
	 * @return The maximum value the client's unstable counter can reach.
	 */
	int getClientMaxUnstableCounterValue();

	/**
	 * The connection to the remote is monitored so that if an error is happening, a
	 * counter is incremented automatically. During the connection life time, it is
	 * likely possible that the connection become unstable. However, if the
	 * connection is stable the counter value should be 0 as no error happened for a
	 * long time. The heal time, in milliseconds, is the time after which the
	 * client's error counter is decremented.
	 * 
	 * @return The time, in ms, after which the client's error counter is
	 *         decremented.
	 */
	int getClientHealTime();
}