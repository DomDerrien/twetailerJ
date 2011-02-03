package twetailer.j2ee.restlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.ReservedOperationException;
import twetailer.dao.MockBaseOperations;
import twetailer.dao.PaymentOperations;
import twetailer.dto.Payment;
import twetailer.j2ee.MockLoginServlet;
import twetailer.task.step.BaseSteps;

import com.dyuproject.openid.OpenIdUser;

import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class TestPaymentRestlet {

    PaymentRestlet ops;
    static OpenIdUser user;

    @BeforeClass
    public static void setUpBeforeClass() {
    }

    @Before
    public void setUp() throws Exception {
        ops = new PaymentRestlet();
        user = MockLoginServlet.buildMockOpenIdUser();
        BaseSteps.resetOperationControllers(true);
        BaseSteps.setMockBaseOperations(new MockBaseOperations());
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetLogger() throws DataSourceException, ClientException {
        assertNotNull(ops.getLogger());
    }

    @Test(expected=ReservedOperationException.class)
    public void testGetResourceI() throws InvalidIdentifierException, ReservedOperationException {
        ops.getResource(new GenericJsonObject(), "not important!", user, false);
    }

    @Test
    public void testGetResourceII() throws InvalidIdentifierException, ReservedOperationException {
        final Long paymentKey = 54645434L;
        BaseSteps.setMockPaymentOperations(new PaymentOperations() {
            @Override
            public Payment getPayment(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(paymentKey, key);
                Payment resource = new Payment();
                resource.setKey(paymentKey);
                return resource;
            }
        });

        JsonObject resource = ops.getResource(new GenericJsonObject(), paymentKey.toString(), user, true);
        assertEquals(paymentKey.longValue(), resource.getLong(Payment.KEY));
    }

    @Test(expected=RuntimeException.class)
    public void testSelectResources() throws DataSourceException, ClientException {
        ops.selectResources(new GenericJsonObject(), user, false);
    }

    @Test(expected=RuntimeException.class)
    public void testCreateResource() throws DataSourceException, ClientException {
        ops.createResource(new GenericJsonObject(), user, false);
    }

    @Test(expected=RuntimeException.class)
    public void testUpdateResource() throws DataSourceException, ClientException {
        ops.updateResource(new GenericJsonObject(), "not important!", user, false);
    }

    @Test(expected=RuntimeException.class)
    public void testDeleteResource() throws DataSourceException, ClientException {
        ops.deleteResource("not important!", user, false);
    }
}
