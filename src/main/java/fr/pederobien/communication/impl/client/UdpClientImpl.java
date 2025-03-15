package fr.pederobien.communication.impl.client;

import java.net.InetSocketAddress;

import fr.pederobien.communication.impl.Communication;
import fr.pederobien.communication.impl.connection.UdpConnectionImpl;
import fr.pederobien.communication.interfaces.client.IClientConfig;
import fr.pederobien.communication.interfaces.client.IClientImpl;
import fr.pederobien.communication.interfaces.connection.IConnection;
import fr.pederobien.communication.interfaces.connection.IConnectionConfig;
import fr.pederobien.communication.interfaces.connection.IUdpSocket;

public class UdpClientImpl implements IClientImpl {
	IUdpSocket socket;

	/**
	 * Create a client for the UDP protocol.
	 */
	public UdpClientImpl() {
		// Do nothing
	}

	@Override
	public void connectImpl(String address, int port, int connectionTimeout) throws Exception {
		socket = new UdpSocket(new InetSocketAddress(address, port));
	}

	@Override
	public IConnection onConnectionComplete(IClientConfig config) {
		String address = socket.getInetAddress().getHostName();
		int port = socket.getInetAddress().getPort();

		// Creating a connection configuration builder.
		IConnectionConfig configuration = Communication.createConnectionConfig(address, port, config);

		return Communication.createCustomConnection(configuration, new UdpConnectionImpl(socket));
	}
}
