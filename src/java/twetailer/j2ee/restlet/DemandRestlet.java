package twetailer.j2ee.restlet;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.ReservedOperationException;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.ProposalOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dto.Command;
import twetailer.dto.Demand;
import twetailer.dto.Entity;
import twetailer.dto.Proposal;
import twetailer.j2ee.BaseRestlet;
import twetailer.j2ee.LoginServlet;
import twetailer.task.CommandProcessor;
import twetailer.validator.ApplicationSettings;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.State;

import com.dyuproject.openid.OpenIdUser;
import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

import domderrien.i18n.DateUtils;
import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonUtils;

@SuppressWarnings("serial")
public class DemandRestlet extends BaseRestlet {
    private static Logger log = Logger.getLogger(DemandRestlet.class.getName());

    protected static BaseOperations _baseOperations = new BaseOperations();
    protected static DemandOperations demandOperations = _baseOperations.getDemandOperations();
    protected static ConsumerOperations consumerOperations = _baseOperations.getConsumerOperations();
    protected static ProposalOperations proposalOperations = _baseOperations.getProposalOperations();
    protected static SaleAssociateOperations saleAssociateOperations = _baseOperations.getSaleAssociateOperations();

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
        // Create the Demand
        parameters.put(Command.SOURCE, Source.api.toString());
        Demand demand = demandOperations.createDemand(parameters, LoginServlet.getConsumerKey(loggedUser));

        // Create a task for that demand validation
        Queue queue = CommandProcessor._baseOperations.getQueue();
        log.warning("Preparing the task: /maezel/validateOpenDemand?key=" + demand.getKey().toString());
        queue.add(
                url(ApplicationSettings.get().getServletApiPath() + "/maezel/validateOpenDemand").
                    param(Proposal.KEY, demand.getKey().toString()).
                    method(Method.GET)
        );

        return demand.toJson();
    }

    @Override
    protected void deleteResource(String resourceId, OpenIdUser loggedUser) throws DataSourceException, ClientException {
        if (isAPrivilegedUser(loggedUser)) {
            PersistenceManager pm = _baseOperations.getPersistenceManager();
            try {
                Long demandKey = Long.valueOf(resourceId);
                delegateResourceDeletion(pm, demandKey, LoginServlet.getConsumerKey(loggedUser), false);
                return;
            }
            finally {
                pm.close();
            }
        }
        throw new ClientException("Restricted access!");
    }

    /**
     * Delete the Demand instances based on the specified criteria.
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param demandKey Identifier of the resource to delete
     * @param consumerKey Identifier of the demand owner
     * @param stopRecursion Should be <code>false</code> if the associated Proposals need to be affected too
     * @return Serialized list of the Consumer instances matching the given criteria

     * @throws DataSourceException If the query to the back-end fails
     *
     * @see SaleAssociateRestlet#delegateResourceDeletion(PersistenceManager, Long)
     */
    protected void delegateResourceDeletion(PersistenceManager pm, Long demandKey, Long consumerKey, boolean stopRecursion) throws DataSourceException{
        // Delete consumer's demands
        demandOperations.deleteDemand(pm, demandKey, consumerKey);
        if (!stopRecursion) {
            // Clean-up the attached proposals
            List<Proposal> proposals = proposalOperations.getProposals(pm, Proposal.DEMAND_KEY, demandKey, 0);
            for (Proposal proposal: proposals) {
                proposal.setState(State.cancelled);
                proposal.setDemandKey(0L); // To cut the link
                proposalOperations.updateProposal(pm, proposal);
            }
        }
    }

    @Override
    protected JsonObject getResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser) throws DataSourceException {
        return demandOperations.getDemand(
                Long.valueOf(resourceId),
                isAPrivilegedUser(loggedUser) ? null : LoginServlet.getConsumerKey(loggedUser)
        ).toJson();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected JsonArray selectResources(JsonObject parameters, OpenIdUser loggedUser) throws DataSourceException, ClientException {
        JsonArray resources;
        String pointOfView = parameters.getString("pointOfView");
        boolean onlyKeys = "onlyKeys".equals(parameters.getString("detailLevel"));
        PersistenceManager pm = _baseOperations.getPersistenceManager();
        try {
            if ("SA".equals(pointOfView)) {
                Long saleAssociateKey = LoginServlet.getSaleAssociateKey(loggedUser, pm);
                if (saleAssociateKey == null) {
                    throw new ReservedOperationException(Action.list, Demand.class.getName());
                }
                Map<String, Object> queryDemands = new HashMap<String, Object>();
                queryDemands.put(Demand.SALE_ASSOCIATE_KEYS, saleAssociateKey);
                queryDemands.put(Command.STATE_COMMAND_LIST, Boolean.TRUE);
                if (parameters.containsKey(Entity.MODIFICATION_DATE)) {
                    try {
                        Date lastUpdate = DateUtils.isoToDate(parameters.getString(Entity.MODIFICATION_DATE));
                        queryDemands.put(">" + Entity.MODIFICATION_DATE, lastUpdate);
                    }
                    catch (ParseException e) {
                        // Ignored error, the date stays not set
                    }
                }
                if (onlyKeys) {
                    // Get the keys
                    resources = new GenericJsonArray((List) demandOperations.getDemandKeys(pm, queryDemands, 0));
                }
                else { // full detail
                    // Get the demands
                    resources = JsonUtils.toJson((List) demandOperations.getDemands(pm, queryDemands, 0));

                    pm.close();
                    pm = _baseOperations.getPersistenceManager();

                    // Get the keys of the proposals owned by the requester
                    Map<String, Object> queryProposals = new HashMap<String, Object>();
                    queryProposals.put(Command.OWNER_KEY, saleAssociateKey);
                    queryProposals.put(Command.STATE_COMMAND_LIST, Boolean.TRUE);
                    ArrayList validProposalKeys = new ArrayList(proposalOperations.getProposalKeys(pm, queryProposals, 0));

                    // Filter out information that don't belong to the requester
                    for (int i=0; i<resources.size(); i++) {
                        JsonObject demand = resources.getJsonObject(i);
                        // Clean-up the list of sale associate identifiers
                        demand.remove(Demand.SALE_ASSOCIATE_KEYS);
                        // Remove all proposal keys that don't belong to the requester or that are not in a valid state for the listing
                        JsonArray attachedProposalKeys = demand.getJsonArray(Demand.PROPOSAL_KEYS);
                        for (int j=0; j<(attachedProposalKeys == null ? 0 : attachedProposalKeys.size()); j++) {
                            if (!validProposalKeys.contains(attachedProposalKeys.getLong(j))) {
                                attachedProposalKeys.remove(j);
                                j--; // To account for the list size reduction
                            }
                        }
                    }
                }
            }
            else { // consumer point-of-view
                Long consumerKey = LoginServlet.getConsumerKey(loggedUser);
                if (onlyKeys) {
                    resources = new GenericJsonArray((List) demandOperations.getDemandKeys(pm, Demand.OWNER_KEY, consumerKey, 0));
                }
                else { // full detail
                    resources = JsonUtils.toJson((List) demandOperations.getDemands(pm, Demand.OWNER_KEY, consumerKey, 0));
                }
            }
        }
        finally {
            pm.close();
        }
        return resources;
    }

    @Override
    protected JsonObject updateResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser) throws DataSourceException {
        throw new RuntimeException("Not yet implemented!");
    }
}
