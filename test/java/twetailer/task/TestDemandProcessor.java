package twetailer.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.DataSourceException;
import twetailer.connector.BaseConnector;
import twetailer.connector.MockTwitterConnector;
import twetailer.connector.TwitterConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.BaseOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.MockAppEngineEnvironment;
import twetailer.dao.MockPersistenceManager;
import twetailer.dao.ProposalOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dao.StoreOperations;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.Proposal;
import twetailer.dto.SaleAssociate;
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
        DemandProcessor._baseOperations = new BaseOperations();
        DemandProcessor.demandOperations = DemandProcessor._baseOperations.getDemandOperations();
        DemandProcessor.locationOperations = DemandProcessor._baseOperations.getLocationOperations();
        DemandProcessor.proposalOperations = DemandProcessor._baseOperations.getProposalOperations();
        DemandProcessor.saleAssociateOperations = DemandProcessor._baseOperations.getSaleAssociateOperations();
        DemandProcessor.storeOperations = DemandProcessor._baseOperations.getStoreOperations();

        BaseConnector.resetLastCommunicationInSimulatedMode();
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
    public void testIdentifySaleAssociatesNoLocationAround() throws DataSourceException {
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

        List<SaleAssociate> saleAssociates = DemandProcessor.identifySaleAssociates(DemandProcessor._baseOperations.getPersistenceManager(), consumerDemand);
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

        List<SaleAssociate> saleAssociates = DemandProcessor.identifySaleAssociates(DemandProcessor._baseOperations.getPersistenceManager(), consumerDemand);
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

        DemandProcessor.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(DemandProcessor._baseOperations.getPersistenceManager(), pm);
                assertEquals(SaleAssociate.STORE_KEY, key);
                assertEquals(storeKey, (Long) value);
                return new ArrayList<SaleAssociate>();
            }
        };

        List<SaleAssociate> saleAssociates = DemandProcessor.identifySaleAssociates(DemandProcessor._baseOperations.getPersistenceManager(), consumerDemand);
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

        final SaleAssociate selectedSaleAssociate = new SaleAssociate() {
            @Override
            public List<String> getCriteria() {
                return null;
            }
        };

        DemandProcessor.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(DemandProcessor._baseOperations.getPersistenceManager(), pm);
                assertEquals(SaleAssociate.STORE_KEY, key);
                assertEquals(storeKey, (Long) value);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                saleAssociates.add(selectedSaleAssociate);
                return saleAssociates;
            }
        };

        List<SaleAssociate> saleAssociates = DemandProcessor.identifySaleAssociates(DemandProcessor._baseOperations.getPersistenceManager(), consumerDemand);
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

        final SaleAssociate selectedSaleAssociate = new SaleAssociate();

        DemandProcessor.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(DemandProcessor._baseOperations.getPersistenceManager(), pm);
                assertEquals(SaleAssociate.STORE_KEY, key);
                assertEquals(storeKey, (Long) value);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                saleAssociates.add(selectedSaleAssociate);
                return saleAssociates;
            }
        };

        List<SaleAssociate> saleAssociates = DemandProcessor.identifySaleAssociates(DemandProcessor._baseOperations.getPersistenceManager(), consumerDemand);
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

        final SaleAssociate selectedSaleAssociate = new SaleAssociate();
        selectedSaleAssociate.addCriterion("test");

        DemandProcessor.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(DemandProcessor._baseOperations.getPersistenceManager(), pm);
                assertEquals(SaleAssociate.STORE_KEY, key);
                assertEquals(storeKey, (Long) value);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                saleAssociates.add(selectedSaleAssociate);
                return saleAssociates;
            }
        };

        List<SaleAssociate> saleAssociates = DemandProcessor.identifySaleAssociates(DemandProcessor._baseOperations.getPersistenceManager(), consumerDemand);
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

        final SaleAssociate selectedSaleAssociate = new SaleAssociate();
        selectedSaleAssociate.addCriterion("ich");
        selectedSaleAssociate.addCriterion("ni");
        selectedSaleAssociate.addCriterion("san");
        selectedSaleAssociate.addCriterion("test");

        DemandProcessor.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(DemandProcessor._baseOperations.getPersistenceManager(), pm);
                assertEquals(SaleAssociate.STORE_KEY, key);
                assertEquals(storeKey, (Long) value);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                saleAssociates.add(selectedSaleAssociate);
                return saleAssociates;
            }
        };

        List<SaleAssociate> saleAssociates = DemandProcessor.identifySaleAssociates(DemandProcessor._baseOperations.getPersistenceManager(), consumerDemand);
        assertNotNull(saleAssociates);
        assertEquals(1, saleAssociates.size());
        assertEquals(selectedSaleAssociate, saleAssociates.get(0));
    }

    @Test
    public void testProcessOneDemandI() throws DataSourceException {
        final Long locationKey = 12345L;
        final Location consumerLocation = new Location();
        consumerLocation.setKey(locationKey);

        final Long demandKey = 67890L;
        final Double demandRange = 25.75D;
        final Demand consumerDemand = new Demand();
        consumerDemand.addCriterion("test");
        consumerDemand.setKey(demandKey);
        consumerDemand.setLocationKey(locationKey);
        // consumerDemand.setQuantity(1L); // Default quantity
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

        DemandProcessor.proposalOperations = new ProposalOperations() {
            @Override
            public List<Proposal> getProposals(PersistenceManager pm, String attribute, Object value, int limit) {
                return new ArrayList<Proposal>();
            }
        };

        DemandProcessor.process(demandKey);

        assertTrue(DemandProcessor._baseOperations.getPersistenceManager().isClosed());

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

        final Long demandKey = 67890L;
        final Double demandRange = 25.75D;
        final Demand consumerDemand = new Demand();
        consumerDemand.addCriterion("test");
        consumerDemand.setKey(demandKey);
        consumerDemand.setLocationKey(locationKey);
        consumerDemand.setRange(demandRange);
        consumerDemand.setQuantity(123L);
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

        DemandProcessor.proposalOperations = new ProposalOperations() {
            @Override
            public List<Proposal> getProposals(PersistenceManager pm, String attribute, Object value, int limit) {
                return new ArrayList<Proposal>();
            }
        };

        DemandProcessor.process(demandKey);

        assertTrue(DemandProcessor._baseOperations.getPersistenceManager().isClosed());

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(consumerDemand.getKey().toString()));
        assertTrue(sentText.contains("test"));
    }

    @Test
    public void testProcessOneDemandForTheRobot() throws Exception {
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

        final SaleAssociate selectedSaleAssociate = new SaleAssociate();
        selectedSaleAssociate.setName(RobotResponder.ROBOT_NAME);
        selectedSaleAssociate.setPreferredConnection(Source.simulated);
        selectedSaleAssociate.addCriterion("test");

        DemandProcessor.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                saleAssociates.add(selectedSaleAssociate);
                return saleAssociates;
            }
        };

        DemandProcessor.proposalOperations = new ProposalOperations() {
            @Override
            public List<Proposal> getProposals(PersistenceManager pm, String attribute, Object value, int limit) {
                return new ArrayList<Proposal>();
            }
        };

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        MockAppEngineEnvironment appEnv = new MockAppEngineEnvironment();
        appEnv.setUp();

        DemandProcessor.process(demandKey);

        appEnv.tearDown();
        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(DemandProcessor._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    @SuppressWarnings("serial")
    public void testProcessOneDemandAlreadyProposed() throws DataSourceException {
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

        final String saleAssociateId = "Ryan";
        final Long saleAssociateKey = 56478L;
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

        DemandProcessor.proposalOperations = new ProposalOperations() {
            @Override
            public List<Proposal> getProposals(PersistenceManager pm, String attribute, Object value, int limit) {
                Proposal badProposal = new Proposal();
                badProposal.setOwnerKey(saleAssociateKey + 12325L);
                Proposal goodProposal = new Proposal();
                goodProposal.setOwnerKey(saleAssociateKey);
                List<Proposal> proposals = new ArrayList<Proposal>();
                proposals.add(badProposal);
                proposals.add(goodProposal);
                return proposals;
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

        DemandProcessor.proposalOperations = new ProposalOperations() {
            @Override
            public List<Proposal> getProposals(PersistenceManager pm, String attribute, Object value, int limit) {
                return new ArrayList<Proposal>();
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

        final String saleAssociateId = "Ryan";
        final SaleAssociate selectedSaleAssociate = new SaleAssociate();
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

        DemandProcessor.proposalOperations = new ProposalOperations() {
            @Override
            public List<Proposal> getProposals(PersistenceManager pm, String attribute, Object value, int limit) {
                return new ArrayList<Proposal>();
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

    @Test
    public void testProcessBatchI() throws DataSourceException {
        DemandProcessor.demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> parameters, int limit) {
                return new ArrayList<Demand>();
            }
        };

        DemandProcessor.batchProcess();

        assertTrue(DemandProcessor._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessBatchII() throws Exception {
        DemandProcessor.demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> parameters, int limit) {
                Demand demand = new Demand();
                demand.setKey(12345L);
                List<Demand> demands = new ArrayList<Demand>();
                demands.add(demand);
                return demands;
            }
        };

        // App Engine Environment mock
        MockAppEngineEnvironment appEnv = new MockAppEngineEnvironment();

        appEnv.setUp();
        DemandProcessor.batchProcess();
        appEnv.tearDown();

        assertTrue(DemandProcessor._baseOperations.getPersistenceManager().isClosed());
    }
}
