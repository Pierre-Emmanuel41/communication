package fr.pederobien.communication.impl.connection;

import fr.pederobien.communication.event.*;
import fr.pederobien.communication.interfaces.IConfiguration;
import fr.pederobien.communication.interfaces.IMessageHandler;
import fr.pederobien.communication.interfaces.connection.ICallback.CallbackArgs;
import fr.pederobien.communication.interfaces.connection.IConnection;
import fr.pederobien.communication.interfaces.connection.IConnectionImpl;
import fr.pederobien.communication.interfaces.connection.IHeaderMessage;
import fr.pederobien.communication.interfaces.connection.IMessage;
import fr.pederobien.communication.interfaces.layer.ILayerInitializer;
import fr.pederobien.utils.Disposable;
import fr.pederobien.utils.HealedCounter;
import fr.pederobien.utils.IDisposable;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.Logger;

import java.util.List;
import java.util.concurrent.Semaphore;

public class Connection<T> implements IConnection {
    private final IConfiguration config;
    private final IConnectionImpl impl;
    private final QueueManager queueManager;
    private final CallbackManager callbackManager;
    private final IDisposable disposable;
    private final ILayerInitializer layerInitializer;
    private final HealedCounter counter;
    // When the synchronous send has been called
    private final Semaphore semaphore;
    private String name;
    private IMessageHandler handler;
    private boolean isEnabled;
    private CallbackArgs argument;

    /**
     * Create an abstract connection that send asynchronously messages to the
     * remote.
     *
     * @param config   The object that holds the connection configuration.
     * @param endPoint The object that gather remote information.
     * @param impl     The connection specific implementation for
     *                 sending/receiving data from the remote.
     */
    public Connection(IConfiguration config, T endPoint, IConnectionImpl impl) {
        this.config = config;
        this.impl = impl;

        String remote = config.getMode() == Mode.CLIENT_TO_SERVER ? "Server" : "Client";
        name = String.format("%s %s", remote, endPoint);

        queueManager = new QueueManager(name);
        queueManager.setOnSend(this::sendMessage);
        queueManager.setOnReceive(this::receiveMessage);
        queueManager.setOnExtract(this::extractMessage);
        queueManager.setOnDispatch(this::dispatch);

        int unstableCounter = config.getConnectionMaxUnstableCounter();
        int healTime = config.getConnectionHealTime();
        String counterName = String.format("[%s HealedCounter]", name);
        counter = new HealedCounter(unstableCounter, healTime, this::onUnstableConnection, counterName);

        callbackManager = new CallbackManager(queueManager, counter);
        disposable = new Disposable();
        semaphore = new Semaphore(0);

        layerInitializer = config.getLayerInitializer();
        name = String.format("[%s]", name);
        isEnabled = true;
    }

    @Override
    public boolean initialise() throws Exception {
        queueManager.initialize();

        // Initializing layer
        Token token = new Token(this, config.getMode());
        handler = token;

        boolean success = layerInitializer.initialize(token);
        token.dispose();

        setMessageHandler(null);
        return success;
    }

    @Override
    public void setMessageHandler(IMessageHandler handler) {
        this.handler = handler == null ? (this::doNothing) : handler;
    }

    @Override
    public void send(IMessage message) {
        disposable.checkDisposed();

        if (isEnabled()) {
            send(0, message);
        }
    }

    @Override
    public void answer(int requestID, IMessage message) {
        disposable.checkDisposed();

        if (isEnabled()) {
            send(requestID, message);
        }
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public void setEnabled(boolean isEnabled) {
        if (this.isEnabled == isEnabled) {
            return;
        }

        this.isEnabled = isEnabled;
        EventManager.callEvent(new ConnectionEnableChangedEvent(this, isEnabled));
    }

    @Override
    public void dispose() {
        if (disposable.dispose()) {

            // Disposing connection implementation
            impl.dispose();

            // Disposing callback manager
            callbackManager.dispose();

            // Disposing sending, receiving and extracting queue
            queueManager.dispose();

            // Disposing unstable counter
            counter.dispose();

            EventManager.callEvent(new ConnectionDisposedEvent(this));
        }
    }

    @Override
    public boolean isDisposed() {
        return disposable.isDisposed();
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Throw an unstable connection event.
     */
    private void onUnstableConnection() {
        Logger.error("%s - Unstable connection detected", name);
        EventManager.callEvent(new ConnectionUnstableEvent(this));
    }

    /**
     * Send asynchronously a message to the remote.
     *
     * @param message The message to send.
     */
    private void sendMessage(IHeaderMessage message) {
        if (isEnabled()) {
            try {
                byte[] data = layerInitializer.getLayer().pack(message);

                callbackManager.start(message.getIdentifier());
                impl.send(data);

            } catch (Exception exception) {
                counter.increment();
            }
        }
    }

    /**
     * Wait asynchronously for receiving data from the remote.
     */
    private void receiveMessage(Object object) {
        if (isEnabled()) {
            byte[] raw = null;

            try {
                raw = impl.receive();
            } catch (Exception e) {
                if (!counter.increment() && !isDisposed()) {
                    // Waiting again for the reception
                    queueManager.getReceivingQueue().add(new Object());
                }

                // No need to go further
                return;
            }

            // When raw is null, a problem happened while waiting
            // for receiving data from the remote
            if (raw == null) {

                // If connection is disabled, client is being disconnected
                if (isEnabled()) {
                    EventManager.callEvent(new ConnectionLostEvent(this));
                }
            } else {

                // Adding raw data for asynchronous extraction
                queueManager.getExtractingQueue().add(raw);

                // Waiting again for the reception
                queueManager.getReceivingQueue().add(new Object());
            }
        }
    }

    /**
     * Extract asynchronously the requests from the raw buffer.
     *
     * @param raw The bytes array received from the remote.
     */
    private void extractMessage(byte[] raw) {
        if (isEnabled()) {

            try {
                // Extracting requests from the raw bytes array received from the network
                List<IHeaderMessage> requests = layerInitializer.getLayer().unpack(raw);

                // For each received request
                for (IHeaderMessage request : requests) {

                    // A 0 identifier means it does not correspond to a response to a request
                    if (request.getRequestID() != 0) {

                        // Execute the callback of the original request
                        callbackManager.unregisterAndExecute(request);
                    } else {
                        // Dispatching asynchronously a message event.
                        MessageEvent event = new MessageEvent(this, request.getIdentifier(), request.getBytes());
                        queueManager.getDispatchingQueue().add(event);
                    }
                }

            } catch (Exception e) {
                counter.increment();
            }
        }
    }

    /**
     * Creates a header message to be sent to the remote.
     *
     * @param requestID The Identifier of the request to respond to.
     * @param message   The message to send to the remote.
     */
    private void send(int requestID, IMessage message) {
        IMessage toSend = message;

        if (message.isSync()) {
            int timeout = message.getCallback().timeout() == -1 ? 10 : message.getCallback().timeout();
            toSend = new Message(message.getBytes(), message.isSync(), timeout, args -> {
                argument = args;
                semaphore.release();
            });
        }

        IHeaderMessage header = new HeaderMessage(requestID, toSend);
        callbackManager.register(header.getIdentifier(), toSend);
        queueManager.getSendingQueue().add(header);

        if (message.isSync()) {
            try {
                // Wait until the callback has been executed
                semaphore.acquire();
            } catch (Exception e) {
                argument = new CallbackArgs(-1, null, true, false);
            } finally {
                // Always draining the semaphore to force the synchronous send
                semaphore.drainPermits();
            }

            // Executing callback
            if (message.getCallback().timeout() != -1) {
                message.getCallback().apply(argument);
            }
        }
    }

    /**
     * Default method to call when an unexpected message has been received from the
     * remote.
     *
     * @param event The event that contains the unexpected message.
     */
    private void doNothing(MessageEvent event) {

    }

    /**
     * Dispatch asynchronously the given event and increment the unstable counter if an exception occurred.
     *
     * @param event The event that contains the unexpected message.
     */
    private void dispatch(MessageEvent event) {
        try {
            handler.handle(event);
        } catch (Exception e) {
            counter.increment();
        }
    }
}
