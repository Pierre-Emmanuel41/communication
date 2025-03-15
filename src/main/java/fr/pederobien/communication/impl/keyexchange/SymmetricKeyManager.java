package fr.pederobien.communication.impl.keyexchange;

import java.util.function.Function;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public abstract class SymmetricKeyManager {
	private SecretKey secretKey;

	/**
	 * @return Generate a secretKey to share with the remote.
	 */
	public SecretKey generateKey() {
		return secretKey = getKeyGenerator().generateKey();
	}

	/**
	 * Parse the byte array using a defined key parser.
	 * 
	 * @param remoteKey The bytes array that contains the secret key to parse.
	 * 
	 * @return True if the remote key equals the generated internally secret key.
	 */
	public boolean parse(byte[] remoteKey) {
		SecretKey secret = getKeyParser().apply(remoteKey);
		if (secretKey != null) {
			return secretKey.equals(secret);
		}

		secretKey = secret;
		return secret != null;
	}

	/**
	 * @return The secret key.
	 */
	public SecretKey getSecretKey() {
		return secretKey;
	}

	/**
	 * @return The generator used to generate a secret key.
	 */
	protected abstract KeyGenerator getKeyGenerator();

	/**
	 * @return The parser used to parse the remote public key.
	 */
	protected abstract Function<byte[], SecretKey> getKeyParser();
}
