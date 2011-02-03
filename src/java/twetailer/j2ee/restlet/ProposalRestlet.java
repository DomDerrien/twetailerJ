package twetailer.j2ee.restlet;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.ReservedOperationException;
import twetailer.connector.BaseConnector.Source;
import twetailer.dto.Command;
import twetailer.dto.Location;
import twetailer.dto.Proposal;
import twetailer.dto.Store;
import twetailer.dto.Command.QueryPointOfView;
import twetailer.j2ee.BaseRestlet;
import twetailer.j2ee.LoginServlet;
import twetailer.task.step.BaseSteps;
import twetailer.task.step.ProposalSteps;

import com.dyuproject.openid.OpenIdUser;

import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.GenericJsonObject;
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
    private static Logger log = Logger.getLogger(ProposalRestlet.class.getName());

    public Logger getLogger() { return log; }

    @Override
    protected JsonObject getResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException, ClientException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            QueryPointOfView pointOfView = QueryPointOfView.fromJson(parameters, QueryPointOfView.SALE_ASSOCIATE);
            Long ownerKey, saleAssociateKey = null, storeKey = null;
            if (isUserAdmin) {
                if (QueryPointOfView.CONSUMER.equals(pointOfView) && !parameters.containsKey(BaseRestlet.ON_BEHALF_CONSUMER_KEY) ||
                    QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView) && !parameters.containsKey(BaseRestlet.ON_BEHALF_ASSOCIATE_KEY)
                ) {
                    throw new IllegalArgumentException("Missing one of the identity identifiers!");
                }
                ownerKey = parameters.getLong(BaseRestlet.ON_BEHALF_CONSUMER_KEY);
                saleAssociateKey = QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView) ? parameters.getLong(BaseRestlet.ON_BEHALF_ASSOCIATE_KEY) : null;
            }
            else {
                ownerKey = LoginServlet.getConsumerKey(loggedUser);
                if (QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView)) {
                    saleAssociateKey = LoginServlet.getSaleAssociateKey(loggedUser, pm);
                    storeKey = LoginServlet.getStoreKey(loggedUser, pm);
                }
            }
            Long proposalKey = Long.valueOf(resourceId);
            Proposal proposal = ProposalSteps.getProposal(pm, proposalKey, ownerKey, pointOfView, saleAssociateKey, storeKey);

            JsonObject out = isUserAdmin ? proposal.toJson() : ProposalSteps.anonymizeProposal(pointOfView, proposal.toJson());

            if (parameters.containsKey(RELATED_RESOURCES_ENTRY_POINT_KEY)) {
                JsonArray relatedResourceNames = parameters.getJsonArray(RELATED_RESOURCES_ENTRY_POINT_KEY);
                JsonObject relatedResources = new GenericJsonObject();
                int idx = relatedResourceNames.size();
                while (0 < idx) {
                    --idx;
                    String relatedResourceName = relatedResourceNames.getString(idx);
                    if (Location.class.getName().contains(relatedResourceName)) {
                        Location location = BaseSteps.getLocationOperations().getLocation(pm, proposal.getLocationKey());
                        relatedResources.put(relatedResourceName, location.toJson());
                    }
                    else if (Store.class.getName().contains(relatedResourceName)) {
                        Store store = BaseSteps.getStoreOperations().getStore(pm, proposal.getStoreKey());
                        relatedResources.put(relatedResourceName, store.toJson());
                    }
                }
                if (0 < relatedResources.size()) {
                    out.put(RELATED_RESOURCES_ENTRY_POINT_KEY, relatedResources);
                }
            }

            return out;
        }
        finally {
            pm.close();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected JsonArray selectResources(JsonObject parameters, OpenIdUser loggedUser, boolean isUserAdmin) throws InvalidIdentifierException, DataSourceException, ReservedOperationException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            QueryPointOfView pointOfView = QueryPointOfView.fromJson(parameters, QueryPointOfView.SALE_ASSOCIATE);
            Long ownerKey, saleAssociateKey;
            if (isUserAdmin) {
                if (QueryPointOfView.CONSUMER.equals(pointOfView) && !parameters.containsKey(BaseRestlet.ON_BEHALF_CONSUMER_KEY) ||
                    QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView) && !parameters.containsKey(BaseRestlet.ON_BEHALF_ASSOCIATE_KEY)
                ) {
                    throw new IllegalArgumentException("Missing one of the identity identifiers!");
                }
                ownerKey = parameters.getLong(BaseRestlet.ON_BEHALF_CONSUMER_KEY);
                saleAssociateKey = QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView) ? parameters.getLong(BaseRestlet.ON_BEHALF_ASSOCIATE_KEY) : null;
            }
            else {
                ownerKey = LoginServlet.getConsumerKey(loggedUser);
                saleAssociateKey = QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView) ? LoginServlet.getSaleAssociateKey(loggedUser, pm) : null;
            }
            boolean onlyKeys = parameters.containsKey(BaseRestlet.ONLY_KEYS_PARAMETER_KEY);

            JsonArray resources;
            if (onlyKeys) {
                // Get the keys
                resources = new GenericJsonArray((List) ProposalSteps.getProposalKeys(pm, parameters, ownerKey, pointOfView, saleAssociateKey));
            }
            else { // full detail
                // Get the demands
                List<Proposal> proposals = ProposalSteps.getProposals(pm, parameters, ownerKey, pointOfView, saleAssociateKey);
                resources = isUserAdmin ? JsonUtils.toJson(proposals) : ProposalSteps.anonymizeProposals(pointOfView, JsonUtils.toJson(proposals));

                if (parameters.containsKey(RELATED_RESOURCES_ENTRY_POINT_KEY) && 0 < proposals.size()) {
                    JsonArray relatedResourceNames = parameters.getJsonArray(RELATED_RESOURCES_ENTRY_POINT_KEY);
                    JsonObject relatedResources = new GenericJsonObject();
                    int idx = relatedResourceNames.size();
                    while (0 < idx) {
                        --idx;
                        String relatedResourceName = relatedResourceNames.getString(idx);
                        if (Location.class.getName().contains(relatedResourceName)) {
                            List<Long> locationKeys = new ArrayList<Long>();
                            for(int i=0; i<proposals.size(); i++) {
                                Long locationKey = proposals.get(i).getLocationKey();
                                if (!locationKeys.contains(locationKey)) {
                                    locationKeys.add(locationKey);
                                }
                            }
                            List<Location> locations = BaseSteps.getLocationOperations().getLocations(pm, locationKeys);
                            relatedResources.put(relatedResourceName, JsonUtils.toJson(locations));
                        }
                        else if (Store.class.getName().contains(relatedResourceName)) {
                            List<Long> storeKeys = new ArrayList<Long>();
                            for(int i=0; i<proposals.size(); i++) {
                                Long storeKey = proposals.get(i).getStoreKey();
                                if (!storeKeys.contains(storeKey)) {
                                    storeKeys.add(storeKey);
                                }
                            }
                            List<Store> stores = BaseSteps.getStoreOperations().getStores(pm, storeKeys);
                            relatedResources.put(relatedResourceName, JsonUtils.toJson(stores));
                        }
                    }
                    if (0 < relatedResources.size()) {
                        resources.getJsonObject(0).put(RELATED_RESOURCES_ENTRY_POINT_KEY, relatedResources);
                    }
                }
            }
            return resources;
        }
        finally {
            pm.close();
        }
    }

    @Override
    protected JsonObject createResource(JsonObject parameters, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException, ClientException {
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
    protected JsonObject updateResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException, ClientException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            // Get the demand owner key
            Long associateKey = null, consumerKey = null;
            if (isUserAdmin) {
                if (parameters.containsKey(BaseRestlet.ON_BEHALF_CONSUMER_KEY)) {
                    consumerKey = parameters.getLong(BaseRestlet.ON_BEHALF_CONSUMER_KEY);
                }
                if (parameters.containsKey(BaseRestlet.ON_BEHALF_ASSOCIATE_KEY)) {
                    associateKey = parameters.getLong(BaseRestlet.ON_BEHALF_ASSOCIATE_KEY);
                }
            }
            else {
                consumerKey = LoginServlet.getConsumerKey(loggedUser);
                associateKey = LoginServlet.getSaleAssociateKey(loggedUser);
            }

            // Update the proposal
            Proposal proposal = null;
            Long proposalKey = Long.valueOf(resourceId);
            QueryPointOfView pointOfView = QueryPointOfView.fromJson(parameters, QueryPointOfView.SALE_ASSOCIATE);
            if (QueryPointOfView.CONSUMER.equals(pointOfView)) {
                proposal = ProposalSteps.updateProposal(pm, null, proposalKey, parameters, consumerKey);
            }
            else {
                proposal = ProposalSteps.updateProposal(pm, null, proposalKey, parameters, associateKey, consumerKey, isUserAdmin);
            }

            return proposal.toJson();
        }
        finally {
            pm.close();
        }
    }

    @Override
    protected void deleteResource(String resourceId, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException, ClientException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            Long proposalKey = Long.valueOf(resourceId);
            ProposalSteps.deleteProposal(pm, proposalKey, LoginServlet.getSaleAssociateKey(loggedUser, pm), LoginServlet.getConsumerKey(loggedUser));
        }
        finally {
            pm.close();
        }
    }
}
