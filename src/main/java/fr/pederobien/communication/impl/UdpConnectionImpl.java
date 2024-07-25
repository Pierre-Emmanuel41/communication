package fr.pederobien.communication.impl;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import fr.pederobien.communication.interfaces.IConnectionImpl;
import fr.pederobien.utils.ByteWrapper;

public class UdpConnectionImpl implements IConnectionImpl {
	private DatagramSocket socket;
	
	/**
	 * Creates a connection specific for UDP protocol.
	 * 
	 * @param socket The socket to use to send/receive data from the remote.
	 */
	public UdpConnectionImpl(DatagramSocket socket) {
		this.socket = socket;
	}

	@Override
	public void sendImpl(byte[] data) throws Exception {
		DatagramPacket packet = null;
		socket.send(packet);
	}

	@Override
	public byte[] receiveImpl(int receivingBufferSize) throws Exception {
		byte[] buffer = new byte[1024];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		socket.receive(packet);
		
		return packet.getLength() == -1 ? null : ByteWrapper.wrap(packet.getData()).extract(0, packet.getLength());
	}
}
