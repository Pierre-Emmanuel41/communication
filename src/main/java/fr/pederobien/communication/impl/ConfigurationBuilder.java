package fr.pederobien.communication.impl;

import java.util.function.Supplier;

import fr.pederobien.communication.impl.layer.SimpleLayer;
import fr.pederobien.communication.interfaces.IConfiguration;
import fr.pederobien.communication.interfaces.ILayer;
import fr.pederobien.communication.interfaces.IRequestReceivedHandler;

public class ConfigurationBuilder {
	private int receivingBufferSize;
	private boolean allowUnexpectedRequest;
	private ILayer layer;
	private Supplier<IRequestReceivedHandler> handler;
	
	public ConfigurationBuilder() {
		receivingBufferSize = 1024;
		allowUnexpectedRequest = true;
		layer = new SimpleLayer();
		handler = () -> new SimpleRequestReceivedHandler();
	}
	
	/**
	 * Set the size, in bytes, of the buffer to receive data from the network.
	 * 
	 * @param receivingBufferSize The size of the buffer.
	 * 
	 * @return This builder.
	 */
	public ConfigurationBuilder setReceivingBufferSize(int receivingBufferSize) {
		this.receivingBufferSize = receivingBufferSize;
		return this;
	}
	
	/**
	 * @return The size, in bytes, of the buffer to receive data from the network.
	 */
	private int getReceivingBufferSize() {
		return receivingBufferSize;
	}
	
	/**
	 * Set the behavior of the connection when an unexpected request has been received.
	 * 
	 * @param allowUnexpectedRequest True to execute the unexpected request, false otherwise.
	 * 
	 * @return This builder.
	 */
	public ConfigurationBuilder setAllowUnexpectedRequest(boolean allowUnexpectedRequest) {
		this.allowUnexpectedRequest = allowUnexpectedRequest;
		return this;
	}
	
	/**
	 * @return True if an unexpected request has been received and should be executed, false otherwise.
	 */
	private boolean isAllowUnexpectedRequest() {
		return allowUnexpectedRequest;
	}
	
	/**
	 * Set the layer responsible to encode/decode data.
	 * 
	 * @param layer The new layer.
	 * 
	 * @return This builder.
	 */
	public ConfigurationBuilder setLayer(ILayer layer) {
		this.layer = layer;
		return this;
	}
	
	/**
	 * @return The layer to encode/decode data.
	 */
	private ILayer getLayer() {
		return layer;
	}
	
	/**
	 * Set the supplier of an handler in order to get a new instance each time a new client is connected.
	 * This handler will be executed each time an unexpected request is received from the remote.
	 * 
	 * @param handler The handler used to get a new instance.
	 * 
	 * @return This builder.
	 */
	public ConfigurationBuilder setRequestReceivedHandler(Supplier<IRequestReceivedHandler> handler) {
		this.handler = handler;
		return this;
	}
	
	/**
	 * @return The handler to execute when an unexpected request has been received from the remote.
	 */
	private Supplier<IRequestReceivedHandler> getRequestReceivedHandler() {
		return handler;
	}
	
	/**
	 * @return The configuration to use for a server.
	 */
	public IConfiguration build() {
		return new Configuration(this);
	}

	private class Configuration implements IConfiguration {
		private ConfigurationBuilder builder;
		
		public Configuration(ConfigurationBuilder builder) {
			this.builder = builder;
		}

		@Override
		public int getReceivingBufferSize() {
			return builder.getReceivingBufferSize();
		}

		@Override
		public boolean isAllowUnexpectedRequest() {
			return builder.isAllowUnexpectedRequest();
		}

		@Override
		public ILayer getLayer() {
			return builder.getLayer();
		}

		@Override
		public IRequestReceivedHandler getRequestReceivedHandler() {
			return builder.getRequestReceivedHandler().get();
		}
	}
}
