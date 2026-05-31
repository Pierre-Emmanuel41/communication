package fr.pederobien.communication.impl.client.ethernet;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Arrays;

import fr.pederobien.communication.interfaces.connection.IUdpSocket;
import fr.pederobien.utils.ByteWrapper;
import fr.pederobien.utils.Disposable;
import fr.pederobien.utils.IDisposable;

public class UdpSocket implements IUdpSocket {
	/**
	 * Key word to send to the remote in order to create a client
	 */
	private static final byte[] INIT = ".INIT".getBytes();

	/**
	 * Key word to send to the remote in order to close the connection
	 */
	private static final byte[] CLOSE = ".CLOSE".getBytes();

	/**
	 * The size of the buffer used to receive data from the remote.
	 */
	private static final int BUFFER_SIZE = 1400;

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
		private boolean closeRequestReceived;

		public ConnectedSocket(DatagramSocket socket, InetSocketAddress address) {
			this.socket = socket;
			this.address = address;

			disposable = new Disposable();
			closeRequestReceived = false;
		}

		@Override
		public void send(byte[] data) throws Exception {
			disposable.checkDisposed();

			// Data can be sent with one packet
			if (data.length < BUFFER_SIZE)
				socket.send(new DatagramPacket(data, data.length, address));
			else {
				int quotient = data.length / BUFFER_SIZE;
				int remainder = data.length % BUFFER_SIZE;
				ByteWrapper wrapper = ByteWrapper.wrap(data);

				for (int i = 0; i < quotient; i++)
					socket.send(new DatagramPacket(wrapper.extract(i * BUFFER_SIZE, BUFFER_SIZE), BUFFER_SIZE, address));

				socket.send(new DatagramPacket(wrapper.extract(quotient * BUFFER_SIZE, remainder), remainder, address));
			}
		}

		@Override
		public DatagramPacket receive() throws Exception {
			disposable.checkDisposed();

			byte[] buffer = new byte[BUFFER_SIZE];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

			try {
				socket.receive(packet);

				// Checking if connection has to be closed
				if (packet.getLength() == CLOSE.length) {
					byte[] data = new byte[CLOSE.length];
					System.arraycopy(packet.getData(), 0, data, 0, CLOSE.length);

					if (Arrays.equals(data, CLOSE)) {
						closeRequestReceived = true;
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
					if (!closeRequestReceived)
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
