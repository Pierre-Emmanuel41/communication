package fr.pederobien.communication.impl.server;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.Arrays;

import fr.pederobien.communication.interfaces.connection.IUdpSocket;
import fr.pederobien.utils.Disposable;
import fr.pederobien.utils.IDisposable;

public class UdpSocket implements IUdpSocket {
	/**
	 * Key word to send to the remote in order to close the connection
	 */
	private static final byte[] CLOSE = ".CLOSE".getBytes();

	private UdpServerSocket socket;
	private InetSocketAddress address;
	private IDisposable disposable;

	/**
	 * Creates a server socket for UDP protocol.
	 * 
	 * @param name    The socket name.
	 * @param socket  The server socket used to send data to the remote.
	 * @param address The address of the remote.
	 */
	public UdpSocket(UdpServerSocket socket, InetSocketAddress address) {
		this.socket = socket;
		this.address = address;

		disposable = new Disposable();
	}

	@Override
	public void send(byte[] data) throws Exception {
		disposable.checkDisposed();
		socket.send(data, address);
	}

	@Override
	public DatagramPacket receive() throws Exception {
		disposable.checkDisposed();

		DatagramPacket packet = socket.receive(address);

		// Checking if connection has to be closed
		if (packet.getLength() == CLOSE.length) {
			byte[] data = new byte[CLOSE.length];
			System.arraycopy(packet.getData(), 0, data, 0, CLOSE.length);

			if (Arrays.equals(data, CLOSE)) {
				packet = null;
			}
		}

		return packet;
	}

	@Override
	public void close() {
		if (disposable.dispose()) {
			try {
				socket.send(CLOSE, address);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				socket.unregister(address);
			}
		}
	}

	@Override
	public InetSocketAddress getInetAddress() {
		return address;
	}
}
