package fr.pederobien.communication.impl.keyexchange;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;

import fr.pederobien.communication.event.RequestReceivedEvent;
import fr.pederobien.communication.interfaces.IToken;
import fr.pederobien.utils.AsyncConsole;
import fr.pederobien.utils.Watchdog;
import fr.pederobien.utils.Watchdog.WatchdogStakeholder;

public class AsymmetricKeyExchange extends Exchange {
	private AsymetricKeyManager keyManager;
	private boolean success;
	private WatchdogStakeholder watchdog;

	/**
	 * Creates a key exchange for asymmetric encoding/decoding.
	 * 
	 * @param token      The token used to send/receive data from the remote.
	 * @param keyManager The manager that generates a key-pair and parse remote
	 *                   public key.
	 */
	public AsymmetricKeyExchange(IToken token, AsymetricKeyManager keyManager) {
		super(token);
		this.keyManager = keyManager;

		success = false;
	}

	@Override
	protected boolean doServerToClientExchange() throws Exception {
		AsyncConsole.printlnWithTimeStamp("[Server] Sending public key");
		send(keyManager.generatePair().getEncoded(), 2000, args -> {
			if (!args.isTimeout()) {

				// Extracting client public key
				if (keyManager.parse(args.getResponse().getBytes()) != null) {
					AsyncConsole.printlnWithTimeStamp("[Server] Client public key valid");

					// Sending positive acknowledgement to the client
					serverToClient_sendPositiveAcknowledgement(args.getIdentifier());
				}
			}
		});

		return success;
	}

	@Override
	protected boolean doClientToServerExchange() throws Exception {
		watchdog = Watchdog.create(() -> {
			AsyncConsole.printlnWithTimeStamp("[Client] Waiting for server public key");

			// Waiting for server public key
			RequestReceivedEvent event = receive();

			// Connection with the remote has been lost
			if (event.getData() == null) {
				watchdog.cancel();
			} else {
				// Parsing remote public key
				if (keyManager.parse(event.getData()) != null) {

					AsyncConsole.printlnWithTimeStamp("[Client] Server public key is valid");

					// Generating a new key to send
					clientToServer_sendPublicKey(event.getIdentifier(), keyManager.generatePair().getEncoded());
				}
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
		AsyncConsole.printlnWithTimeStamp("[Server] Sending Positive ackowlegement to client");
		answer(identifier, SUCCESS_PATTERN, 2000, args -> {
			if (!args.isTimeout()) {

				AsyncConsole.printlnWithTimeStamp("[Server] Positive ackowlegement from client received");
				if (Arrays.equals(SUCCESS_PATTERN, args.getResponse().getBytes())) {
					success = true;
				}
			}
		});
	}

	private void clientToServer_sendPublicKey(int identifier, byte[] keyToSend) {
		AsyncConsole.printlnWithTimeStamp("[Client] Sending public key");
		answer(identifier, keyToSend, 2000, args -> {
			if (!args.isTimeout()) {
				if (Arrays.equals(SUCCESS_PATTERN, args.getResponse().getBytes())) {
					AsyncConsole.printlnWithTimeStamp("[Client] Sending positive acknowledgement to server");
					answer(args.getIdentifier(), SUCCESS_PATTERN);
					success = true;
				}
			} else {
				watchdog.cancel();
			}
		});
	}
}
