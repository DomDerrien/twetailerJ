package twetailer.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Map;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.MockCache;
import javax.cache.MockCacheFactory;
import javax.jdo.MockPersistenceManager;
import javax.jdo.MockPersistenceManagerFactory;
import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dto.Settings;
import twetailer.task.step.BaseSteps;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class TestSettingsOperations {

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
    public void testGetSettingsI() throws DataSourceException {
        CacheHandler.injectMockCache(new MockCache(Collections.emptyMap()) {
            @Override
            public Object get(Object key) {
                return null;
            }
        });
        SettingsOperations ops = new SettingsOperations();
        Settings settings = ops.getSettings();
        assertEquals(Long.valueOf(1L), settings.getLastProcessDirectMessageId());
    }

    @Test(expected=RuntimeException.class)
    public void testGetSettingsII() throws ClientException, DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        SettingsOperations ops = new SettingsOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
            @Override
            public Settings getSettings(PersistenceManager pm) {
                throw new RuntimeException("Done in purpose");
            }
        };

        ops.getSettings();
        assertTrue(pm.isClosed());
    }

    @Test
    public void testGetSettingsIII() throws DataSourceException {
        SettingsOperations ops = new SettingsOperations();

        Settings settings = ops.getSettings();
        assertEquals(Long.valueOf(1L), settings.getLastProcessDirectMessageId());

        settings = ops.getSettings();
        assertEquals(Long.valueOf(1L), settings.getLastProcessDirectMessageId());

        CacheHandler.resetInCache(Settings.class.getName() + "_name_" + Settings.APPLICATION_SETTINGS_ID);

        settings = ops.getSettings();
        assertEquals(Long.valueOf(1L), settings.getLastProcessDirectMessageId());

        CacheHandler.clearCache();

        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        settings = ops.getSettings(pm, false); // Don't use cache
        assertEquals(Long.valueOf(1L), settings.getLastProcessDirectMessageId());
        pm.close();
    }

    @Test
    public void testSetInCacheI() throws DataSourceException {
        Long data = 12345L;
        CacheHandler.setInCache("test", data);
        assertEquals(data, CacheHandler.getFromCache("test"));
    }

    @Test
    public void testSetInCacheII() throws DataSourceException {
        Long data = 12345L;
        CacheHandler.injectMockCache(null);
        CacheHandler.injectMockCacheFactory(new MockCacheFactory() {
            @Override
            @SuppressWarnings("unchecked")
            public Cache createCache(Map arg0) throws CacheException {
                throw new CacheException("done in purpose");
            }
        });
        CacheHandler.setInCache("test", data);
        assertNull(CacheHandler.getFromCache("test"));
    }

    @Test
    public void testUpdateSettingsI() throws DataSourceException {
        // Retrieve default settings and update one field
        SettingsOperations ops = new SettingsOperations();
        PersistenceManager pm = ops.getPersistenceManager();
        try {
            Settings settings = ops.getSettings(pm);
            settings.setLastProcessDirectMessageId(111L);
            ops.updateSettings(pm, settings);
        }
        finally {
            pm.close();
        }

        // Verify the default settings has persisted
        pm = ops.getPersistenceManager();
        try {
            Settings updated = ops.getSettings(pm, false);
            assertEquals(Long.valueOf(111L), updated.getLastProcessDirectMessageId());
        }
        finally {
            pm.close();
        }
    }

    @Test
    public void testUpdateSettingsII() throws DataSourceException {
        // Retrieve default settings and update one field
        SettingsOperations ops = new SettingsOperations();
        Settings settings = ops.getSettings();
        ops.updateSettings(ops.getSettings()); // Second call will get them from cache
    }
}
