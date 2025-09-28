package fr.pederobien.communication.impl.keyexchange;

import fr.pederobien.communication.event.MessageEvent;
import fr.pederobien.communication.interfaces.IToken;
import fr.pederobien.utils.Watchdog;
import fr.pederobien.utils.Watchdog.WatchdogStakeholder;
import fr.pederobien.utils.event.Logger;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;

public class AsymmetricKeyExchange extends Exchange {
	private final AsymmetricKeyManager keyManager;
	private final int timeout;
	private boolean success;
	private WatchdogStakeholder watchdog;

	/**
	 * Creates a key exchange for asymmetric encoding/decoding.
	 *
	 * @param token      The token used to send/receive data from the remote.
	 * @param keyManager The manager that generates a key-pair and parse remote public key.
	 * @param timeout    The maximum time, in ms, to wait for remote response during the key exchange.
	 */
	public AsymmetricKeyExchange(IToken token, AsymmetricKeyManager keyManager, int timeout) {
		super(token);
		this.keyManager = keyManager;
		this.timeout = timeout;

		success = false;
	}

	@Override
	protected boolean doServerToClientExchange() throws Exception {
		Logger.debug("[Server to Client] Generating key pair and sending public key to the client");

		send(keyManager.generatePair().getEncoded(), timeout, args -> {
			if (!args.isTimeout()) {

				// Extracting client public key
				if (keyManager.parse(args.response()) != null) {

					Logger.debug("[Server to Client] Client's public key received successfully");

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

			Logger.debug("[Client to Server] Waiting for server's public key");

			// Waiting for server public key
			MessageEvent event = receive();

			// Connection with the remote has been lost
			if (event.getData() == null) {
				watchdog.cancel();
			}

			// Parsing remote public key
			else if (keyManager.parse(event.getData()) != null) {

				Logger.debug("[Client to Server] Server's public key received successfully");

				// Generating a new key to send
				clientToServer_sendPublicKey(event.getIdentifier(), keyManager.generatePair().getEncoded());
			}
		}, 10000);

		return watchdog.start();
	}

	/**
	 * @return The private key associated to the public key sent to the remote.
	 */
	public PrivateKey getPrivateKey() {
		return keyManager.getPrivateKey();
	}

	/**
	 * @return The public key received from the remote.
	 */
	public PublicKey getRemoteKey() {
		return keyManager.getRemoteKey();
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

	private void clientToServer_sendPublicKey(int identifier, byte[] keyToSend) {
		Logger.debug("[Client to server] Sending public key to the server");

		answer(identifier, keyToSend, timeout, args -> {
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
