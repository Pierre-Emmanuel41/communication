package fr.pederobien.communication.impl.keyexchange;

import fr.pederobien.communication.impl.layer.RsaLayer;
import fr.pederobien.communication.interfaces.IToken;
import fr.pederobien.communication.interfaces.layer.ILayer;

public class RsaKeyExchange {
	private AsymmetricKeyExchange keyExchange;

	/**
	 * Creates a key exchange associated to the RSA algorithm.
	 * 
	 * @param token   The token to perform key exchange.
	 * @param keySize This is analgorithm-specific metric, such as modulus length,
	 *                specified in number of bits.
	 */
	public RsaKeyExchange(IToken token, int keySize) {
		keyExchange = new AsymmetricKeyExchange(token, new RsaKeyManager(keySize));
	}

	/**
	 * Perform the key exchange with the remote.
	 * 
	 * @return True if the key exchange succeed, false otherwise.
	 */
	public ILayer exchange() {
		if (!keyExchange.exchange()) {
			return null;
		}

		return new RsaLayer(keyExchange.getPrivateKey(), keyExchange.getRemoteKey());
	}
}
