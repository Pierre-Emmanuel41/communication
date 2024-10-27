package fr.pederobien.communication.impl;

import fr.pederobien.communication.impl.layer.LayerInitializer;
import fr.pederobien.communication.interfaces.IConfiguration;
import fr.pederobien.communication.interfaces.IUnexpectedRequestHandler;
import fr.pederobien.communication.interfaces.connection.IConnection.Mode;
import fr.pederobien.communication.interfaces.layer.ILayerInitializer;

public class Configuration implements IConfiguration {
	private Mode mode;
	private int receivingBufferSize;
	private ILayerInitializer layerInitializer;
	private IUnexpectedRequestHandler onUnexpectedRequestReceived;

	/**
	 * Creates a configuration that holds parameters for a connection.
	 * 
	 * @param mode The direction of the communication.
	 */
	public Configuration(Mode mode) {
		this.mode = mode;

		receivingBufferSize = 1024;
		layerInitializer = new LayerInitializer();
		onUnexpectedRequestReceived = event -> {};
	}

	@Override
	public Mode getMode() {
		return mode;
	}

	@Override
	public int getReceivingBufferSize() {
		return receivingBufferSize;
	}

	/**
	 * Set the size, in bytes, of the buffer to handle raw data from the network. The default value is 1024.
	 * 
	 * @param receivingBufferSize The size of the buffer to receive data from the network.
	 */
	public void setReceivingBufferSize(int receivingBufferSize) {
		this.receivingBufferSize = receivingBufferSize;
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
	 * Set the handler to execute when an unexpected request has been received from the remote. The default handler to nothing.
	 * 
	 * @param onUnexpectedRequestReceived The handler to call.
	 */
	public void setOnUnexpectedRequestReceived(IUnexpectedRequestHandler onUnexpectedRequestReceived) {
		this.onUnexpectedRequestReceived = onUnexpectedRequestReceived;
	}
}
