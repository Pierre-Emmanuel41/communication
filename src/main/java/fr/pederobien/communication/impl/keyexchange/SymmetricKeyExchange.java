package fr.pederobien.communication.impl.keyexchange;

import java.util.Arrays;

import javax.crypto.SecretKey;

import fr.pederobien.communication.event.RequestReceivedEvent;
import fr.pederobien.communication.interfaces.IToken;
import fr.pederobien.utils.AsyncConsole;
import fr.pederobien.utils.Watchdog;
import fr.pederobien.utils.Watchdog.WatchdogStakeholder;

public class SymmetricKeyExchange extends Exchange {
	private SymmetricKeyManager keyManager;
	private boolean success;
	private WatchdogStakeholder watchdog;

	/**
	 * Creates a key exchange for symmetric encoding/decoding.
	 * 
	 * @param token      The token used to send/receive data from the remote.
	 * @param keyManager The manager that generates a secret key and parse remote
	 *                   secret key.
	 */
	public SymmetricKeyExchange(IToken token, SymmetricKeyManager keyManager) {
		super(token);
		this.keyManager = keyManager;

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
		AsyncConsole.printlnWithTimeStamp("[Server] Sending secret key");
		send(keyManager.generateKey().getEncoded(), 2000, args -> {
			if (!args.isTimeout()) {

				// Extracting client secret key
				if (keyManager.parse(args.getResponse().getBytes())) {
					AsyncConsole.printlnWithTimeStamp("[Server] Client secret key valid");

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
			AsyncConsole.printlnWithTimeStamp("[Client] Waiting for server secret key");

			// Waiting for initialisation to happen successfully
			RequestReceivedEvent event = receive();

			// Connection with the remote has been lost
			if (event.getData() == null) {
				watchdog.cancel();
			} else {

				if (keyManager.parse(event.getData())) {

					AsyncConsole.printlnWithTimeStamp("[Client] Server secret key received");

					// Sending back the remote secret key
					clientToServer_sendBackSecretKey(event.getIdentifier(), event.getData());
				}
			}
		}, 10000);
		return watchdog.start();
	}

	private void serverToClient_sendPositiveAcknowledgement(int identifier) {
		AsyncConsole.printlnWithTimeStamp("[Server] Sending Positive ackowlegement to client");
		answer(identifier, SUCCESS_PATTERN, 2000, args -> {
			if (!args.isTimeout()) {

				if (Arrays.equals(SUCCESS_PATTERN, args.getResponse().getBytes())) {
					AsyncConsole.printlnWithTimeStamp("[Server] Positive ackowlegement from client received");
					success = true;
				}
			}
		});
	}

	private void clientToServer_sendBackSecretKey(int identifier, byte[] remoteKey) {
		AsyncConsole.printlnWithTimeStamp("[Client] Sending server secret key back");
		answer(identifier, remoteKey, 2000, args -> {
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
