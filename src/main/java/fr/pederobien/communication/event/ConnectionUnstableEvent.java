package fr.pederobien.communication.event;

import fr.pederobien.communication.interfaces.connection.IConnection;

import java.util.StringJoiner;

public class ConnectionUnstableEvent extends ConnectionEvent {

    /**
     * Create a connection unstable event.
     *
     * @param connection The unstable connection.
     */
    public ConnectionUnstableEvent(IConnection connection) {
        super(connection);
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(",", "{", "}");
        joiner.add("remote=" + getConnection());
        return String.format("%s_%s", getName(), joiner);
    }
}
