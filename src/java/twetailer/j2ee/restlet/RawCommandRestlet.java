package twetailer.j2ee.restlet;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.ReservedOperationException;
import twetailer.dao.BaseOperations;
import twetailer.dto.Entity;
import twetailer.j2ee.BaseRestlet;
import twetailer.task.step.BaseSteps;

import com.dyuproject.openid.OpenIdUser;

import domderrien.i18n.DateUtils;
import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonUtils;

/**
 * Restlet entry point for the Payment entity control.
 *
 * @author Dom Derrien
 */
@SuppressWarnings("serial")
public class RawCommandRestlet extends BaseRestlet {
    private static Logger log = Logger.getLogger(PaymentRestlet.class.getName());

    public Logger getLogger() { return log; }

    @Override
    protected JsonObject createResource(JsonObject parameters, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException, ClientException {
        throw new ReservedOperationException("Restricted access!");
    }

    @Override
    protected void deleteResource(String resourceId, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException, ClientException {
        throw new ReservedOperationException("Restricted access!");
    }

    @Override
    protected JsonObject getResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser, boolean isUserAdmin) throws InvalidIdentifierException, ReservedOperationException {
        if (isUserAdmin) {
            return BaseSteps.getRawCommandOperations().getRawCommand(Long.valueOf(resourceId)).toJson();
        }
        throw new ReservedOperationException("Restricted access!");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected JsonArray selectResources(JsonObject parameters, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException, ReservedOperationException {
        boolean onlyKeys = parameters.containsKey(BaseRestlet.ONLY_KEYS_PARAMETER_KEY);

        if (isUserAdmin && onlyKeys) {
            Date lastModificationDate = null;
            try {
                Map<String, Object> queryFilters = new HashMap<String, Object>();
                lastModificationDate = DateUtils.isoToDate(parameters.getString(Entity.CREATION_DATE));
                queryFilters.put(BaseOperations.FILTER_GREATER_THAN_OR_EQUAL_TO + Entity.CREATION_DATE, lastModificationDate);
                return new GenericJsonArray((List) BaseSteps.getRawCommandOperations().getRawCommandKeys(queryFilters, 0));
            }
            catch (ParseException ex) {
                ex.printStackTrace();
            }
        }
        throw new ReservedOperationException("Restricted access!");
    }

    @Override
    protected JsonObject updateResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException, ReservedOperationException {
        throw new ReservedOperationException("Restricted access!");
    }
}
