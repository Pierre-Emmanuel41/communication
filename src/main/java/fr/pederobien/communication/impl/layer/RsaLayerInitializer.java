package fr.pederobien.communication.impl.layer;

import fr.pederobien.communication.impl.keyexchange.RsaKeyExchange;
import fr.pederobien.communication.interfaces.layer.ICertificate;

public class RsaLayerInitializer extends LayerInitializer {

    /**
     * Creates a layer initializer in order to perform public key exchange before
     * using an RSA layer.
     *
     * @param certificate The certificate used to sign/authenticate the remote key
     *                    received from the remote.
     * @param keySize     The key size. This is an algorithm-specific metric, such as
     *                    modulus length, specified in number of bits.
     * @param timeout     The maximum time, in ms, to wait for remote response
     *                    during the key exchange.
     */
    public RsaLayerInitializer(ICertificate certificate, int keySize, int timeout) {
        super(new CertifiedLayer(certificate), token -> new RsaKeyExchange(token, keySize, timeout).exchange());
    }

    /**
     * Creates a layer initializer in order to perform public key exchange before
     * using an RSA layer.
     *
     * @param certificate The certificate used to sign/authenticate the remote key
     *                    received from the remote.
     * @param timeout     The maximum time, in ms, to wait for remote response
     *                    during the key exchange.
     */
    public RsaLayerInitializer(ICertificate certificate, int timeout) {
        this(certificate, 2048, timeout);
    }

    /**
     * Creates a layer initializer in order to perform public key exchange before
     * using an RSA layer.
     *
     * @param certificate The certificate used to sign/authenticate the remote key
     *                    received from the remote.
     */
    public RsaLayerInitializer(ICertificate certificate) {
        this(certificate, 2000);
    }
}
