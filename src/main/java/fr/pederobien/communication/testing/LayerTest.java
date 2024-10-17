package fr.pederobien.communication.testing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import fr.pederobien.communication.impl.Communication;
import fr.pederobien.communication.impl.client.ClientConfigBuilder;
import fr.pederobien.communication.impl.connection.HeaderMessage;
import fr.pederobien.communication.impl.connection.Message;
import fr.pederobien.communication.impl.layer.CertifiedLayer;
import fr.pederobien.communication.impl.layer.Encapsuler;
import fr.pederobien.communication.impl.layer.RsaLayerInitializer;
import fr.pederobien.communication.impl.layer.SimpleLayer;
import fr.pederobien.communication.impl.layer.Splitter;
import fr.pederobien.communication.impl.server.ServerConfigBuilder;
import fr.pederobien.communication.interfaces.IClient;
import fr.pederobien.communication.interfaces.IConnection.Mode;
import fr.pederobien.communication.interfaces.IHeaderMessage;
import fr.pederobien.communication.interfaces.ILayer;
import fr.pederobien.communication.interfaces.IServer;
import fr.pederobien.communication.testing.tools.Network;
import fr.pederobien.communication.testing.tools.NetworkSimulator.IModifier;
import fr.pederobien.communication.testing.tools.SimpleAnswerToRequestListener;
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
	
	public void testRsaLayerInitialization() {
		IExecutable test = () -> {
			Network network = new Network();

			ServerConfigBuilder serverBuilder = Communication.createServerConfigBuilder("Dummy Server", 12345);
			serverBuilder.setLayerInitializer(new RsaLayerInitializer(new SimpleCertificate()));

			IServer server = Communication.createCustomServer(serverBuilder.build(), network.getServer());
			server.open();

			ClientConfigBuilder clientBuilder = Communication.createClientConfigBuilder("127.0.0.1", 12345);
			clientBuilder.setLayerInitializer(new RsaLayerInitializer(new SimpleCertificate()));

			IClient client = Communication.createCustomClient(clientBuilder.build(), network.newClient());
			client.connect();

			sleep(2000);

			client.disconnect();
			client.dispose();
			
			sleep(500);
			
			server.close();
			server.dispose();
		};

		runTest("testRsaLayerInitialization", test);
	}
	
	public void testRsaLayerInitializationFailureClientToServer() {
		IExecutable test = () -> {
			IModifier modifier = (counter, data) -> {
				Random random = new Random();
				for (int i = 0; i < 5; i++) {
					int index = random.nextInt(4, data.length - 4);
					int value = random.nextInt(-127, 126);
					
					data[index] = (byte) value;
				}
				return data;
			};
			Network network = new Network(Mode.CLIENT_TO_SERVER, modifier);

			ServerConfigBuilder serverBuilder = Communication.createServerConfigBuilder("Dummy Server", 12345);
			serverBuilder.setLayerInitializer(new RsaLayerInitializer(new SimpleCertificate()));

			IServer server = Communication.createCustomServer(serverBuilder.build(), network.getServer());
			server.open();

			ClientConfigBuilder clientBuilder = Communication.createClientConfigBuilder("127.0.0.1", 12345);
			clientBuilder.setLayerInitializer(new RsaLayerInitializer(new SimpleCertificate()));

			IClient client = Communication.createCustomClient(clientBuilder.build(), network.newClient());
			client.connect();

			sleep(60000);

			client.disconnect();
			client.dispose();
			
			sleep(2000);
			
			server.close();
			server.dispose();
		};

		runTest("testRsaLayerInitializationFailureClientToServer", test);
	}
	
	public void testRsaLayerInitializationFailureServerAcknowledgement() {
		IExecutable test = () -> {
			IModifier modifier = (counter, data) -> {
				// First request: server public key
				// Second request: server acknowledgement
				if (counter == 2) {
					Random random = new Random();
					for (int i = 0; i < 5; i++) {
						int index = random.nextInt(4, data.length - 4);
						int value = random.nextInt(-127, 126);
						
						data[index] = (byte) value;
					}
					return data;
				}
				return data;
			};
			Network network = new Network(Mode.SERVER_TO_CLIENT, modifier);
			
			ServerConfigBuilder serverBuilder = Communication.createServerConfigBuilder("Dummy Server", 12345);
			serverBuilder.setLayerInitializer(new RsaLayerInitializer(new SimpleCertificate()));
			
			IServer server = Communication.createCustomServer(serverBuilder.build(), network.getServer());
			server.open();
			
			ClientConfigBuilder clientBuilder = Communication.createClientConfigBuilder("127.0.0.1", 12345);
			clientBuilder.setLayerInitializer(new RsaLayerInitializer(new SimpleCertificate()));
			
			IClient client = Communication.createCustomClient(clientBuilder.build(), network.newClient());
			client.connect();
			
			sleep(400000000);
			
			client.disconnect();
			client.dispose();
			
			sleep(500);
			
			server.close();
			server.dispose();
		};
		
		runTest("testRsaLayerInitializationFailureServerAcknowledgement", test);
	}
	
	public void testRsaLayerInitializationAndTransmission() {
		IExecutable test = () -> {
			Network network = new Network();
			
			ServerConfigBuilder serverBuilder = Communication.createServerConfigBuilder("Dummy Server", 12345);
			serverBuilder.setLayerInitializer(new RsaLayerInitializer(new SimpleCertificate()));
			serverBuilder.setOnUnexpectedRequestReceived(new SimpleAnswerToRequestListener("I received your request !"));
			
			IServer server = Communication.createCustomServer(serverBuilder.build(), network.getServer());
			server.open();
			
			ClientConfigBuilder clientBuilder = Communication.createClientConfigBuilder("127.0.0.1", 12345);
			clientBuilder.setLayerInitializer(new RsaLayerInitializer(new SimpleCertificate()));
			
			IClient client = Communication.createCustomClient(clientBuilder.build(), network.newClient());
			client.connect();
			
			sleep(2000);
			
			client.getConnection().send(new Message("Hello world".getBytes(), args -> {
				if (!args.isTimeout()) {
					EventManager.callEvent(new LogEvent("Client received: %s", new String(args.getResponse().getBytes())));
				}
				else
					EventManager.callEvent(new LogEvent(ELogLevel.ERROR, "Unexpected timeout occurred"));
			}));
			
			sleep(2000);
			
			client.disconnect();
			client.dispose();
			
			sleep(500);
			
			server.close();
			server.dispose();
		};
		
		runTest("testRsaLayerInitializationAndTransmission", test);
	}
	
	public void testRsaLayerInitializationFirstFailureAndTransmission() {
		IExecutable test = () -> {
			/*
			NetworkSimulator network = new NetworkSimulator(Function.identity(), new NetworkModifier(0, 1));

			ConfigurationBuilder clientConfigBuilder = Communication.createConfigurationBuilder();
			clientConfigBuilder.setLayer(new RSALayer(new SimpleCertificate()));
			
			ConnectionConfigBuilder clientConnectionBuilder = Communication.createConnectionConfigBuilder("127.0.0.1", 12345, clientConfigBuilder.build());
			
			ConfigurationBuilder serverConfigBuilder = Communication.createConfigurationBuilder();
			serverConfigBuilder.setLayer(new RSALayer(new SimpleCertificate()));
			serverConfigBuilder.setRequestReceivedHandler(() -> new SimpleAnswerToRequestListener("I received your request !"));
			
			ConnectionConfigBuilder serverConnectionBuilder = Communication.createConnectionConfigBuilder("127.0.0.1", 12345, serverConfigBuilder.build());

			IConnection client = Communication.createCustomConnection(clientConnectionBuilder.build(), network.getClient(), Mode.CLIENT_TO_SERVER);
			IConnection server = Communication.createCustomConnection(serverConnectionBuilder.build(), network.getServer(), Mode.SERVER_TO_CLIENT);
			
			Thread clientThread = new Thread(() -> {
				try {
					boolean success = client.initialise();
					EventManager.callEvent(new LogEvent("Client initialization %s", success ? "succeed" : "failed"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}, "Client initialization");
			
			Thread serverThread = new Thread(() -> {
				try {
					boolean success = server.initialise();
					EventManager.callEvent(new LogEvent("Server initialization %s", success ? "succeed" : "failed"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}, "Server initialization");
			
			serverThread.start();
			clientThread.start();
			
			sleep(15000);
			
			String message1 = "Hello world";
			client.send(new Message(message1.getBytes(), args -> {
				if (!args.isTimeout()) {
					EventManager.callEvent(new LogEvent("Client received: %s", new String(args.getResponse().getBytes())));
				}
				else
					EventManager.callEvent(new LogEvent(ELogLevel.ERROR, "Unexpected timeout occurred"));
			}));
			
			sleep(2000);
			
			client.dispose();
			server.dispose();
			*/
		};
		
		runTest("testRsaLayerInitializationFirstFailureAndTransmission", test);
	}
	
	public void testRsaLayerInitializationSecondFailureAndTransmission() {
		IExecutable test = () -> {
			/*
			NetworkSimulator network = new NetworkSimulator(new NetworkModifier(0, 1), new NetworkModifier(0, 1));

			ConfigurationBuilder clientConfigBuilder = Communication.createConfigurationBuilder();
			clientConfigBuilder.setLayer(new RSALayer(new SimpleCertificate()));
			
			ConnectionConfigBuilder clientConnectionBuilder = Communication.createConnectionConfigBuilder("127.0.0.1", 12345, clientConfigBuilder.build());
			
			ConfigurationBuilder serverConfigBuilder = Communication.createConfigurationBuilder();
			serverConfigBuilder.setLayer(new RSALayer(new SimpleCertificate()));
			serverConfigBuilder.setRequestReceivedHandler(() -> new SimpleAnswerToRequestListener("I received your request !"));
			
			ConnectionConfigBuilder serverConnectionBuilder = Communication.createConnectionConfigBuilder("127.0.0.1", 12345, serverConfigBuilder.build());

			IConnection client = Communication.createCustomConnection(clientConnectionBuilder.build(), network.getClient(), Mode.CLIENT_TO_SERVER);
			IConnection server = Communication.createCustomConnection(serverConnectionBuilder.build(), network.getServer(), Mode.SERVER_TO_CLIENT);
			
			Thread clientThread = new Thread(() -> {
				try {
					boolean success = client.initialise();
					EventManager.callEvent(new LogEvent("Client initialization %s", success ? "succeed" : "failed"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}, "Client initialization");
			
			Thread serverThread = new Thread(() -> {
				try {
					boolean success = server.initialise();
					EventManager.callEvent(new LogEvent("Server initialization %s", success ? "succeed" : "failed"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}, "Server initialization");
			
			serverThread.start();
			clientThread.start();
			
			sleep(25000);
			
			String message1 = "Hello world";
			client.send(new Message(message1.getBytes(), args -> {
				if (!args.isTimeout()) {
					EventManager.callEvent(new LogEvent("Client received: %s", new String(args.getResponse().getBytes())));
				}
				else
					EventManager.callEvent(new LogEvent(ELogLevel.ERROR, "Unexpected timeout occurred"));
			}));
			
			sleep(2000);
			
			client.dispose();
			server.dispose();
			*/
		};
		
		runTest("testRsaLayerInitializationSecondFailureAndTransmission", test);
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
	
	private void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
