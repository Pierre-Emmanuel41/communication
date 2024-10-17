package fr.pederobien.communication.testing.tools;

import fr.pederobien.communication.event.RequestReceivedEvent;
import fr.pederobien.communication.interfaces.IUnexpectedRequestHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.LogEvent;

public class SimpleClientListener implements IUnexpectedRequestHandler {
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
	public void onUnexpectedRequestReceived(RequestReceivedEvent event) {
		if (exceptionMode)
			throw new RuntimeException("Exception to test unstable counter");

		String received = new String(event.getData());
		EventManager.callEvent(new LogEvent("Client received: %s", received));
	}
}
