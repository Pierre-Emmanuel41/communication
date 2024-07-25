package fr.pederobien.communication.event;

import fr.pederobien.communication.interfaces.ICallbackMessage;
import fr.pederobien.communication.interfaces.IConnection;
import fr.pederobien.communication.interfaces.IMessage;

public class RequestReceivedEvent extends DataEvent {
	private IMessage simpleResponse;
	private ICallbackMessage callbackResponse;

	/**
	 * Creates a request received event. It is possible to set a response to the request.
	 * 
	 * @param connection The connection on which the request has been received.
	 * @param address    The address from which the data has been received.
	 * @param port       The port number of the remote.
	 * @param request The request received from the remote.
	 */
	public RequestReceivedEvent(IConnection connection, String address, int port, byte[] request) {
		super(connection, address, port, request);
	}
	
	/**
	 * @return The response of the received message. Can be null if no response is sent back to the remote.
	 */
	public IMessage getSimpleResponse() {
		return simpleResponse;
	}
	
	/**
	 * Set the response to send to the remote.
	 * 
	 * @param simpleResponse The response to send to the remote.
	 */
	public void setSimpleResponse(IMessage simpleResponse) {
		this.simpleResponse = simpleResponse;
	}
	
	/**
	 * @return The response of the received message. Can be null if no response is sent back to the remote.
	 */
	public ICallbackMessage getCallbackResponse() {
		return callbackResponse;
	}
	
	/**
	 * Set the response to send to the remote. The message will be internally monitored and the callback will
	 * be executed if a response from the remote has been received of if a timeout occurred.
	 * 
	 * @param callbackResponse The response to send to the remote.
	 */
	public void setCallbackResponse(ICallbackMessage callbackResponse) {
		this.callbackResponse = callbackResponse;
	}
}
