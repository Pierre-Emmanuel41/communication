package fr.pederobien.communication.impl.server;

import java.net.ServerSocket;
import java.net.Socket;

import fr.pederobien.communication.impl.Communication;
import fr.pederobien.communication.impl.EthernetEndPoint;
import fr.pederobien.communication.impl.connection.TcpConnectionImpl;
import fr.pederobien.communication.interfaces.IEthernetEndPoint;
import fr.pederobien.communication.interfaces.connection.IConnection;
import fr.pederobien.communication.interfaces.server.IServerConfig;
import fr.pederobien.communication.interfaces.server.IServerImpl;

public class TcpServerImpl implements IServerImpl<IEthernetEndPoint> {
	private ServerSocket serverSocket;

	/**
	 * Creates a TCP implementation for a server.
	 */
	public TcpServerImpl() {
		// Do nothing
	}

	@Override
	public void open(IServerConfig<IEthernetEndPoint> config) throws Exception {
		serverSocket = new ServerSocket(config.getPoint().getPort());
	}

	@Override
	public void close() throws Exception {
		serverSocket.close();
	}

	@Override
	public IConnection waitForClient(IServerConfig<IEthernetEndPoint> config) throws Exception {
		// Waiting for a new client
		Socket socket = serverSocket.accept();

		String address = socket.getInetAddress().getHostName();
		int port = socket.getPort();

		// Creating remote end point
		EthernetEndPoint endPoint = new EthernetEndPoint(address, port);

		return Communication.createConnection(config, endPoint, new TcpConnectionImpl(socket));
	}

}
