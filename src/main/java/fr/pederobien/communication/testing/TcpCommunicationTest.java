package fr.pederobien.communication.testing;

import fr.pederobien.communication.impl.ClientConfig;
import fr.pederobien.communication.impl.Communication;
import fr.pederobien.communication.impl.ServerConfig;
import fr.pederobien.communication.impl.connection.Message;
import fr.pederobien.communication.impl.layer.AesLayerInitializer;
import fr.pederobien.communication.impl.layer.AesSafeLayerInitializer;
import fr.pederobien.communication.impl.layer.LayerInitializer;
import fr.pederobien.communication.impl.layer.RsaLayerInitializer;
import fr.pederobien.communication.interfaces.client.IClient;
import fr.pederobien.communication.interfaces.server.IServer;
import fr.pederobien.communication.testing.tools.DoOnceConnected;
import fr.pederobien.communication.testing.tools.ExceptionLayer;
import fr.pederobien.communication.testing.tools.ExceptionLayer.LayerExceptionMode;
import fr.pederobien.communication.testing.tools.RequestHandler;
import fr.pederobien.communication.testing.tools.SimpleCertificate;
import fr.pederobien.utils.AsyncConsole;
import fr.pederobien.utils.IExecutable;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.LogEvent;
import fr.pederobien.utils.event.LogEvent.ELogLevel;

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
			ServerConfig serverConfig = createServerConfig();
			serverConfig.setOnUnexpectedRequestReceived(new RequestHandler(event -> {
				EventManager.callEvent(new LogEvent("Server received %s", new String(event.getData())));
			}));

			IServer server = Communication.createTcpServer(serverConfig);
			server.open();

			IClient client = createDefaultTcpClient();
			client.connect();

			// Waiting for the client to be connected to the remote
			sleep(2000);

			client.getConnection().send(new Message("a message from a client".getBytes()));

			sleep(2000);

			client.disconnect();
			client.dispose();

			sleep(500);

			server.close();
			server.dispose();
		};

		runTest("testClientToServerCommunication", test);
	}

	public void testServerToClientCommunication() {
		IExecutable test = () -> {
			IServer server = createDefaultTcpServer();
			server.open();

			DoOnceConnected sendToClient = new DoOnceConnected(server, event -> {
				byte[] bytes = "a message from the server".getBytes();
				event.getClient().getConnection().send(new Message(bytes));
			});
			sendToClient.start();

			ClientConfig clientConfig = createClientConfig();
			clientConfig.setOnUnexpectedRequestReceived(new RequestHandler(event -> {
				EventManager.callEvent(new LogEvent("Client received %s", new String(event.getData())));
			}));

			IClient client = Communication.createTcpClient(clientConfig);
			client.connect();

			sleep(2000);

			sendToClient.stop();

			client.disconnect();
			client.dispose();

			sleep(500);

			server.close();
			server.dispose();
		};

		runTest("testServerToClientCommunication", test);
	}

	public void testClientToServerWithCallback() {
		IExecutable test = () -> {
			ServerConfig serverConfig = createServerConfig();
			serverConfig.setOnUnexpectedRequestReceived(new RequestHandler(event -> {
				EventManager.callEvent(new LogEvent("Server received %s", new String(event.getData())));
				byte[] bytes = "a message from the server".getBytes();
				event.getConnection().answer(event.getIdentifier(), new Message(bytes));
			}));

			IServer server = Communication.createTcpServer(serverConfig);
			server.open();

			IClient client = createDefaultTcpClient();
			client.connect();

			sleep(2000);

			client.getConnection().send(new Message("a message from a client".getBytes(), args -> {
				if (!args.isTimeout()) {
					String received = new String(args.getResponse().getBytes());
					EventManager.callEvent(new LogEvent("Client received %s", received));
				} else {
					EventManager.callEvent(new LogEvent(ELogLevel.ERROR, "Unexpected timeout occurred"));
				}
			}));

			sleep(2000);

			client.disconnect();
			client.dispose();

			sleep(500);

			server.close();
			server.dispose();
		};

		runTest("testClientToServerWithCallback", test);
	}

	public void testClientToServerWithCallbackButTimeout() {
		IExecutable test = () -> {
			ServerConfig serverConfig = createServerConfig();
			serverConfig.setOnUnexpectedRequestReceived(new RequestHandler(event -> {
				String formatter = "Server received %s, but will not respond to it";
				EventManager.callEvent(new LogEvent(formatter, new String(event.getData())));
			}));

			IServer server = Communication.createTcpServer(serverConfig);
			server.open();

			IClient client = createDefaultTcpClient();
			client.connect();

			sleep(2000);

			client.getConnection().send(new Message("a message from a client".getBytes(), args -> {
				if (!args.isTimeout()) {
					String received = new String(args.getResponse().getBytes());
					EventManager.callEvent(new LogEvent(ELogLevel.ERROR, "Unexpected response received: %s", received));
				} else {
					EventManager.callEvent(new LogEvent("Client: Expected timeout occured"));
				}
			}));

			sleep(2000);

			client.disconnect();
			client.dispose();

			sleep(500);

			server.close();
			server.dispose();
		};

		runTest("testClientToServerWithCallbackButTimeout", test);
	}

	public void testServerToClientWithCallback() {
		IExecutable test = () -> {
			IServer server = createDefaultTcpServer();
			server.open();

			DoOnceConnected sendToClient = new DoOnceConnected(server, event -> {
				Message message = new Message("a message from the server".getBytes(), args -> {
					if (!args.isTimeout()) {
						String received = new String(args.getResponse().getBytes());
						EventManager.callEvent(new LogEvent("Server received %s", received));
					} else {
						EventManager.callEvent(new LogEvent(ELogLevel.ERROR, "Server: Unexpected timeout occurred"));
					}
				});

				event.getClient().getConnection().send(message);
			});

			sendToClient.start();

			ClientConfig clientConfig = createClientConfig();
			clientConfig.setOnUnexpectedRequestReceived(new RequestHandler(event -> {
				EventManager.callEvent(new LogEvent("Client received %s", new String(event.getData())));
				byte[] bytes = "a message from a client".getBytes();
				event.getConnection().answer(event.getIdentifier(), new Message(bytes));
			}));

			IClient client = Communication.createTcpClient(clientConfig);
			client.connect();

			sleep(2000);

			sendToClient.stop();

			client.disconnect();
			client.dispose();

			sleep(500);

			server.close();
			server.dispose();
		};

		runTest("testServerToClientWithCallback", test);
	}

	public void testServerToClientWithCallbackButTimeout() {
		IExecutable test = () -> {
			IServer server = createDefaultTcpServer();
			server.open();

			DoOnceConnected sendToClient = new DoOnceConnected(server, event -> {
				Message message = new Message("a message from the server".getBytes(), args -> {
					if (!args.isTimeout()) {
						String received = new String(args.getResponse().getBytes());
						EventManager.callEvent(new LogEvent(ELogLevel.ERROR, "Server received %s", received));
					} else {
						EventManager.callEvent(new LogEvent("Server: Expected timeout occurred"));
					}
				});

				event.getClient().getConnection().send(message);
			});

			sendToClient.start();

			ClientConfig clientConfig = createClientConfig();
			clientConfig.setOnUnexpectedRequestReceived(new RequestHandler(event -> {
				String formatter = "Client received %s, but will not respond to it";
				EventManager.callEvent(new LogEvent(formatter, new String(event.getData())));
			}));

			IClient client = Communication.createTcpClient(clientConfig);
			client.connect();

			sleep(2000);

			sendToClient.stop();

			client.disconnect();
			client.dispose();

			sleep(500);

			server.close();
			server.dispose();
		};

		runTest("testServerToClientWithCallbackButTimeout", test);
	}

	public void testExtractionException() {
		IExecutable test = () -> {
			IServer server = createDefaultTcpServer();
			server.open();

			DoOnceConnected sendToClient = new DoOnceConnected(server, event -> {
				sleep(500);
				for (int i = 0; i < 18 && !event.getClient().getConnection().isDisposed(); i++) {
					AsyncConsole.printlnWithTimeStamp("Extracting message %s", i);
					byte[] bytes = "a message from the server".getBytes();
					event.getClient().getConnection().send(new Message(bytes));

					sleep(500);
				}
			});

			sendToClient.start();

			ClientConfig clientConfig = createClientConfig();
			clientConfig.setAutomaticReconnection(false);
			clientConfig.setLayerInitializer(new LayerInitializer(new ExceptionLayer(LayerExceptionMode.UNPACK)));

			IClient client = Communication.createTcpClient(clientConfig);
			client.connect();

			sleep(250);

			AsyncConsole.printlnWithTimeStamp("Expecting unstable connection after extracting 17 messages");

			sleep(11000);

			sendToClient.stop();

			client.disconnect();
			client.dispose();

			sleep(500);

			server.close();
			server.dispose();
		};

		runTest("testExtractionException", test);
	}

	public void testCallbackException() {
		IExecutable test = () -> {
			ServerConfig serverConfig = createServerConfig();
			serverConfig.setOnUnexpectedRequestReceived(new RequestHandler(event -> {
				byte[] bytes = "a message from the server".getBytes();
				event.getConnection().answer(event.getIdentifier(), new Message(bytes));
			}));

			IServer server = Communication.createTcpServer(serverConfig);
			server.open();

			IClient client = createDefaultTcpClient();
			client.connect();

			sleep(250);

			AsyncConsole.printlnWithTimeStamp("Expecting unstable connection after sending 17 messages");

			for (int i = 0; i < 18; i++) {
				AsyncConsole.printlnWithTimeStamp("Sending message %s", i);
				String message = "a message from a client";
				client.getConnection().send(new Message(message.getBytes(), args -> {
					if (!args.isTimeout()) {
						throw new RuntimeException("Exception to test unstable counter");
					} else {
						EventManager.callEvent(new LogEvent(ELogLevel.DEBUG, "Unexpected timeout occured"));
					}
				}));

				sleep(500);
			}

			sleep(2000);

			client.disconnect();
			client.dispose();

			sleep(500);

			server.close();
			server.dispose();
		};

		runTest("testCallbackException", test);
	}

	public void testUnexpectedRequestException() {
		IExecutable test = () -> {
			IServer server = createDefaultTcpServer();
			server.open();

			DoOnceConnected sendToClient = new DoOnceConnected(server, event -> {
				sleep(500);
				for (int i = 0; i < 18 && !event.getClient().getConnection().isDisposed(); i++) {
					AsyncConsole.printlnWithTimeStamp("Server Sending message %s", i);
					byte[] bytes = "a message from the server".getBytes();
					event.getClient().getConnection().send(new Message(bytes));

					sleep(500);
				}
			});

			sendToClient.start();

			ClientConfig clientConfig = createClientConfig();
			clientConfig.setAutomaticReconnection(false);
			clientConfig.setOnUnexpectedRequestReceived(new RequestHandler(event -> {
				throw new RuntimeException("Exception to test unstable counter");
			}));

			IClient client = Communication.createTcpClient(clientConfig);
			client.connect();

			sleep(12000);

			client.disconnect();
			client.dispose();

			sleep(500);

			server.close();
			server.dispose();
		};

		runTest("testUnexpectedRequestException", test);
	}

	public void testUnstableClient() {
		IExecutable test = () -> {
			IServer server = createDefaultTcpServer();
			server.open();

			DoOnceConnected sendToClient = new DoOnceConnected(server, event -> {
				for (int i = 0; i < 18; i++) {
					if (event.getClient().getConnection().isDisposed()) {
						break;
					}

					AsyncConsole.printlnWithTimeStamp("Server Sending message %s", i);
					byte[] bytes = "a message from the server".getBytes();
					event.getClient().getConnection().send(new Message(bytes));

					sleep(250);
				}
			});

			sendToClient.start();

			ClientConfig clientConfig = createClientConfig();
			clientConfig.setClientMaxUnstableCounter(5);
			clientConfig.setClientHealTime(9000);
			clientConfig.setConnectionHealTime(500);
			clientConfig.setOnUnexpectedRequestReceived(new RequestHandler(event -> {
				throw new RuntimeException("Exception to test unstable counter");
			}));

			IClient client = Communication.createTcpClient(clientConfig);
			client.connect();

			sleep(250);

			AsyncConsole.printlnWithTimeStamp("Expecting unstable client after receiving %s unexpected messages",
					17 * clientConfig.getClientMaxUnstableCounterValue());

			sleep(40000);

			client.disconnect();
			client.dispose();

			sleep(500);

			server.close();
			server.dispose();
		};

		runTest("testUnstableClient", test);
	}

	public void testRsaLayer() {
		IExecutable test = () -> {
			ServerConfig serverConfig = createServerConfig();
			serverConfig.setLayerInitializer(new RsaLayerInitializer(new SimpleCertificate()));

			IServer server = Communication.createTcpServer(serverConfig);
			server.open();

			DoOnceConnected sendToClient = new DoOnceConnected(server, event -> {
				sleep(500);

				Message message = new Message("a message from the server".getBytes(), args -> {
					if (!args.isTimeout()) {
						String received = new String(args.getResponse().getBytes());
						EventManager.callEvent(new LogEvent("Server received %s", received));
					} else {
						EventManager.callEvent(new LogEvent(ELogLevel.ERROR, "Unexpected timeout occurred"));
					}
				});

				event.getClient().getConnection().send(message);
			});

			sendToClient.start();

			ClientConfig clientConfig = createClientConfig();
			clientConfig.setLayerInitializer(new RsaLayerInitializer(new SimpleCertificate()));
			clientConfig.setOnUnexpectedRequestReceived(new RequestHandler(event -> {
				String received = new String(event.getData());
				EventManager.callEvent(new LogEvent("Client received %s", received));

				event.getConnection().answer(event.getIdentifier(), new Message("a message from a client".getBytes()));
			}));

			IClient client = Communication.createTcpClient(clientConfig);
			client.connect();

			sleep(3000);

			client.disconnect();
			client.dispose();

			sleep(500);

			server.close();
			server.dispose();
		};

		runTest("testRsaLayer", test);
	}

	public void testAesLayer() {
		IExecutable test = () -> {
			ServerConfig serverConfig = createServerConfig();
			serverConfig.setLayerInitializer(new AesLayerInitializer(new SimpleCertificate()));

			IServer server = Communication.createTcpServer(serverConfig);
			server.open();

			DoOnceConnected sendToClient = new DoOnceConnected(server, event -> {
				sleep(500);
				Message message = new Message("a message from the server".getBytes(), args -> {
					if (!args.isTimeout()) {
						String received = new String(args.getResponse().getBytes());
						EventManager.callEvent(new LogEvent("Server received %s", received));
					} else {
						EventManager.callEvent(new LogEvent(ELogLevel.ERROR, "Unexpected timeout occurred"));
					}
				});

				event.getClient().getConnection().send(message);
			});

			sendToClient.start();

			ClientConfig clientConfig = createClientConfig();
			clientConfig.setLayerInitializer(new AesLayerInitializer(new SimpleCertificate()));
			clientConfig.setOnUnexpectedRequestReceived(new RequestHandler(event -> {
				String received = new String(event.getData());
				EventManager.callEvent(new LogEvent("Client received %s", received));

				event.getConnection().answer(event.getIdentifier(), new Message("a message from a client".getBytes()));
			}));

			IClient client = Communication.createTcpClient(clientConfig);
			client.connect();

			sleep(3000);

			client.disconnect();
			client.dispose();

			sleep(500);

			server.close();
			server.dispose();
		};

		runTest("testAesLayer", test);
	}

	public void testAesSafeLayer() {
		IExecutable test = () -> {
			ServerConfig serverConfig = createServerConfig();
			serverConfig.setLayerInitializer(new AesSafeLayerInitializer(new SimpleCertificate()));

			IServer server = Communication.createTcpServer(serverConfig);
			server.open();

			DoOnceConnected sendToClient = new DoOnceConnected(server, event -> {
				sleep(500);
				Message message = new Message("a message from the server".getBytes(), args -> {
					if (!args.isTimeout()) {
						String received = new String(args.getResponse().getBytes());
						EventManager.callEvent(new LogEvent("Server received %s", received));
					} else {
						EventManager.callEvent(new LogEvent(ELogLevel.ERROR, "Unexpected timeout occurred"));
					}
				});

				event.getClient().getConnection().send(message);
			});

			sendToClient.start();

			ClientConfig clientConfig = createClientConfig();
			clientConfig.setLayerInitializer(new AesSafeLayerInitializer(new SimpleCertificate()));
			clientConfig.setOnUnexpectedRequestReceived(new RequestHandler(event -> {
				String received = new String(event.getData());
				EventManager.callEvent(new LogEvent("Client received %s", received));

				event.getConnection().answer(event.getIdentifier(), new Message("a message from a client".getBytes()));
			}));

			IClient client = Communication.createTcpClient(clientConfig);
			client.connect();

			sleep(3000);

			client.disconnect();
			client.dispose();

			sleep(500);

			server.close();
			server.dispose();
		};

		runTest("testAesSafeLayer", test);
	}

	private void runTest(String testName, IExecutable test) {
		EventManager.callEvent(new LogEvent(ELogLevel.DEBUG, "Begin %s", testName));
		try {
			test.exec();
		} catch (Exception e) {
			EventManager.callEvent(new LogEvent(ELogLevel.ERROR, "Unexpected error: %s", e.getMessage()));
			for (StackTraceElement trace : e.getStackTrace()) {
				EventManager.callEvent(new LogEvent(ELogLevel.ERROR, trace.toString()));
			}
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

	/**
	 * @return Creates a server configuration with default name and port number.
	 */
	private static ServerConfig createServerConfig() {
		return Communication.createServerConfig(SERVER_NAME, PORT);
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
	private static ClientConfig createClientConfig() {
		return Communication.createClientConfig(CLIENT_NAME, ADDRESS, PORT);
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
