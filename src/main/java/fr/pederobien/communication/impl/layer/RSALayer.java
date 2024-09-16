package fr.pederobien.communication.impl.layer;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;

import fr.pederobien.communication.event.RequestReceivedEvent;
import fr.pederobien.communication.impl.connection.HeaderMessage;
import fr.pederobien.communication.impl.connection.Message;
import fr.pederobien.communication.interfaces.ICertificate;
import fr.pederobien.communication.interfaces.IConnection.Mode;
import fr.pederobien.communication.interfaces.IExchange;
import fr.pederobien.communication.interfaces.IHeaderMessage;
import fr.pederobien.communication.interfaces.ILayer;
import fr.pederobien.communication.interfaces.IRequestReceivedHandler;
import fr.pederobien.utils.ByteWrapper;
import fr.pederobien.utils.ReadableByteWrapper;
import fr.pederobien.utils.Watchdog;
import fr.pederobien.utils.Watchdog.WatchdogStakeholder;

public class RSALayer implements ILayer {
	private static final byte[] SUCCESS_PATTERN = "SUCCESS_PATTERN".getBytes();
	
	private ILayer implementation, notInitialised, initialised;
	private PrivateKey privateKey;
	private PublicKey remoteKey;

	/**
	 * Creates a layer using an RSA encryption/decryption algorithm.
	 * 
	 * @param mode The direction of the connection: CLIENT_TO_SERVER or SERVER_TO_CLIENT.
	 * @param certificate The certificate to sign or authenticate the remote public key.
	 */
	public RSALayer(Mode mode, ICertificate certificate) {
		notInitialised = new NotInitialisedLayer(mode, certificate);
		initialised = new InitialisedLayer();
		
		implementation = notInitialised;
	}
	
	@Override
	public boolean initialise(IExchange exchange) throws Exception {
		return implementation.initialise(exchange);
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
		private ILayer layer;
		private Mode mode;
		
		public NotInitialisedLayer(Mode mode, ICertificate certificate) {
			this.mode = mode;
			layer = new CertifiedLayer(certificate);
		}

		@Override
		public boolean initialise(IExchange exchange) throws Exception {
			KeyExchange keyExchange;
			if (mode == Mode.CLIENT_TO_SERVER)
				keyExchange = new ClientToServerKeyExchange(exchange);
			else
				keyExchange = new ServerToClientKeyExchange(exchange);

			boolean success =  keyExchange.exchange();
			if (success) {
				privateKey = keyExchange.getPrivateKey();
				remoteKey = keyExchange.getRemoteKey();
				implementation = initialised;
			}

			return success;
		}

		@Override
		public byte[] pack(IHeaderMessage message) throws Exception {
			return layer.pack(message);
		}

		@Override
		public List<IHeaderMessage> unpack(byte[] raw) throws Exception {
			return layer.unpack(raw);
		}
	}
	
	private abstract class KeyExchange {
		private IExchange exchange;
		private PublicKey publicKey, remoteKey;
		private PrivateKey privateKey;
		
		/**
		 * Creates a key exchange responsible to send and receive public key from the remote.
		 * 
		 * @param exchange The exchange to send/receive data from the remote.
		 */
		public KeyExchange(IExchange exchange) {
			this.exchange = exchange;
		}
		
		/**
		 * Perform a key exchange.
		 * 
		 * @return True if the key exchange went through, false otherwise.
		 */
		protected abstract boolean exchange() throws Exception;

		/**
		 * @return The exchange used to send/receive data from the remote.
		 */
		protected IExchange getExchange() {
			return exchange;
		}

		/**
		 * @return The public key to send to the remote.
		 */
		protected PublicKey getPublicKey() {
			return publicKey;
		}

		/**
		 * @return The private key.
		 */
		protected PrivateKey getPrivateKey() {
			return privateKey;
		}

		/**
		 * @return The public key received from the remote.
		 */
		protected PublicKey getRemoteKey() {
			return remoteKey;
		}

		/**
		 * Set the remote public key.
		 * 
		 * @param remoteKey The remote public key.
		 */
		protected void setRemoteKey(PublicKey remoteKey) {
			this.remoteKey = remoteKey;
		}
		
		protected PublicKey parseRemotePublicKey(byte[] key) {
			PublicKey remoteKey = null;
			try {
				remoteKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(key));
			} catch (Exception e) {
				// Do Nothing
			}
			
			return remoteKey;
		}

		/**
		 * Update internal public / private key.
		 */
		protected void createKeyPair() throws Exception {
			// Step 1: Creating public/private key
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(2048);

			KeyPair keyPair = generator.generateKeyPair();
			privateKey = keyPair.getPrivate();
			publicKey = keyPair.getPublic();
		}
	}
	
	private class ServerToClientKeyExchange extends KeyExchange {
		private boolean success;
		
		/**
		 * Creates a key exchange responsible to send and receive public key from the remote.
		 * 
		 * @param exchange The exchange to send/receive data from the remote.
		 */
		public ServerToClientKeyExchange(IExchange exchange) {
			super(exchange);
		}
		
		@Override
		public boolean exchange() throws Exception {
			// Max three tries to exchange keys
			int counter = 0;
			while (!success && (counter++ < 3)) {

				// Generating new keys each time a error occurs
				createKeyPair();

				// Step 1: Sending public key
				getExchange().send(new Message(getPublicKey().getEncoded(), true, 10000, args -> {
					if (!args.isTimeout()) {

						// Step 2: Excepting remote public key
						setRemoteKey(parseRemotePublicKey(args.getResponse().getBytes()));
						if (getRemoteKey() != null) {

							// Step 3: Sending positive acknowledgement
							getExchange().answer(args.getIdentifier(), new Message(SUCCESS_PATTERN, true));
							success = true;
						}
					}
				}));
			}
			
			return success;
		}
	}
	
	private class ClientToServerKeyExchange extends KeyExchange implements IRequestReceivedHandler {
		private boolean success;
		private WatchdogStakeholder watchdog;

		/**
		 * Creates a key exchange responsible to send and receive public key from the remote.
		 * 
		 * @param exchange The exchange to send/receive data from the remote.
		 */
		public ClientToServerKeyExchange(IExchange exchange) {
			super(exchange);
			
			success = false;
		}

		@Override
		protected boolean exchange() throws Exception {
			watchdog = Watchdog.create(() -> {
				while (!success) {

					// Generating new keys each time a error occurs
					createKeyPair();

					// Waiting for receiving data from the remote
					getExchange().receive(this);
				}
			}, 35000);
			
			return watchdog.start();
		}
		
		@Override
		public void onRequestReceivedEvent(RequestReceivedEvent event) {
			// When the connection with the remote has been lost
			if (event.getData() == null)
				watchdog.cancel();

			setRemoteKey(parseRemotePublicKey(event.getData()));
			if (getRemoteKey() != null) {

				// Sending public key to remote
				getExchange().answer(event.getIdentifier(), new Message(getPublicKey().getEncoded(), true, 10000, args -> {
					if (!args.isTimeout() && Arrays.equals(SUCCESS_PATTERN, args.getResponse().getBytes()))
						success = true;
				}));
			}
		}
	}
	
	private class InitialisedLayer implements ILayer {
		private Splitter splitter;
		private Encapsuler encapsuler;
		
		public InitialisedLayer() {
			splitter = new Splitter(200);
			encapsuler = new Encapsuler("(~@=", "#.?)");
		}
		
		@Override
		public boolean initialise(IExchange exchange) {
			throw new IllegalStateException("Layer already initialised");
		}

		@Override
		public byte[] pack(IHeaderMessage message) throws Exception {
			// Step 1: Splitting message in packets
			byte[] payload = ByteWrapper.create().putInt(message.getRequestID()).put(message.getBytes()).get();
			List<byte[]> packets = splitter.pack(message.getID(), payload);
			
			// Step 2: Encoding packets
			List<byte[]> encrypted = new ArrayList<byte[]>();
			for (byte[] packet : packets)
				encrypted.add(encode(packet));
			
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
			for (byte[] packet : unpacked)
				decrypted.add(decode(packet));
			

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
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			return cipher.doFinal(Base64.getDecoder().decode(data));
		}
	}
}
