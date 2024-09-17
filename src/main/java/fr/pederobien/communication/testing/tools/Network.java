package fr.pederobien.communication.testing.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import fr.pederobien.communication.impl.Communication;
import fr.pederobien.communication.impl.connection.ConnectionConfigBuilder;
import fr.pederobien.communication.interfaces.IClientConfig;
import fr.pederobien.communication.interfaces.IClientImpl;
import fr.pederobien.communication.interfaces.IConnection;
import fr.pederobien.communication.interfaces.IConnectionImpl;
import fr.pederobien.communication.interfaces.IServerConfig;
import fr.pederobien.communication.interfaces.IServerImpl;
import fr.pederobien.utils.Watchdog;

public class Network {

	public static interface INetworkSimulator {

		/**
		 * Modify or not the data sent from one point to another point.
		 * 
		 * @param local The address of the sender.
		 * @param remote The address of the receiver.
		 * @param data The data to send to the receiver.
		 * 
		 * @return The data the receiver will receive.
		 */
		byte[] simulate(Address local, Address remote, byte[] data);
	}

	private Networkstakeholder network;
	private IServerImpl server;

	/**
	 * Create a network to simulate data transmission.
	 * 
	 * @param simulator To simulate an issue while sending data.
	 */
	public Network(INetworkSimulator simulator) {
		network = new Networkstakeholder(simulator);
		server = new ServerImpl(network);
	}

	/**
	 * Creates a network that does not modify data when sent from one point to another point.
	 */
	public Network() {
		this((local, remote, data) -> data);
	}

	/**
	 * @return The server of this network.
	 */
	public IServerImpl getServer() {
		return server;
	}

	/**
	 * @return A new client ready to be connected to the server.
	 */
	public IClientImpl newClient() {
		return new ClientImpl(network);
	}


	private class Networkstakeholder {
		private INetworkSimulator simulator;
		private NetworkServerSocket serverSocket;
		private List<NetworkSocket> sockets;

		/**
		 * Create a network to transmit data from a point to another point.
		 * 
		 * @param simulator To simulate an issue while sending data.
		 */
		public Networkstakeholder(INetworkSimulator simulator) {
			this.simulator = simulator;
			
			sockets = new ArrayList<NetworkSocket>();
		}

		/**
		 * Set the server socket for this network.
		 * 
		 * @param serverSocket The server socket of the network.
		 */
		protected void setServerSocket(NetworkServerSocket serverSocket) {
			this.serverSocket = serverSocket;
		}

		protected void registerSocket(NetworkSocket socket) {
			sockets.add(socket);
		}
		
		protected void unregister(NetworkSocket socket) {
			sockets.remove(socket);
		}

		/**
		 * Attempt a connection with the server.
		 * 
		 * @param local the address of the client
		 * @param remote The address of the server.
		 * @param timeout The time, in ms, after which a timeout occurs
		 * 
		 * @return The local address of the remote.
		 */
		protected Address connect(Address local, Address remote, int timeout) throws Exception {
			boolean success = Watchdog.start(() -> {
				boolean connected = false;
				while (!connected) {
					if (serverSocket == null || !serverSocket.isOpen() || remote.getPort() != serverSocket.getPort())
						Thread.sleep(10);
					else
						connected = true;
				}
			}, timeout);
			
			return success ? serverSocket.notifyNewClient(local) : null;
		}

		/**
		 * Send data to the remote.
		 * 
		 * @param local The address of the sender.
		 * @param remote The address of the remote.
		 * @param data The data to send.
		 */
		protected void send(Address local, Address remote, byte[] data) {
			for (NetworkSocket socket : sockets) {
				if (socket.getLocal() == remote)
					socket.notifyDataReceived(simulator.simulate(local, remote, data));
			}
		}

		/**
		 * Send a notification to notify the remote that the connection has been closed by the remote.
		 * 
		 * @param remote The address of the remote to notify.
		 */
		protected void close(Address remote) {
			for (NetworkSocket socket : sockets) {
				if (socket.getLocal() == remote)
					socket.notifyConnectionClosed();
			}
		}
	}
	
	private class Address {
		private String address;
		private int port;

		public Address(String address, int port) {
			this.address = address;
			this.port = port;
		}

		/**
		 * @return The address of this address.
		 */
		public String getAddress() {
			return address;
		}

		/**
		 * @return The port of this address.
		 */
		public int getPort() {
			return port;
		}
	}

	private class NetworkServerSocket {
		private Networkstakeholder network;
		private int port;
		private Semaphore semaphore;
		private NetworkSocket socket;
		private boolean isOpen;

		/**
		 * Create a network socket waiting for a new client.
		 * 
		 * @param network The network used to send/receive data from the remote.
		 * @param port The port number of the server.
		 */
		public NetworkServerSocket(Networkstakeholder network, int port) {
			this.network = network;
			this.port = port;
			semaphore = new Semaphore(0);
			
			network.setServerSocket(this);
		}

		/**
		 * @return The port number of this server socket.
		 */
		public int getPort() {
			return port;
		}

		/**
		 * Block until a client attempt a connection.
		 * 
		 * @return The socket connected to the client.
		 */
		public NetworkSocket accept() throws Exception {
			isOpen = true;

			semaphore.acquire();

			if (!isOpen)
				throw new RuntimeException("Socket closed");

			return socket;
		}

		/**
		 * Close this socket. The method waitForClient will throw a runtime exception.
		 */
		public void close() {
			isOpen = false;
			semaphore.release();
		}

		/**
		 * @return True if the server is opened, false otherwise.
		 */
		public boolean isOpen() {
			return isOpen;
		}

		/**
		 * Release the underlying semaphore to create a client server connection.
		 * 
		 * @param remote The address of the client.
		 * 
		 * @return The local address of the socket.
		 */
		protected Address notifyNewClient(Address remote) {
			socket = new NetworkSocket(network, remote);
			semaphore.release();
			return socket.getLocal();
		}
	}

	private class NetworkSocket {
		private static final AtomicInteger PORT = new AtomicInteger(1);
		private Networkstakeholder network;
		private Address local, remote;
		private Semaphore semaphore;
		private byte[] data;
		
		/**
		 * Create a socket used to send data to the remote.
		 * 
		 * @param network The network used to send/receive data from the remote.
		 * @param remote The remote address of the socket.
		 */
		public NetworkSocket(Networkstakeholder network, Address remote) {
			this.network = network;
			this.local = new Address("127.0.0.1", PORT.getAndIncrement());
			this.remote = remote;

			semaphore = new Semaphore(0);

			network.registerSocket(this);
		}

		/**
		 * Connect the client to the server
		 * 
		 * @param timeout The time, in ms, after which a timeout occurs
		 */
		public void connect(int timeout) throws Exception {
			Address address = network.connect(getLocal(), getRemote(), timeout);
			if (address == null) {
				network.unregister(this);
				throw new RuntimeException("Timeout occurs");
			}
			
			// Updating the remote address
			remote = address;
		}

		/**
		 * Send the given byte array to the remote.
		 * 
		 * @param data The data to send.
		 */
		public void send(byte[] data) {
			network.send(getLocal(), getRemote(), data);
		}

		/**
		 * Block until data has been received from the remote.
		 * 
		 * @return the raw data received from the remote.
		 */
		public byte[] receive() throws Exception {
			semaphore.acquire();
			return data;
		}

		/**
		 * Notify the remote that is socket is closed.
		 */
		public void close() {
			network.close(getRemote());
		}

		/**
		 * @return The local address.
		 */
		public Address getLocal() {
			return local;
		}

		/**
		 * @return The address of the remote.
		 */
		public Address getRemote() {
			return remote;
		}

		/**
		 * Notify this connection that data has been received.
		 * 
		 * @param data The byte array received from the remote.
		 */
		protected void notifyDataReceived(byte[] data) {
			this.data = data;
			semaphore.release();
		}

		/**
		 * Notify this connection that the remote closed the connection.
		 */
		protected void notifyConnectionClosed() {
			data = null;
			semaphore.release();
		}
	}

	private class Connection implements IConnectionImpl {
		private NetworkSocket socket;

		public Connection(NetworkSocket socket) {
			this.socket = socket;
		}

		@Override
		public void sendImpl(byte[] data) throws Exception {
			socket.send(data);
		}

		@Override
		public byte[] receiveImpl(int receivingBufferSize) throws Exception {
			return socket.receive();
		}

		@Override
		public void disposeImpl() {
			socket.close();
		}
	}

	private class ClientImpl implements IClientImpl {
		private Networkstakeholder network;
		private NetworkSocket socket;

		public ClientImpl(Networkstakeholder network) {
			this.network = network;
		}

		@Override
		public void connectImpl(String address, int port, int connectionTimeout) throws Exception {
			socket = new NetworkSocket(network, new Address(address, port));
			socket.connect(connectionTimeout);
		}

		@Override
		public IConnection onConnectionComplete(IClientConfig config) {
			String address = socket.getRemote().getAddress();
			int port = socket.getRemote().getPort();

			// Creating connection config builder
			ConnectionConfigBuilder builder = Communication.createConnectionConfigBuilder(address, port, config);

			return Communication.createCustomConnection(builder.build(), new Connection(socket), config.getMode());
		}
	}

	private class ServerImpl implements IServerImpl {
		private Networkstakeholder network;
		private NetworkServerSocket server;

		public ServerImpl(Networkstakeholder network) {
			this.network = network;
		}

		@Override
		public void openImpl(int port) throws Exception {
			server = new NetworkServerSocket(network, port);
		}

		@Override
		public void closeImpl() throws Exception {
			server.close();
		}

		@Override
		public IConnection waitForClientImpl(IServerConfig config) throws Exception {
			NetworkSocket socket = server.accept();

			String address = socket.getRemote().getAddress();
			int port = socket.getRemote().getPort();

			// Creating connection config builder
			ConnectionConfigBuilder builder = Communication.createConnectionConfigBuilder(address, port, config);

			return Communication.createCustomConnection(builder.build(), new Connection(socket), config.getMode());
		}
	}
}
