package fr.pederobien.communication.interfaces;

import java.net.InetSocketAddress;

import fr.pederobien.communication.EConnectionState;

public interface IConnection {

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
	 * @return True if the connection can send data to the remote or not. It is independent from the connection with the remote.
	 */
	boolean isEnabled();

	/**
	 * Set if this connection can send data to the remote or not. If this connection is not allowed to send data to the remote, then
	 * calling method {@link #send(IRequestMessage)} will not stores the message.
	 * 
	 * @param isEnabled True to allow this connection to send data to remote.
	 */
	void setIsEnabled(boolean isEnabled);

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
	 * The implementation shall send the provided data **asynchronously**. That is to say that the method is not expecting to block
	 * any time. Error sending data may be reported with the event LogEvent.
	 * 
	 * @param message the request to send to the remote.
	 */
	void send(IRequestMessage message);

	/**
	 * Dispose this connection. After this, it is impossible to send data to the remote using this connection.
	 */
	void dispose();

	/**
	 * @return True if the connection is disposed and cannot be used any more.
	 */
	boolean isDisposed();

	/**
	 * Append an observer to this observable object.
	 * 
	 * @param obs The observer to add.
	 */
	void addObserver(IObsConnection obs);

	/**
	 * Remove an observer from this observable object.
	 * 
	 * @param obs The observer to remove.
	 */
	void removeObserver(IObsConnection obs);
}
