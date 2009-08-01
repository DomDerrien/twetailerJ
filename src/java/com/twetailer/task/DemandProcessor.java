package com.twetailer.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.twetailer.DataSourceException;
import com.twetailer.dto.Demand;
import com.twetailer.dto.Retailer;
import com.twetailer.dto.Store;
import com.twetailer.j2ee.ConsumersServlet;
import com.twetailer.j2ee.DemandsServlet;
import com.twetailer.j2ee.RetailersServlet;
import com.twetailer.j2ee.SettingsServlet;
import com.twetailer.j2ee.StoresServlet;
import com.twetailer.validator.CommandSettings;
import com.twetailer.validator.LocaleValidator;

public class DemandProcessor {
    
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

    protected void process(Locale locale) throws DataSourceException {
        /*
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(Demand.STATE, CommandSettings.State.published);
        */
        List<Demand> demands = getDemandsServlet().getDemands(Demand.STATE, CommandSettings.State.published.toString(), 0);
        for(Demand demand: demands) {
            List<Retailer> retailers = identifyRetailers(demand);
        }
    }

    protected List<Retailer> identifyRetailers(Demand demand) {
        Double latitude = demand.getLatitude();
        Double longitude = demand.getLongitude();
        if (latitude < 0.0D || longitude < 0.0D) {
            LocaleValidator.getGeoCoordinates(demand);
        }
        List<Store> stores = null;
        if (true || latitude < 0.0D || longitude < 0.0D) {
            String postalCode = demand.getPostalCode();
            stores = getStoresServlet().getStores(Store.POSTAL_CODE, postalCode);
        }
        else {
            // FIXME: implements the lookups of stores for that geo-coordinates and in the expected distance
        }
        List<Retailer> crowd = new ArrayList<Retailer>();
        for (Store store: stores) {
            List<Retailer> retailers = getRetailersServlet().getRetailers(Retailer.STORE_KEY, store.getKey());
            crowd.addAll(retailers);
        }
        return crowd;
    }
}