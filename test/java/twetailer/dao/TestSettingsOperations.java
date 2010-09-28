package twetailer.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import static org.junit.Assert.assertNull;

import javax.cache.Cache;
import javax.cache.CacheException;
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
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();
    }

    @Test
    public void testGetSettingsI() throws DataSourceException {
        SettingsOperations ops = new SettingsOperations() {
            @Override
            public Settings getSettingsFromCache() {
                return null;
            }
        };
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

        ops.getSettings(false);
    }

    @Test
    public void testGetSettingsFromCacheI() throws DataSourceException {
        SettingsOperations ops = new SettingsOperations();
        Settings settings = ops.getSettingsFromCache();
        assertNull(settings);
    }

    @Test
    public void testGetSettingsFromCacheII() throws DataSourceException {
        SettingsOperations ops = new SettingsOperations() {
            @Override
            protected Cache getCache() throws CacheException {
                throw new CacheException("done in purpose");
            }
        };
        Settings settings = ops.getSettingsFromCache();
        assertNull(settings);
    }

    @Test
    public void testSetInCacheI() throws DataSourceException {
        Long data = 12345L;
        new SettingsOperations().setInCache("test", data);
        assertEquals(data, new SettingsOperations().getFromCache("test"));
    }

    @Test
    public void testSetInCacheII() throws DataSourceException {
        Long data = 12345L;
        new SettingsOperations() {
            @Override
            protected Cache getCache() throws CacheException {
                throw new CacheException("done in purpose");
            }
        }.setInCache("test", data);
        assertNull(new SettingsOperations().getFromCache("test"));
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
        Settings updated = ops.getSettings(false);
        assertEquals(Long.valueOf(111L), updated.getLastProcessDirectMessageId());
    }

    @Test(expected=RuntimeException.class)
    public void testUpdateSettingsII() throws ClientException, DataSourceException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        SettingsOperations ops = new SettingsOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
            @Override
            public Settings updateSettings(PersistenceManager pm, Settings settings) {
                throw new RuntimeException("Done in purpose");
            }
        };

        ops.updateSettings(new Settings());
    }

    @Test
    public void testUpdateSettingsInCacheI() throws DataSourceException {
        Settings settings = new Settings();
        settings.setRobotConsumerKey(12345L);
        SettingsOperations ops = new SettingsOperations();
        Settings settingsAfterUpdate = ops.updateSettingsInCache(settings).getSettingsFromCache();
        assertEquals(settings.getRobotConsumerKey(), settingsAfterUpdate.getRobotConsumerKey());
    }

    @Test
    public void testUpdateSettingsInCacheII() throws DataSourceException {
        Settings settings = new Settings();
        SettingsOperations ops = new SettingsOperations() {
            @Override
            protected Cache getCache() throws CacheException {
                throw new CacheException("done in purpose");
            }
        };
        ops.updateSettingsInCache(settings); // No side effect
    }

    @Test
    public void testCreateManySettingsI() throws DataSourceException {
        // Retrieve default settings and update one field
        SettingsOperations ops = new SettingsOperations();
        PersistenceManager pm = ops.getPersistenceManager();
        try {
            Settings settings = new Settings();
            settings.setLastProcessDirectMessageId(111L);
            ops.updateSettings(pm, settings);

            settings = new Settings();
            settings.setLastProcessDirectMessageId(222L);
            ops.updateSettings(pm, settings); // Save but does not replace the first one

            settings = new Settings();
            settings.setLastProcessDirectMessageId(333L);
            ops.updateSettings(pm, settings); // Save but does not replace the first one
        }
        finally {
            pm.close();
        }

        // Verify the default settings has persisted
        Settings updated = ops.getSettings(false);
        assertEquals(Long.valueOf(111L), updated.getLastProcessDirectMessageId());
    }

    @Test
    public void testCreateManySettingsII() throws DataSourceException {
        // Retrieve default settings and update one field
        SettingsOperations ops = new SettingsOperations();

        Settings settings = new Settings();
        settings.setLastProcessDirectMessageId(111L);
        ops.updateSettings(settings);

        settings = new Settings();
        settings.setLastProcessDirectMessageId(222L);
        ops.updateSettings(settings); // The first one is reloaded, updated, and persisted

        settings = new Settings();
        settings.setLastProcessDirectMessageId(333L);
        ops.updateSettings(settings); // The first one is reloaded, updated, and persisted

        // Verify the default settings has persisted
        Settings updated = ops.getSettings(false);
        assertEquals(Long.valueOf(333L), updated.getLastProcessDirectMessageId());
    }

    @Test
    public void testGetSettingFromCache() throws DataSourceException {
        SettingsOperations ops = new SettingsOperations();

        ops.getSettings(); // No data in cache

        Settings settings = new Settings();
        settings.setLastProcessDirectMessageId(111L);
        ops.updateSettings(settings);

        ops.getSettings(); // Data loaded from the cache
    }

    @Test
    public void testGetSettingsIII() throws DataSourceException {
        SettingsOperations ops = new SettingsOperations() {
            @Override
            public Settings getSettingsFromCache() {
                Settings settings = new Settings();
                settings.setLastProcessDirectMessageId(12345L);
                return settings;
            }
        };
        Settings settings = ops.getSettings(new MockPersistenceManager(), true);
        assertEquals(Long.valueOf(12345L), settings.getLastProcessDirectMessageId());
    }

    @Test
    public void testGetSettingsIV() throws ClientException, DataSourceException {
        SettingsOperations ops = new SettingsOperations() {
            @Override
            public Settings getSettingsFromCache() {
                return null;
            }
            @Override
            public Settings getSettings(PersistenceManager pm) {
                return new Settings();
            }
        };

        ops.getSettings(new MockPersistenceManager(), true);
    }

    @Test
    public void testGetSettingsV() throws ClientException, DataSourceException {
        SettingsOperations ops = new SettingsOperations() {
            @Override
            public Settings getSettingsFromCache() {
                return new Settings();
            }
            @Override
            public Settings getSettings(PersistenceManager pm) {
                fail("Call not expected!");
                return null;
            }
        };

        ops.getSettings(new MockPersistenceManager(), true);
    }

    @Test
    public void testGetSettingsVI() throws ClientException, DataSourceException {
        SettingsOperations ops = new SettingsOperations() {
            @Override
            public Settings getSettingsFromCache() {
                fail("Call not expected!");
                return null;
            }
            @Override
            public Settings getSettings(PersistenceManager pm) {
                return new Settings();
            }
        };

        ops.getSettings(new MockPersistenceManager(), false);
    }
}
