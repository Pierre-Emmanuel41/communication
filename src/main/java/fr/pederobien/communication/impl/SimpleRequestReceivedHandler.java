package fr.pederobien.communication.impl;

import fr.pederobien.communication.event.RequestReceivedEvent;
import fr.pederobien.communication.interfaces.IRequestReceivedHandler;

public class SimpleRequestReceivedHandler implements IRequestReceivedHandler {

	@Override
	public void onRequestReceivedEvent(RequestReceivedEvent event) {
		// Do nothing
	}
}
