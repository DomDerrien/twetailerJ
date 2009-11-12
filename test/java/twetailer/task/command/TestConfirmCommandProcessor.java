package twetailer.task.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

public class TestConfirmCommandProcessor {

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

        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
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
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertEquals(State.confirmed, proposal.getState());
                return proposal;
            }
        };
        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
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
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(State.confirmed, demand.getState());
                return demand;
            }
        };
        // SaleAssociateOperations mock
        final SaleAssociateOperations saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                assertEquals(saleAssociateKey, key);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setPreferredConnection(Source.simulated);
                return saleAssociate;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;
        CommandProcessor.proposalOperations = proposalOperations;
        CommandProcessor.saleAssociateOperations = saleAssociateOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.confirm.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1);
        assertNotNull(sentText); // Informs the consumer
        assertTrue(sentText.contains(proposalKey.toString()));
        assertTrue(sentText.contains(demandKey.toString()));
        assertTrue(sentText.contains(storeKey.toString()));
        assertTrue(sentText.contains("test"));
        sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText); // Informs the saleAssociate
        assertTrue(sentText.contains(proposalKey.toString()));
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessCommandConfirmII() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 4444L;

        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) {
                assertEquals(proposalKey, key);
                throw new IllegalArgumentException("Done in purpose");
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.proposalOperations = proposalOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.confirm.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_confirm_invalid_proposal_id", Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandConfirmIII() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 4444L;
        final Long demandKey = 5555L;
        final Long saleAssociateKey = 6666L;
        final Long storeKey = 7777L;

        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
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
        };
        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                throw new IllegalArgumentException("Done in purpose");
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;
        CommandProcessor.proposalOperations = proposalOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.confirm.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_confirm_invalid_proposal_id", Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandConfirmIV() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 4444L;
        final Long demandKey = 5555L;
        final Long saleAssociateKey = 6666L;
        final Long storeKey = 7777L;

        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
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
        };
        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
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
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;
        CommandProcessor.proposalOperations = proposalOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.confirm.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText); // Informs the consumer
        assertTrue(sentText.contains(proposalKey.toString()));
        assertTrue(sentText.contains(demandKey.toString()));
        assertTrue(sentText.contains(State.invalid.toString()));
    }
}
