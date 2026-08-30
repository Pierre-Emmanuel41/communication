package fr.pederobien.communication.impl.server.ethernet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

import fr.pederobien.communication.impl.EthernetEndPoint;
import fr.pederobien.communication.impl.connection.TcpConnectionImpl;
import fr.pederobien.communication.impl.server.ClientInfo;
import fr.pederobien.communication.interfaces.IEthernetEndPoint;
import fr.pederobien.communication.interfaces.server.IClientInfo;
import fr.pederobien.communication.interfaces.server.IEthernetServerImpl;
import fr.pederobien.communication.interfaces.server.IServerConfig;
import fr.pederobien.communication.interfaces.server.IServerEthernetEndPoint;
import fr.pederobien.utils.event.Logger;

public class TcpServerImpl extends EthernetServerImpl implements IEthernetServerImpl {
	private ServerSocket serverSocket;

	/**
	 * Creates a TCP implementation for a server.
	 */
	public TcpServerImpl() {
		// Do nothing
	}

	@Override
	public void open(IServerConfig<IServerEthernetEndPoint, IEthernetEndPoint> config) throws Exception {
		serverSocket = createServerSocket(config.getPoint());

		if (serverSocket == null)
			throw new Exception("Cannot create a TCP server, please check if the IP address and port number are already in use");

		// In case the port number from config is 0, the port number is defined by the host machine
		config.getPoint().setPort(serverSocket.getLocalPort());
	}

	@Override
	public void close() throws Exception {
		serverSocket.close();
	}

	@Override
	public IClientInfo<IEthernetEndPoint> waitForClient() throws Exception {
		// Waiting for a new client
		Socket socket = serverSocket.accept();

		String address = socket.getInetAddress().getHostName();
		int port = socket.getPort();

		// Creating remote end point
		EthernetEndPoint endPoint = new EthernetEndPoint(address, port);

		return new ClientInfo<IEthernetEndPoint>(endPoint, new TcpConnectionImpl(socket));
	}

	/**
	 * Create a server socket associated to the properties of the given end-point.
	 * 
	 * @param endPoint The end-point that contains the port number to use or the range to use to create a server socket.
	 * @return The server socket if it was possible
	 * @throws IOException
	 */
	private ServerSocket createServerSocket(IServerEthernetEndPoint endPoint) throws IOException {
		ServerSocket socket = new ServerSocket();

		// A port number is specified
		if (0 <= endPoint.getPort()) {
			// Note: The port number does not matter, if the value is out of range, the socket will throw an exception
			// if the value is 0, the host machine will choose an ephemeral (ie first free) port.

			if (tryBindToAddress(socket, createAddress(endPoint.getAddress(), endPoint.getPort())))
				return socket;

			return null;
		}

		// If a port range is defined
		if (endPoint.getMin() > 0 && endPoint.getMax() > 0) {
			for (int port = endPoint.getMin(); port <= endPoint.getMax(); port++) {
				if (tryBindToAddress(socket, createAddress(endPoint.getAddress(), port)))
					return socket;
			}

			Logger.error("Could not bind to any port in range [%s-%s]", endPoint.getMin(), endPoint.getMax());
		}

		return null;
	}

	/**
	 * Creates a socket address from the given IP address and port number.
	 * 
	 * @param address The IP address to use.
	 * @param port    The port number to use.
	 * 
	 * @return The created SocketAddress.
	 */
	private SocketAddress createAddress(String address, int port) {
		if (address.equals("*"))
			return new InetSocketAddress(port);
		else
			return new InetSocketAddress(address, port);
	}

	/**
	 * Check if the server socket can be bound to the given address.
	 * 
	 * @param socket  The server socket to bind.
	 * @param address The address to use for binding.
	 * @return True if the socket is bound to the address, false otherwise.
	 */
	private boolean tryBindToAddress(ServerSocket socket, SocketAddress address) {
		try {
			socket.bind(address);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
