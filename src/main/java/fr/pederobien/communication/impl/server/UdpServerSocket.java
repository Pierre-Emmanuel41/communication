package fr.pederobien.communication.impl.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import fr.pederobien.communication.interfaces.connection.IUdpSocket;
import fr.pederobien.utils.BlockingQueueTask;

public class UdpServerSocket {
	private DatagramSocket socket;
	private BlockingQueueTask<DatagramPacket> sendingQueue;
	private BlockingQueueTask<Object> receivingQueue;
	private Map<InetSocketAddress, Waiter> waiters;
	private Object lock;
	private NewClientWaiter clientWaiter;

	public UdpServerSocket(String name, int port) throws Exception {
		socket = new DatagramSocket(port);

		sendingQueue = new BlockingQueueTask<DatagramPacket>(String.format("%s_send", name), packet -> sending(packet));
		receivingQueue = new BlockingQueueTask<Object>(String.format("%s_receive", name),
				ignored -> receiving(ignored));
		waiters = new HashMap<InetSocketAddress, Waiter>();
		lock = new Object();
		clientWaiter = new NewClientWaiter(this);
	}

	/**
	 * Starts underlying thread to send/receive data to the remote.
	 */
	public void start() {
		// Starting thread waiting for sending data to the remote
		sendingQueue.start();

		// Starting thread looping for receiving data from the remote.
		receivingQueue.start();
		receivingQueue.add(new Object());
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
		Waiter waiter;
		synchronized (lock) {
			waiter = waiters.get(address);
		}

		if (waiter == null) {
			waiter = new Waiter();
			waiters.put(address, waiter);
		}

		return waiter.waitForReception();
	}

	protected void unregister(InetSocketAddress address) {
		synchronized (lock) {
			waiters.remove(address);
		}
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
	 * 
	 * @param ignored Object used to loop for receiving data from remote.
	 */
	private void receiving(Object ignored) {
		byte[] buffer = new byte[2048];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

		try {
			socket.receive(packet);

			receivingQueue.add(ignored);
		} catch (IOException e) {
			// TODO: unstable counter
		}

		Waiter waiter;
		synchronized (lock) {
			waiter = waiters.get(packet.getSocketAddress());
		}

		if (waiter != null) {
			waiter.notifyForReception(packet);
		} else {
			clientWaiter.notifyForNewClient(packet);
		}
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
		private Semaphore semaphore;
		private IUdpSocket socket;

		public NewClientWaiter(UdpServerSocket serverSocket) {
			this.serverSocket = serverSocket;
			semaphore = new Semaphore(0);
		}

		public IUdpSocket waitForNewClient() throws InterruptedException {
			semaphore.acquire();
			return socket;
		}

		public void notifyForNewClient(DatagramPacket packet) {
			socket = new UdpSocket(serverSocket, (InetSocketAddress) packet.getSocketAddress());
			semaphore.release();
		}
	}
}
