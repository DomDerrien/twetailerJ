package twetailer.j2ee.restlet;

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.ProposalOperations;
import twetailer.dto.Consumer;
import twetailer.dto.Proposal;
import twetailer.j2ee.BaseRestlet;
import twetailer.j2ee.LoginServlet;
import twetailer.validator.CommandSettings.State;

import com.dyuproject.openid.OpenIdUser;

import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

@SuppressWarnings("serial")
public class DemandRestlet extends BaseRestlet {
    private static Logger log = Logger.getLogger(DemandRestlet.class.getName());

    protected static BaseOperations _baseOperations = new BaseOperations();
    protected static DemandOperations demandOperations = _baseOperations.getDemandOperations();
    protected static ConsumerOperations consumerOperations = _baseOperations.getConsumerOperations();
    protected static ProposalOperations proposalOperations = _baseOperations.getProposalOperations();

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
        return demandOperations.createDemand(parameters, (Long) loggedUser.getAttribute(LoginServlet.AUTHENTICATED_USER_TWETAILER_ID)).toJson();
    }

    @Override
    protected void deleteResource(String resourceId, OpenIdUser loggedUser) throws DataSourceException, ClientException {
        if (isAPrivilegedUser(loggedUser)) {
            PersistenceManager pm = _baseOperations.getPersistenceManager();
            try {
                Long demandKey = Long.valueOf(resourceId);
                delegateResourceDeletion(pm, demandKey, (Long) loggedUser.getAttribute(LoginServlet.AUTHENTICATED_USER_TWETAILER_ID), false);
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
                isAPrivilegedUser(loggedUser) ? null : (Long) loggedUser.getAttribute(LoginServlet.AUTHENTICATED_USER_TWETAILER_ID)
        ).toJson();
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
