package twetailer.task.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import twetailer.dao.ProposalOperations;
import twetailer.dao.RawCommandOperations;
import twetailer.dao.ReviewSystemOperations;
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
import twetailer.task.step.BaseSteps;
import twetailer.validator.LocaleValidator;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.State;
import twitter4j.TwitterException;
import domderrien.i18n.DateUtils;
import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class TestCloseCommandProcessor {

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
        new CloseCommandProcessor();
    }

    @Test
    public void testProcessCommandCloseI() throws TwitterException, DataSourceException, ClientException {
        final Long consumerKey = 654980834L;
        final Long demandKey = 5555L;

        // ConsumerOperations mock
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                Consumer resource = new Consumer();
                resource.setKey(consumerKey);
                return resource;
            }
        });
        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(State.closed, demand.getState());
                return demand;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.close.toString());
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
    public void testProcessCommandCloseIIa() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 5555L;
        final State demandState = State.confirmed;
        final Long saleAssociateKey = 43454L;
        final Long proposalKey = 8764334L;
        final Long locationKey = 645032L;
        final Long storeKey = 54652L;

        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setState(demandState);
                demand.setDueDate(DateUtils.getNowDate());
                demand.addSaleAssociateKey(saleAssociateKey);
                demand.addProposalKey(proposalKey);
                demand.setLocationKey(locationKey);
                demand.setSource(Source.simulated);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(State.closed, demand.getState());
                return demand;
            }
        });
        // ProposalOperations mock
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long saKey, Long sKey) {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setState(State.closed);
                proposal.setDueDate(DateUtils.getNowDate());
                proposal.setDemandKey(demandKey);
                proposal.setStoreKey(storeKey);
                return proposal;
            }
        });
        // LocationOperations mock
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                assertEquals(locationKey, key);
                Location location = new Location();
                location.setKey(locationKey);
                location.setCountryCode(LocaleValidator.DEFAULT_COUNTRY_CODE);
                location.setPostalCode("H0H0H0");
                return location;
            }
        });
        // StoreOperations mock
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) {
                assertEquals(storeKey, key);
                Store store = new Store();
                store.setKey(proposalKey);
                return store;
            }
        });
        // ReviewSystemOperations mock
        BaseSteps.setMockReviewSystemOperations(new ReviewSystemOperations());
        // ConsumerOperations mock
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer updateConsumer(PersistenceManager pm, Consumer update) {
                assertEquals(1, update.getClosedDemandNb().longValue());
                return update;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.close.toString());
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        /* TODO: Re-enable when long_core_* messages are in!
        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
        Locale locale = Locale.ENGLISH;
        String demandRef = LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { demandKey }, locale);
        assertEquals(LabelExtractor.get("cp_command_close_acknowledge_demand_closing", new Object[] { demandRef }, locale), sentText);
        */
    }

    @Test
    @Ignore
    public void testProcessCommandCloseIII() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 5555L;
        final State demandState = State.confirmed;
        final Long proposalKey = 6666L;
        final Long saleAssociateKey = 7777L;
        final Long originalRawCommandId = 8888L;
        final Long locationKey = 645032L;

        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setState(demandState);
                demand.setDueDate(DateUtils.getNowDate());
                demand.setLocationKey(locationKey);
                demand.addSaleAssociateKey(saleAssociateKey);
                demand.addProposalKey(proposalKey);
                demand.setSource(Source.simulated);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(State.closed, demand.getState());
                return demand;
            }
        });
        // ProposalOperations mock
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long saKey, Long sKey) {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setRawCommandId(originalRawCommandId);
                proposal.setSource(Source.simulated);
                proposal.setState(State.confirmed);
                proposal.setDueDate(DateUtils.getNowDate());
                proposal.setDemandKey(demandKey);
                return proposal;
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
        // RawCommandOperations mock
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(originalRawCommandId, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(saleAssociateKey);
                rawCommand.setSource(Source.simulated);
                return rawCommand;
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

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.close.toString());
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        /* TODO: Re-enable when long_core_* messages are in!
        String sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1);
        Locale locale = Locale.ENGLISH;
        String demandRef = LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { demandKey }, locale);
        String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposalKey }, locale);

        assertNotNull(sentText);
        assertTrue(sentText.contains(proposalKey.toString()));
        assertEquals(LabelExtractor.get("cp_command_close_demand_closed_proposal_to_close", new Object[] { demandRef, proposalRef }, locale), sentText);
        sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
        assertEquals(LabelExtractor.get("cp_command_close_acknowledge_demand_closing", new Object[] { demandRef }, locale), sentText);
        */
    }

    @Test
    public void testProcessCommandCloseIV() throws TwitterException, DataSourceException, ClientException {
        final Long consumerKey = 76459032090954L;
        final Long demandKey = 5555L;

        // ConsumerOperations mock
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                Consumer resource = new Consumer();
                resource.setKey(consumerKey);
                return resource;
            }
        });
        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) throws InvalidIdentifierException {
                throw new InvalidIdentifierException("Done in purpose");
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.close.toString());
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_close_invalid_demand_id", Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandCloseVa() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 5555L;
        final Long consumerKey = 6666L;
        final Long saConsumerRecordKey = 76325L;
        final Long saleAssociateKey = 7777L;
        final State proposalState = State.confirmed;
        final Long demandKey = 654433L;
        final Long locationKey = 645032L;

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
            @Override
            public SaleAssociate updateSaleAssociate(PersistenceManager pm, SaleAssociate update) {
                assertEquals(1, update.getClosedProposalNb().longValue());
                return update;
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
                saConsumerRecord.setSaleAssociateKey(saleAssociateKey);
                return saConsumerRecord;
            }
        });
        // ProposalOperations mock
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long rKey, Long sKey) {
                assertEquals(proposalKey, key);
                assertEquals(saleAssociateKey, rKey);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setSource(Source.simulated);
                proposal.setState(proposalState);
                proposal.setDemandKey(demandKey);
                proposal.setDueDate(DateUtils.getNowDate());
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertEquals(State.closed, proposal.getState());
                return proposal;
            }
        });
        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long oKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setState(State.closed);
                demand.setDueDate(DateUtils.getNowDate());
                demand.setLocationKey(locationKey);
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
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store updateStore(PersistenceManager pm, Store update) {
                assertEquals(1, update.getClosedProposalNb().longValue());
                return update;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.close.toString());
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
        Locale locale = Locale.ENGLISH;
        String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposalKey }, locale);
        assertEquals(LabelExtractor.get("cp_command_close_acknowledge_proposal_closing", new Object[] { proposalRef }, locale), sentText);
        */
    }

    @Test
    @Ignore
    public void testProcessCommandCloseVI() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 5555L;
        final Long consumerKey = 6666L;
        final Long saConsumerRecordKey = 76325L;
        final Long saleAssociateKey = 7777L;
        final Long demandKey = 888888L;
        final Long originalRawCommandId = 999999L;
        final Long locationKey = 645032L;

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
            public Proposal getProposal(PersistenceManager pm, Long key, Long rKey, Long sKey) {
                assertEquals(proposalKey, key);
                assertEquals(saleAssociateKey, rKey);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setDemandKey(demandKey);
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setSource(Source.simulated);
                proposal.setState(State.confirmed);
                proposal.setDueDate(DateUtils.getNowDate());
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertEquals(State.closed, proposal.getState());
                return proposal;
            }
        });
        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> parameters, int limit) {
                fail("Call not expected");
                return null;
            }
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long ownerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setState(State.confirmed);
                demand.setDueDate(DateUtils.getNowDate());
                demand.setOwnerKey(consumerKey);
                demand.setRawCommandId(originalRawCommandId);
                demand.setSource(Source.simulated);
                demand.setLocationKey(locationKey);
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
        // RawCommandOperations mock
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(originalRawCommandId, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(saleAssociateKey);
                rawCommand.setSource(Source.simulated);
                return rawCommand;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.close.toString());
        command.put(Proposal.PROPOSAL_KEY, proposalKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setSaleAssociateKey(saleAssociateKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        /* TODO: Re-enable when long_core_* messages are in!
        String sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1);
        Locale locale = Locale.ENGLISH;
        String demandRef = LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { demandKey }, locale);
        String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposalKey }, locale);

        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
        assertEquals(LabelExtractor.get("cp_command_close_proposal_closed_demand_to_close", new Object[] { proposalRef, demandRef }, locale), sentText);
        sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(proposalKey.toString()));
        assertEquals(LabelExtractor.get("cp_command_close_acknowledge_proposal_closing", new Object[] { proposalRef }, locale), sentText);
        */
    }

    @Test
    public void testProcessCommandCloseVII() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 5555L;
        final Long consumerKey = 6666L;
        final Long saConsumerRecordKey = 76325L;
        final Long saleAssociateKey = 7777L;
        final State proposalState = State.confirmed;

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
            public Proposal getProposal(PersistenceManager pm, Long key, Long rKey, Long sKey) {
                assertEquals(proposalKey, key);
                assertEquals(saleAssociateKey, rKey);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setSource(Source.simulated);
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertEquals(State.closed, proposal.getState());
                return proposal;
            }
        });
        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> parameters, int limit) {
                assertTrue(parameters.containsKey(Demand.PROPOSAL_KEYS));
                assertEquals(proposalKey, (Long) parameters.get(Demand.PROPOSAL_KEYS));
                assertTrue(parameters.containsKey(Command.STATE));
                assertEquals(proposalState.toString(), (String) parameters.get(Command.STATE));
                throw new IllegalArgumentException("Done in purpose");
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.close.toString());
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
        assertTrue(sentText.contains(proposalKey.toString()));
    }

    @Test
    public void testProcessCommandCloseIX() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 5555L;
        final Long consumerKey = 6666L;
        final Long saConsumerRecordKey = 76325L;
        final Long saleAssociateKey = 7777L;

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
            public Proposal getProposal(PersistenceManager pm, Long key, Long rKey, Long sKey) throws InvalidIdentifierException {
                assertEquals(proposalKey, key);
                assertEquals(saleAssociateKey, rKey);
                throw new InvalidIdentifierException("Done in purpose");
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertEquals(State.closed, proposal.getState());
                return proposal;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.close.toString());
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
        assertEquals(LabelExtractor.get("cp_command_close_invalid_proposal_id", Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandCloseX() throws TwitterException, DataSourceException, ClientException {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.close.toString());

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_close_invalid_parameters", Locale.ENGLISH), sentText);
    }
}
