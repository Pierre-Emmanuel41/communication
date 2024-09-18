package fr.pederobien.communication.testing;

import fr.pederobien.communication.impl.Communication;
import fr.pederobien.communication.impl.client.ClientConfigBuilder;
import fr.pederobien.communication.interfaces.IClient;
import fr.pederobien.communication.interfaces.IServer;
import fr.pederobien.communication.testing.tools.Network;
import fr.pederobien.utils.IExecutable;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.LogEvent;
import fr.pederobien.utils.event.LogEvent.ELogLevel;

public class NetworkTest {
	
	public void testServerInitialisation() {
		IExecutable test = () -> {
			Network network = new Network();
			
			IServer server = Communication.createCustomServer(Communication.createDefaultServerConfig("Dummy Server", 12345), network.getServer());
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
			
			IServer server = Communication.createCustomServer(Communication.createDefaultServerConfig("Dummy Server", 12345), network.getServer());
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
			
			IServer server = Communication.createCustomServer(Communication.createDefaultServerConfig("Dummy Server", 12345), network.getServer());
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
