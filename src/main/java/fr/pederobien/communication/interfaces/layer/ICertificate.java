package fr.pederobien.communication.interfaces.layer;

public interface ICertificate {

    /**
     * Generates a signature for the given message.
     *
     * @param message The message to sign.
     * @return A byte array corresponding to the message and the signature.
     */
    byte[] sign(byte[] message);

    /**
     * Extract the signature embed in the given message and authenticate the
     * signature.
     *
     * @param message The signed message.
     * @return null if the message signature is wrong, the message without the
     * signature otherwise.
     */
    byte[] authenticate(byte[] message);
}
