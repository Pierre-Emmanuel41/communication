package fr.pederobien.communication.impl.keyexchange;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.function.Function;

public abstract class AsymmetricKeyManager {
    private PrivateKey privateKey;
    private PublicKey remoteKey;

    /**
     * Generates a public/private key pair.
     *
     * @return The public key.
     */
    public PublicKey generatePair() {
        KeyPair pair = getKeyGenerator().generateKeyPair();

        privateKey = pair.getPrivate();
        return pair.getPublic();
    }

    /**
     * Parse the byte array using a defined key parser.
     *
     * @param publicKey The bytes array that contains the public key to parse.
     * @return The parsed remote public key.
     */
    public PublicKey parse(byte[] publicKey) {
        return remoteKey = getKeyParser().apply(publicKey);
    }

    /**
     * @return The private key associated to the public key sent to the remote.
     */
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    /**
     * @return The public key received from the remote.
     */
    public PublicKey getRemoteKey() {
        return remoteKey;
    }

    /**
     * @return The generator used to generate a key-pair.
     */
    protected abstract KeyPairGenerator getKeyGenerator();

    /**
     * @return The parser used to parse the remote public key.
     */
    protected abstract Function<byte[], PublicKey> getKeyParser();
}
