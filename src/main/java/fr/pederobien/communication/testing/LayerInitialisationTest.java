package fr.pederobien.communication.testing;

import java.util.Random;

import fr.pederobien.communication.impl.ClientConfig;
import fr.pederobien.communication.impl.Communication;
import fr.pederobien.communication.impl.ServerConfig;
import fr.pederobien.communication.impl.connection.Message;
import fr.pederobien.communication.impl.layer.RsaLayerInitializer;
import fr.pederobien.communication.interfaces.IClient;
import fr.pederobien.communication.interfaces.IConnection.Mode;
import fr.pederobien.communication.interfaces.IServer;
import fr.pederobien.communication.testing.tools.Network;
import fr.pederobien.communication.testing.tools.NetworkSimulator.IModifier;
import fr.pederobien.communication.testing.tools.SimpleAnswerToRequestListener;
import fr.pederobien.communication.testing.tools.SimpleCertificate;
import fr.pederobien.utils.IExecutable;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.LogEvent;
import fr.pederobien.utils.event.LogEvent.ELogLevel;

public class LayerInitialisationTest {

	public void testRsaLayerInitialization() {
		IExecutable test = () -> {
			Network network = new Network();

			ServerConfig serverConfig = Communication.createServerConfig("Dummy Server", 12345);
			serverConfig.setLayerInitializer(new RsaLayerInitializer(new SimpleCertificate()));

			IServer server = Communication.createCustomServer(serverConfig, network.getServer());
			server.open();

			ClientConfig clientConfig = Communication.createClientConfig("127.0.0.1", 12345);
			clientConfig.setLayerInitializer(new RsaLayerInitializer(new SimpleCertificate()));

			IClient client = Communication.createCustomClient(clientConfig, network.newClient());
			client.connect();

			sleep(3000);

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

			ServerConfig serverConfig = Communication.createServerConfig("Dummy Server", 12345);
			serverConfig.setLayerInitializer(new RsaLayerInitializer(new SimpleCertificate()));

			IServer server = Communication.createCustomServer(serverConfig, network.getServer());
			server.open();

			ClientConfig clientConfig = Communication.createClientConfig("127.0.0.1", 12345);
			clientConfig.setLayerInitializer(new RsaLayerInitializer(new SimpleCertificate()));

			IClient client = Communication.createCustomClient(clientConfig, network.newClient());
			client.connect();

			sleep(20000);

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
				if (counter % 2 == 0) {
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

			ServerConfig serverConfig = Communication.createServerConfig("Dummy Server", 12345);
			serverConfig.setLayerInitializer(new RsaLayerInitializer(new SimpleCertificate()));

			IServer server = Communication.createCustomServer(serverConfig, network.getServer());
			server.open();

			ClientConfig clientConfig = Communication.createClientConfig("127.0.0.1", 12345);
			clientConfig.setLayerInitializer(new RsaLayerInitializer(new SimpleCertificate()));

			IClient client = Communication.createCustomClient(clientConfig, network.newClient());
			client.connect();

			sleep(25000);

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

			ServerConfig serverConfig = Communication.createServerConfig("Dummy Server", 12345);
			serverConfig.setLayerInitializer(new RsaLayerInitializer(new SimpleCertificate()));
			serverConfig.setOnUnexpectedRequestReceived(new SimpleAnswerToRequestListener("I received your request !"));

			IServer server = Communication.createCustomServer(serverConfig, network.getServer());
			server.open();

			ClientConfig clientConfig = Communication.createClientConfig("127.0.0.1", 12345);
			clientConfig.setLayerInitializer(new RsaLayerInitializer(new SimpleCertificate()));

			IClient client = Communication.createCustomClient(clientConfig, network.newClient());
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
			IModifier modifier = (counter, data) -> {
				// First request: server public key
				if (counter == 1) {
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
			
			ServerConfig serverConfig = Communication.createServerConfig("Dummy Server", 12345);
			serverConfig.setLayerInitializer(new RsaLayerInitializer(new SimpleCertificate()));
			serverConfig.setOnUnexpectedRequestReceived(new SimpleAnswerToRequestListener("I received your request !"));

			IServer server = Communication.createCustomServer(serverConfig, network.getServer());
			server.open();

			ClientConfig clientConfig = Communication.createClientConfig("127.0.0.1", 12345);
			clientConfig.setLayerInitializer(new RsaLayerInitializer(new SimpleCertificate()));

			IClient client = Communication.createCustomClient(clientConfig, network.newClient());
			client.connect();

			sleep(4000);
			
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

		runTest("testRsaLayerInitializationFirstFailureAndTransmission", test);
	}

	public void testRsaLayerInitializationSecondFailureAndTransmission() {
		IExecutable test = () -> {
			IModifier modifier = (counter, data) -> {
				// First request: server public key
				if (counter == 1) {
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
			
			Network network = new Network(modifier, modifier);
			
			ServerConfig serverConfig = Communication.createServerConfig("Dummy Server", 12345);
			serverConfig.setLayerInitializer(new RsaLayerInitializer(new SimpleCertificate()));
			serverConfig.setOnUnexpectedRequestReceived(new SimpleAnswerToRequestListener("I received your request !"));

			IServer server = Communication.createCustomServer(serverConfig, network.getServer());
			server.open();

			ClientConfig clientConfig = Communication.createClientConfig("127.0.0.1", 12345);
			clientConfig.setLayerInitializer(new RsaLayerInitializer(new SimpleCertificate()));

			IClient client = Communication.createCustomClient(clientConfig, network.newClient());
			client.connect();

			sleep(6000);
			
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
