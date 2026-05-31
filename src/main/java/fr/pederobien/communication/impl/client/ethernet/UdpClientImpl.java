package fr.pederobien.communication.impl.client.ethernet;

import java.net.InetSocketAddress;

import fr.pederobien.communication.impl.connection.UdpConnectionImpl;
import fr.pederobien.communication.interfaces.IEthernetEndPoint;
import fr.pederobien.communication.interfaces.connection.IConnectionImpl;

public class UdpClientImpl extends EthernetClientImpl {

	@Override
	public IConnectionImpl connect(String name, IEthernetEndPoint endPoint, int timeout) throws Exception {
		String address = endPoint.getAddress();
		int port = endPoint.getPort();

		return new UdpConnectionImpl(new UdpSocket(new InetSocketAddress(address, port)));
	}
}
