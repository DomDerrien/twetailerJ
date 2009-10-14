package twetailer.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
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
import twetailer.dao.DemandOperations;
import twetailer.dao.MockAppEngineEnvironment;
import twetailer.dao.MockPersistenceManager;
import twetailer.dao.ProposalOperations;
import twetailer.dao.RetailerOperations;
import twetailer.dto.Demand;
import twetailer.dto.Proposal;
import twetailer.dto.Retailer;
import twetailer.validator.CommandSettings;
import twetailer.validator.CommandSettings.State;
import twitter4j.DirectMessage;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import domderrien.i18n.LabelExtractor;

public class TestProposalValidator {

    private class MockBaseOperations extends BaseOperations {
        private PersistenceManager pm = new MockPersistenceManager();
        @Override
        public PersistenceManager getPersistenceManager() {
            return pm;
        }
    };

    final Long retailerKey = 54321L;
    final Source source = Source.simulated;
    final State state = State.open;
    MockAppEngineEnvironment appEnv;

    @Before
    public void setUp() throws Exception {
        // RetailerOperations mock
        RetailerOperations retailerOperations = new RetailerOperations() {
            @Override
            public Retailer getRetailer(PersistenceManager pm, Long key) {
                assertEquals(retailerKey, key);
                Retailer retailer = new Retailer();
                retailer.setKey(retailerKey);
                retailer.setPreferredConnection(source);
                return retailer;
            }
        };

        // Install the mocks
        ProposalValidator._baseOperations = new MockBaseOperations();
        ProposalValidator.retailerOperations = retailerOperations;

        // Be sure to start with a clean message stack
        BaseConnector.resetLastCommunicationInSimulatedMode();

        // App Engine Environment mock
        appEnv = new MockAppEngineEnvironment();
        appEnv.setUp();
    }

    @After
    public void tearDown() throws Exception {
        ProposalValidator._baseOperations = new BaseOperations();
        ProposalValidator.retailerOperations = ProposalValidator._baseOperations.getRetailerOperations();
        ProposalValidator.proposalOperations = ProposalValidator._baseOperations.getProposalOperations();

        appEnv.tearDown();
    }

    @Test
    public void testConstructor() {
        new ProposalValidator();
    }

    @Test(expected=DataSourceException.class)
    public void testProcessNoProposal() throws DataSourceException {
        final Long proposalKey = 12345L;

        // ProposalOperations mock
        ProposalValidator.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                assertNull(cKey);
                assertNull(sKey);
                throw new DataSourceException("Done in purpose");
            }
        };

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertTrue(ProposalValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessOneProposalInIncorrectState() throws DataSourceException {
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

        ProposalValidator.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                assertNull(cKey);
                assertNull(sKey);
                return proposal;
            }
        };

        ProposalValidator.process(proposalKey);

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(ProposalValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessI() throws DataSourceException {
        //
        // Invalid criteria
        //

        // ProposalOperations mock
        final Long proposalKey = 67890L;
        ProposalValidator.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                assertNull(cKey);
                assertNull(sKey);
                Proposal proposal = new Proposal() {
                    @Override
                    public List<String> getCriteria() {
                        return null;
                    }
                };
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(retailerKey);
                proposal.setSource(source);
                proposal.setState(state);
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertNotNull(proposal);
                assertEquals(CommandSettings.State.invalid, proposal.getState());
                return proposal;
            }
        };

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertEquals(
                LabelExtractor.get("dv_proposalShouldHaveAtLeastOneTag", new Object[] { proposalKey }, Locale.ENGLISH),
                BaseConnector.getLastCommunicationInSimulatedMode()
        );
        assertTrue(ProposalValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessII() throws DataSourceException {
        //
        // Invalid criteria
        //

        // ProposalOperations mock
        final Long proposalKey = 67890L;
        ProposalValidator.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                assertNull(cKey);
                assertNull(sKey);
                Proposal proposal = new Proposal() {
                    @Override
                    public List<String> getCriteria() {
                        return new ArrayList<String>();
                    }
                };
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(retailerKey);
                proposal.setSource(source);
                proposal.setState(state);
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertNotNull(proposal);
                assertEquals(CommandSettings.State.invalid, proposal.getState());
                return proposal;
            }
        };

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertEquals(
                LabelExtractor.get("dv_proposalShouldHaveAtLeastOneTag", new Object[] { proposalKey }, Locale.ENGLISH),
                BaseConnector.getLastCommunicationInSimulatedMode()
        );
        assertTrue(ProposalValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessIII() throws DataSourceException {
        //
        // Valid criteria
        // Invalid quantity
        //

        // ProposalOperations mock
        final Long proposalKey = 67890L;
        ProposalValidator.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                assertNull(cKey);
                assertNull(sKey);
                Proposal proposal = new Proposal() {
                    @Override
                    public Long getQuantity() {
                        return null;
                    }
                };
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(retailerKey);
                proposal.setSource(source);
                proposal.addCriterion("test");
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertNotNull(proposal);
                assertEquals(CommandSettings.State.invalid, proposal.getState());
                return proposal;
            }
        };

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertEquals(
                LabelExtractor.get("dv_proposalShouldConcernAtLeastOneItem", new Object[] { proposalKey }, Locale.ENGLISH),
                BaseConnector.getLastCommunicationInSimulatedMode()
        );
        assertTrue(ProposalValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessIV() throws DataSourceException {
        //
        // Valid criteria
        // Invalid quantity
        //

        // ProposalOperations mock
        final Long proposalKey = 67890L;
        ProposalValidator.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                assertNull(cKey);
                assertNull(sKey);
                Proposal proposal = new Proposal() {
                    @Override
                    public Long getQuantity() {
                        return 0L;
                    }
                };
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(retailerKey);
                proposal.setSource(source);
                proposal.addCriterion("test");
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertNotNull(proposal);
                assertEquals(CommandSettings.State.invalid, proposal.getState());
                return proposal;
            }
        };

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertEquals(
                LabelExtractor.get("dv_proposalShouldConcernAtLeastOneItem", new Object[] { proposalKey }, Locale.ENGLISH),
                BaseConnector.getLastCommunicationInSimulatedMode()
        );
        assertTrue(ProposalValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessV() throws DataSourceException {
        //
        // Valid criteria
        // Valid expiration date
        // Invalid price or total
        //

        // ProposalOperations mock
        final Long proposalKey = 67890L;
        ProposalValidator.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal() {
                    @Override
                    public Double getPrice() {
                        return Double.valueOf(0.0D);
                    }
                    @Override
                    public Double getTotal() {
                        return Double.valueOf(0.0D);
                    }
                };
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(retailerKey);
                proposal.setSource(source);
                proposal.addCriterion("test");
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertNotNull(proposal);
                assertEquals(CommandSettings.State.invalid, proposal.getState());
                return proposal;
            }
        };

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertEquals(
                LabelExtractor.get("dv_proposalShouldHaveAUnitPriceOrTotalPrice", new Object[] { proposalKey }, Locale.ENGLISH),
                BaseConnector.getLastCommunicationInSimulatedMode()
        );
        assertTrue(ProposalValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessVI() throws DataSourceException {
        //
        // Valid criteria
        // Valid quantity
        // Invalid price or total
        //

        // ProposalOperations mock
        final Long proposalKey = 67890L;
        ProposalValidator.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal() {
                    @Override
                    public Double getPrice() {
                        return null;
                    }
                    @Override
                    public Double getTotal() {
                        return null;
                    }
                };
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(retailerKey);
                proposal.setSource(source);
                proposal.addCriterion("test");
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertNotNull(proposal);
                assertEquals(CommandSettings.State.invalid, proposal.getState());
                return proposal;
            }
        };

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertEquals(
                LabelExtractor.get("dv_proposalShouldHaveAUnitPriceOrTotalPrice", new Object[] { proposalKey }, Locale.ENGLISH),
                BaseConnector.getLastCommunicationInSimulatedMode()
        );
        assertTrue(ProposalValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessVII() throws DataSourceException {
        //
        // Valid criteria
        // Valid quantity
        // Invalid price or total
        //

        // ProposalOperations mock
        final Long proposalKey = 67890L;
        ProposalValidator.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal() {
                    @Override
                    public Double getPrice() {
                        return null;
                    }
                    @Override
                    public Double getTotal() {
                        return Double.valueOf(0.0D);
                    }
                };
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(retailerKey);
                proposal.setSource(source);
                proposal.addCriterion("test");
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertNotNull(proposal);
                assertEquals(CommandSettings.State.invalid, proposal.getState());
                return proposal;
            }
        };

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertEquals(
                LabelExtractor.get("dv_proposalShouldHaveAUnitPriceOrTotalPrice", new Object[] { proposalKey }, Locale.ENGLISH),
                BaseConnector.getLastCommunicationInSimulatedMode()
        );
        assertTrue(ProposalValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessVIII() throws DataSourceException {
        //
        // Valid criteria
        // Valid quantity
        // Invalid price or total
        //

        // ProposalOperations mock
        final Long proposalKey = 67890L;
        ProposalValidator.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal() {
                    @Override
                    public Double getPrice() {
                        return Double.valueOf(0.0D);
                    }
                    @Override
                    public Double getTotal() {
                        return null;
                    }
                };
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(retailerKey);
                proposal.setSource(source);
                proposal.addCriterion("test");
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertNotNull(proposal);
                assertEquals(CommandSettings.State.invalid, proposal.getState());
                return proposal;
            }
        };

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertEquals(
                LabelExtractor.get("dv_proposalShouldHaveAUnitPriceOrTotalPrice", new Object[] { proposalKey }, Locale.ENGLISH),
                BaseConnector.getLastCommunicationInSimulatedMode()
        );
        assertTrue(ProposalValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessIXa() throws DataSourceException {
        //
        // Valid criteria
        // Valid quantity
        // Valid price (total stays null because just one required)
        // Invalid demand key
        //

        // ProposalOperations mock
        final Long proposalKey = 67890L;
        ProposalValidator.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal() {
                    @Override
                    public Double getPrice() {
                        return 25.99;
                    }
                };
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(retailerKey);
                proposal.setSource(source);
                proposal.addCriterion("test");
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertNotNull(proposal);
                assertEquals(CommandSettings.State.invalid, proposal.getState());
                return proposal;
            }
        };

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertEquals(
                LabelExtractor.get("dv_proposalShouldHaveADemandReference", new Object[] { proposalKey }, Locale.ENGLISH),
                BaseConnector.getLastCommunicationInSimulatedMode()
        );
        assertTrue(ProposalValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessIXb() throws DataSourceException {
        //
        // Valid criteria
        // Valid quantity
        // Valid price (total stays null because just one required)
        // Invalid demand key
        //

        // ProposalOperations mock
        final Long proposalKey = 67890L;
        ProposalValidator.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal() {
                    @Override
                    public Double getTotal() {
                        return 25.99;
                    }
                };
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(retailerKey);
                proposal.setSource(source);
                proposal.addCriterion("test");
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertNotNull(proposal);
                assertEquals(CommandSettings.State.invalid, proposal.getState());
                return proposal;
            }
        };

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertEquals(
                LabelExtractor.get("dv_proposalShouldHaveADemandReference", new Object[] { proposalKey }, Locale.ENGLISH),
                BaseConnector.getLastCommunicationInSimulatedMode()
        );
        assertTrue(ProposalValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessX() throws DataSourceException {
        //
        // Valid criteria
        // Valid quantity
        // Valid price (total stays null because just one required)
        // Invalid demand key
        //

        // ProposalOperations mock
        final Long proposalKey = 67890L;
        ProposalValidator.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal() {
                    @Override
                    public Double getPrice() {
                        return 25.99;
                    }
                    @Override
                    public Long getDemandKey() {
                        return 0L;
                    }
                };
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(retailerKey);
                proposal.setSource(source);
                proposal.addCriterion("test");
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertNotNull(proposal);
                assertEquals(CommandSettings.State.invalid, proposal.getState());
                return proposal;
            }
        };

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertEquals(
                LabelExtractor.get("dv_proposalShouldHaveADemandReference", new Object[] { proposalKey }, Locale.ENGLISH),
                BaseConnector.getLastCommunicationInSimulatedMode()
        );
        assertTrue(ProposalValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessXI() throws DataSourceException {
        //
        // Valid criteria
        // Valid quantity
        // Valid price (total stays null because just one required)
        // Invalid demand key
        //

        // ProposalOperations mock
        final Long proposalKey = 67890L;
        final Long demandKey = 54321L;
        ProposalValidator.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal() {
                    @Override
                    public Double getPrice() {
                        return 25.99;
                    }
                    @Override
                    public Long getDemandKey() {
                        return demandKey;
                    }
                };
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(retailerKey);
                proposal.setSource(source);
                proposal.addCriterion("test");
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertNotNull(proposal);
                assertEquals(CommandSettings.State.invalid, proposal.getState());
                return proposal;
            }
        };
        ProposalValidator.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws DataSourceException {
                throw new DataSourceException("Done in purpose");
            }
        };

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertEquals(
                LabelExtractor.get("dv_proposalHaveInvalidDemandReference", new Object[] { proposalKey, demandKey }, Locale.ENGLISH),
                BaseConnector.getLastCommunicationInSimulatedMode()
        );
        assertTrue(ProposalValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessXII() throws DataSourceException {
        //
        // Valid criteria
        // Valid quantity
        // Valid price (total stays null because just one required)
        // Valid demand key
        //

        // ProposalOperations mock
        final Long proposalKey = 67890L;
        final Long demandKey = 54321L;
        ProposalValidator.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws DataSourceException {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal() {
                    @Override
                    public Double getPrice() {
                        return 25.99;
                    }
                    @Override
                    public Long getDemandKey() {
                        return demandKey;
                    }
                };
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(retailerKey);
                proposal.setSource(source);
                proposal.addCriterion("test");
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertNotNull(proposal);
                assertEquals(CommandSettings.State.published, proposal.getState());
                return proposal;
            }
        };
        ProposalValidator.demandOperations = new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws DataSourceException {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                return demand;
            }
        };

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(ProposalValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessXIII() throws DataSourceException {
        //
        // Error while getting retailer information
        //

        // ProposalOperations mock
        final Long proposalKey = 67890L;
        ProposalValidator.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws DataSourceException {
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(retailerKey);
                return proposal;
            }
        };
        ProposalValidator.retailerOperations = new RetailerOperations() {
            @Override
            public Retailer getRetailer(PersistenceManager pm, Long key) throws DataSourceException {
                assertEquals(retailerKey, key);
                throw new DataSourceException("Done in purpose");
            }
        };

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(ProposalValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    @SuppressWarnings("serial")
    public void testProcessXIV() throws DataSourceException {
        //
        // Error while informing about the error
        //

        // ProposalOperations mock
        final Long proposalKey = 67890L;
        ProposalValidator.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws DataSourceException {
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(retailerKey);
                proposal.setSource(Source.twitter);
                return proposal;
            }
        };

        final Twitter mockTwitterAccount = (new Twitter() {
            @Override
            public DirectMessage sendDirectMessage(String id, String text) throws TwitterException {
                throw new TwitterException("done in purpose");
            }
        });
        MockTwitterConnector.injectMockTwitterAccount(mockTwitterAccount);

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(ProposalValidator._baseOperations.getPersistenceManager().isClosed());

        MockTwitterConnector.restoreTwitterConnector(mockTwitterAccount, null);
    }
}
