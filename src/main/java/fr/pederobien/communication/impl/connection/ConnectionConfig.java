package fr.pederobien.communication.impl.connection;

import fr.pederobien.communication.interfaces.IConfiguration;
import fr.pederobien.communication.interfaces.IUnexpectedRequestHandler;
import fr.pederobien.communication.interfaces.connection.IConnection.Mode;
import fr.pederobien.communication.interfaces.connection.IConnectionConfig;
import fr.pederobien.communication.interfaces.layer.ILayerInitializer;

public class ConnectionConfig implements IConnectionConfig {
	private String address;
	private int port;
	private IConfiguration configuration;

	/**
	 * Creates a configuration for a connection.
	 * 
	 * @param address       The IP address of the remote.
	 * @param port          The port number of the remote.
	 * @param configuration The configuration that holds the parameters for a
	 *                      connection.
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
	public ILayerInitializer getLayerInitializer() {
		return configuration.getLayerInitializer();
	}

	@Override
	public IUnexpectedRequestHandler getOnUnexpectedRequestReceived() {
		return configuration.getOnUnexpectedRequestReceived();
	}

	@Override
	public int getMaxUnstableCounterValue() {
		return configuration.getConnectionMaxUnstableCounterValue();
	}

	@Override
	public int getHealTime() {
		return configuration.getConnectionHealTime();
	}
}
