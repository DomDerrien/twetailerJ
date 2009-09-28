package twetailer.task;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.DataSourceException;
import twetailer.connector.BaseConnector;
import twetailer.dao.BaseOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.RetailerOperations;
import twetailer.dao.StoreOperations;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.Retailer;
import twetailer.dto.Store;
import twetailer.validator.CommandSettings;
import domderrien.i18n.LabelExtractor;

public class DemandProcessor {

    private static final Logger log = Logger.getLogger(DemandProcessor.class.getName());

    protected static BaseOperations _baseOperations = new BaseOperations();
    protected static DemandOperations demandOperations = _baseOperations.getDemandOperations();
    protected static LocationOperations locationOperations = _baseOperations.getLocationOperations();
    protected static RetailerOperations retailerOperations = _baseOperations.getRetailerOperations();
    protected static StoreOperations storeOperations = _baseOperations.getStoreOperations();

    public static void process() throws DataSourceException {
        PersistenceManager pm = _baseOperations.getPersistenceManager();
        try {
            List<Demand> demands = demandOperations.getDemands(pm, Demand.STATE, CommandSettings.State.published.toString(), 0);
            for(Demand demand: demands) {
                try {
                    Location location = locationOperations.getLocation(pm, demand.getLocationKey());
                    List<Retailer> retailers = identifyRetailers(pm, demand, location);
                    // TODO: use the retailer score to ping the ones with highest score first.
                    for(Retailer retailer: retailers) {
                        StringBuilder tags = new StringBuilder();
                        for(String tag: demand.getCriteria()) {
                            tags.append(tag).append(" ");
                        }
                        BaseConnector.communicateToRetailer(
                                retailer.getPreferredConnection(),
                                retailer,
                                LabelExtractor.get(
                                        "dp_informNewDemand",
                                        new Object[] { demand.getKey(), tags, demand.getExpirationDate() },
                                        retailer.getLocale()
                                )
                        );
                    }
                }
                catch (DataSourceException ex) {
                    log.warning("Cannot get information retaled to demand: " + demand.getKey() + " -- ex: " + ex.getMessage());
                }
            }
        }
        finally {
            pm.close();
        }
    }

    protected static List<Retailer> identifyRetailers(PersistenceManager pm, Demand demand, Location location) throws DataSourceException {
        // Get the stores around the demanded location
        List<Location> locations = locationOperations.getLocations(pm, location, demand.getRange(), demand.getRangeUnit(), 0);
        List<Store> stores = storeOperations.getStores(pm, locations, 0);
        // Extracts all retailers
        List<Retailer> retailers = new ArrayList<Retailer>();
        for (Store store: stores) {
            List<Retailer> employees = retailerOperations.getRetailers(pm, Retailer.STORE_KEY, store.getKey(), 0);
            retailers.addAll(employees);
        }
        // Verifies that the retailers supply the demanded tags
        List<Retailer> selectedRetailers = new ArrayList<Retailer>();
        for (Retailer retailer: retailers) {
            long score = 0;
            for (String tag: demand.getCriteria()) {
                if (retailer.getCriteria() != null && retailer.getCriteria().contains(tag)) {
                    ++ score;
                    break; // TODO: check if it's useful to continue counting
                }
            }
            if (0 < score) {
                log.warning("Retailer " + retailer.getKey() + " selected for the demand: " + demand.getKey());
                retailer.setScore(score);
                selectedRetailers.add(retailer);
            }
        }
        return selectedRetailers;
    }
}
