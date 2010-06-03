package twetailer.j2ee;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javamocks.io.MockOutputStream;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twetailer.dao.BaseOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dao.SeedOperations;
import twetailer.dao.SettingsOperations;
import twetailer.dto.Location;
import twetailer.dto.SaleAssociate;
import twetailer.dto.Seed;
import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

@SuppressWarnings("serial")
public class DirectoryServlet extends HttpServlet {
    private static Logger log = Logger.getLogger(MaezelServlet.class.getName());

    protected BaseOperations _baseOperations = new BaseOperations();
    protected LocationOperations locationOperations = _baseOperations.getLocationOperations();
    protected SaleAssociateOperations saleAssociateOperations = _baseOperations.getSaleAssociateOperations();
    protected SeedOperations seedOperations = _baseOperations.getSeedOperations();
    protected SettingsOperations settingsOperations = _baseOperations.getSettingsOperations();

    /** Just made available for test purposes */
    protected static void setLogger(Logger mockLogger) {
        log = mockLogger;
    }

    public final static String MEMCACHE_PREFIX = "/suppliesTagCloud";
    public final static String SEED_CITY_LIST_ID = "/seedCityList";
    public final static String QUERIED_CITY_ID = "/queriedCity";

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String pathInfo = request.getPathInfo();
        log.warning("Path Info: " + pathInfo);

        boolean bypassCache = Boolean.valueOf(request.getParameter("bypassMemCache"));
        log.warning("Bypass cache: " + bypassCache);

        String seedCityList = (String) settingsOperations.getFromCache(MEMCACHE_PREFIX + SEED_CITY_LIST_ID);
        if (seedCityList == null || seedCityList.length() == 0 || bypassCache) {
            PersistenceManager pm = _baseOperations.getPersistenceManager();
            try {
                // Get the seed cities
                List<Seed> seeds = seedOperations.getAllSeeds(pm);
                log.warning("Seed#: " + seeds.size());
                JsonArray citiesNearby = new GenericJsonArray();
                for(Seed anySeed: seeds) {
                    citiesNearby.add(anySeed.toJson());
                }

                // Serialize the list to keep it in MemCache
                // FIXME: be sure to escape JSON values!
                seedCityList = ((MockOutputStream) citiesNearby.toStream(new MockOutputStream(), false)).getStream().toString();
                settingsOperations.setInCache(MEMCACHE_PREFIX + SEED_CITY_LIST_ID, seedCityList);
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
            finally {
                pm.close();
            }
        }

        String queriedCity = (String) settingsOperations.getFromCache(MEMCACHE_PREFIX + pathInfo);
        if (queriedCity == null || queriedCity.length() == 0 || bypassCache) {
            PersistenceManager pm = _baseOperations.getPersistenceManager();
            try {
                Seed targetedSeed = seedOperations.getSeed(pm, pathInfo);
                log.warning("Retreived seed: " + targetedSeed.toJson().toString());

                JsonObject envelope = targetedSeed.toJson();

                List<SaleAssociate> twetailerSaleReps = saleAssociateOperations.getSaleAssociates(pm, SaleAssociate.STORE_KEY, targetedSeed.getStoreKey(), 1);
                List<String> tags = twetailerSaleReps == null || twetailerSaleReps.size() == 0 ? null : twetailerSaleReps.get(0).getCriteria();
                envelope.put(SaleAssociate.CRITERIA, tags == null || tags.size() == 0 ? new GenericJsonArray() : new GenericJsonArray(tags.toArray()));

                Location location = locationOperations.getLocation(pm, targetedSeed.getLocationKey());
                envelope.put(Location.POSTAL_CODE, location.getPostalCode());
                envelope.put(Location.COUNTRY_CODE, location.getCountryCode());

                // Serialize the list to keep it in MemCache
                // FIXME: be sure to escape JSON values!
                queriedCity = ((MockOutputStream) envelope.toStream(new MockOutputStream(), false)).getStream().toString();
                settingsOperations.setInCache(MEMCACHE_PREFIX + pathInfo, queriedCity);
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
            finally {
                pm.close();
            }
        }

        try {
            String viewUrl = queriedCity == null || queriedCity.length() == 0 ? "/404.html" : "/_includes/directory.jsp";
            request.getSession().setAttribute(QUERIED_CITY_ID, queriedCity);
            request.getSession().setAttribute(SEED_CITY_LIST_ID, seedCityList);
            request.getRequestDispatcher(viewUrl).forward(request, response);
        }
        catch (ServletException ex) {
            response.setStatus(500); // HTTP_ERROR
        }
    }
}
