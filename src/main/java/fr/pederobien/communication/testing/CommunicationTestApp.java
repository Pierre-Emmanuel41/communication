package fr.pederobien.communication.testing;

import fr.pederobien.utils.IExecutable;
import fr.pederobien.utils.event.Logger;

/**
 * Hello world!
 */
public class CommunicationTestApp {
    public static void main(String[] args) {
        Logger.instance().newLine(true).timeStamp(true).colorized(true).debug(true);

        //runTest("Network tests", CommunicationTestApp::runNetworkTest);
        //runTest("Layer tests", CommunicationTestApp::runLayerTests);
        //runTest("Layer initialisation tests", CommunicationTestApp::runLayerInitialisationTest);
        runTest("TCP tests", CommunicationTestApp::runTcpCommunicationTest);
        //runTest("UDP tests", CommunicationTestApp::runUdpCommunicationTest);

        // Asynchronous tests, wait a little bit before closing tests session
        sleep(1000);
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

        tests.testEncapsulaterOneMessage();
        tests.testEncapsulaterTwoMessages();
        tests.testEncapsulaterLastMessageTruncated();
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
        tests.testAesLayerInitialization();
        tests.testAesLayerInitializationFailureClientToServer();
        tests.testAesLayerInitializationFailureServerAcknowledgement();
        tests.testAesLayerInitializationFailureIVExchange();
        tests.testAesLayerInitializationAndTransmission();
        tests.testAesSafeLayerInitializer();
        tests.testAesSafeLayerInitializerAndTransmission();
    }

    private static void runTcpCommunicationTest() {
        TcpCommunicationTest tests = new TcpCommunicationTest();
        
        tests.testRsaLayer();
        tests.testAesLayer();
        tests.testAesSafeLayer();
        tests.testTwoClientsOneServer();
    }

    private static void runUdpCommunicationTest() {
        UdpCommunicationTest tests = new UdpCommunicationTest();

        tests.testServerWithSpecificAddressAndPort();
        tests.testServerWithSpecificAddressButAnyPort();
        tests.testServerWithAnyAddressButSpecificPort();
        tests.testServerWithAnyAddressAndAnyPort();
        tests.testClientToServerCommunication();
        tests.testServerToClientCommunication();
        tests.testClientToServerWithCallback();
        tests.testClientToServerWithCallbackButTimeout();
        tests.testServerToClientWithCallback();
        tests.testServerToClientWithCallbackButTimeout();
        tests.testExtractionException();
        tests.testCallbackException();
        tests.testUnexpectedRequestException();
        tests.testUnstableClient();
        tests.testRsaLayer();
        tests.testAesLayer();
        tests.testAesSafeLayer();
        tests.testTwoClientsOneServer();
    }

    private static void runTest(String testName, IExecutable test) {
        Logger.warning("Start of %s execution", testName);
        try {
            test.exec();
        } catch (Exception e) {
            Logger.error("Unexpected error: %s", e.getMessage());
            for (StackTraceElement trace : e.getStackTrace()) {
                Logger.error(trace.toString());
            }
        }

        sleep(1000);
        Logger.warning("End of %s execution", testName);
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
