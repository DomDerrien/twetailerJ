package twetailer.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import twetailer.DataSourceException;

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

    private static PersistenceManagerFactory pmfInstance = null;

    /** Setter for the injection of a mock */
    public static void setPersistenceManagerFactory(PersistenceManagerFactory pmf) {
        pmfInstance = pmf;
    }

    /**
     * Singleton accessor
     *
     * @return Initial instance of the <code>PersistenceManagerFactory</code> class
     */
    public static PersistenceManagerFactory getPersistenceManagerFactory() {
        if (pmfInstance == null) {
            pmfInstance = JDOHelper.getPersistenceManagerFactory("transactions-optional");
        }
        return pmfInstance;
    }

    /**
     * Accessor isolated to facilitate tests by IOP
     *
     * @return Persistence manager instance
     */
    public PersistenceManager getPersistenceManager() {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        pm.setDetachAllOnCommit(true);
        pm.setCopyOnAttach(false);
        return pm;
    }

    /**
     * Prepare the declaration for the SQL query according to the value's class
     *
     * @param name parameter name
     * @param value parameter value
     * @return Array with the typed declaration and the updated value (updated when a conversion is required)
     *
     * @throws DataSourceException If the parameter class is not supported
     */
    private static Object[] prepareParameter(String name, Object value) throws DataSourceException {
        String declaration;
        if (value == null) {
            throw new DataSourceException("Cannot determine the class of an attribute with a null value");
        }
        if (value instanceof String) {
            declaration = "String " + name;
        }
        else if (value instanceof Long) {
            declaration = "Long " + name;
        }
        else if (value instanceof Integer) {
            declaration = "Long " + name;
            value = Long.valueOf((Integer) value);
        }
        else if (value instanceof Double) {
            declaration = "Double " + name;
        }
        else if (value instanceof Float) {
            declaration = "Double " + name;
            value = Double.valueOf((Float) value);
        }
        else if (value instanceof Date) {
            declaration = "java.util.Date " + name;
        }
        else if (value instanceof Boolean) {
            declaration = "Boolean " + name;
        }
        else {
            throw new DataSourceException("Unsupported criteria value type: " + value.getClass());
        }
        return new Object[] { declaration, value };
    }

    /**
     * Prepare the query for one attribute matching one value
     *
     * @param query Object to prepare
     * @param attribute Name of the demand attribute used a the search criteria
     * @param value Pattern for the search attribute
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Updated query
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    public static Object prepareQuery(Query query, String attribute, Object value, int limit) throws DataSourceException {
        query.setFilter(attribute + " == value");
        query.setOrdering("creationDate desc");
        if (0 < limit) {
            query.setRange(0, limit);
        }
        Object[] preparation = prepareParameter("value", value);
        query.declareParameters((String) preparation[0]);
        return preparation[1];
    }

    /**
     * Prepare the query for many attributes matching many values
     *
     * @param query Object to prepare
     * @param parameters Map of attributes and values to match
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Updated query
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    public static Object[] prepareQuery(Query query, Map<String, Object> parameters, int limit) throws DataSourceException {
        // Prepare the query
        StringBuilder filterDefinition = new StringBuilder();
        StringBuilder parameterDefinitions = new StringBuilder();
        List<Object> values = new ArrayList<Object>(parameters.size());
        String additionalOrder = null; // Just "creationDate desc" initially
        for(String parameterName: parameters.keySet()) {
            Object parameterValue = parameters.get(parameterName);
            char prefix = parameterName.charAt(0);
            if (prefix == '=' || prefix == '!') {
                parameterName = parameterName.substring(1);
            }
            else if (prefix == '<' || prefix == '>') {
                parameterName = parameterName.substring(1);
                if (additionalOrder != null && !additionalOrder.equals(parameterName)) {
                    throw new DataSourceException("App Engine only support queries with maximum comparisons on one field.");
                }
                additionalOrder = parameterName;
            }
            else {
                prefix = '=';
            }
            Object[] preparation = prepareParameter(parameterName + "Value", parameterValue);
            filterDefinition.append(
                    " && " +
                    parameterName +
                    (prefix == '=' ? " == " : prefix == '<' ? " < " : prefix == '>' ? " > " : " != ") +
                    parameterName + "Value"
            );
            parameterDefinitions.append(", ").append((String) preparation[0]);
            values.add(preparation[1]);
        }
        query.setOrdering(additionalOrder != null ? additionalOrder + " desc, creationDate desc" : "creationDate desc");
        query.setFilter(filterDefinition.substring(" && ".length()));
        query.declareParameters(parameterDefinitions.substring(", ".length()));
        if (0 < limit) {
            query.setRange(0, limit);
        }
        return values.toArray();
    }

    private ConsumerOperations _consumerOperations;

    /**
     * Factory for the ConsumerOperations instance
     * @return ConsumerOperations instance
     */
    public ConsumerOperations getConsumerOperations() {
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
    public DemandOperations getDemandOperations() {
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
    public LocationOperations getLocationOperations() {
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
    public ProductOperations getProductOperations() {
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
    public ProposalOperations getProposalOperations() {
        if (_proposalOperations == null) {
            _proposalOperations = new ProposalOperations();
        }
        return _proposalOperations;
    }

    private RawCommandOperations _rawCommandOperations;

    /**
     * Factory for the RawCommandOperations instance
     * @return RawCommandOperations instance
     */
    public RawCommandOperations getRawCommandOperations() {
        if (_rawCommandOperations == null) {
            _rawCommandOperations = new RawCommandOperations();
        }
        return _rawCommandOperations;
    }
    private RetailerOperations _retailerOperations;

    /**
     * Factory for the RetailerOperations instance
     * @return RetailerOperations instance
     */
    public RetailerOperations getRetailerOperations() {
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
    public SettingsOperations getSettingsOperations() {
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
    public StoreOperations getStoreOperations() {
        if (_storeOperations == null) {
            _storeOperations = new StoreOperations();
        }
        return _storeOperations;
    }
}
