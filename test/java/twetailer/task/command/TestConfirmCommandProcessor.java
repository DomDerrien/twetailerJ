package twetailer.task.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import javax.jdo.MockPersistenceManager;
import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.connector.BaseConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.MockBaseOperations;
import twetailer.dao.ProposalOperations;
import twetailer.dao.RawCommandOperations;
import twetailer.dao.RegistrarOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dao.SettingsOperations;
import twetailer.dao.StoreOperations;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.Proposal;
import twetailer.dto.RawCommand;
import twetailer.dto.Registrar;
import twetailer.dto.SaleAssociate;
import twetailer.dto.Settings;
import twetailer.dto.Store;
import twetailer.task.CommandProcessor;
import twetailer.task.RobotResponder;
import twetailer.task.TestCommandProcessor;
import twetailer.task.step.BaseSteps;
import twetailer.validator.LocaleValidator;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.State;
import twitter4j.TwitterException;

import com.google.appengine.api.taskqueue.MockQueue;
import com.google.appengine.api.taskqueue.Queue;

import domderrien.i18n.DateUtils;
import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class TestConfirmCommandProcessor {

    @BeforeClass
    public static void setUpBeforeClass() {
        TestCommandProcessor.setUpBeforeClass();
    }

    @Before
    public void setUp() throws Exception {
        new TestCommandProcessor().setUp();
    }

    @After
    public void tearDown() throws Exception {
        new TestCommandProcessor().tearDown();
    }

    @Test
    public void testConstructor() {
        new ConfirmCommandProcessor();
    }

    @Test
    public void testProcessCommandConfirmI() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 4444L;
        final Long demandKey = 5555L;
        final Long saleAssociateKey = 6666L;
        final Long storeKey = 7777L;
        final Long locationKey = 87654L;

        // ProposalOperations mock
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setDemandKey(demandKey);
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setState(State.published);
                proposal.setSource(Source.simulated);
                proposal.setStoreKey(storeKey);
                proposal.setDueDate(DateUtils.getNowDate());
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertEquals(State.confirmed, proposal.getState());
                return proposal;
            }
        });
        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.addCriterion("test");
                demand.addProposalKey(proposalKey);
                demand.setState(State.published);
                demand.setLocationKey(locationKey);
                demand.setDueDate(DateUtils.getNowDate());
                demand.setSource(Source.simulated);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(State.confirmed, demand.getState());
                return demand;
            }
        });
        // LocationOperations mock
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                assertEquals(locationKey, key);
                Location location = new Location();
                location.setKey(proposalKey);
                location.setCountryCode(LocaleValidator.DEFAULT_COUNTRY_CODE);
                location.setPostalCode("H0H0H0");
                return location;
            }
        });
        // SaleAssociateOperations mock
        final Long saConsumerRecordKey = 76325L;
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                assertEquals(saleAssociateKey, key);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setConsumerKey(saConsumerRecordKey);
                return saleAssociate;
            }
        });
        // ConsumerOperations mock
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(saConsumerRecordKey, key);
                Consumer saConsumerRecord = new Consumer();
                saConsumerRecord.setKey(saConsumerRecordKey);
                saConsumerRecord.setPreferredConnection(Source.simulated);
                return saConsumerRecord;
            }
        });
        // StoreOperations mock
        final String storeName = "store name";
        final Long registrarKey = 7654354L;
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) {
                assertEquals(storeKey, key);
                Store store = new Store();
                store.setKey(storeKey);
                store.setName(storeName);
                store.setLocationKey(locationKey);
                store.setRegistrarKey(registrarKey);
                return store;
            }
        });
        // RegistrarOperations mock
        BaseSteps.setMockRegistrarOperations(new RegistrarOperations() {
            @Override
            public Registrar getRegistrar(PersistenceManager pm, Long key) {
                assertEquals(registrarKey, key);
                Registrar registrar = new Registrar();
                registrar.setKey(registrarKey);
                registrar.setName(storeName);
                return registrar;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.confirm.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(saConsumerRecordKey);
        consumer.setSaleAssociateKey(saleAssociateKey);

        // Settings mock for the RobotResponder
        BaseSteps.setMockSettingsOperations(new SettingsOperations() {
            @Override
            public Settings getSettings(PersistenceManager pm) {
                return new Settings();
            }
        });

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        /* TODO: Re-enable when long_core_* messages are in!
        String sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1);
        assertNotNull(sentText); // Informs the saleAssociate
        assertTrue(sentText.contains(proposalKey.toString()));
        assertTrue(sentText.contains(demandKey.toString()));
        sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText); // Informs the consumer
        assertTrue(sentText.contains(proposalKey.toString()));
        assertTrue(sentText.contains(demandKey.toString()));
        assertTrue(sentText.contains(storeKey.toString()));
        assertTrue(sentText.contains(storeName));
        assertTrue(sentText.contains("test"));
        */
    }

    @Test
    public void testProcessCommandConfirmII() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 4444L;
        final Long consumerKey = 659873L;
        final Long saleAssociateKey = 542476L;

        // ProposalOperations mock
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws InvalidIdentifierException {
                assertEquals(proposalKey, key);
                throw new InvalidIdentifierException("Done in purpose");
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.confirm.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setSaleAssociateKey(saleAssociateKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_confirm_invalid_proposal_id", Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandConfirmIII() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 4444L;
        final Long demandKey = 5555L;
        final Long consumerKey = 659873L;
        final Long saleAssociateKey = 542476L;
        final Long storeKey = 7777L;

        // ProposalOperations mock
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setDemandKey(demandKey);
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setState(State.published);
                proposal.setStoreKey(storeKey);
                proposal.setSource(Source.api);
                return proposal;
            }
        });
        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) throws InvalidIdentifierException {
                throw new InvalidIdentifierException("Done in purpose");
            }
        });
        // SaleAssociateOperations mock
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setConsumerKey(consumerKey);
                return saleAssociate;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.confirm.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setSaleAssociateKey(saleAssociateKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_confirm_invalid_proposal_id", Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandConfirmIV() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 4444L;
        final Long demandKey = 5555L;
        final Long consumerKey = 659873L;
        final Long saleAssociateKey = 542476L;
        final Long storeKey = 7777L;

        // ProposalOperations mock
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setDemandKey(demandKey);
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setState(State.published);
                proposal.setStoreKey(storeKey);
                proposal.setSource(Source.api);
                return proposal;
            }
        });
        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.addCriterion("test");
                demand.addProposalKey(proposalKey);
                demand.setState(State.invalid); // Blocks the confirmation
                return demand;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.confirm.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setSaleAssociateKey(saleAssociateKey);

        CommandProcessor.processCommand(new MockPersistenceManager(),consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText); // Informs the consumer
        assertTrue(sentText.contains(proposalKey.toString()));
        assertTrue(sentText.contains(State.invalid.toString()));
    }

    @Test
    @Ignore
    public void testConfirmCommandByRobot() throws DataSourceException, ClientException {
        final Long proposalKey = 4444L;
        final Long demandKey = 5555L;
        final Long saleAssociateKey = 12345L;
        final Long storeKey = 7777L;
        final Long consumerKey = 65343L;
        final Long locationKey = 54376543232L;

        // ProposalOperations mock
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setDemandKey(demandKey);
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setState(State.published);
                proposal.setStoreKey(storeKey);
                return proposal;
            }
        });
        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.addCriterion("test");
                demand.addProposalKey(proposalKey);
                demand.setState(State.published);
                return demand;
            }
        });
        // DemandOperations mock
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand createRawCommand(PersistenceManager pm, RawCommand command) {
                assertEquals(Source.robot, command.getSource());
                command.setKey(67890L);
                return command;
            }
        });
        // LocationOperations mock
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                assertEquals(locationKey, key);
                Location location = new Location();
                location.setKey(proposalKey);
                location.setCountryCode(LocaleValidator.DEFAULT_COUNTRY_CODE);
                location.setPostalCode("H0H0H0");
                return location;
            }
        });
        // StoreOperations mock
        final String storeName = "store name";
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) {
                assertEquals(storeKey, key);
                Store store = new Store();
                store.setKey(storeKey);
                store.setName(storeName);
                store.setLocationKey(locationKey);
                return store;
            }
        });
        // SaleAssociateOperations mock
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setConsumerKey(consumerKey);
                return saleAssociate;
            }
        });
        // CommandProcessor mock
        final MockQueue queue = new MockQueue();
        BaseSteps.setMockBaseOperations(new MockBaseOperations() {
            @Override
            public Queue getQueue() {
                return queue;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.confirm.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setSaleAssociateKey(saleAssociateKey);

        RobotResponder.setRobotSaleAssociateKey(saleAssociateKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);
        assertEquals(1, queue.getHistory().size());
    }
}
