package fr.pederobien.communication.impl.server;

import fr.pederobien.communication.impl.Communication;
import fr.pederobien.communication.impl.connection.UdpConnectionImpl;
import fr.pederobien.communication.interfaces.connection.IConnection;
import fr.pederobien.communication.interfaces.connection.IConnectionConfig;
import fr.pederobien.communication.interfaces.connection.IUdpSocket;
import fr.pederobien.communication.interfaces.server.IServerConfig;
import fr.pederobien.communication.interfaces.server.IServerImpl;

public class UdpServerImpl implements IServerImpl {
	private String name;
	private UdpServerSocket server;
	
	/**
	 * Create a UDP implementation for a server.
	 */
	public UdpServerImpl(String name) {
		this.name = name;
	}

	@Override
	public void openImpl(int port) throws Exception {
		this.server = new UdpServerSocket(name, port);
		server.start();
	}

	@Override
	public void closeImpl() throws Exception {
		// Do nothing
	}

	@Override
	public IConnection waitForClientImpl(IServerConfig config) throws Exception {
		IUdpSocket socket = server.accept();
		
		String address = socket.getInetAddress().getHostName();
		int port = socket.getInetAddress().getPort();
		
		// Creating a connection configuration builder.
		IConnectionConfig configuration = Communication.createConnectionConfig(address, port, config);

		return Communication.createCustomConnection(configuration, new UdpConnectionImpl(socket));
	}
}
