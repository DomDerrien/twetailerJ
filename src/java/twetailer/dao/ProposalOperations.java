package twetailer.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dto.Location;
import twetailer.dto.Proposal;
import twetailer.dto.SaleAssociate;
import twetailer.task.CommandProcessor;
import domderrien.jsontools.JsonObject;

public class ProposalOperations extends BaseOperations {
    private static Logger log = Logger.getLogger(ProposalOperations.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Create the Proposal instance with the given parameters
     *
     * @param parameters HTTP proposal parameters
     * @param saleAssociate Proposal owner
     * @return Just created resource
     *
     * @throws ClientException If the data given by the client are incorrect
     *
     * @see ProposalOperations#createProposal(Proposal)
     */
    public Proposal createProposal(JsonObject parameters, SaleAssociate saleAssociate) throws ClientException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return createProposal(pm, parameters, saleAssociate);
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
     * @param saleAssociate Proposal owner
     * @return Just created resource
     *
     * @throws ClientException If the data given by the client are incorrect
     *
     * @see ProposalOperations#createProposal(PersistenceManager, Proposal)
     */
    public Proposal createProposal(PersistenceManager pm, JsonObject parameters, SaleAssociate saleAssociate) throws ClientException {
        Long saleAssociateKey = saleAssociate.getKey();
        getLogger().warning("Create proposal for sale associate id: " + saleAssociateKey + " with: " + parameters.toString());
        // Creates new proposal record and persist it
        Proposal newProposal = new Proposal(parameters);
        // Updates the identifier of the creator owner
        Long ownerId = newProposal.getOwnerKey();
        if (ownerId == null || ownerId == 0L) {
            newProposal.setOwnerKey(saleAssociateKey);
        }
        else if (!saleAssociateKey.equals(ownerId)) {
            throw new ClientException("Mismatch of owner identifiers [" + ownerId + "/" + saleAssociateKey + "]");
        }
        // Save the store identifier
        newProposal.setStoreKey(saleAssociate.getStoreKey());
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
     * Use the given reference to get the corresponding Proposal instance for the identified sale associate
     *
     * @param key Identifier of the proposal
     * @param ownerKey Identifier of the proposal owner
     * @param storeKey Identifier of the proposal owner's store
     * @return First proposal matching the given criteria or <code>null</code>
     *
     * @throws DataSourceException If the retrieved proposal does not belong to the specified user
     *
     * @see ProposalOperations#getProposal(PersistenceManager, Long, Long)
     */
    public Proposal getProposal(Long key, Long ownerKey, Long storeKey) throws DataSourceException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return getProposal(pm, key, ownerKey, storeKey);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Use the given reference to get the corresponding Proposal instance for the identified sale associate while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param key Identifier of the proposal
     * @param ownerKey Identifier of the proposal owner
     * @param storeKey Identifier of the proposal owner's store
     * @return First proposal matching the given criteria or <code>null</code>
     *
     * @throws DataSourceException If the retrieved proposal does not belong to the specified user
     */
    public Proposal getProposal(PersistenceManager pm, Long key, Long ownerKey, Long storeKey) throws DataSourceException {
        if (key == null || key == 0L) {
            throw new IllegalArgumentException("Invalid key; cannot retrieve the Proposal instance");
        }
        getLogger().warning("Get Proposal instance with id: " + key);
        try {
            Proposal proposal = pm.getObjectById(Proposal.class, key);
            if (ownerKey != null && ownerKey != 0L && !ownerKey.equals(proposal.getOwnerKey()) && (storeKey == null || storeKey == 0L)) {
                throw new DataSourceException("Mismatch of owner identifiers [" + ownerKey + "/" + proposal.getOwnerKey() + "]");
            }
            if (storeKey != null && storeKey != 0L && !storeKey.equals(proposal.getStoreKey()) && (ownerKey == null || ownerKey == 0L)) {
                throw new DataSourceException("Mismatch of store identifiers [" + storeKey + "/" + proposal.getStoreKey() + "]");
            }
            proposal.getCriteria().size(); // FIXME: remove workaround for a bug in DataNucleus
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
     * Use the given pair {attribute; value} to get the corresponding Proposal identifiers while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param attribute Name of the proposal attribute used a the search criteria
     * @param value Pattern for the search attribute
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of proposal identifiers matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    @SuppressWarnings("unchecked")
    public List<Long> getProposalKeys(PersistenceManager pm, String attribute, Object value, int limit) throws DataSourceException {
        // Prepare the query
        Query queryObj = pm.newQuery("select " + Proposal.KEY + " from " + Proposal.class.getName());
        value = prepareQuery(queryObj, attribute, value, limit);
        getLogger().warning("Select proposal(s) with: " + queryObj.toString());
        // Select the corresponding resources
        List<Long> proposalKeys = (List<Long>) queryObj.execute(value);
        proposalKeys.size(); // FIXME: remove workaround for a bug in DataNucleus
        return proposalKeys;
    }

    /**
     * Use the given pairs {attribute; value} to get the corresponding Proposal instances while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param parameters Map of attributes and values to match
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of proposals matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    @SuppressWarnings("unchecked")
    public List<Proposal> getProposals(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
        // Prepare the query
        Query query = pm.newQuery(Proposal.class);
        Object[] values = prepareQuery(query, parameters, limit);
        getLogger().warning("Select proposal(s) with: " + query.toString());
        // Select the corresponding resources
        List<Proposal> proposals = (List<Proposal>) query.executeWithArray(values);
        proposals.size(); // FIXME: remove workaround for a bug in DataNucleus
        return proposals;
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Proposal instances
     *
     * @param locations list of locations where expected proposals should be retrieved
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of proposals matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data store type
     *
     * @see ProposalOperations#getProposals(PersistenceManager, String, Object)
     */
    public List<Proposal> getProposals(List<Location> locations, int limit) throws DataSourceException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return getProposals(pm, locations, limit);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Proposal instances while leaving the given persistence manager open for future updates
     *
     * Note that this command only return Proposal not canceled, not marked-for-deletion, not closed (see Proposal.stateCmdList attribute and Proposal.setState() method).
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param locations list of locations where expected proposals should be retrieved
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Collection of proposals matching the given criteria
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    public List<Proposal> getProposals(PersistenceManager pm, List<Location> locations, int limit) throws DataSourceException {
        List<Proposal> selection = new ArrayList<Proposal>();
        for (Location location: locations) {
            // Select the corresponding resources
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put(Proposal.LOCATION_KEY, location.getKey());
            parameters.put(Proposal.STATE_COMMAND_LIST, Boolean.TRUE);
            List<Proposal> proposals = CommandProcessor.proposalOperations.getProposals(pm, parameters, limit);
            // Copy into the list to be returned
            selection.addAll(proposals);
            if (limit != 0) {
                if (limit <= selection.size()) {
                    break;
                }
                limit = limit - selection.size();
            }
        }
        return selection;
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

    /**
     * Use the given pair {attribute; value} to get the corresponding Proposal instance and to delete it
     *
     * @param key Identifier of the proposal
     * @param ownerKey Identifier of the proposal owner
     *
     * @throws DataSourceException If the retrieved proposal does not belong to the specified user
     *
     * @see ProposalOperations#deleteProposal(PersistenceManager, Long, Long)
     */
    public void deleteProposal(Long key, Long ownerKey) throws DataSourceException {
        PersistenceManager pm = getPersistenceManager();
        try {
            deleteProposal(pm, key, ownerKey);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Use the given pair {attribute; value} to get the corresponding Proposal instance and to delete it
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param key Identifier of the proposal
     * @param ownerKey Identifier of the proposal owner
     *
     * @throws DataSourceException If the retrieved proposal does not belong to the specified user
     *
     * @see ProposalOperations#getProposals(PersistenceManager, Long, Long, Long)
     * @see ProposalOperations#deleteProposal(PersistenceManager, Proposal)
     */
    public void deleteProposal(PersistenceManager pm, Long key, Long ownerKey) throws DataSourceException {
        Proposal proposal = getProposal(pm, key, ownerKey, null);
        deleteProposal(pm, proposal);
    }

    /**
     * Delete the given proposal while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param key Identifier of the proposal
     */
    public void deleteProposal(PersistenceManager pm, Proposal proposal) {
        getLogger().warning("Delete proposal with id: " + proposal.getKey());
        pm.deletePersistent(proposal);
    }
}
