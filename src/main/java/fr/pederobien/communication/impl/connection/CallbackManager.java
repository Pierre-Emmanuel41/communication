package fr.pederobien.communication.impl.connection;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import fr.pederobien.communication.interfaces.connection.ICallback.CallbackArgs;
import fr.pederobien.communication.interfaces.connection.IHeaderMessage;
import fr.pederobien.communication.interfaces.connection.IMessage;
import fr.pederobien.utils.Disposable;
import fr.pederobien.utils.IDisposable;

public class CallbackManager {
	private Map<Integer, Monitor> monitors;
	private IDisposable disposable;

	/**
	 * Creates a manager responsible to monitor registered message if a timeout
	 * occurs.
	 * 
	 * @param connection The connection associated to this manager.
	 */
	public CallbackManager() {
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
			monitors.put(identifier, new Monitor(identifier, message));
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
			for (Map.Entry<Integer, Monitor> entry : set) {
				try {
					unregisterAndExec(entry.getKey(), null, true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			monitors.clear();
		}
	}

	/**
	 * Get the callback associated to the requestID of the response. If no pending
	 * request is registered for the requestID then the method returns null. If a
	 * pending request is registered for the requestID It is canceled to avoid a
	 * timeout to occurs.
	 * 
	 * @param header The response that has been received from the remote.
	 */
	public void unregisterAndExecute(IHeaderMessage header) {
		disposable.checkDisposed();
		unregisterAndExec(header.getRequestID(), header, false);
	}

	/**
	 * Find the monitor associated to the given identifier, and if found, unregister
	 * it from the pending queue and execute its callback.
	 * 
	 * @param identifier       The identifier of the pending request.
	 * @param header           The response that has been received from the remote.
	 * @param isConnectionLost True if the connection with the remote has been lost,
	 *                         false otherwise.
	 */
	private void unregisterAndExec(int identifier, IHeaderMessage header, boolean isConnectionLost) {
		Monitor monitor = monitors.remove(identifier);
		if (monitor != null) {
			monitor.stop();
			monitor.dispatch(header, isConnectionLost);
		}
	}

	private class Monitor {
		private int identifier;
		private IMessage request;
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
		}

		/**
		 * Start the underlying thread waiting for a timeout to occur.
		 */
		public void start() {
			monitor.start();
		}

		/**
		 * Interrupt the underlying thread waiting for a timeout to occur.
		 */
		public void stop() {
			monitor.interrupt();
		}

		/**
		 * Execute the callback of the underlying request.
		 * 
		 * @param response         The response received from the remote, or null if
		 *                         timeout occurs.
		 * @param isConnectionLost True if the connection with the remote is lost, false
		 *                         otherwise.
		 */
		public void dispatch(IHeaderMessage response, boolean isConnectionLost) {
			// Considering by default that timeout happened
			int identifier = -1;
			IMessage resp = null;
			boolean isTimeout = true;

			// No timeout happened
			if (response != null) {
				identifier = response.getIdentifier();
				resp = new Message(response.getBytes());
				isTimeout = false;
			}

			request.getCallback().apply(new CallbackArgs(identifier, resp, isTimeout, isConnectionLost));
		}

		/**
		 * Block until a timeout occurs or the response has been received.
		 */
		private void monitor() {
			try {
				Thread.sleep(request.getCallback().getTimeout());

				// Unregister pending request
				unregisterAndExec(identifier, null, false);
			} catch (InterruptedException e) {
				// Do nothing
			}
		}
	}
}
