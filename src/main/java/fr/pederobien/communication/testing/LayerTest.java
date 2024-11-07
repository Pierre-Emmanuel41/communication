package fr.pederobien.communication.testing;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import fr.pederobien.communication.impl.connection.HeaderMessage;
import fr.pederobien.communication.impl.connection.Message;
import fr.pederobien.communication.impl.layer.AesLayer;
import fr.pederobien.communication.impl.layer.CertifiedLayer;
import fr.pederobien.communication.impl.layer.Encapsuler;
import fr.pederobien.communication.impl.layer.RsaLayer;
import fr.pederobien.communication.impl.layer.SimpleLayer;
import fr.pederobien.communication.impl.layer.Splitter;
import fr.pederobien.communication.interfaces.connection.IHeaderMessage;
import fr.pederobien.communication.interfaces.layer.ICertificate;
import fr.pederobien.communication.interfaces.layer.ILayer;
import fr.pederobien.communication.testing.tools.SimpleCertificate;
import fr.pederobien.utils.IExecutable;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.LogEvent;
import fr.pederobien.utils.event.LogEvent.ELogLevel;

public class LayerTest {

	public void testEncapsulerOneMessage() {
		IExecutable test = () -> {
			Encapsuler encapsuler = new Encapsuler("(~@=", "#.?)");
			String message = "Hello world";

			byte[] toSend = encapsuler.pack(message.getBytes());
			List<byte[]> messages = encapsuler.unpack(toSend);

			for (byte[] received : messages)
				EventManager.callEvent(new LogEvent("Unpacked message: %s", new String(received)));
		};

		runTest("testEncapsulerOneMessage", test);
	}

	public void testEncapsulerTwoMessages() {
		IExecutable test = () -> {
			Encapsuler encapsuler = new Encapsuler("(~@=", "#.?)");
			String main = "Hello world";

			String message1 = main.concat(" 1");
			String message2 = main.concat(" 2");

			byte[] encapsulated1 = encapsuler.pack(message1.getBytes());
			byte[] encapsulated2 = encapsuler.pack(message2.getBytes());

			byte[] total = new byte[encapsulated1.length + encapsulated2.length];
			System.arraycopy(encapsulated1, 0, total, 0, encapsulated1.length);
			System.arraycopy(encapsulated2, 0, total, encapsulated1.length, encapsulated2.length);

			List<byte[]> messages = encapsuler.unpack(total);
			for (byte[] received : messages)
				EventManager.callEvent(new LogEvent("Unpacked message: %s", new String(received)));
		};

		runTest("testEncapsulerTwoMessages", test);
	}

	public void testEncapsulerLastMessageTruncated() {
		IExecutable test = () -> {
			Encapsuler encapsuler = new Encapsuler("(~@=", "#.?)");
			String main = "Hello world";

			String message1 = main.concat(" 1");
			String message2 = main.concat(" 2");

			byte[] encapsulated1 = encapsuler.pack(message1.getBytes());
			byte[] encapsulated2 = encapsuler.pack(message2.getBytes());

			byte[] total = new byte[encapsulated1.length + encapsulated2.length];
			System.arraycopy(encapsulated1, 0, total, 0, encapsulated1.length);
			System.arraycopy(encapsulated2, 0, total, encapsulated1.length, encapsulated2.length);

			// Missing last 5 bytes
			byte[] truncated = new byte[encapsulated1.length + encapsulated2.length - 5];
			System.arraycopy(total, 0, truncated, 0, truncated.length);

			List<byte[]> messages = encapsuler.unpack(truncated);
			for (byte[] received : messages)
				EventManager.callEvent(new LogEvent("Unpacked message: %s", new String(received)));

			// Remaining bytes
			byte[] remaining = new byte[5];
			System.arraycopy(total, total.length - 5, remaining, 0, 5);

			messages = encapsuler.unpack(remaining);
			for (byte[] received : messages)
				EventManager.callEvent(new LogEvent("Unpacked message: %s", new String(received)));
		};

		runTest("testEncapsulerLastMessageTruncated", test);
	}

	public void testSplitterOneMessage() {
		IExecutable test = () -> {
			String message = "Hello world";

			Splitter splitter = new Splitter(5);

			int identifier = 1;
			List<byte[]> messages = splitter.pack(identifier, message.getBytes());

			Map<Integer, byte[]> concatenated = splitter.unpack(messages);
			for (byte[] received : concatenated.values())
				EventManager.callEvent(new LogEvent("Concatenated message: %s", new String(received)));
		};

		runTest("testSplitterOneMessage", test);
	}

	public void testSplitterTwoMessages() {
		IExecutable test = () -> {
			Splitter splitter = new Splitter(5);
			String main = "Hello world";

			String message1 = main.concat(" 1");
			String message2 = main.concat(" 2");

			int identifier = 1;
			List<byte[]> messages1 = splitter.pack(identifier, message1.getBytes());
			identifier++;
			List<byte[]> messages2 = splitter.pack(identifier, message2.getBytes());

			List<byte[]> total = new ArrayList<byte[]>();
			for (byte[] data : messages1)
				total.add(data);
			for (byte[] data : messages2)
				total.add(data);

			Map<Integer, byte[]> concatenated = splitter.unpack(total);
			for (byte[] received : concatenated.values())
				EventManager.callEvent(new LogEvent("Concatenated message: %s", new String(received)));
		};

		runTest("testSplitterTwoMessages", test);
	}

	public void testSplitterLastMessageTruncated() {
		IExecutable test = () -> {
			Splitter splitter = new Splitter(5);
			String main = "Hello world";

			String message1 = main.concat(" 1");
			String message2 = main.concat(" 2");

			int identifier = 1;
			List<byte[]> messages1 = splitter.pack(identifier, message1.getBytes());
			identifier++;
			List<byte[]> messages2 = splitter.pack(identifier, message2.getBytes());

			// Missing last packet
			List<byte[]> total = new ArrayList<byte[]>();
			for (int i = 0; i < messages1.size() - 1; i++)
				total.add(messages1.get(i));

			for (int i = 0; i < messages2.size() - 1; i++)
				total.add(messages2.get(i));

			EventManager.callEvent(new LogEvent("Intermediate size: %s", splitter.unpack(total).size()));

			// Remaining packet
			List<byte[]> remaining = new ArrayList<byte[]>();
			remaining.add(messages1.getLast());
			remaining.add(messages2.getLast());

			Map<Integer, byte[]> concatenated = splitter.unpack(remaining);
			for (byte[] received : concatenated.values())
				EventManager.callEvent(new LogEvent("Concatenated message: %s", new String(received)));
		};

		runTest("testSplitterLastMessageTruncated", test);
	}

	public void testSimpleLayerOneMessage() {
		IExecutable test = () -> {
			ILayer layer = new SimpleLayer();
			String message = "Hello world";

			byte[] toSend = layer.pack(new HeaderMessage(0, new Message(message.getBytes())));
			List<IHeaderMessage> messages = layer.unpack(toSend);

			for (IHeaderMessage received : messages)
				EventManager.callEvent(new LogEvent("Raw message: %s, data=%s", received, new String(received.getBytes())));
		};

		runTest("testSimpleLayerOneMessage", test);
	}

	public void testSimpleLayerTwoMessages() {
		IExecutable test = () -> {
			ILayer layer = new SimpleLayer();
			String main = "Hello world";
			String message1 = main.concat(" 1");
			String message2 = main.concat(" 2");

			byte[] toSend1 = layer.pack(new HeaderMessage(0, new Message(message1.getBytes())));
			byte[] toSend2 = layer.pack(new HeaderMessage(0, new Message(message2.getBytes())));

			byte[] total = new byte[toSend1.length + toSend2.length];
			System.arraycopy(toSend1, 0, total, 0, toSend1.length);
			System.arraycopy(toSend2, 0, total, toSend1.length, toSend2.length);

			List<IHeaderMessage> messages = layer.unpack(total);
			for (IHeaderMessage received : messages)
				EventManager.callEvent(new LogEvent("Raw message: %s, data=%s", received, new String(received.getBytes())));
		};

		runTest("testSimpleLayerTwoMessages", test);
	}

	public void testSimpleLayerLastMessageTruncated() {
		IExecutable test = () -> {
			ILayer layer = new SimpleLayer();
			String main = "Hello world";

			String message1 = main.concat(" 1");
			String message2 = main.concat(" 2");

			byte[] toSend1 = layer.pack(new HeaderMessage(0, new Message(message1.getBytes())));
			byte[] toSend2 = layer.pack(new HeaderMessage(0, new Message(message2.getBytes())));

			// Missing last 5 bytes
			byte[] total = new byte[toSend1.length + toSend2.length - 5];
			System.arraycopy(toSend1, 0, total, 0, toSend1.length);
			System.arraycopy(toSend2, 0, total, toSend1.length, toSend2.length - 5);

			List<IHeaderMessage> messages = layer.unpack(total);
			for (IHeaderMessage received : messages)
				EventManager.callEvent(new LogEvent("Raw message: %s, data=%s", received, new String(received.getBytes())));

			// Remaining bytes
			byte[] remaining = new byte[5];
			System.arraycopy(toSend2, toSend2.length - 5, remaining, 0, 5);

			messages = layer.unpack(remaining);
			for (IHeaderMessage received : messages)
				EventManager.callEvent(new LogEvent("Raw message: %s, data=%s", received, new String(received.getBytes())));
		};

		runTest("testSimpleLayerLastMessageTruncated", test);
	}

	public void testCertifiedLayerOneMessage() {
		IExecutable test = () -> {
			ILayer layer = new CertifiedLayer(new SimpleCertificate());
			String message = "Hello world";

			byte[] toSend = layer.pack(new HeaderMessage(0, new Message(message.getBytes())));
			List<IHeaderMessage> messages = layer.unpack(toSend);

			for (IHeaderMessage received : messages)
				EventManager.callEvent(new LogEvent("Raw message: %s, data=%s", received, new String(received.getBytes())));
		};

		runTest("testCertifiedLayerOneMessage", test);
	}

	public void testCertifiedLayerTwoMessages() {
		IExecutable test = () -> {
			ILayer layer = new CertifiedLayer(new SimpleCertificate());
			String main = "Hello world";

			String message1 = main.concat(" 1");
			String message2 = main.concat(" 2");

			byte[] toSend1 = layer.pack(new HeaderMessage(0, new Message(message1.getBytes())));
			byte[] toSend2 = layer.pack(new HeaderMessage(0, new Message(message2.getBytes())));

			byte[] total = new byte[toSend1.length + toSend2.length];
			System.arraycopy(toSend1, 0, total, 0, toSend1.length);
			System.arraycopy(toSend2, 0, total, toSend1.length, toSend2.length);

			List<IHeaderMessage> messages = layer.unpack(total);
			for (IHeaderMessage received : messages)
				EventManager.callEvent(new LogEvent("Raw message: %s, data=%s", received, new String(received.getBytes())));
		};

		runTest("testCertifiedLayerTwoMessages", test);
	}

	public void testCertifiedLayerLastMessageTruncated() {
		IExecutable test = () -> {
			ILayer layer = new CertifiedLayer(new SimpleCertificate());
			String main = "Hello world";

			String message1 = main.concat(" 1");
			String message2 = main.concat(" 2");

			byte[] toSend1 = layer.pack(new HeaderMessage(0, new Message(message1.getBytes())));
			byte[] toSend2 = layer.pack(new HeaderMessage(0, new Message(message2.getBytes())));

			byte[] total = new byte[toSend1.length + toSend2.length - 5];
			System.arraycopy(toSend1, 0, total, 0, toSend1.length);
			System.arraycopy(toSend2, 0, total, toSend1.length, toSend2.length - 5);

			List<IHeaderMessage> messages = layer.unpack(total);
			for (IHeaderMessage received : messages)
				EventManager.callEvent(new LogEvent("Raw message: %s, data=%s", received, new String(received.getBytes())));

			// Remaining bytes
			byte[] remaining = new byte[5];
			System.arraycopy(toSend2, toSend2.length - 5, remaining, 0, 5);

			messages = layer.unpack(remaining);
			for (IHeaderMessage received : messages)
				EventManager.callEvent(new LogEvent("Raw message: %s, data=%s", received, new String(received.getBytes())));
		};

		runTest("testCertifiedLayerLastMessageTruncated", test);
	}

	public void testCertifiedLayerOneCorruptedMessage() {
		IExecutable test = () -> {
			ILayer layer = new CertifiedLayer(new SimpleCertificate());
			String message = "Hello world";

			byte[] toSend = layer.pack(new HeaderMessage(0, new Message(message.getBytes())));

			// Simulating corruption: modifying message
			byte[] corrupted1 = new byte[toSend.length];
			System.arraycopy(toSend, 0, corrupted1, 0, toSend.length);
			corrupted1[5] = 'g';

			List<IHeaderMessage> messages = layer.unpack(corrupted1);
			EventManager.callEvent(new LogEvent("Expecting no message, size: %s", messages.size()));

			// Simulating corruption: modifying signature
			byte[] corrupted2 = new byte[toSend.length];
			System.arraycopy(toSend, 0, corrupted2, 0, toSend.length);
			corrupted2[corrupted2.length - 1] = 0;

			messages = layer.unpack(corrupted2);
			EventManager.callEvent(new LogEvent("Expecting no message, size: %s", messages.size()));
		};

		runTest("testCertifiedLayerOneCorruptedMessage", test);
	}

	public void testRsaLayerOneMessage() {
		IExecutable test = () -> {
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(2048);

			KeyPair pairA = generator.generateKeyPair();
			KeyPair pairB = generator.generateKeyPair();

			ILayer rsaA = new RsaLayer(pairA.getPrivate(), pairB.getPublic());
			ILayer rsaB = new RsaLayer(pairB.getPrivate(), pairA.getPublic());

			String message = "Hello World";

			byte[] toSend = rsaA.pack(new HeaderMessage(0, new Message(message.getBytes())));

			List<IHeaderMessage> messages = rsaB.unpack(toSend);

			for (IHeaderMessage received : messages)
				EventManager.callEvent(new LogEvent("Raw data: %s", new String(received.getBytes())));
		};

		runTest("testRsaLayerOneMessage", test);
	}

	public void testRsaLayerTwoMessages() {
		IExecutable test = () -> {
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(2048);

			KeyPair pairA = generator.generateKeyPair();
			KeyPair pairB = generator.generateKeyPair();

			ILayer rsaA = new RsaLayer(pairA.getPrivate(), pairB.getPublic());
			ILayer rsaB = new RsaLayer(pairB.getPrivate(), pairA.getPublic());

			String message = "Hello World";
			String message1 = message.concat(" 1");
			String message2 = message.concat(" 2");

			byte[] toSend1 = rsaA.pack(new HeaderMessage(0, new Message(message1.getBytes())));
			byte[] toSend2 = rsaA.pack(new HeaderMessage(0, new Message(message2.getBytes())));

			byte[] total = new byte[toSend1.length + toSend2.length];
			System.arraycopy(toSend1, 0, total, 0, toSend1.length);
			System.arraycopy(toSend2, 0, total, toSend1.length, toSend2.length);

			List<IHeaderMessage> messages = rsaB.unpack(total);

			for (IHeaderMessage received : messages)
				EventManager.callEvent(new LogEvent("Raw data: %s", new String(received.getBytes())));
		};

		runTest("testRsaLayerTwoMessages", test);
	}

	public void testRsaLayerLastMessageTruncated() {
		IExecutable test = () -> {
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(2048);

			KeyPair pairA = generator.generateKeyPair();
			KeyPair pairB = generator.generateKeyPair();

			ILayer rsaA = new RsaLayer(pairA.getPrivate(), pairB.getPublic());
			ILayer rsaB = new RsaLayer(pairB.getPrivate(), pairA.getPublic());

			String message = "Hello World";
			String message1 = message.concat(" 1");
			String message2 = message.concat(" 2");

			byte[] toSend1 = rsaA.pack(new HeaderMessage(0, new Message(message1.getBytes())));
			byte[] toSend2 = rsaA.pack(new HeaderMessage(0, new Message(message2.getBytes())));

			byte[] total = new byte[toSend1.length + toSend2.length - 5];
			System.arraycopy(toSend1, 0, total, 0, toSend1.length);
			System.arraycopy(toSend2, 0, total, toSend1.length, toSend2.length - 5);

			List<IHeaderMessage> messages = rsaB.unpack(total);
			for (IHeaderMessage received : messages)
				EventManager.callEvent(new LogEvent("Raw data: %s", new String(received.getBytes())));

			// Remaining bytes
			byte[] remaining = new byte[5];
			System.arraycopy(toSend2, toSend2.length - 5, remaining, 0, 5);

			messages = rsaB.unpack(remaining);
			for (IHeaderMessage received : messages)
				EventManager.callEvent(new LogEvent("Raw data: %s", new String(received.getBytes())));
		};

		runTest("testRsaLayerLastMessageTruncated", test);
	}

	public void testRsaLayerOneCorruptedMessage() {
		IExecutable test = () -> {
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(2048);

			KeyPair pairA = generator.generateKeyPair();
			KeyPair pairB = generator.generateKeyPair();

			ILayer rsaA = new RsaLayer(pairA.getPrivate(), pairB.getPublic());
			ILayer rsaB = new RsaLayer(pairB.getPrivate(), pairA.getPublic());

			String message = "Hello World";

			byte[] toSend = rsaA.pack(new HeaderMessage(0, new Message(message.getBytes())));

			// Simulating corruption: modifying message
			byte[] corrupted = new byte[toSend.length];
			System.arraycopy(toSend, 0, corrupted, 0, toSend.length);
			corrupted[5] = 'g';

			List<IHeaderMessage> messages = rsaB.unpack(corrupted);
			EventManager.callEvent(new LogEvent("Expecting no message, size: %s", messages.size()));
		};

		runTest("testRsaLayerOneCorruptedMessage", test);
	}

	public void testRsaLayerOneBigMessage() {
		IExecutable test = () -> {
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(2048);

			KeyPair pairA = generator.generateKeyPair();
			KeyPair pairB = generator.generateKeyPair();

			ILayer rsaA = new RsaLayer(pairA.getPrivate(), pairB.getPublic());
			ILayer rsaB = new RsaLayer(pairB.getPrivate(), pairA.getPublic());

			byte[] message = new byte[500];
			for (int i = 0; i < 200; i++)
				message[i] = 'a';
			for (int i = 200; i < 400; i++)
				message[i] = 'b';
			for (int i = 400; i < 499; i++)
				message[i] = 'c';

			byte[] toSend = rsaA.pack(new HeaderMessage(0, new Message(message)));

			List<IHeaderMessage> messages = rsaB.unpack(toSend);
			for (IHeaderMessage received : messages)
				EventManager.callEvent(new LogEvent("Raw data: %s", new String(received.getBytes())));
		};

		runTest("testRsaLayerOneBigMessage", test);
	}

	public void testAesLayerOneMessage() {
		IExecutable test = () -> {
			KeyGenerator generator = KeyGenerator.getInstance("AES");
			generator.init(128);
			SecretKey secretKey = generator.generateKey();

			byte[] iv = new byte[16];
			SecureRandom random = new SecureRandom();
			random.nextBytes(iv);
			AlgorithmParameterSpec ivParameterSpec = new IvParameterSpec(iv);

			ICertificate certificate = new SimpleCertificate();
			ILayer aesA = new AesLayer(certificate, secretKey, ivParameterSpec);
			ILayer aesB = new AesLayer(certificate, secretKey, ivParameterSpec);

			String message = "Hello World";

			byte[] toSend = aesA.pack(new HeaderMessage(0, new Message(message.getBytes())));

			List<IHeaderMessage> messages = aesB.unpack(toSend);

			for (IHeaderMessage received : messages)
				EventManager.callEvent(new LogEvent("Raw data: %s", new String(received.getBytes())));
		};

		runTest("testAesLayerOneMessage", test);
	}

	public void testAesLayerTwoMessages() {
		IExecutable test = () -> {
			KeyGenerator generator = KeyGenerator.getInstance("AES");
			generator.init(128);
			SecretKey secretKey = generator.generateKey();

			byte[] iv = new byte[16];
			SecureRandom random = new SecureRandom();
			random.nextBytes(iv);
			AlgorithmParameterSpec ivParameterSpec = new IvParameterSpec(iv);

			ICertificate certificate = new SimpleCertificate();
			ILayer aesA = new AesLayer(certificate, secretKey, ivParameterSpec);
			ILayer aesB = new AesLayer(certificate, secretKey, ivParameterSpec);

			String message = "Hello World";
			String message1 = message.concat(" 1");
			String message2 = message.concat(" 2");

			byte[] toSend1 = aesA.pack(new HeaderMessage(0, new Message(message1.getBytes())));
			byte[] toSend2 = aesA.pack(new HeaderMessage(0, new Message(message2.getBytes())));

			byte[] total = new byte[toSend1.length + toSend2.length];
			System.arraycopy(toSend1, 0, total, 0, toSend1.length);
			System.arraycopy(toSend2, 0, total, toSend1.length, toSend2.length);

			List<IHeaderMessage> messages = aesB.unpack(total);

			for (IHeaderMessage received : messages)
				EventManager.callEvent(new LogEvent("Raw data: %s", new String(received.getBytes())));
		};

		runTest("testAesLayerTwoMessages", test);
	}

	public void testAesLayerLastMessageTruncated() {
		IExecutable test = () -> {
			KeyGenerator generator = KeyGenerator.getInstance("AES");
			generator.init(128);
			SecretKey secretKey = generator.generateKey();

			byte[] iv = new byte[16];
			SecureRandom random = new SecureRandom();
			random.nextBytes(iv);
			AlgorithmParameterSpec ivParameterSpec = new IvParameterSpec(iv);

			ICertificate certificate = new SimpleCertificate();
			ILayer aesA = new AesLayer(certificate, secretKey, ivParameterSpec);
			ILayer aesB = new AesLayer(certificate, secretKey, ivParameterSpec);

			String message = "Hello World";
			String message1 = message.concat(" 1");
			String message2 = message.concat(" 2");

			byte[] toSend1 = aesA.pack(new HeaderMessage(0, new Message(message1.getBytes())));
			byte[] toSend2 = aesA.pack(new HeaderMessage(0, new Message(message2.getBytes())));

			byte[] total = new byte[toSend1.length + toSend2.length - 5];
			System.arraycopy(toSend1, 0, total, 0, toSend1.length);
			System.arraycopy(toSend2, 0, total, toSend1.length, toSend2.length - 5);

			List<IHeaderMessage> messages = aesB.unpack(total);
			for (IHeaderMessage received : messages)
				EventManager.callEvent(new LogEvent("Raw data: %s", new String(received.getBytes())));

			// Remaining bytes
			byte[] remaining = new byte[5];
			System.arraycopy(toSend2, toSend2.length - 5, remaining, 0, 5);

			messages = aesB.unpack(remaining);
			for (IHeaderMessage received : messages)
				EventManager.callEvent(new LogEvent("Raw data: %s", new String(received.getBytes())));
		};

		runTest("testAesLayerLastMessageTruncated", test);
	}

	public void testAesLayerOneCorruptedMessage() {
		IExecutable test = () -> {
			KeyGenerator generator = KeyGenerator.getInstance("AES");
			generator.init(128);
			SecretKey secretKey = generator.generateKey();

			byte[] iv = new byte[16];
			SecureRandom random = new SecureRandom();
			random.nextBytes(iv);
			AlgorithmParameterSpec ivParameterSpec = new IvParameterSpec(iv);

			ICertificate certificate = new SimpleCertificate();
			ILayer aesA = new AesLayer(certificate, secretKey, ivParameterSpec);
			ILayer aesB = new AesLayer(certificate, secretKey, ivParameterSpec);

			String message = "Hello World";

			byte[] toSend = aesA.pack(new HeaderMessage(0, new Message(message.getBytes())));

			// Simulating corruption: modifying message
			byte[] corrupted = new byte[toSend.length];
			System.arraycopy(toSend, 0, corrupted, 0, toSend.length);
			corrupted[5] = 'g';

			List<IHeaderMessage> messages = aesB.unpack(corrupted);
			EventManager.callEvent(new LogEvent("Expecting no message, size: %s", messages.size()));
		};

		runTest("testAesLayerOneCorruptedMessage", test);
	}

	private void runTest(String testName, IExecutable test) {
		EventManager.callEvent(new LogEvent(ELogLevel.DEBUG, "Begin %s", testName));
		try {
			test.exec();
		} catch (Exception e) {
			EventManager.callEvent(new LogEvent(ELogLevel.ERROR, "Unexpected error: %s", e.getMessage()));
		}
		EventManager.callEvent(new LogEvent(ELogLevel.DEBUG, "End %s", testName));
	}
}
