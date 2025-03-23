package fr.pederobien.communication.impl;

import fr.pederobien.communication.impl.client.Client;
import fr.pederobien.communication.impl.client.TcpClientImpl;
import fr.pederobien.communication.impl.client.UdpClientImpl;
import fr.pederobien.communication.impl.connection.Connection;
import fr.pederobien.communication.impl.server.Server;
import fr.pederobien.communication.impl.server.TcpServerImpl;
import fr.pederobien.communication.impl.server.UdpServerImpl;
import fr.pederobien.communication.interfaces.IConfiguration;
import fr.pederobien.communication.interfaces.IEthernetEndPoint;
import fr.pederobien.communication.interfaces.client.IClient;
import fr.pederobien.communication.interfaces.client.IClientConfig;
import fr.pederobien.communication.interfaces.client.IClientImpl;
import fr.pederobien.communication.interfaces.connection.IConnection;
import fr.pederobien.communication.interfaces.connection.IConnectionImpl;
import fr.pederobien.communication.interfaces.server.IServer;
import fr.pederobien.communication.interfaces.server.IServerConfig;
import fr.pederobien.communication.interfaces.server.IServerImpl;

public class Communication {

	/**
	 * Create custom connection that send asynchronously messages to the remote.
	 * 
	 * @param config   The object that holds the client configuration.
	 * @param endPoint The object that gather remote information.
	 * @param impl     The connection specific implementation for sending/receiving
	 *                 data from the remote.
	 */
	public static final <T> IConnection createConnection(IConfiguration config, T endPoint, IConnectionImpl impl) {
		return new Connection<T>(config, endPoint, impl);
	}

	/**
	 * Creates a configuration that holds parameters for a client.
	 * 
	 * @param name     The client's name. Essentially used for logging.
	 * @param endPoint The object that gather remote information.
	 */
	public static final <T> ClientConfig<T> createClientConfig(String name, T endPoint) {
		return new ClientConfig<T>(name, endPoint);
	}

	/**
	 * Create a client ready to be connected to a remote.
	 * 
	 * @param config The object that holds the client configuration.
	 * @param impl   The client specific implementation to connect/disconnect from
	 *               the server.
	 */
	public static final <T> IClient<T> createClient(IClientConfig<T> config, IClientImpl<T> impl) {
		return new Client<T>(config, impl);
	}

	/**
	 * Create a client with default configuration ready to be connected to a remote.
	 * 
	 * @param name     The client's name. Essentially used for logging.
	 * @param endPoint The object that gather remote information.
	 * @param impl     The client specific implementation to connect/disconnect from
	 *                 the server.
	 */
	public static final <T> IClient<T> createDefaultClient(String name, T endPoint, IClientImpl<T> impl) {
		return new Client<T>(createClientConfig(name, endPoint), impl);
	}

	/**
	 * Create a client with a TCP connection ready to be connected to a remote.
	 * 
	 * @param config The object that holds the client configuration.
	 */
	public static final IClient<IEthernetEndPoint> createTcpClient(IClientConfig<IEthernetEndPoint> config) {
		return createClient(config, new TcpClientImpl());
	}

	/**
	 * Create a client with a UDP connection ready to be connected to a remote.
	 * 
	 * @param config The object that holds the client configuration.
	 */
	public static final IClient<IEthernetEndPoint> createUdpClient(IClientConfig<IEthernetEndPoint> config) {
		return createClient(config, new UdpClientImpl());
	}

	/**
	 * Creates a client with a TCP connection ready to be connected to a remote.
	 * 
	 * @param name    The client's name. Essentially used for logging.
	 * @param address The IP address of the server.
	 * @param port    The port number of the server.
	 */
	public static final IClient<IEthernetEndPoint> createDefaultTcpClient(String name, String address, int port) {
		return createTcpClient(createClientConfig(name, new EthernetEndPoint(address, port)));
	}

	/**
	 * Creates a client with a UDP connection ready to be connected to a remote.
	 * 
	 * @param name    The client's name. Essentially used for logging.
	 * @param address The IP address of the server.
	 * @param port    The port number of the server.
	 */
	public static final IClient<IEthernetEndPoint> createDefaultUdpClient(String name, String address, int port) {
		return createUdpClient(createClientConfig(name, new EthernetEndPoint(address, port)));
	}

	/**
	 * Creates a configuration that holds the parameters for a server.
	 * 
	 * @param name  The server's name.
	 * @param point The properties of the server communication point.
	 */
	public static final <T> ServerConfig<T> createServerConfig(String name, T point) {
		return new ServerConfig<T>(name, point);
	}

	/**
	 * Creates a custom server.
	 * 
	 * @param config The object that holds the server configuration.
	 * @param impl   The server specific implementation to open/close the server.
	 */
	public static final <T> IServer createServer(IServerConfig<T> config, IServerImpl<T> impl) {
		return new Server<T>(config, impl);
	}

	/**
	 * Creates a custom server with default configuration ready to be opened.
	 * 
	 * @param name  The name of the server.
	 * @param point The properties of the server communication point.
	 * @param impl  The server specific implementation to open/close the server.
	 */
	public static final <T> IServer createDefaultServer(String name, T point, IServerImpl<T> impl) {
		return new Server<T>(createServerConfig(name, point), impl);
	}

	/**
	 * Creates a TCP server ready to be opened.
	 * 
	 * @param config The object that holds the server configuration.
	 */
	public static final IServer createTcpServer(IServerConfig<IEthernetEndPoint> config) {
		return createServer(config, new TcpServerImpl());
	}

	/**
	 * Creates a UDP server ready to be opened.
	 * 
	 * @param config The object that holds the server configuration.
	 */
	public static final IServer createUdpServer(IServerConfig<IEthernetEndPoint> config) {
		return createServer(config, new UdpServerImpl());
	}

	/**
	 * Creates a TCP server ready to be opened.
	 * 
	 * @param name The name of the server.
	 * @param port The port number of the server.
	 */
	public static final IServer createDefaultTcpServer(String name, int port) {
		return createTcpServer(createServerConfig(name, new EthernetEndPoint(port)));
	}

	/**
	 * Creates a UDP server ready to be opened.
	 * 
	 * @param name The name of the server.
	 * @param port The port number of the server.
	 */
	public static final IServer createDefaultUdpServer(String name, int port) {
		return createUdpServer(createServerConfig(name, new EthernetEndPoint(port)));
	}
}
