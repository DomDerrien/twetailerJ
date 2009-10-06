package twetailer.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.DataSourceException;
import twetailer.connector.BaseConnector;
import twetailer.connector.MockTwitterConnector;
import twetailer.connector.TwitterConnector;
import twetailer.dao.BaseOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.MockPersistenceManager;
import twetailer.dao.RetailerOperations;
import twetailer.dao.StoreOperations;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.Retailer;
import twetailer.dto.Store;
import twetailer.validator.CommandSettings.State;
import twitter4j.DirectMessage;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TestDemandProcessor {

    private class MockBaseOperations extends BaseOperations {
        private PersistenceManager pm = new MockPersistenceManager();
        @Override
        public PersistenceManager getPersistenceManager() {
            return pm;
        }
    };

    @Before
    public void setUp() throws Exception {
        DemandProcessor._baseOperations = new MockBaseOperations();
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testConstructor() {
        new DemandProcessor();
    }

    @Test(expected=DataSourceException.class)
    public void testProcessNoDemand() throws DataSourceException {
        final Long demandKey = 12345L;

        // DemandOperations mock
        DemandProcessor.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(consumerKey);
                throw new DataSourceException("Done in purpose");
            }
        };

        DemandProcessor.process(demandKey);

        assertTrue(DemandProcessor._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testIdentifyRetailersNoLocationAround() throws DataSourceException {
        final Long locationKey = 12345L;
        final Location consumerLocation = new Location();
        consumerLocation.setKey(locationKey);

        final Double demandRange = 25.75D;
        final Demand consumerDemand = new Demand();
        consumerDemand.setRange(demandRange);
        consumerDemand.setLocationKey(locationKey);

        DemandProcessor.locationOperations = new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws DataSourceException {
                assertEquals(locationKey, key);
                return consumerLocation;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, int limit) {
                assertEquals(DemandProcessor._baseOperations.getPersistenceManager(), pm);
                assertEquals(location, consumerLocation);
                assertEquals(demandRange, range);
                return new ArrayList<Location>();
            }
        };

        List<Retailer> retailers = DemandProcessor.identifyRetailers(DemandProcessor._baseOperations.getPersistenceManager(), consumerDemand);
        assertNotNull(retailers);
        assertEquals(0, retailers.size());
    }

    @Test
    public void testIdentifyRetailersNoStoreForLocationAround() throws DataSourceException {
        final Long locationKey = 12345L;
        final Location consumerLocation = new Location();
        consumerLocation.setKey(locationKey);

        final Double demandRange = 25.75D;
        final Demand consumerDemand = new Demand();
        consumerDemand.setRange(demandRange);
        consumerDemand.setLocationKey(locationKey);

        DemandProcessor.locationOperations = new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws DataSourceException {
                assertEquals(locationKey, key);
                return consumerLocation;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, int limit) {
                assertEquals(DemandProcessor._baseOperations.getPersistenceManager(), pm);
                assertEquals(location, consumerLocation);
                assertEquals(demandRange, range);
                List<Location> locations = new ArrayList<Location>();
                locations.add(consumerLocation);
                return locations;
            }
        };

        DemandProcessor.storeOperations = new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, List<Location> locations, int limit) {
                assertEquals(DemandProcessor._baseOperations.getPersistenceManager(), pm);
                assertNotNull(locations);
                assertEquals(1, locations.size());
                assertEquals(consumerLocation, locations.get(0));
                return new ArrayList<Store>();
            }
        };

        List<Retailer> retailers = DemandProcessor.identifyRetailers(DemandProcessor._baseOperations.getPersistenceManager(), consumerDemand);
        assertNotNull(retailers);
        assertEquals(0, retailers.size());
    }

    @Test
    public void testIdentifyRetailersForAStoreWithoutEmployees() throws DataSourceException {
        final Long locationKey = 12345L;
        final Location consumerLocation = new Location();
        consumerLocation.setKey(locationKey);

        final Double demandRange = 25.75D;
        final Demand consumerDemand = new Demand();
        consumerDemand.setRange(demandRange);
        consumerDemand.setLocationKey(locationKey);

        DemandProcessor.locationOperations = new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws DataSourceException {
                assertEquals(locationKey, key);
                return consumerLocation;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, int limit) {
                assertEquals(DemandProcessor._baseOperations.getPersistenceManager(), pm);
                assertEquals(location, consumerLocation);
                assertEquals(demandRange, range);
                List<Location> locations = new ArrayList<Location>();
                locations.add(consumerLocation);
                return locations;
            }
        };

        final Long storeKey = 12345L;
        final Store targetedStore = new Store();
        targetedStore.setKey(storeKey);

        DemandProcessor.storeOperations = new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, List<Location> locations, int limit) {
                assertEquals(DemandProcessor._baseOperations.getPersistenceManager(), pm);
                assertNotNull(locations);
                assertEquals(1, locations.size());
                assertEquals(consumerLocation, locations.get(0));
                List<Store> stores = new ArrayList<Store>();
                stores.add(targetedStore);
                return stores;
            }
        };

        DemandProcessor.retailerOperations = new RetailerOperations() {
            @Override
            public List<Retailer> getRetailers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(DemandProcessor._baseOperations.getPersistenceManager(), pm);
                assertEquals(Retailer.STORE_KEY, key);
                assertEquals(storeKey, (Long) value);
                return new ArrayList<Retailer>();
            }
        };

        List<Retailer> retailers = DemandProcessor.identifyRetailers(DemandProcessor._baseOperations.getPersistenceManager(), consumerDemand);
        assertNotNull(retailers);
        assertEquals(0, retailers.size());
    }

    @Test
    public void testIdentifyRetailersWithEmployeeWithoutExpectedTagsI() throws DataSourceException {
        final Long locationKey = 12345L;
        final Location consumerLocation = new Location();
        consumerLocation.setKey(locationKey);

        final Double demandRange = 25.75D;
        final Demand consumerDemand = new Demand();
        consumerDemand.setRange(demandRange);
        consumerDemand.setLocationKey(locationKey);
        consumerDemand.addCriterion("test");

        DemandProcessor.locationOperations = new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws DataSourceException {
                assertEquals(locationKey, key);
                return consumerLocation;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, int limit) {
                assertEquals(DemandProcessor._baseOperations.getPersistenceManager(), pm);
                assertEquals(location, consumerLocation);
                assertEquals(demandRange, range);
                List<Location> locations = new ArrayList<Location>();
                locations.add(consumerLocation);
                return locations;
            }
        };

        final Long storeKey = 12345L;
        final Store targetedStore = new Store();
        targetedStore.setKey(storeKey);

        DemandProcessor.storeOperations = new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, List<Location> locations, int limit) {
                assertEquals(DemandProcessor._baseOperations.getPersistenceManager(), pm);
                assertNotNull(locations);
                assertEquals(1, locations.size());
                assertEquals(consumerLocation, locations.get(0));
                List<Store> stores = new ArrayList<Store>();
                stores.add(targetedStore);
                return stores;
            }
        };

        final Retailer selectedRetailer = new Retailer() {
            @Override
            public List<String> getCriteria() {
                return null;
            }
        };

        DemandProcessor.retailerOperations = new RetailerOperations() {
            @Override
            public List<Retailer> getRetailers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(DemandProcessor._baseOperations.getPersistenceManager(), pm);
                assertEquals(Retailer.STORE_KEY, key);
                assertEquals(storeKey, (Long) value);
                List<Retailer> retailers = new ArrayList<Retailer>();
                retailers.add(selectedRetailer);
                return retailers;
            }
        };

        List<Retailer> retailers = DemandProcessor.identifyRetailers(DemandProcessor._baseOperations.getPersistenceManager(), consumerDemand);
        assertNotNull(retailers);
        assertEquals(0, retailers.size());
    }

    @Test
    public void testIdentifyRetailersWithEmployeeWithoutExpectedTagsII() throws DataSourceException {
        final Long locationKey = 12345L;
        final Location consumerLocation = new Location();
        consumerLocation.setKey(locationKey);

        final Double demandRange = 25.75D;
        final Demand consumerDemand = new Demand();
        consumerDemand.setRange(demandRange);
        consumerDemand.setLocationKey(locationKey);
        consumerDemand.addCriterion("test");

        DemandProcessor.locationOperations = new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws DataSourceException {
                assertEquals(locationKey, key);
                return consumerLocation;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, int limit) {
                assertEquals(DemandProcessor._baseOperations.getPersistenceManager(), pm);
                assertEquals(location, consumerLocation);
                assertEquals(demandRange, range);
                List<Location> locations = new ArrayList<Location>();
                locations.add(consumerLocation);
                return locations;
            }
        };

        final Long storeKey = 12345L;
        final Store targetedStore = new Store();
        targetedStore.setKey(storeKey);

        DemandProcessor.storeOperations = new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, List<Location> locations, int limit) {
                assertEquals(DemandProcessor._baseOperations.getPersistenceManager(), pm);
                assertNotNull(locations);
                assertEquals(1, locations.size());
                assertEquals(consumerLocation, locations.get(0));
                List<Store> stores = new ArrayList<Store>();
                stores.add(targetedStore);
                return stores;
            }
        };

        final Retailer selectedRetailer = new Retailer();

        DemandProcessor.retailerOperations = new RetailerOperations() {
            @Override
            public List<Retailer> getRetailers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(DemandProcessor._baseOperations.getPersistenceManager(), pm);
                assertEquals(Retailer.STORE_KEY, key);
                assertEquals(storeKey, (Long) value);
                List<Retailer> retailers = new ArrayList<Retailer>();
                retailers.add(selectedRetailer);
                return retailers;
            }
        };

        List<Retailer> retailers = DemandProcessor.identifyRetailers(DemandProcessor._baseOperations.getPersistenceManager(), consumerDemand);
        assertNotNull(retailers);
        assertEquals(0, retailers.size());
    }

    @Test
    public void testIdentifyRetailersWithEmployeeWithExpectedTagsI() throws DataSourceException {
        final Long locationKey = 12345L;
        final Location consumerLocation = new Location();
        consumerLocation.setKey(locationKey);

        final Double demandRange = 25.75D;
        final Demand consumerDemand = new Demand();
        consumerDemand.setRange(demandRange);
        consumerDemand.setLocationKey(locationKey);
        consumerDemand.addCriterion("test");

        DemandProcessor.locationOperations = new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws DataSourceException {
                assertEquals(locationKey, key);
                return consumerLocation;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, int limit) {
                assertEquals(DemandProcessor._baseOperations.getPersistenceManager(), pm);
                assertEquals(location, consumerLocation);
                assertEquals(demandRange, range);
                List<Location> locations = new ArrayList<Location>();
                locations.add(consumerLocation);
                return locations;
            }
        };

        final Long storeKey = 12345L;
        final Store targetedStore = new Store();
        targetedStore.setKey(storeKey);

        DemandProcessor.storeOperations = new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, List<Location> locations, int limit) {
                assertEquals(DemandProcessor._baseOperations.getPersistenceManager(), pm);
                assertNotNull(locations);
                assertEquals(1, locations.size());
                assertEquals(consumerLocation, locations.get(0));
                List<Store> stores = new ArrayList<Store>();
                stores.add(targetedStore);
                return stores;
            }
        };

        final Retailer selectedRetailer = new Retailer();
        selectedRetailer.addCriterion("test");

        DemandProcessor.retailerOperations = new RetailerOperations() {
            @Override
            public List<Retailer> getRetailers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(DemandProcessor._baseOperations.getPersistenceManager(), pm);
                assertEquals(Retailer.STORE_KEY, key);
                assertEquals(storeKey, (Long) value);
                List<Retailer> retailers = new ArrayList<Retailer>();
                retailers.add(selectedRetailer);
                return retailers;
            }
        };

        List<Retailer> retailers = DemandProcessor.identifyRetailers(DemandProcessor._baseOperations.getPersistenceManager(), consumerDemand);
        assertNotNull(retailers);
        assertEquals(1, retailers.size());
        assertEquals(selectedRetailer, retailers.get(0));
    }

    @Test
    public void testIdentifyRetailersWithEmployeeWithExpectedTagsII() throws DataSourceException {
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

        DemandProcessor.locationOperations = new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws DataSourceException {
                assertEquals(locationKey, key);
                return consumerLocation;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, int limit) {
                assertEquals(DemandProcessor._baseOperations.getPersistenceManager(), pm);
                assertEquals(location, consumerLocation);
                assertEquals(demandRange, range);
                List<Location> locations = new ArrayList<Location>();
                locations.add(consumerLocation);
                return locations;
            }
        };

        final Long storeKey = 12345L;
        final Store targetedStore = new Store();
        targetedStore.setKey(storeKey);

        DemandProcessor.storeOperations = new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, List<Location> locations, int limit) {
                assertEquals(DemandProcessor._baseOperations.getPersistenceManager(), pm);
                assertNotNull(locations);
                assertEquals(1, locations.size());
                assertEquals(consumerLocation, locations.get(0));
                List<Store> stores = new ArrayList<Store>();
                stores.add(targetedStore);
                return stores;
            }
        };

        final Retailer selectedRetailer = new Retailer();
        selectedRetailer.addCriterion("ich");
        selectedRetailer.addCriterion("ni");
        selectedRetailer.addCriterion("san");
        selectedRetailer.addCriterion("test");

        DemandProcessor.retailerOperations = new RetailerOperations() {
            @Override
            public List<Retailer> getRetailers(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(DemandProcessor._baseOperations.getPersistenceManager(), pm);
                assertEquals(Retailer.STORE_KEY, key);
                assertEquals(storeKey, (Long) value);
                List<Retailer> retailers = new ArrayList<Retailer>();
                retailers.add(selectedRetailer);
                return retailers;
            }
        };

        List<Retailer> retailers = DemandProcessor.identifyRetailers(DemandProcessor._baseOperations.getPersistenceManager(), consumerDemand);
        assertNotNull(retailers);
        assertEquals(1, retailers.size());
        assertEquals(selectedRetailer, retailers.get(0));
    }

    @Test
    @SuppressWarnings("serial")
    public void testProcessOneDemand() throws DataSourceException {
        final Long locationKey = 12345L;
        final Location consumerLocation = new Location();
        consumerLocation.setKey(locationKey);

        final Long demandKey = 67890L;
        final Double demandRange = 25.75D;
        final Demand consumerDemand = new Demand();
        consumerDemand.addCriterion("test");
        consumerDemand.setKey(demandKey);
        consumerDemand.setLocationKey(locationKey);
        consumerDemand.setRange(demandRange);
        consumerDemand.setState(State.published);

        DemandProcessor.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(consumerKey);
                return consumerDemand;
            }
        };

        DemandProcessor.locationOperations = new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws DataSourceException {
                assertEquals(locationKey, key);
                return consumerLocation;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, int limit) {
                return null;
            }
        };

        final Long storeKey = 12345L;
        final Store targetedStore = new Store();
        targetedStore.setKey(storeKey);

        DemandProcessor.storeOperations = new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, List<Location> locations, int limit) {
                List<Store> stores = new ArrayList<Store>();
                stores.add(targetedStore);
                return stores;
            }
        };

        final String retailerId = "Ryan";
        final Retailer selectedRetailer = new Retailer();
        selectedRetailer.setTwitterId(retailerId);
        selectedRetailer.addCriterion("test");

        DemandProcessor.retailerOperations = new RetailerOperations() {
            @Override
            public List<Retailer> getRetailers(PersistenceManager pm, String key, Object value, int limit) {
                List<Retailer> retailers = new ArrayList<Retailer>();
                retailers.add(selectedRetailer);
                return retailers;
            }
        };

        final Twitter mockTwitterAccount = (new Twitter() {
            @Override
            public DirectMessage sendDirectMessage(String id, String text) throws TwitterException {
                assertEquals(retailerId.toString(), id);
                assertTrue(text.contains(consumerDemand.getKey().toString()));
                assertTrue(text.contains("test"));
                return null;
            }
        });
        MockTwitterConnector.injectMockTwitterAccount(mockTwitterAccount);

        DemandProcessor.process(demandKey);

        assertTrue(DemandProcessor._baseOperations.getPersistenceManager().isClosed());

        MockTwitterConnector.restoreTwitterConnector(mockTwitterAccount, null);
    }

    @Test
    public void testProcessOneDemandWithTroubleAccessingDatabase() throws DataSourceException {
        final Long locationKey = 12345L;
        final Location consumerLocation = new Location();
        consumerLocation.setKey(locationKey);

        final Long demandKey = 67890L;
        final Double demandRange = 25.75D;
        final Demand consumerDemand = new Demand();
        consumerDemand.addCriterion("test");
        consumerDemand.setKey(demandKey);
        consumerDemand.setLocationKey(locationKey);
        consumerDemand.setRange(demandRange);
        consumerDemand.setState(State.published);

        DemandProcessor.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(consumerKey);
                return consumerDemand;
            }
        };

        DemandProcessor.locationOperations = new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws DataSourceException {
                throw new DataSourceException("done in purpose");
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, int limit) {
                return null;
            }
        };

        DemandProcessor.process(demandKey);

        assertTrue(DemandProcessor._baseOperations.getPersistenceManager().isClosed());
        TwitterConnector.getTwetailerAccount();
    }

    @Test
    @SuppressWarnings("serial")
    public void testProcessOneDemandWithTwitterTrouble() throws DataSourceException {
        final Long locationKey = 12345L;
        final Location consumerLocation = new Location();
        consumerLocation.setKey(locationKey);

        final Long demandKey = 67890L;
        final Double demandRange = 25.75D;
        final Demand consumerDemand = new Demand();
        consumerDemand.addCriterion("test");
        consumerDemand.setKey(demandKey);
        consumerDemand.setLocationKey(locationKey);
        consumerDemand.setRange(demandRange);
        consumerDemand.setState(State.published);

        DemandProcessor.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(consumerKey);
                return consumerDemand;
            }
        };

        DemandProcessor.locationOperations = new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                assertEquals(locationKey, key);
                return null;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, int limit) {
                return null;
            }
        };

        final Long storeKey = 12345L;
        final Store targetedStore = new Store();
        targetedStore.setKey(storeKey);

        DemandProcessor.storeOperations = new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, List<Location> locations, int limit) {
                List<Store> stores = new ArrayList<Store>();
                stores.add(targetedStore);
                return stores;
            }
        };

        final String retailerId = "Ryan";
        final Retailer selectedRetailer = new Retailer();
        selectedRetailer.setTwitterId(retailerId);
        selectedRetailer.addCriterion("test");

        DemandProcessor.retailerOperations = new RetailerOperations() {
            @Override
            public List<Retailer> getRetailers(PersistenceManager pm, String key, Object value, int limit) {
                List<Retailer> retailers = new ArrayList<Retailer>();
                retailers.add(selectedRetailer);
                return retailers;
            }
        };

        final Twitter mockTwitterAccount = (new Twitter() {
            @Override
            public DirectMessage sendDirectMessage(String id, String text) throws TwitterException {
                throw new TwitterException("done in purpose");
            }
        });
        MockTwitterConnector.injectMockTwitterAccount(mockTwitterAccount);

        DemandProcessor.process(demandKey);

        assertTrue(DemandProcessor._baseOperations.getPersistenceManager().isClosed());

        MockTwitterConnector.restoreTwitterConnector(mockTwitterAccount, null);
    }

    @Test
    public void testProcessOneDemandInIncorrectState() throws DataSourceException {
        final Long demandKey = 67890L;
        final Long locationKey = 12345L;
        final Double demandRange = 25.75D;
        final Demand consumerDemand = new Demand();
        consumerDemand.addCriterion("test");
        consumerDemand.setKey(demandKey);
        consumerDemand.setLocationKey(locationKey);
        consumerDemand.setRange(demandRange);
        consumerDemand.setState(State.invalid); // Not published

        DemandProcessor.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(consumerKey);
                return consumerDemand;
            }
        };

        DemandProcessor.process(demandKey);

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(DemandProcessor._baseOperations.getPersistenceManager().isClosed());
    }
}
