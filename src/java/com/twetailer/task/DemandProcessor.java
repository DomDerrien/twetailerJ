package com.twetailer.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import twitter4j.TwitterException;

import com.twetailer.DataSourceException;
import com.twetailer.adapter.TwitterAdapter;
import com.twetailer.dto.Demand;
import com.twetailer.dto.Retailer;
import com.twetailer.dto.Store;
import com.twetailer.j2ee.ConsumersServlet;
import com.twetailer.j2ee.DemandsServlet;
import com.twetailer.j2ee.RetailersServlet;
import com.twetailer.j2ee.SettingsServlet;
import com.twetailer.j2ee.StoresServlet;
import com.twetailer.validator.CommandSettings;

import domderrien.i18n.LabelExtractor;

public class DemandProcessor {
    
    private static final Logger log = Logger.getLogger(DemandProcessor.class.getName());

    private ConsumersServlet _consumersServlet;
    
    /**
     * Accessor provided for unit tests
     * @return Consumer servlet instance
     */
    public ConsumersServlet getConsumersServlet() {
        if (_consumersServlet == null) {
            _consumersServlet = new ConsumersServlet();
        }
        return _consumersServlet;
    }
    
    private DemandsServlet _demandsServlet;
    
    /**
     * Accessor provided for unit tests
     * @return Demand servlet instance
     */
    public DemandsServlet getDemandsServlet() {
        if (_demandsServlet == null) {
            _demandsServlet = new DemandsServlet();
        }
        return _demandsServlet;
    }
    
    private SettingsServlet _settingsServlet;
    
    /**
     * Accessor provided for unit tests
     * @return Settings servlet instance
     */
    public SettingsServlet getSettingsServlet() {
        if (_settingsServlet == null) {
            _settingsServlet = new SettingsServlet();
        }
        return _settingsServlet;
    }
    
    private StoresServlet _storesServlet;
    
    /**
     * Accessor provided for unit tests
     * @return Store servlet instance
     */
    public StoresServlet getStoresServlet() {
        if (_storesServlet == null) {
            _storesServlet = new StoresServlet();
        }
        return _storesServlet;
    }
    
    private RetailersServlet _retailersServlet;
    
    /**
     * Accessor provided for unit tests
     * @return Retailer servlet instance
     */
    public RetailersServlet getRetailersServlet() {
        if (_retailersServlet == null) {
            _retailersServlet = new RetailersServlet();
        }
        return _retailersServlet;
    }

    public void process(Locale locale) throws DataSourceException {
        List<Demand> demands = getDemandsServlet().getDemands(Demand.STATE, CommandSettings.State.published.toString(), 0);
        log.warning("0");
        log.warning("0 -- " + demands.size());
        for(Demand demand: demands) {
            log.warning("0 -- " + demand.getKey());
            List<Retailer> retailers = identifyRetailers(demand);
            for(Retailer retailer: retailers) {
                try {
                    log.warning("6");
                    log.warning("6 -- " + retailer.getTwitterId());
                    new TwitterAdapter().getTwitterAccount().sendDirectMessage(
                            retailer.getTwitterId().toString(),
                            LabelExtractor.get(
                                    "dp_informNewDemand",
                                    new Object[] { demand.getKey() },
                                    locale
                            )
                    );
                }
                catch(TwitterException ex) {
                    log.warning(
                            "Error while communication information about demand ref:" + demand.getKey() +
                            " to retailer " + retailer.getName() + " (store:" + retailer.getStoreKey() + ")"
                    );
                }
            }
        }
    }

    protected List<Retailer> identifyRetailers(Demand demand) throws DataSourceException {
        Double latitude = demand.getLatitude();
        Double longitude = demand.getLongitude();
        List<Store> stores = null;
        log.warning("1");
        if (Demand.INVALID_COORDINATE.equals(latitude) || Demand.INVALID_COORDINATE.equals(longitude)) {
            String postalCode = demand.getPostalCode();
            log.warning("2");
            stores = getStoresServlet().getStores(Store.POSTAL_CODE, postalCode, 0);
        }
        else {
            // FIXME: implements the lookups of stores for that geo-coordinates and in the expected distance
            log.warning("3");
            stores = new ArrayList<Store>();
        }
        List<Retailer> retailers = new ArrayList<Retailer>();
        for (Store store: stores) {
            log.warning("4");
            List<Retailer> storeEmployees = getRetailersServlet().getRetailers(Retailer.STORE_KEY, store.getKey(), 0);
            log.warning("4 -- " + storeEmployees.size());
            retailers.addAll(storeEmployees);
        }
        log.warning("5");
        log.warning("5 -- " + retailers.size());
        return retailers;
    }
}