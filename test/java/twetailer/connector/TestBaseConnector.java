package twetailer.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.DataSourceException;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.MockAppEngineEnvironment;
import twetailer.dto.Consumer;
import twetailer.dto.RawCommand;
import twetailer.dto.Retailer;
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

    @Test(expected=DataSourceException.class)
    public void testUnsupportedSource() throws DataSourceException {
        BaseConnector.communicateToUser(null, null, null, null, null);
    }

    @Test
    public void testSimulatedSource() throws DataSourceException {
        BaseConnector.resetLastCommunicationInSimulatedMode();
        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());

        final String message = "test";
        BaseConnector.communicateToUser(Source.simulated, null, null, null, message);

        assertEquals(BaseConnector.getLastCommunicationInSimulatedMode(), message);
    }

    @Test
    public void testFromRawCommand() throws DataSourceException {
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        final String message = "test";
        BaseConnector.communicateToEmitter(rawCommand, message);

        assertEquals(BaseConnector.getLastCommunicationInSimulatedMode(), message);
    }

    @Test
    @SuppressWarnings("serial")
    public void testTwitterSourceI() throws DataSourceException {
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

        BaseConnector.communicateToUser(Source.twitter, twitterId, null, null, message);

        MockTwitterConnector.restoreTwitterConnector(mockTwitterAccount, null);
    }

    @Test(expected=DataSourceException.class)
    @SuppressWarnings("serial")
    public void testTwitterSourceII() throws DataSourceException {
        final String twitterId = "tId";
        final String message = "test";
        final Twitter mockTwitterAccount = (new Twitter() {
            @Override
            public DirectMessage sendDirectMessage(String id, String text) throws TwitterException {
                throw new TwitterException("Done in purpose");
            }
        });
        MockTwitterConnector.injectMockTwitterAccount(mockTwitterAccount);

        BaseConnector.communicateToUser(Source.twitter, twitterId, null, null, message);

        MockTwitterConnector.restoreTwitterConnector(mockTwitterAccount, null);
    }

    @Test
    public void testJabberSource() throws DataSourceException {
        final String jabberId = "jId";
        final String message = "test";
        BaseConnector.communicateToUser(Source.jabber, null, jabberId, null, message);
    }

    @Test(expected=RuntimeException.class)
    public void testFacebookSource() throws DataSourceException {
        final String facebookId = "fId";
        final String message = "test";
        BaseConnector.communicateToUser(Source.facebook, null, null, facebookId, message);
    }

    @Test
    public void testCommunicateToConsumer() throws DataSourceException {
        BaseConnector.communicateToConsumer(Source.simulated, new Consumer(), null);
    }

    @Test
    public void testCommunicateToRetailer() throws DataSourceException {
        BaseConnector.communicateToRetailer(Source.simulated, new Retailer(), null);
    }

    @Test
    public void testH() throws DataSourceException {
        BaseConnector.resetLastCommunicationInSimulatedMode();
        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertNull(BaseConnector.getCommunicationForRetroIndexInSimulatedMode(0));
        assertNull(BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1));
        assertNull(BaseConnector.getCommunicationForRetroIndexInSimulatedMode(2));
        assertNull(BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1000));

        String first = "first";
        BaseConnector.communicateToRetailer(Source.simulated, new Retailer(), first);
        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertEquals(first, BaseConnector.getLastCommunicationInSimulatedMode());
        assertEquals(first, BaseConnector.getCommunicationForRetroIndexInSimulatedMode(0));
        assertNull(BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1));

        String second = "second";
        BaseConnector.communicateToRetailer(Source.simulated, new Retailer(), second);
        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertEquals(second, BaseConnector.getLastCommunicationInSimulatedMode());
        assertEquals(second, BaseConnector.getCommunicationForRetroIndexInSimulatedMode(0));
        assertEquals(first, BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1));
        assertNull(BaseConnector.getCommunicationForRetroIndexInSimulatedMode(2));
    }
}
