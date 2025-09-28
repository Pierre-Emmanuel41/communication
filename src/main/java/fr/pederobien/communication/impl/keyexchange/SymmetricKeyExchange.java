package fr.pederobien.communication.impl.keyexchange;

import fr.pederobien.communication.event.MessageEvent;
import fr.pederobien.communication.interfaces.IToken;
import fr.pederobien.utils.Watchdog;
import fr.pederobien.utils.Watchdog.WatchdogStakeholder;
import fr.pederobien.utils.event.Logger;

import javax.crypto.SecretKey;
import java.util.Arrays;

public class SymmetricKeyExchange extends Exchange {
    private final SymmetricKeyManager keyManager;
    private final int timeout;
    private boolean success;
    private WatchdogStakeholder watchdog;

    /**
     * Creates a key exchange for symmetric encoding/decoding.
     *
     * @param token      The token used to send/receive data from the remote.
     * @param keyManager The manager that generates a secret key and parse remote
     *                   secret key.
     * @param timeout    The maximum time, in ms, to wait for remote response during
     *                   the key exchange.
     */
    public SymmetricKeyExchange(IToken token, SymmetricKeyManager keyManager, int timeout) {
        super(token);
        this.keyManager = keyManager;
        this.timeout = timeout;

        success = false;
    }

    /**
     * @return The secret key received from the server.
     */
    public SecretKey getRemoteKey() {
        return keyManager.getSecretKey();
    }

    @Override
    protected boolean doServerToClientExchange() throws Exception {
        Logger.debug("[Server to Client] Generating private key and sending it to the cleint");

        send(keyManager.generateKey().getEncoded(), timeout, args -> {
            if (!args.isTimeout()) {

                // Extracting client secret key
                if (keyManager.parse(args.response())) {

                    Logger.debug("[Server to Client] Client's private key received successfully");

                    // Sending positive acknowledgement to the client
                    serverToClient_sendPositiveAcknowledgement(args.identifier());
                }
            }
        });

        // Adding delay to let the client be ready for next initialisation step
        try {
            Thread.sleep(500);
        } catch (Exception e) {
            // Do nothing
        }

        return success;
    }

    @Override
    protected boolean doClientToServerExchange() throws Exception {
        watchdog = Watchdog.create(() -> {

            Logger.debug("[Client to Server] Waiting for server's private key");

            // Waiting for initialisation to happen successfully
            MessageEvent event = receive();

            // Connection with the remote has been lost
            if (event.getData() == null) {
                watchdog.cancel();
            }

            // Parsing remote secret key
            else if (keyManager.parse(event.getData())) {

                Logger.debug("[Client to Server] Server's private key received successfully");

                // Sending back the remote secret key
                clientToServer_sendBackSecretKey(event.getIdentifier(), event.getData());
            }
        }, 10000);
        return watchdog.start();
    }

    private void serverToClient_sendPositiveAcknowledgement(int identifier) {
        Logger.debug("[Server to Client] Sending positive acknowledgement to the client");

        answer(identifier, SUCCESS_PATTERN, timeout, args -> {
            if (!args.isTimeout()) {
                if (Arrays.equals(SUCCESS_PATTERN, args.response())) {
                    Logger.debug("[Server to Client] Positive acknowledgement received from the client");
                    success = true;
                }
            }
        });
    }

    private void clientToServer_sendBackSecretKey(int identifier, byte[] remoteKey) {
        Logger.debug("[Client to Server] Sending private key to the server");

        answer(identifier, remoteKey, timeout, args -> {
            if (!args.isTimeout()) {
                if (Arrays.equals(SUCCESS_PATTERN, args.response())) {
                    Logger.debug("[Client to Server] Positive acknowledgement received, sending back positive acknowledgement");

                    answer(args.identifier(), SUCCESS_PATTERN);
                    success = true;
                }
            } else {
                watchdog.cancel();
            }
        });
    }
}
