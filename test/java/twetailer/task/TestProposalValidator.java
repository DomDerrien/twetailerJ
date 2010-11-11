package twetailer.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
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
import org.junit.Ignore;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.connector.BaseConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.MockBaseOperations;
import twetailer.dao.ProposalOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Proposal;
import twetailer.dto.SaleAssociate;
import twetailer.task.step.BaseSteps;
import twetailer.validator.CommandSettings;
import twetailer.validator.CommandSettings.State;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import domderrien.i18n.DateUtils;
import domderrien.i18n.LabelExtractor;

public class TestProposalValidator {

    final Long saleAssociateKey = 54321L;
    final Long consumerKey = 33799L;
    final Source source = Source.simulated;
    final State state = State.opened;

    private static LocalServiceTestHelper  helper;

    @BeforeClass
    public static void setUpBeforeClass() {
        ProposalValidator.setLogger(new MockLogger("test", null));
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    }


    @Before
    public void setUp() throws Exception {
        helper.setUp();

        // SaleAssociateOperations mock
        SaleAssociateOperations saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                assertEquals(saleAssociateKey, key);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setConsumerKey(consumerKey);
                return saleAssociate;
            }
        };
        ConsumerOperations consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(saleAssociateKey);
                consumer.setPreferredConnection(source);
                return consumer;
            }
        };

        // Install the mocks
        BaseSteps.resetOperationControllers(true);
        BaseSteps.setMockBaseOperations(new MockBaseOperations());
        BaseSteps.setMockConsumerOperations(consumerOperations);
        BaseSteps.setMockSaleAssociateOperations(saleAssociateOperations);

        // Be sure to start with a clean message stack
        BaseConnector.resetLastCommunicationInSimulatedMode();
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();
    }

    @Test
    public void testConstructor() {
        new ProposalValidator();
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

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessOneProposalInIncorrectState() throws DataSourceException, InvalidIdentifierException {
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

        ProposalValidator.process(proposalKey);

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessI() throws DataSourceException, InvalidIdentifierException {
        //
        // Invalid criteria
        //

        // ProposalOperations mock
        final Long proposalKey = 67890L;
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws InvalidIdentifierException {
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
                proposal.setOwnerKey(saleAssociateKey);
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
        });

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposalKey }, Locale.ENGLISH);
        assertEquals(
                LabelExtractor.get("pv_report_proposal_without_tag", new Object[] { proposalRef }, Locale.ENGLISH),
                BaseConnector.getLastCommunicationInSimulatedMode()
        );
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessII() throws DataSourceException, InvalidIdentifierException {
        //
        // Invalid criteria
        //

        // ProposalOperations mock
        final Long proposalKey = 67890L;
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws InvalidIdentifierException {
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
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setSource(source);
                proposal.setState(state);
                proposal.addCriterion("test");
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertNotNull(proposal);
                assertEquals(CommandSettings.State.invalid, proposal.getState());
                return proposal;
            }
        });

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposalKey }, Locale.ENGLISH);
        assertEquals(
                LabelExtractor.get("pv_report_proposal_without_tag", new Object[] { proposalRef }, Locale.ENGLISH),
                BaseConnector.getLastCommunicationInSimulatedMode()
        );
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessIIIa_1() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid criteria
        // Invalid due date
        //

        // ProposalOperations mock
        final Long proposalKey = 67890L;
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws InvalidIdentifierException {
                assertEquals(proposalKey, key);
                assertNull(cKey);
                assertNull(sKey);
                Proposal proposal = new Proposal() {
                    @Override
                    public Date getDueDate() {
                        return null;
                    }
                };
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setSource(source);
                proposal.setState(state);
                proposal.addCriterion("test");
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertNotNull(proposal);
                assertEquals(CommandSettings.State.invalid, proposal.getState());
                return proposal;
            }
        });

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(proposalKey.toString()));
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessIIIa_2() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid criteria
        // Invalid due date
        //

        // ProposalOperations mock
        final Long proposalKey = 67890L;
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws InvalidIdentifierException {
                assertEquals(proposalKey, key);
                assertNull(cKey);
                assertNull(sKey);
                Proposal proposal = new Proposal() {
                    @Override
                    public Date getDueDate() {
                        return new Date(12345L);
                    }
                };
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(saleAssociateKey);
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
        });

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(proposalKey.toString()));
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessIIIa_3() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid criteria
        // Invalid due date
        //

        // ProposalOperations mock
        final Long proposalKey = 67890L;
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws InvalidIdentifierException {
                assertEquals(proposalKey, key);
                assertNull(cKey);
                assertNull(sKey);
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setSource(source);
                proposal.setState(state);
                proposal.addCriterion("test");
                Calendar dueDate = DateUtils.getNowCalendar();
                dueDate.set(Calendar.YEAR, dueDate.get(Calendar.YEAR) + 2);
                proposal.setDueDate(dueDate.getTime());
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertNotNull(proposal);
                assertEquals(CommandSettings.State.invalid, proposal.getState());
                return proposal;
            }
        });

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(BaseConnector.getLastCommunicationInSimulatedMode().contains(proposalKey.toString()));
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessIIIb() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid criteria
        // Valid due date
        // Invalid quantity
        //

        // ProposalOperations mock
        final Long proposalKey = 67890L;
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws InvalidIdentifierException {
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
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setSource(source);
                proposal.addCriterion("test");
                Calendar dueDate = DateUtils.getNowCalendar();
                dueDate.set(Calendar.MONTH, dueDate.get(Calendar.MONTH) + 1);
                proposal.setDueDate(dueDate.getTime());
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertNotNull(proposal);
                assertEquals(CommandSettings.State.invalid, proposal.getState());
                return proposal;
            }
        });

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposalKey }, Locale.ENGLISH);
        assertEquals(
                LabelExtractor.get("pv_report_quantity_zero", new Object[] { proposalRef }, Locale.ENGLISH),
                BaseConnector.getLastCommunicationInSimulatedMode()
        );
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessIV() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid criteria
        // Valid due date
        // Invalid quantity
        //

        // ProposalOperations mock
        final Long proposalKey = 67890L;
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws InvalidIdentifierException {
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
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setSource(source);
                proposal.addCriterion("test");
                Calendar dueDate = DateUtils.getNowCalendar();
                dueDate.set(Calendar.MONTH, dueDate.get(Calendar.MONTH) + 1);
                proposal.setDueDate(dueDate.getTime());
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertNotNull(proposal);
                assertEquals(CommandSettings.State.invalid, proposal.getState());
                return proposal;
            }
        });

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposalKey }, Locale.ENGLISH);
        assertEquals(
                LabelExtractor.get("pv_report_quantity_zero", new Object[] { proposalRef }, Locale.ENGLISH),
                BaseConnector.getLastCommunicationInSimulatedMode()
        );
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessV() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid criteria
        // Valid due date
        // Valid expiration date
        // Invalid price or total
        //

        // ProposalOperations mock
        final Long proposalKey = 67890L;
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws InvalidIdentifierException {
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
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setSource(source);
                proposal.addCriterion("test");
                Calendar dueDate = DateUtils.getNowCalendar();
                dueDate.set(Calendar.MONTH, dueDate.get(Calendar.MONTH) + 1);
                proposal.setDueDate(dueDate.getTime());
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertNotNull(proposal);
                assertEquals(CommandSettings.State.invalid, proposal.getState());
                return proposal;
            }
        });

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposalKey }, Locale.ENGLISH);
        assertEquals(
                LabelExtractor.get("pv_report_missing_price_and_total", new Object[] { proposalRef }, Locale.ENGLISH),
                BaseConnector.getLastCommunicationInSimulatedMode()
        );
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessVI() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid criteria
        // Valid due date
        // Valid quantity
        // Invalid price or total
        //

        // ProposalOperations mock
        final Long proposalKey = 67890L;
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws InvalidIdentifierException {
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
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setSource(source);
                proposal.addCriterion("test");
                Calendar dueDate = DateUtils.getNowCalendar();
                dueDate.set(Calendar.MONTH, dueDate.get(Calendar.MONTH) + 1);
                proposal.setDueDate(dueDate.getTime());
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertNotNull(proposal);
                assertEquals(CommandSettings.State.invalid, proposal.getState());
                return proposal;
            }
        });

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposalKey }, Locale.ENGLISH);
        assertEquals(
                LabelExtractor.get("pv_report_missing_price_and_total", new Object[] { proposalRef }, Locale.ENGLISH),
                BaseConnector.getLastCommunicationInSimulatedMode()
        );
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessVII() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid criteria
        // Valid due date
        // Valid quantity
        // Invalid price or total
        //

        // ProposalOperations mock
        final Long proposalKey = 67890L;
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws InvalidIdentifierException {
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
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setSource(source);
                proposal.addCriterion("test");
                Calendar dueDate = DateUtils.getNowCalendar();
                dueDate.set(Calendar.MONTH, dueDate.get(Calendar.MONTH) + 1);
                proposal.setDueDate(dueDate.getTime());
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertNotNull(proposal);
                assertEquals(CommandSettings.State.invalid, proposal.getState());
                return proposal;
            }
        });

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposalKey }, Locale.ENGLISH);
        assertEquals(
                LabelExtractor.get("pv_report_missing_price_and_total", new Object[] { proposalRef }, Locale.ENGLISH),
                BaseConnector.getLastCommunicationInSimulatedMode()
        );
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessVIII() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid criteria
        // Valid due date
        // Valid quantity
        // Invalid price or total
        //

        // ProposalOperations mock
        final Long proposalKey = 67890L;
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws InvalidIdentifierException {
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
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setSource(source);
                proposal.addCriterion("test");
                Calendar dueDate = DateUtils.getNowCalendar();
                dueDate.set(Calendar.MONTH, dueDate.get(Calendar.MONTH) + 1);
                proposal.setDueDate(dueDate.getTime());
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertNotNull(proposal);
                assertEquals(CommandSettings.State.invalid, proposal.getState());
                return proposal;
            }
        });

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposalKey }, Locale.ENGLISH);
        assertEquals(
                LabelExtractor.get("pv_report_missing_price_and_total", new Object[] { proposalRef }, Locale.ENGLISH),
                BaseConnector.getLastCommunicationInSimulatedMode()
        );
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessIXa() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid criteria
        // Valid due date
        // Valid quantity
        // Valid price (total stays null because just one required)
        // Invalid demand key
        //

        // ProposalOperations mock
        final Long proposalKey = 67890L;
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws InvalidIdentifierException {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal() {
                    @Override
                    public Double getPrice() {
                        return 25.99;
                    }
                };
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setSource(source);
                proposal.addCriterion("test");
                Calendar dueDate = DateUtils.getNowCalendar();
                dueDate.set(Calendar.MONTH, dueDate.get(Calendar.MONTH) + 1);
                proposal.setDueDate(dueDate.getTime());
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertNotNull(proposal);
                assertEquals(CommandSettings.State.invalid, proposal.getState());
                return proposal;
            }
        });

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposalKey }, Locale.ENGLISH);
        assertEquals(
                LabelExtractor.get("pv_report_missing_demand_reference", new Object[] { proposalRef }, Locale.ENGLISH),
                BaseConnector.getLastCommunicationInSimulatedMode()
        );
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessIXb() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid criteria
        // Valid due date
        // Valid quantity
        // Valid price (total stays null because just one required)
        // Invalid demand key
        //

        // ProposalOperations mock
        final Long proposalKey = 67890L;
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws InvalidIdentifierException {
                assertEquals(proposalKey, key);
                Proposal proposal = new Proposal() {
                    @Override
                    public Double getTotal() {
                        return 25.99;
                    }
                };
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setSource(source);
                proposal.addCriterion("test");
                Calendar dueDate = DateUtils.getNowCalendar();
                dueDate.set(Calendar.MONTH, dueDate.get(Calendar.MONTH) + 1);
                proposal.setDueDate(dueDate.getTime());
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertNotNull(proposal);
                assertEquals(CommandSettings.State.invalid, proposal.getState());
                return proposal;
            }
        });

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposalKey }, Locale.ENGLISH);
        assertEquals(
                LabelExtractor.get("pv_report_missing_demand_reference", new Object[] { proposalRef }, Locale.ENGLISH),
                BaseConnector.getLastCommunicationInSimulatedMode()
        );
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessX() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid criteria
        // Valid due date
        // Valid quantity
        // Valid price (total stays null because just one required)
        // Invalid demand key
        //

        // ProposalOperations mock
        final Long proposalKey = 67890L;
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws InvalidIdentifierException {
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
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setSource(source);
                proposal.addCriterion("test");
                Calendar dueDate = DateUtils.getNowCalendar();
                dueDate.set(Calendar.MONTH, dueDate.get(Calendar.MONTH) + 1);
                proposal.setDueDate(dueDate.getTime());
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertNotNull(proposal);
                assertEquals(CommandSettings.State.invalid, proposal.getState());
                return proposal;
            }
        });

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposalKey }, Locale.ENGLISH);
        assertEquals(
                LabelExtractor.get("pv_report_missing_demand_reference", new Object[] { proposalRef }, Locale.ENGLISH),
                BaseConnector.getLastCommunicationInSimulatedMode()
        );
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessXI() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid criteria
        // Valid due date
        // Valid quantity
        // Valid price (total stays null because just one required)
        // Invalid demand key
        //

        // ProposalOperations mock
        final Long proposalKey = 67890L;
        final Long demandKey = 54321L;
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws InvalidIdentifierException {
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
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setSource(source);
                proposal.addCriterion("test");
                Calendar dueDate = DateUtils.getNowCalendar();
                dueDate.set(Calendar.MONTH, dueDate.get(Calendar.MONTH) + 1);
                proposal.setDueDate(dueDate.getTime());
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertNotNull(proposal);
                assertEquals(CommandSettings.State.invalid, proposal.getState());
                return proposal;
            }
        });
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                throw new InvalidIdentifierException("Done in purpose");
            }
        });

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposalKey }, Locale.ENGLISH);
        String demandRef = LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { demandKey }, Locale.ENGLISH);
        assertEquals(
                LabelExtractor.get("pv_report_invalid_demand_reference", new Object[] { proposalRef, demandRef }, Locale.ENGLISH),
                BaseConnector.getLastCommunicationInSimulatedMode()
        );
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    @Ignore
    public void testProcessXII() throws DataSourceException, InvalidIdentifierException {
        //
        // Valid criteria
        // Valid due date
        // Valid quantity
        // Valid price (total stays null because just one required)
        // Valid demand key
        //

        // ProposalOperations mock
        final Long proposalKey = 67890L;
        final Long demandKey = 54321L;
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws InvalidIdentifierException {
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
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setSource(source);
                proposal.addCriterion("test");
                Calendar dueDate = DateUtils.getNowCalendar();
                dueDate.set(Calendar.MONTH, dueDate.get(Calendar.MONTH) + 1);
                proposal.setDueDate(dueDate.getTime());
                return proposal;
            }
            @Override
            public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
                assertNotNull(proposal);
                assertEquals(CommandSettings.State.published, proposal.getState());
                return proposal;
            }
        });
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long cKey) throws InvalidIdentifierException {
                assertEquals(demandKey, key);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                return demand;
            }
        });

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessXIII() throws DataSourceException, InvalidIdentifierException {
        //
        // Error while getting sale associate information
        //

        // ProposalOperations mock
        final Long proposalKey = 67890L;
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws InvalidIdentifierException {
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(saleAssociateKey);
                return proposal;
            }
        });
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(saleAssociateKey, key);
                throw new InvalidIdentifierException("Done in purpose");
            }
        });

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testProcessXIV() throws DataSourceException, InvalidIdentifierException {
        //
        // Error while informing about the error
        //

        // ProposalOperations mock
        final Long proposalKey = 67890L;
        BaseSteps.setMockProposalOperations(new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws InvalidIdentifierException {
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setSource(Source.mail);
                return proposal;
            }
        });

        final Long saConsumerRecordKey = 76325L;
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setConsumerKey(saConsumerRecordKey);
                return saleAssociate;
            }
        });

        final Consumer consumer = new Consumer();
        consumer.setKey(saConsumerRecordKey);
        consumer.setPreferredConnection(Source.simulated);
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(saConsumerRecordKey, key);
                return consumer;
            }
        });

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousPersistenceManager().isClosed());
    }

    @Test
    public void testFilterHashTagsI() throws ClientException, DataSourceException {
        Proposal proposal = new Proposal() {
            @Override
            public List<String> getHashTags() {
                return null;
            }
        };
        RequestValidator.filterHashTags(new MockPersistenceManager(), new Consumer(), proposal, "proposal");
        assertNull(proposal.getHashTags());
    }

    @Test
    public void testFilterHashTagsII() throws ClientException, DataSourceException {
        Proposal proposal = new Proposal();
        RequestValidator.filterHashTags(new MockPersistenceManager(), new Consumer(), proposal, "proposal");
        assertNotNull(proposal.getHashTags());
        assertEquals(0, proposal.getHashTags().size());
    }

    @Test
    public void testFilterHashTagsIII() throws ClientException, DataSourceException {
        Proposal proposal = new Proposal();
        proposal.addHashTag(RobotResponder.ROBOT_DEMO_HASH_TAG);
        RequestValidator.filterHashTags(new MockPersistenceManager(), new Consumer(), proposal, "proposal");
        assertEquals(1, proposal.getHashTags().size());
        assertEquals(RobotResponder.ROBOT_DEMO_HASH_TAG, proposal.getHashTags().get(0));
    }

    @Test
    public void testFilterHashTagsIV() throws ClientException, DataSourceException {
        Proposal proposal = new Proposal();
        proposal.addHashTag(RobotResponder.ROBOT_DEMO_HASH_TAG);
        proposal.addHashTag(RobotResponder.ROBOT_DEMO_HASH_TAG);
        proposal.addHashTag(RobotResponder.ROBOT_DEMO_HASH_TAG);
        RequestValidator.filterHashTags(new MockPersistenceManager(), new Consumer(), proposal, "proposal");
        assertEquals(1, proposal.getHashTags().size());
        assertEquals(RobotResponder.ROBOT_DEMO_HASH_TAG, proposal.getHashTags().get(0));
    }

    @Test
    public void testFilterHashTagsV() throws ClientException, DataSourceException {
        final Long proposalKey = 67890L;
        Proposal proposal = new Proposal();
        proposal.setKey(proposalKey);
        proposal.setSource(Source.simulated);
        proposal.addHashTag("test");
        RequestValidator.filterHashTags(new MockPersistenceManager(), new Consumer(), proposal, "proposal");
        assertEquals(0, proposal.getHashTags().size());

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposalKey }, Locale.ENGLISH);
        String tags = LabelExtractor.get("cp_tweet_tags_part", new Object[] { "test" }, Locale.ENGLISH);
        assertEquals(LabelExtractor.get("dv_report_hashtag_warning", new Object[] { proposalRef, tags }, Locale.ENGLISH), sentText);
    }

    @Test
    public void testFilterHashTagsVI() throws ClientException, DataSourceException {
        final Long proposalKey = 67890L;
        Proposal proposal = new Proposal();
        proposal.setKey(proposalKey);
        proposal.setSource(Source.simulated);
        proposal.addHashTag(RobotResponder.ROBOT_DEMO_HASH_TAG);
        proposal.addHashTag("unit");
        proposal.addHashTag("test");
        RequestValidator.filterHashTags(new MockPersistenceManager(), new Consumer(), proposal, "proposal");
        assertEquals(1, proposal.getHashTags().size());
        assertEquals(RobotResponder.ROBOT_DEMO_HASH_TAG, proposal.getHashTags().get(0));

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposalKey }, Locale.ENGLISH);
        String tags = LabelExtractor.get("cp_tweet_tags_part", new Object[] { "unit test" }, Locale.ENGLISH);
        assertEquals(LabelExtractor.get("dv_report_hashtag_warning", new Object[] { proposalRef, tags }, Locale.ENGLISH), sentText);
    }

    @Test
    public void testFilterHashTagsVII() throws ClientException, DataSourceException {
        final Long proposalKey = 67890L;
        Proposal proposal = new Proposal();
        proposal.setKey(proposalKey);
        proposal.setSource(Source.simulated);
        proposal.addHashTag("unit");
        proposal.addHashTag("test");
        proposal.addHashTag(RobotResponder.ROBOT_DEMO_HASH_TAG);
        RequestValidator.filterHashTags(new MockPersistenceManager(), new Consumer(), proposal, "proposal");
        assertEquals(1, proposal.getHashTags().size());
        assertEquals(RobotResponder.ROBOT_DEMO_HASH_TAG, proposal.getHashTags().get(0));

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposalKey }, Locale.ENGLISH);
        String tags = LabelExtractor.get("cp_tweet_tags_part", new Object[] { "unit test" }, Locale.ENGLISH);
        assertEquals(LabelExtractor.get("dv_report_hashtag_warning", new Object[] { proposalRef, tags }, Locale.ENGLISH), sentText);
    }
}
