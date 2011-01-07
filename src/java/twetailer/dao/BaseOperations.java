package twetailer.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import twetailer.DataSourceException;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;

/**
 * Base class defining shared methods for entity-based controllers
 *
 * @author Dom Derrien
 */
public class BaseOperations {

    private static PersistenceManagerFactory pmfInstance = null;

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
        return getPersistenceManagerHelper();
    }


    /**
     * Real getter of a PersistenceManager instance
     *
     * @return Persistence manager instance
     */
    public static PersistenceManager getPersistenceManagerHelper() {
        PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
        pm.setDetachAllOnCommit(true);
        pm.setCopyOnAttach(false);
        return pm;
    }

    /**
     * Accessor isolated to facilitate tests by IOP
     *
     * @return Queue where to post asynchronous tasks
     */
    public Queue getQueue() {
        return QueueFactory.getDefaultQueue();
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
            throw new DataSourceException("Cannot determine the class of the attribute " + name + " with a null value");
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
     * @return Value for the query submission
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    public static Object prepareQuery(Query query, String attribute, Object value, int limit) throws DataSourceException {
        query.setFilter(attribute + " == attributeValue");
        // -- begin --
        // Note: The Java datastore interface does not support the != and IN filter operators that are implemented in the Python datastore interface.
        // (In the Python interface, these operators are implemented in the client-side libraries as multiple datastore queries; they are not features of the datastore itself.)
        // More details at: http://code.google.com/appengine/docs/java/datastore/queriesandindexes.html
        //
        // if (!attribute.equals(Command.STATE)) {
        //     query.setFilter(" && " + Command.STATE + " != \"" + State.markedForDeletion.toString() + "\"");
        // }
        // -- end --
        query.setOrdering("creationDate desc");
        if (0 < limit) {
            query.setRange(0, limit);
        }
        Object[] preparation = prepareParameter("attributeValue", value);
        query.declareParameters((String) preparation[0]);
        return preparation[1];
    }

    public static final char FILTER_EQUAL_TO = '=';
    public static final char FILTER_NOT_EQUAL_TO = '!';
    public static final char FILTER_LESS_THAN_OR_EQUAL_TO = '[';
    public static final char FILTER_GREATER_THAN_OR_EQUAL_TO = ']';
    public static final char FILTER_LESS_THAN = '<';
    public static final char FILTER_GREATER_THAN = '>';
    public static final char FILTER_STARTS_WITH = '*';

    /**
     * Prepare the query for many attributes matching many values
     *
     * @param query Object to prepare
     * @param parameters Map of attributes and values to match
     * @param limit Maximum number of expected results, with 0 means the system will use its default limit
     * @return Array of values for the query submission
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    public static Object[] prepareQuery(Query query, Map<String, Object> parameters, int limit) throws DataSourceException {
        // Prepare the query
        StringBuilder filterDefinition = new StringBuilder();
        StringBuilder parameterDefinitions = new StringBuilder();
        List<Object> values = new ArrayList<Object>(parameters.size());
        String additionalOrder = null; // Just "creationDate desc" initially
        int parameterIdx = 0;
        for(String parameterName: parameters.keySet()) {
            Object parameterValue = parameters.get(parameterName);
            char prefix = parameterName.charAt(0);
            if (prefix == FILTER_EQUAL_TO || prefix == FILTER_NOT_EQUAL_TO) {
                parameterName = parameterName.substring(1);
            }
            else if (prefix == FILTER_LESS_THAN || prefix == FILTER_GREATER_THAN || prefix == FILTER_LESS_THAN_OR_EQUAL_TO || prefix == FILTER_GREATER_THAN_OR_EQUAL_TO || prefix == FILTER_STARTS_WITH) {
                parameterName = parameterName.substring(1);
                if (additionalOrder != null && !additionalOrder.equals(parameterName)) {
                    throw new DataSourceException("App Engine only support queries with maximum comparisons on one field.");
                }
                additionalOrder = parameterName;
            }
            else {
                prefix = FILTER_EQUAL_TO;
            }
            Object[] preparation = prepareParameter(parameterName + "Value" + parameterIdx, parameterValue);
            filterDefinition.append(" && ");
            if (prefix == FILTER_STARTS_WITH) {
                filterDefinition.
                    append(parameterName).
                    append(".startsWith(").
                    append(parameterName).append("Value").append(parameterIdx).
                    append(")");
            }
            else {
                filterDefinition.
                    append(parameterName).
                    append(prefix == FILTER_EQUAL_TO ? " == " : prefix == FILTER_LESS_THAN ? " < " : prefix == FILTER_GREATER_THAN ? " > " : prefix == FILTER_LESS_THAN_OR_EQUAL_TO ? " <= " : prefix == FILTER_GREATER_THAN_OR_EQUAL_TO ? " >= " : " != ").
                    append(parameterName).append("Value").append(parameterIdx);
            }
            parameterDefinitions.append(", ").append((String) preparation[0]);
            values.add(preparation[1]);
            parameterIdx ++;
        }
        query.setOrdering(additionalOrder != null ? additionalOrder + " desc, creationDate desc" : "creationDate desc");
        query.setFilter(filterDefinition.length() == 0 ? null : filterDefinition.substring(" && ".length()));
        query.declareParameters(parameterDefinitions.length() == 0 ? null : parameterDefinitions.substring(", ".length()));
        if (0 < limit) {
            query.setRange(0, limit);
        }

//        System.err.println("**** Query: " + query.toString());
//        System.err.print("**** Query parameters: ");
//        for(int jdx = 0; jdx < values.size(); jdx++) {
//            System.err.print("{" + jdx + ": " + values.get(jdx) + "} ");
//        }
//        System.err.println();

        return values.toArray();
    }
}
