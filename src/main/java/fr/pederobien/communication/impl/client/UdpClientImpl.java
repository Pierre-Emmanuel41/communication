package fr.pederobien.communication.impl.client;

import java.net.InetSocketAddress;

import fr.pederobien.communication.impl.Communication;
import fr.pederobien.communication.impl.connection.UdpConnectionImpl;
import fr.pederobien.communication.interfaces.IEthernetEndPoint;
import fr.pederobien.communication.interfaces.client.IClientConfig;
import fr.pederobien.communication.interfaces.client.IClientImpl;
import fr.pederobien.communication.interfaces.connection.IConnection;
import fr.pederobien.communication.interfaces.connection.IUdpSocket;

public class UdpClientImpl implements IClientImpl<IEthernetEndPoint> {

	@Override
	public IConnection connectImpl(IClientConfig<IEthernetEndPoint> config) throws Exception {
		String address = config.getEndPoint().getAddress();
		int port = config.getEndPoint().getPort();

		// Creating a TCP socket to connect with the remote
		IUdpSocket socket = new UdpSocket(new InetSocketAddress(address, port));

		return Communication.createConnection(config, config.getEndPoint(), new UdpConnectionImpl(socket));
	}
}
