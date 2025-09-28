package fr.pederobien.communication.impl.keyexchange;

import fr.pederobien.communication.impl.layer.RsaLayer;
import fr.pederobien.communication.interfaces.IToken;
import fr.pederobien.communication.interfaces.layer.ILayer;

public class RsaKeyExchange {
	private final AsymmetricKeyExchange keyExchange;

	/**
	 * Creates a key exchange associated to the RSA algorithm.
	 *
	 * @param token   The token to perform key exchange.
	 * @param keySize This is an algorithm-specific metric, such as modulus length, specified in number of bits.
	 * @param timeout The maximum time, in ms, to wait for remote response during the key exchange.
	 */
	public RsaKeyExchange(IToken token, int keySize, int timeout) {
		keyExchange = new AsymmetricKeyExchange(token, new RsaKeyManager(keySize), timeout);
	}

	/**
	 * Perform the key exchange with the remote.
	 *
	 * @return An RSA layer if the key exchange succeed, null otherwise.
	 */
	public ILayer exchange() {
		if (!keyExchange.exchange()) {
			return null;
		}

		return new RsaLayer(keyExchange.getPrivateKey(), keyExchange.getRemoteKey());
	}
}
