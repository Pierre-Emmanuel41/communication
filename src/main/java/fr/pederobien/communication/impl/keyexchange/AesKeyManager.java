package fr.pederobien.communication.impl.keyexchange;

import java.security.NoSuchAlgorithmException;
import java.util.function.Function;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AesKeyManager extends SymmetricKeyManager {
	private KeyGenerator keyGenerator;
	private Function<byte[], SecretKey> keyParser;

	/**
	 * Creates a key manager associated to the AES algorithm
	 * 
	 * @param keysize the keysize. This is an algorithm-specific metric, such as
	 *                modulus length, specified in number of bits.
	 */
	public AesKeyManager(int keySize) {
		try {
			keyGenerator = KeyGenerator.getInstance("AES");
			keyGenerator.init(keySize);

			keyParser = data -> {
				return new SecretKeySpec(data, 0, data.length, "AES");
			};
		} catch (NoSuchAlgorithmException e) {
			// Do nothing
		}
	}

	@Override
	protected KeyGenerator getKeyGenerator() {
		return keyGenerator;
	}

	@Override
	protected Function<byte[], SecretKey> getKeyParser() {
		return keyParser;
	}

}
