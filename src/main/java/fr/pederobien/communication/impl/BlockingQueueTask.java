package fr.pederobien.communication.impl;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class BlockingQueueTask<T> {
	private Thread queueThread;
	private Consumer<T> consumer;
	private BlockingQueue<T> queue;
	private AtomicBoolean disposed;
	private boolean isStarted;

	/**
	 * Create a thread associated to a BlockingQueue.
	 * 
	 * @param name     The thread name.
	 * @param consumer The code to execute when an element is added to this queue.
	 */
	public BlockingQueueTask(String name, Consumer<T> consumer) {
		this.consumer = consumer;

		queueThread = new Thread(() -> internalStart(), name);
		queueThread.setDaemon(true);

		disposed = new AtomicBoolean(false);
	}

	public void start() {
		checkIsDisposed();

		if (isStarted)
			return;

		queue = new ArrayBlockingQueue<>(10000);
		queueThread.start();
		isStarted = true;
	}

	public void add(T e) {
		checkIsDisposed();
		queue.add(e);
	}

	public void dispose() {
		if (!disposed.compareAndSet(false, true))
			return;

		queueThread.interrupt();
	}

	private boolean isDisposed() {
		return disposed.get();
	}

	private void internalStart() {
		while (!isDisposed()) {
			try {
				consumer.accept(queue.take());
			} catch (InterruptedException e) {

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void checkIsDisposed() {
		if (isDisposed())
			throw new UnsupportedOperationException("Object disposed");
	}
}
