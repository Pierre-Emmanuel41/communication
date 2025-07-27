package fr.pederobien.communication.impl.keyexchange;

import fr.pederobien.communication.event.MessageEvent;
import fr.pederobien.communication.impl.connection.Message;
import fr.pederobien.communication.interfaces.IToken;
import fr.pederobien.communication.interfaces.connection.ICallback.CallbackArgs;
import fr.pederobien.communication.interfaces.connection.IConnection.Mode;

import java.util.function.Consumer;

public abstract class Exchange {
    public static final byte[] SUCCESS_PATTERN = "SUCCESS_PATTERN".getBytes();

    private final IToken token;


    /**
     * Creates an object to wrap a token.
     *
     * @param token The token used to send/receive data from the remote.
     */
    public Exchange(IToken token) {
        this.token = token;
    }

    /**
     * Perform the exchange with the remote.
     *
     * @return True if the exchange succeed, false otherwise.
     */
    public final boolean exchange() {
        try {
            if (token.getMode() == Mode.SERVER_TO_CLIENT) {
                return doServerToClientExchange();
            } else if (token.getMode() == Mode.CLIENT_TO_SERVER) {
                return doClientToServerExchange();
            }
        } catch (Exception e) {
            // Do nothing
        }

        return false;
    }

    /**
     * Called if the token's mode is Server-to-Client.
     *
     * @return True if the exchange is successful, false otherwise.
     */
    protected abstract boolean doServerToClientExchange() throws Exception;

    /**
     * Called if the token's mode is Client-to-Server.
     *
     * @return True if the exchange is successful, false otherwise.
     */
    protected abstract boolean doClientToServerExchange() throws Exception;

    /**
     * Creates a message to send to the remote.
     *
     * @param bytes    The bytes of the message.
     * @param timeout  The maximum time, in ms, to wait for remote response.
     * @param callback The code to execute once a response has been received or a
     *                 timeout occurs.
     */
    protected void send(byte[] bytes, int timeout, Consumer<CallbackArgs> callback) {
        token.send(new Message(bytes, true, timeout, callback));
    }

    /**
     * Creates a message to send to the remote.
     *
     * @param bytes The bytes of the message.
     */
    protected void send(byte[] bytes) {
        token.send(new Message(bytes, true));
    }

    /**
     * Creates a message to send to the remote.
     *
     * @param requestID The identifier of the request to be answered.
     * @param bytes     The bytes of the message.
     * @param timeout   The maximum time, in ms, to wait for remote response.
     * @param callback  The code to execute once a response has been received or a
     *                  timeout occurs.
     */
    protected void answer(int requestID, byte[] bytes, int timeout, Consumer<CallbackArgs> callback) {
        token.answer(requestID, new Message(bytes, true, timeout, callback));
    }

    /**
     * Creates a message to send to the remote.
     *
     * @param requestID The identifier of the request to be answered.
     * @param bytes     The bytes of the message.
     */
    protected void answer(int requestID, byte[] bytes) {
        token.answer(requestID, new Message(bytes, true));
    }

    /**
     * @return Block until unexpected data has been received from the remote.
     */
    protected MessageEvent receive() throws Exception {
        return token.receive();
    }
}
