package fr.pederobien.communication.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import fr.pederobien.communication.interfaces.IHeaderMessage;

public class CallbackMessageManager {
	private Connection connection;
	private Map<Integer, CallbackManagement> pendingMessages;
	private AtomicBoolean isDisposed;
	
	/**
	 * Creates a manager responsible to monitor registered message if a timeout occurs.
	 * 
	 * @param connection The connection associated to this manager.
	 */
	public CallbackMessageManager(Connection connection) {
		this.connection = connection;
		pendingMessages = new HashMap<Integer, CallbackManagement>();
		isDisposed = new AtomicBoolean(false);
	}
	
	/**
	 * Register the given message to an internal queue in order to monitor if a timeout occurs.
	 * 
	 * @param message The message to register.
	 */
	public void register(HeaderMessage message) {
		checkDisposed();

		pendingMessages.put(message.getID(), new CallbackManagement(this, message));
	}
	
	/**
	 * If a message is registered for the given identifier, then a separated thread is started to check
	 * if a timeout occurs while waiting for an answer.
	 * 
	 * @param message The identified message to monitor.
	 */
	public void start(HeaderMessage message) {
		checkDisposed();
		
		CallbackManagement management = pendingMessages.get(message.getID());
		if (management != null) {
			management.start();
		}
	}
	
	/**
	 * Cancel all pending requests, this object cannot be used anymore.
	 */
	public void dispose() {
		if (isDisposed.compareAndSet(false, true)) {
			for (Map.Entry<Integer, CallbackManagement> entry : pendingMessages.entrySet()) {
				entry.getValue().cancel();
			}
			pendingMessages.clear();
		}
	}
	
	/**
	 * From the input map, extract the registered message. If a message was registered, it execute the callback
	 * in a dedicated thread.
	 * 
	 * @param response The response that has been received from the remote.
	 * 
	 * @return True if there is a pending request registered for the given identifier, false otherwise.
	 */
	public CallbackManagement unregister(IHeaderMessage response) {
		checkDisposed();

		CallbackManagement management = pendingMessages.remove(response.getRequestID());
		if (management != null) {
			management.cancel();
			management.setResponse(response);
		}
		return management;
	}
	
	/**
	 * Unregister the given callback management from this manager.
	 * 
	 * @param management The management for which a timeout occurred.
	 */
	public void timeout(CallbackManagement management) {
		pendingMessages.remove(management.getID());
		connection.timeout(management);
	}
	
	/**
	 * Throws an {@link IllegalStateException} if the connection is disposed. Do nothing otherwise.
	 */
	protected void checkDisposed() {
		if (isDisposed.get())
			throw new IllegalStateException("Object disposed");
	}
}
