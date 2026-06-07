package fr.pederobien.communication.testing;

import java.net.DatagramSocket;

import fr.pederobien.communication.impl.Communication;
import fr.pederobien.communication.impl.EthernetEndPoint;
import fr.pederobien.communication.impl.client.ethernet.EthernetClientConfig;
import fr.pederobien.communication.impl.connection.Message;
import fr.pederobien.communication.impl.layer.AesLayerInitializer;
import fr.pederobien.communication.impl.layer.AesSafeLayerInitializer;
import fr.pederobien.communication.impl.layer.LayerInitializer;
import fr.pederobien.communication.impl.layer.RsaLayerInitializer;
import fr.pederobien.communication.impl.layer.SimpleCertificate;
import fr.pederobien.communication.impl.server.ethernet.EthernetServerConfig;
import fr.pederobien.communication.impl.server.ethernet.ServerEthernetEndPoint;
import fr.pederobien.communication.interfaces.client.IClient;
import fr.pederobien.communication.interfaces.server.IEthernetServerConfig;
import fr.pederobien.communication.interfaces.server.IServer;
import fr.pederobien.communication.testing.tools.ExceptionLayer;
import fr.pederobien.communication.testing.tools.ExceptionLayer.LayerExceptionMode;
import fr.pederobien.communication.testing.tools.ServerListener;
import fr.pederobien.utils.IExecutable;
import fr.pederobien.utils.event.Logger;

public class UdpCommunicationTest {
	private static final String SERVER_NAME = "UDP Server";
	private static final String CLIENT_NAME = "UDP Client";
	private static final String ADDRESS = "127.0.01";
	private static final int PORT = 12345;

	/**
	 * Creates a server configuration associated to the given server name, address and port number.
	 * 
	 * @param name    The name of the server.
	 * @param address The IP address of the server.
	 * @param port    The port number of the server.
	 * @return The created server config.
	 */
	private static EthernetServerConfig createServerConfig(String name, String address, int port) {
		return Communication.createEthernetServerConfig(name, new ServerEthernetEndPoint(address, port));
	}

	/**
	 * Creates a server configuration associated to the given server name, address and port number.
	 * 
	 * @param name    The name of the server.
	 * @param address The IP address of the server.
	 * @param min     The minimum value of the port number of the server.
	 * @param max     The maximum value of the port number of the server.
	 * @return The created server config.
	 */
	private static EthernetServerConfig createServerConfig(String name, String address, int min, int max) {
		return Communication.createEthernetServerConfig(name, new ServerEthernetEndPoint(address, min, max));
	}

	/**
	 * @return Creates a server configuration with default name and port number.
	 */
	private static EthernetServerConfig createServerConfig() {
		return createServerConfig(SERVER_NAME, "*", PORT);
	}

	/**
	 * Creates a server with default configuration and UDP implementation.
	 *
	 * @return The created server.
	 */
	private static IServer createDefaultUdpServer() {
		return Communication.createDefaultUdpServer(SERVER_NAME, PORT);
	}

	/**
	 * @return Creates a client configuration with default name, address and port number.
	 */
	private static EthernetClientConfig createClientConfig() {
		return Communication.createEthernetClientConfig(CLIENT_NAME, new EthernetEndPoint(ADDRESS, PORT));
	}

	/**
	 * Creates a client with default configuration and UDP implementation.
	 *
	 * @return The created client.
	 */
	private static IClient createDefaultUdpClient() {
		return Communication.createDefaultUdpClient(CLIENT_NAME, ADDRESS, PORT);
	}

	public void testServerWithSpecificAddressAndPort() {
		IExecutable test = () -> {
			IEthernetServerConfig config = createServerConfig(SERVER_NAME, "127.0.0.1", 12345);
			IServer server = Communication.createUdpServer(config);

			server.open();

			sleep(1000);

			server.close();
			server.dispose();
		};

		runTest("testServerWithSpecificAddressAndPort", test);
	}

	public void testServerWithSpecificAddressButAnyPort() {
		IExecutable test = () -> {
			IEthernetServerConfig config = createServerConfig(SERVER_NAME, "127.0.0.1", 0);
			IServer server = Communication.createUdpServer(config);

			server.open();

			sleep(1000);

			server.close();
			server.dispose();
		};

		runTest("testServerWithSpecificAddressButAnyPort", test);
	}

	public void testServerWithAnyAddressButSpecificPort() {
		IExecutable test = () -> {
			IEthernetServerConfig config = createServerConfig(SERVER_NAME, "*", 12345);
			IServer server = Communication.createUdpServer(config);

			server.open();

			sleep(1000);

			server.close();
			server.dispose();
		};

		runTest("testServerWithAnyAddressButSpecificPort", test);
	}

	public void testServerWithAnyAddressAndAnyPort() {
		IExecutable test = () -> {
			IEthernetServerConfig config = createServerConfig(SERVER_NAME, "*", 0);
			IServer server = Communication.createUdpServer(config);

			server.open();

			sleep(1000);

			server.close();
			server.dispose();
		};

		runTest("testServerWithAnyAddressAndAnyPort", test);
	}

	public void testServerWithSpecificAddressAndPortRange() {
		IExecutable test = () -> {
			IEthernetServerConfig config = createServerConfig(SERVER_NAME, "127.0.0.1", 50000, 60000);
			IServer server = Communication.createUdpServer(config);

			server.open();

			sleep(1000);

			server.close();
			server.dispose();
		};

		runTest("testServerWithSpecificAddressAndPortRange", test);
	}

	public void testServerWithSpecificAddressAndPortRangeWithFirstThreeAlreadyUsed() {
		IExecutable test = () -> {
			IEthernetServerConfig config = createServerConfig(SERVER_NAME, "127.0.0.1", 50000, 60000);
			IServer server = Communication.createUdpServer(config);

			DatagramSocket server1 = new DatagramSocket(50000);
			DatagramSocket server2 = new DatagramSocket(50001);
			DatagramSocket server3 = new DatagramSocket(50002);

			server.open();

			sleep(1000);

			server.close();
			server.dispose();

			server1.close();
			server2.close();
			server3.close();
		};

		runTest("testServerWithSpecificAddressAndPortRangeWithFirstThreeAlreadyUsed", test);
	}

	public void testServerWithAnyAddressButSpecificPortRange() {
		IExecutable test = () -> {
			IEthernetServerConfig config = createServerConfig(SERVER_NAME, "*", 50000, 60000);
			IServer server = Communication.createUdpServer(config);

			server.open();

			sleep(1000);

			server.close();
			server.dispose();
		};

		runTest("testServerWithSpecificAddressAndPortRange", test);
	}

	public void testServerWithAnyAddressButSpecificPortRangeWithFirstThreeAlreadyUsed() {
		IExecutable test = () -> {
			IEthernetServerConfig config = createServerConfig(SERVER_NAME, "*", 50000, 60000);
			IServer server = Communication.createUdpServer(config);

			DatagramSocket server1 = new DatagramSocket(50000);
			DatagramSocket server2 = new DatagramSocket(50001);
			DatagramSocket server3 = new DatagramSocket(50002);

			server.open();

			sleep(1000);

			server.close();
			server.dispose();

			server1.close();
			server2.close();
			server3.close();
		};

		runTest("testServerWithSpecificAddressAndPortRangeWithFirstThreeAlreadyUsed", test);
	}

	public void testServerCloseClientConnection() {
		IExecutable test = () -> {
			IServer server = createDefaultUdpServer();
			server.open();

			ServerListener listener = new ServerListener(server);
			listener.setActionOnNewClientConnected(event -> {
				sleep(500);
				event.getConnection().setEnabled(false);
				event.getConnection().dispose();
			});

			listener.start();

			sleep(500);

			EthernetClientConfig clientConfig = createClientConfig();
			clientConfig.setAutomaticReconnection(false);
			IClient client = Communication.createUdpClient(clientConfig);

			client.connect();

			sleep(2000);

			server.close();
			server.dispose();
		};

		runTest("testServerCloseClientConnection", test);
	}

	public void testClientCloseConnection() {
		IExecutable test = () -> {
			IServer server = createDefaultUdpServer();
			server.open();

			sleep(500);

			IClient client = createDefaultUdpClient();
			client.connect();

			sleep(2000);

			client.disconnect();

			sleep(2000);

			server.close();
			server.dispose();
		};

		runTest("testClientCloseConnection", test);
	}

	public void testClientToServerCommunication() {
		IExecutable test = () -> {
			IServer server = createDefaultUdpServer();
			server.open();

			ServerListener listener = new ServerListener(server);
			listener.setMessageHandler(event -> Logger.debug("Server received %s", new String(event.getData())));

			listener.start();

			IClient client = createDefaultUdpClient();
			client.connect();

			sleep(100);

			client.getConnection().send(new Message("a message from a client".getBytes()));

			sleep(1000);

			client.getConnection().send(new Message("a second message from a client".getBytes()));

			sleep(2000);

			client.disconnect();
			client.dispose();

			sleep(500);

			listener.stop();
			server.close();
			server.dispose();
		};

		runTest("testClientToServerCommunication", test);
	}

	public void testServerToClientCommunication() {
		IExecutable test = () -> {
			IServer server = createDefaultUdpServer();
			server.open();

			ServerListener listener = new ServerListener(server);
			listener.setActionOnNewClientConnected(event -> {
				sleep(500);

				event.getConnection().send(new Message("a message from the server".getBytes()));
			});

			listener.start();

			EthernetClientConfig clientConfig = createClientConfig();
			clientConfig.setMessageHandler(event -> {
				Logger.debug("Client received %s", new String(event.getData()));

				Message message = new Message("a message from a client".getBytes());
				event.getConnection().send(message);
			});

			IClient client = Communication.createUdpClient(clientConfig);
			client.connect();

			sleep(2000);

			client.disconnect();
			client.dispose();

			sleep(500);

			listener.stop();
			server.close();
			server.dispose();
		};

		runTest("testServerToClientCommunication", test);
	}

	public void testClientToServerWithCallback() {
		IExecutable test = () -> {
			IServer server = createDefaultUdpServer();
			server.open();

			ServerListener listener = new ServerListener(server);
			listener.setMessageHandler(event -> {
				Logger.debug("Server received %s", new String(event.getData()));

				Message message = new Message("a message from the server".getBytes());
				event.getConnection().answer(event.getIdentifier(), message);
			});

			listener.start();

			IClient client = createDefaultUdpClient();
			client.connect();

			sleep(500);

			String message = "a message from a client";
			client.getConnection().send(new Message(message.getBytes(), args -> {
				if (!args.isTimeout()) {
					Logger.debug("Client received %s", new String(args.response()));
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

		runTest("testClientToServerWithCallback", test);
	}

	public void testClientToServerWithCallbackButTimeout() {
		IExecutable test = () -> {
			IServer server = createDefaultUdpServer();
			server.open();

			ServerListener listener = new ServerListener(server);
			listener.setMessageHandler(event -> {
				Logger.debug("Server received %s, but will not respond to it", new String(event.getData()));
			});

			listener.start();

			IClient client = createDefaultUdpClient();
			client.connect();

			sleep(500);

			String message = "a message from a client";
			client.getConnection().send(new Message(message.getBytes(), args -> {
				if (!args.isTimeout()) {
					Logger.error("Unexpected response received: %s", new String(args.response()));
				} else {
					Logger.debug("Expected timeout occurred");
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

		runTest("testClientToServerWithCallbackButTimeout", test);
	}

	public void testServerToClientWithCallback() {
		IExecutable test = () -> {
			IServer server = createDefaultUdpServer();
			server.open();

			ServerListener listener = new ServerListener(server);
			listener.setActionOnNewClientConnected(event -> {
				Message message = new Message("a message from the server".getBytes(), args -> {
					if (!args.isTimeout()) {
						Logger.debug("Server received %s", new String(args.response()));
					} else {
						Logger.error("Unexpected timeout occurred");
					}
				});

				event.getConnection().send(message);
			});

			listener.start();

			EthernetClientConfig clientConfig = createClientConfig();
			clientConfig.setMessageHandler(event -> {
				Logger.debug("Client received %s", new String(event.getData()));

				Message message = new Message("a message from a client".getBytes());
				event.getConnection().answer(event.getIdentifier(), message);
			});

			IClient client = Communication.createUdpClient(clientConfig);
			client.connect();

			sleep(2000);

			client.disconnect();
			client.dispose();

			sleep(500);

			listener.stop();
			server.close();
			server.dispose();
		};

		runTest("testServerToClientWithCallback", test);
	}

	public void testServerToClientWithCallbackButTimeout() {
		IExecutable test = () -> {
			IServer server = createDefaultUdpServer();
			server.open();

			ServerListener listener = new ServerListener(server);
			listener.setActionOnNewClientConnected(event -> {
				Message message = new Message("a message from the server".getBytes(), args -> {
					if (!args.isTimeout()) {
						Logger.error("Server received %s", new String(args.response()));
					} else {
						Logger.debug("Server: Expected timeout occurred");
					}
				});

				event.getConnection().send(message);
			});

			listener.start();

			EthernetClientConfig clientConfig = createClientConfig();
			clientConfig.setMessageHandler(event -> {
				Logger.debug("Client received %s, but will not respond to it", new String(event.getData()));
			});

			IClient client = Communication.createUdpClient(clientConfig);
			client.connect();

			sleep(2000);

			client.disconnect();
			client.dispose();

			sleep(500);

			listener.stop();
			server.close();
			server.dispose();
		};

		runTest("testServerToClientWithCallbackButTimeout", test);
	}

	public void testExtractionException() {
		IExecutable test = () -> {
			IServer server = createDefaultUdpServer();
			server.open();

			ServerListener listener = new ServerListener(server);
			listener.setActionOnNewClientConnected(event -> {
				sleep(500);

				for (int i = 0; i < 18 && !event.getConnection().isDisposed(); i++) {
					Logger.print("Extracting message %s", i);
					byte[] bytes = "a message from the server".getBytes();
					event.getConnection().send(new Message(bytes));

					sleep(500);
				}
			});

			listener.start();

			EthernetClientConfig clientConfig = createClientConfig();
			clientConfig.setAutomaticReconnection(false);
			clientConfig.setLayerInitializer(() -> new LayerInitializer(new ExceptionLayer(LayerExceptionMode.UNPACK)));

			IClient client = Communication.createUdpClient(clientConfig);
			client.connect();

			sleep(250);

			Logger.print("Expecting unstable connection after extracting 18 messages");

			sleep(11000);

			client.disconnect();
			client.dispose();

			sleep(500);

			listener.stop();
			server.close();
			server.dispose();
		};

		runTest("testExtractionException", test);
	}

	public void testCallbackException() {
		IExecutable test = () -> {
			IServer server = createDefaultUdpServer();
			server.open();

			ServerListener listener = new ServerListener(server);
			listener.setMessageHandler(event -> {
				byte[] bytes = "a message from the server".getBytes();
				event.getConnection().answer(event.getIdentifier(), new Message(bytes));
			});

			listener.start();

			EthernetClientConfig clientConfig = createClientConfig();
			clientConfig.setAutomaticReconnection(false);

			IClient client = Communication.createUdpClient(clientConfig);
			client.connect();

			sleep(250);

			Logger.print("Expecting unstable connection after sending 18 messages");

			for (int i = 0; i < 18; i++) {
				Logger.print("Sending message %s", i);

				String message = "a message from a client";
				client.getConnection().send(new Message(message.getBytes(), args -> {
					if (!args.isTimeout()) {
						throw new RuntimeException("Exception to test unstable counter");
					} else {
						Logger.debug("Unexpected timeout occurred");
					}
				}));

				sleep(500);
			}

			sleep(2000);

			client.disconnect();
			client.dispose();

			sleep(500);

			listener.stop();
			server.close();
			server.dispose();
		};

		runTest("testCallbackException", test);
	}

	public void testUnexpectedRequestException() {
		IExecutable test = () -> {
			IServer server = createDefaultUdpServer();
			server.open();

			ServerListener listener = new ServerListener(server);
			listener.setActionOnNewClientConnected(event -> {
				sleep(500);

				for (int i = 0; i < 18 && !event.getConnection().isDisposed(); i++) {
					Logger.print("Server Sending message %s", i);

					byte[] bytes = "a message from the server".getBytes();
					event.getConnection().send(new Message(bytes));

					sleep(500);
				}
			});

			listener.start();

			EthernetClientConfig clientConfig = createClientConfig();
			clientConfig.setAutomaticReconnection(false);
			clientConfig.setMessageHandler(event -> {
				throw new RuntimeException("Exception to test unstable counter");
			});

			IClient client = Communication.createUdpClient(clientConfig);
			client.connect();

			sleep(12000);

			client.disconnect();
			client.dispose();

			sleep(500);

			listener.stop();
			server.close();
			server.dispose();
		};

		runTest("testUnexpectedRequestException", test);
	}

	public void testUnstableClient() {
		IExecutable test = () -> {
			IServer server = createDefaultUdpServer();
			server.open();

			ServerListener listener = new ServerListener(server);
			listener.setActionOnNewClientConnected(event -> {
				for (int i = 0; i < 18 && !event.getConnection().isDisposed(); i++) {
					Logger.print("Server Sending message %s", i);

					byte[] bytes = "a message from the server".getBytes();
					event.getConnection().send(new Message(bytes));

					sleep(250);
				}
			});

			listener.start();

			EthernetClientConfig clientConfig = createClientConfig();
			clientConfig.setClientMaxUnstableCounter(5);
			clientConfig.setClientHealTime(9000);
			clientConfig.setConnectionHealTime(500);
			clientConfig.setMessageHandler(event -> {
				throw new RuntimeException("Exception to test unstable counter");
			});

			IClient client = Communication.createUdpClient(clientConfig);
			client.connect();

			sleep(250);

			Logger.print("Expecting unstable client after receiving 144 unexpected messages");

			sleep(40000);

			client.disconnect();
			client.dispose();

			sleep(500);

			listener.stop();
			server.close();
			server.dispose();
		};

		runTest("testUnstableClient", test);
	}

	public void testRsaLayer() {
		IExecutable test = () -> {
			EthernetServerConfig serverConfig = createServerConfig();
			serverConfig.setLayerInitializer(() -> new RsaLayerInitializer(new SimpleCertificate()));

			IServer server = Communication.createUdpServer(serverConfig);
			server.open();

			ServerListener listener = new ServerListener(server);
			listener.setActionOnNewClientConnected(event -> {
				sleep(500);

				Message message = new Message("a message from the server".getBytes(), args -> {
					if (!args.isTimeout()) {
						Logger.debug("Server received %s", new String(args.response()));
					} else {
						Logger.error("Unexpected timeout occurred");
					}
				});

				event.getConnection().send(message);
			});

			listener.start();

			EthernetClientConfig clientConfig = createClientConfig();
			clientConfig.setLayerInitializer(() -> new RsaLayerInitializer(new SimpleCertificate()));
			clientConfig.setMessageHandler(event -> {
				Logger.debug("Client received %s", new String(event.getData()));

				event.getConnection().answer(event.getIdentifier(), new Message("a message from a client".getBytes()));
			});

			IClient client = Communication.createUdpClient(clientConfig);
			client.connect();

			sleep(3000);

			client.disconnect();
			client.dispose();

			sleep(500);

			listener.stop();
			server.close();
			server.dispose();
		};

		runTest("testRsaLayer", test);
	}

	public void testAesLayer() {
		IExecutable test = () -> {
			EthernetServerConfig serverConfig = createServerConfig();
			serverConfig.setLayerInitializer(() -> new AesLayerInitializer(new SimpleCertificate()));

			IServer server = Communication.createUdpServer(serverConfig);
			server.open();

			ServerListener listener = new ServerListener(server);
			listener.setActionOnNewClientConnected(event -> {
				sleep(500);

				Message message = new Message("a message from the server".getBytes(), args -> {
					if (!args.isTimeout()) {
						Logger.debug("Server received %s", new String(args.response()));
					} else {
						Logger.error("Unexpected timeout occurred");
					}
				});

				event.getConnection().send(message);
			});

			listener.start();

			EthernetClientConfig clientConfig = createClientConfig();
			clientConfig.setLayerInitializer(() -> new AesLayerInitializer(new SimpleCertificate()));
			clientConfig.setMessageHandler(event -> {
				Logger.debug("Client received %s", new String(event.getData()));

				event.getConnection().answer(event.getIdentifier(), new Message("a message from a client".getBytes()));
			});

			IClient client = Communication.createUdpClient(clientConfig);
			client.connect();

			sleep(3000);

			client.disconnect();
			client.dispose();

			sleep(500);

			listener.stop();
			server.close();
			server.dispose();
		};

		runTest("testAesLayer", test);
	}

	public void testAesSafeLayer() {
		IExecutable test = () -> {
			EthernetServerConfig serverConfig = createServerConfig();
			serverConfig.setLayerInitializer(() -> new AesSafeLayerInitializer(new SimpleCertificate()));

			IServer server = Communication.createUdpServer(serverConfig);
			server.open();

			ServerListener listener = new ServerListener(server);
			listener.setActionOnNewClientConnected(event -> {
				sleep(500);

				Message message = new Message("a message from the server".getBytes(), args -> {
					if (!args.isTimeout()) {
						Logger.debug("Server received %s", new String(args.response()));
					} else {
						Logger.error("Unexpected timeout occurred");
					}
				});

				event.getConnection().send(message);
			});

			listener.start();

			EthernetClientConfig clientConfig = createClientConfig();
			clientConfig.setLayerInitializer(() -> new AesSafeLayerInitializer(new SimpleCertificate()));
			clientConfig.setMessageHandler(event -> {
				Logger.debug("Client received %s", new String(event.getData()));

				event.getConnection().answer(event.getIdentifier(), new Message("a message from a client".getBytes()));
			});

			IClient client = Communication.createUdpClient(clientConfig);
			client.connect();

			sleep(3000);

			client.disconnect();
			client.dispose();

			sleep(500);

			listener.stop();
			server.close();
			server.dispose();
		};

		runTest("testAesSafeLayer", test);
	}

	public void testBigRequest() {
		IExecutable test = () -> {
			EthernetServerConfig serverConfig = createServerConfig();
			serverConfig.setLayerInitializer(() -> new AesSafeLayerInitializer(new SimpleCertificate()));

			IServer server = Communication.createUdpServer(serverConfig);
			server.open();

			ServerListener listener = new ServerListener(server);
			listener.setActionOnNewClientConnected(event -> {
				sleep(500);

				byte[] data = new byte[10000];
				byte counter = Byte.MIN_VALUE;
				for (int i = 0; i < data.length; i++) {
					data[i] = counter;

					if (counter == Byte.MAX_VALUE)
						counter = Byte.MIN_VALUE;
					else
						counter++;
				}

				Message message = new Message(data, args -> {
					if (!args.isTimeout()) {
						Logger.debug("Server received %s bytes", args.response().length);
					} else {
						Logger.error("Unexpected timeout occurred");
					}
				});
				event.getConnection().send(message);
			});

			listener.start();

			EthernetClientConfig clientConfig = createClientConfig();
			clientConfig.setLayerInitializer(() -> new AesSafeLayerInitializer(new SimpleCertificate()));
			clientConfig.setMessageHandler(event -> {
				Logger.debug("Client received %s bytes", event.getData().length);

				byte[] data = new byte[10000];
				byte counter = Byte.MIN_VALUE;
				for (int i = 0; i < data.length; i++) {
					data[i] = counter;

					if (counter == Byte.MAX_VALUE)
						counter = Byte.MIN_VALUE;
					else
						counter++;
				}

				event.getConnection().answer(event.getIdentifier(), new Message(data));
			});

			IClient client = Communication.createUdpClient(clientConfig);
			client.connect();

			sleep(3000);

			client.disconnect();
			client.dispose();

			sleep(500);

			listener.stop();
			server.close();
			server.dispose();
		};

		runTest("testBigRequest", test);
	}

	public void testTwoClientsOneServer() {
		IExecutable tests = () -> {
			IServer server = createDefaultUdpServer();
			server.open();

			ServerListener listener = new ServerListener(server);
			listener.setMessageHandler(event -> Logger.debug("Server received %s", new String(event.getData())));

			listener.start();

			sleep(1000);

			IClient client1 = createDefaultUdpClient();
			client1.connect();

			IClient client2 = createDefaultUdpClient();
			client2.connect();

			sleep(2000);

			for (int i = 0; i < 5; i++) {
				client1.getConnection().send(new Message("a message from client1".getBytes()));
				client2.getConnection().send(new Message("a message from client2".getBytes()));
			}

			sleep(2000);

			listener.stop();
			server.close();
			server.dispose();

			sleep(500);

			client1.disconnect();
			client1.dispose();

			client2.disconnect();
			client2.dispose();
		};

		runTest("testTwoClientsOneServer", tests);
	}

	private void runTest(String testName, IExecutable test) {
		Logger.warning("Begin %s", testName);
		try {
			test.exec();
		} catch (Exception e) {
			Logger.error("Unexpected error: %s", e.getMessage());
			for (StackTraceElement trace : e.getStackTrace()) {
				Logger.error(trace.toString());
			}
		}
		Logger.warning("End %s", testName);
	}

	private void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
