package fr.pederobien.communication.interfaces;

public interface IConnection {
	
	public enum Mode {
		CLIENT_TO_SERVER,
		SERVER_TO_CLIENT
	}
	
	/**
	 * First step for initializing this connection.
	 * This method is called before trying to send a message.
	 * 
	 * @return True if this connection is successfully initialized, false otherwise.
	 */
	boolean initialise() throws Exception;
	
	/**
	 * Send a message synchronously and block until a response has been received. The asynchronous send/receive are on suspended.
	 * 
	 * @param message The message to send synchronously to the remote.
	 */
	void sendSync(ICallbackMessage message);
	
	/**
	 * The implementation shall send the provided data **asynchronously**. That is to say that the method is not expecting to block
	 * any time. Error sending data may be reported with the event LogEvent.
	 * 
	 * @param message the message to send to the remote.
	 */
	void send(IMessage message);
	
	/**
	 * The implementation shall send the provided data **asynchronously**. That is to say that the method is not expecting to block
	 * any time. Error sending data may be reported with the event LogEvent.
	 * 
	 * @param message the message to send to the remote.
	 */
	void send(ICallbackMessage message);
	
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
	void setEnabled(boolean isEnabled);
	
	/**
	 * Close this connection definitely. It cannot be used anymore.
	 */
	void dispose();
	
	/**
	 * @return true if this connection has been disposed and cannot be used anymore, false otherwise.
	 */
	boolean isDisposed();

	/**
	 * @return The mode of the connection, it can be from client to server of from server to client.
	 */
	Mode getMode();
}
