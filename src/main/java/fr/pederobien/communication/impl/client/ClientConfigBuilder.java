package fr.pederobien.communication.impl.client;

import fr.pederobien.communication.impl.SimpleRequestReceivedHandler;
import fr.pederobien.communication.impl.layer.SimpleLayer;
import fr.pederobien.communication.interfaces.IClientConfig;
import fr.pederobien.communication.interfaces.ILayer;
import fr.pederobien.communication.interfaces.IRequestReceivedHandler;

public class ClientConfigBuilder {
	private String address;
	private int port;
	private int connectionTimeout;
	private boolean isAutomaticReconnection;
	private int reconnectionDelay;
	private int receivingBufferSize;
	private boolean allowUnexpectedRequest;
	private ILayer layer;
	private IRequestReceivedHandler handler;
	private int maxUnstableCounter;
	
	/**
	 * Creates a builder in order to configure a client.
	 * 
	 * @param address The IP address of the server.
	 * @param port The port number of the server.
	 */
	public ClientConfigBuilder(String address, int port) {
		this.address = address;
		this.port = port;
		
		connectionTimeout = 1000;
		isAutomaticReconnection = true;
		reconnectionDelay = 1000;
		receivingBufferSize = 1024;
		allowUnexpectedRequest = false;
		layer = new SimpleLayer();
		handler = new SimpleRequestReceivedHandler();
		maxUnstableCounter = 5;
	}
	
	/**
	 * @return The IP address of the server.
	 */
	private String getAddress() {
		return address;
	}
	
	/**
	 * @return The port number of the server.
	 */
	private int getPort() {
		return port;
	}
	
	/**
	 * Set the connection timeout value in ms.
	 * 
	 * @param connectionTimeout The time in ms after which a connection timeout occurs.
	 * 
	 * @return This builder.
	 */
	public ClientConfigBuilder setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
		return this;
	}
	
	/**
	 * @return The time in ms after which a connection timeout occurs.
	 */
	public int getConnectionTimeout() {
		return connectionTimeout;
	}
	
	/**
	 * Set if the client should automatically reconnect if a network error occurs.
	 * 
	 * @param connectionTimeout True to automatically reconnect, false otherwise.
	 * 
	 * @return This builder.
	 */
	public ClientConfigBuilder setAutomaticReconnection(boolean isAutomaticReconnection) {
		this.isAutomaticReconnection = isAutomaticReconnection;
		return this;
	}
	
	/**
	 * @return True if the client should automatically reconnect if a network error occurs.
	 */
	public boolean isAutomaticReconnection() {
		return isAutomaticReconnection;
	}
	
	/**
	 * Set the time after which the client should try to reconnect with the server.
	 * 
	 * @param connectionTimeout The time in ms after which the client should try to reconnect with the remote.
	 * 
	 * @return This builder.
	 */
	public ClientConfigBuilder setReconnectionDelay(int reconnectionDelay) {
		this.reconnectionDelay = reconnectionDelay;
		return this;
	}
	
	/**
	 * @return The time in ms after which the client should try to reconnect with the remote.
	 */
	private int getReconnectionDelay() {
		return reconnectionDelay;
	}
	
	/**
	 * Set the size, in byte, of the buffer to handle raw data from the network.
	 * 
	 * @param connectionTimeout The size of the buffer to receive data from the network.
	 * 
	 * @return This builder.
	 */
	public ClientConfigBuilder setReceivingBufferSize(int receivingBufferSize) {
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
	public void setAllowUnexpectedRequest(boolean allowUnexpectedRequest) {
		this.allowUnexpectedRequest = allowUnexpectedRequest;
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
	public ClientConfigBuilder setLayer(ILayer layer) {
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
	 * Set the handler to be executed each time an unexpected request is received from the remote.
	 * 
	 * @param handler The handler to call.
	 * 
	 * @return This builder.
	 */
	public ClientConfigBuilder setRequestReceivedHandler(IRequestReceivedHandler handler) {
		this.handler = handler;
		return this;
	}
	
	/**
	 * @return The handler to execute when an unexpected request has been received from the remote.
	 */
	private IRequestReceivedHandler getRequestReceivedHandler() {
		return handler;
	}
	
	/**
	 * An unstable connection event is thrown if an exception is thrown 10 times in a row.
	 * It can be from the send, receive, extract, callback or dispatcher method. The maximum counter value
	 * corresponds to the maximum number of time a connection unstable event is thrown before stopping
	 * the automatic reconnection if is is enabled. The default value is 5, which allowing up to 50 exceptions
	 * in a row to be thrown before stopping automatic reconnection.
	 * 
	 * @param maxUnstableCounter The maximum number of time a connection unstable before stopping
	 *       the automatic reconnection.
	 */
	public ClientConfigBuilder setMaxUnstableCounter(int maxUnstableCounter) {
		this.maxUnstableCounter = maxUnstableCounter;
		return this;
	}
	
	/**
	 * @return The maximum number of time a connection unstable before stopping the automatic reconnection.
	 */
	private int getMaxUnstableCounter() {
		return maxUnstableCounter;
	}
	
	/**
	 * @return The configuration to use for a client.
	 */
	public IClientConfig build() {
		return new ClientConfig(this);
	}
	
	private class ClientConfig implements IClientConfig {
		private ClientConfigBuilder builder;
		
		/**
		 * Creates a configuration for a client.
		 * 
		 * @param builder The builder that contains all the client configuration parameters.
		 */
		public ClientConfig(ClientConfigBuilder builder) {
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
			return builder.getRequestReceivedHandler();
		}

		@Override
		public String getAddress() {
			return builder.getAddress();
		}

		@Override
		public int getPort() {
			return builder.getPort();
		}

		@Override
		public int getConnectionTimeout() {
			return builder.getConnectionTimeout();
		}

		@Override
		public boolean isAutomaticReconnection() {
			return builder.isAutomaticReconnection();
		}

		@Override
		public int getReconnectionDelay() {
			return builder.getReconnectionDelay();
		}
		
		@Override
		public int getMaxUnstableCounterValue() {
			return builder.getMaxUnstableCounter();
		}
	}
}
