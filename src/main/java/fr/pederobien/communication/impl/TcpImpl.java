package fr.pederobien.communication.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import fr.pederobien.communication.EConnectionState;
import fr.pederobien.communication.event.ConnectionLogEvent.ELogLevel;
import fr.pederobien.communication.event.ConnectionLostEvent;
import fr.pederobien.communication.interfaces.IAnswersExtractor;
import fr.pederobien.communication.interfaces.ICallbackRequestMessage;
import fr.pederobien.communication.interfaces.ITcpConnection;
import fr.pederobien.utils.BlockingQueueTask;
import fr.pederobien.utils.ByteWrapper;
import fr.pederobien.utils.event.EventManager;

public abstract class TcpImpl extends ConnectionOperation implements ITcpConnection {
	private Socket socket;
	private boolean isEnable;
	private RequestResponseManager requestResponseManager;
	private BlockingQueueTask<byte[]> extractingQueue;
	private BlockingQueueTask<ICallbackRequestMessage> sendingQueue;

	protected TcpImpl(String address, IAnswersExtractor extractor) {
		isEnable = true;

		requestResponseManager = new RequestResponseManager(this, address, extractor);
		sendingQueue = new BlockingQueueTask<ICallbackRequestMessage>("Sending_".concat(address), message -> startSending(message));
		extractingQueue = new BlockingQueueTask<byte[]>("Extracting_".concat(address), answer -> startExtracting(answer));
	}

	@Override
	public InetSocketAddress getAddress() {
		return socket == null ? null : (InetSocketAddress) socket.getRemoteSocketAddress();
	}

	@Override
	public boolean isEnable() {
		return isEnable;
	}

	@Override
	public void setEnable(boolean isEnable) {
		this.isEnable = isEnable;
	}

	@Override
	public void send(ICallbackRequestMessage message) {
		checkDisposed();

		if (isEnable() && isState(EConnectionState.CONNECTED))
			sendingQueue.add(message);
	}

	/**
	 * @return The queue that extracts answers from a raw bytes array.
	 */
	protected BlockingQueueTask<byte[]> getExtractingQueue() {
		return extractingQueue;
	}

	/**
	 * @return The queue that sends data to the remote.
	 */
	protected BlockingQueueTask<ICallbackRequestMessage> getSendingQueue() {
		return sendingQueue;
	}

	/**
	 * @return The response manager that stores pending requests.
	 */
	protected RequestResponseManager getRequestResponseManager() {
		return requestResponseManager;
	}

	protected void startReceiving() {
		while (isState(EConnectionState.CONNECTED)) {
			try {
				byte[] buffer = new byte[1024];
				int read = socket.getInputStream().read(buffer);

				if (read == -1) {
					onConnectionLost();
					break;
				}
				extractingQueue.add(ByteWrapper.wrap(buffer).extract(0, read));
			} catch (IOException e) {
				if (isState(EConnectionState.DISCONNECTING) || isState(EConnectionState.DISCONNECTED))
					return;

				onLogEvent(this, ELogLevel.WARNING, "Reception failure : " + e.getMessage(), e);
				onConnectionLost();
				break;
			}
		}
	}

	protected void onConnectionLost() {
		setState(EConnectionState.CONNECTION_LOST);
		onLogEvent(this, ELogLevel.INFO, "Connection lost");

		closeSocket();

		EventManager.callEvent(new ConnectionLostEvent(this));
	}

	/**
	 * @return The socket used to send data through the network.
	 */
	protected Socket getSocket() {
		return socket;
	}

	/**
	 * Set the socket associated to this TCP connection.
	 * 
	 * @param socket The socket used to send data through the network.
	 */
	protected void setSocket(Socket socket) {
		this.socket = socket;
	}

	protected void closeSocket() {
		try {
			if (socket == null)
				return;

			socket.shutdownInput();
			socket.shutdownOutput();
			socket.close();
		} catch (IOException e) {
			// Do nothing
		}
	}

	private void startExtracting(byte[] answer) {
		// publish message
		onDataReceivedEvent(this, answer, answer.length);

		try {
			requestResponseManager.handleResponse(answer);
		} catch (UnsupportedOperationException e) {
			onLogEvent(this, ELogLevel.ERROR, "Exception when retrieving answer : " + e.getMessage(), e);
		}
	}

	private void startSending(ICallbackRequestMessage message) {
		try {
			requestResponseManager.addRequest(message);
			socket.getOutputStream().write(message.getBytes());
			socket.getOutputStream().flush();
		} catch (SocketException e) {
			onLogEvent(this, ELogLevel.WARNING, "Send failure : " + e.getMessage(), e);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
