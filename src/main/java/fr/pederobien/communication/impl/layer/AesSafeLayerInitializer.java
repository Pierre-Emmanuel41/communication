package fr.pederobien.communication.impl.layer;

import fr.pederobien.communication.impl.keyexchange.AesKeyExchange;
import fr.pederobien.communication.impl.keyexchange.RsaKeyExchange;
import fr.pederobien.communication.interfaces.layer.ICertificate;

public class AesSafeLayerInitializer extends LayerInitializer {

	/**
	 * Creates a safe AES key exchange. This initializer perform first an RSA key exchange in order to securely perform an
	 * AES key exchange.
	 * 
	 * @param certificate The sign/authenticate the public key during RSA key exchange and to sign/authenticate data before
	 *                    performing the AES encryption.
	 * @param rsaKeySize This is analgorithm-specific metric, such as modulus length, specified in number of bits.
	 * @param aesKeySize This is an algorithm-specific metric, specified in number of bits.
	 */
	public AesSafeLayerInitializer(ICertificate certificate, int rsaKeySize, int aesKeySize) {
		super(new CertifiedLayer(certificate),
				token -> new RsaKeyExchange(token, rsaKeySize).exchange(),
				token -> new AesKeyExchange(token, certificate, aesKeySize).exchange());
	}
	
	/**
	 * Creates a safe AES key exchange. This initializer perform first an RSA key exchange in order to securely perform an
	 * AES key exchange.
	 * 
	 * @param certificate The sign/authenticate the public key during RSA key exchange and to sign/authenticate data before
	 *                    performing the AES encryption.
	 */
	public AesSafeLayerInitializer(ICertificate certificate) {
		this(certificate, 2048, 128);
	}
}
