package fr.pederobien.communication.impl;

import fr.pederobien.communication.interfaces.connection.IConnection.Mode;
import fr.pederobien.communication.interfaces.server.IClientValidator;
import fr.pederobien.communication.interfaces.server.IServerConfig;

public class ServerConfig<T> extends Configuration implements IServerConfig<T> {
    private final String name;
    private final T point;
    private IClientValidator<T> clientValidator;
    private int serverMaxUnstableCounter;
    private int serverHealTime;

    /**
     * Creates a configuration that holds the parameters for a server.
     *
     * @param name  The server's name.
     * @param point The properties of the server communication point.
     */
    protected ServerConfig(String name, T point) {
        super(Mode.SERVER_TO_CLIENT);

        this.name = name;
        this.point = point;

        clientValidator = endPoint -> true;
        serverMaxUnstableCounter = 5;
        serverHealTime = 1000;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public T getPoint() {
        return point;
    }

    @Override
    public IClientValidator<T> getClientValidator() {
        return clientValidator;
    }

    /**
     * Set the server client validator.
     *
     * @param clientValidator The validator to authorize a client to be connected to
     *                        the server.
     */
    public void setClientValidator(IClientValidator<T> clientValidator) {
        this.clientValidator = clientValidator;
    }

    @Override
    public int getServerMaxUnstableCounter() {
        return serverMaxUnstableCounter;
    }

    /**
     * The server is monitored when waiting for a new client, validating client
     * end-point and initialising the connection with the remote. During the server
     * lifetime, it is likely possible that the server become unstable. The
     * server's max counter is the maximum value the unstable counter can reach
     * before throwing a server unstable event and closing the server. This counter
     * is incremented each time an exception is happening.
     *
     * @param serverMaxUnstableCounter The maximum value the server's unstable
     *                                 counter can reach.
     */
    public void setServerMaxUnstableCounter(int serverMaxUnstableCounter) {
        this.serverMaxUnstableCounter = serverMaxUnstableCounter;
    }

    @Override
    public int getServerHealTime() {
        return serverHealTime;
    }

    /**
     * The server is monitored when waiting for a new client, validating client
     * end-point and initialising the connection with the remote. During the server
     * lifetime, it is likely possible that the server become unstable. However, if
     * the server is stable the unstable counter value should be 0 as no error
     * happened for a long time. The heal time, in milliseconds, is the time after
     * which the server's error counter is decremented.
     *
     * @param serverHealTime The time, in ms, after which the server's error counter
     *                       is decremented.
     */
    public void setServerHealTime(int serverHealTime) {
        this.serverHealTime = serverHealTime;
    }
}
