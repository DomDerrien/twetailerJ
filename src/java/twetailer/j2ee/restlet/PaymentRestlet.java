package twetailer.j2ee.restlet;

import java.util.logging.Logger;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.j2ee.BaseRestlet;
import twetailer.task.step.BaseSteps;

import com.dyuproject.openid.OpenIdUser;

import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

/**
 * Restlet entry point for the Payment entity control.
 *
 * @author Dom Derrien
 */
@SuppressWarnings("serial")
public class PaymentRestlet extends BaseRestlet {
    private static Logger log = Logger.getLogger(PaymentRestlet.class.getName());

    public Logger getLogger() { return log; }

    protected static SaleAssociateRestlet saleAssociateRestlet = new SaleAssociateRestlet();

    @Override
    protected JsonObject createResource(JsonObject parameters, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException, ClientException {
        throw new ClientException("Restricted access!");
    }

    @Override
    protected void deleteResource(String resourceId, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException, ClientException {
        throw new ClientException("Restricted access!");
    }

    @Override
    protected JsonObject getResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser, boolean isUserAdmin) throws InvalidIdentifierException {
        if (isUserAdmin) {
            return BaseSteps.getPaymentOperations().getPayment(Long.valueOf(resourceId)).toJson();
        }
        throw new RuntimeException("Not yet implemented!");
    }

    @Override
    protected JsonArray selectResources(JsonObject parameters, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException {
        throw new RuntimeException("Not yet implemented!");
    }

    @Override
    protected JsonObject updateResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser, boolean isUserAdmin) throws DataSourceException {
        throw new RuntimeException("Not yet implemented!");
    }
}
