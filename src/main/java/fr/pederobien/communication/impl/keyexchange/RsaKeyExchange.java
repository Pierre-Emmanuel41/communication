package fr.pederobien.communication.impl.keyexchange;

import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.function.Function;

import fr.pederobien.communication.impl.layer.RsaLayer;
import fr.pederobien.communication.interfaces.IToken;
import fr.pederobien.communication.interfaces.layer.ILayer;

public class RsaKeyExchange {
	private AsymmetricKeyExchange keyExchange;

	/**
	 * Creates a key exchange associated to the RSA algorithm.
	 * 
	 * @param token The token to perform key exchange.
	 */
	public RsaKeyExchange(IToken token) {
		try {
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(2048);
			
			Function<byte[], PublicKey> keyParser = data -> {
				try {
					return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(data));
				} catch (Exception e) {
					return null;
				}
			};
	
			keyExchange = new AsymmetricKeyExchange(token, generator, keyParser);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Perform the key exchange with the remote.
	 * 
	 * @param token The token to send/receive key from the remote.
	 * 
	 * @return True if the key exchange succeed, false otherwise.
	 */
	public ILayer exchange() {
		if (keyExchange != null && keyExchange.exchange())
			return new RsaLayer(keyExchange.getPrivateKey(), keyExchange.getRemoteKey());

		return null;
	}
}
