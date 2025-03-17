package fr.pederobien.communication.impl.client;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import fr.pederobien.communication.impl.Communication;
import fr.pederobien.communication.impl.connection.TcpConnectionImpl;
import fr.pederobien.communication.interfaces.client.IClientConfig;
import fr.pederobien.communication.interfaces.client.IClientImpl;
import fr.pederobien.communication.interfaces.connection.IConnection;
import fr.pederobien.communication.interfaces.connection.IConnectionConfig;

public class TcpClientImpl implements IClientImpl {

	@Override
	public IConnection connectImpl(IClientConfig config) throws Exception {
		String address = config.getAddress();
		int port = config.getPort();
		int connectionTimeout = config.getConnectionTimeout();

		// Creating a TCP socket to connect with the remote
		Socket socket = new Socket();
		socket.connect(new InetSocketAddress(InetAddress.getByName(address), port), connectionTimeout);

		// Creating a connection configuration.
		IConnectionConfig configuration = Communication.createConnectionConfig(address, port, config);

		return Communication.createCustomConnection(configuration, new TcpConnectionImpl(socket));
	}
}
