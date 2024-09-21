package fr.pederobien.communication.impl;

import fr.pederobien.communication.impl.client.ClientConfigBuilder;
import fr.pederobien.communication.impl.client.CustomClient;
import fr.pederobien.communication.impl.client.TcpClientImpl;
import fr.pederobien.communication.impl.connection.ConnectionConfigBuilder;
import fr.pederobien.communication.impl.connection.CustomConnection;
import fr.pederobien.communication.impl.server.CustomServer;
import fr.pederobien.communication.impl.server.ServerConfigBuilder;
import fr.pederobien.communication.impl.server.TcpServerImpl;
import fr.pederobien.communication.interfaces.IClient;
import fr.pederobien.communication.interfaces.IClientConfig;
import fr.pederobien.communication.interfaces.IClientImpl;
import fr.pederobien.communication.interfaces.IConfiguration;
import fr.pederobien.communication.interfaces.IConnection;
import fr.pederobien.communication.interfaces.IConnection.Mode;
import fr.pederobien.communication.interfaces.IConnectionConfig;
import fr.pederobien.communication.interfaces.IConnectionImpl;
import fr.pederobien.communication.interfaces.IServer;
import fr.pederobien.communication.interfaces.IServerConfig;
import fr.pederobien.communication.interfaces.IServerImpl;

public class Communication {
	
	/**
	 * @return A builder to create a configuration common to a client and a server,
	 *         to be used by a connection configuration.
	 */
	public static final ConfigurationBuilder createConfigurationBuilder() {
		return new ConfigurationBuilder();
	}
	
	/**
	 * @return A configuration with default values, to be used by a connection configuration.
	 */
	public static final IConfiguration createDefaultConfiguration() {
		return createConfigurationBuilder().build();
	}
	
	/**
	 * Creates a builder in order to configure a connection.
	 * 
	 * @param address The IP address of the remote.
	 * @param port The port number of the remote.
	 * @param config The object that holds common connection parameters.
	 */
	public static final ConnectionConfigBuilder createConnectionConfigBuilder(String address, int port, IConfiguration config) {
		return new ConnectionConfigBuilder(address, port, config);
	}
	
	/**
	 * Creates a connection configuration.
	 * 
	 * @param address The IP address of the remote.
	 * @param port The port number of the remote.
	 */
	public static final IConnectionConfig createDefaultConnectionConfig(String address, int port) {
		return createConnectionConfigBuilder(address, port, createDefaultConfiguration()).build();
	}

	/**
	 * Create custom connection that send asynchronously messages to the remote.
	 * 
	 * @param config The object that holds the client configuration.
	 * @param implementation The connection specific implementation for sending/receiving data from the remote.
	 * @param mode Represent the direction of the connection.
	 */
	public static final IConnection createCustomConnection(IConnectionConfig config, IConnectionImpl implementation, Mode mode) {
		return new CustomConnection(config, implementation, mode);
	}

	/**
	 * Creates a builder in order to configure a client.
	 * 
	 * @param address The IP address of the server.
	 * @param port The port number of the server.
	 */
	public static final ClientConfigBuilder createClientConfigBuilder(String address, int port) {
		return new ClientConfigBuilder(address, port);
	}

	/**
	 * Creates a builder in order to configure a client.
	 * 
	 * @param address The IP address of the server.
	 * @param port The port number of the server.
	 */
	public static final IClientConfig createDefaultClientConfig(String address, int port) {
		return createClientConfigBuilder(address, port).build();
	}

	/**
	 * Create a client ready to be connected to a remote.
	 * 
	 * @param config The object that holds the client configuration.
	 * @param implementation The client specific implementation to connect/disconnect from the server.
	 */
	public static final IClient createCustomClient(IClientConfig config, IClientImpl implementation) {
		return new CustomClient(config, implementation);
	}

	/**
	 * Create a client with default configuration ready to be connected to a remote.
	 * 
	 * @param address The IP address of the server.
	 * @param port The port number of the server.
	 * @param implementation The client specific implementation to connect/disconnect from the server.
	 */
	public static final IClient createDefaultCustomClient(String address, int port, IClientImpl implementation) {
		return new CustomClient(createDefaultClientConfig(address, port), implementation);
	}

	/**
	 * Create a client with a TCP connection ready to be connected to a remote.
	 * 
	 * @param config The object that holds the client configuration.
	 */
	public static final IClient createTcpClient(IClientConfig config) {
		return createCustomClient(config, new TcpClientImpl());
	}

	/**
	 * Creates a client with a TCP connection ready to be connected to a remote.
	 * 
	 * @param address The IP address of the server.
	 * @param port The port number of the server.
	 */
	public static final IClient createDefaultTcpClient(String address, int port) {
		return createTcpClient(createDefaultClientConfig(address, port));
	}

	/**
	 * Creates a builder in order to a configuration a server.
	 * 
	 * @param name The name of the server.
	 * @param port The port number of the server.
	 */
	public static final ServerConfigBuilder createServerConfigBuilder(String name, int port) {
		return new ServerConfigBuilder(name, port);
	}

	/**
	 * Creates a default server configuration.
	 * 
	 * @param name The name of the server.
	 * @param port The port number of the server.
	 */
	public static final IServerConfig createDefaultServerConfig(String name, int port) {
		return createServerConfigBuilder(name, port).build();
	}

	/**
	 * Creates a custom server.
	 * 
	 * @param config The object that holds the server configuration.
	 * @param implementation The server specific implementation to open/close the server.
	 */
	public static final IServer createCustomServer(IServerConfig config, IServerImpl implementation) {
		return new CustomServer(config, implementation);
	}

	/**
	 * Creates a custom server with default configuration ready to be opened.
	 * 
	 * @param name The name of the server.
	 * @param port The port number of the server.
	 * @param implementation The server specific implementation to open/close the server.
	 */
	public static final IServer createDefaultCustomServer(String name, int port, IServerImpl implementation) {
		return new CustomServer(createDefaultServerConfig(name, port), implementation);
	}

	/**
	 * Creates a TCP server ready to be opened.
	 * 
	 * @param config The object that holds the server configuration.
	 */
	public static final IServer createTcpServer(IServerConfig config) {
		return createCustomServer(config, new TcpServerImpl());
	}

	/**
	 * Creates a TCP server ready to be opened.
	 * 
	 * @param name The name of the server.
	 * @param port The port number of the server.
	 */
	public static final IServer createDefaultTcpServer(String name, int port) {
		return createTcpServer(createDefaultServerConfig(name, port));
	}
}
