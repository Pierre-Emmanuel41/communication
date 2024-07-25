package fr.pederobien.communication.impl;

import fr.pederobien.communication.interfaces.ILayer;
import fr.pederobien.communication.interfaces.IServerConfig;

public class ServerConfigBuilder {
	private String name;
	private int port;
	private int receivingBufferSize;
	private boolean allowUnexpectedRequest;
	private ILayer layer;
	
	/**
	 * Creates a builder in order to a configuration a server.
	 * 
	 * @param name The name of the server.
	 * @param port The port number of the server.
	 */
	public ServerConfigBuilder(String name, int port) {
		this.name = name;
		this.port = port;
		
		receivingBufferSize = 1024;
		allowUnexpectedRequest = true;
		layer = new SimpleLayer();
	}
	
	/**
	 * @return The name of the server.
	 */
	private String getName() {
		return name;
	}
	
	/**
	 * @return The port number of the server.
	 */
	private int getPort() {
		return port;
	}

	/**
	 * Set the size, in bytes, of the buffer to receive data from the network.
	 * 
	 * @param receivingBufferSize The size of the buffer.
	 * 
	 * @return This builder.
	 */
	public ServerConfigBuilder setReceivingBufferSize(int receivingBufferSize) {
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
	public ServerConfigBuilder setAllowUnexpectedRequest(boolean allowUnexpectedRequest) {
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
	public ServerConfigBuilder setLayer(ILayer layer) {
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
	 * @return The configuration to use for a server.
	 */
	public IServerConfig build() {
		return new ServerConfig(this);
	}
	
	private class ServerConfig extends CommonConfig implements IServerConfig {
		private ServerConfigBuilder builder;
		
		public ServerConfig(ServerConfigBuilder builder) {
			super(builder.getReceivingBufferSize(), builder.isAllowUnexpectedRequest(), builder.getLayer());
			this.builder = builder;
		}

		@Override
		public String getName() {
			return builder.getName();
		}

		@Override
		public int getPort() {
			return builder.getPort();
		}
	}
}
