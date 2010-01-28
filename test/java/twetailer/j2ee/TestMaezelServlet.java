package twetailer.j2ee;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javamocks.util.logging.MockLogger;

import javax.jdo.PersistenceManager;
import javax.servlet.MockServletInputStream;
import javax.servlet.MockServletOutputStream;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.MockHttpServletRequest;
import javax.servlet.http.MockHttpServletResponse;

import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.connector.JabberConnector;
import twetailer.connector.MockTwitterConnector;
import twetailer.connector.TwitterConnector;
import twetailer.dao.BaseOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.MockBaseOperations;
import twetailer.dao.ProposalOperations;
import twetailer.dao.RawCommandOperations;
import twetailer.dao.SettingsOperations;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.Proposal;
import twetailer.dto.RawCommand;
import twetailer.dto.Settings;
import twetailer.task.CommandProcessor;
import twetailer.task.MockCommandProcessor;
import twetailer.task.MockDemandProcessor;
import twetailer.task.MockDemandValidator;
import twetailer.task.MockLocationValidator;
import twetailer.task.MockProposalProcessor;
import twetailer.task.MockProposalValidator;
import twetailer.task.MockRobotResponder;
import twetailer.task.MockTweetLoader;
import twetailer.validator.LocaleValidator;
import twetailer.validator.CommandSettings.State;
import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import com.dyuproject.openid.OpenIdUser;
import com.dyuproject.openid.YadisDiscovery;
import com.google.appengine.api.datastore.DatastoreTimeoutException;
import com.google.appengine.api.xmpp.MockXMPPService;
import com.google.apphosting.api.MockAppEngineEnvironment;

import domderrien.i18n.LabelExtractor;
import domderrien.i18n.LabelExtractor.ResourceFileId;

public class TestMaezelServlet {

    MaezelServlet servlet;

    private static MockAppEngineEnvironment mockAppEngineEnvironment;

    @BeforeClass
    public static void setUpBeforeClass() {
        MaezelServlet.setLogger(new MockLogger("test", null));
        mockAppEngineEnvironment = new MockAppEngineEnvironment();
    }

    @Before
    public void setUp() throws Exception {
        servlet = new MaezelServlet();
        servlet._baseOperations = new MockBaseOperations();
        mockAppEngineEnvironment.setUp();
    }

    @After
    public void tearDown() throws Exception {
        servlet.settingsOperations = new SettingsOperations();
        mockAppEngineEnvironment.tearDown();
        JabberConnector.injectMockXMPPService(null);
    }

    @Test
    public void testConstructor() {
        new MaezelServlet();
    }

    @Test
    public void testDoGetI() throws IOException {
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return null;
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };
        servlet.doGet(mockRequest, mockResponse);
        assertTrue(stream.contains("'success':true"));
    }

    @Test
    public void testDoGetII() throws IOException {
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "";
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };
        servlet.doGet(mockRequest, mockResponse);
        assertTrue(stream.contains("'success':true"));
    }

    @Test
    public void testDoGetIII() throws IOException {
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/zzz";
            }
            @Override
            public Map<String, ?> getParameterMap() {
                return new HashMap<String, Object>();
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };
        servlet.doGet(mockRequest, mockResponse);
        assertTrue(stream.contains("'success':false"));
    }

    @Test
    @SuppressWarnings("serial")
    public void testDoGetLoadTweets() throws IOException {
        // Inject a mock Twitter account
        final Twitter mockTwitterAccount = new Twitter() {
            @Override
            public List<DirectMessage> getDirectMessages(Paging paging) {
                return null;
            }
        };
        MockTwitterConnector.injectMockTwitterAccount(mockTwitterAccount);

        SettingsOperations mockSettingsOperations = new SettingsOperations() {
            @Override
            public Settings getSettings(PersistenceManager pm) {
                return new Settings();
            }
            @Override
            public Settings updateSettings(PersistenceManager pm, Settings settings) {
                return settings;
            }
        };

        // Inject mock operators
        MockTweetLoader.injectMocks(servlet._baseOperations);
        MockTweetLoader.injectMocks(mockSettingsOperations);

        // Prepare mock servlet parameters
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/loadTweets";
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        servlet.doGet(mockRequest, mockResponse);
        assertTrue(stream.contains(Settings.LAST_PROCESSED_DIRECT_MESSAGE_ID));
        assertTrue(stream.contains("1"));
        assertTrue(stream.contains("'success':true"));

        // Clean-up
        MockCommandProcessor.restoreOperations();
        MockTwitterConnector.restoreTwitterConnector(mockTwitterAccount, null);
    }

    @Test
    public void testDoGetProcessCommand() throws IOException {
        // Inject RawCommandOperations mock
        final Long commandKey = 12345L;
        RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) throws DataSourceException {
                assertEquals(commandKey, key);
                throw new DataSourceException("Done in purpose");
            }
        };
        SettingsOperations mockSettingsOperations = new SettingsOperations() {
            @Override
            public Settings getSettings(PersistenceManager pm) {
                return new Settings();
            }
            @Override
            public Settings updateSettings(PersistenceManager pm, Settings settings) {
                return settings;
            }
        };
        MockCommandProcessor.injectMocks(servlet._baseOperations);
        MockCommandProcessor.injectMocks(mockSettingsOperations);
        MockCommandProcessor.injectMocks(rawCommandOperations);

        // Prepare mock servlet parameters
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/processCommand";
            }
            @Override
            public String getParameter(String name) {
                assertEquals(Command.KEY, name);
                return commandKey.toString();
            }
            @Override
            public Map<String, ?> getParameterMap() {
                return new HashMap<String, Object>();
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        servlet.doGet(mockRequest, mockResponse);
        assertTrue(stream.contains("'success':false"));

        // Clean-up
        MockCommandProcessor.restoreOperations();
    }

    @Test
    public void testDoGetValidateLocation() throws Exception {
        final String postalCode = "H2N3C6";
        final String countryCode = "CA";
        final Long consumerKey = 111L;
        final Long rawCommandKey = 227L;
        final Double longitude = -73.3D;

        // Inject LocationOperations mock
        final LocationOperations mockLocationOperations = new LocationOperations() {
            @Override
            public List<Location> getLocations(PersistenceManager pm, String postalCode, String countryCode) {
                Location location = new Location();
                location.setLongitude(longitude);
                List<Location> locations = new ArrayList<Location>();
                locations.add(location);
                return locations;
            }
        };
        MockLocationValidator.injectMocks(servlet._baseOperations);
        MockLocationValidator.injectMocks(mockLocationOperations);

        // Prepare mock servlet parameters
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/validateLocation";
            }
            @Override
            public String getParameter(String name) {
                if (Location.POSTAL_CODE.equals(name)) {
                    return postalCode;
                }
                if (Location.COUNTRY_CODE.equals(name)) {
                    return countryCode;
                }
                if (Consumer.CONSUMER_KEY.equals(name)) {
                    return consumerKey.toString();
                }
                if (Command.KEY.equals(name)) {
                    return rawCommandKey.toString();
                }
                fail("Parameter query for " + name + " not expected");
                return null;
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        servlet.doGet(mockRequest, mockResponse);
        assertTrue(stream.contains("'success':true"));

        // Clean-up
        MockLocationValidator.restoreOperations();
    }

    @Test
    public void testDoGetValidateOpenDemand() throws IOException {
        final Long demandKey= 12345L;
        // Inject DemandOperations mock
        final DemandOperations mockDemandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setState(State.invalid);
                return demand;
            }
        };
        MockDemandValidator.injectMocks(servlet._baseOperations);
        MockDemandValidator.injectMocks(mockDemandOperations);

        // Prepare mock servlet parameters
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/validateOpenDemand";
            }
            @Override
            public String getParameter(String name) {
                assertEquals(Demand.KEY, name);
                return demandKey.toString();
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        servlet.doGet(mockRequest, mockResponse);
        assertTrue(stream.contains("'success':true"));

        // Clean-up
        MockDemandValidator.restoreOperations();
    }

    @Test
    public void testDoGetValidateOpenProposal() throws IOException {
        final Long proposalKey= 12345L;
        // Inject ProposalOperations mock
        final ProposalOperations mockProposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setState(State.invalid);
                return proposal;
            }
        };
        MockProposalValidator.injectMocks(servlet._baseOperations);
        MockProposalValidator.injectMocks(mockProposalOperations);

        // Prepare mock servlet parameters
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/validateOpenProposal";
            }
            @Override
            public String getParameter(String name) {
                assertEquals(Proposal.KEY, name);
                return proposalKey.toString();
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        servlet.doGet(mockRequest, mockResponse);
        assertTrue(stream.contains("'success':true"));

        // Clean-up
        MockProposalValidator.restoreOperations();
    }

    @Test
    public void testDoGetValidatePublishedDemand() throws IOException {
        final Long demandKey= 12345L;
        // Inject DemandOperations mock
        final DemandOperations mockDemandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setState(State.invalid);
                return demand;
            }
        };
        MockDemandProcessor.injectMocks(servlet._baseOperations);
        MockDemandProcessor.injectMocks(mockDemandOperations);

        // Prepare mock servlet parameters
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/processPublishedDemand";
            }
            @Override
            public String getParameter(String name) {
                assertEquals(Demand.KEY, name);
                return demandKey.toString();
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        servlet.doGet(mockRequest, mockResponse);
        assertTrue(stream.contains("'success':true"));

        // Clean-up
        MockDemandProcessor.restoreOperation();
    }

    @Test
    public void testDoGetValidatePublishedDemands() throws IOException {
        final Long demandKey= 12345L;
        // Inject DemandOperations mock
        final DemandOperations mockDemandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> parameters, int limit) {
                return new ArrayList<Demand>();
            }
        };
        MockDemandProcessor.injectMocks(servlet._baseOperations);
        MockDemandProcessor.injectMocks(mockDemandOperations);

        // Prepare mock servlet parameters
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/processPublishedDemands";
            }
            @Override
            public String getParameter(String name) {
                assertEquals(Demand.KEY, name);
                return demandKey.toString();
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        servlet.doGet(mockRequest, mockResponse);
        assertTrue(stream.contains("'success':true"));

        // Clean-up
        MockDemandProcessor.restoreOperation();
    }

    @Test
    public void testDoGetValidatePublishedProposal() throws IOException {
        final Long proposalKey= 12345L;
        // Inject ProposalOperations mock
        final ProposalOperations mockProposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setState(State.invalid);
                return proposal;
            }
        };
        MockProposalProcessor.injectMocks(servlet._baseOperations);
        MockProposalProcessor.injectMocks(mockProposalOperations);

        // Prepare mock servlet parameters
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/processPublishedProposal";
            }
            @Override
            public String getParameter(String name) {
                assertEquals(Proposal.KEY, name);
                return proposalKey.toString();
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        servlet.doGet(mockRequest, mockResponse);
        assertTrue(stream.contains("'success':true"));

        // Clean-up
        MockProposalProcessor.restoreOperation();
    }

    @Test
    public void testDoGetProcessDemandForRobot() throws IOException {
        final Long proposalKey= 12345L;
        // Inject SaleAssociateOperations mock
        final SettingsOperations mockSettingsOperations = new SettingsOperations() {
            @Override
            public Settings getSettings(PersistenceManager pm) {
                return new Settings();
            }
        };
        MockRobotResponder.injectMocks(servlet._baseOperations);
        MockRobotResponder.injectMocks(mockSettingsOperations);

        // Prepare mock servlet parameters
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/processDemandForRobot";
            }
            @Override
            public String getParameter(String name) {
                assertEquals(Proposal.KEY, name);
                return proposalKey.toString();
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        servlet.doGet(mockRequest, mockResponse);
        assertTrue(stream.contains("'success':true"));

        // Clean-up
        MockProposalProcessor.restoreOperation();
    }

    @Test
    public void testDoGetProcessCommandWithDatastoreTimeoutExceptionI() throws IOException {
        // Prepare mock servlet parameters
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/processCommand";
            }
            @Override
            public String getParameter(String name) {
                return "12345";
            }
            @Override
            public Map<String, ?> getParameterMap() {
                return new HashMap<String, Object>();
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        CommandProcessor._baseOperations = new MockBaseOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                throw new DatastoreTimeoutException("Done in purpose!");
            }
        };

        servlet.doGet(mockRequest, mockResponse);
        assertTrue(stream.contains("'success':false"));

        CommandProcessor._baseOperations = new BaseOperations();
    }

    @Test
    public void testDoGetProcessCommandWithDatastoreTimeoutExceptionII() throws IOException {
        // Prepare mock servlet parameters
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/processCommand";
            }
            @Override
            public String getParameter(String name) {
                throw new DatastoreTimeoutException("Done in purpose!");
            }
            @Override
            public Map<String, ?> getParameterMap() {
                return new HashMap<String, Object>();
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        servlet.doGet(mockRequest, mockResponse);
        assertTrue(stream.contains("'success':false"));
    }

    @Test
    public void testDoGetProcessCommandWithDatastoreTimeoutExceptionIII() throws IOException {
        // Prepare mock servlet parameters
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/processCommand";
            }
            @Override
            public String getParameter(String name) {
                throw new DatastoreTimeoutException("Done in purpose!");
            }
            @Override
            public Map<String, ?> getParameterMap() {
                return new HashMap<String, Object>();
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        CatchAllMailHandlerServlet.foolNextMessagePost(); // Will make CatchAllMailHandlerServlet.composeAndPostMailMessage() throwing a MessagingException!

        servlet.doGet(mockRequest, mockResponse);
        assertTrue(stream.contains("'success':false"));
    }

    @Test
    public void testDoPostI() throws IOException {
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return null;
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };
        servlet.doPost(mockRequest, mockResponse);
        assertTrue(stream.contains("'success':true"));
    }

    @Test
    public void testDoPostII() throws IOException {
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "";
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };
        servlet.doPost(mockRequest, mockResponse);
        assertTrue(stream.contains("'success':true"));
    }

    @Test
    public void testDoPostIII() throws IOException {
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/zzz";
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };
        servlet.doPost(mockRequest, mockResponse);
        assertTrue(stream.contains("'success':false"));
    }

    @Test
    public void testDoPostProcessCommandWithDatastoreTimeoutExceptionI() throws IOException {
        // Prepare mock servlet parameters
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/processVerificationCode";
            }
            @Override
            public String getParameter(String name) {
                return "12345";
            }
            @Override
            public ServletInputStream getInputStream() {
                throw new IllegalArgumentException("Done in purpose!");
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        servlet.doPost(mockRequest, mockResponse);
        assertTrue(stream.contains("'success':false"));
    }

    @Test
    public void testDoPostProcessCommandWithDatastoreTimeoutExceptionII() throws IOException {
        // Prepare mock servlet parameters
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/processVerificationCode";
            }
            @Override
            public String getParameter(String name) {
                return "12345";
            }
            @Override
            public ServletInputStream getInputStream() {
                throw new IllegalArgumentException("Done in purpose!");
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        CatchAllMailHandlerServlet.foolNextMessagePost(); // Will make CatchAllMailHandlerServlet.composeAndPostMailMessage() throwing a MessagingException!

        servlet.doPost(mockRequest, mockResponse);
        assertTrue(stream.contains("'success':false"));
    }

    @Test(expected=RuntimeException.class)
    public void testGetVerificationCodeI() throws IOException, ClientException {
        String topic = "zzz";
        String identifier = "unit@test.ca";
        String openId = "http://openId";

        MaezelServlet.getCode(topic, identifier, openId);
    }

    @Test
    public void testGetVerificationCodeII() throws IOException, ClientException {
        String topic = Consumer.EMAIL;
        String identifier = "unit@test.ca";
        String openId = "http://openId";

        long code = MaezelServlet.getCode(topic, identifier, openId);

        assertEquals(code, MaezelServlet.getCode(topic, identifier, openId));
    }

    @Test
    public void testGetVerificationCodeIII() throws IOException, ClientException {
        String topic = Consumer.JABBER_ID;
        String identifier = "unit@test.ca";
        String openId = "http://openId";

        long code = MaezelServlet.getCode(topic, identifier, openId);

        assertEquals(code, MaezelServlet.getCode(topic, identifier, openId));
    }

    @Test
    public void testGetVerificationCodeIV() throws IOException, ClientException {
        String topic = Consumer.TWITTER_ID;
        String identifier = "unit_test_ca";
        String openId = "http://openId";

        long code = MaezelServlet.getCode(topic, identifier, openId);

        assertEquals(code, MaezelServlet.getCode(topic, identifier, openId));
    }

    @Test
    public void testGetVerificationCodeV() throws IOException, ClientException {
        String identifier1 = "unit@test.ca";
        String identifier2 = "unit_test_ca";
        String openId = "http://openId";

        long codeEmail = MaezelServlet.getCode(Consumer.EMAIL, identifier1, openId);
        long codeJabber = MaezelServlet.getCode(Consumer.JABBER_ID, identifier1, openId);
        long codeTwitter = MaezelServlet.getCode(Consumer.TWITTER_ID, identifier2, openId);

        assertNotSame(codeEmail, codeJabber);
        assertNotSame(codeEmail, codeTwitter);
        assertNotSame(codeJabber, codeTwitter);
    }

    @Test
    public void testGetVerificationCodeVI() throws IOException, ClientException {
        String openId = "http://openId";

        long codeNull = MaezelServlet.getCode("Not important", null, openId);
        long codeEmpty = MaezelServlet.getCode("Not important", "", openId);

        assertNotSame(9999999999L, codeNull);
        assertNotSame(9999999999L, codeEmpty);
        assertNotSame(codeNull, codeEmpty);
    }

    @Test(expected=ClientException.class)
    public void testGetVerificationCodeVII() throws IOException, ClientException {
        String identifier = "invalid e-mail address";
        String openId = "http://openId";

        MaezelServlet.getCode(Consumer.EMAIL, identifier, openId);
    }

    @Test(expected=ClientException.class)
    public void testGetVerificationCodeVIII() throws IOException, ClientException {
        String identifier = "@invalid +Twitter+ identifier (format)";
        String openId = "http://openId";

        MaezelServlet.getCode(Consumer.TWITTER_ID, identifier, openId);
    }

    @Test
    public void testWaitForVerificationCodeI() throws IOException {
        final String openId = "http://openId";
        final String identifier = "unit@test.ca";
        final String topic = Consumer.EMAIL;

        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/processVerificationCode";
            }
            @Override
            public ServletInputStream getInputStream() {
                return new MockServletInputStream(
                        "{'topic':'" + topic + "'" +
                        ",'waitForCode':" + Boolean.TRUE +
                        ",'" + Consumer.LANGUAGE + "':'" + LocaleValidator.DEFAULT_LANGUAGE + "'" +
                        ",'" + topic + "':'" + identifier + "'" +
                        "}"
                );
            }
            @Override
            public Object getAttribute(String name) {
                if (OpenIdUser.ATTR_NAME.equals(name)) {
                    OpenIdUser user = OpenIdUser.populate(
                            "http://www.yahoo.com",
                            YadisDiscovery.IDENTIFIER_SELECT,
                            LoginServlet.YAHOO_OPENID_SERVER_URL
                    );
                    Map<String, Object> json = new HashMap<String, Object>();
                    // {a: "claimId", b: "identity", c: "assocHandle", d: associationData, e: "openIdServer", f: "openIdDelegate", g: attributes, h: "identifier"}
                    json.put("a", openId);
                    user.fromJSON(json);
                    return user;
                }
                fail("Attribute access not expected for: " + name);
                return null;
            }
        };

        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        servlet.doPost(mockRequest, mockResponse);
        assertTrue(stream.contains("'success':true"));
        // No way to check what's send by e-mail...
        // See test done for topic == Twitter
    }

    @Test
    public void testWaitForVerificationCodeII() throws IOException, ClientException {
        final String openId = "http://openId";
        final String identifier = "unit@test.ca";
        final String topic = Consumer.JABBER_ID;

        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/processVerificationCode";
            }
            @Override
            public ServletInputStream getInputStream() {
                return new MockServletInputStream(
                        "{'topic':'" + topic + "'" +
                        ",'waitForCode':" + Boolean.TRUE +
                        ",'" + Consumer.LANGUAGE + "':'" + LocaleValidator.DEFAULT_LANGUAGE + "'" +
                        ",'" + topic + "':'" + identifier + "'" +
                        "}"
                );
            }
            @Override
            public Object getAttribute(String name) {
                if (OpenIdUser.ATTR_NAME.equals(name)) {
                    OpenIdUser user = OpenIdUser.populate(
                            "http://www.yahoo.com",
                            YadisDiscovery.IDENTIFIER_SELECT,
                            LoginServlet.YAHOO_OPENID_SERVER_URL
                    );
                    Map<String, Object> json = new HashMap<String, Object>();
                    // {a: "claimId", b: "identity", c: "assocHandle", d: associationData, e: "openIdServer", f: "openIdDelegate", g: attributes, h: "identifier"}
                    json.put("a", openId);
                    user.fromJSON(json);
                    return user;
                }
                fail("Attribute access not expected for: " + name);
                return null;
            }
        };

        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        MockXMPPService mock = new MockXMPPService();
        mock.setPresence(identifier, true);
        JabberConnector.injectMockXMPPService(mock);

        servlet.doPost(mockRequest, mockResponse);
        assertTrue(stream.contains("'success':true"));

        String sentMessage = mock.getLastSentMessage().getBody();
        assertTrue(sentMessage.contains(String.valueOf(MaezelServlet.getCode(topic, identifier, openId))));
    }

    @Test
    public void testWaitForVerificationCodeIII() throws IOException, TwitterException, ClientException {
        final String openId = "http://openId";
        final String identifier = "unit_test_ca";
        final String topic = Consumer.TWITTER_ID;

        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/processVerificationCode";
            }
            @Override
            public ServletInputStream getInputStream() {
                return new MockServletInputStream(
                        "{'topic':'" + topic + "'" +
                        ",'waitForCode':" + Boolean.TRUE +
                        ",'" + Consumer.LANGUAGE + "':'" + LocaleValidator.DEFAULT_LANGUAGE + "'" +
                        ",'" + topic + "':'" + identifier + "'" +
                        "}"
                );
            }
            @Override
            public Object getAttribute(String name) {
                if (OpenIdUser.ATTR_NAME.equals(name)) {
                    OpenIdUser user = OpenIdUser.populate(
                            "http://www.yahoo.com",
                            YadisDiscovery.IDENTIFIER_SELECT,
                            LoginServlet.YAHOO_OPENID_SERVER_URL
                    );
                    Map<String, Object> json = new HashMap<String, Object>();
                    // {a: "claimId", b: "identity", c: "assocHandle", d: associationData, e: "openIdServer", f: "openIdDelegate", g: attributes, h: "identifier"}
                    json.put("a", openId);
                    user.fromJSON(json);
                    return user;
                }
                fail("Attribute access not expected for: " + name);
                return null;
            }
        };

        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        // Expected messages
        final long code = MaezelServlet.getCode(topic, identifier, openId);
        String msg1 = LabelExtractor.get(ResourceFileId.third, "consumer_info_verification_notification_title", Locale.ENGLISH);
        String msg2 = LabelExtractor.get(ResourceFileId.third, "consumer_info_verification_notification_body", new Object[] { code }, Locale.ENGLISH);

        // To inject the mock account
        DirectMessage dm = EasyMock.createMock(DirectMessage.class);
        Twitter account = EasyMock.createMock(Twitter.class);
        EasyMock.expect(account.sendDirectMessage(identifier, msg1)).andReturn(dm).once();
        EasyMock.expect(account.sendDirectMessage(identifier, msg2)).andReturn(dm).once();
        EasyMock.replay(account);
        TwitterConnector.releaseTwetailerAccount(account);

        servlet.doPost(mockRequest, mockResponse);
        assertTrue(stream.contains("'success':true"));

        TwitterConnector.getTwetailerAccount(); // To remove the injected TwitterAccount from the connector pool
    }

    @Test
    public void testGoodVerificationCodeI() throws IOException, ClientException {
        final String openId = "http://openId";
        final String identifier = "unit@test.ca";
        final String topic = Consumer.EMAIL;
        final long code = MaezelServlet.getCode(topic, identifier, openId);

        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/processVerificationCode";
            }
            @Override
            public ServletInputStream getInputStream() {
                return new MockServletInputStream(
                        "{'topic':'" + topic + "'" +
                        ",'waitForCode':" + Boolean.FALSE +
                        ",'" + Consumer.LANGUAGE + "':'" + LocaleValidator.DEFAULT_LANGUAGE + "'" +
                        ",'" + topic + "':'" + identifier + "'" +
                        ",'" + topic + "Code':" + code +
                        "}"
                );
            }
            @Override
            public Object getAttribute(String name) {
                if (OpenIdUser.ATTR_NAME.equals(name)) {
                    OpenIdUser user = OpenIdUser.populate(
                            "http://www.yahoo.com",
                            YadisDiscovery.IDENTIFIER_SELECT,
                            LoginServlet.YAHOO_OPENID_SERVER_URL
                    );
                    Map<String, Object> json = new HashMap<String, Object>();
                    // {a: "claimId", b: "identity", c: "assocHandle", d: associationData, e: "openIdServer", f: "openIdDelegate", g: attributes, h: "identifier"}
                    json.put("a", openId);
                    user.fromJSON(json);
                    return user;
                }
                fail("Attribute access not expected for: " + name);
                return null;
            }
        };

        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        servlet.doPost(mockRequest, mockResponse);
        assertTrue(stream.contains("'success':true"));
        assertTrue(stream.contains("'codeValidity':true"));
    }

    @Test
    public void testGoodVerificationCodeII() throws IOException, ClientException {
        final String openId = "http://openId";
        final String identifier = "unit@test.ca";
        final String topic = Consumer.JABBER_ID;
        final long code = MaezelServlet.getCode(topic, identifier, openId);

        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/processVerificationCode";
            }
            @Override
            public ServletInputStream getInputStream() {
                return new MockServletInputStream(
                        "{'topic':'" + topic + "'" +
                        ",'waitForCode':" + Boolean.FALSE +
                        ",'" + Consumer.LANGUAGE + "':'" + LocaleValidator.DEFAULT_LANGUAGE + "'" +
                        ",'" + topic + "':'" + identifier + "'" +
                        ",'" + topic + "Code':" + code +
                        "}"
                );
            }
            @Override
            public Object getAttribute(String name) {
                if (OpenIdUser.ATTR_NAME.equals(name)) {
                    OpenIdUser user = OpenIdUser.populate(
                            "http://www.yahoo.com",
                            YadisDiscovery.IDENTIFIER_SELECT,
                            LoginServlet.YAHOO_OPENID_SERVER_URL
                    );
                    Map<String, Object> json = new HashMap<String, Object>();
                    // {a: "claimId", b: "identity", c: "assocHandle", d: associationData, e: "openIdServer", f: "openIdDelegate", g: attributes, h: "identifier"}
                    json.put("a", openId);
                    user.fromJSON(json);
                    return user;
                }
                fail("Attribute access not expected for: " + name);
                return null;
            }
        };

        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        servlet.doPost(mockRequest, mockResponse);
        assertTrue(stream.contains("'success':true"));
        assertTrue(stream.contains("'codeValidity':true"));
    }

    @Test
    public void testGoodVerificationCodeIII() throws IOException, ClientException {
        final String openId = "http://openId";
        final String identifier = "unit_test_ca";
        final String topic = Consumer.TWITTER_ID;
        final long code = MaezelServlet.getCode(topic, identifier, openId);

        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/processVerificationCode";
            }
            @Override
            public ServletInputStream getInputStream() {
                return new MockServletInputStream(
                        "{'topic':'" + topic + "'" +
                        ",'waitForCode':" + Boolean.FALSE +
                        ",'" + Consumer.LANGUAGE + "':'" + LocaleValidator.DEFAULT_LANGUAGE + "'" +
                        ",'" + topic + "':'" + identifier + "'" +
                        ",'" + topic + "Code':" + code +
                        "}"
                );
            }
            @Override
            public Object getAttribute(String name) {
                if (OpenIdUser.ATTR_NAME.equals(name)) {
                    OpenIdUser user = OpenIdUser.populate(
                            "http://www.yahoo.com",
                            YadisDiscovery.IDENTIFIER_SELECT,
                            LoginServlet.YAHOO_OPENID_SERVER_URL
                    );
                    Map<String, Object> json = new HashMap<String, Object>();
                    // {a: "claimId", b: "identity", c: "assocHandle", d: associationData, e: "openIdServer", f: "openIdDelegate", g: attributes, h: "identifier"}
                    json.put("a", openId);
                    user.fromJSON(json);
                    return user;
                }
                fail("Attribute access not expected for: " + name);
                return null;
            }
        };

        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        servlet.doPost(mockRequest, mockResponse);
        assertTrue(stream.contains("'success':true"));
        assertTrue(stream.contains("'codeValidity':true"));
    }

    @Test
    public void testBadVerificationCodeI() throws IOException {
        final String openId = "http://openId";
        final String identifier = "unit@test.ca";
        final String topic = Consumer.EMAIL;
        final long code = 0;

        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/processVerificationCode";
            }
            @Override
            public ServletInputStream getInputStream() {
                return new MockServletInputStream(
                        "{'topic':'" + topic + "'" +
                        ",'waitForCode':" + Boolean.FALSE +
                        ",'" + Consumer.LANGUAGE + "':'" + LocaleValidator.DEFAULT_LANGUAGE + "'" +
                        ",'" + topic + "':'" + identifier + "'" +
                        ",'" + topic + "Code':" + code +
                        "}"
                );
            }
            @Override
            public Object getAttribute(String name) {
                if (OpenIdUser.ATTR_NAME.equals(name)) {
                    OpenIdUser user = OpenIdUser.populate(
                            "http://www.yahoo.com",
                            YadisDiscovery.IDENTIFIER_SELECT,
                            LoginServlet.YAHOO_OPENID_SERVER_URL
                    );
                    Map<String, Object> json = new HashMap<String, Object>();
                    // {a: "claimId", b: "identity", c: "assocHandle", d: associationData, e: "openIdServer", f: "openIdDelegate", g: attributes, h: "identifier"}
                    json.put("a", openId);
                    user.fromJSON(json);
                    return user;
                }
                fail("Attribute access not expected for: " + name);
                return null;
            }
        };

        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        servlet.doPost(mockRequest, mockResponse);
        assertTrue(stream.contains("'success':true"));
        assertTrue(stream.contains("'codeValidity':false"));
    }

    @Test
    public void testBadVerificationCodeII() throws IOException {
        final String openId = "http://openId";
        final String identifier = "unit@test.ca";
        final String topic = Consumer.JABBER_ID;
        final long code = 0;

        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/processVerificationCode";
            }
            @Override
            public ServletInputStream getInputStream() {
                return new MockServletInputStream(
                        "{'topic':'" + topic + "'" +
                        ",'waitForCode':" + Boolean.FALSE +
                        ",'" + Consumer.LANGUAGE + "':'" + LocaleValidator.DEFAULT_LANGUAGE + "'" +
                        ",'" + topic + "':'" + identifier + "'" +
                        ",'" + topic + "Code':" + code +
                        "}"
                );
            }
            @Override
            public Object getAttribute(String name) {
                if (OpenIdUser.ATTR_NAME.equals(name)) {
                    OpenIdUser user = OpenIdUser.populate(
                            "http://www.yahoo.com",
                            YadisDiscovery.IDENTIFIER_SELECT,
                            LoginServlet.YAHOO_OPENID_SERVER_URL
                    );
                    Map<String, Object> json = new HashMap<String, Object>();
                    // {a: "claimId", b: "identity", c: "assocHandle", d: associationData, e: "openIdServer", f: "openIdDelegate", g: attributes, h: "identifier"}
                    json.put("a", openId);
                    user.fromJSON(json);
                    return user;
                }
                fail("Attribute access not expected for: " + name);
                return null;
            }
        };

        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        servlet.doPost(mockRequest, mockResponse);
        assertTrue(stream.contains("'success':true"));
        assertTrue(stream.contains("'codeValidity':false"));
    }

    @Test
    public void testBadVerificationCodeIII() throws IOException {
        final String openId = "http://openId";
        final String identifier = "unit_test_ca";
        final String topic = Consumer.TWITTER_ID;
        final long code = 0;

        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/processVerificationCode";
            }
            @Override
            public ServletInputStream getInputStream() {
                return new MockServletInputStream(
                        "{'topic':'" + topic + "'" +
                        ",'waitForCode':" + Boolean.FALSE +
                        ",'" + Consumer.LANGUAGE + "':'" + LocaleValidator.DEFAULT_LANGUAGE + "'" +
                        ",'" + topic + "':'" + identifier + "'" +
                        ",'" + topic + "Code':" + code +
                        "}"
                );
            }
            @Override
            public Object getAttribute(String name) {
                if (OpenIdUser.ATTR_NAME.equals(name)) {
                    OpenIdUser user = OpenIdUser.populate(
                            "http://www.yahoo.com",
                            YadisDiscovery.IDENTIFIER_SELECT,
                            LoginServlet.YAHOO_OPENID_SERVER_URL
                    );
                    Map<String, Object> json = new HashMap<String, Object>();
                    // {a: "claimId", b: "identity", c: "assocHandle", d: associationData, e: "openIdServer", f: "openIdDelegate", g: attributes, h: "identifier"}
                    json.put("a", openId);
                    user.fromJSON(json);
                    return user;
                }
                fail("Attribute access not expected for: " + name);
                return null;
            }
        };

        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        servlet.doPost(mockRequest, mockResponse);
        assertTrue(stream.contains("'success':true"));
        assertTrue(stream.contains("'codeValidity':false"));
    }

    @Test
    public void testBadTopic() throws IOException {
        final String openId = "http://openId";
        final String identifier = "unit@test.ca";
        final String topic = "zzz";
        final long code = 0;

        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/processVerificationCode";
            }
            @Override
            public ServletInputStream getInputStream() {
                return new MockServletInputStream(
                        "{'topic':'" + topic + "'" +
                        ",'waitForCode':" + Boolean.FALSE +
                        ",'" + Consumer.LANGUAGE + "':'" + LocaleValidator.DEFAULT_LANGUAGE + "'" +
                        ",'" + topic + "':'" + identifier + "'" +
                        ",'" + topic + "Code':" + code +
                        "}"
                );
            }
            @Override
            public Object getAttribute(String name) {
                if (OpenIdUser.ATTR_NAME.equals(name)) {
                    OpenIdUser user = OpenIdUser.populate(
                            "http://www.yahoo.com",
                            YadisDiscovery.IDENTIFIER_SELECT,
                            LoginServlet.YAHOO_OPENID_SERVER_URL
                    );
                    Map<String, Object> json = new HashMap<String, Object>();
                    // {a: "claimId", b: "identity", c: "assocHandle", d: associationData, e: "openIdServer", f: "openIdDelegate", g: attributes, h: "identifier"}
                    json.put("a", openId);
                    user.fromJSON(json);
                    return user;
                }
                fail("Attribute access not expected for: " + name);
                return null;
            }
        };

        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        servlet.doPost(mockRequest, mockResponse);
        assertTrue(stream.contains("'success':false"));
    }

    @Test
    public void testDoGetSetupRobotCoordinates() throws IOException {
        final Long consumerKey = 12321L;
        final Long saleAssociateKey = 45654L;
        final Settings settings = new Settings();

        // Prepare mock SettingsOperations
        SettingsOperations mockSettingsOperations = new SettingsOperations() {
            @Override
            public Settings getSettings(PersistenceManager pm) {
                return settings;
            }
            @Override
            public Settings updateSettings(PersistenceManager pm, Settings updatedSettings) {
                assertEquals(settings, updatedSettings);
                assertEquals(consumerKey, updatedSettings.getRobotConsumerKey());
                assertEquals(saleAssociateKey, updatedSettings.getRobotSaleAssociateKey());
                return updatedSettings;
            }
        };
        servlet.settingsOperations = mockSettingsOperations;

        // Prepare mock servlet parameters
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/setupRobotCoordinates";
            }
            @Override
            public String getParameter(String name) {
                if ("consumerKey".equals(name)) {
                    return consumerKey.toString();
                }
                if ("saleAssociateKey".equals(name)) {
                    return saleAssociateKey.toString();
                }
                fail("Parameter query for " + name + " not expected");
                return null;
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        servlet.doGet(mockRequest, mockResponse);
        assertTrue(stream.contains("'success':true"));

        // Clean-up
        MockCommandProcessor.restoreOperations();
    }

    @Test
    public void testDoGetConsolidateConsumerAccounts() throws IOException {
        final Long consumerKey = 12321L;
        final Long demandKey = 45654L;
        final Demand demand = new Demand();
        demand.setKey(demandKey);

        // Prepare mock DemandOperations
        DemandOperations mockDemandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long ownerKey) {
                assertEquals(demandKey, key);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand updatedDemand) {
                assertEquals(demand, updatedDemand);
                assertEquals(consumerKey, updatedDemand.getOwnerKey());
                return updatedDemand;
            }
        };
        servlet.demandOperations = mockDemandOperations;

        // Prepare mock servlet parameters
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/consolidateConsumerAccounts";
            }
            @Override
            public String getParameter(String name) {
                if ("ownerKey".equals(name)) {
                    return consumerKey.toString();
                }
                if ("key".equals(name)) {
                    return demandKey.toString();
                }
                fail("Parameter query for " + name + " not expected");
                return null;
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        servlet.doGet(mockRequest, mockResponse);
        assertTrue(stream.contains("'success':true"));

        // Clean-up
        MockCommandProcessor.restoreOperations();
    }
}
