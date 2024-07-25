package fr.pederobien.communication.impl;

import java.net.DatagramSocket;

import fr.pederobien.communication.interfaces.IConnection;
import fr.pederobien.communication.interfaces.IServerConfig;
import fr.pederobien.communication.interfaces.IServerImpl;

public class UdpServerImpl_ implements IServerImpl {
	private DatagramSocket serverSocket;
	
	/**
	 * Creates a UDP implementation for a server.
	 */
	public UdpServerImpl_() {
		// Do nothing
	}

	@Override
	public void openImpl(int port) throws Exception {
		serverSocket = new DatagramSocket(port);
	}

	@Override
	public void closeImpl() throws Exception {
		serverSocket.close();
	}

	@Override
	public IConnection waitForClientImpl(IServerConfig config) throws Exception {
		return null;
	}
}
