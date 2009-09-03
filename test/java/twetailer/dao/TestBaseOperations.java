package twetailer.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.DataSourceException;
import twetailer.j2ee.MockUserService;
import twetailer.j2ee.ServletUtils;

import com.google.appengine.api.users.User;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class TestBaseOperations {

	static final User user = new User("test-email", "test-domain");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        ServletUtils.setUserService(new MockUserService(){
            @Override
            public User getCurrentUser() {
                return user;
            }
        });
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    private MockAppEngineEnvironment mockAppEngineEnvironment;
    
	@Before
	public void setUp() throws Exception {
        mockAppEngineEnvironment = new MockAppEngineEnvironment();
        
        BaseOperations.setPersistenceManagerFactory(mockAppEngineEnvironment.getPersistenceManagerFactory());
	}

	@After
	public void tearDown() throws Exception {
        mockAppEngineEnvironment.tearDown();
	}

    @Test
    public void testGetLogger() throws IOException {
        Logger log1 = new BaseOperations().getLogger();
        assertNotNull(log1);
        Logger log2 = new BaseOperations().getLogger();
        assertNotNull(log2);
        assertEquals(log1, log2);
    }

    @Test
    public void testGetPersistenceManagerFactory() throws IOException {
        BaseOperations.setPersistenceManagerFactory(null); // Reset it because the setUp() function has replaced it with a mock
        PersistenceManager pm = new BaseOperations().getPersistenceManager();
        assertNotNull(pm);
    }

    @Test
    public void testGetPersistenceManager() throws IOException {
        PersistenceManager pm1 = new BaseOperations().getPersistenceManager();
        assertNotNull(pm1);
        assertFalse(pm1.getCopyOnAttach());
        assertTrue(pm1.getDetachAllOnCommit());
        PersistenceManager pm2 = new BaseOperations().getPersistenceManager();
        assertNotNull(pm2);
        assertNotSame(pm1, pm2);
    }

    @Test
    @SuppressWarnings("serial")
    public void testPrepareQueryWithStringParameter() throws DataSourceException {
        String parameter = "";
        final String parameterName = "key";
        final String parameterType = parameter.getClass().getSimpleName();
        
        MockQuery query = new MockQuery() {
            String variableName;
            @Override
            public void setFilter(String arg) {
                assertNotNull(arg);
                assertNotSame(0, arg.length());
                assertTrue(arg.startsWith(parameterName));
                variableName = arg.substring(arg.indexOf("==") + "==".length()).trim();
                assertNotNull(variableName);
                assertNotSame(0, variableName.length());
            }
            @Override
            public void declareParameters(String arg) {
                assertNotNull(arg);
                assertNotSame(0, arg.length());
                assertFalse(arg.contains(","));
                assertNotNull(variableName);
                assertNotSame(0, variableName.length());
                assertNotSame(-1, arg.indexOf(parameterType));
                assertNotSame(-1, arg.indexOf(variableName));
                assertTrue(arg.indexOf(parameterType) < arg.indexOf(variableName));
            }
            @Override
            public void setRange(long start, long size) {
                fail("Range should stay unset to get the maximum values");
            }
        };
        
        BaseOperations.prepareQuery(query, parameterName, parameter, 0);
    }

    @Test
    @SuppressWarnings("serial")
    public void testPrepareQueryWithLongParameter() throws DataSourceException {
        Long parameter = 0L;
        final String parameterName = "key";
        final String parameterType = parameter.getClass().getSimpleName();
        
        MockQuery query = new MockQuery() {
            String variableName;
            @Override
            public void setFilter(String arg) {
                assertNotNull(arg);
                assertNotSame(0, arg.length());
                assertTrue(arg.startsWith(parameterName));
                variableName = arg.substring(arg.indexOf("==") + "==".length()).trim();
                assertNotNull(variableName);
                assertNotSame(0, variableName.length());
            }
            @Override
            public void declareParameters(String arg) {
                assertNotNull(arg);
                assertNotSame(0, arg.length());
                assertFalse(arg.contains(","));
                assertNotNull(variableName);
                assertNotSame(0, variableName.length());
                assertNotSame(-1, arg.indexOf(parameterType));
                assertNotSame(-1, arg.indexOf(variableName));
                assertTrue(arg.indexOf(parameterType) < arg.indexOf(variableName));
            }
            @Override
            public void setRange(long start, long size) {
                fail("Range should stay unset to get the maximum values");
            }
        };
        
        BaseOperations.prepareQuery(query, parameterName, parameter, 0);
    }

    @Test
    @SuppressWarnings("serial")
    public void testPrepareQueryWithIntegerParameter() throws DataSourceException {
        Integer parameter = 0;
        final String parameterName = "key";
        final String parameterType = Long.class.getSimpleName(); // Integer falls back on Long!
        
        MockQuery query = new MockQuery() {
            String variableName;
            @Override
            public void setFilter(String arg) {
                assertNotNull(arg);
                assertNotSame(0, arg.length());
                assertTrue(arg.startsWith(parameterName));
                variableName = arg.substring(arg.indexOf("==") + "==".length()).trim();
                assertNotNull(variableName);
                assertNotSame(0, variableName.length());
            }
            @Override
            public void declareParameters(String arg) {
                assertNotNull(arg);
                assertNotSame(0, arg.length());
                assertFalse(arg.contains(","));
                assertNotNull(variableName);
                assertNotSame(0, variableName.length());
                assertNotSame(-1, arg.indexOf(parameterType));
                assertNotSame(-1, arg.indexOf(variableName));
                assertTrue(arg.indexOf(parameterType) < arg.indexOf(variableName));
            }
            @Override
            public void setRange(long start, long size) {
                fail("Range should stay unset to get the maximum values");
            }
        };
        
        BaseOperations.prepareQuery(query, parameterName, parameter, 0);
    }

    @Test
    @SuppressWarnings("serial")
    public void testPrepareQueryWithDoubleParameter() throws DataSourceException {
        Double parameter = 3.14159D;
        final String parameterName = "key";
        final String parameterType = parameter.getClass().getSimpleName();
        
        MockQuery query = new MockQuery() {
            String variableName;
            @Override
            public void setFilter(String arg) {
                assertNotNull(arg);
                assertNotSame(0, arg.length());
                assertTrue(arg.startsWith(parameterName));
                variableName = arg.substring(arg.indexOf("==") + "==".length()).trim();
                assertNotNull(variableName);
                assertNotSame(0, variableName.length());
            }
            @Override
            public void declareParameters(String arg) {
                assertNotNull(arg);
                assertNotSame(0, arg.length());
                assertFalse(arg.contains(","));
                assertNotNull(variableName);
                assertNotSame(0, variableName.length());
                assertNotSame(-1, arg.indexOf(parameterType));
                assertNotSame(-1, arg.indexOf(variableName));
                assertTrue(arg.indexOf(parameterType) < arg.indexOf(variableName));
            }
            @Override
            public void setRange(long start, long size) {
                fail("Range should stay unset to get the maximum values");
            }
        };
        
        BaseOperations.prepareQuery(query, parameterName, parameter, 0);
    }

    @Test
    @SuppressWarnings("serial")
    public void testPrepareQueryWithFloatParameter() throws DataSourceException {
        Float parameter = 3.14159f;
        final String parameterName = "key";
        final String parameterType = Double.class.getSimpleName(); // Float falls back on Double!
        
        MockQuery query = new MockQuery() {
            String variableName;
            @Override
            public void setFilter(String arg) {
                assertNotNull(arg);
                assertNotSame(0, arg.length());
                assertTrue(arg.startsWith(parameterName));
                variableName = arg.substring(arg.indexOf("==") + "==".length()).trim();
                assertNotNull(variableName);
                assertNotSame(0, variableName.length());
            }
            @Override
            public void declareParameters(String arg) {
                assertNotNull(arg);
                assertNotSame(0, arg.length());
                assertFalse(arg.contains(","));
                assertNotNull(variableName);
                assertNotSame(0, variableName.length());
                assertNotSame(-1, arg.indexOf(parameterType));
                assertNotSame(-1, arg.indexOf(variableName));
                assertTrue(arg.indexOf(parameterType) < arg.indexOf(variableName));
            }
            @Override
            public void setRange(long start, long size) {
                fail("Range should stay unset to get the maximum values");
            }
        };
        
        BaseOperations.prepareQuery(query, parameterName, parameter, 0);
    }

    @Test
    @SuppressWarnings("serial")
    public void testPrepareQueryWithBooleanParameter() throws DataSourceException {
        Boolean parameter = Boolean.TRUE;
        final String parameterName = "key";
        final String parameterType = parameter.getClass().getSimpleName();
        
        MockQuery query = new MockQuery() {
            String variableName;
            @Override
            public void setFilter(String arg) {
                assertNotNull(arg);
                assertNotSame(0, arg.length());
                assertTrue(arg.startsWith(parameterName));
                variableName = arg.substring(arg.indexOf("==") + "==".length()).trim();
                assertNotNull(variableName);
                assertNotSame(0, variableName.length());
            }
            @Override
            public void declareParameters(String arg) {
                assertNotNull(arg);
                assertNotSame(0, arg.length());
                assertFalse(arg.contains(","));
                assertNotNull(variableName);
                assertNotSame(0, variableName.length());
                assertNotSame(-1, arg.indexOf(parameterType));
                assertNotSame(-1, arg.indexOf(variableName));
                assertTrue(arg.indexOf(parameterType) < arg.indexOf(variableName));
            }
            @Override
            public void setRange(long start, long size) {
                fail("Range should stay unset to get the maximum values");
            }
        };
        
        BaseOperations.prepareQuery(query, parameterName, parameter, 0);
    }

    @Test
    @SuppressWarnings("serial")
    public void testPrepareQueryWithDateParameter() throws DataSourceException {
        Date parameter = domderrien.i18n.DateUtils.getNowDate();
        final String parameterName = "key";
        final String parameterType = parameter.getClass().getSimpleName();
        
        MockQuery query = new MockQuery() {
            String variableName;
            @Override
            public void setFilter(String arg) {
                assertNotNull(arg);
                assertNotSame(0, arg.length());
                assertTrue(arg.startsWith(parameterName));
                variableName = arg.substring(arg.indexOf("==") + "==".length()).trim();
                assertNotNull(variableName);
                assertNotSame(0, variableName.length());
            }
            @Override
            public void declareParameters(String arg) {
                assertNotNull(arg);
                assertNotSame(0, arg.length());
                assertFalse(arg.contains(","));
                assertNotNull(variableName);
                assertNotSame(0, variableName.length());
                assertNotSame(-1, arg.indexOf(parameterType));
                assertNotSame(-1, arg.indexOf(variableName));
                assertTrue(arg.indexOf(parameterType) < arg.indexOf(variableName));
            }
            @Override
            public void setRange(long start, long size) {
                fail("Range should stay unset to get the maximum values");
            }
        };
        
        BaseOperations.prepareQuery(query, parameterName, parameter, 0);
    }

    @Test
    @SuppressWarnings("serial")
    public void testPrepareQueryWithRangeSet() throws DataSourceException {
        String parameter = "";
        final String parameterName = "key";
        
        MockQuery query = new MockQuery() {
            @Override
            public void setRange(long start, long size) {
                assertEquals(0, start);
                assertEquals(12345, size);
            }
        };
        
        BaseOperations.prepareQuery(query, parameterName, parameter, 12345);
    }

    @Test(expected=DataSourceException.class)
    public void testPrepareQueryWithNullValue() throws DataSourceException {
        JsonObject parameter = null;
        final String parameterName = "key";
        
        BaseOperations.prepareQuery(new MockQuery(), parameterName, parameter, 0);
    }

    @Test(expected=DataSourceException.class)
    public void testPrepareQueryWithUnsupportedParameter() throws DataSourceException {
        JsonObject parameter = new GenericJsonObject();
        final String parameterName = "key";
        
        BaseOperations.prepareQuery(new MockQuery(), parameterName, parameter, 0);
    }

    @Test
    @SuppressWarnings("serial")
    public void testPrepareQueryWithManyParameters() throws DataSourceException {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("key", 111L);
        parameters.put("name", "test");
        parameters.put("check", Boolean.FALSE);
        parameters.put("date", new Date());
        
        MockQuery query = new MockQuery() {
            @Override
            public void setFilter(String arg) {
                assertNotNull(arg);
                assertNotSame(0, arg.length());
                for (String name: parameters.keySet()) {
                    int index = arg.indexOf(name, 0);
                    assertNotSame(-1, index);
                    index = arg.indexOf("==", index + name.length());
                    assertNotSame(-1, index);
                    index = arg.indexOf(name + "Value", index + "==".length());
                    assertNotSame(-1, index);
                }
            }
            @Override
            public void declareParameters(String arg) {
                assertNotNull(arg);
                assertNotSame(0, arg.length());
                for (String name: parameters.keySet()) {
                    int index = arg.indexOf(parameters.get(name).getClass().getSimpleName(), 0);
                    assertNotSame(-1, index);
                    index = arg.indexOf(name + "Value", index + name.length());
                    assertNotSame(-1, index);
                }
            }
            @Override
            public void setRange(long start, long size) {
                fail("Range should stay unset to get the maximum values");
            }
        };
        
        BaseOperations.prepareQuery(query, parameters, 0);
    }

    @Test
    @SuppressWarnings("serial")
    public void testPrepareQueryWithManyParametersAndRangeSet() throws DataSourceException {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("key", 111L);
        parameters.put("name", "test");
        
        MockQuery query = new MockQuery() {
            @Override
            public void setRange(long start, long size) {
                assertEquals(0, start);
                assertEquals(12345, size);
            }
        };
        
        BaseOperations.prepareQuery(query, parameters, 12345);
    }

    @Test
    public void testGetConsumerOperations() {
        BaseOperations base = new BaseOperations();
        BaseOperations ops1 = base.getConsumerOperations();
        assertNotNull(ops1);
        BaseOperations ops2 = base.getConsumerOperations();
        assertNotNull(ops2);
        assertEquals(ops1, ops2);
        BaseOperations ops3 = new BaseOperations().getConsumerOperations();
        assertNotNull(ops3);
        assertNotSame(ops1, ops3);
    }

    @Test
    public void testGetDemandOperations() {
        BaseOperations base = new BaseOperations();
        BaseOperations ops1 = base.getDemandOperations();
        assertNotNull(ops1);
        BaseOperations ops2 = base.getDemandOperations();
        assertNotNull(ops2);
        assertEquals(ops1, ops2);
        BaseOperations ops3 = new BaseOperations().getDemandOperations();
        assertNotNull(ops3);
        assertNotSame(ops1, ops3);
    }

    @Test
    public void testGetLocationOperations() {
        BaseOperations base = new BaseOperations();
        BaseOperations ops1 = base.getLocationOperations();
        assertNotNull(ops1);
        BaseOperations ops2 = base.getLocationOperations();
        assertNotNull(ops2);
        assertEquals(ops1, ops2);
        BaseOperations ops3 = new BaseOperations().getLocationOperations();
        assertNotNull(ops3);
        assertNotSame(ops1, ops3);
    }

    @Test
    public void testGetProductOperations() {
        BaseOperations base = new BaseOperations();
        BaseOperations ops1 = base.getProductOperations();
        assertNotNull(ops1);
        BaseOperations ops2 = base.getProductOperations();
        assertNotNull(ops2);
        assertEquals(ops1, ops2);
        BaseOperations ops3 = new BaseOperations().getProductOperations();
        assertNotNull(ops3);
        assertNotSame(ops1, ops3);
    }

    @Test
    public void testGetProposalOperations() {
        BaseOperations base = new BaseOperations();
        BaseOperations ops1 = base.getProposalOperations();
        assertNotNull(ops1);
        BaseOperations ops2 = base.getProposalOperations();
        assertNotNull(ops2);
        assertEquals(ops1, ops2);
        BaseOperations ops3 = new BaseOperations().getProposalOperations();
        assertNotNull(ops3);
        assertNotSame(ops1, ops3);
    }

    @Test
    public void testGetRetailerOperations() {
        BaseOperations base = new BaseOperations();
        BaseOperations ops1 = base.getRetailerOperations();
        assertNotNull(ops1);
        BaseOperations ops2 = base.getRetailerOperations();
        assertNotNull(ops2);
        assertEquals(ops1, ops2);
        BaseOperations ops3 = new BaseOperations().getRetailerOperations();
        assertNotNull(ops3);
        assertNotSame(ops1, ops3);
    }

    @Test
    public void testGetSettingsOperations() {
        BaseOperations base = new BaseOperations();
        BaseOperations ops1 = base.getSettingsOperations();
        assertNotNull(ops1);
        BaseOperations ops2 = base.getSettingsOperations();
        assertNotNull(ops2);
        assertEquals(ops1, ops2);
        BaseOperations ops3 = new BaseOperations().getSettingsOperations();
        assertNotNull(ops3);
        assertNotSame(ops1, ops3);
    }

    @Test
    public void testGetStoreOperations() {
        BaseOperations base = new BaseOperations();
        BaseOperations ops1 = base.getStoreOperations();
        assertNotNull(ops1);
        BaseOperations ops2 = base.getStoreOperations();
        assertNotNull(ops2);
        assertEquals(ops1, ops2);
        BaseOperations ops3 = new BaseOperations().getStoreOperations();
        assertNotNull(ops3);
        assertNotSame(ops1, ops3);
    }
}