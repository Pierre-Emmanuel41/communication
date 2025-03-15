package fr.pederobien.communication.interfaces.connection;

import fr.pederobien.communication.interfaces.IUnexpectedRequestHandler;
import fr.pederobien.communication.interfaces.connection.IConnection.Mode;
import fr.pederobien.communication.interfaces.layer.ILayerInitializer;

public interface IConnectionConfig {

	/**
	 * @return The IP address of the remote.
	 */
	String getAddress();

	/**
	 * @return The port number of the remote.
	 */
	int getPort();

	/**
	 * @return The direction of the communication.
	 */
	Mode getMode();

	/**
	 * @return The layer responsible to encode/decode data.
	 */
	ILayerInitializer getLayerInitializer();

	/**
	 * @return The handler to execute when an unexpected request has been received
	 *         from the remote.
	 */
	IUnexpectedRequestHandler getOnUnexpectedRequestReceived();

	/**
	 * An unstable connection event is thrown if an exception is thrown 10 times in
	 * a row. It can be from the send, receive, extract, callback or dispatcher
	 * method. The maximum counter value corresponds to the maximum number of time a
	 * connection unstable event is thrown before stopping the automatic
	 * reconnection if is is enabled. The default value is 5, which allowing up to
	 * 50 exceptions in a row to be thrown before stopping automatic reconnection.
	 * 
	 * @return The maximum number of time a connection unstable before stopping the
	 *         automatic reconnection.
	 */
	int getMaxUnstableCounterValue();

	/**
	 * The connection to the remote is monitored so that if an error is happening, a
	 * counter is incremented automatically. During the connection life time, it is
	 * likely possible that the connection become unstable. However, if the
	 * connection is stable the counter value should be 0 as no error happened for a
	 * long time. The heal time, in milliseconds, is the time after which the error
	 * counter is decremented.
	 * 
	 * @return The time, in ms, after which the error counter is decremented.
	 */
	int getHealTime();
}
