package twetailer.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
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
import twetailer.connector.BaseConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.BaseOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.MockBaseOperations;
import twetailer.dao.ProposalOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Proposal;
import twetailer.dto.SaleAssociate;
import twetailer.validator.CommandSettings;
import twetailer.validator.CommandSettings.State;

import com.google.apphosting.api.MockAppEngineEnvironment;

import domderrien.i18n.LabelExtractor;

public class TestProposalValidator {

    final Long saleAssociateKey = 54321L;
    final Source source = Source.simulated;
    final State state = State.opened;

    private static MockAppEngineEnvironment mockAppEngineEnvironment;

    @BeforeClass
    public static void setUpBeforeClass() {
        ProposalValidator.setLogger(new MockLogger("test", null));
        mockAppEngineEnvironment = new MockAppEngineEnvironment();
    }

    @Before
    public void setUp() throws Exception {
        mockAppEngineEnvironment.setUp();

        // SaleAssociateOperations mock
        SaleAssociateOperations saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                assertEquals(saleAssociateKey, key);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setPreferredConnection(source);
                return saleAssociate;
            }
        };

        // Install the mocks
        ProposalValidator._baseOperations = new MockBaseOperations();
        ProposalValidator.saleAssociateOperations = saleAssociateOperations;

        // Be sure to start with a clean message stack
        BaseConnector.resetLastCommunicationInSimulatedMode();
    }

    @After
    public void tearDown() throws Exception {
        mockAppEngineEnvironment.tearDown();
        ProposalValidator._baseOperations = new BaseOperations();
        ProposalValidator.saleAssociateOperations = ProposalValidator._baseOperations.getSaleAssociateOperations();
        ProposalValidator.proposalOperations = ProposalValidator._baseOperations.getProposalOperations();
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
        };

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertEquals(
                LabelExtractor.get("pv_report_proposal_without_tag", new Object[] { proposalKey }, Locale.ENGLISH),
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
        };

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertNotNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertEquals(
                LabelExtractor.get("pv_report_proposal_without_tag", new Object[] { proposalKey }, Locale.ENGLISH),
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
                proposal.setOwnerKey(saleAssociateKey);
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
                LabelExtractor.get("pv_report_quantity_zero", new Object[] { proposalKey }, Locale.ENGLISH),
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
                proposal.setOwnerKey(saleAssociateKey);
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
                LabelExtractor.get("pv_report_quantity_zero", new Object[] { proposalKey }, Locale.ENGLISH),
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
                proposal.setOwnerKey(saleAssociateKey);
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
                LabelExtractor.get("pv_report_missing_price_and_total", new Object[] { proposalKey }, Locale.ENGLISH),
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
                proposal.setOwnerKey(saleAssociateKey);
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
                LabelExtractor.get("pv_report_missing_price_and_total", new Object[] { proposalKey }, Locale.ENGLISH),
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
                proposal.setOwnerKey(saleAssociateKey);
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
                LabelExtractor.get("pv_report_missing_price_and_total", new Object[] { proposalKey }, Locale.ENGLISH),
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
                proposal.setOwnerKey(saleAssociateKey);
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
                LabelExtractor.get("pv_report_missing_price_and_total", new Object[] { proposalKey }, Locale.ENGLISH),
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
                proposal.setOwnerKey(saleAssociateKey);
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
                LabelExtractor.get("pv_report_missing_demand_reference", new Object[] { proposalKey }, Locale.ENGLISH),
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
                proposal.setOwnerKey(saleAssociateKey);
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
                LabelExtractor.get("pv_report_missing_demand_reference", new Object[] { proposalKey }, Locale.ENGLISH),
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
                proposal.setOwnerKey(saleAssociateKey);
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
                LabelExtractor.get("pv_report_missing_demand_reference", new Object[] { proposalKey }, Locale.ENGLISH),
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
                proposal.setOwnerKey(saleAssociateKey);
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
                LabelExtractor.get("pv_report_invalid_demand_reference", new Object[] { proposalKey, demandKey }, Locale.ENGLISH),
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
                proposal.setOwnerKey(saleAssociateKey);
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
        // Error while getting sale associate information
        //

        // ProposalOperations mock
        final Long proposalKey = 67890L;
        ProposalValidator.proposalOperations = new ProposalOperations() {
            @Override
            public Proposal getProposal(PersistenceManager pm, Long key, Long cKey, Long sKey) throws DataSourceException {
                Proposal proposal = new Proposal();
                proposal.setKey(proposalKey);
                proposal.setOwnerKey(saleAssociateKey);
                return proposal;
            }
        };
        ProposalValidator.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) throws DataSourceException {
                assertEquals(saleAssociateKey, key);
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
                proposal.setOwnerKey(saleAssociateKey);
                proposal.setSource(Source.mail);
                return proposal;
            }
        };

        ProposalValidator.saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) throws DataSourceException {
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setEmail("@@@@");
                return saleAssociate;
            }
        };

        // Process the test case
        ProposalValidator.process(proposalKey);

        assertNull(BaseConnector.getLastCommunicationInSimulatedMode());
        assertTrue(ProposalValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testFilterHashTagsI() throws ClientException, DataSourceException {
        Proposal proposal = new Proposal() {
            @Override
            public List<String> getHashTags() {
                return null;
            }
        };
        ProposalValidator.filterHashTags(new MockPersistenceManager(), new SaleAssociate(), proposal);
        assertNull(proposal.getHashTags());
    }

    @Test
    public void testFilterHashTagsII() throws ClientException, DataSourceException {
        Proposal proposal = new Proposal();
        ProposalValidator.filterHashTags(new MockPersistenceManager(), new SaleAssociate(), proposal);
        assertNotNull(proposal.getHashTags());
        assertEquals(0, proposal.getHashTags().size());
    }

    @Test
    public void testFilterHashTagsIII() throws ClientException, DataSourceException {
        Proposal proposal = new Proposal();
        proposal.addHashTag(RobotResponder.ROBOT_DEMO_HASH_TAG);
        ProposalValidator.filterHashTags(new MockPersistenceManager(), new SaleAssociate(), proposal);
        assertEquals(1, proposal.getHashTags().size());
        assertEquals(RobotResponder.ROBOT_DEMO_HASH_TAG, proposal.getHashTags().get(0));
    }

    @Test
    public void testFilterHashTagsIV() throws ClientException, DataSourceException {
        Proposal proposal = new Proposal();
        proposal.addHashTag(RobotResponder.ROBOT_DEMO_HASH_TAG);
        proposal.addHashTag(RobotResponder.ROBOT_DEMO_HASH_TAG);
        proposal.addHashTag(RobotResponder.ROBOT_DEMO_HASH_TAG);
        ProposalValidator.filterHashTags(new MockPersistenceManager(), new SaleAssociate(), proposal);
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
        ProposalValidator.filterHashTags(new MockPersistenceManager(), new SaleAssociate(), proposal);
        assertEquals(0, proposal.getHashTags().size());

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertEquals(LabelExtractor.get("pv_report_hashtag_warning", new Object[] { proposalKey, "test" }, Locale.ENGLISH), sentText);
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
        ProposalValidator.filterHashTags(new MockPersistenceManager(), new SaleAssociate(), proposal);
        assertEquals(1, proposal.getHashTags().size());
        assertEquals(RobotResponder.ROBOT_DEMO_HASH_TAG, proposal.getHashTags().get(0));

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertEquals(LabelExtractor.get("pv_report_hashtag_warning", new Object[] { proposalKey, "unit test" }, Locale.ENGLISH), sentText);
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
        ProposalValidator.filterHashTags(new MockPersistenceManager(), new SaleAssociate(), proposal);
        assertEquals(1, proposal.getHashTags().size());
        assertEquals(RobotResponder.ROBOT_DEMO_HASH_TAG, proposal.getHashTags().get(0));

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertEquals(LabelExtractor.get("pv_report_hashtag_warning", new Object[] { proposalKey, "unit test" }, Locale.ENGLISH), sentText);
    }
}
