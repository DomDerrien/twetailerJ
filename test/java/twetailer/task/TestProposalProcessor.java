package twetailer.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

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
import twetailer.dao.MockPersistenceManager;
import twetailer.dao.ProposalOperations;
import twetailer.dao.RetailerOperations;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Proposal;
import twetailer.dto.Retailer;
import twetailer.validator.CommandSettings.State;
import twitter4j.DirectMessage;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import domderrien.i18n.LabelExtractor;

public class TestProposalProcessor {

    private class MockBaseOperations extends BaseOperations {
        private PersistenceManager pm = new MockPersistenceManager();
        @Override
        public PersistenceManager getPersistenceManager() {
            return pm;
        }
    };

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
        ProposalProcessor.retailerOperations = ProposalProcessor._baseOperations.getRetailerOperations();
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
    public void testProcessOneValidProposal() throws DataSourceException {
        final Long proposalKey = 67890L;
        final Long demandKey = 12345L;
        final Double price = 25.75D;
        final Long quantity = 32L;
        final Long storeKey = 5555L;
        final Double total = 29.99D;
        final String currency = "$";
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

        final Long ownerKey = 6666L;
        final Demand demand = new Demand();
        demand.setKey(demandKey);
        demand.setOwnerKey(ownerKey);
        demand.setState(State.published);
        demand.setSource(Source.simulated);
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

        ProposalProcessor.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertEquals(
                LabelExtractor.get("pp_inform_consumer_about_proposal_with_total_cost", new Object[] { proposalKey, demandKey, "test", storeKey, total, currency }, Locale.ENGLISH),
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

        final Long consumerKey = 12590L;
        final Demand demand = new Demand();
        demand.setKey(demandKey);
        demand.setOwnerKey(consumerKey);
        demand.setState(State.published);
        demand.setSource(Source.twitter); // To be able to simulate the failure
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
    public void testProcessOneValidProposalForAnInvalidDemand() throws DataSourceException {
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

        final Long consumerKey = 12590L;
        final Demand demand = new Demand();
        demand.setKey(demandKey);
        demand.setOwnerKey(consumerKey);
        demand.setState(State.invalid);
        demand.setSource(Source.twitter); // To be able to simulate the failure
        ProposalProcessor.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws DataSourceException {
                assertEquals(demandKey, key);
                assertNull(cKey);
                return demand;
            }
        };

        final Long retailerKey = 444L;
        final Retailer retailer = new Retailer();
        retailer.setKey(retailerKey);
        retailer.setPreferredConnection(Source.simulated);
        ProposalProcessor.retailerOperations = new RetailerOperations() {
            @Override
            public Retailer getRetailer(PersistenceManager pm, Long key) {
                return retailer;
            }
        };

        ProposalProcessor.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertEquals(
                LabelExtractor.get("pp_inform_retailer_demand_not_published_state", new Object[] { proposalKey, demandKey, State.invalid.toString() }, Locale.ENGLISH),
                BaseConnector.getLastCommunicationInSimulatedMode()
        );
        assertTrue(ProposalProcessor._baseOperations.getPersistenceManager().isClosed());
    }
}
