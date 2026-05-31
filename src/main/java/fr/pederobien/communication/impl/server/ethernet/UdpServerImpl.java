package fr.pederobien.communication.impl.server.ethernet;

import fr.pederobien.communication.impl.EthernetEndPoint;
import fr.pederobien.communication.impl.connection.UdpConnectionImpl;
import fr.pederobien.communication.impl.server.ClientInfo;
import fr.pederobien.communication.interfaces.IEthernetEndPoint;
import fr.pederobien.communication.interfaces.connection.IUdpSocket;
import fr.pederobien.communication.interfaces.server.IClientInfo;
import fr.pederobien.communication.interfaces.server.IServerConfig;
import fr.pederobien.communication.interfaces.server.IServerEthernetEndPoint;

public class UdpServerImpl extends EthernetServerImpl {
	private UdpServerSocket serverSocket;

	@Override
	public void open(IServerConfig<IServerEthernetEndPoint, IEthernetEndPoint> config) throws Exception {
		serverSocket = new UdpServerSocket(config.getName(), config.getPoint());

		// In case the port number from config is 0, the port number is defined by the host machine
		config.getPoint().setPort(serverSocket.getLocalPort());
	}

	@Override
	public void close() throws Exception {
		serverSocket.close();
	}

	@Override
	public IClientInfo<IEthernetEndPoint> waitForClient() throws Exception {
		IUdpSocket socket = serverSocket.accept();

		String address = socket.getInetAddress().getHostName();
		int port = socket.getInetAddress().getPort();

		// Creating remote end point
		EthernetEndPoint endPoint = new EthernetEndPoint(address, port);

		return new ClientInfo<IEthernetEndPoint>(endPoint, new UdpConnectionImpl(socket));
	}
}
