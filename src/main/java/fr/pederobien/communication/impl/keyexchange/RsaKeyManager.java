package fr.pederobien.communication.impl.keyexchange;

import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.function.Function;

public class RsaKeyManager extends AsymetricKeyManager {
	private KeyPairGenerator keyGenerator;
	private Function<byte[], PublicKey> keyParser;

	/**
	 * Creates a key manager associated to the RSA algorithm
	 * 
	 * @param keysize the keysize. This is an algorithm-specific metric, such as
	 *                modulus length, specified in number of bits.
	 */
	public RsaKeyManager(int keySize) {
		try {
			keyGenerator = KeyPairGenerator.getInstance("RSA");
			keyGenerator.initialize(keySize);

			keyParser = data -> {
				try {
					return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(data));
				} catch (Exception e) {
					return null;
				}
			};
		} catch (NoSuchAlgorithmException e) {
			// Do nothing
		}
	}

	@Override
	protected KeyPairGenerator getKeyGenerator() {
		return keyGenerator;
	}

	@Override
	protected Function<byte[], PublicKey> getKeyParser() {
		return keyParser;
	}

}
