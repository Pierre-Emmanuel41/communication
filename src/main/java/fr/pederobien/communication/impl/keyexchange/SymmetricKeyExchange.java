package fr.pederobien.communication.impl.keyexchange;

import java.util.Arrays;
import java.util.function.Function;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import fr.pederobien.communication.event.RequestReceivedEvent;
import fr.pederobien.communication.interfaces.IToken;
import fr.pederobien.utils.Watchdog;
import fr.pederobien.utils.Watchdog.WatchdogStakeholder;

public class SymmetricKeyExchange extends Exchange {
	private KeyGenerator generator;
	private Function<byte[], SecretKey> keyParser;
	private boolean success;
	private int counter;
	private WatchdogStakeholder watchdog;
	private SecretKey secretKey;

	/**
	 * Creates a key exchange for symmetric encoding/decoding.
	 * 
	 * @param token The token used to send/receive data from the remote.
	 * @param keyword A keyword to be used while sending the asymmetric key.
	 *        This keyword shall be the same for the client and the server.
	 * @param generator An initialised key pair generator.
	 * @param keyParser A function to parse the bytes corresponding to the remote public key.
	 */
	public SymmetricKeyExchange(IToken token, String keyword, KeyGenerator generator, Function<byte[], SecretKey> keyParser) {
		super(token, keyword);
		this.generator = generator;
		this.keyParser = keyParser;

		success = false;
		counter = 0;
	}

	/**
	 * @return The secret key received from the server.
	 */
	public SecretKey getRemoteKey() {
		return secretKey;
	}

	/**
	 * @return Generate a secretKey to share with the remote.
	 */
	private SecretKey generateKey() {
		return secretKey = generator.generateKey();
	}

	protected boolean doServerToClientExchange() throws Exception {
		// Max three tries to exchange keys
		while (!success && (counter++ < 3)) {

			// Generating a new key to send
			byte[] keyToSend = generateKey().getEncoded();

			// Step 1: Sending key
			send(keyToSend, 2000, args -> {
				if (!args.isTimeout()) {

					// Step 2: Receiving remote key
					unpackAndDo(args.getResponse().getBytes(), data -> {
						if (Arrays.equals(data, keyToSend)) {

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

	protected boolean doClientToServerExchange() throws Exception {
		watchdog = Watchdog.create(() -> {
			while (!success) {
				// Waiting for initialisation to happen successfully
				RequestReceivedEvent event = receive();

				// Connection with the remote has been lost
				if (event.getData() == null)
					watchdog.cancel();
				else {

					// Storing key to be used if verified by server
					byte[] key = event.getData();

					// Data for symmetric exchange
					if (key.length > 0) {
						// Sending back the received key to the remote
						answer(event.getIdentifier(), key, 2000, args -> {
							if (!args.isTimeout()) {

								// Extracting acknowledgement
								unpackAndDo(args.getResponse().getBytes(), ack -> {
									if (Arrays.equals(SUCCESS_PATTERN, ack)) {
										secretKey = keyParser.apply(key);
										success = true;
									}
								});
							}
							else if (args.isConnectionLost())
								watchdog.cancel();
						});
					}
				}
			}
		}, 10000);
		return watchdog.start();
	}
}
