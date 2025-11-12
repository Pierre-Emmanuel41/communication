package fr.pederobien.communication.impl.layer;

import fr.pederobien.communication.impl.keyexchange.AesKeyExchange;
import fr.pederobien.communication.interfaces.layer.ICertificate;

public class AesLayerInitializer extends LayerInitializer {

	/**
	 * Creates a layer initializer in order to perform secret key exchange before using an AES layer.
	 *
	 * @param certificate The certificate used to sign/authenticate the secret key received from the remote as well as the data to
	 *                    send to the remote.
	 * @param keySize     The size, in bits, of the AES key.
	 * @param delay       The time, in ms, to wait before sending server's secret key and server's IV.
	 * @param timeout     The maximum time, in ms, to wait for remote response during the key exchange.
	 */
	public AesLayerInitializer(ICertificate certificate, int keySize, int delay, int timeout) {
		super(new CertifiedLayer(certificate), token -> new AesKeyExchange(token, certificate, keySize, delay, timeout).exchange());
	}

	/**
	 * Creates a layer initializer in order to perform secret key exchange (128 bits) before using an AES layer.
	 *
	 * @param certificate The certificate used to sign/authenticate the secret key received from the remote as well as the data to
	 *                    send to the remote.
	 * @param delay       The time, in ms, to wait before sending server's secret key and the time to wait before sending server's IV.
	 * @param timeout     The maximum time, in ms, to wait for remote response during the key exchange.
	 */
	public AesLayerInitializer(ICertificate certificate, int delay, int timeout) {
		this(certificate, 128, delay, timeout);
	}

	/**
	 * Creates a layer initializer in order to perform secret key exchange (128 bits) before using an AES layer.
	 *
	 * @param certificate The certificate used to sign/authenticate the secret key received from the remote as well as the data to
	 *                    send to the remote.
	 * @param delay       The time, in ms, to wait before sending server's secret key and the time to wait before sending server's IV.
	 */
	public AesLayerInitializer(ICertificate certificate, int delay) {
		this(certificate, delay, 2000);
	}

	/**
	 * Creates a layer initializer in order to perform secret key exchange (128 bits) before using an AES layer.
	 *
	 * @param certificate The certificate used to sign/authenticate the secret key received from the remote as well as the data to
	 *                    send to the remote.
	 */
	public AesLayerInitializer(ICertificate certificate) {
		this(certificate, 500);
	}
}
