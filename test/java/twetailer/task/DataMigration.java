package twetailer.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.datanucleus.store.appengine.query.JDOCursorHelper;

import twetailer.dto.Demand;
import twetailer.task.step.BaseSteps;

import com.google.appengine.api.datastore.Cursor;

public class DataMigration {

    @SuppressWarnings("unchecked")
    public String migrate_up(String cursorString, int range) {

        Query query = null;
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            query = pm.newQuery(Demand.class);
            if (cursorString != null) {
                Cursor cursor = Cursor.fromWebSafeString(cursorString);
                Map<String, Object> extensionMap = new HashMap<String, Object>();
                extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
                query.setExtensions(extensionMap);
            }
            query.setRange(0, range);
            List<Demand> results = (List<Demand>) query.execute();
            if (results.iterator().hasNext()) {
                for (Demand friend : results) {
                    // initialize the new field if necessary
                    // By checking first that the field is null, we allow this migration
                    // to be safely run multiple times.
                    if (friend.getCriteria() == null) {
                        friend.setCriteria(new ArrayList<String>());
                    }
                }
                Cursor cursor = JDOCursorHelper.getCursor(results);
                cursorString = cursor.toWebSafeString();
            }
            else {
                // no results
                cursorString = null;
            }
        }
        finally {
            query.closeAll();
            pm.close();
        }
        return cursorString;
    }
}
