package fr.pederobien.communication.impl.connection;

import java.util.function.Consumer;

import fr.pederobien.communication.event.RequestReceivedEvent;
import fr.pederobien.communication.interfaces.connection.IHeaderMessage;
import fr.pederobien.utils.BlockingQueueTask;

public class QueueManager {
	private String name;
	private BlockingQueueTask<IHeaderMessage> sendingQueue;
	private BlockingQueueTask<Object> receivingQueue;
	private BlockingQueueTask<byte[]> extractingQueue;
	private BlockingQueueTask<CallbackManagement> callbackQueue;
	private BlockingQueueTask<RequestReceivedEvent> requestReceivedQueue;
	
	/**
	 * Creates a manager for a connection.
	 * 
	 * @param name the connection name.
	 */
	public QueueManager(String name) {
		this.name = name;
	}

	/**
	 * @return The queue to send data to the remote.
	 */
	public BlockingQueueTask<IHeaderMessage> getSendingQueue() {
		return sendingQueue;
	}

	/**
	 * Creates a new queue to send asynchronously data to the remote.
	 * 
	 * @param consumer The action to execute when data has been added to the queue.
	 */
	public void setSendingQueue(Consumer<IHeaderMessage> consumer) {
		sendingQueue = new BlockingQueueTask<IHeaderMessage>(String.format("[%s send]", name), consumer);
	}

	/**
	 * @return The queue to receive data from the remote.
	 */
	public BlockingQueueTask<Object> getReceivingQueue() {
		return receivingQueue;
	}

	/**
	 * Creates a new queue to receive asynchronously data from the remote.
	 * 
	 * @param consumer The action to execute when data has been added to the queue.
	 */
	public void setReceivingQueue(Consumer<Object> consumer) {
		receivingQueue = new BlockingQueueTask<Object>(String.format("[%s receive]", name), consumer);
	}

	/**
	 * @return The queue to extract data received from the remote.
	 */
	public BlockingQueueTask<byte[]> getExtractingQueue() {
		return extractingQueue;
	}

	/**
	 * Creates a new queue to extract asynchronously data received from the remote.
	 * 
	 * @param consumer The action to execute when data has been added to the queue.
	 */
	public void setExtractingQueue(Consumer<byte[]> consumer) {
		extractingQueue = new BlockingQueueTask<byte[]>(String.format("[%s extract]", name), consumer);
	}

	/**
	 * @return The queue to apply a callback when data has been received from the remote.
	 */
	public BlockingQueueTask<CallbackManagement> getCallbackQueue() {
		return callbackQueue;
	}

	/**
	 * Creates a new queue to apply asynchronously a callback when data has been received from the remote.
	 * 
	 * @param consumer The action to execute when data has been added to the queue.
	 */
	public void setCallbackQueue(Consumer<CallbackManagement> consumer) {
		callbackQueue = new BlockingQueueTask<CallbackManagement>(String.format("[%s callback]", name), consumer);
	}

	/**
	 * @return The queue to handle unexpected request received from the remote.
	 */
	public BlockingQueueTask<RequestReceivedEvent> getRequestReceivedQueue() {
		return requestReceivedQueue;
	}

	/**
	 * Creates a new queue to handle asynchronously unexpected request received from the remote.
	 * 
	 * @param consumer The action to execute when data has been added to the queue.
	 */
	public void setRequestReceivedQueue(Consumer<RequestReceivedEvent> consumer) {
		requestReceivedQueue = new BlockingQueueTask<RequestReceivedEvent>(String.format("[%s unexpected]", name), consumer);
	}

	/**
	 * Start each underlying blocking queue.
	 */
	public void start() {
		// Waiting for a message to be sent		
		sendingQueue.start();
		
		// Waiting for receiving message from network
		receivingQueue.start();
		receivingQueue.add(new Object());
		
		// Waiting for data to extract
		extractingQueue.start();
		
		// Waiting for callback to be called
		callbackQueue.start();
		
		// Waiting for an unexpected request
		requestReceivedQueue.start();
	}
	
	/**
	 * Dispose each underlying blocking queue.
	 */
	public void dispose() {
		sendingQueue.dispose();
		receivingQueue.dispose();
		extractingQueue.dispose();
		callbackQueue.dispose();
		requestReceivedQueue.dispose();
	}
}
