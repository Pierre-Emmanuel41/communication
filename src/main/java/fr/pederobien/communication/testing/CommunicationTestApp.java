package fr.pederobien.communication.testing;

import fr.pederobien.utils.event.EventLogger;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.LogEvent;

/**
 * Hello world!
 *
 */
public class CommunicationTestApp 
{	
    public static void main( String[] args )
    {
    	EventLogger.instance().newLine(true).timeStamp(true).register();

    	EventManager.callEvent(new LogEvent("Start of TCP tests execution"));
        runTcpCommunicationTest();
        EventManager.callEvent(new LogEvent("End of TCP tests execution"));
        
        // Asynchronous tests, wait a little bit before closing tests session
        try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
    
    private static void runTcpCommunicationTest() {
    	TcpCommunicationTest tests = new TcpCommunicationTest();
    	
    	tests.testClientAutomaticReconnection();
        tests.testClientAutomaticReconnectionButWithServerOpenedLater();
        tests.testClientAutomaticReconnectionButServerClosedLater();
        tests.testClientToServerCommunication();
        tests.testServerToClientCommunication();
        tests.testClientToServerWithCallback();
        tests.testClientToServerWithCallbackButTimeout();
        tests.testServerToClientWithCallback();
        tests.testServerToClientWithCallbackButTimeout();
        tests.testSendingException();
        tests.testReceivingException();
        tests.testExtractionException();
        tests.testCallbackException();
        tests.testUnexpectedRequestException();
        tests.testUnstableClient();
    }
}
