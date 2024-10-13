package fr.pederobien.communication.impl.connection;

import fr.pederobien.communication.interfaces.IConfiguration;
import fr.pederobien.communication.interfaces.IConnectionConfig;
import fr.pederobien.communication.interfaces.ILayer;
import fr.pederobien.communication.interfaces.IRequestReceivedHandler;
import fr.pederobien.communication.interfaces.IConnection.Mode;

public class ConnectionConfigBuilder {
	private String address;
	private int port;
	private IConfiguration config;
	
	/**
	 * Creates a builder in order to configure a connection.
	 * 
	 * @param address The IP address of the remote.
	 * @param port The port number of the remote.
	 * @param config The object that holds connection configuration.
	 */
	public ConnectionConfigBuilder(String address, int port, IConfiguration config) {
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
	private IConfiguration getConfig() {
		return config;
	}
	
	/**
	 * @return The configuration to use for a connection.
	 */
	public IConnectionConfig build() {
		return new ConnectionConfig(this);
	}
	
	private class ConnectionConfig implements IConnectionConfig {
		private ConnectionConfigBuilder builder;
		
		public ConnectionConfig(ConnectionConfigBuilder builder) {
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
		public Mode getMode() {
			return builder.getConfig().getMode();
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
		
		@Override
		public IRequestReceivedHandler getRequestReceivedHandler() {
			return builder.getConfig().getRequestReceivedHandler();
		}
	}
}
