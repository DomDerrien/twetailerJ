package twetailer.j2ee.restlet;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.connector.BaseConnector.Source;
import twetailer.dto.Command;
import twetailer.dto.Demand;
import twetailer.dto.Entity;
import twetailer.dto.Proposal;
import twetailer.dto.SaleAssociate;
import twetailer.dto.Command.QueryPointOfView;
import twetailer.j2ee.BaseRestlet;
import twetailer.j2ee.LoginServlet;
import twetailer.payment.AmazonFPS;
import twetailer.task.step.BaseSteps;
import twetailer.task.step.ProposalSteps;

import com.dyuproject.openid.OpenIdUser;

import domderrien.i18n.DateUtils;
import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonUtils;

/**
 * Restlet entry point for the Proposal entity control.
 *
 * @author Dom Derrien
 */
@SuppressWarnings("serial")
public class ProposalRestlet extends BaseRestlet {
    private static Logger log = Logger.getLogger(DemandRestlet.class.getName());

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
    protected JsonObject getResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser) throws DataSourceException, ClientException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            Long proposalKey = Long.valueOf(resourceId);
            Long ownerKey = LoginServlet.getConsumerKey(loggedUser);
            QueryPointOfView pointOfView = QueryPointOfView.fromJson(parameters, QueryPointOfView.SALE_ASSOCIATE);
            Long saleAssociateKey = null;
            Long storeKey = null;
            if (QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView)) {
                SaleAssociate saleAssociate = LoginServlet.getSaleAssociate(loggedUser, pm);
                saleAssociateKey = saleAssociate.getKey();
                storeKey = saleAssociate.getStoreKey();
            }
            Proposal proposal = ProposalSteps.getProposal(pm, proposalKey, ownerKey, pointOfView, saleAssociateKey, storeKey);

            return ProposalSteps.anonymizeProposal(pointOfView, proposal.toJson());
        }
        finally {
            pm.close();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected JsonArray selectResources(JsonObject parameters, OpenIdUser loggedUser) throws InvalidIdentifierException, DataSourceException {

        Map<String, Object> queryDemands = new HashMap<String, Object>();

        if (!parameters.containsKey(ANY_STATE_PARAMETER_KEY)) {
            queryDemands.put(Demand.STATE_COMMAND_LIST, Boolean.TRUE);
        }

        Date lastModificationDate = null;
        if (parameters.containsKey(Entity.MODIFICATION_DATE)) {
            try {
                lastModificationDate = DateUtils.isoToDate(parameters.getString(Entity.MODIFICATION_DATE));
                queryDemands.put(">" + Entity.MODIFICATION_DATE, lastModificationDate);
            }
            catch (ParseException e) { } // Date not set, too bad.
        }

        QueryPointOfView pointOfView = QueryPointOfView.fromJson(parameters, QueryPointOfView.SALE_ASSOCIATE);
        boolean onlyKeys = parameters.containsKey(ONLY_KEYS_PARAMETER_KEY);
        int maximumResults = (int) parameters.getLong(MAXIMUM_RESULTS_PARAMETER_KEY);

        JsonArray resources;
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            if (QueryPointOfView.CONSUMER.equals(pointOfView)) {
                throw new RuntimeException("Consumer getting a list of proposals: not yet implemented!");
            }
            else if (QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView)) {
                Long saleAssociateKey = LoginServlet.getSaleAssociateKey(loggedUser, pm);
                queryDemands.put(Command.OWNER_KEY, saleAssociateKey);
                if (onlyKeys) {
                    resources = new GenericJsonArray((List) BaseSteps.getProposalOperations().getProposalKeys(pm, queryDemands, maximumResults));
                }
                else { // full detail
                    resources = JsonUtils.toJson((List) BaseSteps.getProposalOperations().getProposals(pm, queryDemands, maximumResults));
                }
            }
            else { // if (QueryPointOfView.anonymous.equals(pointOfView)) {
                throw new RuntimeException("Anonymous getting a list of proposals: not yet implemented!");
            }
        }
        finally {
            pm.close();
        }
        return resources;
    }

    @Override
    protected JsonObject createResource(JsonObject parameters, OpenIdUser loggedUser) throws DataSourceException, ClientException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            // Create the Proposal
            parameters.put(Command.SOURCE, Source.api.toString());

            Proposal proposal = ProposalSteps.createProposal(pm, parameters, LoginServlet.getSaleAssociate(loggedUser, pm), LoginServlet.getConsumer(loggedUser, pm));

            return proposal.toJson();
        }
        finally {
            pm.close();
        }
    }

    @Override
    protected JsonObject updateResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser) throws DataSourceException, ClientException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            // Update the proposal
            Proposal proposal = null;
            Long proposalKey = Long.valueOf(resourceId);
            QueryPointOfView pointOfView = QueryPointOfView.fromJson(parameters, QueryPointOfView.SALE_ASSOCIATE);
            if (QueryPointOfView.CONSUMER.equals(pointOfView)) {
                proposal = ProposalSteps.updateProposal(pm, proposalKey, parameters, LoginServlet.getConsumer(loggedUser, pm));
            }
            else {
                proposal = ProposalSteps.updateProposal(pm, proposalKey, parameters, LoginServlet.getSaleAssociate(loggedUser, pm), LoginServlet.getConsumer(loggedUser, pm));
            }

            return proposal.toJson();
        }
        finally {
            pm.close();
        }
    }

    /**** Dom: refactoring limit ***/

    @Override
    protected void deleteResource(String resourceId, OpenIdUser loggedUser) throws DataSourceException, ClientException {
        if (isAPrivilegedUser(loggedUser)) {
            PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
            try {
                Long proposalKey = Long.valueOf(resourceId);
                // Get the sale associate
                SaleAssociate saleAssociate = LoginServlet.getSaleAssociate(loggedUser, pm);
                if (saleAssociate == null) {
                    throw new ClientException("Current user is not a Sale Associate!");
                }
                delegateResourceDeletion(pm, proposalKey, saleAssociate, false);
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
    protected void delegateResourceDeletion(PersistenceManager pm, Long proposalKey, SaleAssociate saleAssociate, boolean stopRecursion) throws InvalidIdentifierException{
        // Delete consumer's proposals
        Proposal proposal = BaseSteps.getProposalOperations().getProposal(pm, proposalKey, saleAssociate.getKey(), null);
        BaseSteps.getProposalOperations().deleteProposal(pm, proposal);
        if (!stopRecursion && proposal.getDemandKey() != null) {
            // Clean-up the attached demand
            try {
                Demand demand = BaseSteps.getDemandOperations().getDemand(pm, proposal.getDemandKey(), saleAssociate.getConsumerKey());
                demand.removeProposalKey(proposalKey);
                BaseSteps.getDemandOperations().updateDemand(pm, demand);
            }
            catch(InvalidIdentifierException ex) {
                // Demand not accessible, that's fine.
                // Worse case, the orphan objects will be collected later
            }
        }
    }
}
