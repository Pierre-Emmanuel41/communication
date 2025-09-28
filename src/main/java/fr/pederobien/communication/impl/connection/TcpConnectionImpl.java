package fr.pederobien.communication.impl.connection;

import fr.pederobien.communication.interfaces.connection.IConnectionImpl;
import fr.pederobien.utils.ByteWrapper;

import java.io.IOException;
import java.net.Socket;

public class TcpConnectionImpl implements IConnectionImpl {
	private final Socket socket;

	/**
	 * Creates a connection specific for TCP protocol.
	 *
	 * @param socket The socket to use to send/receive data from the remote.
	 */
	public TcpConnectionImpl(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void send(byte[] data) throws Exception {
		socket.getOutputStream().write(data);
		socket.getOutputStream().flush();
	}

	@Override
	public byte[] receive() throws Exception {
		byte[] buffer = new byte[2048];

		try {
			int read = socket.getInputStream().read(buffer);
			return ByteWrapper.wrap(buffer).extract(0, read);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public void dispose() {
		if (socket == null) {
			return;
		}

		try {
			socket.shutdownInput();
			socket.shutdownOutput();
			socket.close();
		} catch (IOException e) {
			// Do nothing
		}
	}
}
