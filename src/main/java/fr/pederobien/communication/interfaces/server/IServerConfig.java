package fr.pederobien.communication.interfaces.server;

import fr.pederobien.communication.interfaces.IConfiguration;

public interface IServerConfig<T> extends IConfiguration {

    /**
     * @return The name of the server.
     */
    String getName();

    /**
     * @return The properties of the server communication point.
     */
    T getPoint();

    /**
     * @return The validator to authorize or not the client to be connected to the
     * server.
     */
    IClientValidator<T> getClientValidator();

    /**
     * The server is monitored when waiting for a new client, validating client
     * end-point and initialising the connection with the remote. During the server
     * lifetime, it is likely possible that the server become unstable. The
     * server's max counter is the maximum value the unstable counter can reach
     * before throwing a server unstable event and closing the server. This counter
     * is incremented each time an exception is happening.
     *
     * @return The maximum value the server's unstable counter can reach.
     */
    int getServerMaxUnstableCounter();

    /**
     * The server is monitored when waiting for a new client, validating client
     * end-point and initialising the connection with the remote. During the server
     * lifetime, it is likely possible that the server become unstable. However, if
     * the server is stable the unstable counter value should be 0 as no error
     * happened for a long time. The heal time, in milliseconds, is the time after
     * which the server's error counter is decremented.
     *
     * @return The time, in ms, after which the server's error counter is
     * decremented.
     */
    int getServerHealTime();
}
