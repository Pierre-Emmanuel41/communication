package fr.pederobien.communication.impl.keyexchange;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.function.Function;

import fr.pederobien.communication.event.RequestReceivedEvent;
import fr.pederobien.communication.interfaces.IToken;
import fr.pederobien.utils.Watchdog;
import fr.pederobien.utils.Watchdog.WatchdogStakeholder;

public class AsymmetricKeyExchange extends Exchange {
	private KeyPairGenerator generator;
	private Function<byte[], PublicKey> keyParser;
	private boolean success;
	private WatchdogStakeholder watchdog;
	private PrivateKey privateKey;
	private PublicKey remoteKey;

	/**
	 * Creates a key exchange for asymmetric encoding/decoding.
	 * 
	 * @param token The token used to send/receive data from the remote.
	 * @param keyword A keyword to be used while sending the asymmetric key.
	 *        This keyword shall be the same for the client and the server.
	 * @param generator An initialised key pair generator.
	 * @param keyParser A function to parse the bytes corresponding to the remote public key.
	 */
	public AsymmetricKeyExchange(IToken token, String keyword, KeyPairGenerator generator, Function<byte[], PublicKey> keyParser) {
		super(token, keyword);
		this.generator = generator;
		this.keyParser = keyParser;

		success = false;
	}

	/**
	 * @return The private key associated to the public key sent to the remote.
	 */
	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	/**
	 * @return The public key received from the remote.
	 */
	public PublicKey getRemoteKey() {
		return remoteKey;
	}

	/**
	 * Generates a public/private key pair.
	 * 
	 * @return The public key.
	 */
	private PublicKey generatePair() {
		KeyPair pair = generator.generateKeyPair();

		privateKey = pair.getPrivate();
		return pair.getPublic();
	}

	protected boolean doServerToClientExchange() throws Exception {
		// Max three tries to exchange keys
		int counter = 0;
		while (!success && (counter++ < 3)) {

			// Generating a new key to send
			byte[] keyToSend = generatePair().getEncoded();

			// Step 1: Sending key
			send(keyToSend, 2000, args -> {
				if (!args.isTimeout()) {

					// Step 2: Receiving remote key
					unpackAndDo(args.getResponse().getBytes(), data -> {

						// Parsing remote public key
						remoteKey = keyParser.apply(data);

						if (getRemoteKey() != null) {

							// Step 3: Sending positive acknowledgement
							answer(args.getIdentifier(), SUCCESS_PATTERN);
							success = true;
						}
					});
				}
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

					// Extracting key
					remoteKey = keyParser.apply(event.getData());

					if (getRemoteKey() != null) {

						// Generating a new key to send
						PublicKey keyToSend = generatePair();

						// Sending public key to remote
						answer(event.getIdentifier(), keyToSend.getEncoded(), 2000, args -> {
							if (!args.isTimeout()) {

								// Extracting acknowledgement
								unpackAndDo(args.getResponse().getBytes(), ack -> {
									if (Arrays.equals(SUCCESS_PATTERN, ack))
										success = true;
								});
							}
						});
					}
				}
			}
		}, 10000);

		return watchdog.start();
	}
}
