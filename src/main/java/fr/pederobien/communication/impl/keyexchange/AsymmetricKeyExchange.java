package fr.pederobien.communication.impl.keyexchange;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.function.Function;

import fr.pederobien.communication.event.RequestReceivedEvent;
import fr.pederobien.communication.impl.connection.Message;
import fr.pederobien.communication.interfaces.IConnection.Mode;
import fr.pederobien.communication.interfaces.IToken;
import fr.pederobien.utils.Watchdog;
import fr.pederobien.utils.Watchdog.WatchdogStakeholder;

public class AsymmetricKeyExchange {
	public static final byte[] SUCCESS_PATTERN = "SUCCESS_PATTERN".getBytes();

	private IToken token;
	private KeyPairGenerator generator;
	private Function<byte[], PublicKey> keyParser;
	private boolean success;
	private WatchdogStakeholder watchdog;
	private PrivateKey privateKey;
	private PublicKey remoteKey;

	/**
	 * Creates a key exchange for asymmetric encoding/decoding.
	 * 
	 * @param mode The direction of the communication.
	 * @param generator An initialised key pair generator.
	 * @param keyParser A function to parse the bytes corresponding to the remote public key.
	 */
	public AsymmetricKeyExchange(IToken token, KeyPairGenerator generator, Function<byte[], PublicKey> keyParser) {
		this.token = token;
		this.generator = generator;
		this.keyParser = keyParser;
	}

	/**
	 * Perform the key exchange with the remote.
	 * 
	 * @param token The token to send/receive key from the remote.
	 * 
	 * @return True if the key exchange succeed, false otherwise.
	 */
	public boolean exchange() {
		try {
			if (token.getMode() == Mode.CLIENT_TO_SERVER)
				return keyExchangeClientToServer();
			else if (token.getMode() == Mode.SERVER_TO_CLIENT)
				return keyExchangeServerToClient();
		} catch (Exception e) {
			// do nothing
		}

		return false;
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
	
	private boolean keyExchangeServerToClient() {
		// Max three tries to exchange keys
		int counter = 0;
		while (!success && (counter++ < 3)) {

			// Generating a new key to send
			PublicKey keyToSend = generatePair();

			// Step 1: Sending key
			token.send(new Message(keyToSend.getEncoded(), true, 2000, args -> {
				if (!args.isTimeout()) {

					// Step 2: Receiving remote key
					remoteKey = keyParser.apply(args.getResponse().getBytes());
					if (getRemoteKey() != null) {

						// Step 3: Sending positive acknowledgement
						token.answer(args.getIdentifier(), new Message(SUCCESS_PATTERN, true));
						success = true;
					}
				}
			}));
		}

		return success;
	}
	
	private boolean keyExchangeClientToServer() throws Exception {
		watchdog = Watchdog.create(() -> {
			while (!success) {

				// Waiting for initialisation to happen successfully
				RequestReceivedEvent event = token.receive();
				
				// Connection with the remote has been lost
				if (event.getData() == null)
					watchdog.cancel();
				else {
					remoteKey = keyParser.apply(event.getData());
					if (getRemoteKey() != null) {

						PublicKey keyToSend = generatePair();
						// Sending public key to remote
						event.getConnection().answer(event.getIdentifier(), new Message(keyToSend.getEncoded(), true, 2000, args -> {
							if (!args.isTimeout() && Arrays.equals(SUCCESS_PATTERN, args.getResponse().getBytes()))
								success = true;
						}));
					}
				}
			}
		}, 10000);

		return watchdog.start();
	}
}
