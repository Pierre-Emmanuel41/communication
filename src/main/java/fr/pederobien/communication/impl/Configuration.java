package fr.pederobien.communication.impl;

import fr.pederobien.communication.impl.layer.LayerInitializer;
import fr.pederobien.communication.interfaces.IConfiguration;
import fr.pederobien.communication.interfaces.IUnexpectedRequestHandler;
import fr.pederobien.communication.interfaces.connection.IConnection.Mode;
import fr.pederobien.communication.interfaces.layer.ILayerInitializer;

public class Configuration implements IConfiguration {
	private Mode mode;
	private ILayerInitializer layerInitializer;
	private IUnexpectedRequestHandler onUnexpectedRequestReceived;
	private int connectionMaxUnstableCounter;
	private int connectionHealTime;

	/**
	 * Creates a configuration that holds parameters for a connection.
	 * 
	 * @param mode The direction of the communication.
	 */
	public Configuration(Mode mode) {
		this.mode = mode;

		layerInitializer = new LayerInitializer();
		onUnexpectedRequestReceived = event -> {
		};

		connectionMaxUnstableCounter = 10;
		connectionHealTime = 1000;
	}

	@Override
	public Mode getMode() {
		return mode;
	}

	@Override
	public ILayerInitializer getLayerInitializer() {
		return layerInitializer;
	}

	/**
	 * Set how a layer must be initialized.
	 * 
	 * @param layerInitializer The initialisation sequence.
	 */
	public void setLayerInitializer(ILayerInitializer layerInitializer) {
		this.layerInitializer = layerInitializer;
	}

	@Override
	public IUnexpectedRequestHandler getOnUnexpectedRequestReceived() {
		return onUnexpectedRequestReceived;
	}

	/**
	 * Set the handler to execute when an unexpected request has been received from
	 * the remote. The default handler to nothing.
	 * 
	 * @param onUnexpectedRequestReceived The handler to call.
	 */
	public void setOnUnexpectedRequestReceived(IUnexpectedRequestHandler onUnexpectedRequestReceived) {
		this.onUnexpectedRequestReceived = onUnexpectedRequestReceived;
	}

	@Override
	public int getConnectionMaxUnstableCounterValue() {
		return connectionMaxUnstableCounter;
	}

	/**
	 * The connection to the remote is monitored so that if an error is happening, a
	 * counter is incremented automatically. The connection max counter value is the
	 * maximum value the unstable counter can reach before throwing an connection
	 * unstable event.
	 * 
	 * @param connectionMaxUnstableCounter The maximum value the connection's
	 *                                     unstable counter can reach.
	 */
	public void setConnectionMaxUnstableCounter(int connectionMaxUnstableCounter) {
		this.connectionMaxUnstableCounter = connectionMaxUnstableCounter;
	}

	@Override
	public int getConnectionHealTime() {
		return connectionHealTime;
	}

	/**
	 * The connection to the remote is monitored so that if an error is happening, a
	 * counter is incremented automatically. During the connection life time, it is
	 * likely possible that the connection become unstable. However, if the
	 * connection is stable the counter value should be 0 as no error happened for a
	 * long time. The heal time, in milliseconds, is the time after which the
	 * connection's error counter is decremented.
	 * 
	 * @param connectionHealTime The time, in ms, after which the connection's error
	 *                           counter is decremented.
	 */
	public void setConnectionHealTime(int connectionHealTime) {
		this.connectionHealTime = connectionHealTime;
	}
}
