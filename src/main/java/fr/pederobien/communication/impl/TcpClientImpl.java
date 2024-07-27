package fr.pederobien.communication.impl;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import fr.pederobien.communication.interfaces.IClientConfig;
import fr.pederobien.communication.interfaces.IClientImpl;
import fr.pederobien.communication.interfaces.IConnection;
import fr.pederobien.communication.interfaces.IConnection.Mode;

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
		
		// Creating a connection configuration builder.
		ConnectionConfigBuilder builder = Communication.createConnectionConfigBuilder(address, port, config);
		
		return Communication.createCustomConnection(builder.build(), new TcpConnectionImpl(socket), Mode.CLIENT_TO_SERVER);
	}
	
	@Override
	public void postInitialise() {
		// Do nothing	
	}
}
