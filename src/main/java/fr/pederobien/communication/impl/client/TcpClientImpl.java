package fr.pederobien.communication.impl.client;

import fr.pederobien.communication.impl.connection.TcpConnectionImpl;
import fr.pederobien.communication.interfaces.IEthernetEndPoint;
import fr.pederobien.communication.interfaces.client.IClientImpl;
import fr.pederobien.communication.interfaces.connection.IConnectionImpl;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TcpClientImpl implements IClientImpl<IEthernetEndPoint> {

	@Override
	public IConnectionImpl connect(String name, IEthernetEndPoint endPoint, int timeout) throws Exception {
		String address = endPoint.getAddress();
		int port = endPoint.getPort();

		// Creating a TCP socket to connect with the remote
		Socket socket = new Socket();
		socket.connect(new InetSocketAddress(InetAddress.getByName(address), port), timeout);

		return new TcpConnectionImpl(socket);
	}
}
