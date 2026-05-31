package fr.pederobien.communication.impl.server.ethernet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import fr.pederobien.communication.interfaces.connection.IUdpSocket;
import fr.pederobien.communication.interfaces.server.IServerEthernetEndPoint;
import fr.pederobien.utils.BlockingQueueTask;
import fr.pederobien.utils.ByteWrapper;
import fr.pederobien.utils.event.Logger;

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
	private static final int BUFFER_SIZE = 1400;

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
	public UdpServerSocket(String name, IServerEthernetEndPoint endPoint) throws Exception {
		socket = createDatagramSocket(endPoint);

		if (socket == null)
			throw new Exception("Cannot create a UDP server, please check if the IP address and port number are already in use");

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

	/**
	 * Create a datagram socket associated to the properties of the given end-point.
	 * 
	 * @param endPoint The end-point that contains the port number to use or the range to use to create a server socket.
	 * @return The server socket if it was possible
	 * @throws IOException
	 */
	private DatagramSocket createDatagramSocket(IServerEthernetEndPoint endPoint) throws IOException {

		// A port number is specified
		if (0 <= endPoint.getPort()) {
			// Note: The port number does not matter, if the value is out of range, the socket will throw an exception
			// if the value is 0, the host machine will choose an ephemeral (ie first free) port.

			return new DatagramSocket(createAddress(endPoint.getAddress(), endPoint.getPort()));
		}

		// If a port range is defined
		if (endPoint.getMin() > 0 && endPoint.getMax() > 0) {
			for (int port = endPoint.getMin(); port <= endPoint.getMax(); port++) {
				DatagramSocket socket = tryBindToAddress(createAddress(endPoint.getAddress(), port));
				if (socket != null)
					return socket;
			}

			Logger.error("Could not bind to any port in range [%s-%s]", endPoint.getMin(), endPoint.getMax());
		}

		return null;
	}

	/**
	 * Creates a socket address from the given IP address and port number.
	 * 
	 * @param address The IP address to use.
	 * @param port    The port number to use.
	 * 
	 * @return The created SocketAddress.
	 */
	private SocketAddress createAddress(String address, int port) {
		if (address.equals("*"))
			return new InetSocketAddress(port);
		else
			return new InetSocketAddress(address, port);
	}

	/**
	 * Check if a datagram socket can be bound to the given address.
	 * 
	 * @param address The address to use for binding.
	 * @return A non null datagram if the address is available, null otherwise.
	 */
	private DatagramSocket tryBindToAddress(SocketAddress address) {
		try {
			return new DatagramSocket(address);
		} catch (Exception e) {
			return null;
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
