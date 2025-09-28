package fr.pederobien.communication.impl.client;

import fr.pederobien.communication.impl.connection.UdpConnectionImpl;
import fr.pederobien.communication.interfaces.IEthernetEndPoint;
import fr.pederobien.communication.interfaces.client.IClientImpl;
import fr.pederobien.communication.interfaces.connection.IConnectionImpl;

import java.net.InetSocketAddress;

public class UdpClientImpl implements IClientImpl<IEthernetEndPoint> {

	@Override
	public IConnectionImpl connect(String name, IEthernetEndPoint endPoint, int timeout) throws Exception {
		String address = endPoint.getAddress();
		int port = endPoint.getPort();

		return new UdpConnectionImpl(new UdpSocket(new InetSocketAddress(address, port)));
	}
}
