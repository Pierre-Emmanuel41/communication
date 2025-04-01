package fr.pederobien.communication.testing;

import fr.pederobien.communication.impl.ClientConfig;
import fr.pederobien.communication.impl.Communication;
import fr.pederobien.communication.impl.EthernetEndPoint;
import fr.pederobien.communication.impl.ServerConfig;
import fr.pederobien.communication.impl.connection.Message;
import fr.pederobien.communication.impl.layer.AesLayerInitializer;
import fr.pederobien.communication.impl.layer.AesSafeLayerInitializer;
import fr.pederobien.communication.impl.layer.RsaLayerInitializer;
import fr.pederobien.communication.interfaces.IEthernetEndPoint;
import fr.pederobien.communication.interfaces.client.IClient;
import fr.pederobien.communication.interfaces.server.IServer;
import fr.pederobien.communication.testing.tools.Network;
import fr.pederobien.communication.testing.tools.NetworkCorruptor;
import fr.pederobien.communication.testing.tools.ServerListener;
import fr.pederobien.communication.testing.tools.SimpleCertificate;
import fr.pederobien.utils.IExecutable;
import fr.pederobien.utils.event.Logger;

public class LayerInitialisationTest {
	private static final String SERVER_NAME = "Dummy Server";
	private static final String CLIENT_NAME = "Dummy Client";
	private static final String ADDRESS = "127.0.01";
	private static final int PORT = 12345;

	public void testRsaLayerInitialization() {
		IExecutable test = () -> {
			Network network = new Network();

			ServerConfig<IEthernetEndPoint> serverConfig = createServerConfig();
			serverConfig.setLayerInitializer(() -> new RsaLayerInitializer(new SimpleCertificate()));

			IServer server = Communication.createServer(serverConfig, network.getServer());
			server.open();

			ClientConfig<IEthernetEndPoint> clientConfig = createClientConfig();
			clientConfig.setLayerInitializer(() -> new RsaLayerInitializer(new SimpleCertificate()));

			IClient client = Communication.createClient(clientConfig, network.newClient());
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
			NetworkCorruptor corruptor = new NetworkCorruptor();

			// First request: Client public key
			corruptor.registerClientToServerCorruption(0);

			Network network = new Network(corruptor);

			ServerConfig<IEthernetEndPoint> serverConfig = createServerConfig();
			serverConfig.setLayerInitializer(() -> new RsaLayerInitializer(new SimpleCertificate()));

			IServer server = Communication.createServer(serverConfig, network.getServer());
			server.open();

			ClientConfig<IEthernetEndPoint> clientConfig = createClientConfig();
			clientConfig.setAutomaticReconnection(false);
			clientConfig.setLayerInitializer(() -> new RsaLayerInitializer(new SimpleCertificate()));

			IClient client = Communication.createClient(clientConfig, network.newClient());
			client.connect();

			sleep(5000);

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
			NetworkCorruptor corruptor = new NetworkCorruptor();

			// First request: server public key
			// Second request: server acknowledgement
			corruptor.registerServerToClientCorruption(1);

			Network network = new Network(corruptor);

			ServerConfig<IEthernetEndPoint> serverConfig = createServerConfig();
			serverConfig.setLayerInitializer(() -> new RsaLayerInitializer(new SimpleCertificate()));

			IServer server = Communication.createServer(serverConfig, network.getServer());
			server.open();

			ClientConfig<IEthernetEndPoint> clientConfig = createClientConfig();
			clientConfig.setAutomaticReconnection(false);
			clientConfig.setLayerInitializer(() -> new RsaLayerInitializer(new SimpleCertificate()));

			IClient client = Communication.createClient(clientConfig, network.newClient());
			client.connect();

			sleep(5000);

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

			ServerConfig<IEthernetEndPoint> serverConfig = createServerConfig();
			serverConfig.setLayerInitializer(() -> new RsaLayerInitializer(new SimpleCertificate()));

			IServer server = Communication.createServer(serverConfig, network.getServer());
			server.open();

			ServerListener listener = new ServerListener(server);
			listener.setMessageHandler(event -> {
				Logger.debug("Server received %s", new String(event.getData()));

				Message message = new Message("a message from the server".getBytes());
				event.getConnection().answer(event.getIdentifier(), message);
			});

			listener.start();

			ClientConfig<IEthernetEndPoint> clientConfig = createClientConfig();
			clientConfig.setLayerInitializer(() -> new RsaLayerInitializer(new SimpleCertificate()));

			IClient client = Communication.createClient(clientConfig, network.newClient());
			client.connect();

			sleep(2000);

			String message = "a message from a client";
			client.getConnection().send(new Message(message.getBytes(), args -> {
				if (!args.isTimeout()) {
					Logger.debug("Client received %s", new String(args.getResponse()));
				} else {
					Logger.error("Unexpected timeout occurred");
				}
			}));

			sleep(2000);

			client.disconnect();
			client.dispose();

			sleep(500);

			listener.stop();
			server.close();
			server.dispose();
		};

		runTest("testRsaLayerInitializationAndTransmission", test);
	}

	public void testAesLayerInitialization() {
		IExecutable test = () -> {
			Network network = new Network();

			ServerConfig<IEthernetEndPoint> serverConfig = createServerConfig();
			serverConfig.setLayerInitializer(() -> new AesLayerInitializer(new SimpleCertificate()));

			IServer server = Communication.createServer(serverConfig, network.getServer());
			server.open();

			ClientConfig<IEthernetEndPoint> clientConfig = createClientConfig();
			clientConfig.setLayerInitializer(() -> new AesLayerInitializer(new SimpleCertificate()));

			IClient client = Communication.createClient(clientConfig, network.newClient());
			client.connect();

			sleep(2000);

			client.disconnect();
			client.dispose();

			sleep(500);

			server.close();
			server.dispose();
		};

		runTest("testAesLayerInitialization", test);
	}

	public void testAesLayerInitializationFailureClientToServer() {
		IExecutable test = () -> {
			NetworkCorruptor corruptor = new NetworkCorruptor();

			// First request: Client secret key
			corruptor.registerClientToServerCorruption(0);

			Network network = new Network(corruptor);

			ServerConfig<IEthernetEndPoint> serverConfig = createServerConfig();
			serverConfig.setLayerInitializer(() -> new AesLayerInitializer(new SimpleCertificate()));

			IServer server = Communication.createServer(serverConfig, network.getServer());
			server.open();

			ClientConfig<IEthernetEndPoint> clientConfig = createClientConfig();
			clientConfig.setAutomaticReconnection(false);
			clientConfig.setLayerInitializer(() -> new AesLayerInitializer(new SimpleCertificate()));

			IClient client = Communication.createClient(clientConfig, network.newClient());
			client.connect();

			sleep(4000);

			client.disconnect();
			client.dispose();

			sleep(500);

			server.close();
			server.dispose();
		};

		runTest("testAesLayerInitializationFailureClientToServer", test);
	}

	public void testAesLayerInitializationFailureServerAcknowledgement() {
		IExecutable test = () -> {
			NetworkCorruptor corruptor = new NetworkCorruptor();

			// First: server public key
			// Second: server acknowledgement
			corruptor.registerServerToClientCorruption(1);

			Network network = new Network(corruptor);

			ServerConfig<IEthernetEndPoint> serverConfig = createServerConfig();
			serverConfig.setLayerInitializer(() -> new AesLayerInitializer(new SimpleCertificate()));

			IServer server = Communication.createServer(serverConfig, network.getServer());
			server.open();

			ClientConfig<IEthernetEndPoint> clientConfig = createClientConfig();
			clientConfig.setAutomaticReconnection(false);
			clientConfig.setLayerInitializer(() -> new AesLayerInitializer(new SimpleCertificate()));

			IClient client = Communication.createClient(clientConfig, network.newClient());
			client.connect();

			sleep(5000);

			client.disconnect();
			client.dispose();

			sleep(500);

			server.close();
			server.dispose();
		};

		runTest("testAesLayerInitializationFailureServerAcknowledgement", test);
	}

	public void testAesLayerInitializationFailureIVExchange() {
		IExecutable test = () -> {
			NetworkCorruptor corruptor = new NetworkCorruptor();

			// First: server public key
			// Second: server acknowledgement
			// Third: server IV
			corruptor.registerServerToClientCorruption(2);

			Network network = new Network(corruptor);

			ServerConfig<IEthernetEndPoint> serverConfig = createServerConfig();
			serverConfig.setLayerInitializer(() -> new AesLayerInitializer(new SimpleCertificate()));

			IServer server = Communication.createServer(serverConfig, network.getServer());
			server.open();

			ClientConfig<IEthernetEndPoint> clientConfig = createClientConfig();
			clientConfig.setAutomaticReconnection(false);
			clientConfig.setLayerInitializer(() -> new AesLayerInitializer(new SimpleCertificate()));

			IClient client = Communication.createClient(clientConfig, network.newClient());
			client.connect();

			sleep(5000);

			client.disconnect();
			client.dispose();

			sleep(500);

			server.close();
			server.dispose();
		};

		runTest("testAesLayerInitializationFailureIVExchange", test);
	}

	public void testAesLayerInitializationAndTransmission() {
		IExecutable test = () -> {
			Network network = new Network();

			ServerConfig<IEthernetEndPoint> serverConfig = createServerConfig();
			serverConfig.setLayerInitializer(() -> new AesLayerInitializer(new SimpleCertificate()));

			IServer server = Communication.createServer(serverConfig, network.getServer());
			server.open();

			ServerListener listener = new ServerListener(server);
			listener.setMessageHandler(event -> {
				Logger.debug("Server received %s", new String(event.getData()));

				Message message = new Message("a message from the server".getBytes());
				event.getConnection().answer(event.getIdentifier(), message);
			});

			listener.start();

			ClientConfig<IEthernetEndPoint> clientConfig = createClientConfig();
			clientConfig.setLayerInitializer(() -> new AesLayerInitializer(new SimpleCertificate()));

			IClient client = Communication.createClient(clientConfig, network.newClient());
			client.connect();

			sleep(1000);

			String message = "a message from a client";
			client.getConnection().send(new Message(message.getBytes(), args -> {
				if (!args.isTimeout()) {
					Logger.debug("Client received %s", new String(args.getResponse()));
				} else {
					Logger.error("Unexpected timeout occurred");
				}
			}));

			sleep(2000);

			client.disconnect();
			client.dispose();

			sleep(500);

			listener.stop();
			server.close();
			server.dispose();
		};

		runTest("testAesLayerInitializationAndTransmission", test);
	}

	public void testAesSafeLayerInitializer() {
		IExecutable test = () -> {
			Network network = new Network();

			ServerConfig<IEthernetEndPoint> serverConfig = createServerConfig();
			serverConfig.setLayerInitializer(() -> new AesSafeLayerInitializer(new SimpleCertificate()));

			IServer server = Communication.createServer(serverConfig, network.getServer());
			server.open();

			ClientConfig<IEthernetEndPoint> clientConfig = createClientConfig();
			clientConfig.setLayerInitializer(() -> new AesSafeLayerInitializer(new SimpleCertificate()));

			IClient client = Communication.createClient(clientConfig, network.newClient());
			client.connect();

			sleep(2000);

			client.disconnect();
			client.dispose();

			sleep(500);

			server.close();
			server.dispose();
		};

		runTest("testAesSafeLayerInitializer", test);
	}

	public void testAesSafeLayerInitializerAndTransmission() {
		IExecutable test = () -> {
			Network network = new Network();

			ServerConfig<IEthernetEndPoint> serverConfig = createServerConfig();
			serverConfig.setLayerInitializer(() -> new AesSafeLayerInitializer(new SimpleCertificate()));

			IServer server = Communication.createServer(serverConfig, network.getServer());
			server.open();

			ServerListener listener = new ServerListener(server);
			listener.setMessageHandler(event -> {
				Logger.debug("Server received %s", new String(event.getData()));

				Message message = new Message("a message from the server".getBytes());
				event.getConnection().answer(event.getIdentifier(), message);
			});

			listener.start();

			ClientConfig<IEthernetEndPoint> clientConfig = createClientConfig();
			clientConfig.setLayerInitializer(() -> new AesSafeLayerInitializer(new SimpleCertificate()));

			IClient client = Communication.createClient(clientConfig, network.newClient());
			client.connect();

			sleep(4000);

			String message = "a message from a client";
			client.getConnection().send(new Message(message.getBytes(), args -> {
				if (!args.isTimeout()) {
					Logger.debug("Client received %s", new String(args.getResponse()));
				} else {
					Logger.debug("Unexpected timeout occurred");
				}
			}));

			sleep(2000);

			client.disconnect();
			client.dispose();

			sleep(500);

			listener.stop();
			server.close();
			server.dispose();
		};

		runTest("testAesSafeLayerInitializerAndTransmission", test);
	}

	private void runTest(String testName, IExecutable test) {
		Logger.debug("Begin %s", testName);
		try {
			test.exec();
		} catch (Exception e) {
			Logger.error("Unexpected error: %s", e.getMessage());
			for (StackTraceElement trace : e.getStackTrace()) {
				Logger.error(trace.toString());
			}
		}

		Logger.debug("End %s", testName);

		sleep(1000);
	}

	private void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return Creates a server configuration with default name and port number.
	 */
	private static ServerConfig<IEthernetEndPoint> createServerConfig() {
		return Communication.createServerConfig(SERVER_NAME, new EthernetEndPoint(PORT));
	}

	/*
	 * @return Creates a client configuration with default name, address and port
	 * number.
	 */
	private static ClientConfig<IEthernetEndPoint> createClientConfig() {
		return Communication.createClientConfig(CLIENT_NAME, new EthernetEndPoint(ADDRESS, PORT));
	}
}
