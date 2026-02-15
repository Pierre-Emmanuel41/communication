package fr.pederobien.communication.impl.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import fr.pederobien.communication.interfaces.connection.IUdpSocket;
import fr.pederobien.utils.BlockingQueueTask;
import fr.pederobien.utils.ByteWrapper;

public class UdpServerSocket {
	/**
	 * Key word to send to the remote in order to create a client
	 */
	private static final byte[] INIT = ".INIT".getBytes();

	/**
	 * Key word to send to the remote in order to close the connection
	 */
	protected static final byte[] CLOSE = ".CLOSE".getBytes();

	/**
	 * The size of the buffer used to receive data from the remote.
	 */
	private static final int BUFFER_SIZE = 1500;

	private final DatagramSocket socket;
	private final BlockingQueueTask<DatagramPacket> sendingQueue;
	private final Thread receivingThread;
	private final SocketManager socketManager;
	private final int localPort;

	/**
	 * Creates a socket to communicate through UDP with the remote.
	 *
	 * @param name    The name of this socket.
	 * @param address The address of this socket.
	 * @param port    The port number of this socket.
	 */
	public UdpServerSocket(String name, String address, int port) throws Exception {
		// Note: The port number does not matter, if the value is out of range, the socket will throw an exception
		// if the value is 0, the host machine will choose an ephemeral (ie first free) port.

		// Case 1: Any address
		if (address.equals("*")) {
			socket = new DatagramSocket(port);
		}
		// Case 2: Specific hostname
		else {
			socket = new DatagramSocket(port, InetAddress.getByName(address));
		}

		localPort = socket.getLocalPort();

		sendingQueue = new BlockingQueueTask<DatagramPacket>(name + "_send", this::sending);
		receivingThread = new Thread(this::receiving, name + "_receive");
		socketManager = new SocketManager(this);

		// Starting thread waiting for sending data to the remote
		sendingQueue.start();

		// Starting thread looping for receiving data from the remote.
		receivingThread.setDaemon(true);
		receivingThread.start();
	}

	/**
	 * Returns the port number on the local host to which this socket is bound.
	 *
	 * @return the port number on the local host to which this socket is bound, {@code -1} if the socket is closed, or {@code 0} if it
	 *         is not bound yet.
	 */
	protected int getLocalPort() {
		return localPort;
	}

	/**
	 * Close this socket.
	 */
	public void close() {
		socket.close();
	}

	/**
	 * Blocks until data has been received from an unknown address.
	 *
	 * @return The socket bound to the remote.
	 */
	public IUdpSocket accept() throws InterruptedException {
		return socketManager.waitForNewClient();
	}

	/**
	 * Connection specific implementation to send a message to the remote. The bytes array is the result of the layer that has
	 * encapsulated the payload with other information in order to be received correctly.
	 *
	 * @param data    The byte array to send to the remote.
	 * @param address The socket address that contains the IP address and the port number of the remote.
	 */
	protected void send(byte[] data, InetSocketAddress address) {
		if (data.length < BUFFER_SIZE)
			sendingQueue.add(new DatagramPacket(data, data.length, address));
		else {
			int quotient = data.length / BUFFER_SIZE;
			int remainder = data.length % BUFFER_SIZE;
			ByteWrapper wrapper = ByteWrapper.wrap(data);

			for (int i = 0; i < quotient; i++)
				sendingQueue.add(new DatagramPacket(wrapper.extract(i * BUFFER_SIZE, BUFFER_SIZE), BUFFER_SIZE, address));

			sendingQueue.add(new DatagramPacket(wrapper.extract(quotient * BUFFER_SIZE, remainder), remainder, address));
		}
	}

	/**
	 * Connection specific implementation to receive bytes from the remote.
	 *
	 * @param address The address
	 * @return The packet received from the remote.
	 */
	protected DatagramPacket receive(InetSocketAddress address) {
		try {
			return socketManager.get(address).take();
		} catch (Exception e) {
			// Connection being closed
			return null;
		}
	}

	/**
	 * Remove the waiter associated to the given address.
	 *
	 * @param address The remote address of the waiter.
	 */
	protected void unregister(InetSocketAddress address) {
		socketManager.unregister(address);
	}

	/**
	 * Send the packet to the remote.
	 *
	 * @param packet The packet to send.
	 */
	private void sending(DatagramPacket packet) {
		try {
			socket.send(packet);
		} catch (Exception e) {
			// TODO: unstable counter
		}
	}

	/**
	 * Block until data has been received from the remote.
	 */
	private void receiving() {
		try {
			while (true) {
				byte[] buffer = new byte[BUFFER_SIZE];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet);

				// Dispatching the packet to the correct client
				socketManager.onDataReceived(packet);
			}
		} catch (Exception e) {
			// Server has been closed
		}
	}

	private static class SocketManager {
		private final UdpServerSocket serverSocket;
		private final Object lock;
		private final Map<SocketAddress, BlockingQueue<DatagramPacket>> waiters;
		private final Semaphore semaphore;
		private IUdpSocket socket;

		/**
		 * Creates an object waiting for new UDP client to be connected.
		 *
		 * @param serverSocket The server socket on which UDP will be connected.
		 */
		public SocketManager(UdpServerSocket serverSocket) {
			this.serverSocket = serverSocket;

			lock = new Object();
			waiters = new HashMap<SocketAddress, BlockingQueue<DatagramPacket>>();
			semaphore = new Semaphore(0);
		}

		/**
		 * Blocks until a new client is connected to the server.
		 *
		 * @return The socket connected with the remote.
		 */
		public IUdpSocket waitForNewClient() throws InterruptedException {
			semaphore.acquire();
			return socket;
		}

		/**
		 * If no waiter is registered for the packet address then a new waiter is created, else the existing waiter will be notified that
		 * data has been received.
		 *
		 * @param packet The packet received from the network.
		 */
		public void onDataReceived(DatagramPacket packet) {
			BlockingQueue<DatagramPacket> queue = null;
			synchronized (lock) {
				queue = waiters.get(packet.getSocketAddress());
			}

			if (queue == null) {

				// If the server receives a close request from an unknown client, then ignore
				if (packet.getLength() == CLOSE.length) {
					byte[] data = new byte[CLOSE.length];
					System.arraycopy(packet.getData(), 0, data, 0, CLOSE.length);

					if (Arrays.equals(data, CLOSE))
						return;
				}

				queue = new LinkedBlockingQueue<DatagramPacket>(100);
				waiters.put(packet.getSocketAddress(), queue);
				socket = new UdpSocket(serverSocket, (InetSocketAddress) packet.getSocketAddress());

				// If the packet contains something different from INIT then transmitting to the application
				if (packet.getLength() != INIT.length)
					queue.add(packet);
				else if (packet.getLength() == INIT.length) {
					byte[] data = new byte[INIT.length];
					System.arraycopy(packet.getData(), 0, data, 0, INIT.length);

					if (!Arrays.equals(data, INIT))
						queue.add(packet);
				}

				semaphore.release();
			} else {
				try {
					queue.offer(packet);
				} catch (Exception e) {
					// Do nothing
				}
			}
		}

		/**
		 * Get the waiter associated to the given address.
		 *
		 * @param address The address of the remote.
		 * @return The waiter associated to the remote address.
		 */
		public BlockingQueue<DatagramPacket> get(SocketAddress address) {
			BlockingQueue<DatagramPacket> queue = null;
			synchronized (lock) {
				queue = waiters.get(address);
			}

			return queue;
		}

		/**
		 * Remove the waiter associated to the given address.
		 *
		 * @param address The remote address of the waiter.
		 */
		public void unregister(SocketAddress address) {
			synchronized (lock) {
				waiters.remove(address);
			}
		}
	}
}
