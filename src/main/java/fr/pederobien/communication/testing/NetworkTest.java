package fr.pederobien.communication.testing;

import java.util.Random;

import fr.pederobien.communication.impl.Communication;
import fr.pederobien.communication.impl.client.ClientConfigBuilder;
import fr.pederobien.communication.impl.connection.Message;
import fr.pederobien.communication.impl.server.ServerConfigBuilder;
import fr.pederobien.communication.interfaces.IClient;
import fr.pederobien.communication.interfaces.IServer;
import fr.pederobien.communication.testing.tools.CallbackSendMessageToClientOnceConnected;
import fr.pederobien.communication.testing.tools.ExceptionLayer;
import fr.pederobien.communication.testing.tools.ExceptionLayer.LayerExceptionMode;
import fr.pederobien.communication.testing.tools.Network;
import fr.pederobien.communication.testing.tools.Network.ExceptionMode;
import fr.pederobien.communication.testing.tools.Network.INetworkSimulator;
import fr.pederobien.communication.testing.tools.SimpleAnswerToRequestListener;
import fr.pederobien.communication.testing.tools.SimpleClientListener;
import fr.pederobien.communication.testing.tools.SimpleSendMessageToClientOnceConnected;
import fr.pederobien.communication.testing.tools.SimpleServerListener;
import fr.pederobien.utils.IExecutable;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.LogEvent;
import fr.pederobien.utils.event.LogEvent.ELogLevel;

public class NetworkTest {
	
	public void testServerInitialisation() {
		IExecutable test = () -> {
			Network network = new Network();

			IServer server = Communication.createDefaultCustomServer("Dummy Server", 12345, network.getServer());
			server.open();

			sleep(1000);

			server.close();
		};

		runTest("testServerInitialisation", test);
	}

	public void testClientAutomaticReconnection() {
		IExecutable test = () -> {
			Network network = new Network();

			ClientConfigBuilder builder = Communication.createClientConfigBuilder("127.0.0.1", 12345);
			builder.setConnectionTimeout(500);
			builder.setReconnectionDelay(500);

			IClient client = Communication.createCustomClient(builder.build(), network.newClient());
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

			ClientConfigBuilder builder = Communication.createClientConfigBuilder("127.0.0.1", 12345);
			builder.setConnectionTimeout(500);
			builder.setReconnectionDelay(500);

			IClient client = Communication.createCustomClient(builder.build(), network.newClient());
			client.connect();

			sleep(3000);

			IServer server = Communication.createDefaultCustomServer("Dummy Server", 12345, network.getServer());
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

			IServer server = Communication.createDefaultCustomServer("Dummy Server", 12345, network.getServer());
			server.open();

			sleep(500);

			ClientConfigBuilder builder = Communication.createClientConfigBuilder("127.0.0.1", 12345);
			builder.setConnectionTimeout(500);
			builder.setReconnectionDelay(500);

			IClient client = Communication.createCustomClient(builder.build(), network.newClient());
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

			ServerConfigBuilder builder = Communication.createServerConfigBuilder("Dummy Server", 12345);
			builder.setOnUnexpectedRequestReceived(new SimpleServerListener());

			IServer server = Communication.createCustomServer(builder.build(), network.getServer());
			server.open();

			IClient client = Communication.createDefaultCustomClient("127.0.0.1", 12345, network.newClient());
			client.connect();

			// Waiting for the client to be connected to the remote
			sleep(1000);

			client.getConnection().send(new Message("Hello World !".getBytes()));

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

			IServer server = Communication.createDefaultCustomServer("Dummy Server", 12345, network.getServer());
			server.open();

			SimpleSendMessageToClientOnceConnected sendToClient = new SimpleSendMessageToClientOnceConnected(server, "You are connected !", 1);
			sendToClient.start();

			ClientConfigBuilder clientBuilder = Communication.createClientConfigBuilder("127.0.0.1", 12345);
			clientBuilder.setOnUnexpectedRequestReceived(new SimpleClientListener(false));

			IClient client = Communication.createCustomClient(clientBuilder.build(), network.newClient());
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

			ServerConfigBuilder serverBuilder = Communication.createServerConfigBuilder("Dummy Server", 12345);
			serverBuilder.setOnUnexpectedRequestReceived(new SimpleAnswerToRequestListener("I received your request !"));

			IServer server = Communication.createCustomServer(serverBuilder.build(), network.getServer());
			server.open();

			IClient client = Communication.createDefaultCustomClient("127.0.0.1", 12345, network.newClient());
			client.connect();

			sleep(1000);

			client.getConnection().send(new Message("Hello world !".getBytes(), args -> {
				if (!args.isTimeout()) {
					String received = new String(args.getResponse().getBytes());
					EventManager.callEvent(new LogEvent("Response received: %s", received));
				}
				else
					EventManager.callEvent(new LogEvent(ELogLevel.DEBUG, "Unexpected timeout occurred"));
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

			IServer server = Communication.createDefaultCustomServer("Dummy Server", 12345, network.getServer());
			server.open();

			IClient client = Communication.createDefaultCustomClient("127.0.0.1", 12345, network.newClient());
			client.connect();

			sleep(1000);

			client.getConnection().send(new Message("Hello world !".getBytes(), args -> {
				if (!args.isTimeout()) {
					String received = new String(args.getResponse().getBytes());
					EventManager.callEvent(new LogEvent(ELogLevel.ERROR, "Unexpected response received: %s", received));
				} else
					EventManager.callEvent(new LogEvent("Expected timeout occured"));
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

			IServer server = Communication.createDefaultCustomServer("Dummy Server", 12345, network.getServer());
			server.open();

			CallbackSendMessageToClientOnceConnected sendToClient = new CallbackSendMessageToClientOnceConnected(server, args -> {
				if (!args.isTimeout()) {
					String received = new String(args.getResponse().getBytes());
					EventManager.callEvent(new LogEvent(ELogLevel.DEBUG, "Server received: %s", received));
				}
				else
					EventManager.callEvent(new LogEvent(ELogLevel.ERROR, "Unexpected timeout occurred"));
			});

			sendToClient.start();

			ClientConfigBuilder clientBuilder = Communication.createClientConfigBuilder("127.0.0.1", 12345);
			clientBuilder.setOnUnexpectedRequestReceived(new SimpleAnswerToRequestListener("I guess I am !"));

			IClient client = Communication.createCustomClient(clientBuilder.build(), network.newClient());
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

			IServer server = Communication.createDefaultCustomServer("Dummy Server", 12345, network.getServer());
			server.open();

			CallbackSendMessageToClientOnceConnected sendToClient = new CallbackSendMessageToClientOnceConnected(server, args -> {
				if (!args.isTimeout()) {
					String received = new String(args.getResponse().getBytes());
					EventManager.callEvent(new LogEvent(ELogLevel.ERROR, "Unexpected message received: %s", received));
				}
				else
					EventManager.callEvent(new LogEvent(ELogLevel.DEBUG, "Expected timeout occurred"));
			});

			sendToClient.start();

			IClient client = Communication.createDefaultCustomClient("127.0.0.1", 12345, network.newClient());
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

			IServer server = Communication.createDefaultCustomServer("Dummy Server", 12345, network.getServer());
			server.open();

			ClientConfigBuilder builder = Communication.createClientConfigBuilder("127.0.0.1", 12345);
			builder.setLayer(new ExceptionLayer(LayerExceptionMode.INITIALISATION));

			IClient client = Communication.createCustomClient(builder.build(), network.newClient());
			client.connect();

			sleep(2000);

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

			IServer server = Communication.createDefaultCustomServer("Dummy Server", 12345, network.getServer());
			server.open();

			IClient client = Communication.createDefaultCustomClient("127.0.0.1", 12345, network.newClient(ExceptionMode.SEND));
			client.connect();

			sleep(1000);

			for (int i = 0; i < 15; i++)
				client.getConnection().send(new Message(new byte[0]));

			sleep(3000);

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

			IServer server = Communication.createDefaultCustomServer("Dummy Server", 12345, network.getServer());
			server.open();

			IClient client = Communication.createDefaultCustomClient("127.0.0.1", 12345, network.newClient(ExceptionMode.RECEIVE));
			client.connect();

			sleep(3000);

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

			IServer server = Communication.createDefaultCustomServer("Dummy Server", 12345, network.getServer());
			server.open();

			SimpleSendMessageToClientOnceConnected sendToClient = new SimpleSendMessageToClientOnceConnected(server, "I'm spamming you !", 10);
			sendToClient.start();

			ClientConfigBuilder clientBuilder = Communication.createClientConfigBuilder("127.0.0.1", 12345);
			clientBuilder.setLayer(new ExceptionLayer(LayerExceptionMode.UNPACK));

			IClient client = Communication.createCustomClient(clientBuilder.build(), network.newClient());
			client.connect();

			sleep(3000);

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

			ServerConfigBuilder serverBuilder = Communication.createServerConfigBuilder("Dummy Server", 12345);
			serverBuilder.setOnUnexpectedRequestReceived(new SimpleAnswerToRequestListener("I received your request !"));

			IServer server = Communication.createCustomServer(serverBuilder.build(), network.getServer());
			server.open();

			IClient client = Communication.createDefaultCustomClient("127.0.0.1", 12345, network.newClient());
			client.connect();

			sleep(500);

			for (int i = 0; i < 15; i++) {
				String message = "Hello world";
				if (client.getConnection().isDisposed())
					break;

				client.getConnection().send(new Message(message.getBytes(), args ->  {
					if (!args.isTimeout())
						throw new RuntimeException("Exception to test unstable counter");
					else
						EventManager.callEvent(new LogEvent(ELogLevel.DEBUG, "Unexpected timeout occured"));
				}));
				sleep(100);
			}

			sleep(1000);

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
			IServer server = Communication.createDefaultCustomServer("TCP server test", 12345, network.getServer());
			server.open();

			SimpleSendMessageToClientOnceConnected sendToClient = new SimpleSendMessageToClientOnceConnected(server, "I'm spamming you", 10);
			sendToClient.start();

			ClientConfigBuilder clientBuilder = Communication.createClientConfigBuilder("127.0.0.1", 12345);
			clientBuilder.setOnUnexpectedRequestReceived(new SimpleClientListener(true));

			IClient client = Communication.createCustomClient(clientBuilder.build(), network.newClient());
			client.connect();

			sleep(3000);

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
			IServer server = Communication.createDefaultCustomServer("TCP server test", 12345, network.getServer());
			server.open();

			SimpleSendMessageToClientOnceConnected sendToClient = new SimpleSendMessageToClientOnceConnected(server, "I'm spamming you !", 10);
			sendToClient.start();

			ClientConfigBuilder clientBuilder = Communication.createClientConfigBuilder("127.0.0.1", 12345);
			clientBuilder.setOnUnexpectedRequestReceived(new SimpleClientListener(true));

			IClient client = Communication.createCustomClient(clientBuilder.build(), network.newClient());
			client.connect();

			sleep(12000);

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

			ClientConfigBuilder clientBuilder = Communication.createClientConfigBuilder("127.0.0.1", 12345);
			clientBuilder.setConnectionTimeout(500);
			clientBuilder.setReconnectionDelay(500);

			IClient client1 = Communication.createCustomClient(clientBuilder.build(), network.newClient());
			client1.connect();

			sleep(5000);

			ServerConfigBuilder builder = Communication.createServerConfigBuilder("Dummy Server", 12345);
			builder.setOnUnexpectedRequestReceived(new SimpleServerListener());

			IServer server = Communication.createCustomServer(builder.build(), network.getServer());
			server.open();

			sleep(2000);

			IClient client2 = Communication.createCustomClient(clientBuilder.build(), network.newClient());
			client2.connect();

			sleep(2000);

			client1.getConnection().send(new Message("Hello world from client1!".getBytes()));
			client2.getConnection().send(new Message("Hello world fron client2!".getBytes()));

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
			INetworkSimulator simulator = (local, remote, data) -> {
				Random random = new Random();
				for (int i = 0; i < 5; i++) {
					int index = random.nextInt(16, data.length - 4);
					int value = random.nextInt(-127, 126);
					
					data[index] = (byte) value;
				}
				return data;
			};

			Network network = new Network(simulator);

			ServerConfigBuilder builder = Communication.createServerConfigBuilder("Dummy builder", 12345);
			builder.setOnUnexpectedRequestReceived(new SimpleServerListener());

			IServer server = Communication.createCustomServer(builder.build(), network.getServer());
			server.open();

			IClient client = Communication.createDefaultCustomClient("127.0.0.1", 12345, network.newClient());
			client.connect();

			sleep(1000);

			for (int i = 0; i < 10; i++) {
				client.getConnection().send(new Message("Hello World !".getBytes()));
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
			for (StackTraceElement trace : e.getStackTrace())
				EventManager.callEvent(new LogEvent(ELogLevel.ERROR, trace.toString()));
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
