package twetailer.remote;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import twetailer.dto.SaleAssociate;
import twetailer.dto.Store;
import twetailer.remote.ConnectionUtils.SetupChoice;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.remoteapi.RemoteApiInstaller;

public class UpgradeStoreState {

    public static void main(String[] args) {

        try {
            System.out.println("Starting...");

            RemoteApiInstaller installer = ConnectionUtils.setup(SetupChoice.ANOTHERSOCIALECONOMY);
            System.out.println("Connection configured.");

            try {
                DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

                Query query = new Query(SaleAssociate.class.getSimpleName());
                query.addSort("creationDate", Query.SortDirection.DESCENDING);
                // query.setKeysOnly();
                // query.addFilter("dueDate", Query.FilterOperator.LESS_THAN, today);
                List<Long> keys = new ArrayList<Long>();
                List<Key> storeKeys = new ArrayList<Key>();
                Iterable<Entity> saleAssociates = ds.prepare(query).asIterable();
                System.out.println("Sale Associates retreived.");
                long saleAssociateNb = 0;
                for (Entity saleAssociate : saleAssociates) {
                    ++ saleAssociateNb;
                    System.out.println("  Processing sales associate: " + saleAssociate.getKey() + " / " + saleAssociate.getProperty(SaleAssociate.CONSUMER_KEY));
                    Long storeKey = (Long) saleAssociate.getProperty(SaleAssociate.STORE_KEY);
                    if (!keys.contains(storeKey)) {
                        keys.add(storeKey);
                        System.out.println("  Collecting store key: " + saleAssociate.getProperty(SaleAssociate.STORE_KEY));
                        storeKeys.add(KeyFactory.createKey(Store.class.getSimpleName(), storeKey));
                    }
                }
                System.out.println("Sale Associates processed: " + saleAssociateNb);

                List<Entity> updatedStores = new ArrayList<Entity>();
                Map<Key, Entity> stores = (Map<Key, Entity>) ds.get(storeKeys);
                for (Key storeKey : stores.keySet()) {
                    Entity store = stores.get(storeKey);
                    System.out.println("  Processing store: " + storeKey + " / " + store.getProperty(Store.NAME));
                    String state = (String) store.getProperty(Store.STATE);
                    if (state == null || state.equals(Store.State.referenced.toString())) {
                        store.setUnindexedProperty(Store.STATE, Store.State.inProgress.toString());
                        System.out.println("  Updating store state");
                        updatedStores.add(store);
                    }
                }
                System.out.println("Store updated: " + updatedStores.size());

                if (0 < updatedStores.size()) {
                    System.out.println("  Persisting stores in bulk");
                    ds.put(updatedStores);
                }
            }
            finally {
                installer.uninstall();
            }
            System.out.println("Finished.");
        }
        catch (IOException e) {
            System.out.println("Terminated.");
            e.printStackTrace();
        }
    }

}
