package fr.pederobien.communication.impl.keyexchange;

import java.util.Arrays;

import javax.crypto.SecretKey;

import fr.pederobien.communication.event.RequestReceivedEvent;
import fr.pederobien.communication.interfaces.IToken;
import fr.pederobien.utils.Watchdog;
import fr.pederobien.utils.Watchdog.WatchdogStakeholder;

public class SymmetricKeyExchange extends Exchange {
	private SymmetricKeyManager keyManager;
	private int timeout;
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
		send(keyManager.generateKey().getEncoded(), timeout, args -> {
			if (!args.isTimeout()) {

				// Extracting client secret key
				if (keyManager.parse(args.getResponse().getBytes())) {

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

			// Waiting for initialisation to happen successfully
			RequestReceivedEvent event = receive();

			// Connection with the remote has been lost
			if (event.getData() == null) {
				watchdog.cancel();
			}

			// Parsing remote secret key
			else if (keyManager.parse(event.getData())) {

				// Sending back the remote secret key
				clientToServer_sendBackSecretKey(event.getIdentifier(), event.getData());
			}
		}, 10000);
		return watchdog.start();
	}

	private void serverToClient_sendPositiveAcknowledgement(int identifier) {
		answer(identifier, SUCCESS_PATTERN, timeout, args -> {
			if (!args.isTimeout()) {
				if (Arrays.equals(SUCCESS_PATTERN, args.getResponse().getBytes())) {
					success = true;
				}
			}
		});
	}

	private void clientToServer_sendBackSecretKey(int identifier, byte[] remoteKey) {
		answer(identifier, remoteKey, timeout, args -> {
			if (!args.isTimeout()) {
				if (Arrays.equals(SUCCESS_PATTERN, args.getResponse().getBytes())) {
					answer(args.getIdentifier(), SUCCESS_PATTERN);
					success = true;
				}
			} else {
				watchdog.cancel();
			}
		});
	}
}
