package fr.pederobien.communication.impl;

import fr.pederobien.communication.event.LogEvent;
import fr.pederobien.utils.AsyncConsole;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;

public class LogEventListener implements IEventListener {

	/**
	 * Register this listener in the EventManager in order to display logs from connections.
	 */
	public void register() {
		EventManager.registerListener(this);
	}

	/**
	 * Unregister this listener from the EventManager in order to not be notified when a log event is thrown.
	 */
	public void unregister() {
		EventManager.unregisterListener(this);
	}

	@EventHandler
	private void onLog(LogEvent event) {
		AsyncConsole.printlnWithTimeStamp(event.getMessage());
	}
}
