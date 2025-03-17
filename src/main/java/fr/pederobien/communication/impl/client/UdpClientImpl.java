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

	@Override
	public IConnection connectImpl(IClientConfig config) throws Exception {
		String address = config.getAddress();
		int port = config.getPort();

		// Creating a TCP socket to connect with the remote
		IUdpSocket socket = new UdpSocket(new InetSocketAddress(address, port));

		// Creating a connection configuration builder.
		IConnectionConfig configuration = Communication.createConnectionConfig(address, port, config);

		return Communication.createCustomConnection(configuration, new UdpConnectionImpl(socket));
	}
}
