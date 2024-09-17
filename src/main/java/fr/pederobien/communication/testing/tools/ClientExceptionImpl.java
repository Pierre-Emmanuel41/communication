package fr.pederobien.communication.testing.tools;

import fr.pederobien.communication.impl.Communication;
import fr.pederobien.communication.interfaces.IClientConfig;
import fr.pederobien.communication.interfaces.IClientImpl;
import fr.pederobien.communication.interfaces.IConnection;
import fr.pederobien.communication.interfaces.IConnection.Mode;
import fr.pederobien.communication.interfaces.IConnectionConfig;
import fr.pederobien.communication.interfaces.IConnectionImpl;

public class ClientExceptionImpl implements IClientImpl {
	
	public enum ClientExceptionMode {
		SENDING,
		RECEIVING
	}

	private IConnectionImpl impl;
	private String address;
	private int port;

	/**
	 * Create a client that will throw an exception when the send/ receive method is called.
	 * 
	 * @param mode The exception mode of the client.
	 */
	public ClientExceptionImpl(ClientExceptionMode mode) {
		impl = new ConnectionExceptionImpl(mode);
	}
	
	@Override
	public void connectImpl(String address, int port, int connectionTimeout) throws Exception {
		this.address = address;
		this.port = port;
	}

	@Override
	public IConnection onConnectionComplete(IClientConfig config) {
		// Creates a connection configuration
		IConnectionConfig connectionConfig = Communication.createDefaultConnectionConfig(address, port);

		return Communication.createCustomConnection(connectionConfig, impl, Mode.CLIENT_TO_SERVER);
	}
	
	private class ConnectionExceptionImpl implements IConnectionImpl {
		private ClientExceptionMode mode;
		
		/**
		 * Creates a connection that throws an exception while sending or while receiving.
		 * 
		 * @param exceptionMode The exception mode.
		 */
		public ConnectionExceptionImpl(ClientExceptionMode mode) {
			this.mode = mode;
		}

		@Override
		public void sendImpl(byte[] data) throws Exception {
			if (mode == ClientExceptionMode.SENDING)
				throw new RuntimeException("Exception to test unstable counter");
		}

		@Override
		public byte[] receiveImpl(int receivingBufferSize) throws Exception {
			if (mode == ClientExceptionMode.RECEIVING) {
				Thread.sleep(200);
				throw new RuntimeException("Exception to test unstable counter");
			}
			
			Thread.sleep(100000000);
			return new byte[1024];			
		}

		@Override
		public void disposeImpl() {

		}
	}
}
