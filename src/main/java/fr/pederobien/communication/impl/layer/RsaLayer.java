package fr.pederobien.communication.impl.layer;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;

import fr.pederobien.communication.impl.connection.HeaderMessage;
import fr.pederobien.communication.interfaces.connection.IHeaderMessage;
import fr.pederobien.communication.interfaces.layer.ILayer;
import fr.pederobien.utils.ByteWrapper;
import fr.pederobien.utils.ReadableByteWrapper;

public class RsaLayer implements ILayer {
	private PrivateKey privateKey;
	private PublicKey remoteKey;
	private Splitter splitter;
	private Encapsuler encapsuler;

	/**
	 * Creates an RSA layer for asymmetric encryption.
	 * 
	 * @param privateKey The private key to decode.
	 * @param remoteKey  The remote public key to encode.
	 */
	public RsaLayer(PrivateKey privateKey, PublicKey remoteKey) {
		this.privateKey = privateKey;
		this.remoteKey = remoteKey;

		splitter = new Splitter(200);
		encapsuler = new Encapsuler("(~@=", "#.?)");
	}

	@Override
	public byte[] pack(IHeaderMessage message) throws Exception {
		// Step 1: Splitting message in packets
		byte[] payload = ByteWrapper.create().putInt(message.getRequestID()).put(message.getBytes()).get();
		List<byte[]> packets = splitter.pack(message.getIdentifier(), payload);

		// Step 2: Encoding packets
		List<byte[]> encrypted = new ArrayList<byte[]>();
		for (byte[] packet : packets) {
			encrypted.add(encode(packet));
		}

		// Step 3: Encapsulating each packet
		ByteWrapper wrapper = ByteWrapper.create();
		encrypted.forEach(data -> wrapper.put(encapsuler.pack(data)));
		return wrapper.get();
	}

	@Override
	public List<IHeaderMessage> unpack(byte[] raw) throws Exception {
		List<IHeaderMessage> requests = new ArrayList<IHeaderMessage>();

		// Step 1: Unpacking the raw buffer to get packets entirely received
		List<byte[]> unpacked = encapsuler.unpack(raw);

		// Step 2: Decode each packet
		List<byte[]> decrypted = new ArrayList<byte[]>();
		for (byte[] packet : unpacked) {
			byte[] decoded = decode(packet);
			if (decoded != null) {
				decrypted.add(decode(packet));
			}
		}

		// Step 3: Concatenating decrypted packet in one message
		Map<Integer, byte[]> messages = splitter.unpack(decrypted);

		// Step 4: Updating list of requests
		for (Map.Entry<Integer, byte[]> entry : messages.entrySet()) {
			ReadableByteWrapper wrapper = ReadableByteWrapper.wrap(entry.getValue());

			// bytes 0 -> 3: requestID
			int requestID = wrapper.nextInt();

			requests.add(new HeaderMessage(entry.getKey(), requestID, wrapper.next(-1)));
		}

		return requests;
	}

	/**
	 * Encode the given bytes array using the public key of the remote.
	 * 
	 * @param data The bytes array to encode.
	 * @return A bytes array corresponding to the encoded result.
	 */
	private byte[] encode(byte[] data) throws Exception {
		Cipher encrypt = Cipher.getInstance("RSA");
		encrypt.init(Cipher.ENCRYPT_MODE, remoteKey);
		return Base64.getEncoder().encode(encrypt.doFinal(data));
	}

	/**
	 * Decode the given bytes array using the private key.
	 * 
	 * @param data The bytes array to decode.
	 * @return A bytes array corresponding to the decoded result.
	 */
	private byte[] decode(byte[] data) throws Exception {
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			return cipher.doFinal(Base64.getDecoder().decode(data));
		} catch (Exception e) {
			return null;
		}
	}
}
