package fr.pederobien.communication.impl.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import fr.pederobien.communication.interfaces.connection.IUdpSocket;
import fr.pederobien.utils.BlockingQueueTask;

public class UdpServerSocket {
	private DatagramSocket socket;
	private BlockingQueueTask<DatagramPacket> sendingQueue;
	private BlockingQueueTask<DatagramPacket> notifyingQueue;
	private Thread receivingThread;
	private NewClientWaiter clientWaiter;

	public UdpServerSocket(String name, int port) throws Exception {
		socket = new DatagramSocket(port);

		sendingQueue = new BlockingQueueTask<DatagramPacket>(name + "_send", packet -> sending(packet));
		notifyingQueue = new BlockingQueueTask<DatagramPacket>(name + "_notify", packet -> notifying(packet));
		receivingThread = new Thread(() -> receiving(), name + "_receive");
		clientWaiter = new NewClientWaiter(this);

		// Starting thread waiting for sending data to the remote
		sendingQueue.start();

		// Starting thread looping for receiving data from the remote.
		receivingThread.setDaemon(true);
		receivingThread.start();

		// Starting thread waiting for packet reception
		notifyingQueue.start();
	}

	public void close() {
		socket.close();
	}

	/**
	 * Blocks until data has been received from an unknown address.
	 * 
	 * @return The socket bound to the remote.
	 */
	public IUdpSocket accept() throws InterruptedException {
		return clientWaiter.waitForNewClient();
	}

	/**
	 * Connection specific implementation to send a message to the remote. The bytes
	 * array is the result of the layer that has encapsulated the payload with other
	 * information in order to be received correctly.
	 * 
	 * @param data    The byte array to send to the remote.
	 * @param address The socket address that contains the IP address and the port
	 *                number of the remote.
	 */
	protected void send(byte[] data, InetSocketAddress address) {
		sendingQueue.add(new DatagramPacket(data, data.length, address));
	}

	/**
	 * Connection specific implementation to receive bytes from the remote.
	 * 
	 * @param address The address
	 * 
	 * @return The packet received from the remote.
	 */
	protected DatagramPacket receive(InetSocketAddress address) {
		return clientWaiter.get(address).waitForReception();
	}

	/**
	 * Remove the waiter associated to the given address.
	 * 
	 * @param address The remote address of the waiter.
	 */
	protected void unregister(InetSocketAddress address) {
		clientWaiter.unregister(address);
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
				byte[] buffer = new byte[2048];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet);

				// Handling the received in a dedicated task
				notifyingQueue.add(packet);
			}
		} catch (Exception e) {
			// Server has been closed
		}
	}

	/**
	 * If no waiter is registered for the packet address then a new waiter is
	 * created, else the existing waiter will be notified that data has been
	 * received.
	 * 
	 * @param packet The packet received from the network.
	 */
	private void notifying(DatagramPacket packet) {
		clientWaiter.onDataReceived(packet);
	}

	private class Waiter {
		private Semaphore semaphore;
		private DatagramPacket packet;

		/**
		 * Creates an object that will wait until data has been received
		 */
		public Waiter() {
			semaphore = new Semaphore(0);
		}

		/**
		 * Block until data has been received for this object.
		 * 
		 * @return The packet received from the remote.
		 */
		public DatagramPacket waitForReception() {
			try {
				semaphore.acquire();
			} catch (InterruptedException e) {
				return null;
			}

			return packet;
		}

		/**
		 * Set the byte array received from the remote and notify for further
		 * processing.
		 * 
		 * @param buffer The byte array received from the remote.
		 */
		public void notifyForReception(DatagramPacket packet) {
			this.packet = packet;
			semaphore.release();
		}
	}

	private class NewClientWaiter {
		private UdpServerSocket serverSocket;
		private Object lock;
		private Map<SocketAddress, Waiter> waiters;
		private Semaphore semaphore;
		private IUdpSocket socket;

		/**
		 * Creates an object waiting for new UDP client to be connected.
		 * 
		 * @param serverSocket The server socket on which UDP will be connected.
		 */
		public NewClientWaiter(UdpServerSocket serverSocket) {
			this.serverSocket = serverSocket;

			lock = new Object();
			waiters = new HashMap<SocketAddress, Waiter>();
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
		 * If no waiter is registered for the packet address then a new waiter is
		 * created, else the existing waiter will be notified that data has been
		 * received.
		 * 
		 * @param packet The packet received from the network.
		 */
		public void onDataReceived(DatagramPacket packet) {
			Waiter waiter = null;
			synchronized (lock) {
				waiter = waiters.get(packet.getSocketAddress());
			}

			if (waiter == null) {
				waiter = new Waiter();
				waiters.put(packet.getSocketAddress(), waiter);
				socket = new UdpSocket(serverSocket, (InetSocketAddress) packet.getSocketAddress());
				semaphore.release();
			} else {
				waiter.notifyForReception(packet);
			}
		}

		/**
		 * Get the waiter associated to the given address.
		 * 
		 * @param address The address of the remote.
		 * 
		 * @return The waiter associated to the remote address.
		 */
		public Waiter get(SocketAddress address) {
			Waiter waiter = null;
			synchronized (lock) {
				waiter = waiters.get(address);
			}

			return waiter;
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
