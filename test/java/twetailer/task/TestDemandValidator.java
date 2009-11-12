package twetailer.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.DataSourceException;
import twetailer.connector.BaseConnector;
import twetailer.connector.MockTwitterConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.MockAppEngineEnvironment;
import twetailer.dao.MockPersistenceManager;
import twetailer.dao.RawCommandOperations;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.RawCommand;
import twetailer.validator.CommandSettings;
import twetailer.validator.LocaleValidator;
import twetailer.validator.CommandSettings.State;
import twitter4j.DirectMessage;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TestDemandValidator {

    private class MockBaseOperations extends BaseOperations {
        private PersistenceManager pm = new MockPersistenceManager();
        @Override
        public PersistenceManager getPersistenceManager() {
            return pm;
        }
    };

    final Long OwnerKey = 54321L;
    final String consumerTwitterId = "Katelyn";
    final Source source = Source.simulated;
    MockAppEngineEnvironment appEnv;

    @Before
    public void setUp() throws Exception {
        // ConsumerOperations mock
        ConsumerOperations consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(OwnerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(OwnerKey);
                consumer.setTwitterId(consumerTwitterId);
                consumer.setLanguage(LocaleValidator.DEFAULT_LANGUAGE);
                return consumer;
            }
        };

        // Install the mocks
        DemandValidator._baseOperations = new MockBaseOperations();
        DemandValidator.consumerOperations = consumerOperations;

        // Be sure to start with a clean message stack
        BaseConnector.resetLastCommunicationInSimulatedMode();

        // App Engine Environment mock
        appEnv = new MockAppEngineEnvironment();
        appEnv.setUp();
    }

    @After
    public void tearDown() throws Exception {
        DemandValidator._baseOperations = new BaseOperations();
        DemandValidator.consumerOperations = DemandValidator._baseOperations.getConsumerOperations();
        DemandValidator.demandOperations = DemandValidator._baseOperations.getDemandOperations();
        DemandValidator.locationOperations = DemandValidator._baseOperations.getLocationOperations();
        DemandValidator.rawCommandOperations = DemandValidator._baseOperations.getRawCommandOperations();

        appEnv.tearDown();
    }

    @Test
    public void testConstructor() {
        new DemandValidator();
    }

    @Test(expected=DataSourceException.class)
    public void testProcessNoDemand() throws DataSourceException {
        final Long demandKey = 12345L;

        // DemandOperations mock
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                throw new DataSourceException("Done in purpose");
            }
        };

        // Process the test case
        DemandValidator.process(demandKey);

        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessOneDemandInIncorrectState() throws DataSourceException {
        final Long demandKey = 67890L;
        final Long locationKey = 12345L;
        final Double demandRange = 25.75D;
        final Demand consumerDemand = new Demand();
        consumerDemand.addCriterion("test");
        consumerDemand.setKey(demandKey);
        consumerDemand.setLocationKey(locationKey);
        consumerDemand.setRange(demandRange);
        consumerDemand.setState(State.invalid); // Not published

        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long OwnerKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(OwnerKey);
                return consumerDemand;
            }
        };

        DemandValidator.process(demandKey);

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessI() throws DataSourceException {
        //
        // Invalid criteria
        //

        // RawCommandOperation mock
        final Long rawCommandKey = 111L;
        DemandValidator.rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        };

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public List<String> getCriteria() {
                        return null;
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(OwnerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        };

        // Process the test case
        DemandValidator.process(demandKey);

        System.err.println(BaseConnector.getLastCommunicationInSimulatedMode());
        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessII() throws DataSourceException {
        //
        // Invalid criteria
        //

        // RawCommandOperation mock
        final Long rawCommandKey = 111L;
        DemandValidator.rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        };

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public List<String> getCriteria() {
                        return new ArrayList<String>();
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(OwnerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        };

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessIII() throws DataSourceException {
        //
        // Valid criteria
        // Invalid expiration date
        //

        // RawCommandOperation mock
        final Long rawCommandKey = 111L;
        DemandValidator.rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        };

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public Date getExpirationDate() {
                        return null;
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(OwnerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.addCriterion("test");
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        };

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessIV() throws DataSourceException {
        //
        // Valid criteria
        // Invalid expiration date
        //

        // RawCommandOperation mock
        final Long rawCommandKey = 111L;
        DemandValidator.rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        };

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public Date getExpirationDate() {
                        return new Date(12345L);
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(OwnerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.addCriterion("test");
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        };

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessV() throws DataSourceException {
        //
        // Valid criteria
        // Valid expiration date
        // Invalid range
        //

        // RawCommandOperation mock
        final Long rawCommandKey = 111L;
        DemandValidator.rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        };

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public Double getRange() {
                        return null;
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(OwnerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.addCriterion("test");
                demand.setRangeUnit(LocaleValidator.KILOMETER_UNIT);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        };

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessVIa() throws DataSourceException {
        //
        // Valid criteria
        // Valid expiration date
        // Invalid range
        //

        // RawCommandOperation mock
        final Long rawCommandKey = 111L;
        DemandValidator.rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        };

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public Double getRange() {
                        return 0.0D;
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(OwnerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.addCriterion("test");
                demand.setRangeUnit(LocaleValidator.KILOMETER_UNIT);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        };

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessVIb() throws DataSourceException {
        //
        // Valid criteria
        // Valid expiration date
        // Invalid range
        //

        // RawCommandOperation mock
        final Long rawCommandKey = 111L;
        DemandValidator.rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        };

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public Double getRange() {
                        return 100000000000000.0D;
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(OwnerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.addCriterion("test");
                demand.setRangeUnit(LocaleValidator.KILOMETER_UNIT);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        };

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessVII() throws DataSourceException {
        //
        // Valid criteria
        // Valid expiration date
        // Invalid range
        //

        // RawCommandOperation mock
        final Long rawCommandKey = 111L;
        DemandValidator.rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        };

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public Double getRange() {
                        return null;
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(OwnerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.addCriterion("test");
                demand.setRangeUnit(LocaleValidator.MILE_UNIT);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        };

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessVIIIa() throws DataSourceException {
        //
        // Valid criteria
        // Valid expiration date
        // Invalid range
        //

        // RawCommandOperation mock
        final Long rawCommandKey = 111L;
        DemandValidator.rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        };

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public Double getRange() {
                        return 0.0D;
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(OwnerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.addCriterion("test");
                demand.setRangeUnit(LocaleValidator.MILE_UNIT);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        };

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessVIIIb() throws DataSourceException {
        //
        // Valid criteria
        // Valid expiration date
        // Invalid range
        //

        // RawCommandOperation mock
        final Long rawCommandKey = 111L;
        DemandValidator.rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        };

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public Double getRange() {
                        return 1000000000000000000.0D;
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(OwnerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.addCriterion("test");
                demand.setRangeUnit(LocaleValidator.MILE_UNIT);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        };

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessIXa() throws DataSourceException {
        //
        // Valid criteria
        // Valid expiration date
        // Valid range
        // Invalid quantity
        //

        // RawCommandOperation mock
        final Long rawCommandKey = 111L;
        DemandValidator.rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        };

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public Long getQuantity() {
                        return null;
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(OwnerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.addCriterion("test");
                demand.setRangeUnit(LocaleValidator.KILOMETER_UNIT);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        };

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessIXb() throws DataSourceException {
        //
        // Valid criteria
        // Valid expiration date
        // Valid range
        // Invalid quantity
        //

        // RawCommandOperation mock
        final Long rawCommandKey = 111L;
        DemandValidator.rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        };

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public Long getQuantity() {
                        return null;
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(OwnerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.addCriterion("test");
                demand.setRangeUnit(LocaleValidator.MILE_UNIT);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        };

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessX() throws DataSourceException {
        //
        // Valid criteria
        // Valid expiration date
        // Valid range
        // Invalid quantity
        //

        // RawCommandOperation mock
        final Long rawCommandKey = 111L;
        DemandValidator.rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        };

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public Long getQuantity() {
                        return 0L;
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(OwnerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.addCriterion("test");
                demand.setRangeUnit(LocaleValidator.KILOMETER_UNIT);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        };

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessXI() throws DataSourceException {
        //
        // Valid criteria
        // Valid expiration date
        // Valid range
        // Valid quantity
        // Invalid location key
        //

        // RawCommandOperation mock
        final Long rawCommandKey = 111L;
        DemandValidator.rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        };

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public Long getQuantity() {
                        return 10L;
                    }
                    @Override
                    public Long getLocationKey() {
                        return null;
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(OwnerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.addCriterion("test");
                demand.setRangeUnit(LocaleValidator.KILOMETER_UNIT);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        };

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessXII() throws DataSourceException {
        //
        // Valid criteria
        // Valid expiration date
        // Valid range
        // Valid quantity
        // Invalid location key
        //

        // RawCommandOperation mock
        final Long rawCommandKey = 111L;
        DemandValidator.rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        };

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public Long getLocationKey() {
                        return 0L;
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(OwnerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.addCriterion("test");
                demand.setRangeUnit(LocaleValidator.KILOMETER_UNIT);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        };

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessXIII() throws DataSourceException {
        //
        // Valid criteria
        // Valid expiration date
        // Valid range
        // Valid quantity
        // Valid location key
        // Invalid location coordinates and invalid resolution
        //

        // LocationOperations mock
        final Long locationKey = 54321L;
        DemandValidator.locationOperations = new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                assertEquals(locationKey, key);
                Location location = new Location() {
                    @Override
                    public String getCountryCode() {
                        return "zzz";
                    }
                };
                location.setLatitude(0.0D);
                location.setLongitude(Location.INVALID_COORDINATE);
                location.setPostalCode("zzz");
                return location;
            }
            @Override
            public Location updateLocation(PersistenceManager pm, Location location) {
                assertNotNull(location);
                assertEquals(90.0D, location.getLatitude(), 0.0D);
                assertEquals(0.0D, location.getLongitude(), 0.0D);
                return location;
            }
        };

        // RawCommandOperation mock
        final Long rawCommandKey = 111L;
        DemandValidator.rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        };

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public Long getLocationKey() {
                        return locationKey;
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(OwnerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.addCriterion("test");
                demand.setRangeUnit(LocaleValidator.KILOMETER_UNIT);
                demand.setLocationKey(locationKey);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        };

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessXIV() throws DataSourceException {
        //
        // Valid criteria
        // Valid expiration date
        // Valid range
        // Valid quantity
        // Valid location key
        // Invalid location coordinates with valid resolution
        //

        // LocationOperations mock
        final Long locationKey = 54321L;
        DemandValidator.locationOperations = new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                assertEquals(locationKey, key);
                Location location = new Location();
                location.setLatitude(0.0D);
                location.setLongitude(Location.INVALID_COORDINATE);
                location.setPostalCode(RobotResponder.ROBOT_POSTAL_CODE);
                location.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
                return location;
            }
        };

        // RawCommandOperation mock
        final Long rawCommandKey = 111L;
        DemandValidator.rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        };

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public Long getLocationKey() {
                        return locationKey;
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(OwnerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.addCriterion("test");
                demand.setRangeUnit(LocaleValidator.KILOMETER_UNIT);
                demand.setLocationKey(locationKey);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.published, demand.getState());
                return demand;
            }
        };

        // Process the test case
        DemandValidator.process(demandKey);

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessXV() throws DataSourceException {
        //
        // Valid criteria
        // Valid expiration date
        // Valid range
        // Valid quantity
        // Valid location key
        // Valid location coordinates
        //

        // LocationOperations mock
        final Long locationKey = 54321L;
        DemandValidator.locationOperations = new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                assertEquals(locationKey, key);
                Location location = new Location();
                location.setLatitude(0.0D);
                location.setLongitude(0.0D);
                return location;
            }
        };

        // RawCommandOperation mock
        final Long rawCommandKey = 111L;
        DemandValidator.rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        };

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public Long getLocationKey() {
                        return locationKey;
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(OwnerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.addCriterion("test");
                demand.setRangeUnit(LocaleValidator.KILOMETER_UNIT);
                demand.setLocationKey(locationKey);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.published, demand.getState());
                return demand;
            }
        };

        // Process the test case
        DemandValidator.process(demandKey);

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessXVI() throws DataSourceException {
        //
        // Valid criteria
        // Valid expiration date
        // Valid range
        // Valid quantity
        // Valid location key
        // Impossible to get the corresponding Location instance
        //

        // LocationOperations mock
        final Long locationKey = 54321L;
        DemandValidator.locationOperations = new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws DataSourceException {
                throw new DataSourceException("done in purpose");
            }
        };

        // RawCommandOperation mock
        final Long rawCommandKey = 111L;
        DemandValidator.rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        };

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public Long getLocationKey() {
                        return locationKey;
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(OwnerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.addCriterion("test");
                demand.setRangeUnit(LocaleValidator.KILOMETER_UNIT);
                demand.setLocationKey(locationKey);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        };

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessXVII() throws DataSourceException {
        //
        // Impossible to get the Demand instance
        //

        // ConsumerOperations mock
        DemandValidator.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) throws DataSourceException {
                assertEquals(OwnerKey, key);
                throw new DataSourceException("done in purpose");
            }
        };

        // RawCommandOperation mock
        final Long rawCommandKey = 111L;
        DemandValidator.rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        };

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setOwnerKey(OwnerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                return demand;
            }
        };

        // Process the test case
        DemandValidator.process(demandKey);

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    @SuppressWarnings("serial")
    public void testProcessXVIII() throws DataSourceException {
        //
        // Impossible to tweet the warnings
        //

        final Twitter mockTwitterAccount = new Twitter() {
            @Override
            public DirectMessage sendDirectMessage(String id, String text) throws TwitterException {
                throw new TwitterException("done in purpose");
            }
        };
        MockTwitterConnector.injectMockTwitterAccount(mockTwitterAccount);

        // RawCommandOperation mock
        final Long rawCommandKey = 111L;
        DemandValidator.rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(Source.twitter);
                return rawCommand;
            }
        };

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setOwnerKey(OwnerKey);
                demand.setSource(Source.twitter);
                demand.setRawCommandId(rawCommandKey);
                return demand;
            }
        };

        // Process the test case
        DemandValidator.process(demandKey);

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());

        MockTwitterConnector.restoreTwitterConnector(mockTwitterAccount, null);
    }
}
