package fr.pederobien.communication.testing;

import fr.pederobien.communication.impl.ClientConfig;
import fr.pederobien.communication.impl.Communication;
import fr.pederobien.communication.impl.EthernetEndPoint;
import fr.pederobien.communication.impl.ServerConfig;
import fr.pederobien.communication.impl.connection.Message;
import fr.pederobien.communication.impl.layer.AesLayerInitializer;
import fr.pederobien.communication.impl.layer.AesSafeLayerInitializer;
import fr.pederobien.communication.impl.layer.LayerInitializer;
import fr.pederobien.communication.impl.layer.RsaLayerInitializer;
import fr.pederobien.communication.interfaces.IEthernetEndPoint;
import fr.pederobien.communication.interfaces.client.IClient;
import fr.pederobien.communication.interfaces.connection.IMessage;
import fr.pederobien.communication.interfaces.server.IServer;
import fr.pederobien.communication.testing.tools.ExceptionLayer;
import fr.pederobien.communication.testing.tools.ExceptionLayer.LayerExceptionMode;
import fr.pederobien.communication.testing.tools.ServerListener;
import fr.pederobien.communication.testing.tools.SimpleCertificate;
import fr.pederobien.utils.IExecutable;
import fr.pederobien.utils.event.Logger;

public class TcpCommunicationTest {
	private static final String SERVER_NAME = "TCP Server";
	private static final String CLIENT_NAME = "TCP Client";
	private static final String ADDRESS = "127.0.01";
	private static final int PORT = 12345;

	public void testClientAutomaticReconnection() {
		IExecutable test = () -> {
			IClient client = createDefaultTcpClient();
			client.connect();

			sleep(5000);

			client.disconnect();
			client.dispose();
		};

		runTest("testClientAutomaticReconnection", test);
	}

	public void testClientAutomaticReconnectionButWithServerOpenedLater() {
		IExecutable test = () -> {
			IClient client = createDefaultTcpClient();
			client.connect();

			sleep(3000);

			IServer server = createDefaultTcpServer();
			server.open();

			sleep(2000);

			client.disconnect();
			client.dispose();

			sleep(500);

			server.close();
			server.dispose();
		};

		runTest("testClientAutomaticReconnectionButWithServerOpenedLater", test);
	}

	public void testClientAutomaticReconnectionButServerClosedLater() {
		IExecutable test = () -> {
			IServer server = createDefaultTcpServer();
			server.open();

			sleep(500);

			IClient client = createDefaultTcpClient();
			client.connect();

			sleep(1000);

			server.close();

			sleep(5000);

			server.open();

			sleep(3000);

			client.disconnect();
			client.dispose();

			sleep(500);

			server.close();
			server.dispose();
		};

		runTest("testClientAutomaticReconnectionButServerClosedLater", test);
	}

	public void testClientToServerCommunication() {
		IExecutable test = () -> {
			IServer server = createDefaultTcpServer();
			server.open();

			ServerListener listener = new ServerListener(server);
			listener.setMessageHandler(event -> Logger.debug("Server received %s", new String(event.getData())));

			listener.start();

			IClient client = createDefaultTcpClient();
			client.connect();

			// Waiting for the client to be connected to the remote
			sleep(2000);

			client.getConnection().send(new Message("a message from a client".getBytes()));

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
			IServer server = createDefaultTcpServer();
			server.open();

			ServerListener listener = new ServerListener(server);
			listener.setActionOnNewClientConnected(event -> {
				event.getConnection().send(new Message("a message from the server".getBytes()));
			});

			listener.start();

			ClientConfig<IEthernetEndPoint> clientConfig = createClientConfig();
			clientConfig.setMessageHandler(event -> Logger.debug("Client received %s", new String(event.getData())));

			IClient client = Communication.createTcpClient(clientConfig);
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
			IServer server = createDefaultTcpServer();
			server.open();

			ServerListener listener = new ServerListener(server);
			listener.setMessageHandler(event -> {
				Logger.debug("Server received %s", new String(event.getData()));

				byte[] bytes = "a message from the server".getBytes();
				event.getConnection().answer(event.getIdentifier(), new Message(bytes));
			});

			listener.start();

			IClient client = createDefaultTcpClient();
			client.connect();

			sleep(2000);

			client.getConnection().send(new Message("a message from a client".getBytes(), args -> {
				if (!args.isTimeout()) {
					Logger.debug("Client received %s", new String(args.getResponse().getBytes()));
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
			IServer server = createDefaultTcpServer();
			server.open();

			ServerListener listener = new ServerListener(server);
			listener.setMessageHandler(event -> {
				Logger.debug("Server received %s, but will not respond to it", new String(event.getData()));
			});

			listener.start();

			IClient client = createDefaultTcpClient();
			client.connect();

			sleep(2000);

			client.getConnection().send(new Message("a message from a client".getBytes(), args -> {
				if (!args.isTimeout()) {
					Logger.error("Unexpected response received: %s", new String(args.getResponse().getBytes()));
				} else {
					Logger.debug("Client: Expected timeout occured");
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
			IServer server = createDefaultTcpServer();
			server.open();

			ServerListener listener = new ServerListener(server);
			listener.setActionOnNewClientConnected(event -> {
				IMessage message = new Message("a message from the server".getBytes(), args -> {
					if (!args.isTimeout()) {
						Logger.debug("Server received %s", new String(args.getResponse().getBytes()));
					} else {
						Logger.error("Server: Unexpected timeout occurred");
					}
				});

				event.getConnection().send(message);
			});

			listener.start();

			ClientConfig<IEthernetEndPoint> clientConfig = createClientConfig();
			clientConfig.setMessageHandler(event -> {
				Logger.debug("Client received %s", new String(event.getData()));

				byte[] bytes = "a message from a client".getBytes();
				event.getConnection().answer(event.getIdentifier(), new Message(bytes));
			});

			IClient client = Communication.createTcpClient(clientConfig);
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
			IServer server = createDefaultTcpServer();
			server.open();

			ServerListener listener = new ServerListener(server);
			listener.setActionOnNewClientConnected(event -> {
				IMessage message = new Message("a message from the server".getBytes(), args -> {
					if (!args.isTimeout()) {
						Logger.error("Server received %s", new String(args.getResponse().getBytes()));
					} else {
						Logger.debug("Server: Expected timeout occurred");
					}
				});

				event.getConnection().send(message);
			});

			listener.start();

			ClientConfig<IEthernetEndPoint> clientConfig = createClientConfig();
			clientConfig.setMessageHandler(event -> {
				Logger.debug("Client received %s, but will not respond to it", new String(event.getData()));
			});

			IClient client = Communication.createTcpClient(clientConfig);
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
			IServer server = createDefaultTcpServer();
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

			ClientConfig<IEthernetEndPoint> clientConfig = createClientConfig();
			clientConfig.setAutomaticReconnection(false);
			clientConfig.setLayerInitializer(() -> new LayerInitializer(new ExceptionLayer(LayerExceptionMode.UNPACK)));

			IClient client = Communication.createTcpClient(clientConfig);
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
			IServer server = createDefaultTcpServer();
			server.open();

			ServerListener listener = new ServerListener(server);
			listener.setMessageHandler(event -> {
				byte[] bytes = "a message from the server".getBytes();
				event.getConnection().answer(event.getIdentifier(), new Message(bytes));
			});

			listener.start();

			IClient client = createDefaultTcpClient();
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
						Logger.error("Unexpected timeout occured");
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
			IServer server = createDefaultTcpServer();
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

			ClientConfig<IEthernetEndPoint> clientConfig = createClientConfig();
			clientConfig.setAutomaticReconnection(false);
			clientConfig.setMessageHandler(event -> {
				throw new RuntimeException("Exception to test unstable counter");
			});

			IClient client = Communication.createTcpClient(clientConfig);
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
			IServer server = createDefaultTcpServer();
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

			ClientConfig<IEthernetEndPoint> clientConfig = createClientConfig();
			clientConfig.setClientMaxUnstableCounter(5);
			clientConfig.setClientHealTime(9000);
			clientConfig.setConnectionHealTime(500);
			clientConfig.setMessageHandler(event -> {
				throw new RuntimeException("Exception to test unstable counter");
			});

			IClient client = Communication.createTcpClient(clientConfig);
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
			ServerConfig<IEthernetEndPoint> serverConfig = createServerConfig();
			serverConfig.setLayerInitializer(() -> new RsaLayerInitializer(new SimpleCertificate()));

			IServer server = Communication.createTcpServer(serverConfig);
			server.open();

			ServerListener listener = new ServerListener(server);
			listener.setActionOnNewClientConnected(event -> {
				sleep(500);

				IMessage message = new Message("a message from the server".getBytes(), args -> {
					if (!args.isTimeout()) {
						Logger.debug("Server received %s", new String(args.getResponse().getBytes()));
					} else {
						Logger.error("Unexpected timeout occurred");
					}
				});

				event.getConnection().send(message);
			});

			listener.start();

			ClientConfig<IEthernetEndPoint> clientConfig = createClientConfig();
			clientConfig.setLayerInitializer(() -> new RsaLayerInitializer(new SimpleCertificate()));
			clientConfig.setMessageHandler(event -> {
				Logger.debug("Client received %s", new String(event.getData()));

				event.getConnection().answer(event.getIdentifier(), new Message("a message from a client".getBytes()));
			});

			IClient client = Communication.createTcpClient(clientConfig);
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
			ServerConfig<IEthernetEndPoint> serverConfig = createServerConfig();
			serverConfig.setLayerInitializer(() -> new AesLayerInitializer(new SimpleCertificate()));

			IServer server = Communication.createTcpServer(serverConfig);
			server.open();

			ServerListener listener = new ServerListener(server);
			listener.setActionOnNewClientConnected(event -> {
				sleep(500);

				IMessage message = new Message("a message from the server".getBytes(), args -> {
					if (!args.isTimeout()) {
						Logger.debug("Server received %s", new String(args.getResponse().getBytes()));
					} else {
						Logger.error("Unexpected timeout occurred");
					}
				});

				event.getConnection().send(message);
			});

			listener.start();

			ClientConfig<IEthernetEndPoint> clientConfig = createClientConfig();
			clientConfig.setLayerInitializer(() -> new AesLayerInitializer(new SimpleCertificate()));
			clientConfig.setMessageHandler(event -> {
				Logger.debug("Client received %s", new String(event.getData()));

				event.getConnection().answer(event.getIdentifier(), new Message("a message from a client".getBytes()));
			});

			IClient client = Communication.createTcpClient(clientConfig);
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
			ServerConfig<IEthernetEndPoint> serverConfig = createServerConfig();
			serverConfig.setLayerInitializer(() -> new AesSafeLayerInitializer(new SimpleCertificate()));

			IServer server = Communication.createTcpServer(serverConfig);
			server.open();

			ServerListener listener = new ServerListener(server);
			listener.setActionOnNewClientConnected(event -> {
				sleep(500);

				IMessage message = new Message("a message from the server".getBytes(), args -> {
					if (!args.isTimeout()) {
						Logger.debug("Server received %s", new String(args.getResponse().getBytes()));
					} else {
						Logger.error("Unexpected timeout occurred");
					}
				});

				event.getConnection().send(message);
			});

			listener.start();

			ClientConfig<IEthernetEndPoint> clientConfig = createClientConfig();
			clientConfig.setLayerInitializer(() -> new AesSafeLayerInitializer(new SimpleCertificate()));
			clientConfig.setMessageHandler(event -> {
				Logger.debug("Client received %s", new String(event.getData()));

				event.getConnection().answer(event.getIdentifier(), new Message("a message from a client".getBytes()));
			});

			IClient client = Communication.createTcpClient(clientConfig);
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

	public void testTwoClientsOneServer() {
		IExecutable tests = () -> {
			IClient client1 = createDefaultTcpClient();
			client1.connect();

			sleep(5000);

			IServer server = createDefaultTcpServer();
			server.open();

			ServerListener listener = new ServerListener(server);
			listener.setMessageHandler(event -> Logger.debug("Server received %s", new String(event.getData())));

			listener.start();

			sleep(2000);

			IClient client2 = createDefaultTcpClient();
			client2.connect();

			sleep(2000);

			client1.getConnection().send(new Message("a message from client1".getBytes()));
			client2.getConnection().send(new Message("a message from client2".getBytes()));

			sleep(2000);

			listener.stop();
			server.close();
			server.dispose();

			sleep(5000);

			client1.disconnect();
			client1.dispose();

			client2.disconnect();
			client2.dispose();
		};

		runTest("testTwoClientsOneServer", tests);
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

	/**
	 * Creates a server with default configuration and TCP implementation.
	 * 
	 * @return The created server.
	 */
	private static IServer createDefaultTcpServer() {
		return Communication.createDefaultTcpServer(SERVER_NAME, PORT);
	}

	/**
	 * @return Creates a client configuration with default name, address and port
	 *         number.
	 */
	private static ClientConfig<IEthernetEndPoint> createClientConfig() {
		return Communication.createClientConfig(CLIENT_NAME, new EthernetEndPoint(ADDRESS, PORT));
	}

	/**
	 * Creates a client with default configuration and TCP implementation.
	 * 
	 * @return The created client.
	 */
	private static IClient createDefaultTcpClient() {
		return Communication.createDefaultTcpClient(CLIENT_NAME, ADDRESS, PORT);
	}
}
