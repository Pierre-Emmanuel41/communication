package fr.pederobien.communication.impl.keyexchange;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;

import fr.pederobien.communication.event.MessageEvent;
import fr.pederobien.communication.interfaces.IToken;
import fr.pederobien.utils.Watchdog;
import fr.pederobien.utils.Watchdog.WatchdogStakeholder;

public class AsymmetricKeyExchange extends Exchange {
	private AsymetricKeyManager keyManager;
	private int timeout;
	private boolean success;
	private WatchdogStakeholder watchdog;

	/**
	 * Creates a key exchange for asymmetric encoding/decoding.
	 * 
	 * @param token      The token used to send/receive data from the remote.
	 * @param keyManager The manager that generates a key-pair and parse remote
	 *                   public key.
	 * @param timeout    The maximum time, in ms, to wait for remote response during
	 *                   the key exchange.
	 */
	public AsymmetricKeyExchange(IToken token, AsymetricKeyManager keyManager, int timeout) {
		super(token);
		this.keyManager = keyManager;
		this.timeout = timeout;

		success = false;
	}

	@Override
	protected boolean doServerToClientExchange() throws Exception {
		send(keyManager.generatePair().getEncoded(), timeout, args -> {
			if (!args.isTimeout()) {

				// Extracting client public key
				if (keyManager.parse(args.getResponse()) != null) {

					// Sending positive acknowledgement to the client
					serverToClient_sendPositiveAcknowledgement(args.getIdentifier());
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

			// Waiting for server public key
			MessageEvent event = receive();

			// Connection with the remote has been lost
			if (event.getData() == null) {
				watchdog.cancel();
			}

			// Parsing remote public key
			else if (keyManager.parse(event.getData()) != null) {

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
		answer(identifier, SUCCESS_PATTERN, timeout, args -> {
			if (!args.isTimeout()) {
				if (Arrays.equals(SUCCESS_PATTERN, args.getResponse())) {
					success = true;
				}
			}
		});
	}

	private void clientToServer_sendPublicKey(int identifier, byte[] keyToSend) {
		answer(identifier, keyToSend, timeout, args -> {
			if (!args.isTimeout()) {
				if (Arrays.equals(SUCCESS_PATTERN, args.getResponse())) {
					answer(args.getIdentifier(), SUCCESS_PATTERN);
					success = true;
				}
			} else {
				watchdog.cancel();
			}
		});
	}
}
