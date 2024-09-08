package fr.pederobien.communication.impl;

import java.util.concurrent.Semaphore;

import fr.pederobien.communication.event.RequestReceivedEvent;
import fr.pederobien.communication.interfaces.ICallbackMessage;
import fr.pederobien.communication.interfaces.IConnection;
import fr.pederobien.communication.interfaces.IExchange;

public class Exchange implements IExchange {
	private IConnection connection;
	private Semaphore semaphore;
	private RequestReceivedEvent event;

	public Exchange(IConnection connection) {
		this.connection = connection;
		
		semaphore = new Semaphore(0);
	}
	
	@Override
	public void send(ICallbackMessage message) {
		connection.send(message);
	}
	
	@Override
	public RequestReceivedEvent receive() throws InterruptedException {
		semaphore.acquire();
		return event;
	}
	
	/**
	 * Notify that data has been received from the remote. If something was waiting, this event will
	 * be returned.
	 * 
	 * @param event The event to send back.
	 */
	public void notify(RequestReceivedEvent event) {
		this.event = event;
		semaphore.release();
	}
}
