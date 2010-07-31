package twetailer.task.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.jdo.MockPersistenceManager;
import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.connector.BaseConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.ProposalOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Proposal;
import twetailer.dto.RawCommand;
import twetailer.dto.SaleAssociate;
import twetailer.task.CommandProcessor;
import twetailer.task.TestCommandProcessor;
import twetailer.task.step.BaseSteps;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.State;
import twitter4j.TwitterException;
import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class TestDeleteCommandProcessor {

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
        new DeleteCommandProcessor();
    }

    @Test
    public void testProcessCommandDeleteI() throws TwitterException, DataSourceException, ClientException {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.delete.toString());

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertEquals(LabelExtractor.get("cp_command_delete_invalid_parameters", Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandDeleteII() throws TwitterException, DataSourceException, ClientException {
        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                throw new InvalidIdentifierException("Done in purpose");
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.delete.toString());
        command.put(Demand.REFERENCE, 1111L);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertEquals(LabelExtractor.get("cp_command_delete_invalid_demand_id", Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandDeleteIII() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 1111L;
        final State state = State.published;

        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) {
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setState(state);
                return demand;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.delete.toString());
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        Locale locale = Locale.ENGLISH;
        String demandRef = LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { demandKey }, locale);
        String stateLabel = LabelExtractor.get("cp_tweet_state_part", new Object[] { state.toString() }, locale);
        assertEquals(LabelExtractor.get("cp_command_delete_invalid_demand_state", new Object[] { demandRef, stateLabel }, locale), sentText);
    }

    @Test
    public void testProcessCommandDeleteIV() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 1111L;
        final State state = State.cancelled;

        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) {
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setState(state);
                return demand;
            }
        });

        // ProposalOperations mock
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public List<Proposal >getProposals(PersistenceManager pm, String name, Object value, int limit) {
                assertEquals(Proposal.DEMAND_KEY, name);
                assertEquals(demandKey, (Long) value);
                return new ArrayList<Proposal>();
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.delete.toString());
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        Locale locale = Locale.ENGLISH;
        String demandRef = LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { demandKey }, locale);
        assertEquals(LabelExtractor.get("cp_command_delete_acknowledge_demand_markedForDeletion", new Object[] { demandRef }, locale), sentText);
    }

    @Test
    public void testProcessCommandDeleteV() throws TwitterException, DataSourceException, ClientException {
        final Long consumerKey = 2222L;
        final Long saConsumerRecordKey = 76325L;
        final Long saleAssociateKey = 3333L;

        // SaleAssociateOperations mock
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
                assertTrue(consumerKey == key || saConsumerRecordKey == key);
                Consumer saConsumerRecord = new Consumer();
                saConsumerRecord.setKey(saConsumerRecordKey);
                saConsumerRecord.setPreferredConnection(Source.simulated);
                return saConsumerRecord;
            }
        });
        // ProposalOperations mock
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws InvalidIdentifierException {
                throw new InvalidIdentifierException("Done in purpose");
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.delete.toString());
        command.put(Proposal.PROPOSAL_KEY, 1111L);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setSaleAssociateKey(saleAssociateKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertEquals(LabelExtractor.get("cp_command_delete_invalid_proposal_id", Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandDeleteVI() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 1111L;
        final Long consumerKey = 2222L;
        final Long saConsumerRecordKey = 76325L;
        final Long saleAssociateKey = 3333L;
        final State state = State.published;

        // SaleAssociateOperations mock
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
                assertTrue(consumerKey == key || saConsumerRecordKey == key);
                Consumer saConsumerRecord = new Consumer();
                saConsumerRecord.setKey(saConsumerRecordKey);
                saConsumerRecord.setPreferredConnection(Source.simulated);
                return saConsumerRecord;
            }
        });
        // ProposalOperations mock
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws InvalidIdentifierException {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setState(state);
                return proposal;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.delete.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setSaleAssociateKey(saleAssociateKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        Locale locale = Locale.ENGLISH;
        String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposalKey }, locale);
        String stateLabel = LabelExtractor.get("cp_tweet_state_part", new Object[] { state.toString() }, locale);
        assertEquals(LabelExtractor.get("cp_command_delete_invalid_proposal_state", new Object[] { proposalRef, stateLabel }, locale), sentText);
    }

    @Test
    public void testProcessCommandDeleteVII() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 1111L;
        final Long consumerKey = 2222L;
        final Long saConsumerRecordKey = 76325L;
        final Long saleAssociateKey = 3333L;
        final State state = State.cancelled;

        // SaleAssociateOperations mock
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
                assertTrue(consumerKey == key || saConsumerRecordKey == key);
                Consumer saConsumerRecord = new Consumer();
                saConsumerRecord.setKey(saConsumerRecordKey);
                saConsumerRecord.setPreferredConnection(Source.simulated);
                return saConsumerRecord;
            }
        });
        // ProposalOperations mock
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws InvalidIdentifierException {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setState(state);
                return proposal;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.delete.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setSaleAssociateKey(saleAssociateKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        Locale locale = Locale.ENGLISH;
        String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposalKey }, locale);
        assertEquals(LabelExtractor.get("cp_command_delete_acknowledge_proposal_markedForDeletion", new Object[] { proposalRef }, locale), sentText);
    }

    /***** ddd
    final Long saleAssociateKey = 43454L;
    final Long proposalKey = 8764334L;
        final Long demandKey = 654433L;
            demand.addSaleAssociateKey(saleAssociateKey);
            demand.addProposalKey(proposalKey);
            public Proposal getProposal(PersistenceManager pm, Long key, Long saKey, Long sKey) {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setRawCommandId(originalRawCommandId);
                proposal.setSource(Source.simulated);
                proposal.setState(State.confirmed);
                return proposal;
            }
            public Demand getDemand(PersistenceManager pm, Long key, Long oKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setState(State.closed);
                return demand;
            }
        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(saConsumerRecordKey);
        consumer.setSaleAssociateKey(saleAssociateKey);

        System.err.println("***** " + sentText);
    ddd ****/
}
