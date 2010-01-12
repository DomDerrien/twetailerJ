package twetailer.j2ee.restlet;

import java.util.logging.Logger;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.j2ee.BaseRestlet;
import twetailer.j2ee.LoginServlet;

import com.dyuproject.openid.OpenIdUser;

import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

@SuppressWarnings("serial")
public class DemandRestlet extends BaseRestlet {
    private static Logger log = Logger.getLogger(DemandRestlet.class.getName());

    protected BaseOperations _baseOperations = new BaseOperations();
    protected DemandOperations demandOperations = _baseOperations.getDemandOperations();
    protected ConsumerOperations consumerOperations = _baseOperations.getConsumerOperations();

    // Setter for injection of a MockLogger at test time
    protected static void setLogger(Logger mock) {
        log = mock;
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    protected JsonObject createResource(JsonObject parameters, OpenIdUser loggedUser) throws DataSourceException, ClientException {
        return demandOperations.createDemand(parameters, (Long) loggedUser.getAttribute(LoginServlet.AUTHENTICATED_USER_TWETAILER_ID)).toJson();
    }

    @Override
    protected void deleteResource(String resourceId, OpenIdUser loggedUser) throws DataSourceException {
        throw new RuntimeException("Not yet implemented!");
    }

    @Override
    protected JsonObject getResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser) throws DataSourceException {
        throw new RuntimeException("Not yet implemented!");
    }

    @Override
    protected JsonArray selectResources(JsonObject parameters, OpenIdUser loggedUser) throws DataSourceException {
        throw new RuntimeException("Not yet implemented!");
    }

    @Override
    protected JsonObject updateResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser) throws DataSourceException {
        throw new RuntimeException("Not yet implemented!");
    }
}
