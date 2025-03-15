package fr.pederobien.communication.impl.connection;

import java.net.DatagramPacket;

import fr.pederobien.communication.interfaces.connection.IConnectionImpl;
import fr.pederobien.communication.interfaces.connection.IUdpSocket;
import fr.pederobien.utils.ByteWrapper;

public class UdpConnectionImpl implements IConnectionImpl {
	private IUdpSocket socket;
	
	/**
	 * Creates a connection specific for UDP protocol.
	 * 
	 * @param socket The socket to use to send/receive data from the remote.
	 * @param address The address of the remote.
	 * @param port The port number of the remote.
	 */
	public UdpConnectionImpl(IUdpSocket socket) {
		this.socket = socket;
	}

	@Override
	public void sendImpl(byte[] data) throws Exception {
		socket.send(data);
	}

	@Override
	public byte[] receiveImpl() throws Exception {
		DatagramPacket packet = socket.receive();
		
		// Connection lost
		if (packet == null)
			return null;
		
		return ByteWrapper.wrap(packet.getData()).extract(0, packet.getLength());
	}

	@Override
	public void disposeImpl() {
		if (socket == null)
			return;
		
		socket.close();
	}
}
