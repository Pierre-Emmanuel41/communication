package fr.pederobien.communication.impl;

import fr.pederobien.communication.interfaces.ICommonConfig;
import fr.pederobien.communication.interfaces.IConnectionConfig;
import fr.pederobien.communication.interfaces.ILayer;

public class ConnectionConfigBuilder {
	private String address;
	private int port;
	private ICommonConfig config;
	
	/**
	 * Creates a builder in order to configure a connection.
	 * 
	 * @param address The IP address of the remote.
	 * @param port The port number of the remote.
	 * @param config The object that holds common connection parameters.
	 */
	protected ConnectionConfigBuilder(String address, int port, ICommonConfig config) {
		this.address = address;
		this.port = port;
		this.config = config;
	}
	
	/**
	 * @return The IP address of the remote.
	 */
	private String getAddress() {
		return address;
	}
	
	/**
	 * @return The port number of the remote.
	 */
	private int getPort() {
		return port;
	}
	
	/**
	 * @return The client configuration that holds connection configuration parameters.
	 */
	private ICommonConfig getConfig() {
		return config;
	}
	
	/**
	 * @return The configuration to use for a connection.
	 */
	public IConnectionConfig build() {
		return new ClientConfig(this);
	}
	
	private class ClientConfig implements IConnectionConfig {
		private ConnectionConfigBuilder builder;
		
		public ClientConfig(ConnectionConfigBuilder builder) {
			this.builder = builder;
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
		public int getReceivingBufferSize() {
			return builder.getConfig().getReceivingBufferSize();
		}
		
		@Override
		public boolean isAllowUnexpectedRequest() {
			return builder.getConfig().isAllowUnexpectedRequest();
		}
		
		@Override
		public ILayer getLayer() {
			return builder.getConfig().getLayer();
		}
	}
}
