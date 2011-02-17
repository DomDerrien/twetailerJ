package twetailer.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.datanucleus.store.appengine.query.JDOCursorHelper;

import twetailer.connector.BaseConnector.Source;
import twetailer.dto.Demand;
import twetailer.dto.Proposal;

import com.google.appengine.api.datastore.Cursor;

public class DataMigration {

    private static Logger log = Logger.getLogger(DataMigration.class.getName());

    /// Made available for test purposes
    public static void setMockLogger(Logger mockLogger) {
        log = mockLogger;
    }

    protected static Logger getLogger() {
        return log;
    }

    /**
     * Demonstrates how to use the cursor to be able to pass over an complete set of data
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param cursorString Representation of a cursor, to be used to get the next set of data to process
     * @param range Number of data to process at this particular stage
     * @param defaultSource Value to apply to any Demand instance without a default value
     * @return New representation of the cursor, ready for a next process call
     */
    @SuppressWarnings("unchecked")
    public static String updateSource(PersistenceManager pm, String cursorString, int range, Source defaultSource) {

        Query query = null;
        try {
            query = pm.newQuery(Demand.class);
            if (cursorString != null) {
                Map<String, Object> extensionMap = new HashMap<String, Object>();
                extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, Cursor.fromWebSafeString(cursorString));
                query.setExtensions(extensionMap);
            }
            query.setRange(0, range);
            List<Demand> results = (List<Demand>) query.execute();
            if (results.iterator().hasNext()) {
                for (Demand demand : results) {
                    // Initialize the new field if necessary. By checking first that the field is null, we allow this migration to be safely run multiple times.
                    if (demand.getSource() == null) {
                        demand.setSource(defaultSource);
                        pm.makePersistent(demand);
                    }
                }
                cursorString = JDOCursorHelper.getCursor(results).toWebSafeString();
            }
            else {
                // no results
                cursorString = null;
            }
        }
        finally {
            query.closeAll();
        }
        return cursorString;
    }

    @SuppressWarnings("unchecked")
    public static void migrateCriteriaToContent(PersistenceManager pm) {
        Query query = null;
        try {
            query = pm.newQuery(Demand.class);
            List<Demand> results = (List<Demand>) query.execute();
            for (Demand demand : results) {
                if (demand.getContent().length() == 0) {
                    List<String> criteria = demand.getOriginalCriteria();
                    if (criteria != null && 0 < criteria.size()) {
                        String content = "";
                        for (String tag: criteria) {
                            content += tag + " ";
                        }
                        demand.setContent(content.trim());
                        pm.makePersistent(demand);
                        getLogger().warning("Demand " + demand.getKey() + " -- transfer of: " + content);
                    }
                }
            }
        }
        finally {
            query.closeAll();
        }
        try {
            query = pm.newQuery(Proposal.class);
            List<Proposal> results = (List<Proposal>) query.execute();
            for (Proposal proposal : results) {
                if (proposal.getContent().length() == 0) {
                    List<String> criteria = proposal.getOriginalCriteria();
                    if (criteria != null && 0 < criteria.size()) {
                        String content = "";
                        for (String tag: criteria) {
                            content += tag + " ";
                        }
                        proposal.setContent(content.trim());
                        pm.makePersistent(proposal);
                        getLogger().warning("Proposal " + proposal.getKey() + " -- transfer of: " + content);
                    }
                }
            }
        }
        finally {
            query.closeAll();
        }
    }
}
