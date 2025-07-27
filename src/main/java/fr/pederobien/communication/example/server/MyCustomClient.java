package fr.pederobien.communication.example.server;

import fr.pederobien.communication.event.*;
import fr.pederobien.communication.impl.connection.Message;
import fr.pederobien.communication.interfaces.connection.IConnection;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;
import fr.pederobien.utils.event.Logger;

public record MyCustomClient(IConnection connection) implements IEventListener {
    public MyCustomClient(IConnection connection) {
        this.connection = connection;

        connection.setMessageHandler(this::onMessageReceived);

        // Listening for connection events
        EventManager.registerListener(this);
    }

    /**
     * @return The connection to the remote
     */
    @Override
    public IConnection connection() {
        return connection;
    }

    @EventHandler
    private void onConnectionDisposed(ConnectionDisposedEvent event) {
        if (event.getConnection() != connection) {
            return;
        }

        // Write here what to do when the connection has been disposed

        EventManager.unregisterListener(this);
    }

    @EventHandler
    private void onConnectionEnableChanged(ConnectionEnableChangedEvent event) {
        if (event.getConnection() != connection) {
            return;
        }

        // Write here what to do when the connection enable flag has changed
    }

    @EventHandler
    private void onConnectionLost(ConnectionLostEvent event) {
        if (event.getConnection() != connection) {
            return;
        }

        // Write here what to do when the connection with the remote is lost
    }

    @EventHandler
    private void onConnectionUnstable(ConnectionUnstableEvent event) {
        if (event.getConnection() != connection) {
            return;
        }

        // Write here what to do when the connection with the remote is unstable
    }

    /**
     * Handler when unexpected message has been received from the remote.
     *
     * @param event The event that contains remote message
     */
    private void onMessageReceived(MessageEvent event) {
        String received = new String(event.getData());
        Logger.info("[Server] Received %s", received);

        if (received.equals("I expect a response")) {
            String message = "Here is your response";
            Logger.info("[Server] Sending \"%s\" to client", message);
            Message response = new Message(message.getBytes());

            // event.getConnection() is equivalent to call connection class member directly
            event.getConnection().answer(event.getIdentifier(), response);
        }
    }
}
