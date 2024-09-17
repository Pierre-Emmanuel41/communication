package fr.pederobien.communication.testing.tools;

import fr.pederobien.communication.event.RequestReceivedEvent;
import fr.pederobien.communication.interfaces.IRequestReceivedHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.LogEvent;
import fr.pederobien.utils.event.LogEvent.ELogLevel;

public class SimpleClientListener implements IRequestReceivedHandler {
	private boolean exceptionMode;
	
	/**
	 * Creates a simple listener to handle a request received event.
	 * 
	 * @param exceptionMode True if the listener should throw an exception, false otherwise.
	 */
	public SimpleClientListener(boolean exceptionMode) {
		this.exceptionMode = exceptionMode;
	}

	@Override
	public void onRequestReceivedEvent(RequestReceivedEvent event) {
		if (exceptionMode)
			throw new RuntimeException("Exception to test unstable counter");

		String received = new String(event.getData());
		EventManager.callEvent(new LogEvent(ELogLevel.WARNING, "Client received: %s", received));
	}
}
