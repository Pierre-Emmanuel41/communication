package fr.pederobien.communication.impl.connection;

import fr.pederobien.communication.interfaces.ICallbackMessage;
import fr.pederobien.communication.interfaces.ICallbackMessage.CallbackArgs;
import fr.pederobien.communication.interfaces.IHeaderMessage;
import fr.pederobien.communication.interfaces.IMessage;

public class CallbackManagement implements Runnable {
	private CallbackManager manager;
	private int identifier;
	private ICallbackMessage message;
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
	public CallbackManagement(CallbackManager manager, int identifier, ICallbackMessage message) {
		this.manager = manager;
		this.identifier = identifier;
		this.message = message;
		
		isTimeout = false;
		timeoutThread = new Thread(this, "[Timeout]");
	}

	@Override
	public void run() {
		try {
			Thread.sleep(getMessage().getTimeout());

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
	 * @return The message sent to the remote and waiting for a response.
	 */
	public ICallbackMessage getMessage() {
		return message;
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
		IMessage message = isTimeout ? null : new Message(response.getBytes());
		int identifier = isTimeout ? -1 : response.getID();
		CallbackArgs arguments = new CallbackArgs(identifier, message, isTimeout);
		getMessage().getCallback().accept(arguments);
		return arguments;
	}
}
