package fr.pederobien.communication.impl;

import java.util.HashMap;
import java.util.Map;

import fr.pederobien.communication.interfaces.IHeaderMessage;
import fr.pederobien.utils.Disposable;
import fr.pederobien.utils.IDisposable;

public class CallbackMessageManager {
	private Connection connection;
	private Map<Integer, CallbackManagement> pendingMessages;
	private IDisposable disposable;
	
	/**
	 * Creates a manager responsible to monitor registered message if a timeout occurs.
	 * 
	 * @param connection The connection associated to this manager.
	 */
	public CallbackMessageManager(Connection connection) {
		this.connection = connection;
		pendingMessages = new HashMap<Integer, CallbackManagement>();
		disposable = new Disposable();
	}
	
	/**
	 * Register the given message to an internal queue in order to monitor if a timeout occurs.
	 * 
	 * @param message The message to register.
	 */
	public void register(HeaderMessage message) {
		disposable.checkDisposed();

		pendingMessages.put(message.getID(), new CallbackManagement(this, message));
	}
	
	/**
	 * If a message is registered for the given identifier, then a separated thread is started to check
	 * if a timeout occurs while waiting for an answer.
	 * 
	 * @param message The identified message to monitor.
	 */
	public void start(HeaderMessage message) {
		disposable.isDisposed();
		
		CallbackManagement management = pendingMessages.get(message.getID());
		if (management != null) {
			management.start();
		}
	}
	
	/**
	 * Cancel all pending requests, this object cannot be used anymore.
	 */
	public void dispose() {
		if (disposable.dispose()) {
			for (Map.Entry<Integer, CallbackManagement> entry : pendingMessages.entrySet()) {
				entry.getValue().cancel();
			}
			pendingMessages.clear();
		}
	}
	
	/**
	 * Get the callback associated to the requestID of the response. If no pending request is registered
	 * for the requestID then the method returns null. If a pending request is registered for the requestID
	 * It is canceled to avoid a timeout to occurs.
	 * 
	 * @param response The response that has been received from the remote.
	 * 
	 * @return The callback if there is a pending one, null otherwise.
	 */
	public CallbackManagement unregister(IHeaderMessage response) {
		disposable.checkDisposed();

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
}
