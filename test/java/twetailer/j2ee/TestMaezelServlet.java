package twetailer.j2ee;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.DataSourceException;
import twetailer.connector.MockTwitterConnector;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.MockPersistenceManager;
import twetailer.dao.RawCommandOperations;
import twetailer.dao.RetailerOperations;
import twetailer.dao.SettingsOperations;
import twetailer.dao.StoreOperations;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.RawCommand;
import twetailer.dto.Retailer;
import twetailer.dto.Settings;
import twetailer.dto.Store;
import twetailer.task.MockCommandProcessor;
import twetailer.task.MockDemandProcessor;
import twetailer.task.MockDemandValidator;
import twetailer.task.MockRobotResponder;
import twetailer.task.MockTweetLoader;
import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.Twitter;
import domderrien.mocks.MockHttpServletRequest;
import domderrien.mocks.MockHttpServletResponse;
import domderrien.mocks.MockServletOutputStream;

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
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("true"));
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
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("true"));
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
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("true"));
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
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("true"));

        // Clean-up
        MockCommandProcessor.restoreOperations();
        MockTwitterConnector.restoreTwitterConnector(mockTwitterAccount, null);
    }

    @Test
    public void testDoGetProcessCommands() throws IOException {
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
                return "/processCommands";
            }
            @Override
            public String getParameter(String name) {
                assertEquals("commandId", name);
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
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("true"));

        // Clean-up
        MockCommandProcessor.restoreOperations();
    }

    @Test
    public void testDoGetValidateOpenDemands() throws IOException {
        // Inject DemandOperations mock
        final DemandOperations mockDemandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, String key, Object value, int limit) {
                return new ArrayList<Demand>();
            }
        };
        MockDemandValidator.injectMocks(servlet._baseOperations);
        MockDemandValidator.injectMocks(mockDemandOperations);

        // Prepare mock servlet parameters
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/validateOpenDemands";
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
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("true"));

        // Clean-up
        MockDemandValidator.restoreOperations();
    }

    @Test
    public void testDoGetValidatePublishedDemands() throws IOException {
        // Inject DemandOperations mock
        final DemandOperations mockDemandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, String key, Object value, int limit) {
                return new ArrayList<Demand>();
            }
        };
        MockDemandProcessor.injectMocks(servlet._baseOperations);
        MockDemandProcessor.injectMocks(mockDemandOperations);

        // Prepare mock servlet parameters
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/processPubDemands";
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
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("true"));

        // Clean-up
        MockDemandProcessor.restoreOperation();
    }

    @Test
    @SuppressWarnings("serial")
    public void testDoGetProcessRobotDMs() throws IOException {
        // Inject a mock Twitter account
        final Twitter mockRobotAccount = new Twitter() {
            @Override
            public List<DirectMessage> getDirectMessages(Paging paging) {
                return null;
            }
        };
        MockTwitterConnector.injectMockRobotAccount(mockRobotAccount);

        // Inject the mock BaseOperations instance
        MockRobotResponder.injectMocks(servlet._baseOperations);
        MockRobotResponder.injectMocks(servlet._baseOperations.getSettingsOperations());

        // Prepare mock servlet parameters
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/processRobotMessages";
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
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("true"));

        // Clean-up
        MockRobotResponder.restoreOperations();
        MockTwitterConnector.restoreTwitterConnector(null, mockRobotAccount);
    }

    @Test
    public void testDoGetProcessProposals() throws IOException {
        /* FIXME: Wait for logic implementation
        // Inject ProposalOperations mock
        final ProposalOperations mockProposalOperations = new ProposalOperations() {
            @Override
            public List<Proposal> getProposals(PersistenceManager pm, String key, Object value, int limit) {
                return new ArrayList<Proposal>();
            }
        };

        // Inject the mock BaseOperations instance
        MockProposalProcessor.injectMocks(servlet._baseOperations);
        MockProposalProcessor.injectMocks(mockProposalOperations);
        */

        // Prepare mock servlet parameters
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/processProposals";
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
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("false"));

        // Clean-up
        /* FIXME: Wait for logic implementation
        MockProposalProcessor.restoreOperations();
         */
    }

    @Test
    public void testDoGetCreateLocationI() throws IOException {
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/createLocation";
            }
            @Override
            public String getParameter(String key) {
                if (Location.COUNTRY_CODE.equals(key)) {
                    return "CA";
                }
                if (Location.POSTAL_CODE.equals(key)) {
                    return "H0H0H0";
                }
                if (Location.LATITUDE.equals(key)) {
                    return null;
                }
                if (Location.LONGITUDE.equals(key)) {
                    return null;
                }
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
            public Location createLocation(Location location) {
                return location;
            }
        };

        servlet.locationOperations = mockLocationOperations;
        servlet.doGet(mockRequest, mockResponse);
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("true"));
    }

    @Test
    public void testDoGetCreateLocationII() throws IOException {
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/createLocation";
            }
            @Override
            public String getParameter(String key) {
                if (Location.COUNTRY_CODE.equals(key)) {
                    return "CA";
                }
                if (Location.POSTAL_CODE.equals(key)) {
                    return "H0H0H0";
                }
                if (Location.LATITUDE.equals(key)) {
                    return "0.0";
                }
                if (Location.LONGITUDE.equals(key)) {
                    return "0.0";
                }
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
            public Location createLocation(Location location) {
                return location;
            }
        };

        servlet.locationOperations = mockLocationOperations;
        servlet.doGet(mockRequest, mockResponse);
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("true"));
    }

    @Test
    public void testDoGetCreateStore() throws IOException {
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/createStore";
            }
            @Override
            public String getParameter(String key) {
                if (Store.LOCATION_KEY.equals(key)) {
                    return "12345";
                }
                if (Store.ADDRESS.equals(key)) {
                    return "address";
                }
                if (Store.NAME.equals(key)) {
                    return "name";
                }
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
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("true"));
        assertTrue(servlet._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testDoGetCreateRetailer() throws IOException {
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/createRetailer";
            }
            @Override
            public String getParameter(String key) {
                if (Retailer.CONSUMER_KEY.equals(key)) {
                    return "12345";
                }
                if (Retailer.STORE_KEY.equals(key)) {
                    return "67890";
                }
                if ("supplies".equals(key)) {
                    return "one two three";
                }
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
        final RetailerOperations mockRetailerOperations = new RetailerOperations() {
            @Override
            public Retailer createRetailer(PersistenceManager pm, Consumer consumer, Long storeKey) {
                assertEquals(Long.valueOf(67890L), storeKey);
                Retailer retailer = new Retailer();
                retailer.setKey(12345L);
                return retailer;
            }
            @Override
            public Retailer getRetailer(PersistenceManager pm, Long key) {
                assertEquals(Long.valueOf(12345L), key);
                return new Retailer();
            }
            @Override
            public Retailer updateRetailer(PersistenceManager pm, Retailer retailer) {
                return retailer;
            }
        };

        servlet.consumerOperations = mockConsumerOperations;
        servlet.retailerOperations = mockRetailerOperations;
        servlet.doGet(mockRequest, mockResponse);
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("true"));
        assertTrue(servlet._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testDoGetCreateDemand() throws IOException {
        HttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/createDemand";
            }
            @Override
            public String getParameter(String key) {
                if (Location.POSTAL_CODE.equals(key)) {
                    return "H0H0H0";
                }
                if (Demand.CONSUMER_KEY.equals(key)) {
                    return "12345";
                }
                if ("tags".equals(key)) {
                    return "one two three";
                }
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
            public List<Location> getLocations(PersistenceManager pm, String key, Object value, int limit) {
                Location location = new Location();
                List<Location> locations = new ArrayList<Location>();
                locations.add(location);
                return locations;
            }
        };
        final DemandOperations mockDemandOperations = new DemandOperations() {
            @Override
            public Demand createDemand(PersistenceManager pm, Demand demand) {
                return demand;
            }
        };

        servlet.locationOperations = mockLocationOperations;
        servlet.demandOperations = mockDemandOperations;
        servlet.doGet(mockRequest, mockResponse);
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("true"));
        assertTrue(servlet._baseOperations.getPersistenceManager().isClosed());
    }
}
