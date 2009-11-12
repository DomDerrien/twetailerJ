package twetailer.task.command;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.connector.BaseConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.DemandOperations;
import twetailer.dao.MockBaseOperations;
import twetailer.dao.MockPersistenceManager;
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
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.State;
import twitter4j.TwitterException;

public class TestDeleteCommandProcessor {

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
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertEquals(LabelExtractor.get("cp_command_delete_invalid_parameters", Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandDeleteII() throws TwitterException, DataSourceException, ClientException {
        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws DataSourceException {
                throw new DataSourceException("Done in purpose");
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.delete.toString());
        command.put(Demand.REFERENCE, 1111L);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertEquals(LabelExtractor.get("cp_command_delete_invalid_demand_id", Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandDeleteIII() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 1111L;
        final State state = State.published;

        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) {
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setState(state);
                return demand;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.delete.toString());
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertEquals(LabelExtractor.get("cp_command_delete_invalid_demand_state", new Object[] { demandKey, state }, Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandDeleteIV() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 1111L;
        final State state = State.cancelled;

        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) {
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setState(state);
                return demand;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.delete.toString());
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertEquals(LabelExtractor.get("cp_command_delete_acknowledge_demand_markedForDeletion", new Object[] { demandKey }, Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandDeleteV() throws TwitterException, DataSourceException, ClientException {
        final Long consumerKey = 2222L;
        final Long saleAssociateKey = 3333L;

        // SaleAssociateOperations mock
        final SaleAssociateOperations saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                saleAssociates.add(saleAssociate);
                return saleAssociates;
            }
        };
        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws DataSourceException {
                throw new DataSourceException("Done in purpose");
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.proposalOperations = proposalOperations;
        CommandProcessor.saleAssociateOperations = saleAssociateOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.delete.toString());
        command.put(Proposal.PROPOSAL_KEY, 1111L);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertEquals(LabelExtractor.get("cp_command_delete_invalid_proposal_id", Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandDeleteVI() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 1111L;
        final Long consumerKey = 2222L;
        final Long saleAssociateKey = 3333L;
        final State state = State.published;

        // SaleAssociateOperations mock
        final SaleAssociateOperations saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                saleAssociates.add(saleAssociate);
                return saleAssociates;
            }
        };
        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setState(state);
                return proposal;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.proposalOperations = proposalOperations;
        CommandProcessor.saleAssociateOperations = saleAssociateOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.delete.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertEquals(LabelExtractor.get("cp_command_delete_invalid_proposal_state", new Object[] { proposalKey, state }, Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandDeleteVII() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 1111L;
        final Long consumerKey = 2222L;
        final Long saleAssociateKey = 3333L;
        final State state = State.cancelled;

        // SaleAssociateOperations mock
        final SaleAssociateOperations saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                saleAssociates.add(saleAssociate);
                return saleAssociates;
            }
        };
        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setState(state);
                return proposal;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.proposalOperations = proposalOperations;
        CommandProcessor.saleAssociateOperations = saleAssociateOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.delete.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertEquals(LabelExtractor.get("cp_command_delete_acknowledge_proposal_closing", new Object[] { proposalKey }, Locale.ENGLISH), sentText);
    }
}
