package twetailer.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Locale;

import javamocks.util.logging.MockLogger;

import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.connector.BaseConnector;
import twetailer.connector.MockTwitterConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.MockBaseOperations;
import twetailer.dao.ProposalOperations;
import twetailer.dao.RawCommandOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dao.StoreOperations;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Proposal;
import twetailer.dto.RawCommand;
import twetailer.dto.SaleAssociate;
import twetailer.dto.Store;
import twetailer.task.step.BaseSteps;
import twetailer.validator.CommandSettings.State;
import twitter4j.DirectMessage;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import domderrien.i18n.LabelExtractor;

public class TestProposalProcessor {

    @BeforeClass
    public static void setUpBeforeClass() {
        ProposalProcessor.setLogger(new MockLogger("test", null));
    }

    @Before
    public void setUp() throws Exception {
        BaseSteps.resetOperationControllers(true);
        BaseSteps.setMockBaseOperations(new MockBaseOperations());

        BaseConnector.resetLastCommunicationInSimulatedMode();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testConstructor() {
        new ProposalProcessor();
    }

    @Test(expected=InvalidIdentifierException.class)
    public void testProcessNoProposal() throws DataSourceException, InvalidIdentifierException {
        final Long proposalKey = 12345L;

        // ProposalOperations mock
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws InvalidIdentifierException {
                assertEquals(proposalKey, key);
                assertNull(cKey);
                assertNull(sKey);
                throw new InvalidIdentifierException("Done in purpose");
            }
        });

        ProposalProcessor.process(proposalKey);

        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessOneValidProposalIa() throws DataSourceException, InvalidIdentifierException {
        final Long proposalKey = 67890L;
        final Long demandKey = 12345L;
        final Double price = null;
        final Long quantity = 32L;
        final Long storeKey = 5555L;
        final Double total = 29.99D;
//        final String currency = "$";
        final Proposal proposal = new Proposal();
        proposal.addCriterion("test");
        proposal.setKey(proposalKey);
        proposal.setDemandKey(demandKey);
        proposal.setPrice(price);
        proposal.setQuantity(quantity);
        proposal.setState(State.published);
        proposal.setStoreKey(storeKey);
        proposal.setTotal(total);

        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws InvalidIdentifierException {
                assertEquals(proposalKey, key);
                assertNull(cKey);
                assertNull(sKey);
                assertNotNull(proposal.getCriteria());
                assertNotSame(0, proposal.getCriteria().size());
                return proposal;
            }
        });

        final Long rawCommandKey = 111L;
        final Source source = Source.simulated;
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

        final Long ownerKey = 6666L;
        final Demand demand = new Demand();
        demand.setKey(demandKey);
        demand.setOwnerKey(ownerKey);
        demand.setState(State.published);
        demand.setSource(source);
        demand.setRawCommandId(rawCommandKey);
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertTrue(demand.getProposalKeys().contains(proposalKey));
                return demand;
            }
        });

        final Consumer consumer = new Consumer();
        consumer.setKey(ownerKey);
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                return consumer;
            }
        });

        final String name = "sgrognegneu";
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

        ProposalProcessor.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        Locale locale = Locale.ENGLISH;
        String expectedMessage = LabelExtractor.get(
                "pp_inform_consumer_about_proposal",
                new Object[] {
                        LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposalKey }, locale),
                        LabelExtractor.get("cp_tweet_tags_part", new Object[] { proposal.getSerializedCriteria() }, locale),
                        LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { demandKey }, locale),
                        "", // No tags attached to this demand
                        LabelExtractor.get("cp_tweet_dueDate_part", new Object[] { CommandProcessor.serializeDate(demand.getDueDate()) }, locale),
                        LabelExtractor.get("cp_tweet_dueDate_part", new Object[] { CommandProcessor.serializeDate(demand.getExpirationDate()) }, locale),
                        LabelExtractor.get("cp_tweet_store_part", new Object[] { storeKey, name }, locale),
                        "", // No unit price attached to this demand
                        LabelExtractor.get("cp_tweet_total_part", new Object[] { proposal.getTotal(), "$" }, locale)
                },
                locale);
        assertEquals(
                expectedMessage,
                BaseConnector.getLastCommunicationInSimulatedMode()
        );
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessOneValidProposalIb() throws DataSourceException, InvalidIdentifierException {
        final Long proposalKey = 67890L;
        final Long demandKey = 12345L;
        final Double price = 0.0D;
        final Long quantity = 32L;
        final Long storeKey = 5555L;
        final Double total = 29.99D;
//        final String currency = "$";
        final Proposal proposal = new Proposal();
        proposal.addCriterion("test");
        proposal.setKey(proposalKey);
        proposal.setDemandKey(demandKey);
        proposal.setPrice(price);
        proposal.setQuantity(quantity);
        proposal.setState(State.published);
        proposal.setStoreKey(storeKey);
        proposal.setTotal(total);

        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws InvalidIdentifierException {
                assertEquals(proposalKey, key);
                assertNull(cKey);
                assertNull(sKey);
                assertNotNull(proposal.getCriteria());
                assertNotSame(0, proposal.getCriteria().size());
                return proposal;
            }
        });

        final Long rawCommandKey = 111L;
        final Source source = Source.simulated;
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

        final Long ownerKey = 6666L;
        final Demand demand = new Demand();
        demand.setKey(demandKey);
        demand.setOwnerKey(ownerKey);
        demand.setState(State.published);
        demand.setSource(source);
        demand.setRawCommandId(rawCommandKey);
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertTrue(demand.getProposalKeys().contains(proposalKey));
                return demand;
            }
        });

        final Consumer consumer = new Consumer();
        consumer.setKey(ownerKey);
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                return consumer;
            }
        });

        final String name = "sgrognegneu";
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

        ProposalProcessor.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        Locale locale = Locale.ENGLISH;
        String expectedMessage = LabelExtractor.get(
                "pp_inform_consumer_about_proposal",
                new Object[] {
                        LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposalKey }, locale),
                        LabelExtractor.get("cp_tweet_tags_part", new Object[] { proposal.getSerializedCriteria() }, locale),
                        LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { demandKey }, locale),
                        "", // No tags attached to this demand
                        LabelExtractor.get("cp_tweet_dueDate_part", new Object[] { CommandProcessor.serializeDate(demand.getDueDate()) }, locale),
                        LabelExtractor.get("cp_tweet_dueDate_part", new Object[] { CommandProcessor.serializeDate(demand.getExpirationDate()) }, locale),
                        LabelExtractor.get("cp_tweet_store_part", new Object[] { storeKey, name }, locale),
                        "", // No unit price attached to this proposal
                        LabelExtractor.get("cp_tweet_total_part", new Object[] { proposal.getTotal(), "$" }, locale)
                },
                locale);
        assertEquals(
                expectedMessage,
                BaseConnector.getLastCommunicationInSimulatedMode()
        );
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessOneValidProposalIIa() throws DataSourceException, InvalidIdentifierException {
        final Long proposalKey = 67890L;
        final Long demandKey = 12345L;
        final Double price = 25.75D;
        final Long quantity = 32L;
        final Long storeKey = 5555L;
        final Double total = null;
//        final String currency = "$";
        final Proposal proposal = new Proposal();
        proposal.addCriterion("test");
        proposal.setKey(proposalKey);
        proposal.setDemandKey(demandKey);
        proposal.setPrice(price);
        proposal.setQuantity(quantity);
        proposal.setState(State.published);
        proposal.setStoreKey(storeKey);
        proposal.setTotal(total);

        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws InvalidIdentifierException {
                assertEquals(proposalKey, key);
                assertNull(cKey);
                assertNull(sKey);
                assertNotNull(proposal.getCriteria());
                assertNotSame(0, proposal.getCriteria().size());
                return proposal;
            }
        });

        final Long rawCommandKey = 111L;
        final Source source = Source.simulated;
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

        final Long ownerKey = 6666L;
        final Demand demand = new Demand();
        demand.setKey(demandKey);
        demand.setOwnerKey(ownerKey);
        demand.setState(State.published);
        demand.setSource(source);
        demand.setRawCommandId(rawCommandKey);
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertTrue(demand.getProposalKeys().contains(proposalKey));
                return demand;
            }
        });

        final Consumer consumer = new Consumer();
        consumer.setKey(ownerKey);
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                return consumer;
            }
        });

        final String name = "sgrognegneu";
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

        ProposalProcessor.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        Locale locale = Locale.ENGLISH;
        String expectedMessage = LabelExtractor.get(
                "pp_inform_consumer_about_proposal",
                new Object[] {
                        LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposalKey }, locale),
                        LabelExtractor.get("cp_tweet_tags_part", new Object[] { proposal.getSerializedCriteria() }, locale),
                        LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { demandKey }, locale),
                        "", // No tags attached to this demand
                        LabelExtractor.get("cp_tweet_dueDate_part", new Object[] { CommandProcessor.serializeDate(demand.getDueDate()) }, locale),
                        LabelExtractor.get("cp_tweet_dueDate_part", new Object[] { CommandProcessor.serializeDate(demand.getExpirationDate()) }, locale),
                        LabelExtractor.get("cp_tweet_store_part", new Object[] { storeKey, name }, locale),
                        LabelExtractor.get("cp_tweet_price_part", new Object[] { proposal.getPrice(), "$" }, locale),
                        "" // No total cost attached to this proposal
                },
                locale);
        assertEquals(
                expectedMessage,
                BaseConnector.getLastCommunicationInSimulatedMode()
        );
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessOneValidProposalIIb() throws DataSourceException, InvalidIdentifierException {
        final Long proposalKey = 67890L;
        final Long demandKey = 12345L;
        final Double price = 25.75D;
        final Long quantity = 32L;
        final Long storeKey = 5555L;
        final Double total = 0.0D;
//        final String currency = "$";
        final Proposal proposal = new Proposal();
        proposal.addCriterion("test");
        proposal.setKey(proposalKey);
        proposal.setDemandKey(demandKey);
        proposal.setPrice(price);
        proposal.setQuantity(quantity);
        proposal.setState(State.published);
        proposal.setStoreKey(storeKey);
        proposal.setTotal(total);

        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws InvalidIdentifierException {
                assertEquals(proposalKey, key);
                assertNull(cKey);
                assertNull(sKey);
                assertNotNull(proposal.getCriteria());
                assertNotSame(0, proposal.getCriteria().size());
                return proposal;
            }
        });

        final Long rawCommandKey = 111L;
        final Source source = Source.simulated;
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

        final Long ownerKey = 6666L;
        final Demand demand = new Demand();
        demand.setKey(demandKey);
        demand.setOwnerKey(ownerKey);
        demand.setState(State.published);
        demand.setSource(source);
        demand.setRawCommandId(rawCommandKey);
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertTrue(demand.getProposalKeys().contains(proposalKey));
                return demand;
            }
        });

        final Consumer consumer = new Consumer();
        consumer.setKey(ownerKey);
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                return consumer;
            }
        });

        final String name = "sgrognegneu";
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

        ProposalProcessor.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        Locale locale = Locale.ENGLISH;
        String expectedMessage = LabelExtractor.get(
                "pp_inform_consumer_about_proposal",
                new Object[] {
                        LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposalKey }, locale),
                        LabelExtractor.get("cp_tweet_tags_part", new Object[] { proposal.getSerializedCriteria() }, locale),
                        LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { demandKey }, locale),
                        "", // No tags attached to this demand
                        LabelExtractor.get("cp_tweet_dueDate_part", new Object[] { CommandProcessor.serializeDate(demand.getDueDate()) }, locale),
                        LabelExtractor.get("cp_tweet_dueDate_part", new Object[] { CommandProcessor.serializeDate(demand.getExpirationDate()) }, locale),
                        LabelExtractor.get("cp_tweet_store_part", new Object[] { storeKey, name }, locale),
                        LabelExtractor.get("cp_tweet_price_part", new Object[] { proposal.getPrice(), "$" }, locale),
                        "" // No total cost attached to this proposal
                },
                locale);
        assertEquals(
                expectedMessage,
                BaseConnector.getLastCommunicationInSimulatedMode()
        );
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessOneValidProposalIII() throws DataSourceException, InvalidIdentifierException {
        final Long proposalKey = 67890L;
        final Long demandKey = 12345L;
        final Double price = 25.75D;
        final Long quantity = 32L;
        final Long storeKey = 5555L;
        final Double total = 37.95D;
//        final String currency = "$";
        final Proposal proposal = new Proposal();
        proposal.addCriterion("test");
        proposal.setKey(proposalKey);
        proposal.setDemandKey(demandKey);
        proposal.setPrice(price);
        proposal.setQuantity(quantity);
        proposal.setState(State.published);
        proposal.setStoreKey(storeKey);
        proposal.setTotal(total);

        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws InvalidIdentifierException {
                assertEquals(proposalKey, key);
                assertNull(cKey);
                assertNull(sKey);
                assertNotNull(proposal.getCriteria());
                assertNotSame(0, proposal.getCriteria().size());
                return proposal;
            }
        });

        final Long rawCommandKey = 111L;
        final Source source = Source.simulated;
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

        final Long ownerKey = 6666L;
        final Demand demand = new Demand();
        demand.setKey(demandKey);
        demand.setOwnerKey(ownerKey);
        demand.setState(State.published);
        demand.setSource(source);
        demand.setRawCommandId(rawCommandKey);
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertTrue(demand.getProposalKeys().contains(proposalKey));
                return demand;
            }
        });

        final Consumer consumer = new Consumer();
        consumer.setKey(ownerKey);
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                return consumer;
            }
        });

        final String name = "sgrognegneu";
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

        ProposalProcessor.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        Locale locale = Locale.ENGLISH;
        String expectedMessage = LabelExtractor.get(
                "pp_inform_consumer_about_proposal",
                new Object[] {
                        LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposalKey }, locale),
                        LabelExtractor.get("cp_tweet_tags_part", new Object[] { proposal.getSerializedCriteria() }, locale),
                        LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { demandKey }, locale),
                        "", // No tags attached to this demand
                        LabelExtractor.get("cp_tweet_dueDate_part", new Object[] { CommandProcessor.serializeDate(demand.getDueDate()) }, locale),
                        LabelExtractor.get("cp_tweet_dueDate_part", new Object[] { CommandProcessor.serializeDate(demand.getExpirationDate()) }, locale),
                        LabelExtractor.get("cp_tweet_store_part", new Object[] { storeKey, name }, locale),
                        LabelExtractor.get("cp_tweet_price_part", new Object[] { proposal.getPrice(), "$" }, locale),
                        LabelExtractor.get("cp_tweet_total_part", new Object[] { proposal.getTotal(), "$" }, locale)
                },
                locale);
        assertEquals(
                expectedMessage,
                BaseConnector.getLastCommunicationInSimulatedMode()
        );
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessOneInvalidProposal() throws DataSourceException, InvalidIdentifierException {
        final Long proposalKey = 67890L;
        final Long demandKey = 12345L;
        final Double price = 25.75D;
        final Long quantity = 32L;
        final Long storeKey = 65758L;
        final Double total = 29.99D;
        final Proposal proposal = new Proposal();
        proposal.addCriterion("test");
        proposal.setKey(proposalKey);
        proposal.setDemandKey(demandKey);
        proposal.setPrice(price);
        proposal.setQuantity(quantity);
        proposal.setState(State.invalid);
        proposal.setStoreKey(storeKey);
        proposal.setTotal(total);

        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws InvalidIdentifierException {
                assertEquals(proposalKey, key);
                assertNull(cKey);
                assertNull(sKey);
                return proposal;
            }
        });

        ProposalProcessor.process(proposalKey);

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessOneValidProposalWithFailureGettingDemand() throws DataSourceException, InvalidIdentifierException {
        final Long proposalKey = 67890L;
        final Long demandKey = 12345L;
        final Double price = 25.75D;
        final Long quantity = 32L;
        final Long storeKey = 65758L;
        final Double total = 29.99D;
        final Proposal proposal = new Proposal();
        proposal.addCriterion("test");
        proposal.setKey(proposalKey);
        proposal.setDemandKey(demandKey);
        proposal.setPrice(price);
        proposal.setQuantity(quantity);
        proposal.setState(State.published);
        proposal.setStoreKey(storeKey);
        proposal.setTotal(total);

        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws InvalidIdentifierException {
                assertEquals(proposalKey, key);
                assertNull(cKey);
                assertNull(sKey);
                return proposal;
            }
        });

        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                throw new InvalidIdentifierException("Done in purpose!");
            }
        });

        ProposalProcessor.process(proposalKey);

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    @SuppressWarnings({ "serial", "deprecation" })
    public void testProcessOneValidProposalWithFailureToSendMessageToConsumer() throws DataSourceException, InvalidIdentifierException {
        final Long proposalKey = 67890L;
        final Long demandKey = 12345L;
        final Date demandExpirationDate = new Date();
        final Double price = 25.75D;
        final Long quantity = 32L;
        final Long storeKey = 65758L;
        final Double total = 29.99D;

        final Proposal proposal = new Proposal();
        proposal.addCriterion("test");
        proposal.setKey(proposalKey);
        proposal.setDemandKey(demandKey);
        proposal.setPrice(price);
        proposal.setQuantity(quantity);
        proposal.setState(State.published);
        proposal.setStoreKey(storeKey);
        proposal.setTotal(total);

        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws InvalidIdentifierException {
                assertEquals(proposalKey, key);
                assertNull(cKey);
                assertNull(sKey);
                return proposal;
            }
        });

        final Long rawCommandKey = 111L;
        final Source source = Source.twitter; // To be able to simulate the failure
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

        final Long consumerKey = 12590L;
        final Demand demand = new Demand();
        demand.setKey(demandKey);
        demand.setOwnerKey(consumerKey);
        demand.addCriterion("test");
        demand.setExpirationDate(demandExpirationDate);
        demand.setState(State.published);
        demand.setSource(source);
        demand.setRawCommandId(rawCommandKey);
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(demandKey, demand.getKey());
                return demand;
            }
        });

        final Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                return consumer;
            }
        });

        final String name = "sgrognegneu";
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

        final Twitter mockTwitterAccount = (new Twitter() {
            @Override
            public DirectMessage sendDirectMessage(String id, String text) throws TwitterException {
                throw new TwitterException("done in purpose");
            }
        });
        MockTwitterConnector.injectMockTwitterAccount(mockTwitterAccount);

        ProposalProcessor.process(proposalKey);

        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());

        MockTwitterConnector.restoreTwitterConnector(mockTwitterAccount, null);
    }

    @Test
    public void testProcessOneValidProposalForAnInvalidDemandButWithCommunicationFailure() throws DataSourceException, InvalidIdentifierException {
        final Long rawCommandKey = 111L;
        final Source source = Source.mail; // To be able to simulate the failure
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

        final Long proposalKey = 67890L;
        final Long demandKey = 12345L;
        final Double price = 25.75D;
        final Long quantity = 32L;
        final Long storeKey = 65758L;
        final Double total = 29.99D;
        final Proposal proposal = new Proposal();
        proposal.addCriterion("test");
        proposal.setKey(proposalKey);
        proposal.setDemandKey(demandKey);
        proposal.setPrice(price);
        proposal.setQuantity(quantity);
        proposal.setState(State.published);
        proposal.setStoreKey(storeKey);
        proposal.setTotal(total);
        proposal.setRawCommandId(rawCommandKey);

        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws InvalidIdentifierException {
                assertEquals(proposalKey, key);
                assertNull(cKey);
                assertNull(sKey);
                return proposal;
            }
        });

        final Long consumerKey = 12590L;
        final Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setEmail("@@@@");
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                return consumer;
            }
        });

        final Demand demand = new Demand();
        demand.setKey(demandKey);
        demand.setOwnerKey(consumerKey);
        demand.setState(State.invalid);
        demand.setSource(source);
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                return demand;
            }
        });

        final Long saleAssociateKey = 444L;
        final SaleAssociate saleAssociate = new SaleAssociate();
        saleAssociate.setKey(saleAssociateKey);
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                return saleAssociate;
            }
        });

        final String name = "the store!";
        final Store store = new Store();
        store.setKey(storeKey);
        store.setName(name);
        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) {
                return store;
            }
        });

        ProposalProcessor.process(proposalKey);
    }
}
