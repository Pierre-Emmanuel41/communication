package fr.pederobien.communication.impl.connection;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import fr.pederobien.communication.interfaces.connection.ICallback.CallbackArgs;
import fr.pederobien.communication.interfaces.connection.IHeaderMessage;
import fr.pederobien.communication.interfaces.connection.IMessage;
import fr.pederobien.utils.Disposable;
import fr.pederobien.utils.HealedCounter;
import fr.pederobien.utils.IDisposable;

public class CallbackManager {
	private Map<Integer, Monitor> monitors;
	private IDisposable disposable;
	private QueueManager queueManager;
	private HealedCounter counter;

	/**
	 * Creates a manager responsible to monitor registered message if a timeout
	 * occurs.
	 * 
	 * @param queueManager The manager whose the callback queue is used.
	 * @param counter      The counter to increment if an exception occurred while
	 *                     executing the callback
	 */
	public CallbackManager(QueueManager queueManager, HealedCounter counter) {
		this.queueManager = queueManager;
		this.counter = counter;

		monitors = new HashMap<Integer, Monitor>();
		disposable = new Disposable();
	}

	/**
	 * Register the given message to an internal queue in order to monitor if a
	 * timeout occurs.
	 * 
	 * @param identifier The message identifier of the message.
	 * @param message    The message to register.
	 */
	public void register(int identifier, IMessage message) {
		disposable.checkDisposed();

		// When no timeout defined, no callback defined.
		if (message.getCallback().getTimeout() > 0) {
			synchronized (disposable) {
				monitors.put(identifier, new Monitor(identifier, message));
			}
		}
	}

	/**
	 * If a message is registered for the given identifier, then a separated thread
	 * is started to check if a timeout occurs while waiting for an answer.
	 * 
	 * @param identifier The identifier of the message to monitor.
	 */
	public void start(int identifier) {
		disposable.checkDisposed();

		Monitor monitor = monitors.get(identifier);
		if (monitor != null) {
			monitor.start();
		}
	}

	/**
	 * Cancel all pending requests, this object cannot be used anymore.
	 */
	public void dispose() {
		if (disposable.dispose()) {
			Set<Entry<Integer, Monitor>> set = monitors.entrySet();
			set.forEach(entry -> entry.getValue().onConnectionLost());
		}
	}

	/**
	 * Get the callback associated to the requestID of the response. If no pending
	 * request is registered for the requestID then the method returns null. If a
	 * pending request is registered for the requestID it is cancelled to avoid a
	 * timeout to occurs.
	 */
	public void unregisterAndExecute(IHeaderMessage header) {
		disposable.checkDisposed();

		Monitor monitor = null;
		synchronized (disposable) {
			monitor = monitors.get(header.getRequestID());
		}

		if (monitor != null)
			monitor.onResponseReceived(header);
	}

	/**
	 * Removes the monitor associated to the given identifier.
	 * 
	 * @param identifier The identifier of the monitored request.
	 */
	private void unregister(int identifier) {
		synchronized (disposable) {
			monitors.remove(identifier);
		}
	}

	private class Monitor {
		private int identifier;
		private IMessage request;
		private IHeaderMessage response;
		private boolean isConnectionLost;
		private Semaphore semaphore;
		private Thread monitor;

		/**
		 * Creates a monitor to handle timeout.
		 * 
		 * @param identifier The identifier of the request.
		 * @param request    The request sent to the remote and waiting for a response.
		 */
		private Monitor(int identifier, IMessage request) {
			this.identifier = identifier;
			this.request = request;

			monitor = new Thread(() -> monitor(), String.format("[%s Timeout monitor]", identifier));
			semaphore = new Semaphore(0);
		}

		/**
		 * Start the underlying thread waiting for a timeout to occur.
		 */
		public void start() {
			monitor.start();
		}

		/**
		 * Notify this monitor that a response has been received from the remote.
		 * 
		 * @param response The message received from the remote.
		 */
		public void onResponseReceived(IHeaderMessage response) {
			this.response = response;

			isConnectionLost = false;
			semaphore.release();
		}

		/**
		 * Notify this monitor that the connection with the remote has been lost.
		 */
		public void onConnectionLost() {
			response = null;
			isConnectionLost = true;

			semaphore.release();
		}

		/**
		 * Block until a timeout occurs or the response has been received.
		 */
		private void monitor() {
			try {
				semaphore.tryAcquire(request.getCallback().getTimeout(), TimeUnit.MILLISECONDS);

				// Removing this monitor from the monitors map
				unregister(identifier);

				dispatch();
			} catch (InterruptedException e) {
				// Do nothing
			}
		}

		/**
		 * Execute the callback of the underlying request.
		 */
		private void dispatch() {

			// Considering by default that timeout happened
			int identifier = -1;
			byte[] resp = null;
			boolean isTimeout = true;

			// No timeout happened
			if (response != null) {
				identifier = response.getIdentifier();
				resp = response.getBytes();
				isTimeout = false;
			}

			CallbackArgs args = new CallbackArgs(identifier, resp, isTimeout, isConnectionLost);
			CallbackResult result = new CallbackResult(counter, request.getCallback(), args);
			queueManager.getCallbackQueue().add(result);
		}
	}
}
