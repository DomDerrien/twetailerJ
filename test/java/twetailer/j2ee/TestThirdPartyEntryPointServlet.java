package twetailer.j2ee;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javamocks.util.logging.MockLogger;

import javax.jdo.PersistenceManager;
import javax.mail.internet.InternetAddress;
import javax.servlet.MockServletInputStream;
import javax.servlet.MockServletOutputStream;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.MockHttpServletRequest;
import javax.servlet.http.MockHttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.ReservedOperationException;
import twetailer.connector.JabberConnector;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.InfluencerOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.MockBaseOperations;
import twetailer.dao.StoreOperations;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Influencer;
import twetailer.dto.Location;
import twetailer.dto.Store;
import twetailer.task.step.BaseSteps;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class TestThirdPartyEntryPointServlet {

    private static LocalServiceTestHelper  helper;

    @BeforeClass
    public static void setUpBeforeClass() {
        ThirdPartyEntryPointServlet.setMockLogger(new MockLogger("test", null));
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    }

    @Before
    public void setUp() throws Exception {
        BaseSteps.resetOperationControllers(true);
        BaseSteps.setMockBaseOperations(new MockBaseOperations());
        helper.setUp();
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();
        JabberConnector.injectMockXMPPService(null);
    }

    @Test
    public void testConstructor() {
        new ThirdPartyEntryPointServlet();
    }

    @Test
    public void testDoGetI() throws IOException {
        //
        // Get Location information
        //
        final String[] keys = new String[] { Influencer.REFERRAL_ID };
        final String[][] values = new String[][] { new String[] { "0" } };

        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            private int valueIdx = 0;
            @Override
            public String getPathInfo() {
                return ThirdPartyEntryPointServlet.LOCATION_PREFIX;
            }
            @Override
            public Enumeration<String> getParameterNames() {
                valueIdx = 0;
                return new Enumeration<String>() {
                    private int keyIdx = 0;
                    @Override public boolean hasMoreElements() { return keyIdx < keys.length;}
                    @Override public String nextElement() { keyIdx ++; return keys[keyIdx - 1]; }
                };
            }
            @Override
            public String[] getParameterValues(String key) {
                valueIdx ++;
                return values[valueIdx - 1];
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        BaseSteps.setMockInfluencerOperations(new InfluencerOperations()); // for the verifyReferralIdValidity() method

        new ThirdPartyEntryPointServlet().doGet(mockRequest, mockResponse);

        assertEquals(200, mockResponse.getStatus());
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("true"));
    }

    @Test
    public void testDoGetII() throws IOException {
        //
        // Get Stores information around a Location
        //
        final String[] keys = new String[] { "locationKey", "range", "rangeUnit", Influencer.REFERRAL_ID };
        final String[][] values = new String[][] { new String[] { "12345" }, new String[] { "2.25" }, new String[] { "km" }, new String[] { "0" } };

        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            private int valueIdx = 0;
            @Override
            public String getPathInfo() {
                return ThirdPartyEntryPointServlet.STORE_PREFIX;
            }
            @Override
            public Enumeration<String> getParameterNames() {
                valueIdx = 0;
                return new Enumeration<String>() {
                    private int keyIdx = 0;
                    @Override public boolean hasMoreElements() { return keyIdx < keys.length;}
                    @Override public String nextElement() { keyIdx ++; return keys[keyIdx - 1]; }
                };
            }
            @Override
            public String[] getParameterValues(String key) {
                valueIdx ++;
                return values[valueIdx - 1];
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                return new Location();
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location center, Double range, String rangeUnit, boolean hasStore, int maximumResults) {
                return new ArrayList<Location>();
            }
        });

        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, Map<String, Object> parameters, List<Location> places, int maximumResults) {
                List<Store> stores = new ArrayList<Store>();
                stores.add(new Store());
                return stores;
            }
        });

        BaseSteps.setMockInfluencerOperations(new InfluencerOperations()); // for the verifyReferralIdValidity() method

        new ThirdPartyEntryPointServlet().doGet(mockRequest, mockResponse);

        assertEquals(200, mockResponse.getStatus());
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("true"));
    }

    @Test
    public void testDoGetIII() throws IOException {
        //
        // Try to post Demand information without 'callback' parameter
        //
        final String[] keys = new String[] { Influencer.REFERRAL_ID };
        final String[][] values = new String[][] { new String[] { "0" } };

        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            private int valueIdx = 0;
            @Override
            public String getPathInfo() {
                return ThirdPartyEntryPointServlet.DEMAND_PREFIX;
            }
            @Override
            public Enumeration<String> getParameterNames() {
                valueIdx = 0;
                return new Enumeration<String>() {
                    private int keyIdx = 0;
                    @Override public boolean hasMoreElements() { return keyIdx < keys.length;}
                    @Override public String nextElement() { keyIdx ++; return keys[keyIdx - 1]; }
                };
            }
            @Override
            public String[] getParameterValues(String key) {
                valueIdx ++;
                return values[valueIdx - 1];
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        BaseSteps.setMockInfluencerOperations(new InfluencerOperations()); // for the verifyReferralIdValidity() method

        new ThirdPartyEntryPointServlet().doGet(mockRequest, mockResponse);

        assertEquals(500, mockResponse.getStatus());
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("false"));
    }

    @Test
    @SuppressWarnings("serial")
    public void testDoGetIV() throws IOException {
        //
        // Post Demand information
        //
        final String[] keys = new String[] { Influencer.REFERRAL_ID, "callback" };
        final String[][] values = new String[][] { new String[] { "0" }, new String[] { "callme" } };

        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            private int valueIdx = 0;
            @Override
            public String getPathInfo() {
                return ThirdPartyEntryPointServlet.DEMAND_PREFIX;
            }
            @Override
            public Enumeration<String> getParameterNames() {
                valueIdx = 0;
                return new Enumeration<String>() {
                    private int keyIdx = 0;
                    @Override public boolean hasMoreElements() { return keyIdx < keys.length;}
                    @Override public String nextElement() { keyIdx ++; return keys[keyIdx - 1]; }
                };
            }
            @Override
            public String[] getParameterValues(String key) {
                valueIdx ++;
                return values[valueIdx - 1];
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        BaseSteps.setMockInfluencerOperations(new InfluencerOperations()); // for the verifyReferralIdValidity() method

        new ThirdPartyEntryPointServlet() {
            @Override
            protected void createDemand(PersistenceManager pm, JsonObject in, JsonObject out) throws DataSourceException, ClientException {
                // Will be tested independently below
            }
        }.doGet(mockRequest, mockResponse);

        assertEquals(200, mockResponse.getStatus());
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("true"));
    }

    @Test
    public void testDoGetV() throws IOException {
        //
        // Unsupported call
        //
        final String[] keys = new String[] { Influencer.REFERRAL_ID };
        final String[][] values = new String[][] { new String[] { "0" } };

        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            private int valueIdx = 0;
            @Override
            public String getPathInfo() {
                return "/unsupported-path";
            }
            @Override
            public Enumeration<String> getParameterNames() {
                valueIdx = 0;
                return new Enumeration<String>() {
                    private int keyIdx = 0;
                    @Override public boolean hasMoreElements() { return keyIdx < keys.length;}
                    @Override public String nextElement() { keyIdx ++; return keys[keyIdx - 1]; }
                };
            }
            @Override
            public String[] getParameterValues(String key) {
                valueIdx ++;
                return values[valueIdx - 1];
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        new ThirdPartyEntryPointServlet().doGet(mockRequest, mockResponse);

        assertEquals(404, mockResponse.getStatus());
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("false"));
    }

    @Test
    public void testDoGetVI() throws IOException {
        //
        // Unspecified referral identifier
        //
        final String[] keys = new String[] { };
        final String[][] values = new String[][] { };

        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            private int valueIdx = 0;
            @Override
            public String getPathInfo() {
                return ThirdPartyEntryPointServlet.LOCATION_PREFIX;
            }
            @Override
            public Enumeration<String> getParameterNames() {
                valueIdx = 0;
                return new Enumeration<String>() {
                    private int keyIdx = 0;
                    @Override public boolean hasMoreElements() { return keyIdx < keys.length;}
                    @Override public String nextElement() { keyIdx ++; return keys[keyIdx - 1]; }
                };
            }
            @Override
            public String[] getParameterValues(String key) {
                valueIdx ++;
                return values[valueIdx - 1];
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        new ThirdPartyEntryPointServlet().doGet(mockRequest, mockResponse);

        assertEquals(403, mockResponse.getStatus());
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("false"));
    }

    @Test
    public void testDoPostI() throws IOException {
        //
        // Unsupported call
        //
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return "/unsupported-path";
            }
            @Override
            public ServletInputStream getInputStream() {
                return new MockServletInputStream("{}");
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        new ThirdPartyEntryPointServlet().doPost(mockRequest, mockResponse);

        assertEquals(404, mockResponse.getStatus());
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("false"));
    }

    @Test
    public void testDoPostII() throws IOException {
        //
        // Unspecified referral identifier
        //
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return ThirdPartyEntryPointServlet.DEMAND_PREFIX;
            }
            @Override
            public ServletInputStream getInputStream() {
                return new MockServletInputStream("{}");
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        new ThirdPartyEntryPointServlet().doPost(mockRequest, mockResponse);

        assertEquals(403, mockResponse.getStatus());
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("false"));
    }

    @Test
    public void testDoPostIII() throws IOException {
        //
        // Unspecified referral identifier
        //
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return ThirdPartyEntryPointServlet.DEMAND_PREFIX;
            }
            @Override
            public ServletInputStream getInputStream() {
                return new MockServletInputStream("{}");
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        new ThirdPartyEntryPointServlet().doPost(mockRequest, mockResponse);

        assertEquals(403, mockResponse.getStatus());
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("false"));
    }

    @Test
    public void testDoPostIV() throws IOException {
        //
        // Invalid JSON bag
        //
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return ThirdPartyEntryPointServlet.DEMAND_PREFIX;
            }
            @Override
            public ServletInputStream getInputStream() {
                return new MockServletInputStream("{'what?}");
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        new ThirdPartyEntryPointServlet().doPost(mockRequest, mockResponse);

        assertEquals(500, mockResponse.getStatus());
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("false"));
    }

    @Test
    @SuppressWarnings("serial")
    public void testDoPostV() throws IOException {
        //
        // Post Demand information
        //
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public String getPathInfo() {
                return ThirdPartyEntryPointServlet.DEMAND_PREFIX;
            }
            @Override
            public ServletInputStream getInputStream() {
                return new MockServletInputStream("{'" + Influencer.REFERRAL_ID + "':'0'}");
            }
        };
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        BaseSteps.setMockInfluencerOperations(new InfluencerOperations()); // for the verifyReferralIdValidity() method

        new ThirdPartyEntryPointServlet() {
            @Override
            protected void createDemand(PersistenceManager pm, JsonObject in, JsonObject out) throws DataSourceException, ClientException {
                // Will be tested independently below
            }
        }.doPost(mockRequest, mockResponse);

        assertEquals(200, mockResponse.getStatus());
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("true"));
    }

    @Test
    public void testDoPut() throws IOException {
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        new ThirdPartyEntryPointServlet().doPut(null, mockResponse);

        assertEquals(403, mockResponse.getStatus());
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("false"));
    }

    @Test
    public void testDoDelete() throws IOException {
        final MockServletOutputStream stream = new MockServletOutputStream();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public ServletOutputStream getOutputStream() {
                return stream;
            }
        };

        new ThirdPartyEntryPointServlet().doDelete(null, mockResponse);

        assertEquals(403, mockResponse.getStatus());
        assertTrue(stream.contains("success"));
        assertTrue(stream.contains("false"));
    }

    @Test
    public void testVerifiyReferralIdI() throws ReservedOperationException {
        final String referralId = "54765765-5465654-00";
        BaseSteps.setMockInfluencerOperations(new InfluencerOperations() {
            @Override
            public boolean verifyReferralIdValidity(PersistenceManager pm, String id) {
                assertEquals(referralId, id);
                return true;
            }
        });

        JsonObject params = new GenericJsonObject();
        params.put(Influencer.REFERRAL_ID, referralId);

        ThirdPartyEntryPointServlet.verifyReferralId(null, params, null, null);

        assertEquals(referralId, params.getString(Influencer.REFERRAL_ID));
    }

    @Test
    public void testVerifiyReferralIdII() throws ReservedOperationException {
        final String referralId = "54765765-5465654-00";
        BaseSteps.setMockInfluencerOperations(new InfluencerOperations() {
            @Override
            public boolean verifyReferralIdValidity(PersistenceManager pm, String id) {
                assertEquals(referralId, id);
                return false;
            }
        });

        JsonObject params = new GenericJsonObject();
        params.put(Influencer.REFERRAL_ID, referralId);

        ThirdPartyEntryPointServlet.verifyReferralId(null, params, null, null);

        assertEquals(Influencer.DEFAULT_REFERRAL_ID, params.getString(Influencer.REFERRAL_ID));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCreateDemandI() throws DataSourceException, ClientException {
        JsonObject in = new GenericJsonObject();
        JsonObject out = null;

        new ThirdPartyEntryPointServlet().createDemand(null, in, out);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCreateDemandII() throws DataSourceException, ClientException {
        JsonObject in = new GenericJsonObject();
        in.put(Consumer.EMAIL, "");
        JsonObject out = null;

        new ThirdPartyEntryPointServlet().createDemand(null, in, out);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCreateDemandIII() throws DataSourceException, ClientException {
        JsonObject in = new GenericJsonObject();
        in.put(Consumer.EMAIL, "aaa at aaa dot aaa");
        JsonObject out = null;

        new ThirdPartyEntryPointServlet().createDemand(null, in, out);
    }

    @Test
    public void testCreateDemandIV() throws DataSourceException, ClientException {
        JsonObject in = new GenericJsonObject();
        in.put(Consumer.EMAIL, "aaa@aaa.aaa");
        JsonObject out = new GenericJsonObject();

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer createConsumer(PersistenceManager pm, InternetAddress address, boolean isVerified) {
                Consumer object = new Consumer();
                object.setKey(453765232321L);
                object.setAutomaticLocaleUpdate(false);
                return object;
            }
        });

        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand createDemand(PersistenceManager pm, JsonObject in, Long consumerKey) {
                Demand object = new Demand();
                object.setKey(43245543L);
                return object;
            }
        });

        BaseSteps.setMockBaseOperations(new MockBaseOperations());

        new ThirdPartyEntryPointServlet().createDemand(null, in, out);

        assertTrue(out.containsKey("resource"));
    }

    @Test
    public void testCreateDemandVa() throws DataSourceException, ClientException {
        JsonObject in = new GenericJsonObject();
        in.put(Consumer.EMAIL, "aaa@aaa.aaa");
        // in.put(Consumer.LANGUAGE, "");
        JsonObject out = new GenericJsonObject();

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer createConsumer(PersistenceManager pm, InternetAddress address, boolean isVerified) {
                Consumer object = new Consumer();
                object.setKey(453765232321L);
                object.setAutomaticLocaleUpdate(true);
                return object;
            }
        });

        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand createDemand(PersistenceManager pm, JsonObject in, Long consumerKey) {
                Demand object = new Demand();
                object.setKey(43245543L);
                return object;
            }
        });

        BaseSteps.setMockBaseOperations(new MockBaseOperations());

        new ThirdPartyEntryPointServlet().createDemand(null, in, out);

        assertTrue(out.containsKey("resource"));
    }

    @Test
    public void testCreateDemandVb() throws DataSourceException, ClientException {
        JsonObject in = new GenericJsonObject();
        in.put(Consumer.EMAIL, "aaa@aaa.aaa");
        in.put(Consumer.LANGUAGE, "");
        JsonObject out = new GenericJsonObject();

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer createConsumer(PersistenceManager pm, InternetAddress address, boolean isVerified) {
                Consumer object = new Consumer();
                object.setKey(453765232321L);
                object.setAutomaticLocaleUpdate(true);
                return object;
            }
        });

        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand createDemand(PersistenceManager pm, JsonObject in, Long consumerKey) {
                Demand object = new Demand();
                object.setKey(43245543L);
                return object;
            }
        });

        BaseSteps.setMockBaseOperations(new MockBaseOperations());

        new ThirdPartyEntryPointServlet().createDemand(null, in, out);

        assertTrue(out.containsKey("resource"));
    }

    @Test
    public void testCreateDemandVc() throws DataSourceException, ClientException {
        JsonObject in = new GenericJsonObject();
        in.put(Consumer.EMAIL, "aaa@aaa.aaa");
        in.put(Consumer.LANGUAGE, "FR");
        JsonObject out = new GenericJsonObject();

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer createConsumer(PersistenceManager pm, InternetAddress address, boolean isVerified) {
                Consumer object = new Consumer();
                object.setKey(453765232321L);
                object.setAutomaticLocaleUpdate(true);
                return object;
            }
            @Override
            public Consumer updateConsumer(PersistenceManager pm, Consumer object) {
                assertEquals(Locale.CANADA_FRENCH.getLanguage(), object.getLanguage());
                return object;
            }
        });

        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand createDemand(PersistenceManager pm, JsonObject in, Long consumerKey) {
                Demand object = new Demand();
                object.setKey(43245543L);
                return object;
            }
        });

        BaseSteps.setMockBaseOperations(new MockBaseOperations());

        new ThirdPartyEntryPointServlet().createDemand(null, in, out);

        assertTrue(out.containsKey("resource"));
    }

    @Test
    public void testCreateDemandVd() throws DataSourceException, ClientException {
        JsonObject in = new GenericJsonObject();
        in.put(Consumer.EMAIL, "aaa@aaa.aaa");
        in.put(Consumer.LANGUAGE, "EN");
        JsonObject out = new GenericJsonObject();

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer createConsumer(PersistenceManager pm, InternetAddress address, boolean isVerified) {
                Consumer object = new Consumer();
                object.setKey(453765232321L);
                object.setAutomaticLocaleUpdate(true);
                return object;
            }
            @Override
            public Consumer updateConsumer(PersistenceManager pm, Consumer object) {
                fail("Unexpected call!");
                return object;
            }
        });

        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand createDemand(PersistenceManager pm, JsonObject in, Long consumerKey) {
                Demand object = new Demand();
                object.setKey(43245543L);
                return object;
            }
        });

        BaseSteps.setMockBaseOperations(new MockBaseOperations());

        new ThirdPartyEntryPointServlet().createDemand(null, in, out);

        assertTrue(out.containsKey("resource"));
    }
}
