package fr.pederobien.communication.interfaces;

import java.net.InetSocketAddress;

import fr.pederobien.communication.EConnectionState;

public interface IConnectionOperation {

	/**
	 * Returns the address to which the connection is connected.
	 * <p>
	 * If the connection was connected prior to being {@link #dispose() disposed}, then this method will continue to return the
	 * connected address after the connection is disposed.
	 *
	 * @return the remote IP address to which this connected is connected, or {@code null} if the socket is not connected.
	 */
	InetSocketAddress getAddress();

	/**
	 * @return The current connection state.
	 */
	EConnectionState getState();

	/**
	 * The implementation shall try establishing the connection only when this method is called. The class is expected to retry
	 * establishing the connection as long as Disconnected() is not called. Timeout may be reported in event LogEvent.
	 */
	void connect();

	/**
	 * Abort the connection to the remote.
	 */
	void disconnect();

	/**
	 * Dispose this connection. After this, it is impossible to send data to the remote using this connection.
	 */
	void dispose();

	/**
	 * @return True if the connection is disposed and cannot be used any more.
	 */
	boolean isDisposed();
}
