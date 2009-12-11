package twetailer.j2ee.restlet;

import java.util.logging.Logger;

import twetailer.DataSourceException;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dto.Consumer;
import twetailer.j2ee.BaseRestlet;
import twetailer.j2ee.LoginServlet;

import com.dyuproject.openid.OpenIdUser;

import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonUtils;

@SuppressWarnings("serial")
public class ConsumerRestlet extends BaseRestlet {
    private static Logger log = Logger.getLogger(ConsumerRestlet.class.getName());

    protected BaseOperations _baseOperations = new BaseOperations();
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
    protected JsonObject createResource(JsonObject parameters, OpenIdUser loggedUser) throws DataSourceException {
        // Consumer instances are created automatically when users log in
        throw new RuntimeException("Not yet implemented!");
    }

    @Override
    protected void deleteResource(String resourceId, OpenIdUser loggedUser) throws DataSourceException {
        throw new RuntimeException("Not yet implemented!");
    }

    @Override
    protected JsonObject getResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser) throws DataSourceException {
        Consumer consumer = null;
        if ("current".equals(resourceId)) {
            consumer = consumerOperations.getConsumer((Long) loggedUser.getAttribute(LoginServlet.AUTHENTICATED_USER_TWETAILER_ID));
        }
        else {
            consumer = consumerOperations.getConsumer(Long.valueOf(resourceId));
        }
        if (consumer == null) {
            throw new DataSourceException("No Consumer resource matches the criteria");
        }
        return consumer.toJson();
    }

    @Override
    protected JsonArray selectResources(JsonObject parameters) throws DataSourceException{
        // Get search criteria
        String queryAttribute = parameters.getString("qA");
        String queryValue = parameters.getString("qV");
        if (queryAttribute == null || queryAttribute.length() == 0) {
            queryAttribute = Consumer.EMAIL;
            if (queryValue == null) {
                queryValue = parameters.getString("q");
            }
        } // FIXME: verify the specified attribute name belongs to a list of authorized attributes
        // Select and return the corresponding consumers
        return JsonUtils.toJson(consumerOperations.getConsumers(queryAttribute, queryValue, 0));
    }

    @Override
    protected JsonObject updateResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser) throws DataSourceException {
        throw new RuntimeException("Not yet implemented!");
    }
}
