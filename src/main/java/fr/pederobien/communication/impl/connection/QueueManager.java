package fr.pederobien.communication.impl.connection;

import java.util.function.Consumer;

import fr.pederobien.communication.interfaces.connection.IHeaderMessage;
import fr.pederobien.utils.BlockingQueueTask;

public class QueueManager {
	private BlockingQueueTask<IHeaderMessage> sendingQueue;
	private Consumer<IHeaderMessage> onSend;
	private BlockingQueueTask<Object> receivingQueue;
	private Consumer<Object> onReceive;
	private BlockingQueueTask<byte[]> extractingQueue;
	private Consumer<byte[]> onExtract;
	private BlockingQueueTask<Runnable> dispatchQueue;
	private BlockingQueueTask<CallbackResult> callbackQueue;

	/**
	 * Creates a manager that contains a sending, receiving and extracting queue.
	 * 
	 * @param name The connection name.
	 */
	public QueueManager(String name) {
		String queueName = String.format("[%s send]", name);
		sendingQueue = new BlockingQueueTask<IHeaderMessage>(queueName, message -> onSend(message));

		queueName = String.format("[%s receive]", name);
		receivingQueue = new BlockingQueueTask<Object>(queueName, ignored -> onReceive(ignored));

		queueName = String.format("[%s extract]", name);
		extractingQueue = new BlockingQueueTask<byte[]>(queueName, raw -> onExtract(raw));

		queueName = String.format("[%s dispatch]", name);
		dispatchQueue = new BlockingQueueTask<Runnable>(queueName, runnable -> runnable.run());

		queueName = String.format("[%s callback]", name);
		callbackQueue = new BlockingQueueTask<CallbackResult>(queueName, callback -> callback.apply());
	}

	/**
	 * Create a sending, receiving and extracting queue in order to send, receive
	 * and extract data asynchronously.
	 * 
	 * @param onSend    The code to execute to send data asynchronously.
	 * @param onReceive The code to execute when data has been received.
	 * @param onExtract The code to execute to extract data received from the
	 *                  remote.
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
		dispatchQueue.start();

		// Waiting for callback to be executed
		callbackQueue.start();
	}

	/**
	 * Dispose the underlying sending, receiving and extracting queues. The
	 * underlying thread of each queue is interrupted.
	 */
	public void dispose() {
		sendingQueue.dispose();
		receivingQueue.dispose();
		extractingQueue.dispose();
		dispatchQueue.dispose();
		callbackQueue.dispose();
	}

	/**
	 * @return The queue to send data to the remote.
	 */
	public BlockingQueueTask<IHeaderMessage> getSendingQueue() {
		return sendingQueue;
	}

	/**
	 * Set the code to execute when data should be send to the remote.
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
	public BlockingQueueTask<Runnable> getDispatchQueue() {
		return dispatchQueue;
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
}
