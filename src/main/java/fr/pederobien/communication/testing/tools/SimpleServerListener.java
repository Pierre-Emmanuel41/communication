package fr.pederobien.communication.testing.tools;

import fr.pederobien.communication.event.RequestReceivedEvent;
import fr.pederobien.communication.interfaces.IUnexpectedRequestHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.LogEvent;

public class SimpleServerListener implements IUnexpectedRequestHandler {

	@Override
	public void onUnexpectedRequestReceived(RequestReceivedEvent event) {
		String received = new String(event.getData());
		EventManager.callEvent(new LogEvent("Server received: %s", received));
	}
}
