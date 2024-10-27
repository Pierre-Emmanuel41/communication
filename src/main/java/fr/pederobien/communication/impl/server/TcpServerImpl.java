package fr.pederobien.communication.impl.server;

import java.net.ServerSocket;
import java.net.Socket;

import fr.pederobien.communication.impl.Communication;
import fr.pederobien.communication.impl.connection.TcpConnectionImpl;
import fr.pederobien.communication.interfaces.connection.IConnection;
import fr.pederobien.communication.interfaces.connection.IConnectionConfig;
import fr.pederobien.communication.interfaces.server.IServerConfig;
import fr.pederobien.communication.interfaces.server.IServerImpl;

public class TcpServerImpl implements IServerImpl {
	private ServerSocket serverSocket;
	
	/**
	 * Creates a TCP implementation for a server.
	 */
	public TcpServerImpl() {
		// Do nothing
	}

	@Override
	public void openImpl(int port) throws Exception {
		serverSocket = new ServerSocket(port);
	}

	@Override
	public void closeImpl() throws Exception {
		serverSocket.close();
	}

	@Override
	public IConnection waitForClientImpl(IServerConfig config) throws Exception {
		// Waiting for a new client
		Socket socket = serverSocket.accept();
		
		String address = socket.getInetAddress().getHostName();
		int port = socket.getPort();

		// Creating a connection configuration.
		IConnectionConfig configuration = Communication.createConnectionConfig(address, port, config);
		
		return Communication.createCustomConnection(configuration, new TcpConnectionImpl(socket));
	}

}
