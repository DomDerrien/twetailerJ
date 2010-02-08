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
import twetailer.dao.DemandOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.MockBaseOperations;
import twetailer.dao.ProposalOperations;
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
import twetailer.task.RobotResponder;
import twetailer.task.TestCommandProcessor;
import twetailer.validator.LocaleValidator;
import twetailer.validator.CommandSettings.Action;
import twitter4j.TwitterException;

import com.google.apphosting.api.MockAppEngineEnvironment;

import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

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

    @Test
    public void testProcessCommandListAllDemandsI() throws TwitterException, DataSourceException, ClientException {
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
    public void testProcessCommandListAllDemandsII() throws TwitterException, DataSourceException, ClientException {
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
        assertEquals(LabelExtractor.get("cp_command_list_demand_series_introduction", new Object[] { 1 }, Locale.ENGLISH), sentText);
        sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(0); // Last message with the demand details
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessCommandListAllDemandsIII() throws TwitterException, DataSourceException, ClientException {
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
        assertEquals(LabelExtractor.get("cp_command_list_demand_series_introduction", new Object[] { 1 }, Locale.ENGLISH), sentText);
        sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(0); // Last message with the demand details
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessCommandListOneDemandI() throws TwitterException, DataSourceException, ClientException {
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
    public void testProcessCommandListOneDemandII() throws TwitterException, DataSourceException, ClientException {
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
    public void testProcessCommandListOneDemandIII() throws TwitterException, DataSourceException, ClientException {
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
    public void testProcessCommandListOneProposalI() throws TwitterException, DataSourceException, ClientException {
        final Long proposalKey = 2222L;
        final Long consumerKey = 3333L;
        final Long saleAssociateKey = 4444L;
        final Long storeKey = 55555L;
        final String name = "sgrognegneu";

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
                saleAssociate.setStoreKey(storeKey);
                saleAssociate.setConsumerKey(consumerKey);
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

    @Test
    public void testProcessCommandListOneStoreI() throws TwitterException, DataSourceException, ClientException {
        final Long storeKey = 2222L;

        // StoreOperations mock
        final StoreOperations storeOperations = new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) {
                assertEquals(storeKey, key);
                Store store = new Store();
                store.setKey(storeKey);
                return store;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.storeOperations = storeOperations;

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
        final StoreOperations storeOperations = new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) {
                assertEquals(storeKey, key);
                Store store = new Store();
                store.setKey(storeKey);
                store.setLocationKey(locationKey);
                return store;
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
        CommandProcessor.storeOperations = storeOperations;
        CommandProcessor.locationOperations = locationOperations;

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
        final StoreOperations storeOperations = new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) throws DataSourceException {
                assertEquals(storeKey, key);
                throw new DataSourceException("Done in purpose");
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.storeOperations = storeOperations;

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
        final StoreOperations storeOperations = new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, List<Location> locations, int limit) {
                return new ArrayList<Store>();
            }
        };
        // LocationOperations mock
        final LocationOperations locationOperations = new LocationOperations() {
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
            public List<Location> getLocations(PersistenceManager pm, Location center, Double range, String rangeUnit, int limit) {
                return new ArrayList<Location>();
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.storeOperations = storeOperations;
        CommandProcessor.locationOperations = locationOperations;

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_list_no_store_in_location", new Object[] { postalCode, countryCode, range, rangeUnit }, Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandListAnyStoreII() throws TwitterException, DataSourceException, ClientException {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.list.toString());
        command.put(Store.STORE_KEY, Long.valueOf(-1L));

        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_list_store_missing_location", Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandListAnyStoreIII() throws TwitterException, DataSourceException, ClientException {
        final String postalCode = RobotResponder.ROBOT_POSTAL_CODE;
        final String countryCode = RobotResponder.ROBOT_COUNTRY_CODE;
        final Double range = LocaleValidator.DEFAULT_RANGE;
        final String rangeUnit = LocaleValidator.DEFAULT_RANGE_UNIT;
        final Long locationKey = 12345L;
        final Long storeKey = 67890L;

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.list.toString());
        command.put(Store.STORE_KEY, Long.valueOf(-1L));
        command.put(Location.POSTAL_CODE, postalCode);
        command.put(Location.COUNTRY_CODE, countryCode);
        command.put(Demand.RANGE, range);
        command.put(Demand.RANGE_UNIT, rangeUnit);

        // StoreOperations mock
        final StoreOperations storeOperations = new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, List<Location> locations, int limit) {
                Store store = new Store();
                store.setKey(storeKey);
                store.setLocationKey(locationKey);
                List<Store> stores = new ArrayList<Store>();
                stores.add(store);
                return stores;
            }
        };
        // LocationOperations mock
        final LocationOperations locationOperations = new LocationOperations() {
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
            public List<Location> getLocations(PersistenceManager pm, Location center, Double range, String rangeUnit, int limit) {
                return new ArrayList<Location>();
            }
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                assertEquals(locationKey, key);
                Location location = new Location();
                location.setKey(locationKey);
                location.setPostalCode(postalCode);
                location.setCountryCode(countryCode);
                return location;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.storeOperations = storeOperations;
        CommandProcessor.locationOperations = locationOperations;

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        CommandProcessor.processCommand(new MockPersistenceManager(), new Consumer(), rawCommand, command);

        String sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1); // First message of the series with the introduction
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_list_store_series_introduction", new Object[] { 1 }, Locale.ENGLISH), sentText);
        sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(0); // Last message with the demand details
        assertNotNull(sentText);
        assertTrue(sentText.contains(storeKey.toString()));
        assertTrue(sentText.contains(postalCode));
        assertTrue(sentText.contains(countryCode));
    }

    @Test
    public void testProcessCommandListAnyStoreIV() throws Exception {
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

        // LocationOperations mock
        final LocationOperations locationOperations = new LocationOperations() {
            @Override
            public List<Location> getLocations(PersistenceManager pm, String pCode, String cCode) {
                assertEquals(postalCode, pCode);
                assertEquals(countryCode, cCode);
                List<Location> locations = new ArrayList<Location>();
                return locations;
            }
        };
        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.locationOperations = locationOperations;

        // RawCommand mock
        final Long rawCommandKey = 12345L;
        RawCommand rawCommand = new RawCommand(Source.simulated);
        rawCommand.setKey(rawCommandKey);

        // Consumer mock
        final Long consumerKey = 12345L;
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        MockAppEngineEnvironment appEnv = new MockAppEngineEnvironment();
        appEnv.setUp();
        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);
        appEnv.tearDown();

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_list_store_with_new_location", new Object[] { postalCode, countryCode }, Locale.ENGLISH), sentText);
    }
}
