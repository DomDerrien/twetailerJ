package twetailer.j2ee;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twetailer.validator.ApplicationSettings;
import twetailer.validator.LocaleValidator;

import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

@SuppressWarnings("serial")
public class DirectoryServlet extends HttpServlet {
    private static Logger log = Logger.getLogger(MaezelServlet.class.getName());

    /** Just made available for test purposes */
    protected static void setLogger(Logger mockLogger) {
        log = mockLogger;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String pathInfo = request.getPathInfo();
        log.warning("Path Info: " + pathInfo);

        String pageURL = request.getRequestURI() + (request.getQueryString() == null ? "" : "?" + request.getQueryString());
        log.warning("Page URI: " + pageURL);

        String data;
        String url;

        if ("/ca/qc/montreal".equals(pathInfo)) {
            //
            // FIXME: be sure to escape JSON values!
            //
            data =
                "{" +
                    "'city_label':'Montreal, Qc, Canada'," +
                    "'city_url':'" + pageURL + "'," +
                    "'city_tags': [" +
                        "'Audi'," +
                        "'BMW'," +
                        "'VW'," +
                        "'VolksWagen'," +
                        "'car'," +
                        "'occasion'," +
                        "'rent'," +
                        "'used'," +
                        "'voiture'," +
                        "'3D'," +
                        "'imprimante'," +
                        "'model'," +
                        "'modèle'," +
                        "'maquette'," +
                        "'printing'," +
                        "'prototype'" +
                     "]," +
                     "'cities_nearby':[" +
                         "{'url':'" + pageURL.replace(pathInfo, "/ca/on/toronto") + "', 'label':'Toronto, On, Canada'}," +
                         "{'url':'" + pageURL.replace(pathInfo, "/us/ny/newyork") + "', 'label':'New York, NY, Canada'}" +
                     "]" +
                "}";
            url = "/jsp_includes/directory.jsp";
        }
        else {
            data = "{}";
            url = "/404.html";
        }


        try {
            request.getSession().setAttribute("data", data);
            request.getRequestDispatcher(url).forward(request, response);
        }
        catch (ServletException ex) {
            response.setStatus(500); // HTTP_ERROR
        }
    }
}
