package twetailer.task.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import javax.jdo.MockPersistenceManager;
import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.InvalidIdentifierException;
import twetailer.connector.BaseConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.DemandOperations;
import twetailer.dao.LocationOperations;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.RawCommand;
import twetailer.task.CommandLineParser;
import twetailer.task.TestCommandProcessor;
import twetailer.task.step.BaseSteps;
import twetailer.validator.CommandSettings.State;
import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class TestDemandCommandProcessor {

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
        new DemandCommandProcessor();
    }

    @Test
    public void testProcessExisitingDemandIa() throws Exception {
        final Long demandKey = 2222L;
        final Long locationKey = 3333L;
        final Long consumerKey = 34354L;

        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                // demand.setState(State.opened); // Default state
                demand.setKey(demandKey);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(demandKey, demand.getKey());
                assertEquals(State.opened, demand.getState());
                return demand;
            }
        });
        // LocationOperations mock
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location createLocation(PersistenceManager pm, JsonObject command) {
                Location location = new Location();
                location.setKey(locationKey);
                return location;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Demand.REFERENCE, demandKey);
        command.put(Location.POSTAL_CODE, "zzz");

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        DemandCommandProcessor.processDemandCommand(new MockPersistenceManager(), consumer, rawCommand, command, CommandLineParser.localizedPrefixes.get(Locale.ENGLISH), CommandLineParser.localizedActions.get(Locale.ENGLISH));

        /* TODO: Re-enable when long_core_* messages are in!
        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
        */
    }

    @Test
    public void testProcessExisitingDemandIb() throws Exception {
        final Long demandKey = 2222L;
        final Long locationKey = 3333L;
        final Long consumerKey = 34354L;

        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setState(State.published);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(demandKey, demand.getKey());
                assertEquals(State.opened, demand.getState());
                return demand;
            }
        });
        // LocationOperations mock
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location createLocation(PersistenceManager pm, JsonObject command) {
                Location location = new Location();
                location.setKey(locationKey);
                return location;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Demand.REFERENCE, demandKey);
        command.put(Location.POSTAL_CODE, "zzz");

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        DemandCommandProcessor.processDemandCommand(new MockPersistenceManager(), consumer, rawCommand, command, CommandLineParser.localizedPrefixes.get(Locale.ENGLISH), CommandLineParser.localizedActions.get(Locale.ENGLISH));

        /* TODO: Re-enable when long_core_* messages are in!
        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
        */
    }

    @Test
    public void testProcessExisitingDemandIc() throws Exception {
        final Long demandKey = 2222L;
        final Long locationKey = 3333L;
        final Long consumerKey = 34354L;

        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setState(State.invalid);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(demandKey, demand.getKey());
                assertEquals(State.opened, demand.getState());
                return demand;
            }
        });
        // LocationOperations mock
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location createLocation(PersistenceManager pm, JsonObject command) {
                Location location = new Location();
                location.setKey(locationKey);
                return location;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Demand.REFERENCE, demandKey);
        command.put(Location.POSTAL_CODE, "zzz");

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        DemandCommandProcessor.processDemandCommand(new MockPersistenceManager(), consumer, rawCommand, command, CommandLineParser.localizedPrefixes.get(Locale.ENGLISH), CommandLineParser.localizedActions.get(Locale.ENGLISH));

        /* TODO: Re-enable when long_core_* messages are in!
        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
        */
    }

    @Test
    public void testProcessExisitingDemandId() throws Exception {
        final Long demandKey = 2222L;
        final Long consumerKey = 34354L;

        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setState(State.confirmed); // Too late to update it
                return demand;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Demand.REFERENCE, demandKey);
        command.put(Demand.QUANTITY, 123L);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        DemandCommandProcessor.processDemandCommand(new MockPersistenceManager(), consumer, rawCommand, command, CommandLineParser.localizedPrefixes.get(Locale.ENGLISH), CommandLineParser.localizedActions.get(Locale.ENGLISH));

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        Locale locale = Locale.ENGLISH;
        String demandRef = LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { demandKey }, locale);
        String stateLabel = LabelExtractor.get("cp_tweet_state_part", new Object[] { State.confirmed.toString() }, locale);
        assertEquals(LabelExtractor.get("cp_command_demand_non_modifiable_state", new Object[] { demandRef, stateLabel}, Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessExisitingDemandII() throws Exception {
        final Long demandKey = 2222L;
        final Long locationKey = 3333L;
        final Long consumerKey = 34354L;

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
                assertEquals(demandKey, demand.getKey());
                assertEquals(State.opened, demand.getState());
                return demand;
            }
        });
        // LocationOperations mock
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location createLocation(PersistenceManager pm, JsonObject command) {
                throw new IllegalArgumentException("Done in purpose");
            }
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                Location location = new Location();
                location.setKey(locationKey);
                return location;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        DemandCommandProcessor.processDemandCommand(new MockPersistenceManager(), consumer, rawCommand, command, CommandLineParser.localizedPrefixes.get(Locale.ENGLISH), CommandLineParser.localizedActions.get(Locale.ENGLISH));

        /* TODO: Re-enable when long_core_* messages are in!
        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
        */
    }

    @Test
    public void testProcessExisitingDemandIII() throws Exception {
        final Long demandKey = 2222L;
        final Long consumerKey = 34354L;

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
                assertEquals(demandKey, demand.getKey());
                assertEquals(State.opened, demand.getState());
                return demand;
            }
        });
        // LocationOperations mock
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location createLocation(PersistenceManager pm, JsonObject command) {
                throw new IllegalArgumentException("Done in purpose");
            }
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                throw new InvalidIdentifierException("Done in purpose");
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Demand.REFERENCE, demandKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        DemandCommandProcessor.processDemandCommand(new MockPersistenceManager(), consumer, rawCommand, command, CommandLineParser.localizedPrefixes.get(Locale.ENGLISH), CommandLineParser.localizedActions.get(Locale.ENGLISH));

        /* TODO: Re-enable when long_core_* messages are in!
        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
        */
    }

    /***** ddd
    @Test
    public void testProcessFirstNewDemandI() throws Exception {
        final Long demandKey = 2222L;
        final Long locationKey = 3333L;
        final Long consumerKey = 34354L;

        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
                return new ArrayList<Demand>();
            }
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setLocationKey(locationKey);
                return demand;
            }
            @Override
            public Demand createDemand(PersistenceManager pm, Demand demand) {
                assertNull(demand.getKey());
                assertEquals(State.opened, demand.getState());
                demand.setKey(demandKey);
                return demand;
            }
        });
        // LocationOperations mock
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location createLocation(PersistenceManager pm, JsonObject command) {
                Location location = new Location();
                location.setKey(locationKey);
                return location;
            }
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

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);
        command.put(Location.POSTAL_CODE, "zzz");

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        DemandCommandProcessor.processDemandCommand(new MockPersistenceManager(), consumer, rawCommand, command, CommandLineParser.localizedPrefixes.get(Locale.ENGLISH), CommandLineParser.localizedActions.get(Locale.ENGLISH));

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessFirstNewDemandII() throws Exception {
        final Long demandKey = 2222L;
        final Long locationKey = 3333L;
        final Long consumerKey = 34354L;

        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
                return new ArrayList<Demand>();
            }
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                return demand;
            }
            @Override
            public Demand createDemand(PersistenceManager pm, Demand demand) {
                assertNull(demand.getKey());
                assertEquals(State.opened, demand.getState());
                demand.setKey(demandKey);
                return demand;
            }
        });
        // LocationOperations mock
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location createLocation(PersistenceManager pm, JsonObject command) {
                throw new IllegalArgumentException("Done in purpose");
            }
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                Location location = new Location();
                location.setKey(locationKey);
                return location;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        DemandCommandProcessor.processDemandCommand(new MockPersistenceManager(), consumer, rawCommand, command, CommandLineParser.localizedPrefixes.get(Locale.ENGLISH), CommandLineParser.localizedActions.get(Locale.ENGLISH));

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessFirstNewDemandIII() throws Exception {
        final Long demandKey = 2222L;
        final Long locationKey = 3333L;
        final Long consumerKey = 34354L;
        final String postalCode = "H0H0H0";
        final String countryCode = "CA";

        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
                return new ArrayList<Demand>();
            }
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setLocationKey(locationKey);
                return demand;
            }
            @Override
            public Demand createDemand(PersistenceManager pm, Demand demand) {
                assertNull(demand.getKey());
                assertEquals(State.opened, demand.getState());
                demand.setKey(demandKey);
                return demand;
            }
        });
        // LocationOperations mock
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location createLocation(PersistenceManager pm, JsonObject command) {
                fail("Call not expected!");
                return null;
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
        });

        // Command mock
        JsonObject command = new GenericJsonObject();

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setLocationKey(locationKey);

        DemandCommandProcessor.processDemandCommand(new MockPersistenceManager(), consumer, rawCommand, command, CommandLineParser.localizedPrefixes.get(Locale.ENGLISH), CommandLineParser.localizedActions.get(Locale.ENGLISH));

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
        assertTrue(sentText.contains(postalCode));
        assertTrue(sentText.contains(countryCode));
    }

    @Test
    public void testProcessFirstNewDemandIV() throws Exception {
        final Long demandKey = 2222L;
        final Long locationKey = 3333L;
        final Long consumerKey = 34354L;
        final String postalCode = "H0H0H0";
        final String countryCode = "CA";

        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
                return new ArrayList<Demand>();
            }
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setLocationKey(locationKey);
                return demand;
            }
            @Override
            public Demand createDemand(PersistenceManager pm, Demand demand) {
                assertNull(demand.getKey());
                assertEquals(State.opened, demand.getState());
                demand.setKey(demandKey);
                return demand;
            }
        });
        // LocationOperations mock
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location createLocation(PersistenceManager pm, JsonObject command) {
                assertEquals(locationKey.longValue(), command.getLong(Demand.LOCATION_KEY));
                Location location = new Location();
                location.setKey(locationKey);
                location.setPostalCode(postalCode);
                location.setCountryCode(countryCode);
                return location;
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
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Demand.LOCATION_KEY, locationKey);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        DemandCommandProcessor.processDemandCommand(new MockPersistenceManager(), consumer, rawCommand, command, CommandLineParser.localizedPrefixes.get(Locale.ENGLISH), CommandLineParser.localizedActions.get(Locale.ENGLISH));

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
        assertTrue(sentText.contains(postalCode));
        assertTrue(sentText.contains(countryCode));
    }

    @Test
    public void testProcessAdditionalNewDemandI() throws Exception {
        final Long demandKey = 2222L;
        final Long locationKey = 3333L;
        final Long consumerKey = 34354L;

        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
                Demand demand = new Demand() {
                    @Override public Long getLocationKey() { return null; }
                    @Override public Double getRange() { return null; }
                    @Override public String getRangeUnit() { return null; }
                };
                demand.setKey(demandKey);
                demand.setSource(Source.twitter); // Setup to verify it will be reset with the Source.simulated of the rawCommand
                List<Demand> demands = new ArrayList<Demand>();
                demands.add(demand);
                return demands;
            }
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                return demand;
            }
            @Override
            public Demand createDemand(PersistenceManager pm, Demand demand) {
                assertNull(demand.getKey());
                assertEquals(Source.simulated, demand.getSource()); // Verify the source attribute reset with the raw Command one
                assertEquals(State.opened, demand.getState());
                demand.setKey(demandKey);
                return demand;
            }
        });
        // LocationOperations mock
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location createLocation(PersistenceManager pm, JsonObject command) {
                throw new IllegalArgumentException("Done in purpose");
            }
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                Location location = new Location();
                location.setKey(locationKey);
                return location;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        DemandCommandProcessor.processDemandCommand(new MockPersistenceManager(), consumer, rawCommand, command, CommandLineParser.localizedPrefixes.get(Locale.ENGLISH), CommandLineParser.localizedActions.get(Locale.ENGLISH));

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessAdditionalNewDemandII() throws Exception {
        final Long demandKey = 2222L;
        final Long locationKey = 3333L;
        final Long consumerKey = 34354L;

        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setLocationKey(locationKey);
                demand.setSource(Source.twitter); // Setup to verify it will be reset with the Source.simulated of the rawCommand
                List<Demand> demands = new ArrayList<Demand>();
                demands.add(demand);
                return demands;
            }
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                return demand;
            }
            @Override
            public Demand createDemand(PersistenceManager pm, Demand demand) {
                assertNull(demand.getKey());
                assertEquals(Source.simulated, demand.getSource()); // Verify the source attribute reset with the raw Command one
                assertEquals(State.opened, demand.getState());
                demand.setKey(demandKey);
                return demand;
            }
        });
        // LocationOperations mock
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location createLocation(PersistenceManager pm, JsonObject command) {
                throw new IllegalArgumentException("Done in purpose");
            }
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                Location location = new Location();
                location.setKey(locationKey);
                return location;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        DemandCommandProcessor.processDemandCommand(new MockPersistenceManager(), consumer, rawCommand, command, CommandLineParser.localizedPrefixes.get(Locale.ENGLISH), CommandLineParser.localizedActions.get(Locale.ENGLISH));

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessAdditionalNewDemandIII() throws Exception {
        final Long demandKey = 2222L;
        final Long locationKey = 3333L;
        final Long consumerKey = 34354L;

        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setLocationKey(locationKey);
                demand.setSource(Source.twitter); // Setup to verify it will be reset with the Source.simulated of the rawCommand
                List<Demand> demands = new ArrayList<Demand>();
                demands.add(demand);
                return demands;
            }
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                return demand;
            }
            @Override
            public Demand createDemand(PersistenceManager pm, Demand demand) {
                assertNull(demand.getKey());
                assertEquals(Source.simulated, demand.getSource()); // Verify the source attribute reset with the raw Command one
                assertEquals(State.opened, demand.getState());
                demand.setKey(demandKey);
                return demand;
            }
        });
        // LocationOperations mock
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location createLocation(PersistenceManager pm, JsonObject command) {
                throw new IllegalArgumentException("Done in purpose");
            }
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                Location location = new Location();
                location.setKey(locationKey);
                return location;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setLocationKey(locationKey);

        DemandCommandProcessor.processDemandCommand(new MockPersistenceManager(), consumer, rawCommand, command, CommandLineParser.localizedPrefixes.get(Locale.ENGLISH), CommandLineParser.localizedActions.get(Locale.ENGLISH));

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessAdditionalNewDemandIV() throws Exception {
        final Long demandKey = 2222L;
        final Long locationKey = 3333L;
        final Long consumerKey = 54682L;

        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setLocationKey(locationKey);
                demand.setSource(Source.twitter); // Setup to verify it will be reset with the Source.simulated of the rawCommand
                List<Demand> demands = new ArrayList<Demand>();
                demands.add(demand);
                return demands;
            }
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long consumerKey) {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                return demand;
            }
            @Override
            public Demand createDemand(PersistenceManager pm, Demand demand) {
                assertNull(demand.getKey());
                assertEquals(Source.simulated, demand.getSource()); // Verify the source attribute reset with the raw Command one
                assertEquals(State.opened, demand.getState());
                demand.setKey(demandKey);
                return demand;
            }
        });
        // LocationOperations mock
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location createLocation(PersistenceManager pm, JsonObject command) {
                throw new IllegalArgumentException("Done in purpose");
            }
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                Location location = new Location();
                location.setKey(locationKey);
                return location;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Demand.LOCATION_KEY, locationKey);
        command.put(Demand.RANGE, 15.67);
        command.put(Demand.RANGE_UNIT, "km");

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setLocationKey(locationKey);

        DemandCommandProcessor.processDemandCommand(new MockPersistenceManager(), consumer, rawCommand, command, CommandLineParser.localizedPrefixes.get(Locale.ENGLISH), CommandLineParser.localizedActions.get(Locale.ENGLISH));

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessCommandDemandI() throws Exception {
        final Long consumerKey = 3333L;
        final Long locationKey = 4444L;
        final Long demandKey = 5555L;

        // LocationOperations mock
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location createLocation(PersistenceManager pm, JsonObject parameters) {
                Location location = new Location();
                location.setKey(locationKey);
                return location;
            }
        });
        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
                List<Demand> demands = new ArrayList<Demand>();
                return demands;
            }
            @Override
            public Demand createDemand(PersistenceManager pm, JsonObject parameters, Long consumerKey) {
                Demand demand = new Demand();
                demand.setKey(demandKey);
                return demand;
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.demand.toString());
        command.put(Location.POSTAL_CODE, RobotResponder.ROBOT_POSTAL_CODE);
        command.put(Location.COUNTRY_CODE, RobotResponder.ROBOT_COUNTRY_CODE);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setLocationKey(locationKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertTrue(sentText.contains(demandKey.toString()));
    }

    @Test
    public void testProcessCommandDemandII() throws Exception {
        final Long consumerKey = 3333L;
        final Long locationKey = 4444L;
        final Long demandKey = 5555L;

        // LocationOperations mock
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location createLocation(PersistenceManager pm, JsonObject parameters) {
                Location location = new Location();
                location.setKey(locationKey);
                return location;
            }
        });
        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long demandKey, Long consumerKey) {
                throw new RuntimeException("Done in purpose");
            }
        });

        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.demand.toString());
        command.put(Demand.REFERENCE, demandKey);
        command.put(Location.POSTAL_CODE, RobotResponder.ROBOT_POSTAL_CODE);
        command.put(Location.COUNTRY_CODE, RobotResponder.ROBOT_COUNTRY_CODE);

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setLocationKey(locationKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_demand_invalid_demand_id", Locale.ENGLISH), sentText);
    }
    ddd ****/
}
