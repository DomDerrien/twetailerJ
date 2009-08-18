package com.twetailer.rest;

import java.util.Date;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.twetailer.DataSourceException;
import com.twetailer.j2ee.Utils;

public class BaseOperations {
    private static final Logger _log = Logger.getLogger(BaseOperations.class.getName());

    /**
     * Get the logging handler
     * 
     * @return Reference on the local Logger instance
     */
    protected Logger getLogger() {
        return _log;
    }

    /**
     * Accessor isolated to facilitate tests by IOP
     * 
     * @return Persistence manager instance
     */
    public PersistenceManager getPersistenceManager() {
        PersistenceManager pm = Utils.getPersistenceManager();
        pm.setDetachAllOnCommit(true);
        pm.setCopyOnAttach(false);
        return pm;
    }
    
    /**
     * Prepare the query with the given parameters
     * 
     * @param query Object to prepare
     * @param attribute Name of the demand attribute used a the search criteria
     * @param value Pattern for the search attribute
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Updated query
     * 
     * @throws DataSourceException If given value cannot matched a data store type
     */
    public Query prepareQuery(Query query, String attribute, Object value, int limit) throws DataSourceException {
        query.setFilter(attribute + " == value");
        query.setOrdering("creationDate desc");
        if (value instanceof String) {
            query.declareParameters("String value");
        }
        else if (value instanceof Long) {
            query.declareParameters("Long value");
        }
        else if (value instanceof Integer) {
            query.declareParameters("Long value");
            value = Long.valueOf((Integer) value);
        }
        else if (value instanceof Date) {
            query.declareParameters("Date value");
        }
        else {
            throw new DataSourceException("Unsupported criteria value type: " + value.getClass());
        }
        if (0 < limit) {
            query.setRange(0, limit);
        }
        return query;
    }

    private ConsumerOperations _consumerOperations;

    /**
     * Factory for the ConsumerOperations instance
     * @return ConsumerOperations instance
     */
    public ConsumerOperations getConsumerOperation() {
        if (_consumerOperations == null) {
            _consumerOperations = new ConsumerOperations();
        }
        return _consumerOperations;
    }

    private DemandOperations _demandOperations;

    /**
     * Factory for the DemandOperations instance
     * @return DemandOperations instance
     */
    public DemandOperations getDemandOperation() {
        if (_demandOperations == null) {
            _demandOperations = new DemandOperations();
        }
        return _demandOperations;
    }

    private LocationOperations _locationOperations;

    /**
     * Factory for the LocationOperations instance
     * @return LocationOperations instance
     */
    public LocationOperations getLocationOperation() {
        if (_locationOperations == null) {
            _locationOperations = new LocationOperations();
        }
        return _locationOperations;
    }

    private ProductOperations _productOperations;

    /**
     * Factory for the ProductOperations instance
     * @return ProductOperations instance
     */
    public ProductOperations getProductOperation() {
        if (_productOperations == null) {
            _productOperations = new ProductOperations();
        }
        return _productOperations;
    }

    private ProposalOperations _proposalOperations;

    /**
     * Factory for the ProposalOperations instance
     * @return ProposalOperations instance
     */
    public ProposalOperations getProposalOperation() {
        if (_proposalOperations == null) {
            _proposalOperations = new ProposalOperations();
        }
        return _proposalOperations;
    }

    private RetailerOperations _retailerOperations;

    /**
     * Factory for the RetailerOperations instance
     * @return RetailerOperations instance
     */
    public RetailerOperations getRetailerOperation() {
        if (_retailerOperations == null) {
            _retailerOperations = new RetailerOperations();
        }
        return _retailerOperations;
    }

    private SettingsOperations _settingsOperations;

    /**
     * Factory for the SettingsOperations instance
     * @return SettingsOperations instance
     */
    public SettingsOperations getSettingsOperation() {
        if (_settingsOperations == null) {
            _settingsOperations = new SettingsOperations();
        }
        return _settingsOperations;
    }

    private StoreOperations _storeOperations;

    /**
     * Factory for the StoreOperations instance
     * @return StoreOperations instance
     */
    public StoreOperations getStoreOperation() {
        if (_storeOperations == null) {
            _storeOperations = new StoreOperations();
        }
        return _storeOperations;
    }
}