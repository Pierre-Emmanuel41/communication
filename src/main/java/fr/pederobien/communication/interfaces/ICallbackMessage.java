package fr.pederobien.communication.interfaces;

import java.util.function.Consumer;

public interface ICallbackMessage extends IMessage {

	public class CallbackArgs {
		private int identifier;
		private IMessage response;
		private boolean isTimeout;
		
		/**
		 * Creates a callback argument class to execute a callback depending whether or not a response
		 * has been received from the remote.
		 * 
		 * @param identifier The identifier of the response.
		 * @param response The response received from the remote.
		 * @param isTimeout True if the remote did not answer in time, false otherwise.
		 */
		public CallbackArgs(int identifier, IMessage response, boolean isTimeout) {
			this.identifier = identifier;
			this.response = response;
			this.isTimeout = isTimeout;
		}

		/**
		 * @return The identifier of the response.
		 */
		public int getIdentifier() {
			return identifier;
		}

		/**
		 * @return Response message if it has been received before timeout. Otherwise value is null.
		 */
		public IMessage getResponse() {
			return response;
		}

		/**
		 * @return true if timeout happened before reception of the response. In this case Response is null.
		 */
		public boolean isTimeout() {
			return isTimeout;
		}
	}
	
	/**
	 * @return The timeout in milliseconds for this request.
	 */
	long getTimeout();

	/**
	 * @return Callback function to be called when response is received or when timeout occurs.
	 */
	Consumer<CallbackArgs> getCallback();
	
	/**
	 * @return True if this callback message shall be sent synchronously, false to send it asynchronously.
	 */
	boolean isSync();
}
