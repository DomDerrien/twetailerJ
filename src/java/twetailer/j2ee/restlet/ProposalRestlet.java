package twetailer.j2ee.restlet;

import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.ProposalOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dto.Demand;
import twetailer.dto.Payment;
import twetailer.dto.Proposal;
import twetailer.dto.SaleAssociate;
import twetailer.j2ee.BaseRestlet;
import twetailer.j2ee.LoginServlet;
import twetailer.payment.AmazonFPS;
import twetailer.validator.CommandSettings.State;

import com.dyuproject.openid.OpenIdUser;

import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

@SuppressWarnings("serial")
public class ProposalRestlet extends BaseRestlet {
    private static Logger log = Logger.getLogger(DemandRestlet.class.getName());

    protected static BaseOperations _baseOperations = new BaseOperations();
    protected static DemandOperations demandOperations = _baseOperations.getDemandOperations();
    protected static ConsumerOperations consumerOperations = _baseOperations.getConsumerOperations();
    protected static ProposalOperations proposalOperations = _baseOperations.getProposalOperations();
    protected static SaleAssociateOperations saleAssociateOperations = _baseOperations.getSaleAssociateOperations();

    protected static AmazonFPS amazonFPS = new AmazonFPS();

    // Setter for injection of a MockLogger at test time
    protected static void setLogger(Logger mock) {
        log = mock;
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    protected JsonObject createResource(JsonObject parameters, OpenIdUser loggedUser) throws DataSourceException, ClientException {
        PersistenceManager pm = _baseOperations.getPersistenceManager();
        try {
            // Get the sale associate
            Long consumerKey = (Long) loggedUser.getAttribute(LoginServlet.AUTHENTICATED_USER_TWETAILER_ID);
            List<SaleAssociate> saleAssociates = saleAssociateOperations.getSaleAssociates(pm, SaleAssociate.CONSUMER_KEY, consumerKey, 1);
            if (0 < saleAssociates.size()) {
                return proposalOperations.createProposal(pm, parameters, saleAssociates.get(0)).toJson();
            }
            throw new ClientException("Current user is not a Sale Associate!");
        }
        finally {
            pm.close();
        }
    }

    @Override
    protected void deleteResource(String resourceId, OpenIdUser loggedUser) throws DataSourceException, ClientException {
        if (isAPrivilegedUser(loggedUser)) {
            PersistenceManager pm = _baseOperations.getPersistenceManager();
            try {
                Long proposalKey = Long.valueOf(resourceId);
                // Get the sale associate
                Long consumerKey = (Long) loggedUser.getAttribute(LoginServlet.AUTHENTICATED_USER_TWETAILER_ID);
                List<SaleAssociate> saleAssociates = saleAssociateOperations.getSaleAssociates(pm, SaleAssociate.CONSUMER_KEY, consumerKey, 1);
                if (0 < saleAssociates.size()) {
                    SaleAssociate saleAssociate = saleAssociates.get(0);
                    delegateResourceDeletion(pm, proposalKey, saleAssociate, false);
                }
                return;
            }
            finally {
                pm.close();
            }
        }
        throw new ClientException("Restricted access!");
    }

    /**
     * Delete the Proposal instances based on the specified criteria.
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param proposalKey Identifier of the resource to delete
     * @param saleAssociate Resource owner
     * @param stopRecursion Should be <code>false</code> if the associated Proposals need to be affected too
     * @return Serialized list of the Consumer instances matching the given criteria

     * @throws DataSourceException If the query to the back-end fails
     *
     * @see SaleAssociateRestlet#delegateResourceDeletion(PersistenceManager, Long)
     */
    protected void delegateResourceDeletion(PersistenceManager pm, Long proposalKey, SaleAssociate saleAssociate, boolean stopRecursion) throws DataSourceException{
        // Delete consumer's proposals
        Proposal proposal = proposalOperations.getProposal(pm, proposalKey, saleAssociate.getKey(), null);
        proposalOperations.deleteProposal(pm, proposal);
        if (!stopRecursion && proposal.getDemandKey() != null) {
            // Clean-up the attached demand
            try {
                Demand demand = demandOperations.getDemand(pm, proposal.getDemandKey(), saleAssociate.getConsumerKey());
                demand.removeProposalKey(proposalKey);
                demandOperations.updateDemand(pm, demand);
            }
            catch(DataSourceException ex) {
                // Demand not accessible, that's fine.
                // Worse case, the orphan objects will be collected later
            }
        }
    }

    @Override
    protected JsonObject getResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser) throws DataSourceException {
        PersistenceManager pm = _baseOperations.getPersistenceManager();
        try {
            // Try to get the proposal
            Long proposalKey = Long.valueOf(resourceId);
            Proposal proposal = proposalOperations.getProposal(pm, proposalKey, null, null);

            // Try to get the proposal owner
            Long consumerKey = (Long) loggedUser.getAttribute(LoginServlet.AUTHENTICATED_USER_TWETAILER_ID);
            List<Long> saleAssociates = saleAssociateOperations.getSaleAssociateKeys(pm, SaleAssociate.CONSUMER_KEY, consumerKey, 1);
            if (saleAssociates.size() == 0 || !saleAssociates.get(0).equals(proposal.getOwnerKey())) {
                // Try to get the associated demand -- will fail if the querying consumer does own the demand
                Demand demand = demandOperations.getDemand(pm, proposal.getDemandKey(), consumerKey);
                if (State.confirmed.equals(proposal.getState())) {
                    //
                    // TODO: verify the store record to check if it accepts AWS FPS payment
                    // TODO: verify that the proposal has a total cost value
                    //
                    // Cook the Amazon FPS Co-Branded service URL
                    String description = LabelExtractor.get(
                            "payment_transaction_description",
                            new Object[] {
                                    proposal.getSerializedCriteria(),
                                    demand.getSerializedCriteria()
                            },
                            Locale.ENGLISH // FIXME: get logged user's locale
                    );
                    try {
                        String transactionReference = Payment.getReference(consumerKey, demand.getKey(), proposalKey);
                        proposal.setAWSCBUIURL(amazonFPS.getCoBrandedServiceUrl(transactionReference, description, proposal.getTotal(), "USD"));
                    }
                    catch (Exception ex) {
                        throw new DataSourceException("Cannot compute the AWS FPS Co-Branded Service URL", ex);
                    }
                }
            }

            return proposal.toJson();
        }
        finally {
            pm.close();
        }
    }

    @Override
    protected JsonArray selectResources(JsonObject parameters, OpenIdUser loggedUser) throws DataSourceException {
        throw new RuntimeException("Not yet implemented!");
    }

    @Override
    protected JsonObject updateResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser) throws DataSourceException {
        throw new RuntimeException("Not yet implemented!");
    }
}
