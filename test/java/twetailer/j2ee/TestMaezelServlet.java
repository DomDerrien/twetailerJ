package twetailer.j2ee;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.servlet.MockServletOutputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.MockHttpServletRequest;
import javax.servlet.http.MockHttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.DataSourceException;
import twetailer.connector.MockTwitterConnector;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.MockAppEngineEnvironment;
import twetailer.dao.MockPersistenceManager;
import twetailer.dao.ProposalOperations;
import twetailer.dao.RawCommandOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dao.SettingsOperations;
import twetailer.dao.StoreOperations;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.Proposal;
import twetailer.dto.RawCommand;
import twetailer.dto.SaleAssociate;
import twetailer.dto.Settings;
import twetailer.dto.Store;
import twetailer.task.MockCommandProcessor;
import twetailer.task.MockDemandProcessor;
import twetailer.task.MockDemandValidator;
import twetailer.task.MockProposalProcessor;
import twetailer.task.MockProposalValidator;
import twetailer.task.MockTweetLoader;
import twetailer.validator.CommandSettings.State;
import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.Twitter;

public class TestMaezelServlet {

    private class MockBaseOperations extends BaseOperations {
        private PersistenceManager pm = new MockPersistenceManager();
        @Override
        public PersistenceManager getPersistenceManager() {
            return pm;
        }
        @Override
        public SettingsOperations getSettingsOperations() {
            return new SettingsOperations() {
                @Override
                public Settings getSettings(PersistenceManager pm) {
                    return new Settings();
                }
                @Override
                public Settings updateSettings(PersistenceManager pm, Settings settings) {
                    return settings;
                }
            };
        }
    };

    MaezelServlet servlet;

    @Before
    public void setUp() throws Exception {
        servlet = new MaezelServlet();
        servlet._baseOperations = new MockBaseOperations();
    }

    @After
    public void tearDown() throws Exception {
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

        // Inject mock operators
        MockTweetLoader.injectMocks(servlet._baseOperations);
        MockTweetLoader.injectMocks(servlet._baseOperations.getSettingsOperations());

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
        MockCommandProcessor.injectMocks(servlet._baseOperations);
        MockCommandProcessor.injectMocks(servlet._baseOperations.getSettingsOperations());
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
    public void testDoGetCreateStoreI() throws IOException {
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/createStore";
            }
            @Override
            public String getParameter(String key) {
                if (Store.LOCATION_KEY.equals(key)) { return "12345"; }
                if (Store.ADDRESS.equals(key)) { return "address"; }
                if (Store.NAME.equals(key)) { return "name"; }
                fail("Unexpected parameter gathering for: " + key);
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
        final LocationOperations mockLocationOperations = new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                return new Location();
            }
        };
        final StoreOperations mockStoreOperations = new StoreOperations() {
            @Override
            public Store createStore(Store store) {
                return store;
            }
        };

        servlet.locationOperations = mockLocationOperations;
        servlet.storeOperations = mockStoreOperations;
        servlet.doGet(mockRequest, mockResponse);
        assertTrue(stream.contains("'success':true"));
        assertTrue(servlet._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testDoGetCreateStoreII() throws IOException {
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/createStore";
            }
            @Override
            public String getParameter(String key) {
                throw new RuntimeException("Done in purpose");
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
        assertTrue(servlet._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testDoGetCreateSaleAssociateI() throws IOException {
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/createSaleAssociate";
            }
            @Override
            public String getParameter(String key) {
                if (SaleAssociate.CONSUMER_KEY.equals(key)) { return "12345"; }
                if (SaleAssociate.STORE_KEY.equals(key)) { return "67890"; }
                if (SaleAssociate.NAME.equals(key)) { return "name"; }
                if ("supplies".equals(key)) { return "one two three"; }
                fail("Unexpected parameter gathering for: " + key);
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
        final ConsumerOperations mockConsumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                return new Consumer();
            }
        };
        final SaleAssociateOperations mockSaleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public SaleAssociate createSaleAssociate(PersistenceManager pm, Consumer consumer, Long storeKey) {
                assertEquals(Long.valueOf(67890L), storeKey);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(12345L);
                return saleAssociate;
            }
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                assertEquals(Long.valueOf(12345L), key);
                return new SaleAssociate();
            }
            @Override
            public SaleAssociate updateSaleAssociate(PersistenceManager pm, SaleAssociate saleAssociate) {
                return saleAssociate;
            }
        };

        servlet.consumerOperations = mockConsumerOperations;
        servlet.saleAssociateOperations = mockSaleAssociateOperations;
        servlet.doGet(mockRequest, mockResponse);
        assertTrue(stream.contains("'success':true"));
        assertTrue(servlet._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testDoGetCreateSaleAssociateII() throws IOException {
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/createSaleAssociate";
            }
            @Override
            public String getParameter(String key) {
                if (SaleAssociate.CONSUMER_KEY.equals(key)) { return "12345"; }
                if (SaleAssociate.STORE_KEY.equals(key)) { return "67890"; }
                if (SaleAssociate.NAME.equals(key)) { return null; }
                if ("supplies".equals(key)) { return "one two three"; }
                fail("Unexpected parameter gathering for: " + key);
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
        final ConsumerOperations mockConsumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                return new Consumer();
            }
        };
        final SaleAssociateOperations mockSaleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public SaleAssociate createSaleAssociate(PersistenceManager pm, Consumer consumer, Long storeKey) {
                assertEquals(Long.valueOf(67890L), storeKey);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(12345L);
                return saleAssociate;
            }
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                assertEquals(Long.valueOf(12345L), key);
                return new SaleAssociate();
            }
            @Override
            public SaleAssociate updateSaleAssociate(PersistenceManager pm, SaleAssociate saleAssociate) {
                return saleAssociate;
            }
        };

        servlet.consumerOperations = mockConsumerOperations;
        servlet.saleAssociateOperations = mockSaleAssociateOperations;
        servlet.doGet(mockRequest, mockResponse);
        assertTrue(stream.contains("'success':true"));
        assertTrue(servlet._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testDoGetCreateSaleAssociateIII() throws IOException {
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/createSaleAssociate";
            }
            @Override
            public String getParameter(String key) {
                throw new RuntimeException("Done in purpose");
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
        assertTrue(servlet._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testDoGetUpdateConsumerI() throws IOException {
        final String address = "addr";
        final String email = "e-mail";
        final String jabberId = "jId";
        final Long locationKey = 12345L;
        final String language = "fr";
        final String name = "bozo";
        final String phoneNumber = "514";
        final String twitterId = "tID";
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/updateConsumer";
            }
            @Override
            public String getParameter(String key) {
                if (Consumer.ADDRESS.equals(key)) { return address; }
                if (Consumer.EMAIL.equals(key)) { return email; }
                if (Consumer.JABBER_ID.equals(key)) { return jabberId; }
                if (Consumer.LOCATION_KEY.equals(key)) { return locationKey.toString(); }
                if (Consumer.LANGUAGE.equals(key)) { return language; }
                if (Consumer.NAME.equals(key)) { return name; }
                if (Consumer.PHONE_NUMBER.equals(key)) { return phoneNumber; }
                if (Consumer.TWITTER_ID.equals(key)) { return twitterId; }
                fail("Unexpected parameter gathering for: " + key);
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
        final ConsumerOperations mockConsumerOperations = new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Consumer.TWITTER_ID, key);
                assertEquals(twitterId, (String) value);
                Consumer consumer = new Consumer();
                consumer.setTwitterId(twitterId);
                List<Consumer> consumers = new ArrayList<Consumer>();
                consumers.add(consumer);
                return consumers;
            }
            @Override
            public Consumer updateConsumer(PersistenceManager pm, Consumer consumer) {
                assertEquals(address, consumer.getAddress());
                assertEquals(email, consumer.getEmail());
                assertEquals(jabberId, consumer.getJabberId());
                assertEquals(language, consumer.getLanguage());
                assertEquals(locationKey, consumer.getLocationKey());
                assertEquals(name, consumer.getName());
                assertEquals(phoneNumber, consumer.getPhoneNumber());
                assertEquals(twitterId, consumer.getTwitterId());
                return consumer;
            }
        };

        servlet.consumerOperations = mockConsumerOperations;
        servlet.doGet(mockRequest, mockResponse);
        assertTrue(stream.contains("'success':true"));
        assertTrue(servlet._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testDoGetUpdateConsumerII() throws IOException {
        final String twitterId = "tID";
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/updateConsumer";
            }
            @Override
            public String getParameter(String key) {
                if (Consumer.ADDRESS.equals(key)) { return null; }
                if (Consumer.EMAIL.equals(key)) { return null; }
                if (Consumer.JABBER_ID.equals(key)) { return null; }
                if (Consumer.LOCATION_KEY.equals(key)) { return null; }
                if (Consumer.LANGUAGE.equals(key)) { return null; }
                if (Consumer.NAME.equals(key)) { return null; }
                if (Consumer.PHONE_NUMBER.equals(key)) { return null; }
                if (Consumer.TWITTER_ID.equals(key)) { return twitterId; }
                fail("Unexpected parameter gathering for: " + key);
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
        final ConsumerOperations mockConsumerOperations = new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Consumer.TWITTER_ID, key);
                assertEquals(twitterId, (String) value);
                Consumer consumer = new Consumer();
                consumer.setTwitterId(twitterId);
                List<Consumer> consumers = new ArrayList<Consumer>();
                consumers.add(consumer);
                return consumers;
            }
            @Override
            public Consumer updateConsumer(PersistenceManager pm, Consumer consumer) {
                fail("Not update expected");
                return consumer;
            }
        };

        servlet.consumerOperations = mockConsumerOperations;
        servlet.doGet(mockRequest, mockResponse);
        assertTrue(stream.contains("'success':false"));
        assertTrue(servlet._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testDoGetUpdateConsumerIII() throws IOException {
        final String twitterId = "tID";
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/updateConsumer";
            }
            @Override
            public String getParameter(String key) {
                if (Consumer.TWITTER_ID.equals(key)) { return twitterId; }
                fail("Unexpected parameter gathering for: " + key);
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
        final ConsumerOperations mockConsumerOperations = new ConsumerOperations() {
            @Override
            public List<Consumer> getConsumers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Consumer.TWITTER_ID, key);
                assertEquals(twitterId, (String) value);
                return new ArrayList<Consumer>();
            }
            @Override
            public Consumer updateConsumer(PersistenceManager pm, Consumer consumer) {
                fail("Not update expected");
                return consumer;
            }
        };

        servlet.consumerOperations = mockConsumerOperations;
        servlet.doGet(mockRequest, mockResponse);
        assertTrue(stream.contains("'success':false"));
        assertTrue(servlet._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessDemandForRobot() throws Exception {
        final Long demandKey = 111L;;
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/processDemandForRobot";
            }
            @Override
            public String getParameter(String key) {
                if (Demand.KEY.equals(key)) { return demandKey.toString(); }
                fail("Unexpected parameter gathering for: " + key);
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

        MockAppEngineEnvironment appEnv = new MockAppEngineEnvironment();
        appEnv.setUp(); // In that configuration, no Robot sale associate account will be found, so the request will do nothing
        servlet.doGet(mockRequest, mockResponse);
        appEnv.tearDown();

        assertTrue(stream.contains("'success':true"));
    }
}
