package twetailer.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javamocks.util.logging.MockLogger;

import javax.jdo.MockPersistenceManager;
import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.connector.BaseConnector;
import twetailer.connector.MockTwitterConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.InfluencerOperations;
import twetailer.dao.MockBaseOperations;
import twetailer.dao.RawCommandOperations;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.RawCommand;
import twetailer.task.step.BaseSteps;
import twetailer.validator.CommandSettings;
import twetailer.validator.LocaleValidator;
import twetailer.validator.CommandSettings.State;
import twitter4j.DirectMessage;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import domderrien.i18n.DateUtils;
import domderrien.i18n.LabelExtractor;

public class TestDemandValidator {

    final Long ownerKey = 54321L;
    final String consumerTwitterId = "Katelyn";
    final Source source = Source.simulated;

    private static LocalServiceTestHelper  helper;

    @BeforeClass
    public static void setUpBeforeClass() {
        DemandValidator.setMockLogger(new MockLogger("test", null));
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    }

    @Before
    public void setUp() throws Exception {
        helper.setUp();

        // ConsumerOperations mock
        ConsumerOperations consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(ownerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(ownerKey);
                consumer.setTwitterId(consumerTwitterId);
                consumer.setLanguage(LocaleValidator.DEFAULT_LANGUAGE);
                return consumer;
            }
        };

        // Install the mocks
        BaseSteps.resetOperationControllers(true);
        BaseSteps.setMockBaseOperations(new MockBaseOperations());
        BaseSteps.setMockConsumerOperations(consumerOperations);
        BaseSteps.setMockInfluencerOperations(new InfluencerOperations());

        // Be sure to start with a clean message stack
        BaseConnector.resetLastCommunicationInSimulatedMode();
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();

        MockTwitterConnector.restoreTwitterConnector();
    }

    @Test
    public void testConstructor() {
        new DemandValidator();
    }

    @Test(expected=InvalidIdentifierException.class)
    public void testProcessNoDemand() throws DataSourceException, InvalidIdentifierException {
        final Long demandKey = 12345L;

        // DemandOperations mock
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                throw new InvalidIdentifierException("Done in purpose");
            }
        });

        // Process the test case
        DemandValidator.process(demandKey);

        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessOneDemandInIncorrectState() throws DataSourceException, InvalidIdentifierException {
        final Long demandKey = 67890L;
        final Long locationKey = 12345L;
        final Double demandRange = 25.75D;
        final Demand consumerDemand = new Demand();
        consumerDemand.setContent("test");
        consumerDemand.setKey(demandKey);
        consumerDemand.setLocationKey(locationKey);
        consumerDemand.setRange(demandRange);
        consumerDemand.setState(State.invalid); // Not published

        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long OwnerKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(OwnerKey);
                return consumerDemand;
            }
        });

        DemandValidator.process(demandKey);

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessI() throws DataSourceException, InvalidIdentifierException {
        //
        // Invalid content
        //

        // RawCommandOperations mock
        final Long rawCommandKey = 111L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        });

        // DemandOperations mock
        final Long demandKey = 67890L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand(); // getCriteria() was returning null
                demand.setKey(demandKey);
                demand.setOwnerKey(ownerKey);
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
        });

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessII() throws DataSourceException, InvalidIdentifierException {
        //
        // Invalid content
        //

        // RawCommandOperations mock
        final Long rawCommandKey = 111L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        });

        // DemandOperations mock
        final Long demandKey = 67890L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setOwnerKey(ownerKey);
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
        });

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessIIIa() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid content
        // Invalid due date
        //

        // RawCommandOperations mock
        final Long rawCommandKey = 111L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        });

        // DemandOperations mock
        final Long demandKey = 67890L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public Date getDueDate() {
                        return null;
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(ownerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.setContent("test");
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        });

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessIVa() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid content
        // Invalid due date
        //

        // RawCommandOperations mock
        final Long rawCommandKey = 111L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        });

        // DemandOperations mock
        final Long demandKey = 67890L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public Date getDueDate() {
                        return new Date(12345L);
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(ownerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.setContent("test");
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        });

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessIIIb() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid content
        // Valid due date
        // Invalid expiration date
        //

        // RawCommandOperations mock
        final Long rawCommandKey = 111L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        });

        // DemandOperations mock
        final Long demandKey = 67890L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public Date getExpirationDate() {
                        return null;
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(ownerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.setContent("test");
                Calendar dueDate = DateUtils.getNowCalendar();
                dueDate.set(Calendar.MONTH, dueDate.get(Calendar.MONTH) + 1);
                demand.setDueDate(dueDate.getTime());
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        });

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessIVb() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid content
        // Valid due date
        // Invalid expiration date
        //

        // RawCommandOperations mock
        final Long rawCommandKey = 111L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        });

        // DemandOperations mock
        final Long demandKey = 67890L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public Date getExpirationDate() {
                        return new Date(12345L);
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(ownerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.setContent("test");
                Calendar dueDate = DateUtils.getNowCalendar();
                dueDate.set(Calendar.MONTH, dueDate.get(Calendar.MONTH) + 1);
                demand.setDueDate(dueDate.getTime());
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        });

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessVb() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid content
        // Valid due date
        // Valid expiration date
        // Invalid range
        //

        // RawCommandOperations mock
        final Long rawCommandKey = 111L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        });

        // DemandOperations mock
        final Long demandKey = 67890L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public Double getRange() {
                        return null;
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(ownerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.setContent("test");
                Calendar dueDate = DateUtils.getNowCalendar();
                dueDate.set(Calendar.MONTH, dueDate.get(Calendar.MONTH) + 1);
                demand.setDueDate(dueDate.getTime());
                demand.setRangeUnit(LocaleValidator.KILOMETER_UNIT);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        });

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessVIa() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid content
        // Valid due date
        // Valid expiration date
        // Invalid range
        //

        // RawCommandOperations mock
        final Long rawCommandKey = 111L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        });

        // DemandOperations mock
        final Long demandKey = 67890L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public Double getRange() {
                        return 0.0D;
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(ownerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.setContent("test");
                Calendar dueDate = DateUtils.getNowCalendar();
                dueDate.set(Calendar.MONTH, dueDate.get(Calendar.MONTH) + 1);
                demand.setDueDate(dueDate.getTime());
                demand.setRangeUnit(LocaleValidator.KILOMETER_UNIT);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        });

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessVIb() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid content
        // Valid due date
        // Valid expiration date
        // Invalid range
        //

        // RawCommandOperations mock
        final Long rawCommandKey = 111L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        });

        // DemandOperations mock
        final Long demandKey = 67890L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public Double getRange() {
                        return 100000000000000.0D;
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(ownerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.setContent("test");
                Calendar dueDate = DateUtils.getNowCalendar();
                dueDate.set(Calendar.MONTH, dueDate.get(Calendar.MONTH) + 1);
                demand.setDueDate(dueDate.getTime());
                demand.setRangeUnit(LocaleValidator.KILOMETER_UNIT);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        });

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessVII() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid content
        // Valid due date
        // Valid expiration date
        // Invalid range
        //

        // RawCommandOperations mock
        final Long rawCommandKey = 111L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        });

        // DemandOperations mock
        final Long demandKey = 67890L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public Double getRange() {
                        return null;
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(ownerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.setContent("test");
                Calendar dueDate = DateUtils.getNowCalendar();
                dueDate.set(Calendar.MONTH, dueDate.get(Calendar.MONTH) + 1);
                demand.setDueDate(dueDate.getTime());
                demand.setRangeUnit(LocaleValidator.MILE_UNIT);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        });

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessVIIIa() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid content
        // Valid due date
        // Valid expiration date
        // Invalid range
        //

        // RawCommandOperations mock
        final Long rawCommandKey = 111L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        });

        // DemandOperations mock
        final Long demandKey = 67890L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public Double getRange() {
                        return 0.0D;
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(ownerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.setContent("test");
                Calendar dueDate = DateUtils.getNowCalendar();
                dueDate.set(Calendar.MONTH, dueDate.get(Calendar.MONTH) + 1);
                demand.setDueDate(dueDate.getTime());
                demand.setRangeUnit(LocaleValidator.MILE_UNIT);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        });

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessVIIIb() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid content
        // Valid due date
        // Valid expiration date
        // Invalid range
        //

        // RawCommandOperations mock
        final Long rawCommandKey = 111L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        });

        // DemandOperations mock
        final Long demandKey = 67890L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public Double getRange() {
                        return 1000000000000000000.0D;
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(ownerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.setContent("test");
                Calendar dueDate = DateUtils.getNowCalendar();
                dueDate.set(Calendar.MONTH, dueDate.get(Calendar.MONTH) + 1);
                demand.setDueDate(dueDate.getTime());
                demand.setRangeUnit(LocaleValidator.MILE_UNIT);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        });

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessIXa() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid content
        // Valid due date
        // Valid expiration date
        // Valid range
        // Invalid quantity
        //

        // RawCommandOperations mock
        final Long rawCommandKey = 111L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        });

        // DemandOperations mock
        final Long demandKey = 67890L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public Long getQuantity() {
                        return null;
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(ownerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.setContent("test");
                Calendar dueDate = DateUtils.getNowCalendar();
                dueDate.set(Calendar.MONTH, dueDate.get(Calendar.MONTH) + 1);
                demand.setDueDate(dueDate.getTime());
                demand.setRangeUnit(LocaleValidator.KILOMETER_UNIT);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        });

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessIXb() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid content
        // Valid due date
        // Valid expiration date
        // Valid range
        // Invalid quantity
        //

        // RawCommandOperations mock
        final Long rawCommandKey = 111L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        });

        // DemandOperations mock
        final Long demandKey = 67890L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public Long getQuantity() {
                        return null;
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(ownerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.setContent("test");
                Calendar dueDate = DateUtils.getNowCalendar();
                dueDate.set(Calendar.MONTH, dueDate.get(Calendar.MONTH) + 1);
                demand.setDueDate(dueDate.getTime());
                demand.setRangeUnit(LocaleValidator.MILE_UNIT);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        });

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessX() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid content
        // Valid due date
        // Valid expiration date
        // Valid range
        // Invalid quantity
        //

        // RawCommandOperations mock
        final Long rawCommandKey = 111L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        });

        // DemandOperations mock
        final Long demandKey = 67890L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public Long getQuantity() {
                        return 0L;
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(ownerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.setContent("test");
                Calendar dueDate = DateUtils.getNowCalendar();
                dueDate.set(Calendar.MONTH, dueDate.get(Calendar.MONTH) + 1);
                demand.setDueDate(dueDate.getTime());
                demand.setRangeUnit(LocaleValidator.KILOMETER_UNIT);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        });

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessXI() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid content
        // Valid due date
        // Valid expiration date
        // Valid range
        // Valid quantity
        // Invalid location key
        //

        // RawCommandOperations mock
        final Long rawCommandKey = 111L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        });

        // DemandOperations mock
        final Long demandKey = 67890L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
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
                demand.setOwnerKey(ownerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.setContent("test");
                Calendar dueDate = DateUtils.getNowCalendar();
                dueDate.set(Calendar.MONTH, dueDate.get(Calendar.MONTH) + 1);
                demand.setDueDate(dueDate.getTime());
                demand.setRangeUnit(LocaleValidator.KILOMETER_UNIT);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        });

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessXII() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid content
        // Valid due date
        // Valid expiration date
        // Valid range
        // Valid quantity
        // Invalid location key
        //

        // RawCommandOperations mock
        final Long rawCommandKey = 111L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        });

        // DemandOperations mock
        final Long demandKey = 67890L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public Long getLocationKey() {
                        return 0L;
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(ownerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.setContent("test");
                Calendar dueDate = DateUtils.getNowCalendar();
                dueDate.set(Calendar.MONTH, dueDate.get(Calendar.MONTH) + 1);
                demand.setDueDate(dueDate.getTime());
                demand.setRangeUnit(LocaleValidator.KILOMETER_UNIT);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        });

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    /**** ddd
    @Test
    public void testProcessXIII() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid content
        // Valid due date
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

        // RawCommandOperations mock
        final Long rawCommandKey = 111L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        });

        // DemandOperations mock
        final Long demandKey = 67890L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public Long getLocationKey() {
                        return locationKey;
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(ownerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.setContent("test");
                Calendar dueDate = DateUtils.getNowCalendar();
                dueDate.set(Calendar.MONTH, dueDate.get(Calendar.MONTH) + 1);
                demand.setDueDate(dueDate.getTime());
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
        });

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessXIV() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid content
        // Valid due date
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
                location.setKey(locationKey);
                location.setLatitude(0.0D);
                location.setLongitude(Location.INVALID_COORDINATE);
                location.setPostalCode(RobotResponder.ROBOT_POSTAL_CODE);
                location.setCountryCode(RobotResponder.ROBOT_COUNTRY_CODE);
                return location;
            }
        };

        // RawCommandOperations mock
        final Long rawCommandKey = 111L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        });

        // ConsumerOperations mock
        DemandValidator.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(ownerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(ownerKey);
                return consumer;
            }
            @Override
            public Consumer updateConsumer(PersistenceManager pm, Consumer consumer) {
                assertEquals(ownerKey, consumer.getKey());
                assertEquals(locationKey, consumer.getLocationKey());
                return consumer;
            }
        };

        // DemandOperations mock
        final Long demandKey = 67890L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public Long getLocationKey() {
                        return locationKey;
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(ownerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.setContent("test");
                Calendar dueDate = DateUtils.getNowCalendar();
                dueDate.set(Calendar.MONTH, dueDate.get(Calendar.MONTH) + 1);
                demand.setDueDate(dueDate.getTime());
                demand.setRangeUnit(LocaleValidator.KILOMETER_UNIT);
                demand.setLocationKey(locationKey);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                // assertEquals(CommandSettings.State.published, demand.getState());
                return demand;
            }
        });

        // Process the test case
        DemandValidator.process(demandKey);

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessXVa() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid content
        // Valid due date
        // Valid expiration date
        // Valid range
        // Valid quantity
        // Valid location key
        // Valid location coordinates => consumer don't want automatic locale update
        //

        // LocationOperations mock
        final Long locationKey = 54321L;
        DemandValidator.locationOperations = new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                assertEquals(locationKey, key);
                Location location = new Location();
                location.setKey(locationKey);
                location.setLatitude(0.0D);
                location.setLongitude(0.0D);
                return location;
            }
        };

        // RawCommandOperations mock
        final Long rawCommandKey = 111L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        });

        // ConsumerOperations mock
        DemandValidator.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(ownerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(ownerKey);
                consumer.setAutomaticLocaleUpdate(Boolean.FALSE);
                return consumer;
            }
            @Override
            public Consumer updateConsumer(PersistenceManager pm, Consumer consumer) {
                assertEquals(ownerKey, consumer.getKey());
                assertEquals(locationKey, consumer.getLocationKey());
                fail("Call not expected!");
                return consumer;
            }
        };

        // DemandOperations mock
        final Long demandKey = 67890L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public Long getLocationKey() {
                        return locationKey;
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(ownerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.setContent("test");
                Calendar dueDate = DateUtils.getNowCalendar();
                dueDate.set(Calendar.MONTH, dueDate.get(Calendar.MONTH) + 1);
                demand.setDueDate(dueDate.getTime());
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
        });

        // Process the test case
        DemandValidator.process(demandKey);

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessXVb() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid content
        // Valid due date
        // Valid expiration date
        // Valid range
        // Valid quantity
        // Valid location key
        // Valid location coordinates => consumer not updated because already located there
        //

        // LocationOperations mock
        final Long locationKey = 54321L;
        DemandValidator.locationOperations = new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                assertEquals(locationKey, key);
                Location location = new Location();
                location.setKey(locationKey);
                location.setLatitude(0.0D);
                location.setLongitude(0.0D);
                return location;
            }
        };

        // RawCommandOperations mock
        final Long rawCommandKey = 111L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        });

        // ConsumerOperations mock
        DemandValidator.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(ownerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(ownerKey);
                consumer.setLocationKey(locationKey);
                return consumer;
            }
            @Override
            public Consumer updateConsumer(PersistenceManager pm, Consumer consumer) {
                assertEquals(ownerKey, consumer.getKey());
                assertEquals(locationKey, consumer.getLocationKey());
                fail("Call not expected!");
                return consumer;
            }
        };

        // DemandOperations mock
        final Long demandKey = 67890L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public Long getLocationKey() {
                        return locationKey;
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(ownerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.setContent("test");
                Calendar dueDate = DateUtils.getNowCalendar();
                dueDate.set(Calendar.MONTH, dueDate.get(Calendar.MONTH) + 1);
                demand.setDueDate(dueDate.getTime());
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
        });

        // Process the test case
        DemandValidator.process(demandKey);

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessXVc() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid content
        // Valid due date
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
                location.setKey(locationKey);
                location.setLatitude(0.0D);
                location.setLongitude(0.0D);
                return location;
            }
        };

        // RawCommandOperations mock
        final Long rawCommandKey = 111L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        });

        // ConsumerOperations mock
        DemandValidator.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(ownerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(ownerKey);
                return consumer;
            }
            @Override
            public Consumer updateConsumer(PersistenceManager pm, Consumer consumer) {
                assertEquals(ownerKey, consumer.getKey());
                assertEquals(locationKey, consumer.getLocationKey());
                return consumer;
            }
        };

        // DemandOperations mock
        final Long demandKey = 67890L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public Long getLocationKey() {
                        return locationKey;
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(ownerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.setContent("test");
                Calendar dueDate = DateUtils.getNowCalendar();
                dueDate.set(Calendar.MONTH, dueDate.get(Calendar.MONTH) + 1);
                demand.setDueDate(dueDate.getTime());
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
        });

        // Process the test case
        DemandValidator.process(demandKey);

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessXVI() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid content
        // Valid due date
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
            public Location getLocation(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                throw new DataSourceException("done in purpose");
            }
        };

        // RawCommandOperations mock
        final Long rawCommandKey = 111L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        });

        // DemandOperations mock
        final Long demandKey = 67890L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand() {
                    @Override
                    public Long getLocationKey() {
                        return locationKey;
                    }
                };
                demand.setKey(demandKey);
                demand.setOwnerKey(ownerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.setContent("test");
                Calendar dueDate = DateUtils.getNowCalendar();
                dueDate.set(Calendar.MONTH, dueDate.get(Calendar.MONTH) + 1);
                demand.setDueDate(dueDate.getTime());
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
        });

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }
    ddd ****/

    @Test
    public void testProcessXVII() throws DataSourceException, InvalidIdentifierException {
        //
        // Impossible to get the Demand instance
        //

        // ConsumerOperations mock
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(ownerKey, key);
                throw new InvalidIdentifierException("done in purpose");
            }
        });

        // RawCommandOperations mock
        final Long rawCommandKey = 111L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        });

        // DemandOperations mock
        final Long demandKey = 67890L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setOwnerKey(ownerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                return demand;
            }
        });

        // Process the test case
        DemandValidator.process(demandKey);

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    @SuppressWarnings({ "serial", "deprecation" })
    public void testProcessXVIII() throws DataSourceException, InvalidIdentifierException {
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

        // RawCommandOperations mock
        final Long rawCommandKey = 111L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(Source.twitter);
                return rawCommand;
            }
        });

        // DemandOperations mock
        final Long demandKey = 67890L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setOwnerKey(ownerKey);
                demand.setSource(Source.twitter);
                demand.setRawCommandId(rawCommandKey);
                return demand;
            }
        });

        // Process the test case
        DemandValidator.process(demandKey);

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testFilterHashTagsI() throws ClientException, DataSourceException {
        Demand demand = new Demand() {
            @Override
            public List<String> getHashTags() {
                return null;
            }
        };
        RequestValidator.filterHashTags(new MockPersistenceManager(), new Consumer(), demand, "demand");
        assertNull(demand.getHashTags());
    }

    @Test
    public void testFilterHashTagsII() throws ClientException, DataSourceException {
        Demand demand = new Demand();
        RequestValidator.filterHashTags(new MockPersistenceManager(), new Consumer(), demand, "demand");
        assertNotNull(demand.getHashTags());
        assertEquals(0, demand.getHashTags().size());
    }

    @Test
    public void testFilterHashTagsIII() throws ClientException, DataSourceException {
        Demand demand = new Demand();
        demand.addHashTag(RobotResponder.ROBOT_DEMO_HASH_TAG);
        RequestValidator.filterHashTags(new MockPersistenceManager(), new Consumer(), demand, "demand");
        assertEquals(1, demand.getHashTags().size());
        assertEquals(RobotResponder.ROBOT_DEMO_HASH_TAG, demand.getHashTags().get(0));
    }

    @Test
    public void testFilterHashTagsIV() throws ClientException, DataSourceException {
        Demand demand = new Demand();
        demand.addHashTag(RobotResponder.ROBOT_DEMO_HASH_TAG);
        demand.addHashTag(RobotResponder.ROBOT_DEMO_HASH_TAG);
        demand.addHashTag(RobotResponder.ROBOT_DEMO_HASH_TAG);
        RequestValidator.filterHashTags(new MockPersistenceManager(), new Consumer(), demand, "demand");
        assertEquals(1, demand.getHashTags().size());
        assertEquals(RobotResponder.ROBOT_DEMO_HASH_TAG, demand.getHashTags().get(0));
    }

    @Test
    public void testFilterHashTagsV() throws ClientException, DataSourceException {
        final Long demandKey = 67890L;
        Demand demand = new Demand();
        demand.setKey(demandKey);
        demand.setSource(Source.simulated);
        demand.addHashTag("test");
        RequestValidator.filterHashTags(new MockPersistenceManager(), new Consumer(), demand, "demand");
        assertEquals(0, demand.getHashTags().size());

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        String demandRef = LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { demand.getKey() }, Locale.ENGLISH);
        String tags = LabelExtractor.get("cp_tweet_tags_part", new Object[] { "test" }, Locale.ENGLISH);
        assertEquals(LabelExtractor.get("dv_report_hashtag_warning", new Object[] { demandRef, tags }, Locale.ENGLISH), sentText);
    }

    @Test
    public void testFilterHashTagsVI() throws ClientException, DataSourceException {
        final Long demandKey = 67890L;
        Demand demand = new Demand();
        demand.setKey(demandKey);
        demand.setSource(Source.simulated);
        demand.addHashTag(RobotResponder.ROBOT_DEMO_HASH_TAG);
        demand.addHashTag("unit");
        demand.addHashTag("test");
        RequestValidator.filterHashTags(new MockPersistenceManager(), new Consumer(), demand, "demand");
        assertEquals(1, demand.getHashTags().size());
        assertEquals(RobotResponder.ROBOT_DEMO_HASH_TAG, demand.getHashTags().get(0));

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        String demandRef = LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { demand.getKey() }, Locale.ENGLISH);
        String tags = LabelExtractor.get("cp_tweet_tags_part", new Object[] { "unit test" }, Locale.ENGLISH);
        assertEquals(LabelExtractor.get("dv_report_hashtag_warning", new Object[] { demandRef, tags }, Locale.ENGLISH), sentText);
    }

    @Test
    public void testFilterHashTagsVII() throws ClientException, DataSourceException {
        final Long demandKey = 67890L;
        Demand demand = new Demand();
        demand.setKey(demandKey);
        demand.setSource(Source.simulated);
        demand.addHashTag("unit");
        demand.addHashTag("test");
        demand.addHashTag(RobotResponder.ROBOT_DEMO_HASH_TAG);
        RequestValidator.filterHashTags(new MockPersistenceManager(), new Consumer(), demand, "demand");
        assertEquals(1, demand.getHashTags().size());
        assertEquals(RobotResponder.ROBOT_DEMO_HASH_TAG, demand.getHashTags().get(0));

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        String demandRef = LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { demand.getKey() }, Locale.ENGLISH);
        String tags = LabelExtractor.get("cp_tweet_tags_part", new Object[] { "unit test" }, Locale.ENGLISH);
        assertEquals(LabelExtractor.get("dv_report_hashtag_warning", new Object[] { demandRef, tags }, Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessXIXa() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid contente
        // Invalid due date
        //

        // RawCommandOperations mock
        final Long rawCommandKey = 111L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        });

        // DemandOperations mock
        final Long demandKey = 67890L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setOwnerKey(ownerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.setContent("test");
                Calendar dueDate = DateUtils.getNowCalendar();
                dueDate.set(Calendar.YEAR, dueDate.get(Calendar.YEAR) + 2);
                demand.setDueDate(dueDate.getTime());
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        });

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessXIXb() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid content
        // Valid due date
        // Invalid expiration date
        //

        // RawCommandOperations mock
        final Long rawCommandKey = 111L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        });

        // DemandOperations mock
        final Long demandKey = 67890L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setOwnerKey(ownerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.setContent("test");
                Calendar dueDate = DateUtils.getNowCalendar();
                dueDate.set(Calendar.MONTH, dueDate.get(Calendar.MONTH) + 1);
                demand.setDueDate(dueDate.getTime());
                Calendar expirationDate = DateUtils.getNowCalendar();
                expirationDate.set(Calendar.YEAR, expirationDate.get(Calendar.YEAR) + 2);
                demand.setExpirationDate(expirationDate.getTime());
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        });

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessXX() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid content
        // Valid due date
        // Valid expiration date => but expiration date after due date!
        //

        // RawCommandOperations mock
        final Long rawCommandKey = 111L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        });

        // DemandOperations mock
        final Long demandKey = 67890L;
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setOwnerKey(ownerKey);
                demand.setSource(source);
                demand.setRawCommandId(rawCommandKey);
                demand.setContent("test");
                Calendar dueDate = DateUtils.getNowCalendar();
                dueDate.set(Calendar.MONTH, dueDate.get(Calendar.MONTH) + 1);
                demand.setDueDate(dueDate.getTime());
                Calendar expirationDate = DateUtils.getNowCalendar();
                expirationDate.set(Calendar.MONTH, expirationDate.get(Calendar.MONTH) + 2);
                demand.setExpirationDate(expirationDate.getTime());
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        });

        // Process the test case
        DemandValidator.process(demandKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(demandKey.toString()));
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }
}
