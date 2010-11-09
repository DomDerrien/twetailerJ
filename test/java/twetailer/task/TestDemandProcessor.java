package twetailer.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javamocks.util.logging.MockLogger;

import javax.jdo.MockPersistenceManager;
import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.connector.BaseConnector;
import twetailer.connector.MailConnector;
import twetailer.connector.MockTwitterConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.InfluencerOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.MockBaseOperations;
import twetailer.dao.ProposalOperations;
import twetailer.dao.RawCommandOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dao.SettingsOperations;
import twetailer.dao.StoreOperations;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.Proposal;
import twetailer.dto.RawCommand;
import twetailer.dto.SaleAssociate;
import twetailer.dto.Settings;
import twetailer.dto.Store;
import twetailer.task.step.BaseSteps;
import twetailer.validator.LocaleValidator;
import twetailer.validator.CommandSettings.State;
import twitter4j.DirectMessage;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import com.google.appengine.api.labs.taskqueue.MockQueue;
import com.google.appengine.api.labs.taskqueue.TaskOptions;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class TestDemandProcessor {

    private static LocalServiceTestHelper  helper;

    @BeforeClass
    public static void setUpBeforeClass() {
        DemandProcessor.setLogger(new MockLogger("test", null));
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    }


    @Before
    public void setUp() throws Exception {
        helper.setUp();

        BaseSteps.resetOperationControllers(true);
        BaseSteps.setMockBaseOperations(new MockBaseOperations());
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();

        RobotResponder.setRobotSaleAssociateKey(null);

        BaseConnector.resetLastCommunicationInSimulatedMode();

        MockTwitterConnector.restoreTwitterConnector();
    }

    @Test
    public void testConstructor() {
        new DemandProcessor();
    }

    /***** ddd
     * @throws InvalidIdentifierException
    @Test(expected=DataSourceException.class)
    public void testProcessNoDemand() throws DataSourceException {
        final Long demandKey = 12345L;

        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(consumerKey);
                throw new DataSourceException("Done in purpose");
            }
        });

        DemandProcessor.process(demandKey, true);

        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testIdentifySaleAssociatesNoLocationAround() throws DataSourceException {
        final Long locationKey = 12345L;
        final Location consumerLocation = new Location();
        consumerLocation.setKey(locationKey);

        final Double demandRange = 25.75D;
        final Demand consumerDemand = new Demand();
        consumerDemand.setRange(demandRange);
        consumerDemand.setLocationKey(locationKey);

        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(locationKey, key);
                return consumerLocation;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) {
                assertEquals(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager(), pm);
                assertEquals(location, consumerLocation);
                assertEquals(demandRange, range);
                return new ArrayList<Location>();
            }
        });

        List<SaleAssociate> saleAssociates = DemandProcessor.identifySaleAssociates(BaseSteps.getBaseOperations().getPersistenceManager(), consumerDemand, new Consumer());
        assertNotNull(saleAssociates);
        assertEquals(0, saleAssociates.size());
    }

    @Test
    public void testIdentifySaleAssociatesNoStoreForLocationAround() throws DataSourceException {
        final Long locationKey = 12345L;
        final Location consumerLocation = new Location();
        consumerLocation.setKey(locationKey);

        final Double demandRange = 25.75D;
        final Demand consumerDemand = new Demand();
        consumerDemand.setRange(demandRange);
        consumerDemand.setLocationKey(locationKey);

        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(locationKey, key);
                return consumerLocation;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) {
                assertEquals(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager(), pm);
                assertEquals(location, consumerLocation);
                assertEquals(demandRange, range);
                List<Location> locations = new ArrayList<Location>();
                locations.add(consumerLocation);
                return locations;
            }
        });

        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, Map<String, Object> parameters, List<Location> locations, int limit) {
                assertEquals(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager(), pm);
                assertNotNull(locations);
                assertEquals(1, locations.size());
                assertEquals(consumerLocation, locations.get(0));
                return new ArrayList<Store>();
            }
        });

        List<SaleAssociate> saleAssociates = DemandProcessor.identifySaleAssociates(BaseSteps.getBaseOperations().getPersistenceManager(), consumerDemand, new Consumer());
        assertNotNull(saleAssociates);
        assertEquals(0, saleAssociates.size());
    }

    @Test
    public void testIdentifySaleAssociatesForAStoreWithoutEmployees() throws DataSourceException {
        final Long locationKey = 12345L;
        final Location consumerLocation = new Location();
        consumerLocation.setKey(locationKey);

        final Double demandRange = 25.75D;
        final Demand consumerDemand = new Demand();
        consumerDemand.setRange(demandRange);
        consumerDemand.setLocationKey(locationKey);

        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(locationKey, key);
                return consumerLocation;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) {
                assertEquals(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager(), pm);
                assertEquals(location, consumerLocation);
                assertEquals(demandRange, range);
                List<Location> locations = new ArrayList<Location>();
                locations.add(consumerLocation);
                return locations;
            }
        });

        final Long storeKey = 12345L;
        final Store targetedStore = new Store();
        targetedStore.setKey(storeKey);

        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, Map<String, Object> parameters, List<Location> locations, int limit) {
                assertEquals(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager(), pm);
                assertNotNull(locations);
                assertEquals(1, locations.size());
                assertEquals(consumerLocation, locations.get(0));
                List<Store> stores = new ArrayList<Store>();
                stores.add(targetedStore);
                return stores;
            }
        });

        DemandProcessor.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager(), pm);
                assertEquals(SaleAssociate.STORE_KEY, key);
                assertEquals(storeKey, (Long) value);
                return new ArrayList<SaleAssociate>();
            }
        };

        List<SaleAssociate> saleAssociates = DemandProcessor.identifySaleAssociates(BaseSteps.getBaseOperations().getPersistenceManager(), consumerDemand, new Consumer());
        assertNotNull(saleAssociates);
        assertEquals(0, saleAssociates.size());
    }

    @Test
    public void testIdentifySaleAssociatesWithEmployeeWithoutExpectedTagsI() throws DataSourceException {
        final Long locationKey = 12345L;
        final Location consumerLocation = new Location();
        consumerLocation.setKey(locationKey);

        final Double demandRange = 25.75D;
        final Demand consumerDemand = new Demand();
        consumerDemand.setRange(demandRange);
        consumerDemand.setLocationKey(locationKey);
        consumerDemand.addCriterion("test");

        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(locationKey, key);
                return consumerLocation;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) {
                assertEquals(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager(), pm);
                assertEquals(location, consumerLocation);
                assertEquals(demandRange, range);
                List<Location> locations = new ArrayList<Location>();
                locations.add(consumerLocation);
                return locations;
            }
        });

        final Long storeKey = 12345L;
        final Store targetedStore = new Store();
        targetedStore.setKey(storeKey);

        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, Map<String, Object> parameters, List<Location> locations, int limit) {
                assertEquals(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager(), pm);
                assertNotNull(locations);
                assertEquals(1, locations.size());
                assertEquals(consumerLocation, locations.get(0));
                List<Store> stores = new ArrayList<Store>();
                stores.add(targetedStore);
                return stores;
            }
        });

        final SaleAssociate selectedSaleAssociate = new SaleAssociate() {
            @Override
            public List<String> getCriteria() {
                return null;
            }
        };

        DemandProcessor.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager(), pm);
                assertEquals(SaleAssociate.STORE_KEY, key);
                assertEquals(storeKey, (Long) value);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                saleAssociates.add(selectedSaleAssociate);
                return saleAssociates;
            }
        };

        List<SaleAssociate> saleAssociates = DemandProcessor.identifySaleAssociates(BaseSteps.getBaseOperations().getPersistenceManager(), consumerDemand, new Consumer());
        assertNotNull(saleAssociates);
        assertEquals(0, saleAssociates.size());
    }

    @Test
    public void testIdentifySaleAssociatesWithEmployeeWithoutExpectedTagsII() throws DataSourceException {
        final Long locationKey = 12345L;
        final Location consumerLocation = new Location();
        consumerLocation.setKey(locationKey);

        final Double demandRange = 25.75D;
        final Demand consumerDemand = new Demand();
        consumerDemand.setRange(demandRange);
        consumerDemand.setLocationKey(locationKey);
        consumerDemand.addCriterion("test");

        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(locationKey, key);
                return consumerLocation;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) {
                assertEquals(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager(), pm);
                assertEquals(location, consumerLocation);
                assertEquals(demandRange, range);
                List<Location> locations = new ArrayList<Location>();
                locations.add(consumerLocation);
                return locations;
            }
        });

        final Long storeKey = 12345L;
        final Store targetedStore = new Store();
        targetedStore.setKey(storeKey);

        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, Map<String, Object> parameters, List<Location> locations, int limit) {
                assertEquals(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager(), pm);
                assertNotNull(locations);
                assertEquals(1, locations.size());
                assertEquals(consumerLocation, locations.get(0));
                List<Store> stores = new ArrayList<Store>();
                stores.add(targetedStore);
                return stores;
            }
        });

        final SaleAssociate selectedSaleAssociate = new SaleAssociate();

        DemandProcessor.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager(), pm);
                assertEquals(SaleAssociate.STORE_KEY, key);
                assertEquals(storeKey, (Long) value);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                saleAssociates.add(selectedSaleAssociate);
                return saleAssociates;
            }
        };

        List<SaleAssociate> saleAssociates = DemandProcessor.identifySaleAssociates(BaseSteps.getBaseOperations().getPersistenceManager(), consumerDemand, new Consumer());
        assertNotNull(saleAssociates);
        assertEquals(0, saleAssociates.size());
    }

    @Test
    public void testIdentifySaleAssociatesWithEmployeeWithExpectedTagsI() throws DataSourceException {
        final Long locationKey = 12345L;
        final Location consumerLocation = new Location();
        consumerLocation.setKey(locationKey);

        final Double demandRange = 25.75D;
        final Demand consumerDemand = new Demand();
        consumerDemand.setRange(demandRange);
        consumerDemand.setLocationKey(locationKey);
        consumerDemand.addCriterion("test");

        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(locationKey, key);
                return consumerLocation;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) {
                assertEquals(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager(), pm);
                assertEquals(location, consumerLocation);
                assertEquals(demandRange, range);
                List<Location> locations = new ArrayList<Location>();
                locations.add(consumerLocation);
                return locations;
            }
        });

        final Long storeKey = 12345L;
        final Store targetedStore = new Store();
        targetedStore.setKey(storeKey);

        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, Map<String, Object> parameters, List<Location> locations, int limit) {
                assertEquals(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager(), pm);
                assertNotNull(locations);
                assertEquals(1, locations.size());
                assertEquals(consumerLocation, locations.get(0));
                List<Store> stores = new ArrayList<Store>();
                stores.add(targetedStore);
                return stores;
            }
        });

        final SaleAssociate selectedSaleAssociate = new SaleAssociate();
        selectedSaleAssociate.addCriterion("test");

        DemandProcessor.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager(), pm);
                assertEquals(SaleAssociate.STORE_KEY, key);
                assertEquals(storeKey, (Long) value);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                saleAssociates.add(selectedSaleAssociate);
                return saleAssociates;
            }
        };

        List<SaleAssociate> saleAssociates = DemandProcessor.identifySaleAssociates(BaseSteps.getBaseOperations().getPersistenceManager(), consumerDemand, new Consumer());
        assertNotNull(saleAssociates);
        assertEquals(1, saleAssociates.size());
        assertEquals(selectedSaleAssociate, saleAssociates.get(0));
    }

    @Test
    public void testIdentifySaleAssociatesWithEmployeeWithExpectedTagsII() throws DataSourceException {
        final Long locationKey = 12345L;
        final Location consumerLocation = new Location();
        consumerLocation.setKey(locationKey);

        final Double demandRange = 25.75D;
        final Demand consumerDemand = new Demand();
        consumerDemand.setRange(demandRange);
        consumerDemand.setLocationKey(locationKey);
        consumerDemand.addCriterion("one");
        consumerDemand.addCriterion("two");
        consumerDemand.addCriterion("three");
        consumerDemand.addCriterion("test");

        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(locationKey, key);
                return consumerLocation;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) {
                assertEquals(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager(), pm);
                assertEquals(location, consumerLocation);
                assertEquals(demandRange, range);
                List<Location> locations = new ArrayList<Location>();
                locations.add(consumerLocation);
                return locations;
            }
        });

        final Long storeKey = 12345L;
        final Store targetedStore = new Store();
        targetedStore.setKey(storeKey);

        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, Map<String, Object> parameters, List<Location> locations, int limit) {
                assertEquals(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager(), pm);
                assertNotNull(locations);
                assertEquals(1, locations.size());
                assertEquals(consumerLocation, locations.get(0));
                List<Store> stores = new ArrayList<Store>();
                stores.add(targetedStore);
                return stores;
            }
        });

        final SaleAssociate selectedSaleAssociate = new SaleAssociate();
        selectedSaleAssociate.addCriterion("ich");
        selectedSaleAssociate.addCriterion("ni");
        selectedSaleAssociate.addCriterion("san");
        selectedSaleAssociate.addCriterion("test");

        DemandProcessor.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager(), pm);
                assertEquals(SaleAssociate.STORE_KEY, key);
                assertEquals(storeKey, (Long) value);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                saleAssociates.add(selectedSaleAssociate);
                return saleAssociates;
            }
        };

        List<SaleAssociate> saleAssociates = DemandProcessor.identifySaleAssociates(BaseSteps.getBaseOperations().getPersistenceManager(), consumerDemand, new Consumer());
        assertNotNull(saleAssociates);
        assertEquals(1, saleAssociates.size());
        assertEquals(selectedSaleAssociate, saleAssociates.get(0));
    }

    @Test
    public void testIdentifyUnkownSaleAssociatesWithEmployeeWithExpectedTagsI() throws DataSourceException {
        final Long locationKey = 12345L;
        final Location consumerLocation = new Location();
        consumerLocation.setKey(locationKey);

        final Double demandRange = 25.75D;
        final Demand consumerDemand = new Demand() {
            @Override
            public List<Long> getSaleAssociateKeys() {
                return null;
            }
        };
        consumerDemand.setRange(demandRange);
        consumerDemand.setLocationKey(locationKey);
        consumerDemand.addCriterion("test");

        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(locationKey, key);
                return consumerLocation;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) {
                assertEquals(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager(), pm);
                assertEquals(location, consumerLocation);
                assertEquals(demandRange, range);
                List<Location> locations = new ArrayList<Location>();
                locations.add(consumerLocation);
                return locations;
            }
        });

        final Long storeKey = 12345L;
        final Store targetedStore = new Store();
        targetedStore.setKey(storeKey);

        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, Map<String, Object> parameters, List<Location> locations, int limit) {
                assertEquals(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager(), pm);
                assertNotNull(locations);
                assertEquals(1, locations.size());
                assertEquals(consumerLocation, locations.get(0));
                List<Store> stores = new ArrayList<Store>();
                stores.add(targetedStore);
                return stores;
            }
        });

        final SaleAssociate selectedSaleAssociate = new SaleAssociate();
        selectedSaleAssociate.addCriterion("test");

        DemandProcessor.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager(), pm);
                assertEquals(SaleAssociate.STORE_KEY, key);
                assertEquals(storeKey, (Long) value);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                saleAssociates.add(selectedSaleAssociate);
                return saleAssociates;
            }
        };

        List<SaleAssociate> saleAssociates = DemandProcessor.identifySaleAssociates(BaseSteps.getBaseOperations().getPersistenceManager(), consumerDemand, new Consumer());
        assertNotNull(saleAssociates);
        assertEquals(1, saleAssociates.size());
        assertEquals(selectedSaleAssociate, saleAssociates.get(0));
    }

    @Test
    public void testIdentifyUnkownSaleAssociatesWithEmployeeWithExpectedTagsII() throws DataSourceException {
        final Long locationKey = 12345L;
        final Location consumerLocation = new Location();
        consumerLocation.setKey(locationKey);

        final Double demandRange = 25.75D;
        final Demand consumerDemand = new Demand() {
            @Override
            public List<Long> getSaleAssociateKeys() {
                return new ArrayList<Long>();
            }
        };
        consumerDemand.setRange(demandRange);
        consumerDemand.setLocationKey(locationKey);
        consumerDemand.addCriterion("test");

        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(locationKey, key);
                return consumerLocation;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) {
                assertEquals(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager(), pm);
                assertEquals(location, consumerLocation);
                assertEquals(demandRange, range);
                List<Location> locations = new ArrayList<Location>();
                locations.add(consumerLocation);
                return locations;
            }
        });

        final Long storeKey = 12345L;
        final Store targetedStore = new Store();
        targetedStore.setKey(storeKey);

        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, Map<String, Object> parameters, List<Location> locations, int limit) {
                assertEquals(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager(), pm);
                assertNotNull(locations);
                assertEquals(1, locations.size());
                assertEquals(consumerLocation, locations.get(0));
                List<Store> stores = new ArrayList<Store>();
                stores.add(targetedStore);
                return stores;
            }
        });

        final SaleAssociate selectedSaleAssociate = new SaleAssociate();
        selectedSaleAssociate.addCriterion("test");

        DemandProcessor.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager(), pm);
                assertEquals(SaleAssociate.STORE_KEY, key);
                assertEquals(storeKey, (Long) value);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                saleAssociates.add(selectedSaleAssociate);
                return saleAssociates;
            }
        };

        List<SaleAssociate> saleAssociates = DemandProcessor.identifySaleAssociates(BaseSteps.getBaseOperations().getPersistenceManager(), consumerDemand, new Consumer());
        assertNotNull(saleAssociates);
        assertEquals(1, saleAssociates.size());
        assertEquals(selectedSaleAssociate, saleAssociates.get(0));
    }

    @Test
    public void testIdentifyKownSaleAssociatesWithEmployeeWithExpectedTags() throws DataSourceException {
        final Long locationKey = 12345L;
        final Long saleAssociateKey = 1111L;
        final Location consumerLocation = new Location();
        consumerLocation.setKey(locationKey);

        final Double demandRange = 25.75D;
        final Demand consumerDemand = new Demand();
        consumerDemand.setRange(demandRange);
        consumerDemand.setLocationKey(locationKey);
        consumerDemand.addCriterion("test");
        consumerDemand.addSaleAssociateKey(saleAssociateKey);

        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(locationKey, key);
                return consumerLocation;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) {
                assertEquals(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager(), pm);
                assertEquals(location, consumerLocation);
                assertEquals(demandRange, range);
                List<Location> locations = new ArrayList<Location>();
                locations.add(consumerLocation);
                return locations;
            }
        });

        final Long storeKey = 12345L;
        final Store targetedStore = new Store();
        targetedStore.setKey(storeKey);

        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, Map<String, Object> parameters, List<Location> locations, int limit) {
                assertEquals(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager(), pm);
                assertNotNull(locations);
                assertEquals(1, locations.size());
                assertEquals(consumerLocation, locations.get(0));
                List<Store> stores = new ArrayList<Store>();
                stores.add(targetedStore);
                return stores;
            }
        });

        final SaleAssociate selectedSaleAssociate = new SaleAssociate();
        selectedSaleAssociate.setKey(saleAssociateKey);
        selectedSaleAssociate.addCriterion("test");

        DemandProcessor.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager(), pm);
                assertEquals(SaleAssociate.STORE_KEY, key);
                assertEquals(storeKey, (Long) value);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                saleAssociates.add(selectedSaleAssociate);
                return saleAssociates;
            }
        };

        List<SaleAssociate> saleAssociates = DemandProcessor.identifySaleAssociates(BaseSteps.getBaseOperations().getPersistenceManager(), consumerDemand, new Consumer());
        assertNotNull(saleAssociates);
        assertEquals(0, saleAssociates.size());
    }

    @Test
    public void testProcessOneDemandIa() throws DataSourceException {
        final Long locationKey = 12345L;
        final Location consumerLocation = new Location();
        consumerLocation.setKey(locationKey);

        final Long consumerKey = 43432L;
        final Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(consumerKey, key);
                return consumer;
            }
        });

        final Long demandKey = 67890L;
        final Double demandRange = 25.75D;
        final Demand consumerDemand = new Demand();
        consumerDemand.setOwnerKey(consumerKey);
        consumerDemand.addCriterion("test");
        consumerDemand.setKey(demandKey);
        consumerDemand.setLocationKey(locationKey);
        // consumerDemand.setQuantity(1L); // Default quantity
        consumerDemand.setRange(demandRange);
        consumerDemand.setState(State.published);

        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(consumerKey);
                return consumerDemand;
            }
        });

        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(locationKey, key);
                return consumerLocation;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) {
                List<Location> locations = new ArrayList<Location>();
                locations.add(consumerLocation);
                return locations;
            }
        });

        final Long storeKey = 12345L;
        final Store targetedStore = new Store();
        targetedStore.setKey(storeKey);

        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, Map<String, Object> parameters, List<Location> locations, int limit) {
                List<Store> stores = new ArrayList<Store>();
                stores.add(targetedStore);
                return stores;
            }
        });

        final String saleAssociateId = "Ryan";
        final SaleAssociate selectedSaleAssociate = new SaleAssociate();
        selectedSaleAssociate.setTwitterId(saleAssociateId);
        selectedSaleAssociate.addCriterion("test");
        selectedSaleAssociate.setPreferredConnection(Source.simulated);

        DemandProcessor.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                saleAssociates.add(selectedSaleAssociate);
                return saleAssociates;
            }
        };

        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                RawCommand rawCommand = new RawCommand();
                rawCommand.setSource(Source.simulated);
                return rawCommand;
            }
        });

        DemandProcessor.process(demandKey, true);

        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());

        String sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1);
        assertNotNull(sentText);
        assertTrue(sentText.contains(consumerDemand.getKey().toString()));
        assertTrue(sentText.contains("test"));

        sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessOneDemandIb() throws DataSourceException {
        final Long locationKey = 12345L;
        final Location consumerLocation = new Location();
        consumerLocation.setKey(locationKey);

        final Long consumerKey = 43432L;
        final Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(consumerKey, key);
                return consumer;
            }
        });

        final Long demandKey = 67890L;
        final Double demandRange = 25.75D;
        final Demand consumerDemand = new Demand() {
            @Override
            public List<Long> getSaleAssociateKeys() {
                return null;
            }
        };
        consumerDemand.setOwnerKey(consumerKey);
        consumerDemand.addCriterion("test");
        consumerDemand.setKey(demandKey);
        consumerDemand.setLocationKey(locationKey);
        // consumerDemand.setQuantity(1L); // Default quantity
        consumerDemand.setRange(demandRange);
        consumerDemand.setState(State.published);

        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(consumerKey);
                return consumerDemand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(demandKey, demand.getKey());
                return demand;
            }
        });

        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(locationKey, key);
                return consumerLocation;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) {
                List<Location> locations = new ArrayList<Location>();
                locations.add(consumerLocation);
                return locations;
            }
        });

        final Long storeKey = 12345L;
        final Store targetedStore = new Store();
        targetedStore.setKey(storeKey);

        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, Map<String, Object> parameters, List<Location> locations, int limit) {
                List<Store> stores = new ArrayList<Store>();
                stores.add(targetedStore);
                return stores;
            }
        });

        final String saleAssociateId = "Ryan";
        final SaleAssociate selectedSaleAssociate = new SaleAssociate();
        selectedSaleAssociate.setTwitterId(saleAssociateId);
        selectedSaleAssociate.addCriterion("test");
        selectedSaleAssociate.setPreferredConnection(Source.simulated);

        DemandProcessor.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                saleAssociates.add(selectedSaleAssociate);
                return saleAssociates;
            }
        };

        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                RawCommand rawCommand = new RawCommand();
                rawCommand.setSource(Source.simulated);
                return rawCommand;
            }
        });

        DemandProcessor.process(demandKey, true);

        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());

        String sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1);
        assertNotNull(sentText);
        assertTrue(sentText.contains(consumerDemand.getKey().toString()));
        assertTrue(sentText.contains("test"));

        sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("dp_inform_consumer_about_no_store", Locale.ENGLISH), sentText);
    }


    @Test
    public void testProcessOneDemandIc() throws DataSourceException {
        final Long locationKey = 12345L;
        final Location consumerLocation = new Location();
        consumerLocation.setKey(locationKey);

        final Long consumerKey = 43432L;
        final Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(consumerKey, key);
                return consumer;
            }
        });

        final Long demandKey = 67890L;
        final Double demandRange = 25.75D;
        final Demand consumerDemand = new Demand() {
            @Override
            public List<Long> getSaleAssociateKeys() {
                return null;
            }
        };
        consumerDemand.setOwnerKey(consumerKey);
        consumerDemand.addCriterion("test");
        consumerDemand.setKey(demandKey);
        consumerDemand.setLocationKey(locationKey);
        // consumerDemand.setQuantity(1L); // Default quantity
        consumerDemand.setRange(demandRange);
        consumerDemand.setState(State.published);

        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(consumerKey);
                return consumerDemand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(demandKey, demand.getKey());
                return demand;
            }
        });

        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(locationKey, key);
                return consumerLocation;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) {
                List<Location> locations = new ArrayList<Location>();
                locations.add(consumerLocation);
                return locations;
            }
        });

        final Long storeKey = 12345L;
        final Store targetedStore = new Store();
        targetedStore.setKey(storeKey);

        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, Map<String, Object> parameters, List<Location> locations, int limit) {
                List<Store> stores = new ArrayList<Store>();
                stores.add(targetedStore);
                return stores;
            }
        });

        final String saleAssociateId = "Ryan";
        final SaleAssociate selectedSaleAssociate = new SaleAssociate();
        selectedSaleAssociate.setTwitterId(saleAssociateId);
        selectedSaleAssociate.addCriterion("test");
        selectedSaleAssociate.setPreferredConnection(Source.simulated);

        DemandProcessor.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                saleAssociates.add(selectedSaleAssociate);
                return saleAssociates;
            }
        };

        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                RawCommand rawCommand = new RawCommand();
                rawCommand.setSource(Source.simulated);
                return rawCommand;
            }
        });

        DemandProcessor.process(demandKey, false);

        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(consumerDemand.getKey().toString()));
        assertTrue(sentText.contains("test"));
    }

    @Test
    public void testProcessOneDemandII() throws DataSourceException {
        final Long locationKey = 12345L;
        final Location consumerLocation = new Location();
        consumerLocation.setKey(locationKey);

        final Long consumerKey = 43432L;
        final Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(consumerKey, key);
                return consumer;
            }
        });

        final Long demandKey = 67890L;
        final Double demandRange = 25.75D;
        final Demand consumerDemand = new Demand();
        consumerDemand.setOwnerKey(consumerKey);
        consumerDemand.addCriterion("test");
        consumerDemand.setKey(demandKey);
        consumerDemand.setLocationKey(locationKey);
        consumerDemand.setRange(demandRange);
        consumerDemand.setQuantity(123L);
        consumerDemand.setState(State.published);

        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(consumerKey);
                return consumerDemand;
            }
        });

        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(locationKey, key);
                return consumerLocation;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) {
                List<Location> locations = new ArrayList<Location>();
                locations.add(consumerLocation);
                return locations;
            }
        });

        final Long storeKey = 12345L;
        final Store targetedStore = new Store();
        targetedStore.setKey(storeKey);

        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, Map<String, Object> parameters, List<Location> locations, int limit) {
                List<Store> stores = new ArrayList<Store>();
                stores.add(targetedStore);
                return stores;
            }
        });

        final String saleAssociateId = "Ryan";
        final SaleAssociate selectedSaleAssociate = new SaleAssociate();
        selectedSaleAssociate.setTwitterId(saleAssociateId);
        selectedSaleAssociate.addCriterion("test");
        selectedSaleAssociate.setPreferredConnection(Source.simulated);

        DemandProcessor.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                saleAssociates.add(selectedSaleAssociate);
                return saleAssociates;
            }
        };

        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                RawCommand rawCommand = new RawCommand();
                rawCommand.setSource(Source.simulated);
                return rawCommand;
            }
        });

        DemandProcessor.process(demandKey, true);

        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());

        String sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1);
        assertNotNull(sentText);
        assertTrue(sentText.contains(consumerDemand.getKey().toString()));
        assertTrue(sentText.contains("test"));

        sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    @SuppressWarnings("serial")
    public void testProcessOneDemandAlreadyProposed() throws DataSourceException {
        final Long locationKey = 12345L;
        final Location consumerLocation = new Location();
        consumerLocation.setKey(locationKey);

        final Long consumerKey = 43432L;
        final Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(consumerKey, key);
                return consumer;
            }
        });

        final Long demandKey = 67890L;
        final Double demandRange = 25.75D;
        final Long saleAssociateKey = 56478L;
        final Demand consumerDemand = new Demand();
        consumerDemand.setOwnerKey(consumerKey);
        consumerDemand.addCriterion("test");
        consumerDemand.setKey(demandKey);
        consumerDemand.setLocationKey(locationKey);
        consumerDemand.setRange(demandRange);
        consumerDemand.setState(State.published);
        consumerDemand.addSaleAssociateKey(saleAssociateKey);

        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(consumerKey);
                return consumerDemand;
            }
        });

        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(locationKey, key);
                return consumerLocation;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) {
                List<Location> locations = new ArrayList<Location>();
                locations.add(consumerLocation);
                return locations;
            }
        });

        final Long storeKey = 12345L;
        final Store targetedStore = new Store();
        targetedStore.setKey(storeKey);

        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, Map<String, Object> parameters, List<Location> locations, int limit) {
                List<Store> stores = new ArrayList<Store>();
                stores.add(targetedStore);
                return stores;
            }
        });

        final String saleAssociateId = "Ryan";
        final SaleAssociate selectedSaleAssociate = new SaleAssociate();
        selectedSaleAssociate.setKey(saleAssociateKey);
        selectedSaleAssociate.setTwitterId(saleAssociateId);
        selectedSaleAssociate.addCriterion("test");

        DemandProcessor.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                saleAssociates.add(selectedSaleAssociate);
                return saleAssociates;
            }
        };

        final Twitter mockTwitterAccount = (new Twitter() {
            @Override
            public DirectMessage sendDirectMessage(String id, String text) throws TwitterException {
                assertEquals(saleAssociateId.toString(), id);
                assertTrue(text.contains(consumerDemand.getKey().toString()));
                assertTrue(text.contains("test"));
                return null;
            }
        });
        MockTwitterConnector.injectMockTwitterAccount(mockTwitterAccount);

        DemandProcessor.process(demandKey, true);

        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test(expected=DataSourceException.class)
    public void testProcessOneDemandWithTroubleAccessingDatabase() throws DataSourceException {
        final Long locationKey = 12345L;
        final Location consumerLocation = new Location();
        consumerLocation.setKey(locationKey);

        final Long consumerKey = 43432L;
        final Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(consumerKey, key);
                return consumer;
            }
        });

        final Long demandKey = 67890L;
        final Double demandRange = 25.75D;
        final Demand consumerDemand = new Demand();
        consumerDemand.setOwnerKey(consumerKey);
        consumerDemand.addCriterion("test");
        consumerDemand.setKey(demandKey);
        consumerDemand.setLocationKey(locationKey);
        consumerDemand.setRange(demandRange);
        consumerDemand.setState(State.published);

        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(consumerKey);
                return consumerDemand;
            }
        });

        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                throw new DataSourceException("done in purpose");
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) {
                List<Location> locations = new ArrayList<Location>();
                locations.add(consumerLocation);
                return locations;
            }
        });

        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public List<Proposal> getProposals(PersistenceManager pm, String attribute, Object value, int limit) {
                return new ArrayList<Proposal>();
            }
        });

        DemandProcessor.process(demandKey, true);

        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
        TwitterConnector.getTwetailerAccount();
    }
    ddd ****/

    @Test
    @SuppressWarnings({ "serial", "deprecation" })
    public void testProcessOneDemandWithTwitterTrouble() throws DataSourceException, InvalidIdentifierException {
        final Long locationKey = 12345L;
        final Location consumerLocation = new Location();
        consumerLocation.setKey(locationKey);

        /*
        final Long saConsumerRecordKey = 76325L;
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertTrue(consumerKey == key || saConsumerRecordKey == key);
                final Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                consumer.setPreferredConnection(Source.simulated);
                return consumer;
            }
        });
        final Long storeKey = 98236L;
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(storeKey, key);
                final Store store = new Store();
                store.setKey(storeKey);
                return store;
            }
        });
         */
        final Long consumerKey = 43432L;
        final Long saConsumerRecordKey = 76325L;
        final Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setPreferredConnection(Source.simulated);
        consumer.setPublishedDemandNb(36L);
        consumer.setClosedDemandNb(3L);
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertTrue(consumerKey == key || saConsumerRecordKey == key);
                return consumer;
            }
        });

        final Long demandKey = 67890L;
        final Double demandRange = 25.75D;
        final Demand consumerDemand = new Demand();
        consumerDemand.setOwnerKey(consumerKey);
        consumerDemand.addCriterion("test");
        consumerDemand.setKey(demandKey);
        consumerDemand.setLocationKey(locationKey);
        consumerDemand.setRange(demandRange);
        consumerDemand.setState(State.published);

        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(consumerKey);
                return consumerDemand;
            }
        });

        BaseSteps.setMockInfluencerOperations(new InfluencerOperations());

        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                assertEquals(locationKey, key);
                return null;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) {
                List<Location> locations = new ArrayList<Location>();
                locations.add(consumerLocation);
                return locations;
            }
        });

        final Long storeKey = 12345L;
        final Store targetedStore = new Store();
        targetedStore.setKey(storeKey);

        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, Map<String, Object> parameters, List<Location> locations, int limit) {
                List<Store> stores = new ArrayList<Store>();
                stores.add(targetedStore);
                return stores;
            }
        });

        Collator collator = LocaleValidator.getCollator(Locale.ENGLISH);

        final SaleAssociate selectedSaleAssociate = new SaleAssociate();
        selectedSaleAssociate.setConsumerKey(saConsumerRecordKey);
        selectedSaleAssociate.addCriterion("test", collator);

        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                saleAssociates.add(selectedSaleAssociate);
                return saleAssociates;
            }
        });

        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public List<Proposal> getProposals(PersistenceManager pm, String attribute, Object value, int limit) {
                return new ArrayList<Proposal>();
            }
        });

        final Twitter mockTwitterAccount = (new Twitter() {
            @Override
            public DirectMessage sendDirectMessage(String id, String text) throws TwitterException {
                throw new TwitterException("done in purpose");
            }
        });
        MockTwitterConnector.injectMockTwitterAccount(mockTwitterAccount);

        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                RawCommand rawCommand = new RawCommand();
                rawCommand.setSource(Source.simulated);
                return rawCommand;
            }
        });

        CommandLineParser.loadLocalizedSettings(Locale.ENGLISH);

        DemandProcessor.process(demandKey, true);

        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    @SuppressWarnings({ "serial", "deprecation" })
    public void testProcessOneDemandWithTwitterAndCatchAllTrouble() throws DataSourceException, InvalidIdentifierException {
        final Long locationKey = 12345L;
        final Location consumerLocation = new Location();
        consumerLocation.setKey(locationKey);

        final Long consumerKey = 43432L;
        final Long saConsumerRecordKey = 76325L;
        final Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setPreferredConnection(Source.simulated);
        consumer.setPublishedDemandNb(36L);
        consumer.setClosedDemandNb(3L);
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertTrue(consumerKey == key || saConsumerRecordKey == key);
                return consumer;
            }
        });

        final Long demandKey = 67890L;
        final Double demandRange = 25.75D;
        final Demand consumerDemand = new Demand();
        consumerDemand.setOwnerKey(consumerKey);
        consumerDemand.addCriterion("test");
        consumerDemand.setKey(demandKey);
        consumerDemand.setLocationKey(locationKey);
        consumerDemand.setRange(demandRange);
        consumerDemand.setState(State.published);

        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(consumerKey);
                return consumerDemand;
            }
        });

        BaseSteps.setMockInfluencerOperations(new InfluencerOperations());

        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                assertEquals(locationKey, key);
                return null;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) {
                List<Location> locations = new ArrayList<Location>();
                locations.add(consumerLocation);
                return locations;
            }
        });

        final Long storeKey = 12345L;
        final Store targetedStore = new Store();
        targetedStore.setKey(storeKey);

        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, Map<String, Object> parameters, List<Location> locations, int limit) {
                List<Store> stores = new ArrayList<Store>();
                stores.add(targetedStore);
                return stores;
            }
        });

        Collator collator = LocaleValidator.getCollator(Locale.ENGLISH);

        final SaleAssociate selectedSaleAssociate = new SaleAssociate();
        selectedSaleAssociate.setConsumerKey(saConsumerRecordKey);
        selectedSaleAssociate.addCriterion("test", collator);

        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                saleAssociates.add(selectedSaleAssociate);
                return saleAssociates;
            }
        });

        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public List<Proposal> getProposals(PersistenceManager pm, String attribute, Object value, int limit) {
                return new ArrayList<Proposal>();
            }
        });

        final Twitter mockTwitterAccount = (new Twitter() {
            @Override
            public DirectMessage sendDirectMessage(String id, String text) throws TwitterException {
                throw new TwitterException("done in purpose");
            }
        });
        MockTwitterConnector.injectMockTwitterAccount(mockTwitterAccount);

        MailConnector.foolNextMessagePost(); // To generate a MessagingException while trying to send an e-mail

        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                RawCommand rawCommand = new RawCommand();
                rawCommand.setSource(Source.simulated);
                return rawCommand;
            }
        });

        DemandProcessor.process(demandKey, true);

        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessOneDemandInIncorrectState() throws DataSourceException, InvalidIdentifierException {
        final Long consumerKey = 43432L;
        final Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(consumerKey, key);
                return consumer;
            }
        });

        final Long demandKey = 67890L;
        final Long locationKey = 12345L;
        final Double demandRange = 25.75D;
        final Demand consumerDemand = new Demand();
        consumerDemand.setOwnerKey(consumerKey);
        consumerDemand.addCriterion("test");
        consumerDemand.setKey(demandKey);
        consumerDemand.setLocationKey(locationKey);
        consumerDemand.setRange(demandRange);
        consumerDemand.setState(State.invalid); // Not published

        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(consumerKey);
                return consumerDemand;
            }
        });

        DemandProcessor.process(demandKey, true);

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test(expected=RuntimeException.class)
    public void testProcessBatchWithFailure() throws DataSourceException {
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> parameters, int limit) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        });

        DemandProcessor.batchProcess();
    }

    @Test
    public void testProcessBatchI() throws DataSourceException {
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> parameters, int limit) {
                return new ArrayList<Demand>();
            }
        });

        DemandProcessor.batchProcess();

        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessBatchII() throws Exception {
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> parameters, int limit) {
                Demand demand = new Demand();
                demand.setKey(12345L);
                List<Demand> demands = new ArrayList<Demand>();
                demands.add(demand);
                return demands;
            }
        });

        DemandProcessor.batchProcess();

        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessOneDemandForTheRobotI() throws Exception {
        final Long robotKey = 12321L;
        BaseSteps.setMockSettingsOperations(new SettingsOperations() {
            @Override
            @SuppressWarnings("serial")
            public Settings getSettings(PersistenceManager pm) throws DataSourceException {
                return new Settings() {
                    @Override
                    public Long getRobotSaleAssociateKey() {
                        return robotKey;
                    }
                };
            }
        });

        Demand demand = new Demand();
        demand.addSaleAssociateKey(robotKey);

        assertTrue(DemandProcessor.hasRobotAlreadyContacted(new MockPersistenceManager(), demand));
    }

    @Test
    public void testProcessOneDemandForTheRobotII() throws Exception {
        final Long robotKey = 12321L;
        BaseSteps.setMockSettingsOperations(new SettingsOperations() {
            @Override
            public Settings getSettings(PersistenceManager pm) throws DataSourceException {
                return new Settings();
            }
        });

        Demand demand = new Demand();
        demand.addSaleAssociateKey(robotKey);

        assertTrue(DemandProcessor.hasRobotAlreadyContacted(new MockPersistenceManager(), demand));
    }

    @Test
    public void testProcessOneDemandForTheRobotIII() throws Exception {
        final Long robotKey = 12321L;
        RobotResponder.setRobotSaleAssociateKey(robotKey);

        Demand demand = new Demand();
        demand.addSaleAssociateKey(robotKey);

        assertTrue(DemandProcessor.hasRobotAlreadyContacted(new MockPersistenceManager(), demand));
    }

    @Test
    @Ignore
    public void testHasRobotAlreadyContactedI() throws Exception {
        final Long locationKey = 12345L;
        final Location consumerLocation = new Location();
        consumerLocation.setKey(locationKey);

        final Long consumerKey = 43432L;
        final Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(consumerKey, key);
                return consumer;
            }
        });

        final Long demandKey = 67890L;
        final Double demandRange = 25.75D;
        final Demand consumerDemand = new Demand();
        consumerDemand.setOwnerKey(consumerKey);
        consumerDemand.addCriterion("test");
        consumerDemand.setKey(demandKey);
        consumerDemand.setLocationKey(locationKey);
        consumerDemand.setRange(demandRange);
        consumerDemand.setState(State.published);
        consumerDemand.addHashTag(RobotResponder.ROBOT_DEMO_HASH_TAG);

        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(consumerKey);
                return consumerDemand;
            }
        });

        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(locationKey, key);
                return consumerLocation;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) {
                return new ArrayList<Location>();
            }
        });

        final Long robotKey = 12321L;
        BaseSteps.setMockSettingsOperations(new SettingsOperations() {
            @Override
            @SuppressWarnings("serial")
            public Settings getSettings(PersistenceManager pm) throws DataSourceException {
                return new Settings() {
                    @Override
                    public Long getRobotSaleAssociateKey() {
                        return robotKey;
                    }
                };
            }
        });

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());

        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                RawCommand rawCommand = new RawCommand();
                rawCommand.setSource(Source.simulated);
                return rawCommand;
            }
        });

        DemandProcessor.process(demandKey, true);

        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));

        List<TaskOptions> tasks = ((MockQueue) ((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousQueue()).getHistory();
        assertNotNull(tasks);
        assertNotSame(0, tasks.size());
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testHasRobotAlreadyContactedII() throws Exception {
        final Long locationKey = 12345L;
        final Location consumerLocation = new Location();
        consumerLocation.setKey(locationKey);

        final Long consumerKey = 43432L;
        final Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(consumerKey, key);
                return consumer;
            }
        });

        final Long robotKey = 12321L;

        final Long demandKey = 67890L;
        final Double demandRange = 25.75D;
        final Demand consumerDemand = new Demand();
        consumerDemand.setOwnerKey(consumerKey);
        consumerDemand.addCriterion("test");
        consumerDemand.setKey(demandKey);
        consumerDemand.setLocationKey(locationKey);
        consumerDemand.setRange(demandRange);
        consumerDemand.setState(State.published);
        consumerDemand.addHashTag(RobotResponder.ROBOT_DEMO_HASH_TAG);
        consumerDemand.addSaleAssociateKey(robotKey);

        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(consumerKey);
                return consumerDemand;
            }
        });

        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(locationKey, key);
                return consumerLocation;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) {
                return new ArrayList<Location>();
            }
        });

        BaseSteps.setMockSettingsOperations(new SettingsOperations() {
            @Override
            @SuppressWarnings("serial")
            public Settings getSettings(PersistenceManager pm) throws DataSourceException {
                return new Settings() {
                    @Override
                    public Long getRobotSaleAssociateKey() {
                        return robotKey;
                    }
                };
            }
        });

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());

        DemandProcessor.process(demandKey, true);

        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        List<TaskOptions> tasks = ((MockQueue) BaseSteps.getBaseOperations().getQueue()).getHistory();
        assertNotNull(tasks);
        assertEquals(0, tasks.size());
    }

    @Test
    public void testProcessWithNullAssociateKeyReference () throws DataSourceException, InvalidIdentifierException {
        final Long demandKey = 43543L;
        final Demand demand = new Demand() {
            @Override
            public List<Long> getSaleAssociateKeys() {
                return null;
            }
        };
        demand.setState(State.closed);

        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(consumerKey);
                return demand;
            }
        });

        DemandProcessor.process(demandKey, true);
    }

    @Test
    public void testProcessWithEmptyAssociateKeyArray () throws DataSourceException, InvalidIdentifierException {
        final Long demandKey = 43543L;
        final Demand demand = new Demand() {
            @Override
            public List<Long> getSaleAssociateKeys() {
                return new ArrayList<Long>();
            }
        };
        demand.setState(State.closed);

        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(consumerKey);
                return demand;
            }
        });

        DemandProcessor.process(demandKey, true);
    }

    @Test
    public void testProcessWithFullAssociateKeyArray () throws DataSourceException, InvalidIdentifierException {
        final Long demandKey = 43543L;
        final Demand demand = new Demand() {
            @Override
            public List<Long> getSaleAssociateKeys() {
                List<Long> data = new ArrayList<Long>();
                data.add(34567L);
                data.add(65768L);
                return data;
            }
        };
        demand.setState(State.closed);

        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(consumerKey);
                return demand;
            }
        });

        DemandProcessor.process(demandKey, true);
    }

    @Test
    @SuppressWarnings({ "serial", "deprecation" })
    public void testFailingToContactConsumerI() throws Exception {
        final Long locationKey = 12345L;
        final Location consumerLocation = new Location();
        consumerLocation.setKey(locationKey);

        final Long consumerKey = 43432L;
        final Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(consumerKey, key);
                return consumer;
            }
        });

        final Long demandKey = 67890L;
        final Double demandRange = 25.75D;
        final Demand consumerDemand = new Demand();
        consumerDemand.setOwnerKey(consumerKey);
        consumerDemand.addCriterion("test");
        consumerDemand.setKey(demandKey);
        consumerDemand.setLocationKey(locationKey);
        consumerDemand.setRange(demandRange);
        consumerDemand.setSource(Source.twitter);
        consumerDemand.setState(State.published);

        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(consumerKey);
                return consumerDemand;
            }
        });

        BaseSteps.setMockInfluencerOperations(new InfluencerOperations());

        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(locationKey, key);
                return consumerLocation;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) {
                return new ArrayList<Location>();
            }
        });

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());

        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                RawCommand rawCommand = new RawCommand();
                rawCommand.setSource(Source.twitter);
                consumer.setTwitterId("fake"); // To be sure the communication uses the MockTwitter defined below
                return rawCommand;
            }
        });

        final Twitter mockTwitterAccount = (new Twitter() {
            @Override
            public DirectMessage sendDirectMessage(String id, String text) throws TwitterException {
                throw new TwitterException("Done in purpose");
            }
        });
        MockTwitterConnector.injectMockTwitterAccount(mockTwitterAccount);

        DemandProcessor.process(demandKey, true);
    }

    @Test
    @SuppressWarnings({ "serial", "deprecation" })
    public void testFailingToContactConsumerII() throws Exception {
        final Long locationKey = 12345L;
        final Location consumerLocation = new Location();
        consumerLocation.setKey(locationKey);

        final Long consumerKey = 43432L;
        final Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(consumerKey, key);
                return consumer;
            }
        });

        final Long demandKey = 67890L;
        final Double demandRange = 25.75D;
        final Demand consumerDemand = new Demand();
        consumerDemand.setOwnerKey(consumerKey);
        consumerDemand.addCriterion("test");
        consumerDemand.setKey(demandKey);
        consumerDemand.setLocationKey(locationKey);
        consumerDemand.setRange(demandRange);
        consumerDemand.setSource(Source.twitter);
        consumerDemand.setState(State.published);

        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(consumerKey);
                return consumerDemand;
            }
        });

        BaseSteps.setMockInfluencerOperations(new InfluencerOperations());

        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(locationKey, key);
                return consumerLocation;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) {
                return new ArrayList<Location>();
            }
        });

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());

        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                RawCommand rawCommand = new RawCommand();
                rawCommand.setSource(Source.twitter);
                consumer.setTwitterId("fake"); // To be sure the communication uses the MockTwitter defined below
                return rawCommand;
            }
        });

        final Twitter mockTwitterAccount = (new Twitter() {
            @Override
            public DirectMessage sendDirectMessage(String id, String text) throws TwitterException {
                throw new TwitterException("Done in purpose");
            }
        });
        MockTwitterConnector.injectMockTwitterAccount(mockTwitterAccount);

        MailConnector.foolNextMessagePost();

        DemandProcessor.process(demandKey, true);
    }
}
