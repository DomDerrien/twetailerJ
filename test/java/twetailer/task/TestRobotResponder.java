package twetailer.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.DataSourceException;
import twetailer.connector.BaseConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.MockBaseOperations;
import twetailer.dao.ProposalOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dao.StoreOperations;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.Proposal;
import twetailer.dto.SaleAssociate;
import twetailer.dto.Store;
import twetailer.validator.CommandSettings.State;
import twitter4j.TwitterException;

import com.google.apphosting.api.MockAppEngineEnvironment;

import domderrien.i18n.LabelExtractor;

public class TestRobotResponder {

    private static MockAppEngineEnvironment mockAppEngineEnvironment;

    @BeforeClass
    public static void setUpBeforeClass() {
        mockAppEngineEnvironment = new MockAppEngineEnvironment();
    }

    @Before
    public void setUp() throws Exception {
        // Install the mocks
        mockAppEngineEnvironment.setUp();
        RobotResponder._baseOperations = new MockBaseOperations();

        // Be sure to start with a clean message stack
        BaseConnector.resetLastCommunicationInSimulatedMode();
    }

    @After
    public void tearDown() throws Exception {
        mockAppEngineEnvironment.tearDown();
        RobotResponder._baseOperations = new BaseOperations();
        RobotResponder.demandOperations = RobotResponder._baseOperations.getDemandOperations();
        RobotResponder.consumerOperations = RobotResponder._baseOperations.getConsumerOperations();
        RobotResponder.locationOperations = RobotResponder._baseOperations.getLocationOperations();
        RobotResponder.saleAssociateOperations = RobotResponder._baseOperations.getSaleAssociateOperations();
        RobotResponder.proposalOperations = RobotResponder._baseOperations.getProposalOperations();
        RobotResponder.storeOperations = RobotResponder._baseOperations.getStoreOperations();
    }

    @Test
    public void testContructor() {
        new RobotResponder();
    }

    final Long demandKey = 111L;
    final Long saleAssociateKey = 222L;
    final Long locationKey = 333L;
    final Long storeKey = 444L;
    final Long consumerKey = 555L;
    final Long proposalKey = 666L;

    @Test
    public void testProcessDemandI() throws TwitterException, DataSourceException {
        RobotResponder.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                return saleAssociates;
            }
        };

        RobotResponder.processDemand(demandKey);

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(RobotResponder._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessDemandII() throws TwitterException, DataSourceException {
        RobotResponder.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setStoreKey(storeKey);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                saleAssociates.add(saleAssociate);
                return saleAssociates;
            }
        };
        RobotResponder.locationOperations = new LocationOperations() {
            @Override
            public List<Location> getLocations(PersistenceManager pm, String postalCode, String countryCode) {
                assertEquals(RobotResponder.ROBOT_POSTAL_CODE, postalCode);
                assertEquals(RobotResponder.ROBOT_COUNTRY_CODE, countryCode);
                Location location = new Location();
                location.setKey(locationKey);
                location.setPostalCode(RobotResponder.ROBOT_POSTAL_CODE);
                location.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
                List<Location> locations = new ArrayList<Location>();
                locations.add(location);
                return locations;
            }
        };
        RobotResponder.storeOperations = new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Store.LOCATION_KEY, key);
                assertEquals(locationKey, (Long) value);
                Store store = new Store();
                store.setKey(storeKey);
                store.setLocationKey(locationKey);
                List<Store> stores = new ArrayList<Store>();
                stores.add(store);
                return stores;
            }
        };
        RobotResponder.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand();
                demand.setOwnerKey(consumerKey);
                demand.setSource(Source.simulated);
                demand.setState(State.invalid);
                return demand;
            }
        };
        RobotResponder.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                return consumer;
            }
        };

        RobotResponder.processDemand(demandKey);

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(RobotResponder._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessDemandIII() throws Exception {
        RobotResponder.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setStoreKey(storeKey);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                saleAssociates.add(saleAssociate);
                return saleAssociates;
            }
        };
        RobotResponder.locationOperations = new LocationOperations() {
            @Override
            public List<Location> getLocations(PersistenceManager pm, String postalCode, String countryCode) {
                assertEquals(RobotResponder.ROBOT_POSTAL_CODE, postalCode);
                assertEquals(RobotResponder.ROBOT_COUNTRY_CODE, countryCode);
                Location location = new Location();
                location.setKey(locationKey);
                location.setPostalCode(RobotResponder.ROBOT_POSTAL_CODE);
                location.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
                List<Location> locations = new ArrayList<Location>();
                locations.add(location);
                return locations;
            }
        };
        RobotResponder.storeOperations = new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Store.LOCATION_KEY, key);
                assertEquals(locationKey, (Long) value);
                Store store = new Store();
                store.setKey(storeKey);
                store.setLocationKey(locationKey);
                List<Store> stores = new ArrayList<Store>();
                stores.add(store);
                return stores;
            }
        };
        RobotResponder.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand();
                demand.setOwnerKey(consumerKey);
                demand.setSource(Source.simulated);
                demand.setState(State.published);
                return demand;
            }
        };
        RobotResponder.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                return consumer;
            }
        };
        RobotResponder.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal createProposal(PersistenceManager pm, Proposal proposal) {
                assertEquals(demandKey, proposal.getDemandKey());
                assertEquals(storeKey, proposal.getStoreKey());
                StringBuilder message = new StringBuilder();
                for (String tag : proposal.getCriteria()) {
                    message.append(tag).append(" ");
                }
                assertEquals(LabelExtractor.get("rr_robot_automatic_proposition", Locale.ENGLISH).trim(), message.toString().trim());
                proposal.setKey(proposalKey);
                return proposal;
            }
        };

        RobotResponder.processDemand(demandKey);

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(RobotResponder._baseOperations.getPersistenceManager().isClosed());
    }
}
