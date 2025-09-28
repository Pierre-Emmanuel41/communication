package fr.pederobien.communication.impl.connection;

import fr.pederobien.communication.interfaces.connection.ICallback;
import fr.pederobien.communication.interfaces.connection.ICallback.CallbackArgs;
import fr.pederobien.utils.HealedCounter;

public class CallbackResult {
	private final ICallback callback;
	private final CallbackArgs args;
	private final HealedCounter counter;

	/**
	 * Creates a callback result.
	 *
	 * @param counter  The counter to increment if an exception occurred.
	 * @param callback The callback to execute.
	 * @param args     The callback arguments.
	 */
	public CallbackResult(HealedCounter counter, ICallback callback, CallbackArgs args) {
		this.counter = counter;
		this.callback = callback;
		this.args = args;
	}

	/**
	 * Run the callback.
	 */
	public void apply() {
		try {
			callback.apply(args);
		} catch (Exception e) {
			counter.increment();
		}
	}
}
