package twetailer.payment;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.DataSourceException;
import twetailer.dto.Payment;
import twetailer.j2ee.MaezelServlet;

import com.amazonaws.fps.AmazonFPSClient;
import com.amazonaws.fps.AmazonFPSException;
import com.amazonaws.fps.model.CurrencyCode;
import com.amazonaws.fps.model.PayRequest;
import com.amazonaws.fps.model.PayResponse;
import com.amazonaws.fps.model.PayResult;
import com.amazonaws.fps.model.ResponseMetadata;
import com.amazonaws.fps.model.TransactionStatus;
import com.amazonaws.ipnreturnurlvalidation.SignatureUtilsForOutbound;

public class TestAmazonFPS {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetCoBrandedServiceUrl() throws MalformedURLException, SignatureException, UnsupportedEncodingException {
        final Long consumerKey = 11L;
        final Long demandKey = 22L;
        final Long proposalKey = 33L;
        final String reference = Payment.getReference(consumerKey, demandKey, proposalKey);

        final String description = "test";
        final Double total = 25.95D;
        final String currency = "USD"; // FIXME: put a static definition in LocaleValidator

        String url = new AmazonFPS().getCoBrandedServiceUrl(reference, description, total, currency);

        assertTrue(url.contains(reference));
        assertTrue(url.contains(description));
        assertTrue(url.contains(total.toString()));
        assertTrue(url.contains(currency));

        assertTrue(url.contains(URLEncoder.encode(MaezelServlet.getCoBrandedServiceEndPointURL(), "UTF8")));

    }

    @Test
    public void testGetAmazonSignatureVerifier() {
        assertNotNull(new AmazonFPS().getAmazonSignatureVerifier());
    }

    @Test(expected=NullPointerException.class)
    public void testVerifyCoBrandedServiceResponseI() throws SignatureException {
        assertTrue(new AmazonFPS().verifyCoBrandedServiceResponse(null));
    }

    @Test
    public void testVerifyCoBrandedServiceResponseII() throws SignatureException {
        AmazonFPS instance = new AmazonFPS() {
            @Override
            protected SignatureUtilsForOutbound getAmazonSignatureVerifier() {
                return new SignatureUtilsForOutbound() {
                    @Override
                    public boolean validateRequest(Map<String, String> parameters, String urlEndPoint, String httpMethod) throws SignatureException  {
                        assertTrue(parameters.containsKey("a"));
                        assertEquals("b", parameters.get("a"));
                        return true;
                    }
                };
            }
        };

        // Normal map
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("a", "b");

        assertTrue(instance.verifyCoBrandedServiceResponse(parameters));
    }

    @Test
    public void testVerifyCoBrandedServiceResponseIII() throws SignatureException {
        AmazonFPS instance = new AmazonFPS() {
            @Override
            protected SignatureUtilsForOutbound getAmazonSignatureVerifier() {
                return new SignatureUtilsForOutbound() {
                    @Override
                    public boolean validateRequest(Map<String, String> parameters, String urlEndPoint, String httpMethod) throws SignatureException  {
                        assertTrue(parameters.containsKey("a"));
                        assertEquals("b", parameters.get("a"));
                        return true;
                    }
                };
            }
        };

        // Map as generated for by the HttpServletRequest#getParameterMap
        Map<String, String[]> parameters = new HashMap<String, String[]>();
        parameters.put("a", new String[] { "b" });

        assertTrue(instance.verifyCoBrandedServiceResponse(parameters));
    }

    @Test
    public void testVerifyCoBrandedServiceResponseIV() throws SignatureException {
        AmazonFPS instance = new AmazonFPS() {
            @Override
            protected SignatureUtilsForOutbound getAmazonSignatureVerifier() {
                return new SignatureUtilsForOutbound() {
                    @Override
                    public boolean validateRequest(Map<String, String> parameters, String urlEndPoint, String httpMethod) throws SignatureException  {
                        assertTrue(parameters.containsKey("a"));
                        assertEquals("b", parameters.get("a"));
                        return true;
                    }
                };
            }
        };

        // Unexpected map but play nicely
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("a", new Object() {
            @Override
            public String toString() {
                return "b";
            }
        });

        assertTrue(instance.verifyCoBrandedServiceResponse(parameters));
    }

    @Test
    public void testVerifyCoBrandedServiceResponseV() throws SignatureException {
        AmazonFPS instance = new AmazonFPS() {
            @Override
            protected SignatureUtilsForOutbound getAmazonSignatureVerifier() {
                return new SignatureUtilsForOutbound() {
                    @Override
                    public boolean validateRequest(Map<String, String> parameters, String urlEndPoint, String httpMethod) throws SignatureException  {
                        assertTrue(parameters.containsKey("a"));
                        assertNull(parameters.get("a"));
                        return true;
                    }
                };
            }
        };

        // Map with <code>null</code> value
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("a", null);

        assertTrue(instance.verifyCoBrandedServiceResponse(parameters));
    }

    @Test
    public void testGetAmazonPaymentProcessor() {
        assertNotNull(new AmazonFPS().getAmazonPaymentProcessor());
    }

    @Test
    public void testMakePayRequestI() throws DataSourceException {
        final String tokenId = "5435456.6546456456.45645";
        final String reference = "11--22--33";
        Payment payment = new Payment();
        payment.setAuthorizationId(tokenId);
        payment.setReference(reference);

        AmazonFPS instance = new AmazonFPS() {
            @Override
            protected AmazonFPSClient getAmazonPaymentProcessor() {
                return new AmazonFPSClient("public", "secret") {
                    @Override
                    public PayResponse pay(PayRequest request) throws AmazonFPSException {
                        assertEquals(tokenId, request.getSenderTokenId());
                        assertEquals(reference, request.getCallerReference());
                        // FIXME: need to be updated when the Payment data are loaded from the confirmed proposal!
                        assertEquals("1", request.getTransactionAmount().getValue());
                        assertEquals(CurrencyCode.USD, request.getTransactionAmount().getCurrencyCode());

                        return new PayResponse();
                    }
                };
            }
        };

        payment = instance.makePayRequest(payment);
        assertNull(payment.getTransactionId());
        assertNull(payment.getStatus());
        assertNull(payment.getRequestId());
    }

    @Test
    public void testMakePayRequestII() throws DataSourceException {
        final String tokenId = "5435456.6546456456.45645";
        final String reference = "11--22--33";
        Payment payment = new Payment();
        payment.setAuthorizationId(tokenId);
        payment.setReference(reference);

        AmazonFPS instance = new AmazonFPS() {
            @Override
            protected AmazonFPSClient getAmazonPaymentProcessor() {
                return new AmazonFPSClient("public", "secret") {
                    @Override
                    public PayResponse pay(PayRequest request) throws AmazonFPSException {
                        assertEquals(tokenId, request.getSenderTokenId());
                        assertEquals(reference, request.getCallerReference());
                        // FIXME: need to be updated when the Payment data are loaded from the confirmed proposal!
                        assertEquals("1", request.getTransactionAmount().getValue());
                        assertEquals(CurrencyCode.USD, request.getTransactionAmount().getCurrencyCode());

                        PayResponse response = new PayResponse();
                        response.setPayResult(new PayResult());
                        response.setResponseMetadata(new ResponseMetadata());
                        return response;
                    }
                };
            }
        };

        payment = instance.makePayRequest(payment);
        assertNull(payment.getTransactionId());
        assertNull(payment.getStatus());
        assertNull(payment.getRequestId());
    }

    @Test
    public void testMakePayRequestIII() throws DataSourceException {
        final String tokenId = "5435456.6546456456.45645";
        final String reference = "11--22--33";
        Payment payment = new Payment();
        payment.setAuthorizationId(tokenId);
        payment.setReference(reference);

        final String transactionId = "transId";
        final TransactionStatus status = TransactionStatus.SUCCESS;
        final String requestId = "reqId";

        AmazonFPS instance = new AmazonFPS() {
            @Override
            protected AmazonFPSClient getAmazonPaymentProcessor() {
                return new AmazonFPSClient("public", "secret") {
                    @Override
                    public PayResponse pay(PayRequest request) throws AmazonFPSException {
                        assertEquals(tokenId, request.getSenderTokenId());
                        assertEquals(reference, request.getCallerReference());
                        // FIXME: need to be updated when the Payment data are loaded from the confirmed proposal!
                        assertEquals("1", request.getTransactionAmount().getValue());
                        assertEquals(CurrencyCode.USD, request.getTransactionAmount().getCurrencyCode());

                        PayResult result = new PayResult();
                        result.setTransactionId(transactionId);
                        result.setTransactionStatus(status);

                        ResponseMetadata metadata = new ResponseMetadata();
                        metadata.setRequestId(requestId);

                        PayResponse response = new PayResponse();
                        response.setPayResult(result);
                        response.setResponseMetadata(metadata);
                        return response;
                    }
                };
            }
        };

        payment = instance.makePayRequest(payment);
        assertEquals(transactionId, payment.getTransactionId());
        assertEquals(status, payment.getStatus());
        assertEquals(requestId, payment.getRequestId());
    }

    @Test(expected=DataSourceException.class)
    public void testMakePayRequestIV() throws DataSourceException {
        final String tokenId = "5435456.6546456456.45645";
        final String reference = "11--22--33";
        Payment payment = new Payment();
        payment.setAuthorizationId(tokenId);
        payment.setReference(reference);

        AmazonFPS instance = new AmazonFPS() {
            @Override
            protected AmazonFPSClient getAmazonPaymentProcessor() {
                return new AmazonFPSClient("public", "secret") {
                    @Override
                    public PayResponse pay(PayRequest request) throws AmazonFPSException {
                        assertEquals(tokenId, request.getSenderTokenId());
                        assertEquals(reference, request.getCallerReference());
                        throw new AmazonFPSException("Done in purpose!");
                    }
                };
            }
        };

        instance.makePayRequest(payment);
    }
}
