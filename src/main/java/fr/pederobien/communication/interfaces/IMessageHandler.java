package fr.pederobien.communication.interfaces;

import fr.pederobien.communication.event.MessageEvent;

public interface IMessageHandler {

    /**
     * Method called when an unexpected message is received from the remote.
     *
     * @param event The event that contains the unexpected message.
     */
    void handle(MessageEvent event);
}
