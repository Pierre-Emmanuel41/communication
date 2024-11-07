package fr.pederobien.communication.impl.layer;

import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import fr.pederobien.communication.interfaces.connection.IHeaderMessage;
import fr.pederobien.communication.interfaces.layer.ICertificate;
import fr.pederobien.communication.interfaces.layer.ILayer;

public class AesLayer implements ILayer {
	private CertifiedLayer certifiedLayer;
	private SecretKey secretKey;
	private AlgorithmParameterSpec parameterSpec;

	/**
	 * Creates an AES layer for symmetric encryption.
	 *  
	 * @param secretKey The secret key to encode/decode.
	 * @param parameterSpec The IV parameter spec.
	 */
	public AesLayer(ICertificate certificate, SecretKey secretKey, AlgorithmParameterSpec parameterSpec) {
		this.certifiedLayer = new CertifiedLayer(certificate);
		this.secretKey = secretKey;
		this.parameterSpec = parameterSpec;

		certifiedLayer.setPostSigning(data -> encode(data));
		certifiedLayer.setPreAuthentication(data -> decode(data));
	}

	@Override
	public byte[] pack(IHeaderMessage message) throws Exception {
		return certifiedLayer.pack(message);
	}

	@Override
	public List<IHeaderMessage> unpack(byte[] raw) throws Exception {
		return certifiedLayer.unpack(raw);
	}

	/**
	 * Encode the given bytes array using the public key of the remote.
	 * 
	 * @param data The bytes array to encode.
	 * @return A bytes array corresponding to the encoded result.
	 */
	private byte[] encode(byte[] data) {
		try {
			Cipher encrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
			encrypt.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
			return Base64.getEncoder().encode(encrypt.doFinal(data));
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid data to be ciphered");
		}
	}

	/**
	 * Decode the given bytes array using the private key.
	 * 
	 * @param data The bytes array to decode.
	 * @return A bytes array corresponding to the decoded result.
	 */
	private byte[] decode(byte[] data) {
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
			return cipher.doFinal(Base64.getDecoder().decode(data));
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid data to be deciphered");
		}
	}
}
