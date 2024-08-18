package fr.pederobien.communication.impl.connection;

import fr.pederobien.communication.interfaces.ICallbackMessage;
import fr.pederobien.communication.interfaces.ICallbackMessage.CallbackArgs;
import fr.pederobien.communication.interfaces.IHeaderMessage;
import fr.pederobien.communication.interfaces.IMessage;

public class CallbackManagement implements Runnable {
	private CallbackManager manager;
	private HeaderMessage request;
	private IHeaderMessage response;
	private Thread timeoutThread;
	private boolean isTimeout;
	
	/**
	 * Creates a callback management to monitor if a response has been received for a request.
	 * 
	 * @param manager The manager that monitor all requests waiting for a response from the remote.
	 * @param request The request waiting for a response.
	 */
	public CallbackManagement(CallbackManager manager, HeaderMessage request) {
		this.manager = manager;
		this.request = request;
		
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
	public int getID() {
		return request.getID();
	}
	
	/**
	 * @return The message sent to the remote and waiting for a response.
	 */
	public ICallbackMessage getMessage() {
		return (ICallbackMessage) request.getMessage();
	}
	
	/**
	 * @return The response of the message, if a response has been received.
	 */
	public IHeaderMessage getResponse() {
		return response;
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
		IMessage response = isTimeout ? null : new Message(getResponse().getBytes());
		CallbackArgs arguments = new CallbackArgs(response, isTimeout);
		getMessage().getCallback().accept(arguments);
		return arguments;
	}
}
