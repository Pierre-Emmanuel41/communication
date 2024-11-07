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

    	runTest("Network tests", () -> runNetworkTest());
    	runTest("Layer tests", () -> runLayerTests());
    	runTest("Layer initialisation tests", () -> runLayerInitialisationTest());
    	runTest("TCP tests", () -> runTcpCommunicationTest());
        
        // Asynchronous tests, wait a little bit before closing tests session
        try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
    
    private static void runNetworkTest() {
    	NetworkTest tests = new NetworkTest();
    	
    	tests.testServerInitialisation();
    	tests.testClientAutomaticReconnection();
    	tests.testClientAutomaticReconnectionButWithServerOpenedLater();
    	tests.testClientAutomaticReconnectionButServerClosedLater();
    	tests.testClientToServerCommunication();
    	tests.testServerToClientCommunication();
    	tests.testClientToServerWithCallback();
    	tests.testClientToServerWithCallbackButTimeout();
    	tests.testServerToClientWithCallback();
    	tests.testServerToClientWithCallbackButTimeout();
    	tests.testInitialisationFailure();
    	tests.testSendingException();
    	tests.testReceivingException();
    	tests.testExtractionException();
    	tests.testCallbackException();
    	tests.testUnexpectedRequestException();
    	tests.testUnstableClient();
    	tests.testTwoClientsOneServer();
    	tests.testNetworkIssues();
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
    	tests.testRsaLayerOneMessage();
    	tests.testRsaLayerTwoMessages();
    	tests.testRsaLayerLastMessageTruncated();
    	tests.testRsaLayerOneCorruptedMessage();
    	tests.testRsaLayerOneBigMessage();
    	tests.testAesLayerOneMessage();
    	tests.testAesLayerTwoMessages();
    	tests.testAesLayerLastMessageTruncated();
    	tests.testAesLayerOneCorruptedMessage();
    }

    private static void runLayerInitialisationTest() {
    	LayerInitialisationTest tests = new LayerInitialisationTest();

    	tests.testRsaLayerInitialization();
    	tests.testRsaLayerInitializationFailureClientToServer();
    	tests.testRsaLayerInitializationFailureServerAcknowledgement();
    	tests.testRsaLayerInitializationAndTransmission();
    	tests.testRsaLayerInitializationFirstFailureAndTransmission();
    	tests.testAesLayerInitialization();
    	tests.testAesLayerInitializationFailureClientToServer();
    	tests.testAesLayerInitializationFailureServerAcknowledgement();
    	tests.testAesLayerInitializationFailureIVExchange();
    	tests.testAesLayerInitializationAndTransmission();
    	tests.testAesLayerInitializationFirstFailureAndTransmission();
    	tests.testAesLayerInitializationFirstIVFailureAndTransmission();
    	tests.testAesSafeLayerInitializer();
    	tests.testAesSafeLayerInitializerAndTransmission();
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
