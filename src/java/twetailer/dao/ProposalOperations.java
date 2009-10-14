package twetailer.dao;

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import domderrien.jsontools.JsonObject;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dto.Proposal;
import twetailer.dto.Retailer;

public class ProposalOperations extends BaseOperations {
    private static final Logger log = Logger.getLogger(ProposalOperations.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Create the Proposal instance with the given parameters
     *
     * @param parameters HTTP proposal parameters
     * @param retailer Proposal owner
     * @return Just created resource
     *
     * @throws ClientException If the data given by the client are incorrect
     *
     * @see ProposalOperations#createProposal(Proposal)
     */
    public Proposal createProposal(JsonObject parameters, Retailer retailer) throws ClientException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return createProposal(pm, parameters, retailer);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Create the Proposal instance with the given parameters
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param parameters HTTP proposal parameters
     * @param retailer Proposal owner
     * @return Just created resource
     *
     * @throws ClientException If the data given by the client are incorrect
     *
     * @see ProposalOperations#createProposal(PersistenceManager, Proposal)
     */
    public Proposal createProposal(PersistenceManager pm, JsonObject parameters, Retailer retailer) throws ClientException {
        Long retailerKey = retailer.getKey();
        getLogger().warning("Create proposal for retailer id: " + retailerKey + " with: " + parameters.toString());
        // Creates new proposal record and persist it
        Proposal newProposal = new Proposal(parameters);
        // Updates the identifier of the creator consumer
        Long consumerId = newProposal.getConsumerKey();
        if (consumerId == null || consumerId == 0L) {
            newProposal.setConsumerKey(retailerKey);
        }
        else if (!retailerKey.equals(consumerId)) {
            throw new ClientException("Mismatch of consumer identifiers [" + consumerId + "/" + retailerKey + "]");
        }
        // Save the store identifier
        newProposal.setStoreKey(retailer.getStoreKey());
        // Persist it
        return createProposal(pm, newProposal);
    }

    /**
     * Create the Proposal instance with the given parameters
     *
     * @param proposal Resource to persist
     * @return Just created resource
     */
    public Proposal createProposal(Proposal proposal) {
        PersistenceManager pm = getPersistenceManager();
        try {
            return createProposal(pm, proposal);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Create the Proposal instance with the given parameters
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param proposal Resource to persist
     * @return Just created resource
     */
    public Proposal createProposal(PersistenceManager pm, Proposal proposal) {
        pm.makePersistent(proposal);
        return proposal;
    }
    /**
     * Use the given reference to get the corresponding Proposal instance for the identified consumer
     *
     * @param key Identifier of the proposal
     * @param consumerKey Identifier of the demand owner
     * @param storeKey Identifier of the proposal owner's store
     * @return First proposal matching the given criteria or <code>null</code>
     *
     * @throws DataSourceException If the retrieved proposal does not belong to the specified user
     *
     * @see ProposalOperations#getProposal(PersistenceManager, Long, Long)
     */
    public Proposal getProposal(Long key, Long consumerKey, Long storeKey) throws DataSourceException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return getProposal(pm, key, consumerKey, storeKey);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Use the given reference to get the corresponding Proposal instance for the identified consumer while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param key Identifier of the proposal
     * @param consumerKey Identifier of the demand owner
     * @param storeKey Identifier of the proposal owner's store
     * @return First proposal matching the given criteria or <code>null</code>
     *
     * @throws DataSourceException If the retrieved proposal does not belong to the specified user
     */
    public Proposal getProposal(PersistenceManager pm, Long key, Long consumerKey, Long storeKey) throws DataSourceException {
        if (key == null || key == 0L) {
            throw new IllegalArgumentException("Invalid key; cannot retrieve the Proposal instance");
        }
        getLogger().warning("Get Proposal instance with id: " + key);
        try {
            Proposal proposal = pm.getObjectById(Proposal.class, key);
            if (consumerKey != null && consumerKey != 0L && !consumerKey.equals(proposal.getConsumerKey()) && (storeKey == null || storeKey == 0L)) {
                throw new DataSourceException("Mismatch of consumer identifiers [" + consumerKey + "/" + proposal.getConsumerKey() + "]");
            }
            if (storeKey != null && storeKey != 0L && !storeKey.equals(proposal.getStoreKey()) && (consumerKey == null || consumerKey == 0L)) {
                throw new DataSourceException("Mismatch of store identifiers [" + storeKey + "/" + proposal.getStoreKey() + "]");
            }
            if (proposal.getCriteria() != null) {
                proposal.getCriteria().size(); // FIXME: remove workaround for a bug in DataNucleus
            }
            return proposal;
        }
        catch(Exception ex) {
            throw new DataSourceException("Error while retrieving proposal for identifier: " + key + " -- ex: " + ex.getMessage(), ex);
        }
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Proposal instances
     *
     * @param attribute Name of the proposal attribute used a the search criteria
     * @param value Pattern for the search attribute
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of proposals matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data store type
     *
     * @see ProposalOperations#getProposals(PersistenceManager, String, Object)
     */
    public List<Proposal> getProposals(String attribute, Object value, int limit) throws DataSourceException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return getProposals(pm, attribute, value, limit);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Proposal instances while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param attribute Name of the proposal attribute used a the search criteria
     * @param value Pattern for the search attribute
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of proposals matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    @SuppressWarnings("unchecked")
    public List<Proposal> getProposals(PersistenceManager pm, String attribute, Object value, int limit) throws DataSourceException {
        // Prepare the query
        Query queryObj = pm.newQuery(Proposal.class);
        value = prepareQuery(queryObj, attribute, value, limit);
        getLogger().warning("Select proposal(s) with: " + queryObj.toString());
        // Select the corresponding resources
        List<Proposal> proposals = (List<Proposal>) queryObj.execute(value);
        proposals.size(); // FIXME: remove workaround for a bug in DataNucleus
        return proposals;
    }

    /**
     * Persist the given (probably updated) resource
     *
     * @param proposal Resource to update
     * @return Updated resource
     *
     * @see ProposalOperations#updateProposal(PersistenceManager, Proposal)
     */
    public Proposal updateProposal(Proposal proposal) {
        PersistenceManager pm = getPersistenceManager();
        try {
            // Persist updated proposal
            return updateProposal(pm, proposal);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Persist the given (probably updated) resource while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param proposal Resource to update
     * @return Updated resource
     */
    public Proposal updateProposal(PersistenceManager pm, Proposal proposal) {
        getLogger().warning("Updating proposal with id: " + proposal.getKey());
        pm.makePersistent(proposal);
        return proposal;
    }
}
