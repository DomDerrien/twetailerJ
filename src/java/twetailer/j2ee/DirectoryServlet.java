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
import twetailer.dao.SaleAssociateOperations;
import twetailer.dao.SeedOperations;
import twetailer.dao.SettingsOperations;
import twetailer.dto.SaleAssociate;
import twetailer.dto.Seed;
import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

@SuppressWarnings("serial")
public class DirectoryServlet extends HttpServlet {
    private static Logger log = Logger.getLogger(MaezelServlet.class.getName());

    protected BaseOperations _baseOperations = new BaseOperations();
    protected SaleAssociateOperations saleAssociateOperations = _baseOperations.getSaleAssociateOperations();
    protected SeedOperations seedOperations = _baseOperations.getSeedOperations();
    protected SettingsOperations settingsOperations = _baseOperations.getSettingsOperations();

    /** Just made available for test purposes */
    protected static void setLogger(Logger mockLogger) {
        log = mockLogger;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String pathInfo = request.getPathInfo();
        log.warning("Path Info: " + pathInfo);

        String data = (String) settingsOperations.getFromCache("/suppliesTagCloud" + pathInfo);
        if (data == null || data.length() == 0) {
            PersistenceManager pm = _baseOperations.getPersistenceManager();
            try {
                Seed targetedSeed = seedOperations.getSeed(pm, pathInfo);
                log.warning("Retreived seed: " + targetedSeed.toJson().toString());

                JsonObject envelope = new GenericJsonObject();
                envelope.put("city_label", targetedSeed.getLabel());
                envelope.put("city_url", targetedSeed.buildQueryString());

                List<SaleAssociate> twetailerSaleReps = saleAssociateOperations.getSaleAssociates(pm, SaleAssociate.STORE_KEY, targetedSeed.getStoreKey(), 1);
                List<String> tags = twetailerSaleReps.get(0).getCriteria();
                envelope.put("city_tags", new GenericJsonArray(tags.toArray()));

                List<Seed> seeds = seedOperations.getAllSeeds(pm);
                JsonArray citiesNearby = new GenericJsonArray();
                for(Seed anySeed: seeds) {
                    citiesNearby.add(anySeed.toJson());
                }
                envelope.put("cities_nearby", citiesNearby);

                //
                // FIXME: be sure to escape JSON values!
                //
                MockOutputStream out = new MockOutputStream();
                envelope.toStream(out, false);
                data = out.getStream().toString();
                settingsOperations.setInCache("/suppliesTagCloud" + pathInfo, data);

                // Temporary injection
                seedOperations.createSeed(pm, new Seed("us", "ny", "new_york", "New York, NY, USA", 790L)); // 11 Wall Street, New York, NY 10005
                seedOperations.createSeed(pm, new Seed("us", "ca", "los_angeles", "Los Angeles, NY, USA", 790L)); // 11710 Telegraph Road, Santa Fe Springs, CA 90670
                seedOperations.createSeed(pm, new Seed("us", "il", "chicago", "Chicago, IL, USA", 790L)); // 6N001 Medinah road, Medinah, IL 60157
                // San Francisco, CA
                // Boston, MA
                // San Diego, CA
                // Houston, TX
                // Atlanta, Georgia
                // Washington, DC
                // Seatle, WA
                // Dallas-Forth Worth, TX
                // Las Vegas, Nevada
                // Philadelphia, Pennsylvania
                // Miami, Florida
                // Portand, Oregon
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
            finally {
                pm.close();
            }
        }

        try {
            String viewUrl = data == null || data.length() == 0 ? "/404.html" : "/jsp_includes/directory.jsp";
            request.getSession().setAttribute("data", data);
            request.getRequestDispatcher(viewUrl).forward(request, response);
        }
        catch (ServletException ex) {
            response.setStatus(500); // HTTP_ERROR
        }
    }
}
