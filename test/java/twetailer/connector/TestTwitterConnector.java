package twetailer.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.util.ArrayList;
import java.util.List;

import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;

import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TestTwitterConnector {

    @Before
    public void setUp() throws Exception {
        TwitterConnector.resetAccountLists();
    }

    @Test
    public void testConstructor() {
        new TwitterConnector();
    }

    @Test
    public void testAccessorsI() {
        TwitterConnector.getTwetailerScreenName();
    }

    @Test
    public void testTwetailerAccountDistributorI() {
        Twitter first = TwitterConnector.getTwetailerAccount();
        Twitter second = TwitterConnector.getTwetailerAccount();

        assertNotSame(first, second);

        TwitterConnector.releaseTwetailerAccount(first);

        Twitter third = TwitterConnector.getTwetailerAccount();

        assertEquals(first, third);
    }

    @Test
    public void testTwetailerAccountDistributorII() {
        Twitter first = TwitterConnector.getRobotAccount();
        Twitter second = TwitterConnector.getRobotAccount();

        assertNotSame(first, second);

        TwitterConnector.releaseRobotAccount(first);

        Twitter third = TwitterConnector.getRobotAccount();

        assertEquals(first, third);
    }

    @Test
    public void testSendPublicMessageI() throws TwitterException {
        Status status = EasyMock.createMock(Status.class);

        Twitter account = EasyMock.createMock(Twitter.class);
        EasyMock.expect(account.updateStatus("test")).andReturn(status).once();
        EasyMock.replay(account);

        // To inject the mock account
        TwitterConnector.releaseTwetailerAccount(account);

        Status response = TwitterConnector.sendPublicMessage("test");
        assertEquals(status, response);
    }

    @Test(expected=TwitterException.class)
    public void testSendPublicMessageII() throws TwitterException {
        Status status = EasyMock.createMock(Status.class);

        Twitter account = EasyMock.createMock(Twitter.class);
        EasyMock.expect(account.updateStatus("test")).andThrow(new TwitterException("Done in purpose")).once();
        EasyMock.replay(account);

        // To inject the mock account
        TwitterConnector.releaseTwetailerAccount(account);

        Status response = TwitterConnector.sendPublicMessage("test");
        assertEquals(status, response);
    }

    @Test
    public void testSendDirectMessageI() throws TwitterException {
        DirectMessage dm = EasyMock.createMock(DirectMessage.class);

        Twitter account = EasyMock.createMock(Twitter.class);
        EasyMock.expect(account.sendDirectMessage("target", "test")).andReturn(dm).once();
        EasyMock.replay(account);

        // To inject the mock account
        TwitterConnector.releaseTwetailerAccount(account);

        DirectMessage response = TwitterConnector.sendDirectMessage("target", "test");
        assertEquals(dm, response);
    }

    @Test(expected=TwitterException.class)
    public void testSendDirectMessageII() throws TwitterException {
        DirectMessage dm = EasyMock.createMock(DirectMessage.class);

        Twitter account = EasyMock.createMock(Twitter.class);
        EasyMock.expect(account.sendDirectMessage("target", "test")).andThrow(new TwitterException("Done in purpose")).once();
        EasyMock.replay(account);

        // To inject the mock account
        TwitterConnector.releaseTwetailerAccount(account);

        DirectMessage response = TwitterConnector.sendDirectMessage("target", "test");
        assertEquals(dm, response);
    }

    @Test
    public void testGetDirectMessages() throws TwitterException {
        final long sinceId = 12L;

        Twitter account = EasyMock.createMock(Twitter.class);
        EasyMock.expect(account.getDirectMessages((Paging) EasyMock.anyObject())).andReturn(new ArrayList<DirectMessage>()).once();
        EasyMock.replay(account);

        // To inject the mock account
        TwitterConnector.releaseTwetailerAccount(account);

        List<DirectMessage> messages = TwitterConnector.getDirectMessages(sinceId);
        assertEquals(0, messages.size());
    }
}
