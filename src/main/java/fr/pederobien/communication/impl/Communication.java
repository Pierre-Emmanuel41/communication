package fr.pederobien.communication.impl;

import fr.pederobien.communication.impl.client.Client;
import fr.pederobien.communication.impl.client.ClientConfig;
import fr.pederobien.communication.impl.client.ethernet.EthernetClientConfig;
import fr.pederobien.communication.impl.client.ethernet.TcpClientImpl;
import fr.pederobien.communication.impl.client.ethernet.UdpClientImpl;
import fr.pederobien.communication.impl.connection.Connection;
import fr.pederobien.communication.impl.server.EthernetServer;
import fr.pederobien.communication.impl.server.Server;
import fr.pederobien.communication.impl.server.ServerConfig;
import fr.pederobien.communication.impl.server.ethernet.EthernetServerConfig;
import fr.pederobien.communication.impl.server.ethernet.ServerEthernetEndPoint;
import fr.pederobien.communication.impl.server.ethernet.TcpServerImpl;
import fr.pederobien.communication.impl.server.ethernet.UdpServerImpl;
import fr.pederobien.communication.interfaces.IConfiguration;
import fr.pederobien.communication.interfaces.IEthernetEndPoint;
import fr.pederobien.communication.interfaces.client.IClient;
import fr.pederobien.communication.interfaces.client.IClientConfig;
import fr.pederobien.communication.interfaces.client.IClientImpl;
import fr.pederobien.communication.interfaces.client.IEthernetClientConfig;
import fr.pederobien.communication.interfaces.client.IEthernetClientImpl;
import fr.pederobien.communication.interfaces.connection.IConnection;
import fr.pederobien.communication.interfaces.connection.IConnectionImpl;
import fr.pederobien.communication.interfaces.server.IEthernetServer;
import fr.pederobien.communication.interfaces.server.IEthernetServerConfig;
import fr.pederobien.communication.interfaces.server.IEthernetServerImpl;
import fr.pederobien.communication.interfaces.server.IServer;
import fr.pederobien.communication.interfaces.server.IServerConfig;
import fr.pederobien.communication.interfaces.server.IServerEthernetEndPoint;
import fr.pederobien.communication.interfaces.server.IServerImpl;

public class Communication {

	/**
	 * Create custom connection that send asynchronously messages to the remote.
	 *
	 * @param config   The object that holds the client configuration.
	 * @param endPoint The object that gather remote information.
	 * @param impl     The connection specific implementation for sending/receiving data from the remote.
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
	 * @param impl   The client specific implementation to connect/disconnect from the server.
	 */
	public static final <T> IClient createClient(IClientConfig<T> config, IClientImpl<T> impl) {
		return new Client<T>(config, impl);
	}

	/**
	 * Create a client with default configuration ready to be connected to a remote.
	 *
	 * @param name     The client's name. Essentially used for logging.
	 * @param endPoint The object that gather remote information.
	 * @param impl     The client specific implementation to connect/disconnect from the server.
	 */
	public static final <T> IClient createDefaultClient(String name, T endPoint, IClientImpl<T> impl) {
		return createClient(createClientConfig(name, endPoint), impl);
	}

	/**
	 * Creates an Ethernet configuration that holds parameters for a client.
	 *
	 * @param name     The client's name. Essentially used for logging.
	 * @param endPoint The object that gather remote information.
	 */
	public static final EthernetClientConfig createEthernetClientConfig(String name, IEthernetEndPoint endPoint) {
		return new EthernetClientConfig(name, endPoint);
	}

	/**
	 * Create an Ethernet client ready to be connected to a remote.
	 *
	 * @param config The object that holds the client configuration.
	 * @param impl   The client specific implementation to connect/disconnect from the server.
	 */
	public static final IClient createEthernetClient(IEthernetClientConfig config, IEthernetClientImpl impl) {
		return createClient(config, impl);
	}

	/**
	 * Create an Ethernet client with default configuration ready to be connected to a remote.
	 *
	 * @param name     The client's name. Essentially used for logging.
	 * @param endPoint The object that gather remote information.
	 * @param impl     The client specific implementation to connect/disconnect from the server.
	 */
	public static final IClient createDefaultEthernetClient(String name, IEthernetEndPoint endPoint, IEthernetClientImpl impl) {
		return createClient(createEthernetClientConfig(name, endPoint), impl);
	}

	/**
	 * Create a client with a TCP connection ready to be connected to a remote.
	 *
	 * @param config The object that holds the client configuration.
	 */
	public static final IClient createTcpClient(IClientConfig<IEthernetEndPoint> config) {
		return createClient(config, new TcpClientImpl());
	}

	/**
	 * Create a client with a TCP connection ready to be connected to a remote.
	 *
	 * @param config The object that holds the client configuration.
	 */
	public static final IClient createTcpClient(IEthernetClientConfig config) {
		return createClient(config, new TcpClientImpl());
	}

	/**
	 * Create a client with a UDP connection ready to be connected to a remote.
	 *
	 * @param config The object that holds the client configuration.
	 */
	public static final IClient createUdpClient(IClientConfig<IEthernetEndPoint> config) {
		return createClient(config, new UdpClientImpl());
	}

	/**
	 * Create a client with a UDP connection ready to be connected to a remote.
	 *
	 * @param config The object that holds the client configuration.
	 */
	public static final IClient createUdpClient(IEthernetClientConfig config) {
		return createClient(config, new UdpClientImpl());
	}

	/**
	 * Creates a client with a TCP connection ready to be connected to a remote.
	 *
	 * @param name    The client's name. Essentially used for logging.
	 * @param address The IP address of the server.
	 * @param port    The port number of the server.
	 */
	public static final IClient createDefaultTcpClient(String name, String address, int port) {
		return createTcpClient(createEthernetClientConfig(name, new EthernetEndPoint(address, port)));
	}

	/**
	 * Creates a client with a UDP connection ready to be connected to a remote.
	 *
	 * @param name    The client's name. Essentially used for logging.
	 * @param address The IP address of the server.
	 * @param port    The port number of the server.
	 */
	public static final IClient createDefaultUdpClient(String name, String address, int port) {
		return createUdpClient(createEthernetClientConfig(name, new EthernetEndPoint(address, port)));
	}

	/**
	 * Creates a configuration that holds the parameters for a server.
	 *
	 * @param name  The server's name.
	 * @param point The properties of the server communication point.
	 */
	public static final <T, U> ServerConfig<T, U> createServerConfig(String name, T point) {
		return new ServerConfig<T, U>(name, point);
	}

	/**
	 * Creates a custom server.
	 *
	 * @param config The object that holds the server configuration.
	 * @param impl   The server specific implementation to open/close the server.
	 */
	public static final <T, U> IServer createServer(IServerConfig<T, U> config, IServerImpl<T, U> impl) {
		return new Server<T, U>(config, impl);
	}

	/**
	 * Creates a custom server with default configuration ready to be opened.
	 *
	 * @param name  The name of the server.
	 * @param point The properties of the server communication point.
	 * @param impl  The server specific implementation to open/close the server.
	 */
	public static final <T, U> IServer createDefaultServer(String name, T point, IServerImpl<T, U> impl) {
		return createServer(createServerConfig(name, point), impl);
	}

	/**
	 * Creates an Ethernet configuration that holds the parameters for a server.
	 *
	 * @param name  The server's name.
	 * @param point The properties of the server communication point.
	 */
	public static final EthernetServerConfig createEthernetServerConfig(String name, IServerEthernetEndPoint point) {
		return new EthernetServerConfig(name, point);
	}

	/**
	 * Creates an ethernet server.
	 *
	 * @param config The object that holds the server configuration.
	 * @param impl   The server specific implementation to open/close the server.
	 */
	public static final IEthernetServer createEthernetServer(IServerConfig<IServerEthernetEndPoint, IEthernetEndPoint> config,
			IServerImpl<IServerEthernetEndPoint, IEthernetEndPoint> impl) {
		return new EthernetServer(config, impl);
	}

	/**
	 * Creates an ethernet server.
	 *
	 * @param config The object that holds the server configuration.
	 * @param impl   The server specific implementation to open/close the server.
	 */
	public static final IEthernetServer createEthernetServer(IEthernetServerConfig config, IEthernetServerImpl impl) {
		return new EthernetServer(config, impl);
	}

	/**
	 * Creates an ethernet server with default configuration ready to be opened.
	 *
	 * @param name  The name of the server.
	 * @param point The properties of the server communication point.
	 * @param impl  The server specific implementation to open/close the server.
	 */
	public static final IEthernetServer createDefaultEthernetServer(String name, IServerEthernetEndPoint point, IEthernetServerImpl impl) {
		return createEthernetServer(createEthernetServerConfig(name, point), impl);
	}

	/**
	 * Creates a TCP server ready to be opened.
	 *
	 * @param config The object that holds the server configuration.
	 */
	public static final IEthernetServer createTcpServer(IServerConfig<IServerEthernetEndPoint, IEthernetEndPoint> config) {
		return createEthernetServer(config, new TcpServerImpl());
	}

	/**
	 * Creates a TCP server ready to be opened.
	 *
	 * @param config The object that holds the server configuration.
	 */
	public static final IEthernetServer createTcpServer(IEthernetServerConfig config) {
		return createEthernetServer(config, new TcpServerImpl());
	}

	/**
	 * Creates a UDP server ready to be opened.
	 *
	 * @param config The object that holds the server configuration.
	 */
	public static final IEthernetServer createUdpServer(IServerConfig<IServerEthernetEndPoint, IEthernetEndPoint> config) {
		return createEthernetServer(config, new UdpServerImpl());
	}

	/**
	 * Creates a UDP server ready to be opened.
	 *
	 * @param config The object that holds the server configuration.
	 */
	public static final IEthernetServer createUdpServer(IEthernetServerConfig config) {
		return createEthernetServer(config, new UdpServerImpl());
	}

	/**
	 * Creates a TCP server ready to be opened.
	 *
	 * @param name    The name of the server.
	 * @param address the IP address of the server.
	 * @param port    The port number of the server.
	 */
	public static final IEthernetServer createDefaultTcpServer(String name, String address, int port) {
		return createTcpServer(createEthernetServerConfig(name, new ServerEthernetEndPoint(address, port)));
	}

	/**
	 * Creates a TCP server ready to be opened.
	 *
	 * @param name The name of the server.
	 * @param port The port number of the server.
	 */
	public static final IEthernetServer createDefaultTcpServer(String name, int port) {
		return createTcpServer(createEthernetServerConfig(name, new ServerEthernetEndPoint(port)));
	}

	/**
	 * Creates a TCP server ready to be opened.
	 *
	 * @param name    The name of the server.
	 * @param address the IP address of the server.
	 * @param min     The minimum value of the port number of the server.
	 * @param max     The maximum value of the port number of the server.
	 */
	public static final IEthernetServer createDefaultTcpServer(String name, String address, int min, int max) {
		return createTcpServer(createEthernetServerConfig(name, new ServerEthernetEndPoint(address, min, max)));
	}

	/**
	 * Creates a TCP server ready to be opened.
	 *
	 * @param name The name of the server.
	 * @param min  The minimum value of the port number of the server.
	 * @param max  The maximum value of the port number of the server.
	 */
	public static final IEthernetServer createDefaultTcpServer(String name, int min, int max) {
		return createTcpServer(createEthernetServerConfig(name, new ServerEthernetEndPoint(min, max)));
	}

	/**
	 * Creates a UDP server ready to be opened.
	 *
	 * @param name    The name of the server.
	 * @param address the IP address of the server.
	 * @param port    The port number of the server.
	 */
	public static final IEthernetServer createDefaultUdpServer(String name, String address, int port) {
		return createUdpServer(createEthernetServerConfig(name, new ServerEthernetEndPoint(address, port)));
	}

	/**
	 * Creates a UDP server ready to be opened.
	 *
	 * @param name The name of the server.
	 * @param port The port number of the server.
	 */
	public static final IEthernetServer createDefaultUdpServer(String name, int port) {
		return createUdpServer(createEthernetServerConfig(name, new ServerEthernetEndPoint(port)));
	}

	/**
	 * Creates a UDP server ready to be opened.
	 *
	 * @param name    The name of the server.
	 * @param address the IP address of the server.
	 * @param min     The minimum value of the port number of the server.
	 * @param max     The maximum value of the port number of the server.
	 */
	public static final IEthernetServer createDefaultUdpServer(String name, String address, int min, int max) {
		return createUdpServer(createEthernetServerConfig(name, new ServerEthernetEndPoint(address, min, max)));
	}

	/**
	 * Creates a UDP server ready to be opened.
	 *
	 * @param name The name of the server.
	 * @param min  The minimum value of the port number of the server.
	 * @param max  The maximum value of the port number of the server.
	 */
	public static final IEthernetServer createDefaultUdpServer(String name, int min, int max) {
		return createUdpServer(createEthernetServerConfig(name, new ServerEthernetEndPoint(min, max)));
	}
}
