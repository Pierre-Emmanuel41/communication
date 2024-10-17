package fr.pederobien.communication.impl.connection;

import java.util.concurrent.Semaphore;

import fr.pederobien.communication.event.ConnectionLostEvent;
import fr.pederobien.communication.event.RequestReceivedEvent;
import fr.pederobien.communication.interfaces.IConnection;
import fr.pederobien.communication.interfaces.IConnection.Mode;
import fr.pederobien.communication.interfaces.IMessage;
import fr.pederobien.communication.interfaces.IToken;
import fr.pederobien.communication.interfaces.IUnexpectedRequestHandler;
import fr.pederobien.utils.Disposable;
import fr.pederobien.utils.IDisposable;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;

public class Token implements IToken, IEventListener, IUnexpectedRequestHandler {
	private IConnection connection;
	private IDisposable disposable;
	private RequestReceivedEvent event;
	private Semaphore semaphore;

	/**
	 * Creates a token to perform information exchange before calling ClientConnectedEvent.
	 * 
	 * @param connection The live connection to the remote.
	 */
	public Token(IConnection connection) {
		this.connection = connection;

		disposable = new Disposable();
		semaphore = new Semaphore(0);

		EventManager.registerListener(this);
	}

	@Override
	public Mode getMode() {
		return connection.getConfig().getMode();
	}

	@Override
	public void send(IMessage message) {
		disposable.checkDisposed();

		connection.send(message);
	}

	@Override
	public void answer(int requestID, IMessage message) {
		disposable.checkDisposed();

		connection.answer(requestID, message);
	}

	@Override
	public RequestReceivedEvent receive() throws InterruptedException {
		disposable.checkDisposed();

		// Block until an unexpected data has been received.
		semaphore.acquire();

		// To ensure the line above will block.
		semaphore.drainPermits();

		return event;
	}

	@Override
	public void dispose() {
		if (disposable.dispose()) {

			// Notifying listener
			notify(new RequestReceivedEvent(connection, null, -1));

			EventManager.unregisterListener(this);
		}
	}

	@Override
	public void onUnexpectedRequestReceived(RequestReceivedEvent event) {
		if (event.getConnection() != connection)
			return;

		notify(event);
	}
	
	@EventHandler
	private void onConnectionLostEvent(ConnectionLostEvent event) {
		if (event.getConnection() != connection)
			return;

		notify(new RequestReceivedEvent(connection, null, -1));
	}

	/**
	 * Notify listeners, if any, that an unexpected request has been received from the remote.
	 * 
	 * @param event The event that holds the unexpected request.
	 */
	private void notify(RequestReceivedEvent event) {
		this.event = event;
		semaphore.release();
	}
}
