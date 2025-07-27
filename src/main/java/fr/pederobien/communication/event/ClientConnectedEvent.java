package fr.pederobien.communication.event;

import fr.pederobien.communication.interfaces.client.IClient;

import java.util.StringJoiner;

public class ClientConnectedEvent extends ClientEvent {

    /**
     * Creates a client connected event.
     *
     * @param client The client connected to the remote.
     */
    public ClientConnectedEvent(IClient client) {
        super(client);
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(",", "{", "}");
        joiner.add("remote=" + getClient().getConnection());
        return String.format("%s_%s", getName(), joiner);
    }
}
