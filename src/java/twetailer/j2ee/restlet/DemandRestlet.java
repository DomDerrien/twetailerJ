package twetailer.j2ee.restlet;

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.ReservedOperationException;
import twetailer.connector.BaseConnector.Source;
import twetailer.dto.Command;
import twetailer.dto.Demand;
import twetailer.dto.Proposal;
import twetailer.dto.Command.QueryPointOfView;
import twetailer.j2ee.BaseRestlet;
import twetailer.j2ee.LoginServlet;
import twetailer.task.step.BaseSteps;
import twetailer.task.step.DemandSteps;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.State;

import com.dyuproject.openid.OpenIdUser;

import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonUtils;

/**
 * Restlet entry point for the Demand entity control.
 *
 * @author Dom Derrien
 */
@SuppressWarnings("serial")
public class DemandRestlet extends BaseRestlet {
    private static Logger log = Logger.getLogger(DemandRestlet.class.getName());

    // Setter for injection of a MockLogger at test time
    protected static void setLogger(Logger mock) {
        log = mock;
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    public JsonObject getResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser) throws DataSourceException, ClientException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            Long demandKey = Long.valueOf(resourceId);
            Long ownerKey = LoginServlet.getConsumerKey(loggedUser);
            QueryPointOfView pointOfView = QueryPointOfView.fromJson(parameters, QueryPointOfView.CONSUMER);
            Long saleAssociateKey = QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView) ? LoginServlet.getSaleAssociateKey(loggedUser, pm) : null;
            Demand demand = DemandSteps.getDemand(pm, demandKey, ownerKey, pointOfView, saleAssociateKey);

            return DemandSteps.anonymizeDemand(pm, pointOfView, demand.toJson(), saleAssociateKey);
        }
        finally {
            pm.close();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected JsonArray selectResources(JsonObject parameters, OpenIdUser loggedUser) throws DataSourceException, ClientException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            Long ownerKey = LoginServlet.getConsumerKey(loggedUser);
            QueryPointOfView pointOfView = QueryPointOfView.fromJson(parameters, QueryPointOfView.CONSUMER);
            Long saleAssociateKey = QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView) ? LoginServlet.getSaleAssociateKey(loggedUser, pm) : null;
            boolean onlyKeys = parameters.containsKey(BaseRestlet.ONLY_KEYS_PARAMETER_KEY);

            JsonArray resources;
            if (onlyKeys) {
                // Get the keys
                resources = new GenericJsonArray((List) DemandSteps.getDemandKeys(pm, parameters, ownerKey, pointOfView, saleAssociateKey));
            }
            else { // full detail
                // Get the demands
                resources = JsonUtils.toJson(DemandSteps.getDemands(pm, parameters, ownerKey, pointOfView, saleAssociateKey));
                resources = DemandSteps.anonymizeDemands(pointOfView, resources, saleAssociateKey);
            }
            return resources;
        }
        finally {
            pm.close();
        }
    }

    @Override
    protected JsonObject createResource(JsonObject parameters, OpenIdUser loggedUser) throws DataSourceException, ClientException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            // Create the Demand
            parameters.put(Command.SOURCE, Source.api.toString());

            Demand demand = DemandSteps.createDemand(pm, parameters, LoginServlet.getConsumer(loggedUser));

            return demand.toJson();
        }
        finally {
            pm.close();
        }
    }

    @Override
    protected JsonObject updateResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser) throws DataSourceException, ClientException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            // Update the Demand
            Long demandKey = Long.valueOf(resourceId);
            Demand demand = DemandSteps.updateDemand(pm, demandKey, parameters, LoginServlet.getConsumer(loggedUser));

            return demand.toJson();
        }
        finally {
            pm.close();
        }
    }

    /**** Dom: refactoring limit
     * @throws DataSourceException ***/

    @Override
    protected void deleteResource(String resourceId, OpenIdUser loggedUser) throws InvalidIdentifierException, ReservedOperationException, DataSourceException {
        if (isAPrivilegedUser(loggedUser)) {
            PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
            try {
                Long demandKey = Long.valueOf(resourceId);
                delegateResourceDeletion(pm, demandKey, LoginServlet.getConsumerKey(loggedUser), false);
                return;
            }
            finally {
                pm.close();
            }
        }
        throw new ReservedOperationException(Action.delete, "Restricted access!");
    }

    /**
     * Delete the Demand instances based on the specified criteria.
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param demandKey Identifier of the resource to delete
     * @param consumerKey Identifier of the demand owner
     * @param stopRecursion Should be <code>false</code> if the associated Proposals need to be affected too
     * @return Serialized list of the Consumer instances matching the given criteria

     * @throws InvalidIdentifierException If the query to the back-end fails
     * @throws DataSourceException If the deletion operation fails
     *
     * @see SaleAssociateRestlet#delegateResourceDeletion(PersistenceManager, Long)
     */
    protected void delegateResourceDeletion(PersistenceManager pm, Long demandKey, Long consumerKey, boolean stopRecursion) throws InvalidIdentifierException, DataSourceException{
        // Delete consumer's demands
        BaseSteps.getDemandOperations().deleteDemand(pm, demandKey, consumerKey);
        if (!stopRecursion) {
            // Clean-up the attached proposals
            List<Proposal> proposals = BaseSteps.getProposalOperations().getProposals(pm, Proposal.DEMAND_KEY, demandKey, 0);
            for (Proposal proposal: proposals) {
                proposal.setState(State.cancelled);
                proposal.setDemandKey(0L); // To cut the link
                BaseSteps.getProposalOperations().updateProposal(pm, proposal);
            }
        }
    }
}
