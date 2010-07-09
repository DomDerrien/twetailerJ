package twetailer.task.command;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.Collator;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.task.TestCommandProcessor;
import twetailer.validator.LocaleValidator;

public class TestListCommandProcessor {

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
        new ListCommandProcessor();
    }

    /***** ddd
    @Test
    public void testProcessCommandListAllDemandsI() throws TwitterException, DataSourceException, ClientException {
        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> parameters, int limit) {
                return new ArrayList<Demand>();
            }
        });

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
    public void testProcessCommandListAllDemandsII() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 2222L;

        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> parameters, int limit) {
                Demand demand = new Demand();
                demand.setKey(demandKey);
                List<Demand> demands = new ArrayList<Demand>();
                demands.add(demand);
                return demands;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.list.toString());

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1); // First message of the series with the introduction
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_list_personal_demand_series_introduction", new Object[] { 1 }, Locale.ENGLISH), sentText);
        sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(0); // Last message with the demand details
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessCommandListAllDemandsIII() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 2222L;
        final Long locationKey = 3333L;

        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> parameters, int limit) {
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setLocationKey(locationKey);
                List<Demand> demands = new ArrayList<Demand>();
                demands.add(demand);
                return demands;
            }
        });
        // LocationOperations mock
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                assertEquals(locationKey, key);
                Location location = new Location();
                location.setKey(locationKey);
                return location;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.list.toString());

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1); // First message of the series with the introduction
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_list_personal_demand_series_introduction", new Object[] { 1 }, Locale.ENGLISH), sentText);
        sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(0); // Last message with the demand details
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessCommandListOneDemandI() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 2222L;

        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                return demand;
            }
        });

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
    public void testProcessCommandListOneDemandII() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 2222L;
        final Long locationKey = 3333L;

        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
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
                location.setKey(locationKey);
                return location;
            }
        });

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
    public void testProcessCommandListOneDemandIII() throws TwitterException, DataSourceException, ClientException {
        final Long demandKey = 2222L;

        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) throws DataSourceException {
                assertEquals(demandKey, key);
                throw new DataSourceException("Done in purpose");
            }
        });

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
    public void testProcessCommandListOneProposalI() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 2222L;
        final Long consumerKey = 3333L;
        final Long saleAssociateKey = 4444L;
        final Long storeKey = 55555L;
        final String name = "sgrognegneu";

        // ProposalOperations mock
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long rKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                // assertEquals(saleAssociateKey, rKey); // rKey now null to let consumer listing received proposals
                assertNull(rKey);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setStoreKey(storeKey);
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
                saleAssociate.setConsumerKey(consumerKey);
                return saleAssociate;
            }
        });
        // StoreOperations mock
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) {
                assertEquals(storeKey, key);
                Store store = new Store();
                store.setKey(storeKey);
                store.setName(name);
                return store;
            }
        });

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
    public void testProcessCommandListOneProposalII() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 2222L;
        final Long consumerKey = 3333L;
        final Long saleAssociateKey = 4444L;

        // ProposalOperations mock
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long rKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                // assertEquals(saleAssociateKey, rKey); // rKey now null to let consumer listing received proposals
                assertNull(rKey);
                throw new DataSourceException("Done in purpose");
            }
        });
        // SaleAssociateOperations mock
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                assertEquals(saleAssociateKey, key);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setConsumerKey(consumerKey);
                return saleAssociate;
            }
        });

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

    @Test
    public void testProcessCommandListOneProposalIII() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 2222L;
        final Long consumerKey = 3333L;
        final Long saleAssociateKey = 4444L;

        // ProposalOperations mock
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long rKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                // assertEquals(saleAssociateKey, rKey); // rKey now null to let consumer listing received proposals
                assertNull(rKey);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(saleAssociateKey * 2); // To let the system knows that it does not belong to the expected sale associate
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
                saleAssociate.setConsumerKey(consumerKey);
                return saleAssociate;
            }
        });

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

    @Test
    public void testProcessCommandListOneStoreI() throws TwitterException, DataSourceException, ClientException {
        final Long storeKey = 2222L;

        // StoreOperations mock
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) {
                assertEquals(storeKey, key);
                Store store = new Store();
                store.setKey(storeKey);
                return store;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.list.toString());
        command.put(Store.STORE_KEY, storeKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(storeKey.toString()));
    }

    @Test
    public void testProcessCommandListOneStoreII() throws TwitterException, DataSourceException, ClientException {
        final Long storeKey = 2222L;
        final Long locationKey = 3333L;

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
        // LocationOperations mock
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                assertEquals(locationKey, key);
                Location location = new Location();
                location.setKey(locationKey);
                return location;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.list.toString());
        command.put(Store.STORE_KEY, storeKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(storeKey.toString()));
    }

    @Test
    public void testProcessCommandListOneStoreIII() throws TwitterException, DataSourceException, ClientException {
        final Long storeKey = 2222L;

        // StoreOperations mock
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) throws DataSourceException {
                assertEquals(storeKey, key);
                throw new DataSourceException("Done in purpose");
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.list.toString());
        command.put(Store.STORE_KEY, storeKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_list_invalid_store_id", new Object[] { storeKey }, Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandListAnyStoreI() throws TwitterException, DataSourceException, ClientException {
        final String postalCode = RobotResponder.ROBOT_POSTAL_CODE;
        final String countryCode = RobotResponder.ROBOT_COUNTRY_CODE;
        final Double range = LocaleValidator.DEFAULT_RANGE;
        final String rangeUnit = LocaleValidator.DEFAULT_RANGE_UNIT;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.list.toString());
        command.put(Store.STORE_KEY, Long.valueOf(-1L));
        command.put(Location.POSTAL_CODE, postalCode);
        command.put(Location.COUNTRY_CODE, countryCode);
        command.put(Demand.RANGE, range);
        command.put(Demand.RANGE_UNIT, rangeUnit);

        // StoreOperations mock
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, Map<String, Object> parameters, List<Location> locations, int limit) {
                return new ArrayList<Store>();
            }
        });
        // LocationOperations mock
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public List<Location> getLocations(PersistenceManager pm, String pCode, String cCode) {
                assertEquals(postalCode, pCode);
                assertEquals(countryCode, cCode);
                Location location = new Location();
                location.setPostalCode(postalCode);
                location.setCountryCode(countryCode);
                List<Location> locations = new ArrayList<Location>();
                locations.add(location);
                return locations;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location center, Double range, String rangeUnit, boolean withStore, int limit) {
                return new ArrayList<Location>();
            }
        });

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(
                LabelExtractor.get(
                        "cp_command_list_no_store_in_location",
                        new Object[] {
                                LabelExtractor.get( "cp_tweet_locale_part", new Object[] { postalCode, countryCode }, Locale.ENGLISH),
                                LabelExtractor.get( "cp_tweet_range_part", new Object[] { range, rangeUnit }, Locale.ENGLISH)
                        },
                        Locale.ENGLISH
                ),
                sentText
        );

        final Long consumerKey = 12345L;
        final Long locationKey = 43542L;

        // ConsumerOperations mock
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                consumer.setLocationKey(locationKey);
                return consumer;
            }
        };
    }

    @Test
    public void testListProposalByConsumerI() throws DataSourceException, ClientException {
        final Long proposalKey = 2222L;
        final Long consumerKey = 3333L;
        final Long saleAssociateKey = 4444L;
        final Long storeKey = 55555L;
        final String name = "sgrognegneu";
        final Long demandKey = 6666L;

        // ProposalOperations mock
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long rKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                // assertEquals(saleAssociateKey, rKey); // rKey now null to let consumer listing received proposals
                assertNull(rKey);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setStoreKey(storeKey);
                proposal.setDemandKey(demandKey);
                return proposal;
            }
        });
        // StoreOperations mock
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) {
                assertEquals(storeKey, key);
                Store store = new Store();
                store.setKey(storeKey);
                store.setName(name);
                return store;
            }
        });
        // SaleAssociateOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) {
                assertEquals(demandKey, key);
                assertEquals(consumerKey, cKey);
                return new Demand();
            }
        });

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
    public void testListProposalByConsumerII() throws DataSourceException, ClientException {
        final Long proposalKey = 2222L;
        final Long consumerKey = 3333L;
        final Long storeKey = 55555L;
        final String name = "sgrognegneu";
        final Long demandKey = 6666L;

        // ProposalOperations mock
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long rKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                // assertEquals(saleAssociateKey, rKey); // rKey now null to let consumer listing received proposals
                assertNull(rKey);
                throw new DataSourceException("Done in purpose!");
            }
        });
        // StoreOperations mock
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) {
                assertEquals(storeKey, key);
                Store store = new Store();
                store.setKey(storeKey);
                store.setName(name);
                return store;
            }
        });
        // SaleAssociateOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) {
                assertEquals(demandKey, key);
                assertEquals(consumerKey, cKey);
                return new Demand();
            }
        });

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
        assertEquals(LabelExtractor.get("cp_command_list_invalid_proposal_id", Locale.ENGLISH), sentText);
    }

    @Test
    public void testListProposalByConsumerIII() throws DataSourceException, ClientException {
        final Long proposalKey = 2222L;
        final Long consumerKey = 3333L;
        final Long saleAssociateKey = 4444L;
        final Long storeKey = 55555L;
        final String name = "sgrognegneu";
        final Long demandKey = 6666L;

        // ProposalOperations mock
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long rKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                // assertEquals(saleAssociateKey, rKey); // rKey now null to let consumer listing received proposals
                assertNull(rKey);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setStoreKey(storeKey);
                proposal.setDemandKey(demandKey);
                return proposal;
            }
        });
        // StoreOperations mock
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) {
                assertEquals(storeKey, key);
                Store store = new Store();
                store.setKey(storeKey);
                store.setName(name);
                return store;
            }
        });
        // SaleAssociateOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertEquals(consumerKey, cKey);
                throw new DataSourceException("Done in purpose!"); // To simulate that the demand does not belong to the querying consumer
            }
        });

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
        assertEquals(LabelExtractor.get("cp_command_list_invalid_proposal_id", Locale.ENGLISH), sentText);
    }

    @Test
    public void testGetLocationI() throws DataSourceException, ClientException {
        final Long consumerKey = 12345L;

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        Location retreived = ListCommandProcessor.getLocation(
                new MockPersistenceManager(),
                consumer,
                new RawCommand(Source.simulated),
                new GenericJsonObject(),
                "Not important!"
        );

        assertNull(retreived);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("Not important!", Locale.ENGLISH), sentText);
    }

    @Test
    public void testGetLocationII() throws Exception {
        final Long consumerKey = 12345L;
        final Long rawCommandKey = 23456L;

        // LocationOperations mock
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public List<Location> getLocations(PersistenceManager pm, String postalCode, String countryCode) throws DataSourceException {
                assertEquals(RobotResponder.ROBOT_POSTAL_CODE, postalCode);
                assertEquals(RobotResponder.ROBOT_COUNTRY_CODE, countryCode);
                return new ArrayList<Location>();
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Location.POSTAL_CODE, RobotResponder.ROBOT_POSTAL_CODE);
        command.put(Location.COUNTRY_CODE, RobotResponder.ROBOT_COUNTRY_CODE);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);
        rawCommand.setKey(rawCommandKey);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        Location retreived = ListCommandProcessor.getLocation(new MockPersistenceManager(), consumer, rawCommand, command, "not important");

        assertNull(retreived);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(
                LabelExtractor.get(
                        "cp_command_list_with_new_location",
                        new Object[] { LabelExtractor.get("cp_tweet_locale_part", new Object[] { RobotResponder.ROBOT_POSTAL_CODE, RobotResponder.ROBOT_COUNTRY_CODE }, Locale.ENGLISH) },
                        Locale.ENGLISH),
                sentText
        );
    }

    @Test
    public void testGetLocationIII() throws Exception {
        final Long consumerKey = 12345L;
        final Long rawCommandKey = 23456L;
        final Long locationKey = 34567L;

        // LocationOperations mock
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public List<Location> getLocations(PersistenceManager pm, String postalCode, String countryCode) throws DataSourceException {
                assertEquals(RobotResponder.ROBOT_POSTAL_CODE, postalCode);
                assertEquals(RobotResponder.ROBOT_COUNTRY_CODE, countryCode);
                Location location = new Location();
                location.setKey(locationKey);
                location.setPostalCode(postalCode);
                location.setCountryCode(countryCode);
                List<Location> locations = new ArrayList<Location>();
                locations.add(location);
                return locations;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Location.POSTAL_CODE, RobotResponder.ROBOT_POSTAL_CODE);
        command.put(Location.COUNTRY_CODE, RobotResponder.ROBOT_COUNTRY_CODE);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);
        rawCommand.setKey(rawCommandKey);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        Location retreived = ListCommandProcessor.getLocation(new MockPersistenceManager(), consumer, rawCommand, command, "not important");

        assertEquals(locationKey, retreived.getKey());

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNull(sentText);
    }

    @Test
    public void testGetLocationIV() throws DataSourceException, ClientException {
        final Long consumerKey = 12345L;
        final Long locationKey = 23456L;

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setLocationKey(locationKey);

        // LocationOperations mock
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws DataSourceException {
                assertEquals(locationKey, key);
                Location location = new Location();
                location.setKey(locationKey);
                return location;
            }
        });

        Location retreived = ListCommandProcessor.getLocation(
                new MockPersistenceManager(),
                consumer,
                new RawCommand(Source.simulated),
                new GenericJsonObject(),
                "Not important!"
        );

        assertNotNull(retreived);
        assertEquals(locationKey, retreived.getKey());

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNull(sentText);
    }

    @Test
    public void testListDemandInAreaI() throws Exception {
        final Long consumerKey = 12345L;
        final Long rawCommandKey = 23456L;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.list.toString());
        command.put(Demand.REFERENCE, -1);
        command.put(Demand.RANGE, 3.14159);
        command.put(Demand.RANGE_UNIT, LocaleValidator.KILOMETER_UNIT);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);
        rawCommand.setKey(rawCommandKey);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(
                LabelExtractor.get(
                        "cp_command_list_demand_missing_location",
                        new Object[] { LabelExtractor.get( "cp_tweet_demand_reference_part", new Object[] { "*" }, Locale.ENGLISH) },
                        Locale.ENGLISH),
                sentText
        );
    }

    @Test
    public void testListDemandInAreaII() throws Exception {
        final Long consumerKey = 12345L;
        final Long rawCommandKey = 23456L;
        final Long locationKey = 34567L;

        // LocationOperations mock
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public List<Location> getLocations(PersistenceManager pm, String postalCode, String countryCode) throws DataSourceException {
                assertEquals(RobotResponder.ROBOT_POSTAL_CODE, postalCode);
                assertEquals(RobotResponder.ROBOT_COUNTRY_CODE, countryCode);
                Location location = new Location();
                location.setKey(locationKey);
                location.setPostalCode(postalCode);
                location.setCountryCode(countryCode);
                List<Location> locations = new ArrayList<Location>();
                locations.add(location);
                return locations;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) throws DataSourceException {
                assertEquals(locationKey, location.getKey());
                List<Location> locations = new ArrayList<Location>();
                // Data returned not important, getDemands() below verifies the array has a zero size
                return locations;
            }
        });
        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> parameters, List<Location> locations, int limit) throws DataSourceException {
                assertNotNull(locations);
                assertEquals(0, locations.size());
                List<Demand> demands = new ArrayList<Demand>();
                return demands;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.list.toString());
        command.put(Demand.REFERENCE, -1);
        command.put(Location.POSTAL_CODE, RobotResponder.ROBOT_POSTAL_CODE);
        command.put(Location.COUNTRY_CODE, RobotResponder.ROBOT_COUNTRY_CODE);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);
        rawCommand.setKey(rawCommandKey);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(
                LabelExtractor.get(
                        "cp_command_list_no_demand_in_location",
                        new Object[] {
                                LabelExtractor.get("cp_tweet_locale_part", new Object[] { RobotResponder.ROBOT_POSTAL_CODE, RobotResponder.ROBOT_COUNTRY_CODE }, Locale.ENGLISH),
                                LabelExtractor.get("cp_tweet_range_part", new Object[] { LocaleValidator.DEFAULT_RANGE, LocaleValidator.DEFAULT_RANGE_UNIT }, Locale.ENGLISH)
                        },
                        Locale.ENGLISH
                ),
                sentText
        );
    }

    @Test
    public void testListDemandInAreaIII() throws Exception {
        final Long consumerKey = 12345L;
        final Long rawCommandKey = 23456L;
        final Long locationKey = 34567L;
        final Long demandKey = 45678L;

        // LocationOperations mock
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public List<Location> getLocations(PersistenceManager pm, String postalCode, String countryCode) throws DataSourceException {
                assertEquals(RobotResponder.ROBOT_POSTAL_CODE, postalCode);
                assertEquals(RobotResponder.ROBOT_COUNTRY_CODE, countryCode);
                Location location = new Location();
                location.setKey(locationKey);
                location.setPostalCode(postalCode);
                location.setCountryCode(countryCode);
                List<Location> locations = new ArrayList<Location>();
                locations.add(location);
                return locations;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) throws DataSourceException {
                assertEquals(locationKey, location.getKey());
                List<Location> locations = new ArrayList<Location>();
                // Data returned not important, getDemands() below verifies the array has a zero size
                return locations;
            }
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws DataSourceException {
                assertEquals(locationKey, key);
                return new Location();
            }
        });
        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> parameters, List<Location> locations, int limit) throws DataSourceException {
                assertNotNull(locations);
                assertEquals(0, locations.size());
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setLocationKey(locationKey);
                List<Demand> demands = new ArrayList<Demand>();
                demands.add(demand);
                return demands;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.list.toString());
        command.put(Demand.REFERENCE, -1);
        command.put(Location.POSTAL_CODE, RobotResponder.ROBOT_POSTAL_CODE);
        command.put(Location.COUNTRY_CODE, RobotResponder.ROBOT_COUNTRY_CODE);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);
        rawCommand.setKey(rawCommandKey);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1);
        assertNotNull(sentText);
        assertEquals(
                LabelExtractor.get(
                         "cp_command_list_demand_series_introduction",
                         new Object[] {
                                 1,
                                 LabelExtractor.get("cp_tweet_locale_part", new Object[] { RobotResponder.ROBOT_POSTAL_CODE, RobotResponder.ROBOT_COUNTRY_CODE }, Locale.ENGLISH),
                                 LabelExtractor.get("cp_tweet_range_part", new Object[] { LocaleValidator.DEFAULT_RANGE, LocaleValidator.DEFAULT_RANGE_UNIT }, Locale.ENGLISH)
                         },
                         Locale.ENGLISH
                 ),
                 sentText
         );

        sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertFalse(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testListDemandInAreaIV() throws Exception {
        final Long consumerKey = 12345L;
        final Long rawCommandKey = 23456L;
        final Long locationKey = 34567L;
        final Long demandKey = 45678L;

        // LocationOperations mock
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public List<Location> getLocations(PersistenceManager pm, String postalCode, String countryCode) throws DataSourceException {
                assertEquals(RobotResponder.ROBOT_POSTAL_CODE, postalCode);
                assertEquals(RobotResponder.ROBOT_COUNTRY_CODE, countryCode);
                Location location = new Location();
                location.setKey(locationKey);
                location.setPostalCode(postalCode);
                location.setCountryCode(countryCode);
                List<Location> locations = new ArrayList<Location>();
                locations.add(location);
                return locations;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) throws DataSourceException {
                assertEquals(locationKey, location.getKey());
                List<Location> locations = new ArrayList<Location>();
                // Data returned not important, getDemands() below verifies the array has a zero size
                return locations;
            }
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws DataSourceException {
                assertEquals(locationKey, key);
                return new Location();
            }
        });
        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> parameters, List<Location> locations, int limit) throws DataSourceException {
                assertNotNull(locations);
                assertEquals(0, locations.size());
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setLocationKey(locationKey);
                demand.addCriterion("Not");
                demand.addCriterion("Important");
                List<Demand> demands = new ArrayList<Demand>();
                demands.add(demand);
                return demands;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.list.toString());
        command.put(Demand.REFERENCE, -1);
        command.put(Location.POSTAL_CODE, RobotResponder.ROBOT_POSTAL_CODE);
        command.put(Location.COUNTRY_CODE, RobotResponder.ROBOT_COUNTRY_CODE);
        JsonArray tags = new GenericJsonArray();
        tags.add("wii");
        command.put(Demand.CRITERIA_ADD, tags);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);
        rawCommand.setKey(rawCommandKey);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(
                LabelExtractor.get(
                        "cp_command_list_no_demand_in_location",
                        new Object[] {
                                LabelExtractor.get("cp_tweet_locale_part", new Object[] { RobotResponder.ROBOT_POSTAL_CODE, RobotResponder.ROBOT_COUNTRY_CODE }, Locale.ENGLISH),
                                LabelExtractor.get("cp_tweet_range_part", new Object[] { LocaleValidator.DEFAULT_RANGE, LocaleValidator.DEFAULT_RANGE_UNIT }, Locale.ENGLISH)
                        },
                        Locale.ENGLISH
                ),
                sentText
        );
    }

    @Test
    public void testListDemandInAreaV() throws Exception {
        final Long consumerKey = 12345L;
        final Long rawCommandKey = 23456L;
        final Long locationKey = 34567L;
        final Long demandKey = 45678L;

        // LocationOperations mock
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public List<Location> getLocations(PersistenceManager pm, String postalCode, String countryCode) throws DataSourceException {
                assertEquals(RobotResponder.ROBOT_POSTAL_CODE, postalCode);
                assertEquals(RobotResponder.ROBOT_COUNTRY_CODE, countryCode);
                Location location = new Location();
                location.setKey(locationKey);
                location.setPostalCode(postalCode);
                location.setCountryCode(countryCode);
                List<Location> locations = new ArrayList<Location>();
                locations.add(location);
                return locations;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) throws DataSourceException {
                assertEquals(locationKey, location.getKey());
                List<Location> locations = new ArrayList<Location>();
                // Data returned not important, getDemands() below verifies the array has a zero size
                return locations;
            }
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws DataSourceException {
                assertEquals(locationKey, key);
                return new Location();
            }
        });
        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> parameters, List<Location> locations, int limit) throws DataSourceException {
                assertNotNull(locations);
                assertEquals(0, locations.size());
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setLocationKey(locationKey);
                demand.addCriterion("console");
                demand.addCriterion("wii");
                demand.addCriterion("remote");
                List<Demand> demands = new ArrayList<Demand>();
                demands.add(demand);
                return demands;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.list.toString());
        command.put(Demand.REFERENCE, -1);
        command.put(Location.POSTAL_CODE, RobotResponder.ROBOT_POSTAL_CODE);
        command.put(Location.COUNTRY_CODE, RobotResponder.ROBOT_COUNTRY_CODE);
        JsonArray tags = new GenericJsonArray();
        tags.add("wii");
        command.put(Demand.CRITERIA, tags);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);
        rawCommand.setKey(rawCommandKey);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1);
        assertNotNull(sentText);
        assertEquals(
                LabelExtractor.get(
                         "cp_command_list_demand_series_introduction",
                         new Object[] {
                                 1,
                                 LabelExtractor.get("cp_tweet_locale_part", new Object[] { RobotResponder.ROBOT_POSTAL_CODE, RobotResponder.ROBOT_COUNTRY_CODE }, Locale.ENGLISH),
                                 LabelExtractor.get("cp_tweet_range_part", new Object[] { LocaleValidator.DEFAULT_RANGE, LocaleValidator.DEFAULT_RANGE_UNIT }, Locale.ENGLISH)
                         },
                         Locale.ENGLISH
                 ),
                 sentText
         );

        sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertFalse(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testListProposalInAreaI() throws Exception {
        final Long consumerKey = 12345L;
        final Long rawCommandKey = 23456L;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.list.toString());
        command.put(Proposal.PROPOSAL_KEY, -1);
        command.put(Demand.RANGE, 3.14159);
        command.put(Demand.RANGE_UNIT, LocaleValidator.KILOMETER_UNIT);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);
        rawCommand.setKey(rawCommandKey);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(
                LabelExtractor.get(
                        "cp_command_list_proposal_missing_location",
                        new Object[] { LabelExtractor.get( "cp_tweet_proposal_reference_part", new Object[] { "*" }, Locale.ENGLISH) },
                        Locale.ENGLISH),
                sentText
        );
    }

    @Test
    public void testListProposalInAreaII() throws Exception {
        final Long consumerKey = 12345L;
        final Long rawCommandKey = 23456L;
        final Long locationKey = 34567L;

        // LocationOperations mock
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public List<Location> getLocations(PersistenceManager pm, String postalCode, String countryCode) throws DataSourceException {
                assertEquals(RobotResponder.ROBOT_POSTAL_CODE, postalCode);
                assertEquals(RobotResponder.ROBOT_COUNTRY_CODE, countryCode);
                Location location = new Location();
                location.setKey(locationKey);
                location.setPostalCode(postalCode);
                location.setCountryCode(countryCode);
                List<Location> locations = new ArrayList<Location>();
                locations.add(location);
                return locations;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) throws DataSourceException {
                assertEquals(locationKey, location.getKey());
                List<Location> locations = new ArrayList<Location>();
                // Data returned not important, getProposals() below verifies the array has a zero size
                return locations;
            }
        });
        // ProposalOperations mock
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public List<Proposal> getProposals(PersistenceManager pm, Map<String, Object> parameters, List<Location> locations, int limit) throws DataSourceException {
                assertNotNull(locations);
                assertEquals(0, locations.size());
                List<Proposal> proposals = new ArrayList<Proposal>();
                return proposals;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.list.toString());
        command.put(Proposal.PROPOSAL_KEY, -1);
        command.put(Location.POSTAL_CODE, RobotResponder.ROBOT_POSTAL_CODE);
        command.put(Location.COUNTRY_CODE, RobotResponder.ROBOT_COUNTRY_CODE);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);
        rawCommand.setKey(rawCommandKey);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(
                LabelExtractor.get(
                        "cp_command_list_no_proposal_in_location",
                        new Object[] {
                                LabelExtractor.get("cp_tweet_locale_part", new Object[] { RobotResponder.ROBOT_POSTAL_CODE, RobotResponder.ROBOT_COUNTRY_CODE }, Locale.ENGLISH),
                                LabelExtractor.get("cp_tweet_range_part", new Object[] { LocaleValidator.DEFAULT_RANGE, LocaleValidator.DEFAULT_RANGE_UNIT }, Locale.ENGLISH)
                        },
                        Locale.ENGLISH
                ),
                sentText
        );
    }

    @Test
    public void testListProposalInAreaIII() throws Exception {
        final Long consumerKey = 12345L;
        final Long rawCommandKey = 23456L;
        final Long locationKey = 34567L;
        final Long proposalKey = 45678L;
        final Long storeKey = 56789L;

        // LocationOperations mock
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public List<Location> getLocations(PersistenceManager pm, String postalCode, String countryCode) throws DataSourceException {
                assertEquals(RobotResponder.ROBOT_POSTAL_CODE, postalCode);
                assertEquals(RobotResponder.ROBOT_COUNTRY_CODE, countryCode);
                Location location = new Location();
                location.setKey(locationKey);
                location.setPostalCode(postalCode);
                location.setCountryCode(countryCode);
                List<Location> locations = new ArrayList<Location>();
                locations.add(location);
                return locations;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) throws DataSourceException {
                assertEquals(locationKey, location.getKey());
                List<Location> locations = new ArrayList<Location>();
                // Data returned not important, getProposals() below verifies the array has a zero size
                return locations;
            }
        });
        // ProposalOperations mock
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public List<Proposal> getProposals(PersistenceManager pm, Map<String, Object> parameters, List<Location> locations, int limit) throws DataSourceException {
                assertNotNull(locations);
                assertEquals(0, locations.size());
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setStoreKey(storeKey);
                List<Proposal> proposals = new ArrayList<Proposal>();
                proposals.add(proposal);
                return proposals;
            }
        });
        // StoreOperations mock
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) throws DataSourceException {
                assertEquals(storeKey, key);
                return new Store();
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.list.toString());
        command.put(Proposal.PROPOSAL_KEY, -1);
        command.put(Location.POSTAL_CODE, RobotResponder.ROBOT_POSTAL_CODE);
        command.put(Location.COUNTRY_CODE, RobotResponder.ROBOT_COUNTRY_CODE);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);
        rawCommand.setKey(rawCommandKey);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1);
        assertNotNull(sentText);
        assertEquals(
                LabelExtractor.get(
                         "cp_command_list_proposal_series_introduction",
                         new Object[] {
                                 1,
                                 LabelExtractor.get("cp_tweet_locale_part", new Object[] { RobotResponder.ROBOT_POSTAL_CODE, RobotResponder.ROBOT_COUNTRY_CODE }, Locale.ENGLISH),
                                 LabelExtractor.get("cp_tweet_range_part", new Object[] { LocaleValidator.DEFAULT_RANGE, LocaleValidator.DEFAULT_RANGE_UNIT }, Locale.ENGLISH)
                         },
                         Locale.ENGLISH
                 ),
                 sentText
         );

        sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertFalse(sentText.contains(proposalKey.toString()));
    }

    @Test
    public void testListProposalInAreaIV() throws Exception {
        final Long consumerKey = 12345L;
        final Long rawCommandKey = 23456L;
        final Long locationKey = 34567L;
        final Long proposalKey = 45678L;
        final Long storeKey = 56789L;

        // LocationOperations mock
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public List<Location> getLocations(PersistenceManager pm, String postalCode, String countryCode) throws DataSourceException {
                assertEquals(RobotResponder.ROBOT_POSTAL_CODE, postalCode);
                assertEquals(RobotResponder.ROBOT_COUNTRY_CODE, countryCode);
                Location location = new Location();
                location.setKey(locationKey);
                location.setPostalCode(postalCode);
                location.setCountryCode(countryCode);
                List<Location> locations = new ArrayList<Location>();
                locations.add(location);
                return locations;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) throws DataSourceException {
                assertEquals(locationKey, location.getKey());
                List<Location> locations = new ArrayList<Location>();
                // Data returned not important, getProposals() below verifies the array has a zero size
                return locations;
            }
        });
        // ProposalOperations mock
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public List<Proposal> getProposals(PersistenceManager pm, Map<String, Object> parameters, List<Location> locations, int limit) throws DataSourceException {
                assertNotNull(locations);
                assertEquals(0, locations.size());
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setStoreKey(storeKey);
                proposal.addCriterion("Not");
                proposal.addCriterion("Important");
                List<Proposal> proposals = new ArrayList<Proposal>();
                proposals.add(proposal);
                return proposals;
            }
        });
        // StoreOperations mock
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) throws DataSourceException {
                assertEquals(storeKey, key);
                return new Store();
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.list.toString());
        command.put(Proposal.PROPOSAL_KEY, -1);
        command.put(Location.POSTAL_CODE, RobotResponder.ROBOT_POSTAL_CODE);
        command.put(Location.COUNTRY_CODE, RobotResponder.ROBOT_COUNTRY_CODE);
        JsonArray tags = new GenericJsonArray();
        tags.add("wii");
        command.put(Proposal.CRITERIA_ADD, tags);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);
        rawCommand.setKey(rawCommandKey);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(
                LabelExtractor.get(
                        "cp_command_list_no_proposal_in_location",
                        new Object[] {
                                LabelExtractor.get("cp_tweet_locale_part", new Object[] { RobotResponder.ROBOT_POSTAL_CODE, RobotResponder.ROBOT_COUNTRY_CODE }, Locale.ENGLISH),
                                LabelExtractor.get("cp_tweet_range_part", new Object[] { LocaleValidator.DEFAULT_RANGE, LocaleValidator.DEFAULT_RANGE_UNIT }, Locale.ENGLISH)
                        },
                        Locale.ENGLISH
                ),
                sentText
        );
    }

    @Test
    public void testListProposalInAreaV() throws Exception {
        final Long consumerKey = 12345L;
        final Long rawCommandKey = 23456L;
        final Long locationKey = 34567L;
        final Long proposalKey = 45678L;
        final Long storeKey = 56789L;

        // LocationOperations mock
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public List<Location> getLocations(PersistenceManager pm, String postalCode, String countryCode) throws DataSourceException {
                assertEquals(RobotResponder.ROBOT_POSTAL_CODE, postalCode);
                assertEquals(RobotResponder.ROBOT_COUNTRY_CODE, countryCode);
                Location location = new Location();
                location.setKey(locationKey);
                location.setPostalCode(postalCode);
                location.setCountryCode(countryCode);
                List<Location> locations = new ArrayList<Location>();
                locations.add(location);
                return locations;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) throws DataSourceException {
                assertEquals(locationKey, location.getKey());
                List<Location> locations = new ArrayList<Location>();
                // Data returned not important, getProposals() below verifies the array has a zero size
                return locations;
            }
        });
        // ProposalOperations mock
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public List<Proposal> getProposals(PersistenceManager pm, Map<String, Object> parameters, List<Location> locations, int limit) throws DataSourceException {
                assertNotNull(locations);
                assertEquals(0, locations.size());
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setStoreKey(storeKey);
                proposal.addCriterion("console");
                proposal.addCriterion("wii");
                proposal.addCriterion("remote");
                List<Proposal> proposals = new ArrayList<Proposal>();
                proposals.add(proposal);
                return proposals;
            }
        });
        // StoreOperations mock
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) throws DataSourceException {
                assertEquals(storeKey, key);
                return new Store();
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.list.toString());
        command.put(Proposal.PROPOSAL_KEY, -1);
        command.put(Location.POSTAL_CODE, RobotResponder.ROBOT_POSTAL_CODE);
        command.put(Location.COUNTRY_CODE, RobotResponder.ROBOT_COUNTRY_CODE);
        JsonArray tags = new GenericJsonArray();
        tags.add("wii");
        command.put(Proposal.CRITERIA, tags);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);
        rawCommand.setKey(rawCommandKey);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1);
        assertNotNull(sentText);
        assertEquals(
                LabelExtractor.get(
                         "cp_command_list_proposal_series_introduction",
                         new Object[] {
                                 1,
                                 LabelExtractor.get("cp_tweet_locale_part", new Object[] { RobotResponder.ROBOT_POSTAL_CODE, RobotResponder.ROBOT_COUNTRY_CODE }, Locale.ENGLISH),
                                 LabelExtractor.get("cp_tweet_range_part", new Object[] { LocaleValidator.DEFAULT_RANGE, LocaleValidator.DEFAULT_RANGE_UNIT }, Locale.ENGLISH)
                         },
                         Locale.ENGLISH
                 ),
                 sentText
         );

        sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertFalse(sentText.contains(proposalKey.toString()));
    }

    @Test
    public void testListStoreInAreaI() throws Exception {
        final Long consumerKey = 12345L;
        final Long rawCommandKey = 23456L;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.list.toString());
        command.put(Store.STORE_KEY, -1);
        command.put(Demand.RANGE, 3.14159);
        command.put(Demand.RANGE_UNIT, LocaleValidator.KILOMETER_UNIT);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);
        rawCommand.setKey(rawCommandKey);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(
                LabelExtractor.get(
                        "cp_command_list_store_missing_location",
                        new Object[] { LabelExtractor.get( "cp_tweet_store_reference_part", new Object[] { "*" }, Locale.ENGLISH) },
                        Locale.ENGLISH),
                sentText
        );
    }

    @Test
    public void testListStoreInAreaII() throws Exception {
        final Long consumerKey = 12345L;
        final Long rawCommandKey = 23456L;
        final Long locationKey = 34567L;

        // LocationOperations mock
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public List<Location> getLocations(PersistenceManager pm, String postalCode, String countryCode) throws DataSourceException {
                assertEquals(RobotResponder.ROBOT_POSTAL_CODE, postalCode);
                assertEquals(RobotResponder.ROBOT_COUNTRY_CODE, countryCode);
                Location location = new Location();
                location.setKey(locationKey);
                location.setPostalCode(postalCode);
                location.setCountryCode(countryCode);
                List<Location> locations = new ArrayList<Location>();
                locations.add(location);
                return locations;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) throws DataSourceException {
                assertEquals(locationKey, location.getKey());
                List<Location> locations = new ArrayList<Location>();
                // Data returned not important, getStores() below verifies the array has a zero size
                return locations;
            }
        });
        // StoreOperations mock
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, Map<String, Object> parameters, List<Location> locations, int limit) throws DataSourceException {
                assertNotNull(locations);
                assertEquals(0, locations.size());
                List<Store> stores = new ArrayList<Store>();
                return stores;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.list.toString());
        command.put(Store.STORE_KEY, -1);
        command.put(Location.POSTAL_CODE, RobotResponder.ROBOT_POSTAL_CODE);
        command.put(Location.COUNTRY_CODE, RobotResponder.ROBOT_COUNTRY_CODE);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);
        rawCommand.setKey(rawCommandKey);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(
                LabelExtractor.get(
                        "cp_command_list_no_store_in_location",
                        new Object[] {
                                LabelExtractor.get("cp_tweet_locale_part", new Object[] { RobotResponder.ROBOT_POSTAL_CODE, RobotResponder.ROBOT_COUNTRY_CODE }, Locale.ENGLISH),
                                LabelExtractor.get("cp_tweet_range_part", new Object[] { LocaleValidator.DEFAULT_RANGE, LocaleValidator.DEFAULT_RANGE_UNIT }, Locale.ENGLISH)
                        },
                        Locale.ENGLISH
                ),
                sentText
        );
    }

    @Test
    public void testListStoreInAreaIII() throws Exception {
        final Long consumerKey = 12345L;
        final Long rawCommandKey = 23456L;
        final Long locationKey = 34567L;
        final Long storeKey = 45678L;

        // LocationOperations mock
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public List<Location> getLocations(PersistenceManager pm, String postalCode, String countryCode) throws DataSourceException {
                assertEquals(RobotResponder.ROBOT_POSTAL_CODE, postalCode);
                assertEquals(RobotResponder.ROBOT_COUNTRY_CODE, countryCode);
                Location location = new Location();
                location.setKey(locationKey);
                location.setPostalCode(postalCode);
                location.setCountryCode(countryCode);
                List<Location> locations = new ArrayList<Location>();
                locations.add(location);
                return locations;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) throws DataSourceException {
                assertEquals(locationKey, location.getKey());
                List<Location> locations = new ArrayList<Location>();
                // Data returned not important, getStores() below verifies the array has a zero size
                return locations;
            }
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws DataSourceException {
                assertEquals(locationKey, key);
                return new Location();
            }
        });
        // StoreOperations mock
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, Map<String, Object> parameters, List<Location> locations, int limit) throws DataSourceException {
                assertNotNull(locations);
                assertEquals(0, locations.size());
                Store store = new Store();
                store.setKey(storeKey);
                store.setLocationKey(locationKey);
                List<Store> stores = new ArrayList<Store>();
                stores.add(store);
                return stores;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.list.toString());
        command.put(Store.STORE_KEY, -1);
        command.put(Location.POSTAL_CODE, RobotResponder.ROBOT_POSTAL_CODE);
        command.put(Location.COUNTRY_CODE, RobotResponder.ROBOT_COUNTRY_CODE);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);
        rawCommand.setKey(rawCommandKey);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1);
        assertNotNull(sentText);
        assertEquals(
                LabelExtractor.get(
                         "cp_command_list_store_series_introduction",
                         new Object[] {
                                 1,
                                 LabelExtractor.get("cp_tweet_locale_part", new Object[] { RobotResponder.ROBOT_POSTAL_CODE, RobotResponder.ROBOT_COUNTRY_CODE }, Locale.ENGLISH),
                                 LabelExtractor.get("cp_tweet_range_part", new Object[] { LocaleValidator.DEFAULT_RANGE, LocaleValidator.DEFAULT_RANGE_UNIT }, Locale.ENGLISH)
                         },
                         Locale.ENGLISH
                 ),
                 sentText
         );

        sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(storeKey.toString())); // Not anonymized
    }
    ddd ****/

    private static Collator getCollator() {
        return LocaleValidator.getCollator(Locale.ENGLISH);
    }

    @Test
    public void testCheckIfIncludedI() {
        //
        // No match
        //
        List<Object> tags = Arrays.asList(new Object[] { "two", "four"});
        List<String> criteria = Arrays.asList(new String[] { "one", "three"});

        assertFalse(ListCommandProcessor.checkIfIncluded(getCollator(), tags, criteria));
    }

    @Test
    public void testCheckIfIncludedII() {
        //
        // One keyword matches exactly
        //
        List<Object> tags = Arrays.asList(new Object[] { "two", "four"});
        List<String> criteria = Arrays.asList(new String[] { "one", "two", "three"});

        assertTrue(ListCommandProcessor.checkIfIncluded(getCollator(), tags, criteria));
    }

    @Test
    public void testCheckIfIncludedIII() {
        //
        // Two keyword match exactly
        //
        List<Object> tags = Arrays.asList(new Object[] { "two", "four"});
        List<String> criteria = Arrays.asList(new String[] { "one", "two", "three", "four"});

        assertTrue(ListCommandProcessor.checkIfIncluded(getCollator(), tags, criteria));
    }

    @Test
    public void testCheckIfIncludedIV() {
        //
        // One keyword start matches
        //
        List<Object> tags = Arrays.asList(new Object[] { "tw*", "four"});
        List<String> criteria = Arrays.asList(new String[] { "one", "two", "three"});

        assertTrue(ListCommandProcessor.checkIfIncluded(getCollator(), tags, criteria));
    }

    @Test
    public void testCheckIfIncludedV() {
        //
        // One keyword start matches, with accented characters and mixed cases
        //
        List<Object> tags = Arrays.asList(new Object[] { "cérÉAL*"});
        List<String> criteria = Arrays.asList(new String[] { "cereals"});

        assertTrue(ListCommandProcessor.checkIfIncluded(getCollator(), tags, criteria));
    }

    @Test
    public void testCheckIfIncludedVI() {
        //
        // One keyword start matches, with accented characters and mixed cases
        //
        List<Object> tags = Arrays.asList(new Object[] { "cérÉALe*"});
        List<String> criteria = Arrays.asList(new String[] { "cereals"});

        assertFalse(ListCommandProcessor.checkIfIncluded(getCollator(), tags, criteria));
    }

    @Test
    public void testCheckIfIncludedVII() {
        //
        // One keyword start matches, with accented characters and mixed cases
        //
        List<Object> tags = Arrays.asList(new Object[] { "cérÉAL*"});
        List<String> criteria = Arrays.asList(new String[] { "cer"});

        assertFalse(ListCommandProcessor.checkIfIncluded(getCollator(), tags, criteria));
    }
}
