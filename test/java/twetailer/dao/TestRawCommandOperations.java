package twetailer.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.cache.MockCacheFactory;
import javax.jdo.MockPersistenceManagerFactory;
import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.dto.Entity;
import twetailer.dto.RawCommand;
import twetailer.task.step.BaseSteps;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import domderrien.i18n.DateUtils;

public class TestRawCommandOperations {

    private static LocalServiceTestHelper  helper;

    @BeforeClass
    public static void setUpBeforeClass() {
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    }

    @Before
    public void setUp() throws Exception {
        helper.setUp();
        BaseSteps.resetOperationControllers(false); // Use helper!
        CacheHandler.injectMockCacheFactory(new MockCacheFactory());
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();
        CacheHandler.injectMockCacheFactory(null);
        CacheHandler.injectMockCache(null);
    }

    @Test
    public void testCreateI() {
        RawCommand item = new RawCommandOperations().createRawCommand(new RawCommand());
        assertNotNull(item.getKey());
    }

    @Test(expected=RuntimeException.class)
    public void testCreateII() throws ClientException, DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        RawCommandOperations ops = new RawCommandOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
            @Override
            public RawCommand createRawCommand(PersistenceManager pm, RawCommand rawCommand) {
                throw new RuntimeException("Done in purpose");
            }
        };

        ops.createRawCommand(new RawCommand());
    }

    @Test
    public void testGetI() throws InvalidIdentifierException {
        RawCommandOperations ops = new RawCommandOperations();
        RawCommand item = ops.createRawCommand(new RawCommand());

        RawCommand selected = ops.getRawCommand(item.getKey());
        assertNotNull(selected);
        assertEquals(item.getKey(), selected.getKey());
    }

    @Test(expected=InvalidIdentifierException.class)
    public void testGetII() throws InvalidIdentifierException {
        new RawCommandOperations().getRawCommand(null);
    }

    @Test(expected=InvalidIdentifierException.class)
    public void testGetIII() throws InvalidIdentifierException {
        new RawCommandOperations().getRawCommand(0L);
    }

    @Test(expected=InvalidIdentifierException.class)
    public void testGetIV() throws InvalidIdentifierException {
        new RawCommandOperations().getRawCommand(888L);
    }

    @Test(expected=RuntimeException.class)
    public void testUpdateWithFailureI() throws DataSourceException {
        RawCommandOperations ops = new RawCommandOperations() {
            @Override
            public RawCommand updateRawCommand(PersistenceManager pm, RawCommand item) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        };
        ops.updateRawCommand(new RawCommand());
    }

    @Test
    public void testUpdate() throws ClientException, DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        RawCommandOperations ops = new RawCommandOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        RawCommand object = new RawCommand();
        object.setEmitterId("emitter");
        object = ops.createRawCommand(pm, object); // Gives the PersistenceManager so it won't be closed
        object.setEmitterId("who?");
        object.setErrorMessage("error");

        RawCommand updated = ops.updateRawCommand(object);
        assertNotNull(updated);
        assertEquals(object.getKey(), updated.getKey());
        assertEquals("who?", object.getEmitterId());
        assertEquals("error", object.getErrorMessage());
        assertTrue(pm.isClosed());
    }

    @Test(expected=RuntimeException.class)
    public void testDeleteWithFailureI() throws InvalidIdentifierException {
        RawCommandOperations ops = new RawCommandOperations() {
            @Override
            public void deleteRawCommand(PersistenceManager pm, Long key) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        };
        ops.deleteRawCommand(12345L);
    }

    @Test
    public void testDeleteI() throws InvalidIdentifierException {
        final Long rawCommandKey = 54657L;
        RawCommandOperations ops = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                return rawCommand;
            }
            @Override
            public void deleteRawCommand(PersistenceManager pm, RawCommand item) {
                assertEquals(rawCommandKey, item.getKey());
            }
        };
        ops.deleteRawCommand(rawCommandKey);
    }

    @Test
    public void testDeleteII() throws InvalidIdentifierException {
        final String tag = "tag";
        RawCommand toBeCreated = new RawCommand();
        toBeCreated.setCommand(tag);
        RawCommandOperations ops = new RawCommandOperations();
        RawCommand justCreated = ops.createRawCommand(toBeCreated);
        assertNotNull(justCreated.getKey());
        assertEquals(tag, justCreated.getCommand());
        ops.deleteRawCommand(justCreated.getKey());
    }

    @Test
    public void testGetRawCommandKeys() throws DataSourceException, ParseException {
        RawCommandOperations ops = new RawCommandOperations();
        RawCommand item = new RawCommand();
        item.setCommandId("Zobi la mouche!");
        item = ops.createRawCommand(item);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(BaseOperations.FILTER_GREATER_THAN_OR_EQUAL_TO + Entity.CREATION_DATE, DateUtils.isoToDate("2000-01-01T00:00:00"));

        List<Long> selected = ops.getRawCommandKeys(parameters, 0);
        assertNotNull(selected);
        assertEquals(item.getKey(), selected.get(0));
    }
}
