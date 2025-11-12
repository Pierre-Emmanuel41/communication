package fr.pederobien.communication.impl.layer;

import fr.pederobien.communication.impl.keyexchange.AesKeyExchange;
import fr.pederobien.communication.impl.keyexchange.RsaKeyExchange;
import fr.pederobien.communication.interfaces.layer.ICertificate;

public class AesSafeLayerInitializer extends LayerInitializer {

	/**
	 * Creates a safe AES key exchange. This initializer perform first an RSA key exchange in order to securely perform an AES key
	 * exchange.
	 *
	 * @param certificate The sign/authenticate the public key during RSA key exchange and to sign/authenticate data before performing
	 *                    the AES encryption.
	 * @param rsaKeySize  This is an algorithm-specific metric, such as modulus length, specified in number of bits.
	 * @param aesKeySize  This is an algorithm-specific metric, specified in number of bits.
	 * @param delay       The time, in ms, to wait before sending server's public key, server's secret key and sending server's IV.
	 * @param timeout     The maximum time, in ms, to wait for remote response during the key exchange.
	 */
	public AesSafeLayerInitializer(ICertificate certificate, int rsaKeySize, int aesKeySize, int delay, int timeout) {
		super(new CertifiedLayer(certificate), token -> new RsaKeyExchange(token, rsaKeySize, delay, timeout).exchange(),
				token -> new AesKeyExchange(token, certificate, aesKeySize, delay, timeout).exchange());
	}

	/**
	 * Creates a safe AES key exchange. This initializer perform first an RSA key exchange in order to securely perform an AES key
	 * exchange.
	 *
	 * @param certificate The sign/authenticate the public key during RSA key exchange and to sign/authenticate data before performing
	 *                    the AES encryption.
	 * @param delay       The time, in ms, to wait before sending server's public key, server's secret key and sending server's IV.
	 * @param timeout     The maximum time, in ms, to wait for remote response during the key exchange.
	 */
	public AesSafeLayerInitializer(ICertificate certificate, int delay, int timeout) {
		this(certificate, 2048, 128, delay, timeout);
	}

	/**
	 * Creates a safe AES key exchange. This initializer perform first an RSA key exchange in order to securely perform an AES key
	 * exchange.
	 *
	 * @param certificate The sign/authenticate the public key during RSA key exchange and to sign/authenticate data before performing
	 *                    the AES encryption.
	 * @param delay       The time, in ms, to wait before sending server's public key, server's secret key and sending server's IV.
	 */
	public AesSafeLayerInitializer(ICertificate certificate, int delay) {
		this(certificate, delay, 2000);
	}

	/**
	 * Creates a safe AES key exchange. This initializer perform first an RSA key exchange in order to securely perform an AES key
	 * exchange.
	 *
	 * @param certificate The sign/authenticate the public key during RSA key exchange and to sign/authenticate data before performing
	 *                    the AES encryption.
	 * @param delay       The time, in ms, to wait before sending server's public key, server's secret key and sending server's IV.
	 */
	public AesSafeLayerInitializer(ICertificate certificate) {
		this(certificate, 500);
	}
}
