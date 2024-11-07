package fr.pederobien.communication.impl.keyexchange;

import java.util.function.Function;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import fr.pederobien.communication.impl.layer.AesLayer;
import fr.pederobien.communication.interfaces.IToken;
import fr.pederobien.communication.interfaces.layer.ICertificate;
import fr.pederobien.communication.interfaces.layer.ILayer;

public class AesKeyExchange {
	public static final byte[] SUCCESS_PATTERN = "SUCCESS_PATTERN".getBytes();

	private ICertificate certificate;
	private SymmetricKeyExchange keyExchange;
	private IvParameterSpecExchange ivExchange;

	/**
	 * Creates a AES key exchange. A size-bits AES secret key will be generated as well as a 16 bytes IV.
	 * 
	 * @param token The token to send/receive data from the remote.
	 * @param certificate To sign the data before AES encryption.
	 * @param keySize This is an algorithm-specific metric, specified in number of bits.
	 */
	public AesKeyExchange(IToken token, ICertificate certificate, int keySize) {
		this.certificate = certificate;

		try {
			KeyGenerator generator = KeyGenerator.getInstance("AES");
			generator.init(keySize);

			Function<byte[], SecretKey> keyParser = data -> {
				return new SecretKeySpec(data, 0, data.length, "AES");
			};

			keyExchange = new SymmetricKeyExchange(token, "AES_Exchange", generator, keyParser);
			ivExchange = new IvParameterSpecExchange(token, "IV_Exchange");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Perform the key exchange with the remote.
	 * 
	 * @return True if the key exchange succeed, false otherwise.
	 */
	public ILayer exchange() {
		if (keyExchange == null || !keyExchange.exchange())
			return null;

		if (ivExchange == null || !ivExchange.exchange())
			return null;

		return new AesLayer(certificate, keyExchange.getRemoteKey(), ivExchange.getIvParameterSpec());
	}
}
