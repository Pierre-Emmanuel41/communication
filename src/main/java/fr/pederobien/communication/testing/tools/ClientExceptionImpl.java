package fr.pederobien.communication.testing.tools;

import fr.pederobien.communication.impl.Communication;
import fr.pederobien.communication.interfaces.IEthernetEndPoint;
import fr.pederobien.communication.interfaces.client.IClientConfig;
import fr.pederobien.communication.interfaces.client.IClientImpl;
import fr.pederobien.communication.interfaces.connection.IConnection;
import fr.pederobien.communication.interfaces.connection.IConnectionImpl;

public class ClientExceptionImpl implements IClientImpl<IEthernetEndPoint> {

	public enum ClientExceptionMode {
		SENDING, RECEIVING
	}

	private IConnectionImpl impl;

	/**
	 * Create a client that will throw an exception when the send/ receive method is
	 * called.
	 * 
	 * @param mode The exception mode of the client.
	 */
	public ClientExceptionImpl(ClientExceptionMode mode) {
		impl = new ConnectionExceptionImpl(mode);
	}

	@Override
	public IConnection connect(IClientConfig<IEthernetEndPoint> config) throws Exception {
		return Communication.createConnection(config, config.getEndPoint(), impl);
	}

	private class ConnectionExceptionImpl implements IConnectionImpl {
		private ClientExceptionMode mode;

		/**
		 * Creates a connection that throws an exception while sending or while
		 * receiving.
		 * 
		 * @param exceptionMode The exception mode.
		 */
		public ConnectionExceptionImpl(ClientExceptionMode mode) {
			this.mode = mode;
		}

		@Override
		public void send(byte[] data) throws Exception {
			if (mode == ClientExceptionMode.SENDING) {
				throw new RuntimeException("Exception to test unstable counter");
			}
		}

		@Override
		public byte[] receive() throws Exception {
			if (mode == ClientExceptionMode.RECEIVING) {
				Thread.sleep(200);
				throw new RuntimeException("Exception to test unstable counter");
			}

			Thread.sleep(100000000);
			return new byte[1024];
		}

		@Override
		public void dispose() {

		}
	}
}
