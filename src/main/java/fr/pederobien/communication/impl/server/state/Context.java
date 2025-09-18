package fr.pederobien.communication.impl.server.state;

import fr.pederobien.communication.event.ServerUnstableEvent;
import fr.pederobien.communication.interfaces.server.IServer;
import fr.pederobien.communication.interfaces.server.IServerConfig;
import fr.pederobien.communication.interfaces.server.IServerImpl;
import fr.pederobien.utils.HealedCounter;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.Logger;

public class Context<T> implements IContext {
    private final IServer server;
    private final IServerConfig<T> config;
    private final IServerImpl<T> impl;
    private final IState opened;
    private final IState closed;
    private final IState disposed;
    private HealedCounter counter;
    private IState state;
    private boolean firstInit;

    public Context(IServer server, IServerConfig<T> config, IServerImpl<T> impl) {
        this.server = server;
        this.config = config;
        this.impl = impl;

        opened = new Opened<T>(this);
        closed = new Closed<T>(this);
        disposed = new Disposed<T>(this);
        state = closed;

        firstInit = true;
    }

    @Override
    public boolean open() {
        if (state.open()) {
            postInitialization();
            return true;
        }

        return false;
    }

    @Override
    public boolean close() {
        return state.close();
    }

    @Override
    public boolean dispose() {
        return state.dispose();
    }

    @Override
    public String getName() {
        return String.format("[%s %s]", config.getName(), config.getPoint());
    }

    /**
     * @return The server associated to this context.
     */
    public IServer getServer() {
        return server;
    }

    /**
     * @return The server configuration.
     */
    public IServerConfig<T> getConfig() {
        return config;
    }

    /**
     * @return The server implementation.
     */
    public IServerImpl<T> getImpl() {
        return impl;
    }

    /**
     * @return The opened state
     */
    public IState getOpened() {
        return opened;
    }

    /**
     * @return The closed state
     */
    public IState getClosed() {
        return closed;
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
     * Method called to initialize properties once the server is opened.
     */
    private void postInitialization() {
        if (!firstInit) {
            // Free resources associated to counter in order to create a new one
            counter.dispose();
        }

        int unstableCounter = config.getServerMaxUnstableCounter();
        int healTime = config.getServerHealTime();
        String CounterName = String.format("[%s %s unstable counter]", config.getName(), config.getPoint());
        counter = new HealedCounter(unstableCounter, healTime, this::onServerUnstable, CounterName);
        firstInit = false;
    }

    /**
     * Method called when the unstable counter reached its maximum value.
     */
    private void onServerUnstable() {
        Logger.error(String.format("%s - closing server", this));
        EventManager.callEvent(new ServerUnstableEvent(server));
    }
}
