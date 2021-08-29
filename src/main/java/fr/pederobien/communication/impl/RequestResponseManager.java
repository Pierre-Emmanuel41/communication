package fr.pederobien.communication.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import fr.pederobien.communication.ResponseCallbackArgs;
import fr.pederobien.communication.event.UnexpectedDataReceivedEvent;
import fr.pederobien.communication.interfaces.IAnswersExtractor;
import fr.pederobien.communication.interfaces.ICallbackRequestMessage;
import fr.pederobien.communication.interfaces.IResponseMessage;
import fr.pederobien.utils.BlockingQueueTask;

public class RequestResponseManager {

	private class CallbackManagement {
		private Consumer<ResponseCallbackArgs> callback;
		private PendingRequestEntry entry;
		private IResponseMessage response;
		private boolean timeout;

		public CallbackManagement(Consumer<ResponseCallbackArgs> callback, PendingRequestEntry entry, IResponseMessage response, boolean timeout) {
			this.callback = callback;
			this.entry = entry;
			this.response = response;
			this.timeout = timeout;
		}
	}

	public class TimeoutCallback extends TimerTask {
		private PendingRequestEntry entry;

		public void setEntry(PendingRequestEntry entry) {
			this.entry = entry;
		}

		@Override
		public void run() {
			timeoutCallback(entry);
		}
	}

	private BlockingQueueTask<CallbackManagement> callbackQueue;
	private BlockingQueueTask<Map.Entry<Integer, byte[]>> unexpectedDataReceivedQueue;
	private Object lock;
	private Map<String, PendingRequestEntry> pendingRequest;
	private IAnswersExtractor answersExtractors;
	private AtomicBoolean disposed;
	private TcpClientConnection connection;

	public RequestResponseManager(TcpClientConnection connection, String remoteAddress, IAnswersExtractor answersExtractor) {
		this.connection = connection;
		this.answersExtractors = answersExtractor;

		lock = new Object();

		disposed = new AtomicBoolean(false);
		pendingRequest = new HashMap<String, PendingRequestEntry>();

		callbackQueue = new BlockingQueueTask<>("Callback_".concat(remoteAddress), callback -> startCallBack(callback));
		callbackQueue.start();

		unexpectedDataReceivedQueue = new BlockingQueueTask<>("UnexpectedData_".concat(remoteAddress), entry -> startUnexpectedDataReceived(entry));
		unexpectedDataReceivedQueue.start();
	}

	public void dispose() {
		if (!disposed.compareAndSet(false, true))
			return;

		for (PendingRequestEntry entry : pendingRequest.values())
			entry.dispose();

		pendingRequest.clear();
		callbackQueue.dispose();
		unexpectedDataReceivedQueue.dispose();
	}

	/**
	 * Add a request to the list of pending requests, with the specified callback, and the specified timeout.
	 * 
	 * @param request The request message to add.
	 */
	public void addRequest(ICallbackRequestMessage request) {
		checkIsDisposed();

		int requestOrder = 0;

		synchronized (lock) {
			while (pendingRequest.containsKey(generateKey(request.getUniqueIdentifier(), requestOrder))) {
				requestOrder++;
			}

			String key = generateKey(request.getUniqueIdentifier(), requestOrder);
			pendingRequest.put(key, new PendingRequestEntry(new TimeoutCallback(), request, requestOrder, request.getCallback(), request.getTimeout()));
		}
	}

	/**
	 * Handle a response message: invoke callback and remove request from pending requests.
	 * 
	 * @param received The byte array received from the remote.
	 */
	public void handleResponse(byte[] received) {
		checkIsDisposed();

		synchronized (lock) {
			Map<Integer, byte[]> identifiers = answersExtractors.extract(received);
			Map<Integer, byte[]> notExpected = new HashMap<Integer, byte[]>();

			for (Map.Entry<Integer, byte[]> identifier : identifiers.entrySet()) {
				String key = generateKey(identifier.getKey(), 0);
				PendingRequestEntry entry = pendingRequest.get(key);
				if (entry != null)
					handleSingleResponse(entry, key, new ResponseMessage(identifier.getKey(), identifier.getValue()));
				else
					notExpected.put(identifier.getKey(), identifier.getValue());
			}

			if (!notExpected.isEmpty())
				for (Map.Entry<Integer, byte[]> entry : notExpected.entrySet())
					unexpectedDataReceivedQueue.add(entry);
		}
	}

	/**
	 * Generate the key of the dictionary from properties of the interface.
	 * 
	 * @param uniqueIdentifier The unique identifier of the message.
	 * @param requestOrder     The ordering number of the request to identify parallel requests.
	 * 
	 * @return The generated key.
	 */
	private String generateKey(int uniqueIdentifier, int requestOrder) {
		return String.format("%s.%s", uniqueIdentifier, requestOrder);
	}

	/**
	 * Called each time a request times out. Timeout is invoked by passing a PendingRequestEntry as target object.
	 * 
	 * @param target The entry thats times out.
	 */
	private void timeoutCallback(PendingRequestEntry target) {
		PendingRequestEntry entry = null;
		Consumer<ResponseCallbackArgs> callback = null;

		synchronized (lock) {
			String key = generateKey(target.getRequest().getUniqueIdentifier(), target.getRequestOrder());
			entry = pendingRequest.get(key);
			if (entry != null) {
				callback = entry.getCallback();
				pendingRequest.remove(key);
				entry.dispose();
				reorderRemaingKeys(entry.getRequest().getUniqueIdentifier(), entry.getRequestOrder());
			}
		}

		if (callback != null)
			callbackQueue.add(new CallbackManagement(callback, entry, null, true));
	}

	private void handleSingleResponse(PendingRequestEntry entry, String key, IResponseMessage response) {
		pendingRequest.remove(key);
		entry.dispose();
		reorderRemaingKeys(response.getRequestIdentifier(), 0);

		if (entry.getCallback() != null && entry.getRequest() != null)
			callbackQueue.add(new CallbackManagement(entry.getRequest().getCallback(), entry, response, false));
	}

	/**
	 * Reorder the remaining keys following the handling of a response.
	 * 
	 * @param uniqueIdentifier The unique identifier of the keys to be reordered.
	 * @param removedOrder     The request order of the removed entry.
	 */
	private void reorderRemaingKeys(int uniqueIdentifier, int removedOrder) {
		int requestOrder = removedOrder + 1;
		String remainingKey = generateKey(uniqueIdentifier, requestOrder);
		String newKey = generateKey(uniqueIdentifier, removedOrder);

		while (pendingRequest.containsKey(remainingKey)) {
			PendingRequestEntry entry = pendingRequest.get(remainingKey);

			// Update request order
			entry.decrementRequestOrder();

			// Update key
			pendingRequest.put(newKey, entry);
			pendingRequest.remove(remainingKey);

			// Next entry
			newKey = remainingKey;
			requestOrder++;
			remainingKey = generateKey(uniqueIdentifier, requestOrder);
		}
	}

	private void startCallBack(CallbackManagement callback) {
		callback.callback.accept(new ResponseCallbackArgs(callback.entry.getRequest(), callback.response, callback.timeout));
	}

	private void startUnexpectedDataReceived(Map.Entry<Integer, byte[]> entry) {
		connection.getObservers().notifyObservers(obs -> obs.onUnexpectedDataReceived(new UnexpectedDataReceivedEvent(connection, entry.getKey(), entry.getValue())));
	}

	private boolean isDisposed() {
		return disposed.get();
	}

	private void checkIsDisposed() {
		if (isDisposed())
			throw new UnsupportedOperationException("Object disposed");
	}
}
