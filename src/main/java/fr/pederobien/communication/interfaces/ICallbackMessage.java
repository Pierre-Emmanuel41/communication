package fr.pederobien.communication.interfaces;

import java.util.function.Consumer;

public interface ICallbackMessage extends IMessage {

	public class CallbackArgs {
		private IMessage response;
		private boolean isTimeout;
		private IMessage simpleRequest;
		private ICallbackMessage callbackRequest;
		
		/**
		 * Creates a callback argument class to execute a callback depending whether or not a response
		 * has been received from the remote.
		 * 
		 * @param response The response received from the remote.
		 * @param isTimeout True if the remote did not answer in time, false otherwise.
		 */
		public CallbackArgs(IMessage response, boolean isTimeout) {
			this.response = response;
			this.isTimeout = isTimeout;
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
		
		/**
		 * @return The request, which is a response of the received response. Can be null if no response is sent back to the remote.
		 */
		public IMessage getSimpleRequest() {
			return simpleRequest;
		}
		
		/**
		 * Set the request to send back to the remote.
		 * 
		 * @param simpleRequest The request, which is a response of the received response.
		 *                       Can be null if no response is sent back to the remote.
		 */
		public void setSimpleRequest(IMessage simpleRequest) {
			this.simpleRequest = simpleRequest;
		}
		
		/**
		 * @return The request, which is a response of the received response. Can be null if no response is sent back to the remote.
		 */
		public ICallbackMessage getCallbackRequest() {
			return callbackRequest;
		}
		
		/**
		 * Set the request to send back to the remote. The message will be internally monitored and the callback will
		 * be executed if a response from the remote has been received of if a timeout occurred.
		 * 
		 * @param callbackRequest The response to send to the remote.
		 */
		public void setCallbackRequest(ICallbackMessage callbackRequest) {
			this.callbackRequest = callbackRequest;
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
