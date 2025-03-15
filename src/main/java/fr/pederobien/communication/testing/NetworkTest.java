package fr.pederobien.communication.testing;

import fr.pederobien.communication.impl.ClientConfig;
import fr.pederobien.communication.impl.Communication;
import fr.pederobien.communication.impl.ServerConfig;
import fr.pederobien.communication.impl.connection.Message;
import fr.pederobien.communication.impl.layer.LayerInitializer;
import fr.pederobien.communication.interfaces.client.IClient;
import fr.pederobien.communication.interfaces.server.IServer;
import fr.pederobien.communication.testing.tools.DoOnceConnected;
import fr.pederobien.communication.testing.tools.ExceptionLayer;
import fr.pederobien.communication.testing.tools.ExceptionLayer.LayerExceptionMode;
import fr.pederobien.communication.testing.tools.Network;
import fr.pederobien.communication.testing.tools.Network.ExceptionMode;
import fr.pederobien.communication.testing.tools.NetworkCorruptor;
import fr.pederobien.communication.testing.tools.RequestHandler;
import fr.pederobien.utils.AsyncConsole;
import fr.pederobien.utils.IExecutable;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.LogEvent;
import fr.pederobien.utils.event.LogEvent.ELogLevel;

public class NetworkTest {
	private static final String SERVER_NAME = "Dummy Server";
	private static final String CLIENT_NAME = "Dummy Client";
	private static final String ADDRESS = "127.0.01";
	private static final int PORT = 12345;

	public void testServerInitialisation() {
		IExecutable test = () -> {
			Network network = new Network();

			IServer server = createDefaultCustomServer(network);
			server.open();

			sleep(2000);

			server.close();
		};

		runTest("testServerInitialisation", test);
	}

	public void testClientAutomaticReconnection() {
		IExecutable test = () -> {
			Network network = new Network();

			IClient client = createDefaultCustomClient(network);
			client.connect();

			sleep(5000);

			client.disconnect();
			client.dispose();
		};

		runTest("testClientAutomaticReconnection", test);
	}

	public void testClientAutomaticReconnectionButWithServerOpenedLater() {
		IExecutable test = () -> {
			Network network = new Network();

			IClient client = createDefaultCustomClient(network);
			client.connect();

			sleep(3000);

			IServer server = createDefaultCustomServer(network);
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
			Network network = new Network();

			IServer server = createDefaultCustomServer(network);
			server.open();

			sleep(500);

			IClient client = createDefaultCustomClient(network);
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
			Network network = new Network();

			ServerConfig serverConfig = createServerConfig();
			serverConfig.setOnUnexpectedRequestReceived(new RequestHandler(event -> {
				EventManager.callEvent(new LogEvent("Server received %s", new String(event.getData())));
			}));

			IServer server = Communication.createCustomServer(serverConfig, network.getServer());
			server.open();

			IClient client = createDefaultCustomClient(network);
			client.connect();

			// Waiting for the client to be connected to the remote
			sleep(1000);

			client.getConnection().send(new Message("a message from a client".getBytes()));

			sleep(1000);

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
			Network network = new Network();

			IServer server = createDefaultCustomServer(network);
			server.open();

			DoOnceConnected sendToClient = new DoOnceConnected(server, event -> {
				event.getClient().getConnection().send(new Message("a message from the server".getBytes()));
			});

			sendToClient.start();

			ClientConfig clientConfig = createClientConfig();
			clientConfig.setOnUnexpectedRequestReceived(new RequestHandler(event -> {
				EventManager.callEvent(new LogEvent("Client received %s", new String(event.getData())));
			}));

			IClient client = Communication.createCustomClient(clientConfig, network.newClient());
			client.connect();

			sleep(1000);

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
			Network network = new Network();

			ServerConfig serverConfig = createServerConfig();
			serverConfig.setOnUnexpectedRequestReceived(new RequestHandler(event -> {
				EventManager.callEvent(new LogEvent("Server received %s", new String(event.getData())));
				Message message = new Message("a message from the server".getBytes());
				event.getConnection().answer(event.getIdentifier(), message);
			}));

			IServer server = Communication.createCustomServer(serverConfig, network.getServer());
			server.open();

			IClient client = createDefaultCustomClient(network);
			client.connect();

			sleep(1000);

			client.getConnection().send(new Message("a message from a client".getBytes(), args -> {
				if (!args.isTimeout()) {
					String received = new String(args.getResponse().getBytes());
					EventManager.callEvent(new LogEvent("Client received %s", received));
				} else {
					EventManager.callEvent(new LogEvent(ELogLevel.DEBUG, "Client: Unexpected timeout occurred"));
				}
			}));

			sleep(1000);

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
			Network network = new Network();

			ServerConfig serverConfig = createServerConfig();
			serverConfig.setOnUnexpectedRequestReceived(new RequestHandler(event -> {
				String formatter = "Server received %s, but will not respond to it";
				EventManager.callEvent(new LogEvent(formatter, new String(event.getData())));
			}));

			IServer server = Communication.createCustomServer(serverConfig, network.getServer());
			server.open();

			IClient client = createDefaultCustomClient(network);
			client.connect();

			sleep(1000);

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
			Network network = new Network();

			IServer server = createDefaultCustomServer(network);
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
				event.getConnection().answer(event.getIdentifier(), new Message("a message from a client".getBytes()));
			}));

			IClient client = Communication.createCustomClient(clientConfig, network.newClient());
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
			Network network = new Network();

			IServer server = createDefaultCustomServer(network);
			server.open();

			DoOnceConnected sendToClient = new DoOnceConnected(server, event -> {
				Message message = new Message("a message from the server".getBytes(), args -> {
					if (!args.isTimeout()) {
						String received = new String(args.getResponse().getBytes());
						String formatter = "Unexpected message received: %s";
						EventManager.callEvent(new LogEvent(ELogLevel.ERROR, formatter, received));
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

			IClient client = Communication.createCustomClient(clientConfig, network.newClient());
			client.connect();

			sleep(3000);

			sendToClient.stop();

			client.disconnect();
			client.dispose();

			sleep(500);

			server.close();
			server.dispose();
		};

		runTest("testServerToClientWithCallbackButTimeout", test);
	}

	public void testInitialisationFailure() {
		IExecutable test = () -> {
			Network network = new Network();

			IServer server = createDefaultCustomServer(network);
			server.open();

			ClientConfig clientConfig = createClientConfig();
			clientConfig.setLayerInitializer(new LayerInitializer(token -> {
				throw new RuntimeException("Exception to test unstable counter");
			}));

			IClient client = Communication.createCustomClient(clientConfig, network.newClient());
			client.connect();

			sleep(5000);

			client.disconnect();
			client.dispose();

			sleep(500);

			server.close();
			server.dispose();
		};

		runTest("testInitialisationFailure", test);
	}

	public void testSendingException() {
		IExecutable test = () -> {
			Network network = new Network();

			IServer server = createDefaultCustomServer(network);
			server.open();

			ClientConfig clientConfig = createClientConfig();
			clientConfig.setAutomaticReconnection(false);

			IClient client = Communication.createCustomClient(clientConfig, network.newClient(ExceptionMode.SEND));
			client.connect();

			sleep(1000);

			AsyncConsole.printlnWithTimeStamp("Expecting unstable connection after sending 17 messages");
			for (int i = 0; i < 18; i++) {
				AsyncConsole.printlnWithTimeStamp("Sending message %s", i);
				client.getConnection().send(new Message(new byte[0]));
				sleep(500);
			}

			sleep(2000);

			client.disconnect();
			client.dispose();

			sleep(500);

			server.close();
			server.dispose();
		};

		runTest("testSendingException", test);
	}

	public void testReceivingException() {
		IExecutable test = () -> {
			Network network = new Network();

			IServer server = createDefaultCustomServer(network);
			server.open();

			// In receive mode, no need for the server to send request to the client

			ClientConfig clientConfig = createClientConfig();
			clientConfig.setAutomaticReconnection(false);

			IClient client = Communication.createCustomClient(clientConfig, network.newClient(ExceptionMode.RECEIVE));
			client.connect();

			sleep(250);

			AsyncConsole.printlnWithTimeStamp("Expecting unstable connection after receiving 17 messages");

			sleep(11000);

			client.disconnect();
			client.dispose();

			sleep(500);

			server.close();
			server.dispose();
		};

		runTest("testReceivingException", test);
	}

	public void testExtractionException() {
		IExecutable test = () -> {
			Network network = new Network();

			IServer server = createDefaultCustomServer(network);
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
			clientConfig.setLayerInitializer(new LayerInitializer(new ExceptionLayer(LayerExceptionMode.UNPACK)));
			clientConfig.setAutomaticReconnection(false);

			IClient client = Communication.createCustomClient(clientConfig, network.newClient());
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
			Network network = new Network();

			ServerConfig serverConfig = createServerConfig();
			serverConfig.setOnUnexpectedRequestReceived(new RequestHandler(event -> {
				byte[] bytes = "a message from the server".getBytes();
				event.getConnection().answer(event.getIdentifier(), new Message(bytes));
			}));

			IServer server = Communication.createCustomServer(serverConfig, network.getServer());
			server.open();

			IClient client = createDefaultCustomClient(network);
			client.connect();

			sleep(250);

			AsyncConsole.printlnWithTimeStamp("Expecting unstable connection after sending 17 messages");

			for (int i = 0; i < 18 && !client.getConnection().isDisposed(); i++) {
				AsyncConsole.printlnWithTimeStamp("Sending message %s", i);

				String message = "a message from a client";
				client.getConnection().send(new Message(message.getBytes(), args -> {
					if (!args.isTimeout()) {
						throw new RuntimeException("Exception to test unstable counter");
					} else {
						EventManager.callEvent(new LogEvent(ELogLevel.ERROR, "Unexpected timeout occured"));
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
			Network network = new Network();

			IServer server = createDefaultCustomServer(network);
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

			IClient client = Communication.createCustomClient(clientConfig, network.newClient());
			client.connect();

			sleep(250);

			AsyncConsole.printlnWithTimeStamp("Expecting unstable connection after receiving 17 unexpected messages");

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
			Network network = new Network();

			IServer server = createDefaultCustomServer(network);
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

			IClient client = Communication.createCustomClient(clientConfig, network.newClient());
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

	public void testTwoClientsOneServer() {
		IExecutable tests = () -> {
			Network network = new Network();

			IClient client1 = createDefaultCustomClient(network);
			client1.connect();

			sleep(5000);

			ServerConfig serverConfig = createServerConfig();
			serverConfig.setOnUnexpectedRequestReceived(new RequestHandler(event -> {
				EventManager.callEvent(new LogEvent("Server received %s", new String(event.getData())));
			}));

			IServer server = Communication.createCustomServer(serverConfig, network.getServer());
			server.open();

			sleep(2000);

			IClient client2 = createDefaultCustomClient(network);
			client2.connect();

			sleep(2000);

			client1.getConnection().send(new Message("a message from client1".getBytes()));
			client2.getConnection().send(new Message("a message from client2".getBytes()));

			sleep(2000);

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

	public void testNetworkIssues() {
		IExecutable test = () -> {
			NetworkCorruptor corruptor = new NetworkCorruptor();

			// Each data sent to the server will be corrupted
			for (int i = 0; i < 10; i++) {
				corruptor.registerClientToServerCorruption(i);
			}

			Network network = new Network(corruptor);

			ServerConfig serverConfig = createServerConfig();
			serverConfig.setOnUnexpectedRequestReceived(new RequestHandler(event -> {
				EventManager.callEvent(new LogEvent("Server received %s", new String(event.getData())));
			}));

			IServer server = Communication.createCustomServer(serverConfig, network.getServer());
			server.open();

			IClient client = createDefaultCustomClient(network);
			client.connect();

			sleep(1000);

			for (int i = 0; i < 10; i++) {
				client.getConnection().send(new Message("a message from a client".getBytes()));
				sleep(100);
			}

			sleep(2000);

			client.disconnect();
			client.dispose();

			sleep(500);

			server.close();
			server.dispose();
		};

		runTest("testNetworkIssues", test);
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
	 * Creates a server with default configuration
	 * 
	 * @param network The network that provide the server implementation to use.
	 * 
	 * @return The created server.
	 */
	private static IServer createDefaultCustomServer(Network network) {
		return Communication.createDefaultCustomServer(SERVER_NAME, PORT, network.getServer());
	}

	/**
	 * @return Creates a client configuration with default name, address and port
	 *         number.
	 */
	private static ClientConfig createClientConfig() {
		return Communication.createClientConfig(CLIENT_NAME, ADDRESS, PORT);
	}

	/**
	 * Creates a client with default configuration.
	 * 
	 * @param network The network that provide the client implementation to use.
	 * 
	 * @return The created client.
	 */
	private static IClient createDefaultCustomClient(Network network) {
		return Communication.createDefaultCustomClient(CLIENT_NAME, ADDRESS, PORT, network.newClient());
	}
}
