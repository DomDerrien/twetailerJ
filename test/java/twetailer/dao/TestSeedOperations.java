package twetailer.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

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
import twetailer.InvalidIdentifierException;
import twetailer.dto.Consumer;
import twetailer.dto.Seed;
import twetailer.task.step.BaseSteps;

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class TestSeedOperations {

    private static LocalServiceTestHelper  helper;

    @BeforeClass
    public static void setUpBeforeClass() {
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());;
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
    public void testCreateSeedI() throws InvalidIdentifierException {
        // Verify there's no instance
        Query query = new Query(Seed.class.getSimpleName());
        assertEquals(0, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());

        final Long storeKey = 544376L;
        SeedOperations ops = new SeedOperations();
        assertNotNull(ops.createSeed(new Seed("country", "region", "city", "label", storeKey)).getKey());

        // Verify there's one instance
        assertEquals(1, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());
    }

    @Test
    public void testCreateSeedII() throws InvalidIdentifierException {
        final Long storeKey = 544376L;
        SeedOperations ops = new SeedOperations();
        Seed first = ops.createSeed(new Seed("country", "region", "city", "label", storeKey));

        // Verify there's one instance
        Query query = new Query(Seed.class.getSimpleName());
        assertEquals(1, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());

        Seed second = ops.createSeed(new Seed("country", "region", "city", "label", storeKey));

        // Verify there's still just one instance
        assertEquals(1, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());

        assertEquals(first.getKey(), second.getKey());
    }

    @Test
    public void testCreateSeedIII() throws InvalidIdentifierException {
        final Long storeKey = 544376L;
        SeedOperations ops = new SeedOperations();
        Seed first = ops.createSeed(new Seed("country", "region", "city", "label", storeKey));

        // Verify there's one instance
        Query query = new Query(Seed.class.getSimpleName());
        assertEquals(1, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());

        Seed second = ops.createSeed(first);

        // Verify there's still just one instance
        assertEquals(1, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());

        assertEquals(first.getKey(), second.getKey());
    }

    @Test(expected=InvalidIdentifierException.class)
    public void testGetSeedI() throws InvalidIdentifierException {
        new SeedOperations().getSeed("country", "region", "city");
    }

    @Test(expected=InvalidIdentifierException.class)
    public void testGetSeedII() throws InvalidIdentifierException {
        new SeedOperations().getSeed("seek_key");
    }

    @Test(expected=InvalidIdentifierException.class)
    public void testGetSeedIII() throws InvalidIdentifierException {
        new SeedOperations().getSeed(null);
    }

    @Test(expected=InvalidIdentifierException.class)
    public void testGetSeedIV() throws InvalidIdentifierException {
        new SeedOperations().getSeed("");
    }

    @Test
    public void testGetSeedV() throws InvalidIdentifierException {
        final Long storeKey = 544376L;
        SeedOperations ops = new SeedOperations();
        Seed first = ops.createSeed(new Seed("country", "region", "city", "label", storeKey));

        Seed second = ops.getSeed("country", "region", "city");

        assertEquals(first.getKey(), second.getKey());
    }

    @Test
    public void testGetSeedsI() throws InvalidIdentifierException {
        final Long storeKey = 544376L;
        SeedOperations ops = new SeedOperations();
        Seed first = ops.createSeed(new Seed("1", "1", "1", "1", storeKey));
        Seed second = ops.createSeed(new Seed("2", "2", "2", "2", storeKey));
        Seed third = ops.createSeed(new Seed("3", "3", "3", "3", storeKey));

        List<Seed> seeds = ops.getAllSeeds();
        assertEquals(3, seeds.size());
        boolean firstFound = false, secondFound = false, thirdFound = false;
        for (Seed seed: seeds) {
            firstFound = firstFound || seed.getKey().equals(first.getKey());
            secondFound = secondFound || seed.getKey().equals(second.getKey());
            thirdFound = thirdFound || seed.getKey().equals(third.getKey());
        }
        assertTrue(firstFound && secondFound && thirdFound);
    }

    @Test
    public void testUpdateSeedI() throws InvalidIdentifierException, DataSourceException {
        final Long storeKey = 544376L;
        SeedOperations ops = new SeedOperations();
        Seed first = ops.createSeed(new Seed("1", "1", "1", "1", storeKey));

        first.setLabel("new-1");
        first.setStoreKey(12345L);
        ops.updateSeed(first);

        Seed reload = ops.getAllSeeds().get(0);
        assertEquals("new-1", reload.getLabel());
        assertEquals(Long.valueOf(12345L), reload.getStoreKey());
    }
}
