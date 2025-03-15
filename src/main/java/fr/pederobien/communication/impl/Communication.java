package fr.pederobien.communication.impl;

import fr.pederobien.communication.impl.client.CustomClient;
import fr.pederobien.communication.impl.client.TcpClientImpl;
import fr.pederobien.communication.impl.connection.ConnectionConfig;
import fr.pederobien.communication.impl.connection.CustomConnection;
import fr.pederobien.communication.impl.server.CustomServer;
import fr.pederobien.communication.impl.server.TcpServerImpl;
import fr.pederobien.communication.interfaces.IConfiguration;
import fr.pederobien.communication.interfaces.client.IClient;
import fr.pederobien.communication.interfaces.client.IClientConfig;
import fr.pederobien.communication.interfaces.client.IClientImpl;
import fr.pederobien.communication.interfaces.connection.IConnection;
import fr.pederobien.communication.interfaces.connection.IConnectionConfig;
import fr.pederobien.communication.interfaces.connection.IConnectionImpl;
import fr.pederobien.communication.interfaces.server.IServer;
import fr.pederobien.communication.interfaces.server.IServerConfig;
import fr.pederobien.communication.interfaces.server.IServerImpl;

public class Communication {

	/**
	 * Creates a configuration for a connection.
	 * 
	 * @param address       The IP address of the remote.
	 * @param port          The port number of the remote.
	 * @param configuration The configuration that holds the parameters for a
	 *                      connection.
	 */
	public static final IConnectionConfig createConnectionConfig(String address, int port,
			IConfiguration configuration) {
		return new ConnectionConfig(address, port, configuration);
	}

	/**
	 * Create custom connection that send asynchronously messages to the remote.
	 * 
	 * @param config         The object that holds the client configuration.
	 * @param implementation The connection specific implementation for
	 *                       sending/receiving data from the remote.
	 */
	public static final IConnection createCustomConnection(IConnectionConfig config, IConnectionImpl implementation) {
		return new CustomConnection(config, implementation);
	}

	/**
	 * Creates a configuration that holds parameters for a client.
	 * 
	 * @param name    The client's name. Essentially used for logging.
	 * @param address The address of the remote.
	 * @param port    The port number of the remote.
	 */
	public static final ClientConfig createClientConfig(String name, String address, int port) {
		return new ClientConfig(name, address, port);
	}

	/**
	 * Create a client ready to be connected to a remote.
	 * 
	 * @param config         The object that holds the client configuration.
	 * @param implementation The client specific implementation to
	 *                       connect/disconnect from the server.
	 */
	public static final IClient createCustomClient(IClientConfig config, IClientImpl implementation) {
		return new CustomClient(config, implementation);
	}

	/**
	 * Create a client with default configuration ready to be connected to a remote.
	 * 
	 * @param name    The client's name. Essentially used for logging.
	 * @param address The IP address of the server.
	 * @param port    The port number of the server.
	 * @param impl    The client specific implementation to connect/disconnect from
	 *                the server.
	 */
	public static final IClient createDefaultCustomClient(String name, String address, int port, IClientImpl impl) {
		return new CustomClient(createClientConfig(name, address, port), impl);
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
	 * @param name    The client's name. Essentially used for logging.
	 * @param address The IP address of the server.
	 * @param port    The port number of the server.
	 */
	public static final IClient createDefaultTcpClient(String name, String address, int port) {
		return createTcpClient(createClientConfig(name, address, port));
	}

	/**
	 * Creates a configuration that holds the parameters for a server.
	 * 
	 * @param name The server's name.
	 * @param port The server port number.
	 */
	public static final ServerConfig createServerConfig(String name, int port) {
		return new ServerConfig(name, port);
	}

	/**
	 * Creates a custom server.
	 * 
	 * @param config         The object that holds the server configuration.
	 * @param implementation The server specific implementation to open/close the
	 *                       server.
	 */
	public static final IServer createCustomServer(IServerConfig config, IServerImpl implementation) {
		return new CustomServer(config, implementation);
	}

	/**
	 * Creates a custom server with default configuration ready to be opened.
	 * 
	 * @param name           The name of the server.
	 * @param port           The port number of the server.
	 * @param implementation The server specific implementation to open/close the
	 *                       server.
	 */
	public static final IServer createDefaultCustomServer(String name, int port, IServerImpl implementation) {
		return new CustomServer(createServerConfig(name, port), implementation);
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
		return createTcpServer(createServerConfig(name, port));
	}
}
