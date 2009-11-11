package twetailer.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.MockAppEngineEnvironment;
import twetailer.dto.Consumer;
import twetailer.dto.RawCommand;
import twetailer.dto.SaleAssociate;
import twitter4j.DirectMessage;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TestBaseConnector {

    private MockAppEngineEnvironment mockAppEngineEnvironment;

    @Before
    public void setUp() throws Exception {
        mockAppEngineEnvironment = new MockAppEngineEnvironment();
        mockAppEngineEnvironment.setUp();
    }

    @After
    public void tearDown() throws Exception {
        mockAppEngineEnvironment.tearDown();
    }

    @Test
    public void testConstructor() {
        new BaseConnector();
    }

    @Test(expected=ClientException.class)
    public void testUnsupportedSource() throws ClientException {
        BaseConnector.communicateToUser(null, null, null, null);
    }

    @Test
    public void testSimulatedSource() throws ClientException {
        BaseConnector.resetLastCommunicationInSimulatedMode();
        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());

        final String message = "test";
        BaseConnector.communicateToUser(Source.simulated, null, null, message);

        assertEquals(BaseConnector.getLastCommunicationInSimulatedMode(), message);
    }

    @Test
    public void testFromRawCommand() throws ClientException {
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        final String message = "test";
        BaseConnector.communicateToEmitter(rawCommand, message);

        assertEquals(BaseConnector.getLastCommunicationInSimulatedMode(), message);
    }

    @Test
    @SuppressWarnings("serial")
    public void testTwitterSourceI() throws ClientException {
        final String twitterId = "tId";
        final String message = "test";
        final Twitter mockTwitterAccount = (new Twitter() {
            @Override
            public DirectMessage sendDirectMessage(String id, String text) throws TwitterException {
                assertEquals(twitterId, id);
                assertEquals(message, text);
                return null;
            }
        });
        MockTwitterConnector.injectMockTwitterAccount(mockTwitterAccount);

        BaseConnector.communicateToUser(Source.twitter, twitterId, null, message);

        MockTwitterConnector.restoreTwitterConnector(mockTwitterAccount, null);
    }

    @Test(expected=ClientException.class)
    @SuppressWarnings("serial")
    public void testTwitterSourceII() throws ClientException {
        final String twitterId = "tId";
        final String message = "test";
        final Twitter mockTwitterAccount = (new Twitter() {
            @Override
            public DirectMessage sendDirectMessage(String id, String text) throws TwitterException {
                throw new TwitterException("Done in purpose");
            }
        });
        MockTwitterConnector.injectMockTwitterAccount(mockTwitterAccount);

        BaseConnector.communicateToUser(Source.twitter, twitterId, null, message);

        MockTwitterConnector.restoreTwitterConnector(mockTwitterAccount, null);
    }

    @Test
    public void testJabberSource() throws ClientException {
        final String jabberId = "jId";
        final String message = "test";
        BaseConnector.communicateToUser(Source.jabber, jabberId, null, message);
    }

    @Test
    public void testMailSourceI() throws ClientException {
        final String mailAddress = "unit@test.net";
        final String message = "test";
        BaseConnector.communicateToUser(Source.mail, mailAddress, null, message);
    }

    @Test(expected=ClientException.class)
    public void testMailSourceII() throws ClientException {
        final String mailAddress = "@@@";
        final String message = "test";
        BaseConnector.communicateToUser(Source.mail, mailAddress, null, message);
    }

    @Test(expected=RuntimeException.class)
    public void testFacebookSource() throws ClientException {
        final String facebookId = "fId";
        final String message = "test";
        BaseConnector.communicateToUser(Source.facebook, facebookId, null, message);
    }

    @Test
    public void testCommunicateToConsumerI() throws ClientException {
        BaseConnector.communicateToConsumer(Source.simulated, new Consumer(), null);
    }

    @Test
    public void testCommunicateToConsumerII() throws ClientException {
        BaseConnector.communicateToConsumer(Source.twitter, new Consumer(), null);
    }

    @Test
    public void testCommunicateToConsumerIII() throws ClientException {
        BaseConnector.communicateToConsumer(Source.jabber, new Consumer(), null);
    }

    @Test
    public void testCommunicateToConsumerIV() throws ClientException {
        BaseConnector.communicateToConsumer(Source.mail, new Consumer(), null);
    }

    @Test
    public void testCommunicateToConsumerV() throws ClientException {
        BaseConnector.communicateToConsumer(Source.mail, new Consumer() { @Override public String getEmail() { return "unit@test.net"; } }, null);
    }

    @Test
    public void testCommunicateToSaleAssociateI() throws ClientException {
        BaseConnector.communicateToSaleAssociate(Source.simulated, new SaleAssociate(), null);
    }

    @Test
    public void testCommunicateToSaleAssociateII() throws ClientException {
        BaseConnector.communicateToSaleAssociate(Source.twitter, new SaleAssociate(), null);
    }

    @Test
    public void testCommunicateToSaleAssociateIII() throws ClientException {
        BaseConnector.communicateToSaleAssociate(Source.jabber, new SaleAssociate(), null);
    }

    @Test
    public void testCommunicateToSaleAssociateIV() throws ClientException {
        BaseConnector.communicateToSaleAssociate(Source.mail, new SaleAssociate(), null);
    }

    @Test
    public void testCommunicateToSaleAssociateV() throws ClientException {
        BaseConnector.communicateToSaleAssociate(Source.mail, new SaleAssociate() { @Override public String getEmail() { return "unit@test.net"; } }, null);
    }

    @Test
    public void testCommunicateManyMessages() throws ClientException {
        BaseConnector.resetLastCommunicationInSimulatedMode();
        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertNull(BaseConnector.getCommunicationForRetroIndexInSimulatedMode(0));
        assertNull(BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1));
        assertNull(BaseConnector.getCommunicationForRetroIndexInSimulatedMode(2));
        assertNull(BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1000));

        String first = "first";
        BaseConnector.communicateToSaleAssociate(Source.simulated, new SaleAssociate(), first);
        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertEquals(first, BaseConnector.getLastCommunicationInSimulatedMode());
        assertEquals(first, BaseConnector.getCommunicationForRetroIndexInSimulatedMode(0));
        assertNull(BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1));

        String second = "second";
        BaseConnector.communicateToSaleAssociate(Source.simulated, new SaleAssociate(), second);
        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertEquals(second, BaseConnector.getLastCommunicationInSimulatedMode());
        assertEquals(second, BaseConnector.getCommunicationForRetroIndexInSimulatedMode(0));
        assertEquals(first, BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1));
        assertNull(BaseConnector.getCommunicationForRetroIndexInSimulatedMode(2));
    }

    @Test
    public void testCheckMessageLengthIa() {
        List<String> output = BaseConnector.checkMessageLength(null, 1000);
        assertNotNull(output);
        assertEquals(0, output.size());
    }

    @Test
    public void testCheckMessageLengthIb() {
        List<String> output = BaseConnector.checkMessageLength("", 1000);
        assertNotNull(output);
        assertEquals(0, output.size());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCheckMessageLengthIc() {
        BaseConnector.checkMessageLength("", 0);
    }

    @Test
    public void testCheckMessageLengthII() {
        String message = "blah blah blah";
        List<String> output = BaseConnector.checkMessageLength(message, 1000);
        assertNotNull(output);
        assertEquals(1, output.size());
        assertEquals(message, output.get(0));
    }

    @Test
    public void testCheckMessageLengthIIIa() {
        String part1 = "blah blah blah";
        String part2 = "and more";
        String message = part1 + BaseConnector.SUGGESTED_MESSAGE_SEPARATOR + part2;
        List<String> output = BaseConnector.checkMessageLength(message, 1000);
        assertNotNull(output);
        assertEquals(2, output.size());
        assertEquals(part1, output.get(0));
        assertEquals(part2, output.get(1));
    }

    @Test
    public void testCheckMessageLengthIIIb() {
        String part1 = "blah blah blah";
        String part2 = "and more";
        String message = part1 + BaseConnector.SUGGESTED_MESSAGE_SEPARATOR + BaseConnector.SUGGESTED_MESSAGE_SEPARATOR + BaseConnector.SUGGESTED_MESSAGE_SEPARATOR + part2;
        List<String> output = BaseConnector.checkMessageLength(message, 1000);
        assertNotNull(output);
        assertEquals(4, output.size());
        assertEquals(part1, output.get(0));
        assertEquals("", output.get(1));
        assertEquals("", output.get(2));
        assertEquals(part2, output.get(3));
    }

    @Test
    public void testCheckMessageLengthIIIc() {
        String part1 = "blah blah blah";
        String part2 = "and more";
        String message = part1 + BaseConnector.SUGGESTED_MESSAGE_SEPARATOR + part2 + BaseConnector.SUGGESTED_MESSAGE_SEPARATOR;
        List<String> output = BaseConnector.checkMessageLength(message, 1000);
        assertNotNull(output);
        assertEquals(2, output.size());
        assertEquals(part1, output.get(0));
        assertEquals(part2, output.get(1));
    }

    @Test
    public void testCheckMessageLengthIVa() {
        // Test separator after the word
        String part1 = "blah blah blah";
        String part2 = "and more";
        String message = part1 + " " + part2; // Space
        List<String> output = BaseConnector.checkMessageLength(message, part1.length());
        assertNotNull(output);
        assertEquals(2, output.size());
        assertEquals(part1, output.get(0));
        assertEquals(part2, output.get(1));
    }

    @Test
    public void testCheckMessageLengthIVb() {
        // Test separator after the word
        String part1 = "blah blah blah";
        String part2 = "and more";
        String message = part1 + "\t" + part2; // Space
        List<String> output = BaseConnector.checkMessageLength(message, part1.length());
        assertNotNull(output);
        assertEquals(2, output.size());
        assertEquals(part1, output.get(0));
        assertEquals(part2, output.get(1));
    }

    @Test
    public void testCheckMessageLengthVa() {
        // Test separator before the word
        String part1 = "blah blah blah";
        String part2 = "and more";
        String message = part1 + " " + part2; // Space
        List<String> output = BaseConnector.checkMessageLength(message, part1.length() + 3);
        assertNotNull(output);
        assertEquals(2, output.size());
        assertEquals(part1, output.get(0));
        assertEquals(part2, output.get(1));
    }

    @Test
    public void testCheckMessageLengthVb() {
        // Test separator before the word
        String part1 = "blah blah blah";
        String part2 = "and more";
        String message = part1 + "\t" + part2; // Space
        List<String> output = BaseConnector.checkMessageLength(message, part1.length() + 3);
        assertNotNull(output);
        assertEquals(2, output.size());
        assertEquals(part1, output.get(0));
        assertEquals(part2, output.get(1));
    }

    @Test
    public void testCheckMessageLengthVI() {
        // Verify the trim
        String part1 = "blah blah blah";
        String part2 = "and more";
        String message = part1 + " \t \t " + part2; // Space
        List<String> output = BaseConnector.checkMessageLength(message, part1.length() + 3);
        assertNotNull(output);
        assertEquals(2, output.size());
        assertEquals(part1, output.get(0));
        assertEquals(part2, output.get(1));
    }
}
