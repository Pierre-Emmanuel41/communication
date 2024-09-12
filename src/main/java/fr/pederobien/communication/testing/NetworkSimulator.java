package fr.pederobien.communication.testing;

import java.util.concurrent.Semaphore;
import java.util.function.Function;

import fr.pederobien.communication.interfaces.IConnectionImpl;

public class NetworkSimulator {
	private ConnectionImpl client;
	private ConnectionImpl server;
	private Function<byte[], byte[]> clientToServer;
	private Function<byte[], byte[]> serverToClient;
	
	/**
	 * Create a network simulator that will simulate an issue while sending data to the server or
	 * to the client.
	 * 
	 * @param clientToServer The modifier that simulate an issue in the direction client to server.
	 * @param serverToClient The modifier that simulate an issue in the direction server to client.
	 */
	public NetworkSimulator(Function<byte[], byte[]> clientToServer, Function<byte[], byte[]> serverToClient) {
		this.clientToServer = clientToServer;
		this.serverToClient = serverToClient;
		client = new ConnectionImpl(this);
		server = new ConnectionImpl(this);
	}
	
	/**
	 * Create a network simulator that will simulate an issue while sending data to the server or
	 * to the client.
	 * 
	 * @param modifier The modifier that simulate an issue in both directions : client <-> server.
	 */
	public NetworkSimulator(Function<byte[], byte[]> modifier) {
		this(modifier, modifier);
	}
	
	/**
	 * Create a simple network simulator.
	 */
	public NetworkSimulator() {
		this(bytes -> bytes);
	}
	
	public IConnectionImpl getClient() {
		return client;
	}
	
	public IConnectionImpl getServer() {
		return server;
	}
	
	private void sendToRemote(IConnectionImpl local, byte[] data) {
		// Calling modifier in order to simulate an issue while sending data to the remote.
		if (local == client)
			server.notifyReceptionForReception(clientToServer.apply(data));
		else
			client.notifyReceptionForReception(serverToClient.apply(data));
	}
	
	private void notifyConnectionClosed(IConnectionImpl local) {
		if (local == client)
			server.notifyRemoteConnectionClosed();
		else
			client.notifyRemoteConnectionClosed();
	}

	private class ConnectionImpl implements IConnectionImpl {
		private NetworkSimulator network;
		private Semaphore semaphore;
		private byte[] buffer;
		
		public ConnectionImpl(NetworkSimulator network) {
			this.network = network;
			semaphore = new Semaphore(0);
		}

		@Override
		public void sendImpl(byte[] data) throws Exception {
			network.sendToRemote(this, data);
		}

		@Override
		public byte[] receiveImpl(int receivingBufferSize) throws Exception {
			semaphore.acquire();
			return buffer;
		}

		@Override
		public void disposeImpl() {
			network.notifyConnectionClosed(this);
		}
		
		public void notifyReceptionForReception(byte[] data) {
			buffer = data;
			semaphore.release();
		}

		public void notifyRemoteConnectionClosed() {
			buffer = null;
			semaphore.release();
		}
	}
}
