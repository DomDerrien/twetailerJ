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
import org.junit.Test;

import twetailer.InvalidIdentifierException;
import twetailer.connector.BaseConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.ProposalOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dao.StoreOperations;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Proposal;
import twetailer.dto.RawCommand;
import twetailer.dto.SaleAssociate;
import twetailer.dto.Store;
import twetailer.task.CommandProcessor;
import twetailer.task.TestCommandProcessor;
import twetailer.task.step.BaseSteps;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.State;
import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class TestProposeCommandProcessor {

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
        new ProposeCommandProcessor();
    }

    @Test
    public void testProcessCommandProposeI() throws Exception {
        final Long consumerKey = 3333L;
        final Long saConsumerRecordKey = 76325L;
        final Long proposalKey = 5555L;
        final Long saleAssociateKey =  6666L;
        final Long storeKey = 7777L;
        final Long locationKey = 8888L;
        final Long demandKey = 6457657L;

        // ProposalOperations mock
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal createProposal(PersistenceManager pm, JsonObject parameters, SaleAssociate saleAssociate) {
                assertEquals(saConsumerRecordKey, saleAssociate.getConsumerKey());
                assertEquals(locationKey.longValue(), parameters.getLong(Command.LOCATION_KEY));
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setLocationKey(locationKey);
                return proposal;
            }
        });
        // SaleAssociateOperations mock
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                assertEquals(saleAssociateKey, key);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setStoreKey(storeKey);
                saleAssociate.setConsumerKey(saConsumerRecordKey);
                saleAssociate.setLocationKey(locationKey);
                return saleAssociate;
            }
        });
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
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            public Demand getDemand(PersistenceManager pm, Long key, Long oKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setState(State.published);
                return demand;
            }
        });
        // StoreOperations mock
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

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.propose.toString());
        command.put(Proposal.DEMAND_KEY, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setSaleAssociateKey(saleAssociateKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        /* TODO: Re-enable when long_core_* messages are in!
        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(proposalKey.toString()));
        */
    }

    @Test
    public void testProcessCommandProposeIIa() throws Exception {
        final Long consumerKey = 3333L;
        final Long saConsumerRecordKey = 76325L;
        final Long proposalKey = 5555L;
        final Long saleAssociateKey =  6666L;
        final Long storeKey = 7777L;
        final Long demandKey = 6587234L;

        // ProposalOperations mock
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) {
                assertEquals(proposalKey, key);
                assertEquals(storeKey, sKey);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(saleAssociateKey);
                // proposal.setState(State.opened); // Default state
                proposal.setStoreKey(storeKey);
                proposal.setDemandKey(demandKey);
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertEquals(State.opened, proposal.getState());
                proposal.setKey(proposalKey);
                return proposal;
            }
        });
        // SaleAssociateOperations mock
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                assertEquals(saleAssociateKey, key);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setStoreKey(storeKey);
                saleAssociate.setConsumerKey(saConsumerRecordKey);
                return saleAssociate;
            }
        });
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
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            public Demand getDemand(PersistenceManager pm, Long key, Long oKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setState(State.published);
                return demand;
            }
        });
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(storeKey, key);
                final Store store = new Store();
                store.setKey(storeKey);
                return store;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.propose.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setSaleAssociateKey(saleAssociateKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        /* TODO: Re-enable when long_core_* messages are in!
        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(proposalKey.toString()));
        */
    }

    @Test
    public void testProcessCommandProposeIIb() throws Exception {
        final Long consumerKey = 3333L;
        final Long saConsumerRecordKey = 76325L;
        final Long proposalKey = 5555L;
        final Long saleAssociateKey =  6666L;
        final Long storeKey = 7777L;
        final Long demandKey = 45354L;

        // ProposalOperations mock
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) {
                assertEquals(proposalKey, key);
                assertEquals(storeKey, sKey);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setState(State.published); // To be able to verify the reset to "open"
                proposal.setStoreKey(storeKey);
                proposal.setDemandKey(demandKey);
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertEquals(State.opened, proposal.getState());
                proposal.setKey(proposalKey);
                return proposal;
            }
        });
        // SaleAssociateOperations mock
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                assertEquals(saleAssociateKey, key);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setStoreKey(storeKey);
                saleAssociate.setConsumerKey(saConsumerRecordKey);
                return saleAssociate;
            }
        });
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
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            public Demand getDemand(PersistenceManager pm, Long key, Long oKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setState(State.published);
                return demand;
            }
        });
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(storeKey, key);
                final Store store = new Store();
                store.setKey(storeKey);
                return store;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.propose.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setSaleAssociateKey(saleAssociateKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        /* TODO: Re-enable when long_core_* messages are in!
        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(proposalKey.toString()));
        */
    }

    @Test
    public void testProcessCommandProposeIIc() throws Exception {
        final Long consumerKey = 3333L;
        final Long saConsumerRecordKey = 76325L;
        final Long proposalKey = 5555L;
        final Long saleAssociateKey =  6666L;
        final Long storeKey = 7777L;
        final Long demandKey = 6543L;

        // ProposalOperations mock
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) {
                assertEquals(proposalKey, key);
                assertEquals(storeKey, sKey);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setState(State.invalid);
                proposal.setStoreKey(storeKey);
                proposal.setDemandKey(demandKey);
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertEquals(State.opened, proposal.getState());
                proposal.setKey(proposalKey);
                return proposal;
            }
        });
        // SaleAssociateOperations mock
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                assertEquals(saleAssociateKey, key);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setStoreKey(storeKey);
                saleAssociate.setConsumerKey(saConsumerRecordKey);
                return saleAssociate;
            }
        });
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
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            public Demand getDemand(PersistenceManager pm, Long key, Long oKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setState(State.published);
                return demand;
            }
        });
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(storeKey, key);
                final Store store = new Store();
                store.setKey(storeKey);
                return store;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.propose.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setSaleAssociateKey(saleAssociateKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        /* TODO: Re-enable when long_core_* messages are in!
        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(proposalKey.toString()));
        */
    }

    @Test
    public void testProcessCommandProposeIId() throws Exception {
        final Long consumerKey = 3333L;
        final Long saConsumerRecordKey = 76325L;
        final Long proposalKey = 5555L;
        final Long saleAssociateKey =  6666L;
        final Long storeKey = 7777L;
        final Long demandKey = 5465L;

        // ProposalOperations mock
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) {
                assertEquals(proposalKey, key);
                assertEquals(storeKey, sKey);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setState(State.confirmed); // Too late
                proposal.setStoreKey(storeKey);
                proposal.setDemandKey(demandKey);
                return proposal;
            }
        });
        // SaleAssociateOperations mock
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                assertEquals(saleAssociateKey, key);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setStoreKey(storeKey);
                saleAssociate.setConsumerKey(saConsumerRecordKey);
                return saleAssociate;
            }
        });
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
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            public Demand getDemand(PersistenceManager pm, Long key, Long oKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setState(State.published);
                return demand;
            }
        });
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(storeKey, key);
                final Store store = new Store();
                store.setKey(storeKey);
                return store;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.propose.toString());
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
        Locale locale = Locale.ENGLISH;
        String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposalKey }, locale);
        String stateLabel = LabelExtractor.get("cp_tweet_state_part", new Object[] { State.confirmed.toString() }, locale);
        assertEquals(LabelExtractor.get("cp_command_propose_non_modifiable_state", new Object[] { proposalRef, stateLabel }, Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandProposeIII() throws Exception {
        final Long consumerKey = 3333L;
        final Long proposalKey = 5555L;
        final Long saConsumerRecordKey = 76325L;
        final Long saleAssociateKey =  6666L;
        final Long storeKey = 7777L;

        // ProposalOperations mock
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws InvalidIdentifierException {
                throw new InvalidIdentifierException("Done in purpose");
            }
        });
        // SaleAssociateOperations mock
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                assertEquals(saleAssociateKey, key);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setStoreKey(storeKey);
                saleAssociate.setConsumerKey(saConsumerRecordKey);
                return saleAssociate;
            }
        });
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
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(storeKey, key);
                final Store store = new Store();
                store.setKey(storeKey);
                return store;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.propose.toString());
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
        assertEquals(LabelExtractor.get("cp_command_propose_invalid_proposal_id", Locale.ENGLISH), sentText);
    }
}
