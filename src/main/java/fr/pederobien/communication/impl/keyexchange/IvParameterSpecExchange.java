package fr.pederobien.communication.impl.keyexchange;

import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

import javax.crypto.spec.IvParameterSpec;

import fr.pederobien.communication.event.RequestReceivedEvent;
import fr.pederobien.communication.interfaces.IToken;
import fr.pederobien.utils.Watchdog;
import fr.pederobien.utils.Watchdog.WatchdogStakeholder;

public class IvParameterSpecExchange extends Exchange {
	private AlgorithmParameterSpec ivParameterSpec;
	private boolean success;
	private WatchdogStakeholder watchdog;

	/**
	 * Creates a IV exchange for symmetric encoding/decoding.
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
		// Generating a new parameter specification to send
		byte[] iv = new byte[16];
		SecureRandom random = new SecureRandom();
		random.nextBytes(iv);

		send(iv, 2000, args -> {
			if (!args.isTimeout()) {

				// Step 2: Receiving remote parameter specification
				if (Arrays.equals(args.getResponse().getBytes(), iv)) {

					// Sending positive acknowledgement to the client
					serverToClient_sendPositiveAcknowledgement(args.getIdentifier(), iv);
				}
			}
		});

		return success;
	}

	@Override
	protected boolean doClientToServerExchange() throws Exception {
		watchdog = Watchdog.create(() -> {
			while (!success) {

				// Waiting for initialisation to happen successfully
				RequestReceivedEvent event = receive();

				// Connection with the remote has been lost
				if (event.getData() == null) {
					watchdog.cancel();
				} else {

					// Sending back the remote secret key
					clientToServer_sendBackSecretKey(event.getIdentifier(), event.getData());
				}
			}
		}, 10000);
		return watchdog.start();
	}

	private void serverToClient_sendPositiveAcknowledgement(int identifier, byte[] iv) {
		answer(identifier, SUCCESS_PATTERN, 2000, args -> {
			if (!args.isTimeout()) {

				if (Arrays.equals(SUCCESS_PATTERN, args.getResponse().getBytes())) {
					ivParameterSpec = new IvParameterSpec(iv);
					success = true;
				}
			}
		});
	}

	private void clientToServer_sendBackSecretKey(int identifier, byte[] iv) {
		answer(identifier, iv, 2000, args -> {
			if (!args.isTimeout()) {
				if (Arrays.equals(SUCCESS_PATTERN, args.getResponse().getBytes())) {
					answer(args.getIdentifier(), SUCCESS_PATTERN);
					ivParameterSpec = new IvParameterSpec(iv);
					success = true;
				}
			} else if (args.isConnectionLost()) {
				watchdog.cancel();
			}
		});
	}
}
