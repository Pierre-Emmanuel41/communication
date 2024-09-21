package fr.pederobien.communication.testing.tools;

import fr.pederobien.communication.event.RequestReceivedEvent;
import fr.pederobien.communication.interfaces.IRequestReceivedHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.LogEvent;

public class SimpleServerListener implements IRequestReceivedHandler {

	@Override
	public void onRequestReceivedEvent(RequestReceivedEvent event) {
		String received = new String(event.getData());
		EventManager.callEvent(new LogEvent("Server received: %s", received));
	}
}
