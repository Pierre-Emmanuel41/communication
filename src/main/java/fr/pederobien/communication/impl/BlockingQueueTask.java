package fr.pederobien.communication.impl;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class BlockingQueueTask<T> {
	private ScheduledExecutorService executorService;
	private Consumer<T> consumer;
	private BlockingQueue<T> queue;
	private AtomicBoolean disposed;
	private boolean isStarted;
	private ScheduledFuture<?> task;

	public BlockingQueueTask(ScheduledExecutorService executorService, Consumer<T> consumer) {
		this.executorService = executorService;
		this.consumer = consumer;
		disposed = new AtomicBoolean(false);
	}

	public void start() {
		checkIsDisposed();

		if (isStarted)
			return;

		isStarted = true;
		queue = new ArrayBlockingQueue<>(10000);

		task = executorService.schedule(() -> internalStart(), 1, TimeUnit.MILLISECONDS);
	}

	public void add(T e) {
		checkIsDisposed();
		queue.add(e);
	}

	public void dispose() {
		if (!disposed.compareAndSet(false, true))
			return;

		task.cancel(true);
	}

	private boolean isDisposed() {
		return disposed.get();
	}

	private void internalStart() {
		while (!isDisposed()) {
			try {
				consumer.accept(queue.take());
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	private void checkIsDisposed() {
		if (isDisposed())
			throw new UnsupportedOperationException("Object disposed");
	}
}
