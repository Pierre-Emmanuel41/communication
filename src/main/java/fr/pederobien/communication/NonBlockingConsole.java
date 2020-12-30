package fr.pederobien.communication;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class NonBlockingConsole {
	private static BlockingQueue<String> queue;
	private static DateTimeFormatter formater;

	static {
		queue = new ArrayBlockingQueue<>(10000);
		formater = DateTimeFormatter.ofPattern("HH:mm:ss:SSS");
		Thread thread = new Thread(() -> work());
		thread.setDaemon(true);
		thread.start();
	}

	public static void print(Object message) {
		queue.add(String.format("[%s] : %s", formater.format(LocalTime.now()), message.toString()));
	}

	public static void println(Object message) {
		print(message + "\r\n");
	}

	private static void work() {
		while (true) {
			try {

				System.out.print(queue.take());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
