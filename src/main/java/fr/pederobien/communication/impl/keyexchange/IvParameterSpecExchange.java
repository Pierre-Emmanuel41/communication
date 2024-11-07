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
	private int counter;
	private WatchdogStakeholder watchdog;

	/**
	 * Creates a IV exchange for symmetric encoding/decoding.
	 * 
	 * @param token The token used to send/receive data from the remote.
	 * @param keyword A keyword to be used while sending the asymmetric key.
	 *        This keyword shall be the same for the client and the server.
	 */
	public IvParameterSpecExchange(IToken token, String keyword) {
		super(token, keyword);

		success = false;
		counter = 0;
	}

	/**
	 * @return The IV parameter spec shared between the client and the server.
	 */
	public AlgorithmParameterSpec getIvParameterSpec() {
		return ivParameterSpec;
	}

	@Override
	protected boolean doServerToClientExchange() throws Exception {
		// Max three tries to exchange keys
		while (!success && (counter++ < 3)) {

			// Generating a new parameter specification to send
			byte[] iv = new byte[16];
			SecureRandom random = new SecureRandom();
			random.nextBytes(iv);

			// Step 1: Sending parameter specification
			send(iv, 2000, args -> {
				if (!args.isTimeout()) {

					// Step 2: Receiving remote parameter specification
					unpackAndDo(args.getResponse().getBytes(), data -> {
						if (Arrays.equals(data, iv)) {

							ivParameterSpec = new IvParameterSpec(iv);

							// Step 3: Sending positive acknowledgement
							answer(args.getIdentifier(), SUCCESS_PATTERN);
							success = true;
						}
					});
				}
				else if (args.isConnectionLost())
					counter = 3;
			});
		}

		return success;
	}

	@Override
	protected boolean doClientToServerExchange() throws Exception {
		watchdog = Watchdog.create(() -> {
			while (!success) {

				// Waiting for initialisation to happen successfully
				RequestReceivedEvent event = receive();

				// Connection with the remote has been lost
				if (event.getData() == null)
					watchdog.cancel();
				else {

					// Extracting IV
					byte[] iv = event.getData();

					// Sending back the received key to remote
					answer(event.getIdentifier(), iv, 2000, args -> {
						if (!args.isTimeout()) {

							// Extracting acknowledgement
							unpackAndDo(args.getResponse().getBytes(), ack -> {
								if (Arrays.equals(SUCCESS_PATTERN, ack)) {
									ivParameterSpec = new IvParameterSpec(iv);
									success = true;
								}
							});
						}
						else if (args.isConnectionLost())
							watchdog.cancel();
					});
				}
			}
		}, 10000);
		return watchdog.start();
	}
}
