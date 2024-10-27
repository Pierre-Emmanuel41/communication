package fr.pederobien.communication.impl.connection;

import java.util.function.Consumer;

import fr.pederobien.communication.event.ConnectionDisposedEvent;
import fr.pederobien.communication.event.ConnectionLostEvent;
import fr.pederobien.communication.event.ConnectionUnstableEvent;
import fr.pederobien.communication.interfaces.connection.IConnection;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;

public class ConnectionListener implements IEventListener {
	private IConnection connection;
	private Consumer<ConnectionLostEvent> onConnectionLost;
	private Consumer<ConnectionUnstableEvent> onConnectionUnstable;
	private Consumer<ConnectionDisposedEvent> onConnectionDisposed;

	public ConnectionListener(IConnection connection) {
		this.connection = connection;
	}

	/**
	 * Start monitoring the underlying connection.
	 */
	public void start() {
		EventManager.registerListener(this);
	}

	/**
	 * Stop monitoring the underlying connection.
	 */
	public void stop() {
		EventManager.unregisterListener(this);
	}
	
	/**
	 * Set handler when a connection lost event is thrown.
	 * 
	 * @param onConnectionLost The handler to call.
	 */
	public void setOnConnectionLost(Consumer<ConnectionLostEvent> onConnectionLost) {
		this.onConnectionLost = onConnectionLost;
	}
	
	/**
	 * Set handler when a connection unstable event is thrown.
	 * 
	 * @param onConnectionUnstable The handler to call.
	 */
	public void setOnConnectionUnstable(Consumer<ConnectionUnstableEvent> onConnectionUnstable) {
		this.onConnectionUnstable = onConnectionUnstable;
	}
	
	/**
	 * Set handler when a connection disposed event is thrown.
	 * 
	 * @param onConnectionDisposed The handler to call.
	 */
	public void setOnConnectionDisposed(Consumer<ConnectionDisposedEvent> onConnectionDisposed) {
		this.onConnectionDisposed = onConnectionDisposed;
	}
	
	@EventHandler
	private void onConnectionLost(ConnectionLostEvent event) {
		if (event.getConnection() != connection || onConnectionLost == null)
			return;
		
		onConnectionLost.accept(event);
	}
	
	@EventHandler
	private void onConnectionUnstable(ConnectionUnstableEvent event) {
		if (event.getConnection() != connection || onConnectionUnstable == null)
			return;
		
		onConnectionUnstable.accept(event);
	}
	
	@EventHandler
	private void onConnectionDisposed(ConnectionDisposedEvent event) {
		if (event.getConnection() != connection)
			return;
		
		if (onConnectionDisposed != null)
			onConnectionDisposed.accept(event);
		
		// A disposed connection cannot be used anymore, stopping monitoring
		stop();
	}
}
