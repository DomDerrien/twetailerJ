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

import javamocks.util.logging.MockLogger;

import javax.jdo.MockQuery;
import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.DataSourceException;
import twetailer.j2ee.MockLoginServlet;
import twetailer.task.step.BaseSteps;

import com.dyuproject.openid.OpenIdUser;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class TestBaseOperations {

    static final OpenIdUser user = MockLoginServlet.buildMockOpenIdUser();

    private static LocalServiceTestHelper  helper;

    @BeforeClass
    public static void setUpBeforeClass() {
        BaseOperations.setLogger(new MockLogger("test", null));
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());;
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        BaseSteps.resetOperationControllers(true);
        helper.setUp();
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();
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
    @SuppressWarnings("serial")
    public void testPrepareQueryWithParameterEqualsValue() throws DataSourceException {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("key", 111L);
        parameters.put("=name", "test");

        MockQuery query = new MockQuery() {
            @Override
            public void setFilter(String arg) {
                assertNotNull(arg);
                assertNotSame(0, arg.length());
                assertTrue(arg.contains("name == nameValue"));
            }
        };

        BaseOperations.prepareQuery(query, parameters, 12345);
    }

    @Test
    @SuppressWarnings("serial")
    public void testPrepareQueryWithParameterNotEqualsValue() throws DataSourceException {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("key", 111L);
        parameters.put("!name", "test");

        MockQuery query = new MockQuery() {
            @Override
            public void setFilter(String arg) {
                assertNotNull(arg);
                assertNotSame(0, arg.length());
                assertTrue(arg.contains("name != nameValue"));
            }
        };

        BaseOperations.prepareQuery(query, parameters, 12345);
    }

    @Test
    @SuppressWarnings("serial")
    public void testPrepareQueryWithParameterLessThanValue() throws DataSourceException {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("key", 111L);
        parameters.put("<name", "test");

        MockQuery query = new MockQuery() {
            @Override
            public void setFilter(String arg) {
                assertNotNull(arg);
                assertNotSame(0, arg.length());
                assertTrue(arg.contains("name < nameValue"));
            }
        };

        BaseOperations.prepareQuery(query, parameters, 12345);
    }

    @Test
    @SuppressWarnings("serial")
    public void testPrepareQueryWithParameterGreaterThanValue() throws DataSourceException {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("key", 111L);
        parameters.put(">name", "test");

        MockQuery query = new MockQuery() {
            @Override
            public void setFilter(String arg) {
                assertNotNull(arg);
                assertNotSame(0, arg.length());
                assertTrue(arg.contains("name > nameValue"));
            }
        };

        BaseOperations.prepareQuery(query, parameters, 12345);
    }

    @Test
    public void testPrepareQueryWithSafeDoubleComparison() throws DataSourceException {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("key", 111L);
        parameters.put(">first", "test");
        parameters.put("<first", "test");

        BaseOperations.prepareQuery(new MockQuery(), parameters, 12345);
    }

    @Test(expected=DataSourceException.class)
    public void testPrepareQueryWithUnafeDoubleComparison() throws DataSourceException {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("key", 111L);
        parameters.put(">first", "test");
        parameters.put("<second", "test");

        BaseOperations.prepareQuery(new MockQuery(), parameters, 12345);
    }

    @Test
    public void testGetQueue() {
        assertNotNull(new BaseOperations().getQueue());
    }
}
