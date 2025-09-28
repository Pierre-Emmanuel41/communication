package fr.pederobien.communication.impl.client;

import fr.pederobien.communication.interfaces.connection.IUdpSocket;
import fr.pederobien.utils.Disposable;
import fr.pederobien.utils.IDisposable;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Arrays;

public class UdpSocket implements IUdpSocket {
	/**
	 * Key word to send to the remote in order to create a client
	 */
	private static final byte[] INIT = ".INIT".getBytes();

	/**
	 * Key word to send to the remote in order to close the connection
	 */
	private static final byte[] CLOSE = ".CLOSE".getBytes();

	private IUdpSocket impl;

	/**
	 * Creates a simple UDP socket.
	 *
	 * @param address The address of the remote.
	 */
	public UdpSocket(InetSocketAddress address) {
		try {
			impl = new ConnectedSocket(new DatagramSocket(), address);

			// Sending .INIT message to create a UDP client on server side
			impl.send(INIT);
		} catch (Exception e) {
			impl = new NotConnectedSocket();
		}
	}

	@Override
	public void send(byte[] data) throws Exception {
		impl.send(data);
	}

	@Override
	public DatagramPacket receive() throws Exception {
		return impl.receive();
	}

	@Override
	public void close() {
		impl.close();
	}

	@Override
	public InetSocketAddress getInetAddress() {
		return impl.getInetAddress();
	}

	private static class NotConnectedSocket implements IUdpSocket {

		@Override
		public void send(byte[] data) throws Exception {
			throw new IllegalStateException("UDP socket not connected");
		}

		@Override
		public DatagramPacket receive() throws Exception {
			throw new IllegalStateException("UDP socket not connected");
		}

		@Override
		public void close() {
			// Do nothing
		}

		@Override
		public InetSocketAddress getInetAddress() {
			return null;
		}
	}

	private static class ConnectedSocket implements IUdpSocket {
		private final DatagramSocket socket;
		private final InetSocketAddress address;
		private final IDisposable disposable;

		public ConnectedSocket(DatagramSocket socket, InetSocketAddress address) {
			this.socket = socket;
			this.address = address;

			disposable = new Disposable();
		}

		@Override
		public void send(byte[] data) throws Exception {
			disposable.checkDisposed();
			socket.send(new DatagramPacket(data, data.length, address));
		}

		@Override
		public DatagramPacket receive() throws Exception {
			disposable.checkDisposed();

			byte[] buffer = new byte[2048];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

			try {
				socket.receive(packet);

				// Checking if connection has to be closed
				if (packet.getLength() == CLOSE.length) {
					byte[] data = new byte[CLOSE.length];
					System.arraycopy(packet.getData(), 0, data, 0, CLOSE.length);

					if (Arrays.equals(data, CLOSE)) {
						packet = null;
					}
				}

			} catch (SocketException e) {
				packet = null;
			}

			return packet;
		}

		@Override
		public void close() {
			if (disposable.dispose()) {
				try {
					// Notifying remote the connection has been closed.
					socket.send(new DatagramPacket(CLOSE, CLOSE.length, address));
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					socket.close();
				}
			}
		}

		@Override
		public InetSocketAddress getInetAddress() {
			return address;
		}
	}
}
