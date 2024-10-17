package fr.pederobien.communication.impl;

import java.util.concurrent.Semaphore;

import fr.pederobien.communication.event.ConnectionLostEvent;
import fr.pederobien.communication.event.RequestReceivedEvent;
import fr.pederobien.communication.interfaces.IConnection;
import fr.pederobien.communication.interfaces.IConnection.Mode;
import fr.pederobien.communication.interfaces.IExchange;
import fr.pederobien.communication.interfaces.IMessage;
import fr.pederobien.communication.interfaces.IUnexpectedRequestHandler;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;

public class Exchange implements IExchange, IEventListener {
	private IConnection connection;
	private Semaphore receive, handled;
	private RequestReceivedEvent event;

	public Exchange(IConnection connection) {
		this.connection = connection;
		
		receive = new Semaphore(0);
		handled = new Semaphore(0);

		EventManager.registerListener(this);
	}
	
	@Override
	public void send(IMessage message) {
		connection.send(message);
	}

	@Override
	public void answer(int identifier, IMessage message) {
		connection.answer(identifier, message);
	}

	@Override
	public void receive(IUnexpectedRequestHandler handler) throws InterruptedException {
		// Waiting for receiving data from remote
		receive.acquire();
		receive.drainPermits();
		
		// Delay to let the notifying thread to acquire the handled semaphore
		Thread.sleep(10);
		
		// Handling received data
		handler.onUnexpectedRequestReceived(event);
		
		// Notifying the data has been handled
		handled.release();
	}

	@Override
	public Mode getMode() {
		return connection.getConfig().getMode();
	}

	/**
	 * Notify that data has been received from the remote. If something was waiting, this event will
	 * be returned.
	 * 
	 * @param event The event to send back.
	 */
	public void notify(RequestReceivedEvent event) throws InterruptedException {
		this.event = event;
		receive.release();
		
		// Waiting for the event to be dispatched to the handler
		handled.acquire();
		handled.drainPermits();
	}

	@EventHandler
	private void onConnectionLost(ConnectionLostEvent event) {
		if (connection != event.getConnection())
			return;

		this.event = new RequestReceivedEvent(connection, null, -1);
		receive.release();
	}
}
