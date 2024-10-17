package fr.pederobien.communication.impl.connection;

import fr.pederobien.communication.interfaces.IConfiguration;
import fr.pederobien.communication.interfaces.IConnection.Mode;
import fr.pederobien.communication.interfaces.IConnectionConfig;
import fr.pederobien.communication.interfaces.ILayerInitializer;
import fr.pederobien.communication.interfaces.IUnexpectedRequestHandler;

public class ConnectionConfig implements IConnectionConfig {
	private String address;
	private int port;
	private IConfiguration configuration;
	
	/**
	 * Creates a configuration for a connection.
	 * 
	 * @param address The IP address of the remote.
	 * @param port The port number of the remote.
	 * @param configuration The configuration that holds the parameters for a connection.
	 */
	public ConnectionConfig(String address, int port, IConfiguration configuration) {
		this.address = address;
		this.port = port;
		this.configuration = configuration;
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
		return configuration.getMode();
	}

	@Override
	public int getReceivingBufferSize() {
		return configuration.getReceivingBufferSize();
	}

	@Override
	public ILayerInitializer getLayerInitializer() {
		return configuration.getLayerInitializer();
	}

	@Override
	public IUnexpectedRequestHandler getOnUnexpectedRequestReceived() {
		return configuration.getOnUnexpectedRequestReceived();
	}
}
