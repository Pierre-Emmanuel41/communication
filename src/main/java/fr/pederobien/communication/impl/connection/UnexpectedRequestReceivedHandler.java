package fr.pederobien.communication.impl.connection;

import fr.pederobien.communication.event.RequestReceivedEvent;
import fr.pederobien.communication.interfaces.IUnexpectedRequestHandler;

public class UnexpectedRequestReceivedHandler implements IUnexpectedRequestHandler {

	@Override
	public void onUnexpectedRequestReceived(RequestReceivedEvent event) {
		// Do nothing
	}
}
