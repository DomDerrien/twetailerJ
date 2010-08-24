package twetailer.j2ee.restlet;

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

    protected static SaleAssociateRestlet saleAssociateRestlet = new SaleAssociateRestlet();

    @Override
    protected JsonObject createResource(JsonObject parameters, OpenIdUser loggedUser) throws DataSourceException, ClientException {
        throw new ClientException("Restricted access!");
    }

    @Override
    protected void deleteResource(String resourceId, OpenIdUser loggedUser) throws DataSourceException, ClientException {
        throw new ClientException("Restricted access!");
    }

    @Override
    protected JsonObject getResource(JsonObject parameters, String resourceId, OpenIdUser loggedUser) throws InvalidIdentifierException {
        if (isAPrivilegedUser(loggedUser)) {
            return BaseSteps.getPaymentOperations().getPayment(Long.valueOf(resourceId)).toJson();
        }
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
