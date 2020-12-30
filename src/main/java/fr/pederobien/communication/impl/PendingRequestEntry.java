package fr.pederobien.communication.impl;

import java.util.Timer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import fr.pederobien.communication.ResponseCallbackArgs;
import fr.pederobien.communication.impl.RequestResponseManager.TimeoutCallback;
import fr.pederobien.communication.interfaces.IRequestMessage;

public class PendingRequestEntry {
	private IRequestMessage request;
	private Consumer<ResponseCallbackArgs> callback;
	private int requestOrder;
	private Timer timeoutTimer;
	private AtomicBoolean disposed;

	public PendingRequestEntry(TimeoutCallback timeoutCallback, IRequestMessage request, int requestOrder, Consumer<ResponseCallbackArgs> callback, long timeout) {
		this.request = request;
		this.requestOrder = requestOrder;
		this.callback = callback;

		timeoutCallback.setEntry(this);
		disposed = new AtomicBoolean(false);

		timeoutTimer = new Timer();
		timeoutTimer.schedule(timeoutCallback, timeout);
	}

	public void decrementRequestOrder() {
		requestOrder--;
	}

	public void dispose() {
		if (disposed.compareAndSet(false, true))
			return;

		timeoutTimer.cancel();
	}

	/**
	 * @return Get the request corresponding to the entry.
	 */
	public IRequestMessage getRequest() {
		return request;
	}

	/**
	 * @return Get the callback that shall be called each time a response is received.
	 */
	public Consumer<ResponseCallbackArgs> getCallback() {
		return callback;
	}

	/**
	 * @return Get the order of the request to differentiate between parallel requests.
	 */
	public int getRequestOrder() {
		return requestOrder;
	}
}
