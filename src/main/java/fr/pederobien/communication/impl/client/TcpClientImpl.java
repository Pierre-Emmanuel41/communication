package fr.pederobien.communication.impl.client;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import fr.pederobien.communication.impl.Communication;
import fr.pederobien.communication.impl.connection.TcpConnectionImpl;
import fr.pederobien.communication.interfaces.IEthernetEndPoint;
import fr.pederobien.communication.interfaces.client.IClientConfig;
import fr.pederobien.communication.interfaces.client.IClientImpl;
import fr.pederobien.communication.interfaces.connection.IConnection;

public class TcpClientImpl implements IClientImpl<IEthernetEndPoint> {

	@Override
	public IConnection connectImpl(IClientConfig<IEthernetEndPoint> config) throws Exception {
		String address = config.getEndPoint().getAddress();
		int port = config.getEndPoint().getPort();
		int connectionTimeout = config.getConnectionTimeout();

		// Creating a TCP socket to connect with the remote
		Socket socket = new Socket();
		socket.connect(new InetSocketAddress(InetAddress.getByName(address), port), connectionTimeout);

		return Communication.createConnection(config, config.getEndPoint(), new TcpConnectionImpl(socket));
	}
}
