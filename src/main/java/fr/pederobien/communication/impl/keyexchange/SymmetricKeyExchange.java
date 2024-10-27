package fr.pederobien.communication.impl.keyexchange;

import java.util.Arrays;
import java.util.function.Function;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import fr.pederobien.communication.event.RequestReceivedEvent;
import fr.pederobien.communication.impl.connection.Message;
import fr.pederobien.communication.interfaces.IToken;
import fr.pederobien.communication.interfaces.connection.IConnection.Mode;
import fr.pederobien.utils.Watchdog;
import fr.pederobien.utils.Watchdog.WatchdogStakeholder;

public class SymmetricKeyExchange {
	public static final byte[] SUCCESS_PATTERN = "SUCCESS_PATTERN".getBytes();

	private IToken token;
	private KeyGenerator generator;
	private Function<byte[], SecretKey> keyParser;
	private boolean success;
	private WatchdogStakeholder watchdog;
	private SecretKey secretKey;
	
	/**
	 * Creates a key exchange for symmetric encoding/decoding.
	 * 
	 * @param mode The direction of the communication.
	 * @param generator An initialised key generator.
	 * @param keyParser A function to parse the bytes corresponding to the remote key.
	 */
	public SymmetricKeyExchange(IToken token, KeyGenerator generator, Function<byte[], SecretKey> keyParser) {
		this.token = token;
		this.generator = generator;
		this.keyParser = keyParser;
	}
	
	/**
	 * Perform the key exchange with the remote.
	 * 
	 * @return True if the key exchange succeed, false otherwise.
	 */
	public boolean exchange() throws Exception {
		if (token.getMode() == Mode.CLIENT_TO_SERVER)
			return keyExchangeClientToServer();
		else if (token.getMode() == Mode.SERVER_TO_CLIENT)
			return keyExchangeServerToClient();

		return false;
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
	
	private boolean keyExchangeServerToClient() throws Exception {
		// Max three tries to exchange keys
		int counter = 0;
		while (!success && (counter++ < 3)) {

			// Generating a new key to send
			SecretKey keyToSend = generateKey();

			// Step 1: Sending key
			token.send(new Message(keyToSend.getEncoded(), true, 10000, args -> {
				if (!args.isTimeout()) {

					// Step 2: Receiving remote key
					SecretKey received = keyParser.apply(args.getResponse().getBytes());
					if (received != null && received.equals(keyToSend)) {

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
					secretKey = keyParser.apply(event.getData());
					if (secretKey != null) {

						// Sending back the received key to remote
						event.getConnection().answer(event.getIdentifier(), new Message(secretKey.getEncoded(), true, 10000, args -> {
							if (!args.isTimeout() && Arrays.equals(SUCCESS_PATTERN, args.getResponse().getBytes()))
								success = true;
						}));
					}
				}
			}
		}, 35000);
		return watchdog.start();
	}
}
