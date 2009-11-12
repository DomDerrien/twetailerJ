package twetailer.task.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import twetailer.dao.LocationOperations;
import twetailer.dao.MockBaseOperations;
import twetailer.dao.MockPersistenceManager;
import twetailer.dao.ProposalOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.Proposal;
import twetailer.dto.RawCommand;
import twetailer.dto.SaleAssociate;
import twetailer.task.CommandProcessor;
import twetailer.task.TestCommandProcessor;
import twetailer.validator.CommandSettings.Action;
import twitter4j.TwitterException;

public class TestListCommandProcessor {

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
        new ListCommandProcessor();
    }

    @Test
    public void testProcessCommandListI() throws TwitterException, DataSourceException, ClientException {
        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> parameters, int limit) {
                return new ArrayList<Demand>();
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.list.toString());

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_list_no_active_demand", Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandListII() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 2222L;

        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> parameters, int limit) {
                Demand demand = new Demand();
                demand.setKey(demandKey);
                List<Demand> demands = new ArrayList<Demand>();
                demands.add(demand);
                return demands;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.list.toString());

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1); // First message of the series with the introduction
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_list_series_introduction", new Object[] { 1 }, Locale.ENGLISH), sentText);
        sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(0); // Last message with the demand details
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessCommandListIII() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 2222L;
        final Long locationKey = 3333L;

        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> parameters, int limit) {
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setLocationKey(locationKey);
                List<Demand> demands = new ArrayList<Demand>();
                demands.add(demand);
                return demands;
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
        command.put(Command.ACTION, Action.list.toString());

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1); // First message of the series with the introduction
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_list_series_introduction", new Object[] { 1 }, Locale.ENGLISH), sentText);
        sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(0); // Last message with the demand details
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessCommandListIV() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 2222L;

        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                return demand;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.list.toString());
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessCommandListV() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 2222L;
        final Long locationKey = 3333L;

        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setLocationKey(locationKey);
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
        command.put(Command.ACTION, Action.list.toString());
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessCommandListVI() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 2222L;

        // DemandOperations mock
        final DemandOperations demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) throws DataSourceException {
                assertEquals(demandKey, key);
                throw new DataSourceException("Done in purpose");
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.demandOperations = demandOperations;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.list.toString());
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_list_invalid_demand_id", new Object[] { demandKey }, Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandListVII() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 2222L;
        final Long consumerKey = 3333L;
        final Long saleAssociateKey = 4444L;

        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long rKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                assertEquals(saleAssociateKey, rKey);
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
        command.put(Command.ACTION, Action.list.toString());
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
    }

    @Test
    public void testProcessCommandListVIII() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 2222L;
        final Long consumerKey = 3333L;
        final Long saleAssociateKey = 4444L;

        // ProposalOperations mock
        final ProposalOperations proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long rKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                assertEquals(saleAssociateKey, rKey);
                throw new DataSourceException("Done in purpose");
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
        command.put(Command.ACTION, Action.list.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_list_invalid_proposal_id", new Object[] { proposalKey }, Locale.ENGLISH), sentText);
    }
}
