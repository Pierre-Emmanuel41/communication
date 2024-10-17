package fr.pederobien.communication.impl.server;

import fr.pederobien.communication.impl.connection.UnexpectedRequestReceivedHandler;
import fr.pederobien.communication.impl.layer.LayerInitializer;
import fr.pederobien.communication.interfaces.ILayerInitializer;
import fr.pederobien.communication.interfaces.IServerConfig;
import fr.pederobien.communication.interfaces.IUnexpectedRequestHandler;

public class ServerConfigBuilder {
	private String name;
	private int port;
	private int receivingBufferSize;
	private ILayerInitializer layerInitializer;
	private IUnexpectedRequestHandler handler;
	
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
		layerInitializer = new LayerInitializer();
		handler = new UnexpectedRequestReceivedHandler();
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
	 * Set how a layer must be initialized.
	 * 
	 * @param layerInitializer The initialisation sequence.
	 * 
	 * @return This builder.
	 */
	public ServerConfigBuilder setLayerInitializer(ILayerInitializer layerInitializer) {
		this.layerInitializer = layerInitializer;
		return this;
	}
	
	/**
	 * @return An object that specify how a layer must be initialized.
	 */
	private ILayerInitializer getLayerInitializer() {
		return layerInitializer;
	}
	
	/**
	 * Set the handler to be executed each time an unexpected request is received from the remote.
	 * 
	 * @param handler The handler to call.
	 * 
	 * @return This builder.
	 */
	public ServerConfigBuilder setOnUnexpectedRequestReceived(IUnexpectedRequestHandler handler) {
		this.handler = handler;
		return this;
	}
	
	/**
	 * @return The handler to execute when an unexpected request has been received from the remote.
	 */
	private IUnexpectedRequestHandler getOnUnexpectedRequestReceived() {
		return handler;
	}
	
	/**
	 * @return The configuration to use for a server.
	 */
	public IServerConfig build() {
		return new ServerConfig(this);
	}
	
	private class ServerConfig implements IServerConfig {
		private ServerConfigBuilder builder;
		
		public ServerConfig(ServerConfigBuilder builder) {
			this.builder = builder;
		}

		@Override
		public int getReceivingBufferSize() {
			return builder.getReceivingBufferSize();
		}

		@Override
		public ILayerInitializer getLayerInitializer() {
			return builder.getLayerInitializer();
		}

		@Override
		public IUnexpectedRequestHandler getOnUnexpectedRequestReceived() {
			return builder.getOnUnexpectedRequestReceived();
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
