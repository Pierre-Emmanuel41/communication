package fr.pederobien.communication.impl.layer;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import javax.crypto.Cipher;

import fr.pederobien.communication.impl.connection.HeaderMessage;
import fr.pederobien.communication.impl.connection.Message;
import fr.pederobien.communication.interfaces.ICertificate;
import fr.pederobien.communication.interfaces.IConnection;
import fr.pederobien.communication.interfaces.IHeaderMessage;
import fr.pederobien.communication.interfaces.ILayer;
import fr.pederobien.utils.ByteWrapper;
import fr.pederobien.utils.ReadableByteWrapper;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.LogEvent;

public class RSALayer implements ILayer {
	private ICertificate certificate;
	private Encapsuler encapsuler;
	private Splitter splitter;
	private ILayer implementation, notInitialised, initialised;
	private PrivateKey privateKey;
	private PublicKey publicKey, otherKey;

	/**
	 * Creates a layer using an RSA encryption/decryption algorithm.
	 * 
	 * @param certificate The certificate to sign or authenticate the remote public key.
	 */
	public RSALayer(ICertificate certificate) {
		this.certificate = certificate;
		try {
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(2048);
			KeyPair keyPair = generator.generateKeyPair();
			privateKey = keyPair.getPrivate();
			publicKey = keyPair.getPublic();

			encapsuler = new Encapsuler("(~@=", "#.?)");
			splitter = new Splitter(200);
			notInitialised = new NotInitialisedLayer();
			initialised = new InitialisedLayer();
			
			implementation = notInitialised;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void initialise(IConnection connection) throws Exception {
		implementation.initialise(connection);
	}

	@Override
	public byte[] pack(IHeaderMessage message) throws Exception {
		return implementation.pack(message);
	}

	@Override
	public List<IHeaderMessage> unpack(byte[] raw) throws Exception {
		return implementation.unpack(raw);
	}
	
	private class NotInitialisedLayer implements ILayer {
		private Semaphore semaphore;
		
		public NotInitialisedLayer() {
			semaphore = new Semaphore(0);
		}
		
		@Override
		public void initialise(IConnection connection) throws Exception {
			connection.send(new Message(certificate.sign(publicKey.getEncoded())));
			semaphore.acquire();
		}

		@Override
		public byte[] pack(IHeaderMessage message) throws Exception {
			return encapsuler.pack(ByteWrapper.create().put(message.getBytes()).get());
		}

		@Override
		public List<IHeaderMessage> unpack(byte[] raw) throws Exception {
			List<IHeaderMessage> requests = new ArrayList<IHeaderMessage>();
			List<byte[]> messages = encapsuler.unpack(raw);		
			for (byte[] message : messages) {
				try {
					byte[] certifiedKey = certificate.authenticate(message);
					if (certifiedKey == null)
						throw new IllegalStateException("Could not verify remote public key");

					otherKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(message));

					implementation = initialised;
					semaphore.release();
				} catch (Exception e) {
					EventManager.callEvent(new LogEvent("Failure during RSA layer initialisation: %s", e.getMessage()));
					throw new RuntimeException("Fail to initialise RSA layer");
				}
			}
						
			return requests;
		}
	}
	
	private class InitialisedLayer implements ILayer {
		
		@Override
		public void initialise(IConnection connection) {
			throw new IllegalStateException("Layer already initialised");
		}

		@Override
		public byte[] pack(IHeaderMessage message) throws Exception {
			// Step 1: Splitting message in packets
			byte[] payload = ByteWrapper.create().putInt(message.getRequestID()).put(message.getBytes()).get();
			List<byte[]> packets = splitter.pack(message.getID(), payload);
			
			// Step 2: Encrypting packets
			List<byte[]> encrypted = new ArrayList<byte[]>();
			for (byte[] packet : packets)
				encrypted.add(encrypt(packet));
			
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
			
			// Step 2: Decrypting each packet
			List<byte[]> decrypted = new ArrayList<byte[]>();
			for (byte[] packet : unpacked)
				decrypted.add(decrypt(packet));
			
			// Step 3: Concatenating decrypted packet in one message
			Map<Integer, ReadableByteWrapper> messages = splitter.unpack(decrypted);
			
			// Step 4: Updating list of requests
			for (Map.Entry<Integer, ReadableByteWrapper> entry : messages.entrySet()) {
				ReadableByteWrapper wrapper = entry.getValue();
				
				// bytes 0 -> 3: requestID
				int requestID = wrapper.nextInt();
				
				requests.add(new HeaderMessage(entry.getKey(), requestID, wrapper.next(-1)));
			}
			
			return requests;
		}
		
		/**
		 * Encrypt the given bytes array using the public key of the remote.
		 * 
		 * @param data The bytes array to encrypt.
		 * @return A bytes array corresponding to the encryption result.
		 */
		private byte[] encrypt(byte[] data) throws Exception {
			Cipher encrypt = Cipher.getInstance("RSA");
			encrypt.init(Cipher.ENCRYPT_MODE, otherKey);
			return Base64.getEncoder().encode(encrypt.doFinal(data));
		}

		/**
		 * Decrypt the given bytes array using the private key.
		 * 
		 * @param data The bytes array to decrypt.
		 * @return A bytes array corresponding to the decryption result.
		 */
		private byte[] decrypt(byte[] data) throws Exception {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			return cipher.doFinal(Base64.getDecoder().decode(data));
		}
	}
}
