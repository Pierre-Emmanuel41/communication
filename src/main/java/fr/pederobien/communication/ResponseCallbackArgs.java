package fr.pederobien.communication;

import fr.pederobien.communication.interfaces.IRequestMessage;
import fr.pederobien.communication.interfaces.IResponseMessage;

public class ResponseCallbackArgs {
	private IRequestMessage request;
	private IResponseMessage response;
	private boolean timeout;

	public ResponseCallbackArgs(IRequestMessage request, IResponseMessage response, boolean timeout) {
		this.request = request;
		this.response = response;
		this.timeout = timeout;
	}

	/**
	 * @return The initial request message.
	 */
	public IRequestMessage getRequest() {
		return request;
	}

	/**
	 * @return Response message if it has been received before timeout. Otherwise value is null.
	 */
	public IResponseMessage getResponse() {
		return response;
	}

	/**
	 * @return true if timeout happened before reception of the response. In this case Response is null.
	 */
	public boolean isTimeout() {
		return timeout;
	}
}
