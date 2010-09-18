package twetailer.task.command;

import static org.junit.Assert.assertEquals;

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
import twetailer.dao.DemandOperations;
import twetailer.dao.ProposalOperations;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Proposal;
import twetailer.dto.RawCommand;
import twetailer.task.CommandProcessor;
import twetailer.task.TestCommandProcessor;
import twetailer.task.step.BaseSteps;
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
    @Ignore
    public void testProcessCommandDeclineIII() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 4444L;
        final Long consumerKey = 6666L;

        // ProposalOperations mock
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long rKey, Long sKey) throws InvalidIdentifierException {
                throw new InvalidIdentifierException("Done in purpose");
            }
        });

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
    @Ignore
    public void testProcessCommandDeclineIV() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 4444L;
        final Long demandKey = 5555L;
        final Long consumerKey = 6666L;
        final Long saleAssociateKey = 7777L;

        // ProposalOperations mock
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long rKey, Long sKey) throws InvalidIdentifierException {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setDemandKey(demandKey);
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setState(State.published);
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertEquals(State.declined, proposal.getState());
                return proposal;
            }
        });
        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setState(State.published);
                return demand;
            }
        });

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
        String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposalKey }, Locale.ENGLISH);
        assertEquals(LabelExtractor.get("cp_command_decline_acknowledge_proposal_declination", new Object[] { proposalRef }, Locale.ENGLISH), sentText);
    }
}
