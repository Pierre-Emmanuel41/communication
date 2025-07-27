package fr.pederobien.communication.impl.connection;

import fr.pederobien.communication.event.ConnectionLostEvent;
import fr.pederobien.communication.event.MessageEvent;
import fr.pederobien.communication.interfaces.IMessageHandler;
import fr.pederobien.communication.interfaces.IToken;
import fr.pederobien.communication.interfaces.connection.IConnection;
import fr.pederobien.communication.interfaces.connection.IConnection.Mode;
import fr.pederobien.communication.interfaces.connection.IMessage;
import fr.pederobien.utils.Disposable;
import fr.pederobien.utils.IDisposable;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;

import java.util.concurrent.Semaphore;

public class Token implements IToken, IEventListener, IMessageHandler {
    private final IConnection connection;
    private final Mode mode;
    private final IDisposable disposable;
    private final Semaphore semaphore;
    private MessageEvent event;

    /**
     * Creates a token to perform information exchange before calling
     * ClientConnectedEvent.
     *
     * @param connection The live connection to the remote.
     */
    public Token(IConnection connection, Mode mode) {
        this.connection = connection;
        this.mode = mode;

        disposable = new Disposable();
        semaphore = new Semaphore(0);

        EventManager.registerListener(this);
    }

    @Override
    public Mode getMode() {
        return mode;
    }

    @Override
    public void send(IMessage message) {
        disposable.checkDisposed();

        connection.send(message);
    }

    @Override
    public void answer(int requestID, IMessage message) {
        disposable.checkDisposed();

        connection.answer(requestID, message);
    }

    @Override
    public MessageEvent receive() throws InterruptedException {
        disposable.checkDisposed();

        // Block until an unexpected data has been received.
        semaphore.acquire();

        // To ensure the line above will block.
        semaphore.drainPermits();

        return event;
    }

    @Override
    public void dispose() {
        if (disposable.dispose()) {

            // Notifying listener
            notify(new MessageEvent(connection, -1, null));

            EventManager.unregisterListener(this);
        }
    }

    @Override
    public void handle(MessageEvent event) {
        if (event.getConnection() != connection) {
            return;
        }

        notify(event);
    }

    @EventHandler
    private void onConnectionLostEvent(ConnectionLostEvent event) {
        if (event.getConnection() != connection) {
            return;
        }

        notify(new MessageEvent(connection, -1, null));
    }

    /**
     * Notify listeners, if any, that an unexpected request has been received from
     * the remote.
     *
     * @param event The event that holds the unexpected request.
     */
    private void notify(MessageEvent event) {
        this.event = event;
        semaphore.release();
    }
}
