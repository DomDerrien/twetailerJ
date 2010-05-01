package twetailer.task.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
import twetailer.connector.BaseConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.DemandOperations;
import twetailer.dao.MockBaseOperations;
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
import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class TestDeclineCommandProcessor {

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
        new DeclineCommandProcessor();
    }

    @Test
    public void testProcessCommandDeclineI() throws TwitterException, DataSourceException, ClientException {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.decline.toString());

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertEquals(LabelExtractor.get("cp_command_decline_invalid_parameters", Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandDeclineII() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 5555L;
        final Long consumerKey = 6666L;
        final Long saleAssociateKey = 7777L;

        // SaleAssociateOperations mock
        final SaleAssociateOperations saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<Long> getSaleAssociateKeys(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                List<Long> saleAssociateKeys = new ArrayList<Long>();
                saleAssociateKeys.add(saleAssociateKey);
                return saleAssociateKeys;
            }
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                assertEquals(saleAssociateKey, key);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                return saleAssociate;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.saleAssociateOperations = saleAssociateOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.decline.toString());
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText); // Implement when the declination of proposal will be implemented
        assertTrue(sentText.contains("Not yet implemented"));
    }

    @Test
    public void testProcessCommandDeclineIII() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 4444L;
        final Long consumerKey = 6666L;

        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long rKey, Long sKey) throws DataSourceException {
                throw new IllegalArgumentException("Done in purpose");
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.proposalOperations = proposalOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.decline.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertEquals(LabelExtractor.get("cp_command_decline_invalid_proposal_id", Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandDeclineIV() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 4444L;
        final Long demandKey = 5555L;
        final Long consumerKey = 6666L;
        final Long saleAssociateKey = 7777L;

        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long rKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setDemandKey(demandKey);
                proposal.setOwnerKey(saleAssociateKey);
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertEquals(State.declined, proposal.getState());
                return proposal;
            }
        };
        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                return demand;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;
        CommandProcessor.proposalOperations = proposalOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.decline.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertEquals(LabelExtractor.get("cp_command_decline_acknowledge_proposal_closing", new Object[] { proposalKey }, Locale.ENGLISH), sentText);
    }
}
