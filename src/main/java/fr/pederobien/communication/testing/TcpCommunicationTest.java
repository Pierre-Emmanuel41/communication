package fr.pederobien.communication.testing;

import fr.pederobien.communication.impl.Communication;
import fr.pederobien.communication.impl.client.ClientConfigBuilder;
import fr.pederobien.communication.impl.connection.Message;
import fr.pederobien.communication.impl.server.ServerConfigBuilder;
import fr.pederobien.communication.interfaces.IClient;
import fr.pederobien.communication.interfaces.IServer;
import fr.pederobien.communication.testing.ClientExceptionImpl.ClientExceptionMode;
import fr.pederobien.communication.testing.ExceptionLayer.LayerExceptionMode;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.LogEvent;
import fr.pederobien.utils.event.LogEvent.ELogLevel;

public class TcpCommunicationTest {

	public void testClientAutomaticReconnection() {
		Runnable test = () -> {
			IClient client = Communication.createDefaultTcpClient("127.0.0.1", 12345);
			client.connect();
			
			sleep(5000);
			
			client.disconnect();
			client.dispose();
		};
		
		runTest("testClientAutomaticReconnection", test);
	}
	
	public void testClientAutomaticReconnectionButWithServerOpenedLater() {
		Runnable test = () -> {
			IClient client = Communication.createDefaultTcpClient("127.0.0.1", 12345);
			client.connect();
			
			sleep(3000);
			
			IServer server = Communication.createDefaultTcpServer("TCP server test", 12345);
			server.open();
			
			sleep(1000);
			
			client.disconnect();
			client.dispose();
			
			sleep(500);

			server.close();
			server.dispose();
		};
		
		runTest("testClientAutomaticReconnectionButWithServerOpenedLater", test);
	}
	
	public void testClientAutomaticReconnectionButServerClosedLater() {
		Runnable test = () -> {
			IServer server = Communication.createDefaultTcpServer("TCP server test", 12345);
			server.open();
			
			sleep(500);
			
			IClient client = Communication.createDefaultTcpClient("127.0.0.1", 12345);
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
		Runnable test = () -> {
			ServerConfigBuilder serverBuilder = Communication.createServerConfigBuilder("TCP server test", 12345);
			serverBuilder.setRequestReceivedHandler(() -> new SimpleServerListener());

			IServer server = Communication.createTcpServer(serverBuilder.build());
			server.open();
			
			IClient client = Communication.createDefaultTcpClient("127.0.0.1", 12345);
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
		Runnable test = () -> {
			IServer server = Communication.createDefaultTcpServer("TCP server test", 12345);
			server.open();
			
			SimpleSendMessageToClientOnceConnected sendToClient = new SimpleSendMessageToClientOnceConnected(server, "You are connected !", 1);
			sendToClient.start();
			
			ClientConfigBuilder clientBuilder = Communication.createClientConfigBuilder("127.0.0.1", 12345);
			clientBuilder.setAllowUnexpectedRequest(true);
			clientBuilder.setRequestReceivedHandler(() -> new SimpleClientListener(false));

			IClient client = Communication.createTcpClient(clientBuilder.build());
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
		Runnable test = () -> {
			ServerConfigBuilder serverBuilder = Communication.createServerConfigBuilder("TCP server test", 12345);
			serverBuilder.setRequestReceivedHandler(() -> new SimpleAnswerToRequestListener("I received your request !"));

			IServer server = Communication.createTcpServer(serverBuilder.build());
			server.open();
			
			IClient client = Communication.createDefaultTcpClient("127.0.0.1", 12345);
			client.connect();
			
			sleep(1000);
			
			client.getConnection().send(new Message("Hello world !".getBytes(), args -> {
				if (!args.isTimeout()) {
					String received = new String(args.getResponse().getBytes());
					EventManager.callEvent(new LogEvent(ELogLevel.WARNING, "Response received: %s", received));
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
		Runnable test = () -> {
			IServer server = Communication.createDefaultTcpServer("TCP server test", 12345);
			server.open();
			
			IClient client = Communication.createDefaultTcpClient("127.0.0.1", 12345);
			client.connect();
			
			sleep(1000);
			
			client.getConnection().send(new Message("Hello world !".getBytes(), args -> {
				if (!args.isTimeout()) {
					String received = new String(args.getResponse().getBytes());
					EventManager.callEvent(new LogEvent(ELogLevel.WARNING, "Response received: %s", received));
				} else
					EventManager.callEvent(new LogEvent(ELogLevel.DEBUG, "Expected timeout occured"));
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
		Runnable test = () -> {
			IServer server = Communication.createDefaultTcpServer("TCP server test", 12345);
			server.open();

			CallbackSendMessageToClientOnceConnected sendToClient = new CallbackSendMessageToClientOnceConnected(server, args -> {
				if (!args.isTimeout()) {
					String received = new String(args.getResponse().getBytes());
					EventManager.callEvent(new LogEvent(ELogLevel.DEBUG, "Server received: %s", received));
				}
				else
					EventManager.callEvent(new LogEvent(ELogLevel.DEBUG, "Unexpected timeout occurred"));
			});
			
			sendToClient.start();
			
			ClientConfigBuilder clientBuilder = Communication.createClientConfigBuilder("127.0.0.1", 12345);
			clientBuilder.setAllowUnexpectedRequest(true);
			clientBuilder.setRequestReceivedHandler(() -> new SimpleAnswerToRequestListener("I guess I am !"));
			
			IClient client = Communication.createTcpClient(clientBuilder.build());
			client.connect();
			
			sleep(1000);
			
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
		Runnable test = () -> {
			IServer server = Communication.createDefaultTcpServer("TCP server test", 12345);
			server.open();

			CallbackSendMessageToClientOnceConnected sendToClient = new CallbackSendMessageToClientOnceConnected(server, args -> {
				if (!args.isTimeout()) {
					String received = new String(args.getResponse().getBytes());
					EventManager.callEvent(new LogEvent(ELogLevel.DEBUG, "Server received: %s", received));
				}
				else
					EventManager.callEvent(new LogEvent(ELogLevel.DEBUG, "Expected timeout occurred"));
			});
			
			sendToClient.start();
			
			IClient client = Communication.createTcpClient(Communication.createDefaultClientConfig("127.0.0.1", 12345));
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
	
	public void testSendingException() {
		Runnable test = () -> {
			IServer server = Communication.createDefaultTcpServer("TCP server test", 12345);
			server.open();
			
			ClientConfigBuilder clientBuilder = Communication.createClientConfigBuilder("127.0.0.1", 12345);
			IClient client = Communication.createCustomClient(clientBuilder.build(), new ClientExceptionImpl(ClientExceptionMode.SENDING));
			client.connect();
			
			sleep(1000);
			
			for (int i = 0; i < 15; i++) {
				client.getConnection().send(new Message(new byte[0]));
			}
			
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
		Runnable test = () -> {
			IServer server = Communication.createDefaultTcpServer("TCP server test", 12345);
			server.open();
			
			ClientConfigBuilder clientBuilder = Communication.createClientConfigBuilder("127.0.0.1", 12345);
			IClient client = Communication.createCustomClient(clientBuilder.build(), new ClientExceptionImpl(ClientExceptionMode.RECEIVING));
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
		Runnable test = () -> {
			IServer server = Communication.createDefaultTcpServer("TCP server test", 12345);
			server.open();
			
			SimpleSendMessageToClientOnceConnected sendToClient = new SimpleSendMessageToClientOnceConnected(server, "I'm spamming you !", 10);
			sendToClient.start();
			
			ClientConfigBuilder clientBuilder = Communication.createClientConfigBuilder("127.0.0.1", 12345);
			clientBuilder.setAllowUnexpectedRequest(true);
			clientBuilder.setLayer(new ExceptionLayer(LayerExceptionMode.UNPACK));

			IClient client = Communication.createTcpClient(clientBuilder.build());
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
		Runnable test = () -> {
			ServerConfigBuilder serverBuilder = Communication.createServerConfigBuilder("TCP server test", 12345);
			serverBuilder.setRequestReceivedHandler(() -> new SimpleAnswerToRequestListener("I received your request !"));
			
			IServer server = Communication.createTcpServer(serverBuilder.build());
			server.open();
			
			IClient client = Communication.createDefaultTcpClient("127.0.0.1", 12345);
			client.connect();

			sleep(500);

			for (int i = 0; i < 15; i++) {
				String message = "Hello world";
				client.getConnection().send(new Message(message.getBytes(), args ->  {
					if (!args.isTimeout())
						throw new RuntimeException("Exception to test unstable counter");
					else
						EventManager.callEvent(new LogEvent(ELogLevel.DEBUG, "Unexpected timeout occured"));
				}));
				sleep(100);
			}
			
			sleep(500);
			
			client.disconnect();
			client.dispose();
			
			sleep(500);
			
			server.close();
			server.dispose();
		};
		
		runTest("testCallbackException", test);
	}
	
	public void testUnexpectedRequestException() {
		Runnable test = () -> {
			IServer server = Communication.createDefaultTcpServer("TCP server test", 12345);
			server.open();
			
			SimpleSendMessageToClientOnceConnected sendToClient = new SimpleSendMessageToClientOnceConnected(server, "I'm spamming you", 10);
			sendToClient.start();
			
			ClientConfigBuilder clientBuilder = Communication.createClientConfigBuilder("127.0.0.1", 12345);
			clientBuilder.setAllowUnexpectedRequest(true);
			clientBuilder.setRequestReceivedHandler(() -> new SimpleClientListener(true));
			
			IClient client = Communication.createTcpClient(clientBuilder.build());
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
		Runnable test = () -> {
			IServer server = Communication.createDefaultTcpServer("TCP server test", 12345);
			server.open();
			
			SimpleSendMessageToClientOnceConnected sendToClient = new SimpleSendMessageToClientOnceConnected(server, "I'm spamming you !", 10);
			sendToClient.start();
			
			ClientConfigBuilder clientBuilder = Communication.createClientConfigBuilder("127.0.0.1", 12345);
			clientBuilder.setAllowUnexpectedRequest(true);
			clientBuilder.setRequestReceivedHandler(() -> new SimpleClientListener(true));
			
			IClient client = Communication.createTcpClient(clientBuilder.build());
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
	
	private void runTest(String testName, Runnable runnable) {
		EventManager.callEvent(new LogEvent(ELogLevel.DEBUG, "Begin %s", testName));
		try {
			runnable.run();
		} catch (Exception e) {
			e.printStackTrace();
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
