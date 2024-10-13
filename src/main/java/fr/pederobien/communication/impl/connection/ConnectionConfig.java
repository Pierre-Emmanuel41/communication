package fr.pederobien.communication.impl.connection;

import fr.pederobien.communication.impl.SimpleRequestReceivedHandler;
import fr.pederobien.communication.impl.layer.SimpleLayer;
import fr.pederobien.communication.interfaces.IConfiguration;
import fr.pederobien.communication.interfaces.IConnectionConfig;
import fr.pederobien.communication.interfaces.ILayer;
import fr.pederobien.communication.interfaces.IRequestReceivedHandler;
import fr.pederobien.communication.interfaces.IConnection.Mode;

public class ConnectionConfig implements IConnectionConfig {
	private IConnectionConfig config;
	
	/**
	 * Creates a configuration for a connection.
	 * 
	 * @param address The IP address of the remote.
	 * @param port The port number of the remote.
	 * @param mode The direction of the communication.
	 */
	public ConnectionConfig(String address, int port, Mode mode) {
		config = new ConnectionConfigStandalone(address, port, mode);
	}
	
	/**
	 * Creates a configuration for a connection.
	 * 
	 * @param address The IP address of the remote.
	 * @param port The port number of the remote.
	 * @param configuration The configuration that holds the parameters for a connection.
	 */
	public ConnectionConfig(String address, int port, IConfiguration configuration) {
		config = new ConnectionConfigDerived(address, port, configuration);
	}
	
	@Override
	public String getAddress() {
		return config.getAddress();
	}

	@Override
	public int getPort() {
		return config.getPort();
	}

	@Override
	public Mode getMode() {
		return config.getMode();
	}

	@Override
	public int getReceivingBufferSize() {
		return config.getReceivingBufferSize();
	}

	@Override
	public void setReceivingBufferSize(int receivingBufferSize) {
		config.setReceivingBufferSize(receivingBufferSize);
	}

	@Override
	public ILayer getLayer() {
		return config.getLayer();
	}

	@Override
	public void setLayer(ILayer layer) {
		config.setLayer(layer);
	}

	@Override
	public IRequestReceivedHandler getRequestReceivedHandler() {
		return config.getRequestReceivedHandler();
	}
	
	private class ConnectionConfigDerived implements IConnectionConfig {
		private IConnectionConfig config;

		public ConnectionConfigDerived(String address, int port, IConfiguration configuration) {
			config = new ConnectionConfigStandalone(address, port, configuration.getMode());
			config.setReceivingBufferSize(configuration.getReceivingBufferSize());
			config.setLayer(config.getLayer());
		}

		@Override
		public String getAddress() {
			return config.getAddress();
		}

		@Override
		public int getPort() {
			return config.getPort();
		}

		@Override
		public Mode getMode() {
			return config.getMode();
		}

		@Override
		public int getReceivingBufferSize() {
			return config.getReceivingBufferSize();
		}

		@Override
		public void setReceivingBufferSize(int receivingBufferSize) {
			config.setReceivingBufferSize(receivingBufferSize);
		}

		@Override
		public ILayer getLayer() {
			return config.getLayer();
		}

		@Override
		public void setLayer(ILayer layer) {
			config.setLayer(layer);
		}

		@Override
		public IRequestReceivedHandler getRequestReceivedHandler() {
			return config.getRequestReceivedHandler();
		}
	}
	
	private class ConnectionConfigStandalone implements IConnectionConfig {
		private String address;
		private int port;
		private Mode mode;
		private int receivingBufferSize;
		private ILayer layer;
		private IRequestReceivedHandler handler;
		
		public ConnectionConfigStandalone(String address, int port, Mode mode) {
			this.address = address;
			this.port = port;
			this.mode = mode;

			receivingBufferSize = 1024;
			layer = new SimpleLayer();
			handler = new SimpleRequestReceivedHandler();
		}

		@Override
		public String getAddress() {
			return address;
		}

		@Override
		public int getPort() {
			return port;
		}

		@Override
		public Mode getMode() {
			return mode;
		}

		@Override
		public int getReceivingBufferSize() {
			return receivingBufferSize;
		}

		@Override
		public void setReceivingBufferSize(int receivingBufferSize) {
			this.receivingBufferSize = receivingBufferSize;
		}

		@Override
		public ILayer getLayer() {
			return layer;
		}

		@Override
		public void setLayer(ILayer layer) {
			this.layer = layer;
		}

		@Override
		public IRequestReceivedHandler getRequestReceivedHandler() {
			return handler;
		}
	}
}
