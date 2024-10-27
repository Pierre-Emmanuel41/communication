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
	private Socket socket;
	
	/**
	 * Create a client for the TCP protocol
	 */
	public TcpClientImpl() {
		// Do nothing
	}

	@Override
	public void connectImpl(String address, int port, int connectionTimeout) throws Exception {
		Socket socket = new Socket();
		socket.connect(new InetSocketAddress(InetAddress.getByName(address), port), connectionTimeout);
		this.socket = socket;
	}

	@Override
	public IConnection onConnectionComplete(IClientConfig config) {
		String address = socket.getInetAddress().getHostName();
		int port = socket.getPort();
		
		// Creating a connection configuration.
		IConnectionConfig configuration = Communication.createConnectionConfig(address, port, config);
		
		return Communication.createCustomConnection(configuration, new TcpConnectionImpl(socket));
	}
}
