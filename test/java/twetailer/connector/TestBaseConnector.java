package twetailer.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.Locale;

import javamocks.util.logging.MockLogger;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import twetailer.CommunicationException;
import twetailer.connector.BaseConnector.Source;
import twetailer.dto.Consumer;
import twetailer.dto.RawCommand;
import twitter4j.DirectMessage;
import twitter4j.MockDirectMessage;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import com.google.appengine.api.xmpp.MockXMPPService;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class TestBaseConnector {

    private static LocalServiceTestHelper  helper;

    @BeforeClass
    public static void setUpBeforeClass() {
        BaseConnector.setLogger(new MockLogger("test", null));
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    }

    @Before
    public void setUp() throws Exception {
        helper.setUp();
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();
        JabberConnector.injectMockXMPPService(null);
        MockTwitterConnector.restoreTwitterConnector();
    }

    @Test
    public void testConstructor() {
        new BaseConnector();
    }

    @Test(expected=CommunicationException.class)
    public void testUnsupportedSource() throws CommunicationException {
        BaseConnector.communicateToUser(null, false, null, null, null, null, Locale.ENGLISH);
    }

    @Test
    public void testSimulatedSource() throws CommunicationException {
        BaseConnector.resetLastCommunicationInSimulatedMode();
        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());

        final String message = "test";
        BaseConnector.communicateToUser(Source.simulated, false, null, null, null, new String[] { message }, Locale.ENGLISH);

        assertEquals(BaseConnector.getLastCommunicationInSimulatedMode(), message);
    }

    @Test
    public void testFromRawCommand() throws CommunicationException {
        RawCommand rawCommand = new RawCommand(Source.simulated);

        final String message = "test";
        BaseConnector.communicateToEmitter(rawCommand, new String[] { message }, Locale.ENGLISH);

        assertEquals(BaseConnector.getLastCommunicationInSimulatedMode(), message);
    }

    @Test
    @SuppressWarnings({ "serial", "deprecation" })
    public void testTwitterSourceI() throws CommunicationException {
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

        BaseConnector.communicateToUser(Source.twitter, false, twitterId, null, null, new String[] { message }, Locale.ENGLISH);
    }

    @Test(expected=CommunicationException.class)
    @SuppressWarnings({ "serial", "deprecation" })
    public void testTwitterSourceII() throws CommunicationException {
        final String twitterId = "tId";
        final String message = "test";
        final Twitter mockTwitterAccount = (new Twitter() {
            @Override
            public DirectMessage sendDirectMessage(String id, String text) throws TwitterException {
                throw new TwitterException("Done in purpose");
            }
        });
        MockTwitterConnector.injectMockTwitterAccount(mockTwitterAccount);

        BaseConnector.communicateToUser(Source.twitter, false, twitterId, null, null, new String[] { message }, Locale.ENGLISH);
    }

    @Test
    public void testJabberSourceI() throws CommunicationException {
        final String jabberId = "jId";
        final String message = "test";
        BaseConnector.communicateToUser(Source.jabber, false, jabberId, null, null, new String[] { message }, Locale.ENGLISH);
    }

    @Test(expected=CommunicationException.class)
    public void testJabberSourceII() throws CommunicationException {
        final String jabberId = "jId";
        final String message = "test";
        MockXMPPService mockXMPPService = new MockXMPPService();
        mockXMPPService.setPresence(jabberId, false);
        JabberConnector.injectMockXMPPService(mockXMPPService);
        BaseConnector.communicateToUser(Source.jabber, false, jabberId, null, null, new String[] { message }, Locale.ENGLISH);
    }

    @Test
    public void testMailSourceI() throws CommunicationException {
        final String mailAddress = "unit@test.net";
        final String message = "test";
        final String subject = "subject";
        BaseConnector.communicateToUser(Source.mail, false, mailAddress, null, subject, new String[] { message }, Locale.ENGLISH);
    }

    @Test(expected=CommunicationException.class)
    public void testMailSourceII() throws CommunicationException {
        final String mailAddress = "@@@";
        final String message = "test";
        final String subject = "subject";
        BaseConnector.communicateToUser(Source.mail, false, mailAddress, null, subject, new String[] { message }, Locale.ENGLISH);
    }

    @Ignore // Because .facebook is temporarily mapped to .mail
    @Test(expected=RuntimeException.class)
    public void testFacebookSource() throws CommunicationException {
        final String facebookId = "fId";
        final String message = "test";
        final String subject = "subject";
        BaseConnector.communicateToUser(Source.facebook, false, facebookId, null, subject, new String[] { message }, Locale.ENGLISH);
    }

    @Test
    public void testCommunicateToConsumerI() throws CommunicationException {
        BaseConnector.communicateToConsumer(Source.simulated, "", new Consumer(), new String[0]);
    }

    @Test
    public void testCommunicateToConsumerII() throws CommunicationException {
        BaseConnector.communicateToConsumer(Source.twitter, "", new Consumer(), new String[0]);
    }

    @Test
    public void testCommunicateToConsumerIII() throws CommunicationException {
        BaseConnector.communicateToConsumer(Source.jabber, "", new Consumer(), new String[0]);
    }

    @Test
    public void testCommunicateToConsumerIV() throws CommunicationException {
        BaseConnector.communicateToConsumer(Source.mail, "", new Consumer(), new String[0]);
    }

    @Test
    public void testCommunicateToConsumerV() throws CommunicationException {
        BaseConnector.communicateToConsumer(Source.mail, "", new Consumer() { @Override public String getEmail() { return "unit@test.net"; } }, new String[0]);
    }

    @Test
    public void testCommunicateToConsumerVI() throws CommunicationException {
        BaseConnector.communicateToConsumer(Source.widget, "", new Consumer(), new String[0]);
    }

    @Test
    public void testCommunicateToConsumerVII() throws CommunicationException {
        BaseConnector.communicateToConsumer(Source.widget, "", new Consumer() { @Override public String getEmail() { return "unit@test.net"; } }, new String[0]);
    }

    @Test
    public void testCommunicateManyMessagesI() throws CommunicationException {
        BaseConnector.resetLastCommunicationInSimulatedMode();
        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertNull(BaseConnector.getCommunicationForRetroIndexInSimulatedMode(0));
        assertNull(BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1));
        assertNull(BaseConnector.getCommunicationForRetroIndexInSimulatedMode(2));
        assertNull(BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1000));

        String first = "first";
        BaseConnector.communicateToConsumer(Source.simulated, "", new Consumer(), new String[] { first });
        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertEquals(first, BaseConnector.getLastCommunicationInSimulatedMode());
        assertEquals(first, BaseConnector.getCommunicationForRetroIndexInSimulatedMode(0));
        assertNull(BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1));

        String second = "second";
        BaseConnector.communicateToConsumer(Source.simulated, "", new Consumer(), new String[] { second });
        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertEquals(second, BaseConnector.getLastCommunicationInSimulatedMode());
        assertEquals(second, BaseConnector.getCommunicationForRetroIndexInSimulatedMode(0));
        assertEquals(first, BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1));
        assertNull(BaseConnector.getCommunicationForRetroIndexInSimulatedMode(2));
    }

    @Test
    public void testCommunicateManyMessagesII() throws CommunicationException {
        BaseConnector.resetLastCommunicationInSimulatedMode();
        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertNull(BaseConnector.getCommunicationForRetroIndexInSimulatedMode(0));
        assertNull(BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1));
        assertNull(BaseConnector.getCommunicationForRetroIndexInSimulatedMode(2));
        assertNull(BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1000));

        String first = "first";
        String second = "second";
        BaseConnector.communicateToConsumer(Source.simulated, "", new Consumer(), new String[] { first, second });
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

    @Test
    public void testMailMultipleMessages() throws CommunicationException {
        final String mailAddress = "unit@test.net";
        final String subject = "subject";
        final String message1 = "test1";
        final String message2 = "test2";
        BaseConnector.communicateToUser(Source.mail, false, mailAddress, null, subject, new String[] { message1, message2 }, Locale.ENGLISH);
    }

    @Test
    public void testCheckMessageLengthVII() {
        String message = ":-) Proposal:106004 for tags:nikon d500 has been confirmed.| Please mark, hold for Consumer with Demand reference:106002 and then !close proposal:106004, or, !flag proposal:106004 note:your-note-here.";
        List<String> output = BaseConnector.checkMessageLength(message, 140);
        assertEquals(2, output.size());
    }

    @Test
    public void testCheckMessageLengthVIII() {
        // Verify the trim
        String part1 = "blah blah blah";
        String message = part1;
        List<String> output = BaseConnector.checkMessageLength(message, part1.length() - 2);
        assertNotNull(output);
        assertEquals(2, output.size());
        assertEquals(part1.substring(0, 9), output.get(0));
        assertEquals(part1.substring(10), output.get(1));
    }

    @Test
    public void testCheckMessageLengthIX() {
        // Verify the trim
        String part1 = "blah blah\tblah";
        String message = part1;
        List<String> output = BaseConnector.checkMessageLength(message, part1.length() - 2);
        assertNotNull(output);
        assertEquals(2, output.size());
        assertEquals(part1.substring(0, 9), output.get(0));
        assertEquals(part1.substring(10), output.get(1));
    }

    @Test(expected=CommunicationException.class)
    public void testGetCommunicationchannelI() throws CommunicationException {
        final String coordinate = null;
        BaseConnector.getCCedCommunicationChannel(coordinate);
    }

    @Test(expected=CommunicationException.class)
    public void testGetCommunicationchannelII() throws CommunicationException {
        final String coordinate = "";
        BaseConnector.getCCedCommunicationChannel(coordinate);
    }

    @Test
    public void testGetCommunicationchannelIII() throws CommunicationException {
        final String coordinate = "jack@crusher";
        assertEquals(Source.mail, BaseConnector.getCCedCommunicationChannel(coordinate));
    }

    @Test
    public void testGetCommunicationchannelIV() throws CommunicationException {
        final String coordinate = "@jack_crusher";
        assertEquals(Source.twitter, BaseConnector.getCCedCommunicationChannel(coordinate)); // With the leading @
        assertEquals(Source.twitter, BaseConnector.getCCedCommunicationChannel(coordinate.substring(1))); // Without the leading @
    }

    @Test(expected=CommunicationException.class)
    public void testCommunicateToCCedI() throws CommunicationException {
        final String mailAddress = "jack@@@crusher";
        final String message = "test";
        final String subject = "subject";
        BaseConnector.communicateToCCed(null, mailAddress, subject, message, Locale.ENGLISH); // Source == null to let the method calling getCCedCommunicationChannel()
    }

    @Test(expected=CommunicationException.class)
    @SuppressWarnings({ "deprecation", "serial" })
    public void testCommunicateToCCedII() throws CommunicationException {
        final String twitterId = "jack_crusher";
        final String message = "test";
        final String subject = "subject";
        final Twitter mockTwitterAccount = (new Twitter() {
            @Override
            public DirectMessage sendDirectMessage(String id, String text) throws TwitterException {
                throw new TwitterException("Done in purpose");
            }
        });
        MockTwitterConnector.injectMockTwitterAccount(mockTwitterAccount);
        BaseConnector.communicateToCCed(Source.twitter, twitterId, subject, message, Locale.ENGLISH);
    }

    @Test(expected=CommunicationException.class)
    @SuppressWarnings({ "deprecation", "serial" })
    public void testCommunicateToCCedIII() throws CommunicationException {
        final String twitterId = "@jack_crusher";
        final String message = "test";
        final String subject = "subject";
        final Twitter mockTwitterAccount = (new Twitter() {
            @Override
            public DirectMessage sendDirectMessage(String id, String text) throws TwitterException {
                throw new TwitterException("Done in purpose");
            }
        });
        MockTwitterConnector.injectMockTwitterAccount(mockTwitterAccount);
        BaseConnector.communicateToCCed(Source.mail, twitterId, subject, message, Locale.ENGLISH);
    }

    @Test
    @SuppressWarnings({ "deprecation", "serial" })
    public void testCommunicateToCCedIV() throws CommunicationException {
        final String twitterId = "@jack_crusher";
        final String message = "test";
        final String subject = "subject";
        final Twitter mockTwitterAccount = (new Twitter() {
            @Override
            public DirectMessage sendDirectMessage(String id, String text) throws TwitterException {
                return new MockDirectMessage("ds", "ds", "ds");
            }
        });
        MockTwitterConnector.injectMockTwitterAccount(mockTwitterAccount);
        BaseConnector.communicateToCCed(Source.twitter, twitterId, subject, message, Locale.ENGLISH);
    }

    @Test
    public void testCheckMessageLengthX() {
        String message = "blah :-\\| blah|blah \r \n \t  blah  blah ";
        List<String> output = BaseConnector.checkMessageLength(message, 1000);
        assertNotNull(output);
        assertEquals(2, output.size());
        assertEquals("blah :-| blah", output.get(0));
        assertEquals("blah blah blah", output.get(1)); // trimmed
    }

    @Test
    public void testEncodeCommandI() {
        String in = "blah\\|blah";
        assertEquals("blah%7Cblah", BaseConnector.urlEncodeValue(in, true));
        assertEquals("blah%7Cblah", BaseConnector.urlEncodeValue(in, false));
    }

    @Test
    public void testEncodeCommandII() {
        String in = "blah|blah";
        assertEquals("blah%7Cblah", BaseConnector.urlEncodeValue(in, true));
        assertEquals("blah%0Ablah", BaseConnector.urlEncodeValue(in, false));
    }

    @Test
    public void testEncodeCommandIII() {
        String in = "blah \\| blah|blah \r \n \t  blah";
        assertEquals("blah%20%7C%20blah%7Cblah%20blah", BaseConnector.urlEncodeValue(in, true));
        assertEquals("blah%20%7C%20blah%0Ablah%20blah", BaseConnector.urlEncodeValue(in, false));
    }
}
