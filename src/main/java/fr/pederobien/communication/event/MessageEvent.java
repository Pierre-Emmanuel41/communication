package fr.pederobien.communication.event;

import fr.pederobien.communication.interfaces.connection.IConnection;
import fr.pederobien.utils.ByteWrapper;

import java.util.StringJoiner;

public class MessageEvent extends ConnectionEvent {
    private final int identifier;
    private final byte[] data;

    /**
     * Creates a request received event. It is possible to set a response to the
     * request.
     *
     * @param connection The connection on which the message has been received.
     * @param identifier The identifier of the unexpected message.
     * @param data       The data received from the remote.
     */
    public MessageEvent(IConnection connection, int identifier, byte[] data) {
        super(connection);
        this.identifier = identifier;
        this.data = data;
    }

    /**
     * @return The identifier of the unexpected request.
     */
    public int getIdentifier() {
        return identifier;
    }

    /**
     * @return The data received from the remote.
     */
    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(",", "{", "}");
        joiner.add("connection=" + getConnection());
        joiner.add("identifier=" + getIdentifier());
        joiner.add("data=" + ByteWrapper.wrap(getData()));
        return String.format("%s_%s", getName(), joiner);
    }
}
