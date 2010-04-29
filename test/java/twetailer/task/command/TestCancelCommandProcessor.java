package twetailer.task.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.MockBaseOperations;
import twetailer.dao.ProposalOperations;
import twetailer.dao.RawCommandOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dao.StoreOperations;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.Proposal;
import twetailer.dto.RawCommand;
import twetailer.dto.SaleAssociate;
import twetailer.dto.Store;
import twetailer.task.CommandProcessor;
import twetailer.task.TestCommandProcessor;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.Prefix;
import twetailer.validator.CommandSettings.State;
import twitter4j.TwitterException;
import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class TestCancelCommandProcessor {

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
        new CancelCommandProcessor();
    }

    @Test
    public void testProcessCommandCancelI() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 5555L;
        final Long consumerKey = 73824L;

        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setOwnerKey(consumerKey);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(State.cancelled, demand.getState());
                return demand;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.cancel.toString());
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessCommandCancelII() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 5555L;
        final Long locationKey = 3333L;
        final Long consumerKey = 73824L;

        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setLocationKey(locationKey);
                demand.setOwnerKey(consumerKey);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(State.cancelled, demand.getState());
                return demand;
            }
        };
        // LocationOperations mock
        final LocationOperations locationOperations = new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                assertEquals(locationKey, key);
                Location location = new Location();
                location.setKey(locationKey);
                return location;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;
        CommandProcessor.locationOperations = locationOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.cancel.toString());
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
        assertTrue(sentText.contains(Prefix.state.toString()+":"+State.cancelled.toString()));
    }

    @Test
    public void testProcessCommandCancelIII() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 5555L;
        final Long consumerKey = 73824L;

        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                throw new RuntimeException("Done in purpose");
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.cancel.toString());
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_cancel_invalid_demand_id", Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandCancelIV() throws TwitterException, DataSourceException, ClientException {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.cancel.toString());

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_cancel_missing_demand_id", Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandCancelV() throws TwitterException, DataSourceException, ClientException {
        final Long consumerKey = 2222L;
        final Long saleAssociateKey = 3333L;
        final Long proposalKey = 5555L;
        final Long storeKey = 66666L;
        final String name = "sgrognegneu";

        // SaleAssociateOperations mock
        final SaleAssociateOperations saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setStoreKey(storeKey);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                saleAssociates.add(saleAssociate);
                return saleAssociates;
            }
        };
        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) {
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(saleAssociateKey);
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertEquals(proposalKey, proposal.getKey());
                assertEquals(State.cancelled, proposal.getState());
                return proposal;
            }
        };
        // StoreOperations mock
        final StoreOperations storeOperations = new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) {
                assertEquals(storeKey, key);
                Store store = new Store();
                store.setKey(storeKey);
                store.setName(name);
                return store;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.proposalOperations = proposalOperations;
        CommandProcessor.saleAssociateOperations = saleAssociateOperations;
        CommandProcessor.storeOperations = storeOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.cancel.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(proposalKey.toString()));
        assertTrue(sentText.contains(Prefix.state.toString()+":"+State.cancelled.toString()));
    }

    @Test
    public void testProcessCommandCancelVI() throws TwitterException, DataSourceException, ClientException {
        final Long consumerKey = 2222L;
        final Long saleAssociateKey = 3333L;
        final Long proposalKey = 5555L;

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
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) {
                throw new RuntimeException("Done in purpose");
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.proposalOperations = proposalOperations;
        CommandProcessor.saleAssociateOperations = saleAssociateOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.cancel.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_cancel_invalid_proposal_id", Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandCancelXI() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 5555L;
        final State state = State.closed;

        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setState(state);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(State.cancelled, demand.getState());
                return demand;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.cancel.toString());
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_cancel_invalid_demand_state", new Object[] { demandKey, state.toString() }, Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandCancelXII() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 5555L;
        final State state = State.cancelled;

        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setState(state);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(State.cancelled, demand.getState());
                return demand;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.cancel.toString());
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_cancel_invalid_demand_state", new Object[] { demandKey, state.toString() }, Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandCancelXIII() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 5555L;
        final State state = State.markedForDeletion;

        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setState(state);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(State.cancelled, demand.getState());
                return demand;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.cancel.toString());
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_cancel_invalid_demand_state", new Object[] { demandKey, state.toString() }, Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandCancelXIV() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 5555L;
        final State state = State.closed;
        final Long consumerKey = 3333L;
        final Long saleAssociateKey = 3333L;

        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setState(state);
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertEquals(State.cancelled, proposal.getState());
                return proposal;
            }
        };
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
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.proposalOperations = proposalOperations;
        CommandProcessor.saleAssociateOperations = saleAssociateOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.cancel.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_cancel_invalid_proposal_state", new Object[] { proposalKey, state.toString() }, Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandCancelXV() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 5555L;
        final State state = State.cancelled;
        final Long consumerKey = 3333L;
        final Long saleAssociateKey = 3333L;

        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setState(state);
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertEquals(State.cancelled, proposal.getState());
                return proposal;
            }
        };
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
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.proposalOperations = proposalOperations;
        CommandProcessor.saleAssociateOperations = saleAssociateOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.cancel.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_cancel_invalid_proposal_state", new Object[] { proposalKey, state.toString() }, Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandCancelXVI() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 5555L;
        final State state = State.markedForDeletion;
        final Long consumerKey = 3333L;
        final Long saleAssociateKey = 4444L;

        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setState(state);
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertEquals(State.cancelled, proposal.getState());
                return proposal;
            }
        };
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
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.proposalOperations = proposalOperations;
        CommandProcessor.saleAssociateOperations = saleAssociateOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.cancel.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_cancel_invalid_proposal_state", new Object[] { proposalKey, state.toString() }, Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandCancelXXI() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 5555L;
        final State state = State.published;
        final Long consumerKey = 73824L;

        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setState(state);
                demand.setOwnerKey(consumerKey);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(State.cancelled, demand.getState());
                return demand;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.cancel.toString());
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        // To be controlled later, when the sale associates will be informed about the demand cancellation
    }

    @Test
    public void testProcessCommandCancelXXII() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 5555L;
        final State state = State.confirmed;
        final Long consumerKey = 73824L;

        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setState(state);
                demand.setOwnerKey(consumerKey);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(State.cancelled, demand.getState());
                return demand;
            }
        };
        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public List<Proposal> getProposals(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
                return new ArrayList<Proposal>();
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;
        CommandProcessor.proposalOperations = proposalOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.cancel.toString());
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        // To be controlled later, when the sale associates will be informed about the demand cancellation
    }

    @Test
    public void testProcessCommandCancelXXIII() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 5555L;
        final State state = State.declined;
        final Long consumerKey = 3333L;
        final Long saleAssociateKey = 4444L;
        final Long storeKey = 55555L;
        final String name = "sgrognegneu";

        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setState(state);
                proposal.setOwnerKey(saleAssociateKey);
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertEquals(State.cancelled, proposal.getState());
                return proposal;
            }
        };
        final SaleAssociateOperations saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setStoreKey(storeKey);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                saleAssociates.add(saleAssociate);
                return saleAssociates;
            }
        };
        // StoreOperations mock
        final StoreOperations storeOperations = new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) {
                assertEquals(storeKey, key);
                Store store = new Store();
                store.setKey(storeKey);
                store.setName(name);
                return store;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.proposalOperations = proposalOperations;
        CommandProcessor.saleAssociateOperations = saleAssociateOperations;
        CommandProcessor.storeOperations = storeOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.cancel.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        // To be controlled later, when the consumer will be informed about the proposal cancellation
    }

    @Test
    public void testProcessCommandCancelWithSaleAssociateNotificationI() throws TwitterException, DataSourceException, ClientException {
        final Long consumerKey = 11L;
        final Long saleAssociateKey = 222L;
        final Long demandKey = 5555L;
        final State state = State.confirmed;
        final Long proposalKey = 7777L;
        final Long originalRawCommandId = 88888L;

        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long ownerKey) {
                assertEquals(demandKey, key);
                assertEquals(consumerKey, ownerKey);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setOwnerKey(consumerKey);
                demand.setState(state);
                demand.setSource(Source.simulated);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(demandKey, demand.getKey());
                assertEquals(State.cancelled, demand.getState());
                assertEquals(consumerKey, demand.getCancelerKey());
                return demand;
            }
        };
        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public List<Proposal> getProposals(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setDemandKey(demandKey);
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setRawCommandId(originalRawCommandId);
                proposal.setSource(Source.simulated);
                List<Proposal> proposals = new ArrayList<Proposal>();
                proposals.add(proposal);
                return proposals;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertEquals(proposalKey, proposal.getKey());
                assertEquals(State.cancelled, proposal.getState());
                assertEquals(consumerKey, proposal.getCancelerKey());
                return proposal;
            }
        };
        // RawCommandOperations mock
        final RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(originalRawCommandId, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(saleAssociateKey);
                return rawCommand;
            }
        };
        // SaleAssociateOperations mock
        final SaleAssociateOperations saleAssociateOperations = new SaleAssociateOperations() {
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
        CommandProcessor.demandOperations = demandOperations;
        CommandProcessor.proposalOperations = proposalOperations;
        CommandProcessor.rawCommandOperations = rawCommandOperations;
        CommandProcessor.saleAssociateOperations = saleAssociateOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.cancel.toString());
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        // To be controlled later, when the sale associates will be informed about the demand cancellation
    }

    @Test
    public void testProcessCommandCancelWithSaleAssociateNotificationII() throws TwitterException, DataSourceException, ClientException {
        final Long consumerKey = 11L;
        final Long demandKey = 5555L;
        final State state = State.confirmed;

        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long ownerKey) {
                assertEquals(demandKey, key);
                assertEquals(consumerKey, ownerKey);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setOwnerKey(consumerKey);
                demand.setState(state);
                demand.setSource(Source.simulated);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(demandKey, demand.getKey());
                assertEquals(State.cancelled, demand.getState());
                assertEquals(consumerKey, demand.getCancelerKey());
                return demand;
            }
        };
        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public List<Proposal> getProposals(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
                throw new DataSourceException("Done in purpose!");
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;
        CommandProcessor.proposalOperations = proposalOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.cancel.toString());
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        // To be controlled later, when the sale associates will be informed about the demand cancellation
    }

    @Test
    public void testProcessCommandCancelWithConsumerNotificationI() throws TwitterException, DataSourceException, ClientException {
        final Long consumerKey = 11L;
        final Long saleAssociateKey = 222L;
        final Long storeKey = 3030L;
        final Long demandKey = 5555L;
        final State state = State.confirmed;
        final Long proposalKey = 7777L;
        final Long originalRawCommandId = 88888L;
        final String name = "Store name";

        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long ownerKey, Long storeKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                assertEquals(saleAssociateKey, ownerKey);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setDemandKey(demandKey);
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setState(state);
                proposal.setSource(Source.simulated);
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertEquals(proposalKey, proposal.getKey());
                assertEquals(State.cancelled, proposal.getState());
                assertEquals(saleAssociateKey, proposal.getCancelerKey());
                return proposal;
            }
        };
        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long ownerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setOwnerKey(consumerKey);
                demand.setRawCommandId(originalRawCommandId);
                demand.setSource(Source.simulated);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(demandKey, demand.getKey());
                assertEquals(State.published, demand.getState());
                assertEquals(saleAssociateKey, demand.getCancelerKey());
                return demand;
            }
        };
        // RawCommandOperations mock
        final RawCommandOperations rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(originalRawCommandId, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(saleAssociateKey);
                rawCommand.setSource(Source.simulated);
                return rawCommand;
            }
        };
        // ConsumerOperations mock
        final ConsumerOperations consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                return consumer;
            }
        };
        // SaleAssociateOperations mock
        final SaleAssociateOperations saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setStoreKey(storeKey);
                List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
                saleAssociates.add(saleAssociate);
                return saleAssociates;
            }
        };
        // StoreOperations mock
        final StoreOperations storeOperations = new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) {
                assertEquals(storeKey, key);
                Store store = new Store();
                store.setKey(storeKey);
                store.setName(name);
                return store;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.consumerOperations = consumerOperations;
        CommandProcessor.demandOperations = demandOperations;
        CommandProcessor.proposalOperations = proposalOperations;
        CommandProcessor.rawCommandOperations = rawCommandOperations;
        CommandProcessor.saleAssociateOperations = saleAssociateOperations;
        CommandProcessor.storeOperations = storeOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.cancel.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        // To be controlled later, when the sale associates will be informed about the demand cancellation
    }
}
