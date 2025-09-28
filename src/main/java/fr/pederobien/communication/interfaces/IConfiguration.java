package fr.pederobien.communication.interfaces;

import fr.pederobien.communication.interfaces.connection.IConnection.Mode;
import fr.pederobien.communication.interfaces.layer.ILayerInitializer;

public interface IConfiguration {

	/**
	 * @return The direction of the communication.
	 */
	Mode getMode();

	/**
	 * @return An object that specify how a layer must be initialized.
	 */
	ILayerInitializer getLayerInitializer();

	/**
	 * The connection to the remote is monitored so that if an error is happening, a counter is incremented automatically. The
	 * connection max counter value is the maximum value the unstable counter can reach before throwing a connection unstable event.
	 *
	 * @return The maximum value the connection's unstable counter can reach.
	 */
	int getConnectionMaxUnstableCounter();

	/**
	 * The connection to the remote is monitored so that if an error is happening, a counter is incremented automatically. During the
	 * connection lifetime, it is likely possible that the connection become unstable. However, if the connection is stable the
	 * counter value should be 0 as no error happened for a long time. The heal time, in milliseconds, is the time after which the
	 * connection's error counter is decremented.
	 *
	 * @return The time, in ms, after which the connection's error counter is decremented.
	 */
	int getConnectionHealTime();
}
