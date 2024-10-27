package fr.pederobien.communication.impl.connection;

import java.io.IOException;
import java.net.Socket;

import fr.pederobien.communication.interfaces.connection.IConnectionImpl;
import fr.pederobien.utils.ByteWrapper;

public class TcpConnectionImpl implements IConnectionImpl {
	private Socket socket;
	
	/**
	 * Creates a connection specific for TCP protocol.
	 * 
	 * @param socket The socket to use to send/receive data from the remote.
	 */
	public TcpConnectionImpl(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void sendImpl(byte[] data) throws Exception {
		socket.getOutputStream().write(data);
		socket.getOutputStream().flush();
	}

	@Override
	public byte[] receiveImpl(int receivingBufferSize) throws Exception {
		byte[] buffer = new byte[receivingBufferSize];
		int read = socket.getInputStream().read(buffer);
		
		return read == -1 ? null : ByteWrapper.wrap(buffer).extract(0, read);
	}
	
	@Override
	public void disposeImpl() {
		if (socket == null)
			return;

		try {
			socket.shutdownInput();
			socket.shutdownOutput();
			socket.close();
		} catch (IOException e) {
			// Do nothing
		}
	}
}
