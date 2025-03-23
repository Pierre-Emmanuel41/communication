package fr.pederobien.communication.impl.keyexchange;

import fr.pederobien.communication.impl.layer.AesLayer;
import fr.pederobien.communication.interfaces.IToken;
import fr.pederobien.communication.interfaces.layer.ICertificate;
import fr.pederobien.communication.interfaces.layer.ILayer;

public class AesKeyExchange {
	private ICertificate certificate;
	private SymmetricKeyExchange keyExchange;
	private IvParameterSpecExchange ivExchange;

	/**
	 * Creates a AES key exchange. A size-bits AES secret key will be generated as
	 * well as a 16 bytes IV.
	 * 
	 * @param token       The token to send/receive data from the remote.
	 * @param certificate To sign the data before AES encryption.
	 * @param keySize     This is an algorithm-specific metric, specified in number
	 *                    of bits.
	 * @param timeout     The maximum time, in ms, to wait for remote response
	 *                    during the key exchange.
	 */
	public AesKeyExchange(IToken token, ICertificate certificate, int keySize, int timeout) {
		this.certificate = certificate;
		keyExchange = new SymmetricKeyExchange(token, new AesKeyManager(keySize), timeout);
		ivExchange = new IvParameterSpecExchange(token);
	}

	/**
	 * Perform the key exchange with the remote.
	 * 
	 * @return True if the key exchange succeed, false otherwise.
	 */
	public ILayer exchange() {
		if (!keyExchange.exchange() || !ivExchange.exchange()) {
			return null;
		}

		return new AesLayer(certificate, keyExchange.getRemoteKey(), ivExchange.getIvParameterSpec());
	}
}
