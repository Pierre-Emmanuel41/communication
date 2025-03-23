package fr.pederobien.communication.impl.layer;

import fr.pederobien.communication.impl.keyexchange.AesKeyExchange;
import fr.pederobien.communication.interfaces.layer.ICertificate;

public class AesLayerInitializer extends LayerInitializer {

	/**
	 * Creates a layer initializer in order to perform secret key exchange before
	 * using an AES layer.
	 * 
	 * @param certificate The certificate used to sign/authenticate the secret key
	 *                    received from the remote as well as the data to send to
	 *                    the remote.
	 * @param keySize     The size, in bits, of the AES key.
	 * @param timeout     The maximum time, in ms, to wait for remote response
	 *                    during the key exchange.
	 */
	public AesLayerInitializer(ICertificate certificate, int keySize, int timeout) {
		super(new CertifiedLayer(certificate),
				token -> new AesKeyExchange(token, certificate, keySize, timeout).exchange());
	}

	/**
	 * Creates a layer initializer in order to perform secret key exchange (128
	 * bits) before using an AES layer.
	 * 
	 * @param certificate The certificate used to sign/authenticate the secret key
	 *                    received from the remote as well as the data to send to
	 *                    the remote.
	 * @param timeout     The maximum time, in ms, to wait for remote response
	 *                    during the key exchange.
	 */
	public AesLayerInitializer(ICertificate certificate, int timeout) {
		this(certificate, 128, timeout);
	}

	/**
	 * Creates a layer initializer in order to perform secret key exchange (128
	 * bits) before using an AES layer.
	 * 
	 * @param certificate The certificate used to sign/authenticate the secret key
	 *                    received from the remote as well as the data to send to
	 *                    the remote.
	 */
	public AesLayerInitializer(ICertificate certificate) {
		this(certificate, 2000);
	}
}
