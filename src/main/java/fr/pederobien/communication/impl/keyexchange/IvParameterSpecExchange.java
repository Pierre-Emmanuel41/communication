package fr.pederobien.communication.impl.keyexchange;

import fr.pederobien.communication.event.MessageEvent;
import fr.pederobien.communication.interfaces.IToken;
import fr.pederobien.utils.Watchdog;
import fr.pederobien.utils.Watchdog.WatchdogStakeholder;
import fr.pederobien.utils.event.Logger;

import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

public class IvParameterSpecExchange extends Exchange {
    private AlgorithmParameterSpec ivParameterSpec;
    private boolean success;
    private WatchdogStakeholder watchdog;

    /**
     * Creates an IV exchange for symmetric encoding/decoding.
     *
     * @param token The token used to send/receive data from the remote.
     */
    public IvParameterSpecExchange(IToken token) {
        super(token);

        success = false;
    }

    /**
     * @return The IV parameter spec shared between the client and the server.
     */
    public AlgorithmParameterSpec getIvParameterSpec() {
        return ivParameterSpec;
    }

    @Override
    protected boolean doServerToClientExchange() throws Exception {
        Logger.debug("[Server to Client] Sending IV parameter to the client");

        // Generating a new parameter specification to send
        byte[] iv = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        send(iv, 2000, args -> {
            if (!args.isTimeout()) {

                // Step 2: Receiving remote parameter specification
                if (Arrays.equals(args.response(), iv)) {

                    Logger.debug("[Server to Client] Client's IV parameter identical to server IV parameter");

                    // Sending positive acknowledgement to the client
                    serverToClient_sendPositiveAcknowledgement(args.identifier(), iv);
                }
            }
        });

        return success;
    }

    @Override
    protected boolean doClientToServerExchange() throws Exception {
        watchdog = Watchdog.create(() -> {
            while (!success) {

                Logger.debug("[Client to Server] Waiting for server's IV parameter");

                // Waiting for initialisation to happen successfully
                MessageEvent event = receive();

                // Connection with the remote has been lost
                if (event.getData() == null) {
                    watchdog.cancel();
                } else {

                    Logger.debug("[Client to Server] Server's IV parameter received successfully");

                    // Sending back the remote secret key
                    clientToServer_sendBackSecretKey(event.getIdentifier(), event.getData());
                }
            }
        }, 10000);
        return watchdog.start();
    }

    private void serverToClient_sendPositiveAcknowledgement(int identifier, byte[] iv) {
        Logger.debug("[Server to Client] Sending positive acknowledgement to the client");

        answer(identifier, SUCCESS_PATTERN, 2000, args -> {
            if (!args.isTimeout()) {

                if (Arrays.equals(SUCCESS_PATTERN, args.response())) {
                    Logger.debug("[Server to Client] Positive acknowledgement received from the client");

                    ivParameterSpec = new IvParameterSpec(iv);
                    success = true;
                }
            }
        });
    }

    private void clientToServer_sendBackSecretKey(int identifier, byte[] iv) {
        Logger.debug("[Client to server] Sending IV parameter to the server");

        answer(identifier, iv, 2000, args -> {
            if (!args.isTimeout()) {
                if (Arrays.equals(SUCCESS_PATTERN, args.response())) {
                    Logger.debug("[Client to Server] Positive acknowledgement received, sending back positive acknowledgement");

                    answer(args.identifier(), SUCCESS_PATTERN);
                    ivParameterSpec = new IvParameterSpec(iv);
                    success = true;
                }
            } else if (args.isConnectionLost()) {
                watchdog.cancel();
            }
        });
    }
}
