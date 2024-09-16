package fr.pederobien.communication.impl.connection;

import fr.pederobien.communication.interfaces.ICallback.CallbackArgs;
import fr.pederobien.communication.interfaces.IHeaderMessage;
import fr.pederobien.communication.interfaces.IMessage;

public class CallbackManagement implements Runnable {
	private CallbackManager manager;
	private int identifier;
	private IMessage message;
	private IHeaderMessage response;
	private Thread timeoutThread;
	private boolean isTimeout;
	
	/**
	 * Creates a callback management to monitor if a response has been received for a request.
	 * 
	 * @param manager The manager that monitor all requests waiting for a response from the remote.
	 * @param identifier The identifier of the message to monitor.
	 * @param message The message waiting for a response.
	 */
	public CallbackManagement(CallbackManager manager, int identifier, IMessage message) {
		this.manager = manager;
		this.identifier = identifier;
		this.message = message;
		
		isTimeout = false;
		timeoutThread = new Thread(this, "[Timeout]");
	}

	@Override
	public void run() {
		try {
			Thread.sleep(message.getCallback().getTimeout());

			isTimeout = true;
			
			/* Timeout, need to remove the pending message */
			manager.timeout(this);
		} catch (InterruptedException e) {
			/* Do nothing */
		}
	}
	
	/**
	 * Interrupt the underlying thread waiting for a timeout to occur.
	 */
	public void cancel() {
		timeoutThread.interrupt();
	}
	
	/**
	 * Start the underlying thread waiting for a timeout to occur.
	 */
	public void start() {
		timeoutThread.start();
	}
	
	/**
	 * @return The identifier of the message.
	 */
	public int getIdentifier() {
		return identifier;
	}
	
	/**
	 * Set the response of the message.
	 * 
	 * @param response The response received from the remote.
	 */
	public void setResponse(IHeaderMessage response) {
		this.response = response;
	}
	
	/**
	 * Execute the callback of the underlying message.
	 * 
	 * @return The callback arguments to see if a request should be sent back to the remote as response for its response.
	 */
	public CallbackArgs apply() {
		IMessage resp = isTimeout ? null : new Message(response.getBytes());
		int identifier = isTimeout ? -1 : response.getIdentifier();

		CallbackArgs arguments = new CallbackArgs(identifier, resp, isTimeout);
		message.getCallback().apply(arguments);
		return arguments;
	}
}
