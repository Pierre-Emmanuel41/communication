package fr.pederobien.communication.testing;

import fr.pederobien.utils.event.EventLogger;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.LogEvent;
import fr.pederobien.utils.event.LogEvent.ELogLevel;

/**
 * Hello world!
 *
 */
public class CommunicationTestApp 
{	
    public static void main( String[] args )
    {
    	EventLogger.instance().newLine(true).timeStamp(true).register();

    	runTest("Layer tests", () -> runLayerTests());
    	runTest("TCP tests", () -> runTcpCommunicationTest());
        
        // Asynchronous tests, wait a little bit before closing tests session
        try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
    
    private static void runLayerTests() {
    	LayerTest tests = new LayerTest();

    	tests.testEncapsulerOneMessage();
    	tests.testEncapsulerTwoMessages();
    	tests.testEncapsulerLastMessageTruncated();
    	tests.testSplitterOneMessage();
    	tests.testSplitterTwoMessages();
    	tests.testSplitterLastMessageTruncated();
    	tests.testSimpleLayerOneMessage();
    	tests.testSimpleLayerTwoMessages();
    	tests.testSimpleLayerLastMessageTruncated();
    	tests.testCertifiedLayerOneMessage();
    	tests.testCertifiedLayerTwoMessages();
    	tests.testCertifiedLayerLastMessageTruncated();
    	tests.testCertifiedLayerOneCorruptedMessage();
    	tests.testRsaLayerInitialization();
    	tests.testRsaLayerInitializationFailureClientToServer();
    	tests.testRsaLayerInitializationFailureServerAcknowledgement();
    	tests.testRsaLayerInitializationAndTransmission();
    	tests.testRsaLayerInitializationFirstFailureAndTransmission();
    	tests.testRsaLayerInitializationSecondFailureAndTransmission();
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
    
    private static void runTest(String testName, Runnable runnable) {
		EventManager.callEvent(new LogEvent(ELogLevel.DEBUG, "Start of %s execution", testName));
		try {
			runnable.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		EventManager.callEvent(new LogEvent(ELogLevel.DEBUG, "End of %s execution", testName));
	}
}
