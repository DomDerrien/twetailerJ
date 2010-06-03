package twetailer.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import twetailer.dao.MockBaseOperations;
import twetailer.dao.ProposalOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dao.SettingsOperations;
import twetailer.dao.StoreOperations;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Proposal;
import twetailer.dto.SaleAssociate;
import twetailer.dto.Settings;
import twetailer.dto.Store;
import twetailer.validator.CommandSettings.State;
import twitter4j.TwitterException;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import domderrien.i18n.LabelExtractor;

public class TestRobotResponder {

    private static LocalServiceTestHelper  helper;

    @BeforeClass
    public static void setUpBeforeClass() {
        // RobotResponder.setLogger(new MockLogger("test", null));
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());;
    }


    @Before
    public void setUp() throws Exception {
        // Install the mocks
        helper.setUp();
        RobotResponder._baseOperations = new MockBaseOperations();

        // Be sure to start with a clean message stack
        BaseConnector.resetLastCommunicationInSimulatedMode();
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();

        RobotResponder._baseOperations = new BaseOperations();
        RobotResponder.demandOperations = RobotResponder._baseOperations.getDemandOperations();
        RobotResponder.consumerOperations = RobotResponder._baseOperations.getConsumerOperations();
        RobotResponder.saleAssociateOperations = RobotResponder._baseOperations.getSaleAssociateOperations();
        RobotResponder.proposalOperations = RobotResponder._baseOperations.getProposalOperations();
        RobotResponder.settingsOperations = RobotResponder._baseOperations.getSettingsOperations();
        RobotResponder.storeOperations = RobotResponder._baseOperations.getStoreOperations();
    }

    @Test
    public void testContructor() {
        new RobotResponder();
    }

    @Test(expected=RuntimeException.class)
    public void testProcessWithFailure() throws DataSourceException {
        RobotResponder.settingsOperations = new SettingsOperations() {
            @Override
            public Settings getSettings(PersistenceManager pm) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        };

        RobotResponder.processDemand(12345L);
    }

    final Long demandKey = 111L;
    final Long robotKey = 2222L;
    final Long locationKey = 333L;
    final Long storeKey = 444L;
    final Long consumerKey = 555L;
    final Long proposalKey = 666L;

    @Test
    public void testProcessDemandI() throws TwitterException, DataSourceException {
        RobotResponder.settingsOperations = new SettingsOperations() {
            @Override
            public Settings getSettings(PersistenceManager pm) {
                return new Settings();
            }
        };

        RobotResponder.processDemand(demandKey);

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(((MockBaseOperations) RobotResponder._baseOperations).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessDemandII() throws TwitterException, DataSourceException {
        RobotResponder.settingsOperations = new SettingsOperations() {
            @Override
            public Settings getSettings(PersistenceManager pm) {
                Settings settings = new Settings();
                settings.setRobotSaleAssociateKey(robotKey);
                return settings;
            }
        };
        RobotResponder.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                assertEquals(robotKey, key);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(robotKey);
                saleAssociate.setStoreKey(storeKey);
                return saleAssociate;
            }
        };
        RobotResponder.storeOperations = new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) {
                assertEquals(storeKey, key);
                Store store = new Store();
                store.setKey(storeKey);
                store.setLocationKey(locationKey);
                return store;
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
        assertTrue(((MockBaseOperations) RobotResponder._baseOperations).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessDemandIII() throws Exception {
        RobotResponder.settingsOperations = new SettingsOperations() {
            @Override
            public Settings getSettings(PersistenceManager pm) {
                Settings settings = new Settings();
                settings.setRobotSaleAssociateKey(robotKey);
                return settings;
            }
        };
        RobotResponder.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                assertEquals(robotKey, key);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(robotKey);
                saleAssociate.setStoreKey(storeKey);
                return saleAssociate;
            }
        };
        RobotResponder.storeOperations = new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) {
                assertEquals(storeKey, key);
                Store store = new Store();
                store.setKey(storeKey);
                store.setLocationKey(locationKey);
                return store;
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
                    message.append(tag).append(Command.SPACE);
                }
                assertEquals(LabelExtractor.get("rr_robot_automatic_proposition", Locale.ENGLISH).trim(), message.toString().trim());
                proposal.setKey(proposalKey);
                return proposal;
            }
        };

        RobotResponder.processDemand(demandKey);

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(((MockBaseOperations) RobotResponder._baseOperations).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testGetRobotConsumerKey() throws DataSourceException {
        final Long robotKey = 12345L;
        final Settings settings = new Settings();
        settings.setRobotConsumerKey(robotKey);
        RobotResponder.settingsOperations = new SettingsOperations() {
            boolean alreadyCalled = false;
            @Override
            public Settings getSettings(PersistenceManager pm) {
                if (alreadyCalled) {
                    fail("Should have been called only once");
                }
                alreadyCalled = true;
                assertNull(pm);
                return settings;
            }
        };
        RobotResponder.setRobotConsumerKey(null); // To be sure to have a fresh behavior
        assertEquals(robotKey, RobotResponder.getRobotConsumerKey(null));
        assertEquals(robotKey, RobotResponder.getRobotConsumerKey(null));
        assertEquals(robotKey, RobotResponder.getRobotConsumerKey(null));
    }
}
