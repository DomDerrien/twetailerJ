package twetailer.adapter;

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

public class TestTwitterUtils {

    @Before
    public void setUp() throws Exception {
        TwitterUtils.resetAccountLists();
    }
    
    @Test
    public void testConstructor() {
        new TwitterUtils();
    }
    
    @Test
    public void testAccessorsI() {
        String current = TwitterUtils.getTwetailerScreenName();

        String proposed = "newForTests";
        TwitterUtils.setTwetailerScreenName(proposed);
        assertEquals(proposed, TwitterUtils.getTwetailerScreenName());
        
        TwitterUtils.setTwetailerScreenName(current);
    }

    @Test
    public void testAccessorsII() {
        String current = TwitterUtils.getTwetailerPassword();

        String proposed = "newForTests";
        TwitterUtils.setTwetailerPassword(proposed);
        assertEquals(proposed, TwitterUtils.getTwetailerPassword());
        
        TwitterUtils.setTwetailerPassword(current);
    }

    @Test
    public void testAccessorsIII() {
        String current = TwitterUtils.getRobotScreenName();

        String proposed = "newForTests";
        TwitterUtils.setRobotScreenName(proposed);
        assertEquals(proposed, TwitterUtils.getRobotScreenName());
        
        TwitterUtils.setRobotScreenName(current);
    }

    @Test
    public void testAccessorsIV() {
        String current = TwitterUtils.getRobotPassword();

        String proposed = "newForTests";
        TwitterUtils.setRobotPassword(proposed);
        assertEquals(proposed, TwitterUtils.getRobotPassword());
        
        TwitterUtils.setRobotPassword(current);
    }
    
    @Test
    public void testTwetailerAccountDistributorI() {
        Twitter first = TwitterUtils.getTwetailerAccount();
        Twitter second = TwitterUtils.getTwetailerAccount();

        assertNotSame(first, second);
        
        TwitterUtils.releaseTwetailerAccount(first);
        
        Twitter third = TwitterUtils.getTwetailerAccount();
        
        assertEquals(first, third);
    }
    
    @Test
    public void testTwetailerAccountDistributorII() {
        Twitter first = TwitterUtils.getRobotAccount();
        Twitter second = TwitterUtils.getRobotAccount();

        assertNotSame(first, second);
        
        TwitterUtils.releaseRobotAccount(first);
        
        Twitter third = TwitterUtils.getRobotAccount();
        
        assertEquals(first, third);
    }
    
    @Test
    public void testSendPublicMessage() throws TwitterException {
        Status status = EasyMock.createMock(Status.class);

        Twitter account = EasyMock.createMock(Twitter.class);
        EasyMock.expect(account.updateStatus("test")).andReturn(status).once();
        EasyMock.replay(account);
        
        // To inject the mock account
        TwitterUtils.releaseTwetailerAccount(account);
        
        Status response = TwitterUtils.sendPublicMessage("test");
        assertEquals(status, response);
    }
    
    @Test
    public void testSendDirectMessage() throws TwitterException {
        DirectMessage dm = EasyMock.createMock(DirectMessage.class);

        Twitter account = EasyMock.createMock(Twitter.class);
        EasyMock.expect(account.sendDirectMessage("target", "test")).andReturn(dm).once();
        EasyMock.replay(account);
        
        // To inject the mock account
        TwitterUtils.releaseTwetailerAccount(account);
        
        DirectMessage response = TwitterUtils.sendDirectMessage("target", "test");
        assertEquals(dm, response);
    }
    
    @Test
    public void testGetDirectMessages() throws TwitterException {
        final long sinceId = 12L;
        
        Twitter account = EasyMock.createMock(Twitter.class);
        EasyMock.expect(account.getDirectMessages((Paging) EasyMock.anyObject())).andReturn(new ArrayList<DirectMessage>()).once();
        EasyMock.replay(account);

        // To inject the mock account
        TwitterUtils.releaseTwetailerAccount(account);
        
        List<DirectMessage> messages = TwitterUtils.getDirectMessages(sinceId);
        assertEquals(0, messages.size());
    }
}