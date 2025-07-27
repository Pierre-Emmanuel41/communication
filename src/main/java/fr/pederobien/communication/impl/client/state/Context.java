package fr.pederobien.communication.impl.client.state;

import fr.pederobien.communication.event.ClientUnstableEvent;
import fr.pederobien.communication.interfaces.client.IClient;
import fr.pederobien.communication.interfaces.client.IClientConfig;
import fr.pederobien.communication.interfaces.client.IClientImpl;
import fr.pederobien.communication.interfaces.connection.IConnection;
import fr.pederobien.utils.HealedCounter;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.Logger;

public class Context<T> implements IContext {
    private final IClient client;
    private final IClientConfig<T> config;
    private final IClientImpl<T> impl;
    private final IState disconnected;
    private final IState connected;
    private final IState disposed;
    private final HealedCounter counter;
    private IState state;
    private IConnection connection;

    /**
     * Create a state context.
     */
    public Context(IClient client, IClientConfig<T> config, IClientImpl<T> impl) {
        this.client = client;
        this.config = config;
        this.impl = impl;

        disconnected = new Disconnected<T>(this);
        connected = new Connected<T>(this);
        disposed = new Disposed<T>(this);

        state = disconnected;

        int unstableCounter = config.getClientMaxUnstableCounter();
        int healTime = config.getClientHealTime();
        String counterName = String.format("%s unstable counter", client);
        counter = new HealedCounter(unstableCounter, healTime, this::onClientUnstable, counterName);
    }

    @Override
    public void connect() {
        state.connect();
    }

    @Override
    public void disconnect() {
        state.disconnect();
    }

    @Override
    public void dispose() {
        state.dispose();
    }

    @Override
    public boolean isDisposed() {
        return state == disposed;
    }

    @Override
    public IConnection getConnection() {
        return connection;
    }

    /**
     * Set the connection of this context. The connection can be connected to the
     * client or null.
     *
     * @param connection The connection of the context.
     */
    public void setConnection(IConnection connection) {
        this.connection = connection;
    }

    /**
     * @return The client associated to this context.
     */
    public IClient getClient() {
        return client;
    }

    /**
     * @return The client configuration.
     */
    public IClientConfig<T> getConfig() {
        return config;
    }

    /**
     * @return The implementation specific to the network.
     */
    public IClientImpl<T> getImpl() {
        return impl;
    }

    /**
     * @return The disconnected state.
     */
    public IState getDisconnected() {
        return disconnected;
    }

    /**
     * @return The connected state.
     */
    public IState getConnected() {
        return connected;
    }

    /**
     * @return The disposed state.
     */
    public IState getDisposed() {
        return disposed;
    }

    /**
     * @return The healed-counter associated to this context.
     */
    public HealedCounter getCounter() {
        return counter;
    }

    /**
     * Set the state of this context.
     *
     * @param state The new state.
     */
    public void setState(IState state) {
        this.state.setEnabled(false);
        this.state = state;
        this.state.setEnabled(true);
    }

    /**
     * Function called when the healed counter reached its maximum value.
     */
    private void onClientUnstable() {
        Logger.error("%s - Stopping automatic reconnection", client);
        EventManager.callEvent(new ClientUnstableEvent(client));
    }
}
