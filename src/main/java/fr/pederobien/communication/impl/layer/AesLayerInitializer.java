package fr.pederobien.communication.impl.layer;

import fr.pederobien.communication.impl.keyexchange.AesKeyExchange;
import fr.pederobien.communication.interfaces.layer.ICertificate;

public class AesLayerInitializer extends LayerInitializer {

	/**
	 * Creates a layer initializer in order to perform secret key exchange before using an AES layer.
	 * 
	 * @param certificate The certificate used to sign/authenticate the secret key received from the
	 *                    remote as well as the data to send to the remote.
	 * @param keySize The size, in bits, of the AES key.
	 */
	public AesLayerInitializer(ICertificate certificate, int keySize) {
		super(new CertifiedLayer(certificate), token -> new AesKeyExchange(token, certificate, keySize).exchange());
	}

	/**
	 * Creates a layer initializer in order to perform secret key exchange (128 bits) before using an AES layer.
	 * 
	 * @param certificate The certificate used to sign/authenticate the secret key received from the
	 *                    remote as well as the data to send to the remote.
	 */
	public AesLayerInitializer(ICertificate certificate) {
		this(certificate, 128);
	}
}
