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
import twetailer.InvalidIdentifierException;
import twetailer.connector.BaseConnector;
import twetailer.connector.BaseConnector.Source;
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
import twetailer.task.step.BaseSteps;
import twetailer.validator.CommandSettings.State;
import twitter4j.TwitterException;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import domderrien.i18n.LabelExtractor;
import domderrien.i18n.LabelExtractor.ResourceFileId;
import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonParser;

public class TestRobotResponder {

    private static LocalServiceTestHelper  helper;

    @BeforeClass
    public static void setUpBeforeClass() {
        // RobotResponder.setMockLogger(new MockLogger("test", null));
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    }

    @Before
    public void setUp() throws Exception {
        // Install the mocks
        helper.setUp();

        BaseSteps.resetOperationControllers(true);
        BaseSteps.setMockBaseOperations(new MockBaseOperations());

        // Be sure to start with a clean message stack
        BaseConnector.resetLastCommunicationInSimulatedMode();
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();
    }

    @Test
    public void testContructor() {
        new RobotResponder();
    }

    @Test(expected=RuntimeException.class)
    public void testProcessWithFailure() throws InvalidIdentifierException, DataSourceException {
        BaseSteps.setMockSettingsOperations(new SettingsOperations() {
            @Override
            public Settings getSettings(PersistenceManager pm) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        });

        RobotResponder.processDemand(12345L);
    }

    final Long demandKey = 111L;
    final Long robotKey = 2222L;
    final Long locationKey = 333L;
    final Long storeKey = 444L;
    final Long consumerKey = 555L;
    final Long proposalKey = 666L;

    @Test
    public void testProcessDemandI() throws TwitterException, DataSourceException, InvalidIdentifierException {
        BaseSteps.setMockSettingsOperations(new SettingsOperations() {
            @Override
            public Settings getSettings(PersistenceManager pm) {
                return new Settings();
            }
        });

        RobotResponder.processDemand(demandKey);

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessDemandII() throws TwitterException, DataSourceException, InvalidIdentifierException {
        BaseSteps.setMockSettingsOperations(new SettingsOperations() {
            @Override
            public Settings getSettings(PersistenceManager pm) {
                Settings settings = new Settings();
                settings.setRobotSaleAssociateKey(robotKey);
                return settings;
            }
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                assertEquals(robotKey, key);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(robotKey);
                saleAssociate.setStoreKey(storeKey);
                return saleAssociate;
            }
        });
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) {
                assertEquals(storeKey, key);
                Store store = new Store();
                store.setKey(storeKey);
                store.setLocationKey(locationKey);
                return store;
            }
        });
        BaseSteps.setMockDemandOperations(new DemandOperations() {
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
        });
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                return consumer;
            }
        });

        RobotResponder.processDemand(demandKey);

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessDemandIII() throws Exception {
        BaseSteps.setMockSettingsOperations(new SettingsOperations() {
            @Override
            public Settings getSettings(PersistenceManager pm) {
                Settings settings = new Settings();
                settings.setRobotSaleAssociateKey(robotKey);
                return settings;
            }
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                assertEquals(robotKey, key);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(robotKey);
                saleAssociate.setStoreKey(storeKey);
                return saleAssociate;
            }
        });
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) {
                assertEquals(storeKey, key);
                Store store = new Store();
                store.setKey(storeKey);
                store.setLocationKey(locationKey);
                return store;
            }
        });
        BaseSteps.setMockDemandOperations(new DemandOperations() {
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
        });
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                return consumer;
            }
        });
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal createProposal(PersistenceManager pm, Proposal proposal) {
                assertEquals(demandKey, proposal.getDemandKey());
                assertEquals(storeKey, proposal.getStoreKey());
                // "long_" because Source.mail is the default Demand source
                // "core_" because Demand has no default hash tag
                String jsonBag = LabelExtractor.get(ResourceFileId.fourth, "long_" + "core_" + "robot_automatedResponse", Locale.ENGLISH);
                try {
                    assertEquals(new Proposal(new JsonParser(jsonBag).getJsonObject()).getSerializedCriteria(), proposal.getSerializedCriteria());
                    proposal.setKey(proposalKey);
                    return proposal;
                }
                catch(JsonException ex) {
                    return null;
                }
            }
        });

        RobotResponder.processDemand(demandKey);

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testGetRobotConsumerKey() throws DataSourceException {
        final Long robotKey = 12345L;
        final Settings settings = new Settings();
        settings.setRobotConsumerKey(robotKey);
        BaseSteps.setMockSettingsOperations(new SettingsOperations() {
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
        });
        RobotResponder.setRobotConsumerKey(null); // To be sure to have a fresh behavior
        assertEquals(robotKey, RobotResponder.getRobotConsumerKey(null));
        assertEquals(robotKey, RobotResponder.getRobotConsumerKey(null));
        assertEquals(robotKey, RobotResponder.getRobotConsumerKey(null));
    }
}
