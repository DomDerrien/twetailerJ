package twetailer.task.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

import twetailer.connector.BaseConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.MockAppEngineEnvironment;
import twetailer.dao.MockBaseOperations;
import twetailer.dao.MockPersistenceManager;
import twetailer.dao.ProposalOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Proposal;
import twetailer.dto.RawCommand;
import twetailer.dto.SaleAssociate;
import twetailer.task.CommandProcessor;
import twetailer.task.TestCommandProcessor;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.State;

public class TestProposeCommandProcessor {

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
        final Long proposalKey = 5555L;
        final Long saleAssociateKey =  6666L;
        final Long storeKey = 7777L;

        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public Proposal createProposal(PersistenceManager pm, JsonObject parameters, SaleAssociate saleAssociate) {
                assertEquals(consumerKey, saleAssociate.getConsumerKey());
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(saleAssociateKey);
                return proposal;
            }
        };
        // SaleAssociateOperations mock
        final SaleAssociateOperations saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(key, SaleAssociate.CONSUMER_KEY);
                assertEquals(consumerKey, (Long) value);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setConsumerKey(consumerKey);
                saleAssociate.setStoreKey(storeKey);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                saleAssociates.add(saleAssociate);
                return saleAssociates;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.proposalOperations = proposalOperations;
        CommandProcessor.saleAssociateOperations = saleAssociateOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.propose.toString());

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        // App Engine Environment mock
        MockAppEngineEnvironment appEnv = new MockAppEngineEnvironment();

        appEnv.setUp();
        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);
        appEnv.tearDown();

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(proposalKey.toString()));
    }

    @Test
    public void testProcessCommandProposeIIa() throws Exception {
        final Long consumerKey = 3333L;
        final Long proposalKey = 5555L;
        final Long saleAssociateKey =  6666L;
        final Long storeKey = 7777L;

        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) {
                assertEquals(proposalKey, key);
                assertEquals(storeKey, sKey);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(saleAssociateKey);
                // proposal.setState(State.opened); // Default state
                proposal.setStoreKey(storeKey);
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertEquals(State.opened, proposal.getState());
                proposal.setKey(proposalKey);
                return proposal;
            }
        };
        // SaleAssociateOperations mock
        final SaleAssociateOperations saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(key, SaleAssociate.CONSUMER_KEY);
                assertEquals(consumerKey, (Long) value);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setConsumerKey(consumerKey);
                saleAssociate.setStoreKey(storeKey);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                saleAssociates.add(saleAssociate);
                return saleAssociates;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.proposalOperations = proposalOperations;
        CommandProcessor.saleAssociateOperations = saleAssociateOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.propose.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        // App Engine Environment mock
        MockAppEngineEnvironment appEnv = new MockAppEngineEnvironment();

        appEnv.setUp();
        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);
        appEnv.tearDown();

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(proposalKey.toString()));
    }

    @Test
    public void testProcessCommandProposeIIb() throws Exception {
        final Long consumerKey = 3333L;
        final Long proposalKey = 5555L;
        final Long saleAssociateKey =  6666L;
        final Long storeKey = 7777L;

        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) {
                assertEquals(proposalKey, key);
                assertEquals(storeKey, sKey);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setState(State.published); // To be able to verify the reset to "open"
                proposal.setStoreKey(storeKey);
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertEquals(State.opened, proposal.getState());
                proposal.setKey(proposalKey);
                return proposal;
            }
        };
        // SaleAssociateOperations mock
        final SaleAssociateOperations saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(key, SaleAssociate.CONSUMER_KEY);
                assertEquals(consumerKey, (Long) value);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setConsumerKey(consumerKey);
                saleAssociate.setStoreKey(storeKey);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                saleAssociates.add(saleAssociate);
                return saleAssociates;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.proposalOperations = proposalOperations;
        CommandProcessor.saleAssociateOperations = saleAssociateOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.propose.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        // App Engine Environment mock
        MockAppEngineEnvironment appEnv = new MockAppEngineEnvironment();

        appEnv.setUp();
        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);
        appEnv.tearDown();

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(proposalKey.toString()));
    }

    @Test
    public void testProcessCommandProposeIIc() throws Exception {
        final Long consumerKey = 3333L;
        final Long proposalKey = 5555L;
        final Long saleAssociateKey =  6666L;
        final Long storeKey = 7777L;

        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) {
                assertEquals(proposalKey, key);
                assertEquals(storeKey, sKey);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setState(State.invalid);
                proposal.setStoreKey(storeKey);
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertEquals(State.opened, proposal.getState());
                proposal.setKey(proposalKey);
                return proposal;
            }
        };
        // SaleAssociateOperations mock
        final SaleAssociateOperations saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(key, SaleAssociate.CONSUMER_KEY);
                assertEquals(consumerKey, (Long) value);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setConsumerKey(consumerKey);
                saleAssociate.setStoreKey(storeKey);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                saleAssociates.add(saleAssociate);
                return saleAssociates;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.proposalOperations = proposalOperations;
        CommandProcessor.saleAssociateOperations = saleAssociateOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.propose.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        // App Engine Environment mock
        MockAppEngineEnvironment appEnv = new MockAppEngineEnvironment();

        appEnv.setUp();
        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);
        appEnv.tearDown();

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(proposalKey.toString()));
    }

    @Test
    public void testProcessCommandProposeIId() throws Exception {
        final Long consumerKey = 3333L;
        final Long proposalKey = 5555L;
        final Long saleAssociateKey =  6666L;
        final Long storeKey = 7777L;

        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) {
                assertEquals(proposalKey, key);
                assertEquals(storeKey, sKey);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setState(State.confirmed); // Too late
                proposal.setStoreKey(storeKey);
                return proposal;
            }
        };
        // SaleAssociateOperations mock
        final SaleAssociateOperations saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(key, SaleAssociate.CONSUMER_KEY);
                assertEquals(consumerKey, (Long) value);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setConsumerKey(consumerKey);
                saleAssociate.setStoreKey(storeKey);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                saleAssociates.add(saleAssociate);
                return saleAssociates;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.proposalOperations = proposalOperations;
        CommandProcessor.saleAssociateOperations = saleAssociateOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.propose.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        // App Engine Environment mock
        MockAppEngineEnvironment appEnv = new MockAppEngineEnvironment();

        appEnv.setUp();
        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);
        appEnv.tearDown();

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_proposal_non_modifiable_state", new Object[] { proposalKey, State.confirmed.toString()}, Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandProposeIII() throws Exception {
        final Long consumerKey = 3333L;
        final Long proposalKey = 5555L;
        final Long saleAssociateKey =  6666L;
        final Long storeKey = 7777L;

        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) {
                throw new IllegalArgumentException("Done in purpose");
            }
        };
        // SaleAssociateOperations mock
        final SaleAssociateOperations saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(key, SaleAssociate.CONSUMER_KEY);
                assertEquals(consumerKey, (Long) value);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setConsumerKey(consumerKey);
                saleAssociate.setStoreKey(storeKey);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                saleAssociates.add(saleAssociate);
                return saleAssociates;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.proposalOperations = proposalOperations;
        CommandProcessor.saleAssociateOperations = saleAssociateOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.propose.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand();
        rawCommand.setSource(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        // App Engine Environment mock
        MockAppEngineEnvironment appEnv = new MockAppEngineEnvironment();

        appEnv.setUp();
        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);
        appEnv.tearDown();

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_proposal_invalid_proposal_id", Locale.ENGLISH), sentText);
    }
}
