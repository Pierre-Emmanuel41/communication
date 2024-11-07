package fr.pederobien.communication.impl.layer;

import fr.pederobien.communication.impl.keyexchange.RsaKeyExchange;
import fr.pederobien.communication.interfaces.layer.ICertificate;

public class RsaLayerInitializer extends LayerInitializer {

	/**
	 * Creates a layer initializer in order to perform public key exchange before using an RSA layer.
	 * 
	 * @param certificate The certificate used to sign/authenticate the remote key received from the
	 *                    remote.
	 * @param keySize The key size. This is analgorithm-specific metric, such as modulus length,
	 *                specified in number of bits.
	 */
	public RsaLayerInitializer(ICertificate certificate, int keySize) {
		super(new CertifiedLayer(certificate), token -> new RsaKeyExchange(token, keySize).exchange());
	}
	
	/**
	 * Creates a layer initializer in order to perform public key exchange before using an RSA layer.
	 * 
	 * @param certificate The certificate used to sign/authenticate the remote key received from the
	 *                    remote.
	 */
	public RsaLayerInitializer(ICertificate certificate) {
		this(certificate, 2048);
	}
}
