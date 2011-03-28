package twetailer.dao;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.ObjectState;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.dto.Entity;
import twetailer.dto.Report;

/**
 * Controller defining various methods used for the CRUD operations on Report entities
 *
 * @author Dom Derrien
 */
public class ReportOperations extends BaseOperations {

    // Add entries for JabberId & TwitterId when these connectors start to be heavily used
    private static final CacheHandler<Report> cacheHandler = new CacheHandler<Report>(Report.class.getName(), new String[] { Entity.KEY });

    private static Report cacheReport(Report report) {
        return cacheHandler.cacheInstance(report);
    }

    private static Report decacheReport(Report report) {
        return cacheHandler.decacheInstance(report);
    }

    private static Report getCachedReport(Long key) {
        return cacheHandler.getCachedInstance(Report.KEY, key);
    }

    @SuppressWarnings("unused")
    private static List<Report> getCachedReports(String key, Object value) {
        Report report = cacheHandler.getCachedInstance(key, value);
        if (report != null) {
            List<Report> reports = new ArrayList<Report>();
            reports.add(report);
            return reports;
        }
        return null;
    }

    /**
     * Create the Report instance with the given parameters
     *
     * @param report Resource to persist
     * @return Just created resource
     *
     * @see ReportOperations#createReport(PersistenceManager, Reporter)
     */
    public Report createReport(Report report) {
        PersistenceManager pm = getPersistenceManager();
        try {
            return createReport(pm, report);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Create the Report instance with the given parameters
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param report Resource to persist
     * @return Just created resource
     */
    public Report createReport(PersistenceManager pm, Report report) {
        // Persist new report
        report = pm.makePersistent(report);
        // Cache the new instance
        cacheReport(report);
        return report;
    }

    /**
     * Use the given key to get the corresponding Report instance
     *
     * @param key Identifier of the Report instance
     * @return First Report instance matching the given filter or <code>null</code>
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid Report record
     *
     * @see ReportOperations#getReport(PersistenceManager, Long)
     */
    public Report getReport(Long key) throws InvalidIdentifierException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return getReport(pm, key);
        }
        finally {
            Boolean.TRUE.booleanValue(); // Stupid & harmless call to prevent false alarm from Cobertura & DataNucleus
            pm.close();
        }
    }

    /**
     * Use the given key to get the corresponding Report instance
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param key Identifier of the Report instance
     * @return First Report instance matching the given filter or <code>null</code>
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid Report record
     */
    public Report getReport(PersistenceManager pm, Long key) throws InvalidIdentifierException {
        return getReport(pm, key, true);
    }

    /**
     * Use the given key to get the corresponding Report instance
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param key Identifier of the Report instance
     * @param useCache If <code>true</code> the Report record might come from the cache, otherwise it's loaded from the data store
     * @return First Report instance matching the given filter or <code>null</code>
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid Report record
     */
    public Report getReport(PersistenceManager pm, Long key, boolean useCache) throws InvalidIdentifierException {
        if (key == null || key == 0L) {
            throw new InvalidIdentifierException("Invalid key; cannot retrieve the Report instance");
        }
        // Try to get a copy from the cache
        Report report = useCache ? getCachedReport(key) : null;
        if (report != null) {
            return report;
        }
        try {
            // Get it from the data store
            report = pm.getObjectById(Report.class, key);
            // Cache the instance
            if (useCache) {
                cacheReport(report);
            }
            return report;
        }
        catch(Exception ex) {
            throw new InvalidIdentifierException("Error while retrieving Report for identifier: " + key + " -- ex: " + ex.getMessage(), ex);
        }
    }

    /**
     * Get the identified Report instances while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param reportKeys list of Report instance identifiers
     * @return Collection of consumers matching the given filter
     *
     * @throws DataSourceException If given value cannot matched a data store type
     */
    @SuppressWarnings("unchecked")
    public List<Report> getReports(PersistenceManager pm, List<Long> reportKeys) throws DataSourceException {
        // Select the corresponding resources
        Query query = pm.newQuery(Report.class, ":p.contains(key)"); // Reported as being more efficient than pm.getObjectsById()
        try {
            // Copy the list
            List<Long> loadKeys = new ArrayList<Long>();
            loadKeys.addAll(reportKeys);
            // Get the report from the cache
            List<Report> reports = new ArrayList<Report>();
            for (Long key: reportKeys) {
                Report report = getCachedReport(key);
                if (report != null) {
                    reports.add(report);
                    loadKeys.remove(key);
                }
            }
            // Get remaining reports from the data store
            if (0 < loadKeys.size()) {
                List<Report> loadedReports = (List<Report>) query.execute(reportKeys);
                reports.addAll(loadedReports);
            }
            return reports;
        }
        finally {
            query.closeAll();
        }
    }

    /**
     * Persist the given (probably updated) resource
     *
     * @param report Resource to update
     * @return Updated resource
     *
     * @throws DataSourceException If the data management failed data store side
     *
     * @see ReportOperations#updateReport(PersistenceManager, Report)
     */
    public Report updateReport(Report report) throws DataSourceException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return updateReport(pm, report);
        }
        finally {
            Boolean.TRUE.booleanValue(); // Stupid & harmless call to prevent false alarm from Cobertura & DataNucleus
            pm.close();
        }
    }

    /**
     * Persist the given (probably updated) resource while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param report Resource to update
     * @return Updated resource
     *
     * @throws DataSourceException If the data management failed data store side
     */
    public Report updateReport(PersistenceManager pm, Report report) throws DataSourceException {
        ObjectState state = JDOHelper.getObjectState(report);
        if (ObjectState.TRANSIENT.equals(state)) {
            // Get a fresh user copy from the data store
            Report transientReport = report;
            try {
                report = getReport(pm, report.getKey(), false);
            }
            catch (InvalidIdentifierException ex) {
                throw new DataSourceException("Cannot retreive a fresh copy of the report key:" + report.getKey(), ex);
            }
            // Remove the previous copy from the cache
            decacheReport(transientReport); // To handle the possibility of an attribute used as a cache key being updated and leaving a wrong entry into the cache
            // Merge the attribute of the old copy into the fresh one
            report.fromJson(transientReport.toJson(), true, true);
        }
        // Persist updated report
        report = pm.makePersistent(report);
        // Cache the new instance
        cacheReport(report);
        return report;
    }

    protected final static String RECENT_REPORT_KEYS_ENTRY = "recentReportKeys";
    protected final static String STANDBY_REPORT_KEYS_ENTRY = "standbyReportKeys";

    /**
     * Add the given report identifier in a shared memory to ease its process by
     * the next cron job
     *
     * @param key Identifier of the report to track
     */
    @SuppressWarnings("unchecked")
    public void trackRecentReport(Long key) {
        List<Long> recentKeys = (List<Long>) CacheHandler.getFromCache(RECENT_REPORT_KEYS_ENTRY);
        if (recentKeys == null) {
            recentKeys = new ArrayList<Long>();
        }
        if (!recentKeys.contains(key)) {
            recentKeys.add(key);
            CacheHandler.setInCache(RECENT_REPORT_KEYS_ENTRY, recentKeys);
        }
    }

    /**
     * Return the list of reports that have not been updated since last call. Note
     * that recently updated reports are not immediately returned just in case it's
     * going to be updated again in the future...
     *
     * @return List of inactive reports
     */
    // recent:  1, 2, 3    2, 3, 4, 5    4, 5, 6, 7    7, 8          ---        ---
    // standby: ---        1, 2, 3       2, 3, 4, 5    4, 5, 6, 7    7, 8       ---
    // return:  ---        1             2, 3          4, 5, 6       7, 8       ---
    @SuppressWarnings("unchecked")
    public List<Long> getReadyReports() {
        List<Long> recentKeys = (List<Long>) CacheHandler.getFromCache(RECENT_REPORT_KEYS_ENTRY);
        List<Long> standbyKeys = (List<Long>) CacheHandler.getFromCache(STANDBY_REPORT_KEYS_ENTRY);
        List<Long> readyKeys = null;

        // No new report, so return all in standby
        if (recentKeys == null || recentKeys.size() == 0) {
            if (standbyKeys != null && 0 < standbyKeys.size()) {
                readyKeys = new ArrayList<Long>();
                readyKeys.addAll(standbyKeys);
                standbyKeys.clear();
                CacheHandler.setInCache(STANDBY_REPORT_KEYS_ENTRY, standbyKeys);
            }
        }
        // Put all new reports in standby, nothing ready
        else if (standbyKeys == null || standbyKeys.size() == 0) {
            if (standbyKeys == null) {
                standbyKeys = new ArrayList<Long>();
            }
            for (Long key: recentKeys) {
                // Don't add duplicates
                if (!standbyKeys.contains(key)) {
                    standbyKeys.add(key);
                }
            }
            recentKeys.clear();
            CacheHandler.setInCache(RECENT_REPORT_KEYS_ENTRY, recentKeys);
            CacheHandler.setInCache(STANDBY_REPORT_KEYS_ENTRY, standbyKeys);
        }
        // Extract as ready only the keys in standby but not recent, merge recent ones in standby, and clear recent
        else {
            readyKeys = new ArrayList<Long>();
            for(Long key: standbyKeys) {
                // Don't consider ready a report that has been recently updated
                if (!recentKeys.contains(key)) {
                    readyKeys.add(key);
                }
            }
            standbyKeys.removeAll(readyKeys);
            for (Long key: recentKeys) {
                // Don't add duplicates
                if (!standbyKeys.contains(key)) {
                    standbyKeys.add(key);
                }
            }
            recentKeys.clear();
            CacheHandler.setInCache(RECENT_REPORT_KEYS_ENTRY, recentKeys);
            CacheHandler.setInCache(STANDBY_REPORT_KEYS_ENTRY, standbyKeys);
        }
        return readyKeys;
    }
}
