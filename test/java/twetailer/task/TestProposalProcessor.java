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
import twetailer.connector.BaseConnector;
import twetailer.connector.MockTwitterConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.BaseOperations;
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
        ProposalProcessor._baseOperations = new MockBaseOperations();
        BaseConnector.resetLastCommunicationInSimulatedMode();
    }

    @After
    public void tearDown() {
        ProposalProcessor._baseOperations = new BaseOperations();
        ProposalProcessor.proposalOperations = ProposalProcessor._baseOperations.getProposalOperations();
        ProposalProcessor.locationOperations = ProposalProcessor._baseOperations.getLocationOperations();
        ProposalProcessor.proposalOperations = ProposalProcessor._baseOperations.getProposalOperations();
        ProposalProcessor.rawCommandOperations = ProposalProcessor._baseOperations.getRawCommandOperations();
        ProposalProcessor.saleAssociateOperations = ProposalProcessor._baseOperations.getSaleAssociateOperations();
        ProposalProcessor.storeOperations = ProposalProcessor._baseOperations.getStoreOperations();
    }

    @Test
    public void testConstructor() {
        new ProposalProcessor();
    }

    @Test(expected=DataSourceException.class)
    public void testProcessNoProposal() throws DataSourceException {
        final Long proposalKey = 12345L;

        // ProposalOperations mock
        ProposalProcessor.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                assertNull(cKey);
                assertNull(sKey);
                throw new DataSourceException("Done in purpose");
            }
        };

        ProposalProcessor.process(proposalKey);

        assertTrue(ProposalProcessor._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessOneValidProposalIa() throws DataSourceException {
        final Long proposalKey = 67890L;
        final Long demandKey = 12345L;
        final Double price = null;
        final Long quantity = 32L;
        final Long storeKey = 5555L;
        final Double total = 29.99D;
        final String currency = "\\$";
        final Proposal proposal = new Proposal();
        proposal.addCriterion("test");
        proposal.setKey(proposalKey);
        proposal.setDemandKey(demandKey);
        proposal.setPrice(price);
        proposal.setQuantity(quantity);
        proposal.setState(State.published);
        proposal.setStoreKey(storeKey);
        proposal.setTotal(total);

        ProposalProcessor.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                assertNull(cKey);
                assertNull(sKey);
                assertNotNull(proposal.getCriteria());
                assertNotSame(0, proposal.getCriteria().size());
                return proposal;
            }
        };

        final Long rawCommandKey = 111L;
        final Source source = Source.simulated;
        ProposalProcessor.rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        };

        final Long ownerKey = 6666L;
        final Demand demand = new Demand();
        demand.setKey(demandKey);
        demand.setOwnerKey(ownerKey);
        demand.setState(State.published);
        demand.setSource(source);
        demand.setRawCommandId(rawCommandKey);
        ProposalProcessor.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertTrue(demand.getProposalKeys().contains(proposalKey));
                return demand;
            }
        };

        final Consumer consumer = new Consumer();
        consumer.setKey(ownerKey);
        ProposalProcessor.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                return consumer;
            }
        };

        final String name = "sgrognegneu";
        ProposalProcessor.storeOperations = new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) {
                assertEquals(storeKey, key);
                Store store = new Store();
                store.setKey(storeKey);
                store.setName(name);
                return store;
            }
        };

        ProposalProcessor.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        String expectedMessage = LabelExtractor.get(
                "pp_inform_consumer_about_proposal_with_total_cost_only",
                new Object[] {
                        proposal.getKey(),
                        proposal.getSerializedCriteria(),
                        demand.getKey(),
                        demand.getSerializedCriteria(),
                        demand.getExpirationDate(),
                        proposal.getStoreKey(),
                        name,
                        currency,
                        total
                },
                Locale.ENGLISH);
        assertEquals(
                expectedMessage,
                BaseConnector.getLastCommunicationInSimulatedMode()
        );
        assertTrue(ProposalProcessor._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessOneValidProposalIb() throws DataSourceException {
        final Long proposalKey = 67890L;
        final Long demandKey = 12345L;
        final Double price = 0.0D;
        final Long quantity = 32L;
        final Long storeKey = 5555L;
        final Double total = 29.99D;
        final String currency = "\\$";
        final Proposal proposal = new Proposal();
        proposal.addCriterion("test");
        proposal.setKey(proposalKey);
        proposal.setDemandKey(demandKey);
        proposal.setPrice(price);
        proposal.setQuantity(quantity);
        proposal.setState(State.published);
        proposal.setStoreKey(storeKey);
        proposal.setTotal(total);

        ProposalProcessor.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                assertNull(cKey);
                assertNull(sKey);
                assertNotNull(proposal.getCriteria());
                assertNotSame(0, proposal.getCriteria().size());
                return proposal;
            }
        };

        final Long rawCommandKey = 111L;
        final Source source = Source.simulated;
        ProposalProcessor.rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        };

        final Long ownerKey = 6666L;
        final Demand demand = new Demand();
        demand.setKey(demandKey);
        demand.setOwnerKey(ownerKey);
        demand.setState(State.published);
        demand.setSource(source);
        demand.setRawCommandId(rawCommandKey);
        ProposalProcessor.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertTrue(demand.getProposalKeys().contains(proposalKey));
                return demand;
            }
        };

        final Consumer consumer = new Consumer();
        consumer.setKey(ownerKey);
        ProposalProcessor.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                return consumer;
            }
        };

        final String name = "sgrognegneu";
        ProposalProcessor.storeOperations = new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) {
                assertEquals(storeKey, key);
                Store store = new Store();
                store.setKey(storeKey);
                store.setName(name);
                return store;
            }
        };

        ProposalProcessor.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        String expectedMessage = LabelExtractor.get(
                "pp_inform_consumer_about_proposal_with_total_cost_only",
                new Object[] {
                        proposal.getKey(),
                        proposal.getSerializedCriteria(),
                        demand.getKey(),
                        demand.getSerializedCriteria(),
                        demand.getExpirationDate(),
                        proposal.getStoreKey(),
                        name,
                        currency,
                        total
                },
                Locale.ENGLISH);
        assertEquals(
                expectedMessage,
                BaseConnector.getLastCommunicationInSimulatedMode()
        );
        assertTrue(ProposalProcessor._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessOneValidProposalIIa() throws DataSourceException {
        final Long proposalKey = 67890L;
        final Long demandKey = 12345L;
        final Double price = 25.75D;
        final Long quantity = 32L;
        final Long storeKey = 5555L;
        final Double total = null;
        final String currency = "\\$";
        final Proposal proposal = new Proposal();
        proposal.addCriterion("test");
        proposal.setKey(proposalKey);
        proposal.setDemandKey(demandKey);
        proposal.setPrice(price);
        proposal.setQuantity(quantity);
        proposal.setState(State.published);
        proposal.setStoreKey(storeKey);
        proposal.setTotal(total);

        ProposalProcessor.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                assertNull(cKey);
                assertNull(sKey);
                assertNotNull(proposal.getCriteria());
                assertNotSame(0, proposal.getCriteria().size());
                return proposal;
            }
        };

        final Long rawCommandKey = 111L;
        final Source source = Source.simulated;
        ProposalProcessor.rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        };

        final Long ownerKey = 6666L;
        final Demand demand = new Demand();
        demand.setKey(demandKey);
        demand.setOwnerKey(ownerKey);
        demand.setState(State.published);
        demand.setSource(source);
        demand.setRawCommandId(rawCommandKey);
        ProposalProcessor.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertTrue(demand.getProposalKeys().contains(proposalKey));
                return demand;
            }
        };

        final Consumer consumer = new Consumer();
        consumer.setKey(ownerKey);
        ProposalProcessor.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                return consumer;
            }
        };

        final String name = "sgrognegneu";
        ProposalProcessor.storeOperations = new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) {
                assertEquals(storeKey, key);
                Store store = new Store();
                store.setKey(storeKey);
                store.setName(name);
                return store;
            }
        };

        ProposalProcessor.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        String expectedMessage = LabelExtractor.get(
                "pp_inform_consumer_about_proposal_with_price_only",
                new Object[] {
                        proposal.getKey(),
                        proposal.getSerializedCriteria(),
                        demand.getKey(),
                        demand.getSerializedCriteria(),
                        demand.getExpirationDate(),
                        proposal.getStoreKey(),
                        name,
                        currency,
                        price
                },
                Locale.ENGLISH);
        assertEquals(
                expectedMessage,
                BaseConnector.getLastCommunicationInSimulatedMode()
        );
        assertTrue(ProposalProcessor._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessOneValidProposalIIb() throws DataSourceException {
        final Long proposalKey = 67890L;
        final Long demandKey = 12345L;
        final Double price = 25.75D;
        final Long quantity = 32L;
        final Long storeKey = 5555L;
        final Double total = 0.0D;
        final String currency = "\\$";
        final Proposal proposal = new Proposal();
        proposal.addCriterion("test");
        proposal.setKey(proposalKey);
        proposal.setDemandKey(demandKey);
        proposal.setPrice(price);
        proposal.setQuantity(quantity);
        proposal.setState(State.published);
        proposal.setStoreKey(storeKey);
        proposal.setTotal(total);

        ProposalProcessor.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                assertNull(cKey);
                assertNull(sKey);
                assertNotNull(proposal.getCriteria());
                assertNotSame(0, proposal.getCriteria().size());
                return proposal;
            }
        };

        final Long rawCommandKey = 111L;
        final Source source = Source.simulated;
        ProposalProcessor.rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        };

        final Long ownerKey = 6666L;
        final Demand demand = new Demand();
        demand.setKey(demandKey);
        demand.setOwnerKey(ownerKey);
        demand.setState(State.published);
        demand.setSource(source);
        demand.setRawCommandId(rawCommandKey);
        ProposalProcessor.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertTrue(demand.getProposalKeys().contains(proposalKey));
                return demand;
            }
        };

        final Consumer consumer = new Consumer();
        consumer.setKey(ownerKey);
        ProposalProcessor.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                return consumer;
            }
        };

        final String name = "sgrognegneu";
        ProposalProcessor.storeOperations = new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) {
                assertEquals(storeKey, key);
                Store store = new Store();
                store.setKey(storeKey);
                store.setName(name);
                return store;
            }
        };

        ProposalProcessor.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        String expectedMessage = LabelExtractor.get(
                "pp_inform_consumer_about_proposal_with_price_only",
                new Object[] {
                        proposal.getKey(),
                        proposal.getSerializedCriteria(),
                        demand.getKey(),
                        demand.getSerializedCriteria(),
                        demand.getExpirationDate(),
                        proposal.getStoreKey(),
                        name,
                        currency,
                        price
                },
                Locale.ENGLISH);
        assertEquals(
                expectedMessage,
                BaseConnector.getLastCommunicationInSimulatedMode()
        );
        assertTrue(ProposalProcessor._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessOneValidProposalIII() throws DataSourceException {
        final Long proposalKey = 67890L;
        final Long demandKey = 12345L;
        final Double price = 25.75D;
        final Long quantity = 32L;
        final Long storeKey = 5555L;
        final Double total = 37.95D;
        final String currency = "\\$";
        final Proposal proposal = new Proposal();
        proposal.addCriterion("test");
        proposal.setKey(proposalKey);
        proposal.setDemandKey(demandKey);
        proposal.setPrice(price);
        proposal.setQuantity(quantity);
        proposal.setState(State.published);
        proposal.setStoreKey(storeKey);
        proposal.setTotal(total);

        ProposalProcessor.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                assertNull(cKey);
                assertNull(sKey);
                assertNotNull(proposal.getCriteria());
                assertNotSame(0, proposal.getCriteria().size());
                return proposal;
            }
        };

        final Long rawCommandKey = 111L;
        final Source source = Source.simulated;
        ProposalProcessor.rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        };

        final Long ownerKey = 6666L;
        final Demand demand = new Demand();
        demand.setKey(demandKey);
        demand.setOwnerKey(ownerKey);
        demand.setState(State.published);
        demand.setSource(source);
        demand.setRawCommandId(rawCommandKey);
        ProposalProcessor.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                return demand;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertTrue(demand.getProposalKeys().contains(proposalKey));
                return demand;
            }
        };

        final Consumer consumer = new Consumer();
        consumer.setKey(ownerKey);
        ProposalProcessor.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                return consumer;
            }
        };

        final String name = "sgrognegneu";
        ProposalProcessor.storeOperations = new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) {
                assertEquals(storeKey, key);
                Store store = new Store();
                store.setKey(storeKey);
                store.setName(name);
                return store;
            }
        };

        ProposalProcessor.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        String expectedMessage = LabelExtractor.get(
                "pp_inform_consumer_about_proposal_with_price_and_total_cost",
                new Object[] {
                        proposal.getKey(),
                        proposal.getSerializedCriteria(),
                        demand.getKey(),
                        demand.getSerializedCriteria(),
                        demand.getExpirationDate(),
                        proposal.getStoreKey(),
                        name,
                        currency,
                        price,
                        total
                },
                Locale.ENGLISH);
        assertEquals(
                expectedMessage,
                BaseConnector.getLastCommunicationInSimulatedMode()
        );
        assertTrue(ProposalProcessor._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessOneInvalidProposal() throws DataSourceException {
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

        ProposalProcessor.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                assertNull(cKey);
                assertNull(sKey);
                return proposal;
            }
        };

        ProposalProcessor.process(proposalKey);

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(ProposalProcessor._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessOneValidProposalWithFailureGettingDemand() throws DataSourceException {
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

        ProposalProcessor.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                assertNull(cKey);
                assertNull(sKey);
                return proposal;
            }
        };

        ProposalProcessor.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws DataSourceException {
                throw new DataSourceException("Done in purpose");
            }
        };

        ProposalProcessor.process(proposalKey);

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(ProposalProcessor._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    @SuppressWarnings("serial")
    public void testProcessOneValidProposalWithFailureToSendMessageToConsumer() throws DataSourceException {
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

        ProposalProcessor.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                assertNull(cKey);
                assertNull(sKey);
                return proposal;
            }
        };

        final Long rawCommandKey = 111L;
        final Source source = Source.twitter; // To be able to simulate the failure
        ProposalProcessor.rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        };

        final Long consumerKey = 12590L;
        final Demand demand = new Demand();
        demand.setKey(demandKey);
        demand.setOwnerKey(consumerKey);
        demand.addCriterion("test");
        demand.setExpirationDate(demandExpirationDate);
        demand.setState(State.published);
        demand.setSource(source);
        demand.setRawCommandId(rawCommandKey);
        ProposalProcessor.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                return demand;
            }
        };

        final Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        ProposalProcessor.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                return consumer;
            }
        };

        final String name = "sgrognegneu";
        ProposalProcessor.storeOperations = new StoreOperations() {
            @Override
            public Store getStore(PersistenceManager pm, Long key) {
                assertEquals(storeKey, key);
                Store store = new Store();
                store.setKey(storeKey);
                store.setName(name);
                return store;
            }
        };

        final Twitter mockTwitterAccount = (new Twitter() {
            @Override
            public DirectMessage sendDirectMessage(String id, String text) throws TwitterException {
                throw new TwitterException("done in purpose");
            }
        });
        MockTwitterConnector.injectMockTwitterAccount(mockTwitterAccount);

        ProposalProcessor.process(proposalKey);

        assertTrue(ProposalProcessor._baseOperations.getPersistenceManager().isClosed());

        MockTwitterConnector.restoreTwitterConnector(mockTwitterAccount, null);
    }

    @Test
    public void testProcessOneValidProposalForAnInvalidDemandButWithCommunicationFailure() throws DataSourceException {
        final Long rawCommandKey = 111L;
        final Source source = Source.mail; // To be able to simulate the failure
        ProposalProcessor.rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                assertEquals(rawCommandKey, key);
                RawCommand rawCommand = new RawCommand();
                rawCommand.setKey(rawCommandKey);
                rawCommand.setSource(source);
                return rawCommand;
            }
        };

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

        ProposalProcessor.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                assertNull(cKey);
                assertNull(sKey);
                return proposal;
            }
        };

        final Long consumerKey = 12590L;
        final Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setEmail("@@@@");
        ProposalProcessor.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                return consumer;
            }
        };

        final Demand demand = new Demand();
        demand.setKey(demandKey);
        demand.setOwnerKey(consumerKey);
        demand.setState(State.invalid);
        demand.setSource(source);
        ProposalProcessor.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                return demand;
            }
        };

        final Long saleAssociateKey = 444L;
        final SaleAssociate saleAssociate = new SaleAssociate();
        saleAssociate.setKey(saleAssociateKey);
        saleAssociate.setEmail("@@@@");
        saleAssociate.setPreferredConnection(Source.mail);
        ProposalProcessor.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                return saleAssociate;
            }
        };

        ProposalProcessor.process(proposalKey);
    }
}
