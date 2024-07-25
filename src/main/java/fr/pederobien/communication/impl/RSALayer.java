package fr.pederobien.communication.impl;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import javax.crypto.Cipher;

import fr.pederobien.communication.interfaces.IConnection;
import fr.pederobien.communication.interfaces.IHeaderMessage;
import fr.pederobien.communication.interfaces.ILayer;
import fr.pederobien.utils.ByteWrapper;
import fr.pederobien.utils.ReadableByteWrapper;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.LogEvent;

public class RSALayer implements ILayer {
	private static final int MAX_PACKET_SIZE = 200;

	private Map<Integer, ByteWrapper> remaining;
	private LayerHelper helper;
	private ILayer implementation, notInitialised, initialised;
	private PrivateKey privateKey;
	private PublicKey publicKey, otherKey;

	/**
	 * Creates a layer using an RSA encryption/decryption algorithm
	 */
	public RSALayer() {
		try {
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(2048);
			KeyPair keyPair = generator.generateKeyPair();
			privateKey = keyPair.getPrivate();
			publicKey = keyPair.getPublic();

			helper = new LayerHelper("(~@=", "#.?)");
			notInitialised = new NotInitialisedLayer();
			initialised = new InitialisedLayer();
			
			remaining = new HashMap<Integer, ByteWrapper>();
			
			implementation = notInitialised;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public byte[] initialise() {
		return helper.pack(publicKey.getEncoded());
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
			connection.send(new Message(publicKey.getEncoded()));
			semaphore.acquire();
		}

		@Override
		public byte[] pack(IHeaderMessage message) throws Exception {
			return helper.pack(ByteWrapper.create().put(message.getBytes()).get());
		}

		@Override
		public List<IHeaderMessage> unpack(byte[] raw) throws Exception {
			List<IHeaderMessage> requests = new ArrayList<IHeaderMessage>();
			List<byte[]> messages = helper.unpack(raw);		
			for (byte[] message : messages) {
				try {
					otherKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(message));

					implementation = initialised;
					semaphore.release();
				} catch (Exception e) {
					EventManager.callEvent(new LogEvent("Fail to initialise RSA layer"));
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
			ReadableByteWrapper wrapper = ReadableByteWrapper.wrap(message.getBytes());
			int fullPacketNumber = message.getBytes().length / MAX_PACKET_SIZE;
			int numberOfByteInLastPacket = message.getBytes().length % MAX_PACKET_SIZE;
			int total = fullPacketNumber + (numberOfByteInLastPacket > 0 ? 1 : 0);

			ByteWrapper toSend = ByteWrapper.create();
			for (int current = 0; current < total; current++) {
				ByteWrapper packet = ByteWrapper.create();
				packet.putInt(message.getID());
				packet.putInt(message.getRequestID());
				packet.putInt(total);
				packet.putInt(current);
				int length = current < total - 1 ? MAX_PACKET_SIZE : numberOfByteInLastPacket;
				packet.putInt(length);
				packet.put(wrapper.next(length));
				toSend.put(helper.pack(encrypt(packet.get())));
			}

			return toSend.get();
		}

		@Override
		public List<IHeaderMessage> unpack(byte[] raw) throws Exception {
			List<IHeaderMessage> requests = new ArrayList<IHeaderMessage>();

			// Step 1: Unpacking the raw buffer to get messages entirely received
			List<byte[]> messages = helper.unpack(raw);
			
			// Step 2: Concatenating messages into requests
			for (byte[] message : messages) {
				// Structure of a message:
				ReadableByteWrapper messageWrapper = ReadableByteWrapper.wrap(decrypt(message));
				
				// bytes 0 -> 3: ID
				int ID = messageWrapper.nextInt();
				
				// bytes 4 -> 7: requestID
				int requestID = messageWrapper.nextInt();

				// bytes 8 -> 11: total
				int total = messageWrapper.nextInt();

				// bytes 12 -> 15: current
				int current = messageWrapper.nextInt();

				// bytes 16 -> 19: length
				int length = messageWrapper.nextInt();
				
				// bytes 20 -> 20 + length: payload
				byte[] payload = messageWrapper.next(length);
				
				// Original request to split in different packets
				if (total == 1)
					requests.add(new HeaderMessage(ID, requestID, payload));
				else {
					IHeaderMessage request = registerRequest(ID, requestID, total, current, payload);
					if (request != null)
						requests.add(request);
				}
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
		
		/**
		 * If a request is already registered for the given ID, the payload is added at the end of the previous payload.
		 * When the value of current correspond to total - 1, all the packets have been received. The request has been
		 * received entirely and can be used.
		 * If no request has been received for the given ID, the payload will be stored until a new packet is received.
		 * 
		 * @param ID The identifier of this request.
		 * @param requestID The identifier of the request associated to this response.
		 * @param total The number of packets to receive in order to receive a full request.
		 * @param current The current packet number.
		 * @param payload The payload of the request.
		 */
		private IHeaderMessage registerRequest(int ID, int requestID, int total, int current, byte[] payload) {
			ByteWrapper wrapper = remaining.get(ID);
			
			// No request is registered
			if (wrapper == null)
				remaining.put(ID, ByteWrapper.wrap(payload));
			else {
				wrapper.put(payload);
				
				// Request received entirely
				if (current == total - 1) {
					return new HeaderMessage(ID, requestID, wrapper.get());
				}
			}
			
			return null;
		}
	}
}
