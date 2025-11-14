package fr.pederobien.communication.impl.connection;

import java.util.function.Consumer;

import fr.pederobien.communication.event.MessageEvent;
import fr.pederobien.communication.interfaces.connection.IHeaderMessage;
import fr.pederobien.utils.BlockingQueueTask;

public class QueueManager {
	private final BlockingQueueTask<IHeaderMessage> sendingQueue;
	private final BlockingQueueTask<Object> receivingQueue;
	private final BlockingQueueTask<byte[]> extractingQueue;
	private final BlockingQueueTask<MessageEvent> dispatchingQueue;
	private final BlockingQueueTask<CallbackResult> callbackQueue;
	private Consumer<IHeaderMessage> onSend;
	private Consumer<Object> onReceive;
	private Consumer<byte[]> onExtract;
	private Consumer<MessageEvent> onDispatch;

	/**
	 * Creates a manager that contains a sending, receiving and extracting queue.
	 */
	public QueueManager() {
		sendingQueue = new BlockingQueueTask<IHeaderMessage>("[Client send]", this::onSend);
		receivingQueue = new BlockingQueueTask<Object>("[Client receive]", this::onReceive);
		extractingQueue = new BlockingQueueTask<byte[]>("[Client extract]", this::onExtract);
		dispatchingQueue = new BlockingQueueTask<MessageEvent>("[Client dispatch]", this::onDispatch);
		callbackQueue = new BlockingQueueTask<CallbackResult>("[Client callback]", CallbackResult::apply);
	}

	/**
	 * Create a sending, receiving and extracting queue in order to send, receive and extract data asynchronously.
	 */
	public void initialize() {

		// Waiting for a message to be sent
		sendingQueue.start();

		// Waiting for receiving message from network
		receivingQueue.start();
		receivingQueue.add(new Object());

		// Waiting for data to extract
		extractingQueue.start();

		// Waiting for data to be dispatched to the client/server
		dispatchingQueue.start();

		// Waiting for callback to be executed
		callbackQueue.start();
	}

	/**
	 * Dispose the underlying sending, receiving and extracting queues. The underlying thread of each queue is interrupted.
	 */
	public void dispose() {
		sendingQueue.dispose();
		receivingQueue.dispose();
		extractingQueue.dispose();
		dispatchingQueue.dispose();
		callbackQueue.dispose();
	}

	/**
	 * @return The queue to send data to the remote.
	 */
	public BlockingQueueTask<IHeaderMessage> getSendingQueue() {
		return sendingQueue;
	}

	/**
	 * Set the code to execute when data should be sent to the remote.
	 *
	 * @param onSend The code to execute to send data to the remote.
	 */
	public void setOnSend(Consumer<IHeaderMessage> onSend) {
		this.onSend = onSend;
	}

	/**
	 * @return The queue to receive data from the remote.
	 */
	public BlockingQueueTask<Object> getReceivingQueue() {
		return receivingQueue;
	}

	/**
	 * Set the code to execute to receive data from the remote.
	 *
	 * @param onReceive The code to execute to send data to the remote.
	 */
	public void setOnReceive(Consumer<Object> onReceive) {
		this.onReceive = onReceive;
	}

	/**
	 * @return The queue to extract data received from the remote.
	 */
	public BlockingQueueTask<byte[]> getExtractingQueue() {
		return extractingQueue;
	}

	/**
	 * Set the code to execute to parse data received from the remote.
	 *
	 * @param onExtract The code to execute to send data to the remote.
	 */
	public void setOnExtract(Consumer<byte[]> onExtract) {
		this.onExtract = onExtract;
	}

	/**
	 * @return The queue to dispatch an unexpected message.
	 */
	public BlockingQueueTask<MessageEvent> getDispatchingQueue() {
		return dispatchingQueue;
	}

	/**
	 * Set how to dispatch an unexpected message.
	 *
	 * @param onDispatch The code to execute to dispatch an unexpected message.
	 */
	public void setOnDispatch(Consumer<MessageEvent> onDispatch) {
		this.onDispatch = onDispatch;
	}

	/**
	 * @return The queue to execute a callback.
	 */
	public BlockingQueueTask<CallbackResult> getCallbackQueue() {
		return callbackQueue;
	}

	private void onSend(IHeaderMessage message) {
		if (onSend != null) {
			onSend.accept(message);
		}
	}

	private void onReceive(Object ignored) {
		if (onReceive != null) {
			onReceive.accept(ignored);
		}
	}

	private void onExtract(byte[] raw) {
		if (onExtract != null) {
			onExtract.accept(raw);
		}
	}

	private void onDispatch(MessageEvent event) {
		if (onDispatch != null) {
			onDispatch.accept(event);
		}
	}
}
