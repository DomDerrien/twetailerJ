package twetailer.payment;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;

import twetailer.dto.Payment;
import twetailer.j2ee.MaezelServlet;

import com.amazonaws.cbui.AmazonFPSSingleUsePipeline;
import com.amazonaws.fps.AmazonFPSClient;
import com.amazonaws.fps.AmazonFPSException;
import com.amazonaws.fps.model.Amount;
import com.amazonaws.fps.model.CurrencyCode;
import com.amazonaws.fps.model.PayRequest;
import com.amazonaws.fps.model.PayResponse;
import com.amazonaws.fps.model.PayResult;
import com.amazonaws.fps.model.ResponseMetadata;
import com.amazonaws.ipnreturnurlvalidation.SignatureUtilsForOutbound;
import com.amazonaws.utils.PropertyBundle;
import com.amazonaws.utils.PropertyKeys;

public class AmazonFPS {

    public static final String CALLER_REFERENCE = "callerReference";
    public static final String TOKEN_ID = "tokenID";

    public static String getCoBrandedServiceUrl(String transactionReference, String description, Double total, String currency) throws MalformedURLException, SignatureException, UnsupportedEncodingException {

        String accessKey = PropertyBundle.getProperty(PropertyKeys.AWS_ACCESS_KEY);
        String secretKey = PropertyBundle.getProperty(PropertyKeys.AWS_SECRET_KEY);

        AmazonFPSSingleUsePipeline pipeline = new AmazonFPSSingleUsePipeline(accessKey, secretKey);

        /*
         * Source: http://docs.amazonwebservices.com/AmazonFPS/latest/FPSGettingStartedGuide/gsMakingCoBrandedUIRequests.html
         *
        Parameter           Definition
        ==========================================================================
        callerKey           Twetailer's AWS Access Key ID.
        callerReference     A unique value Twetailer generated to identify the sender token for future references.
        currencyCode        Indicates the token's currency.
        paymentMethod       Payment methods that Twetailer supports. In this case the payment methods include an Amazon Payments account balance, bank transfers, and credit cards.
        paymentReason       Description of this transaction. John can see this on the Payment Authorization page.
        pipelineName        Name of the particular CBUI authorization pipeline Twetailer requested.
                            The value SingleUse refers to the pipeline that creates a payment token that is to be used only once. For information about payment tokens that can be used more than once (e.g., for recurring payments), go to the Amazon FPS Advanced Quick Start Developer Guide.
        returnURL           The destination web site John is redirected to after completing the payment authorization. This is a location on Twetailer's site.
        signature           A value Twetailer calculated using an HmacSHA256 or HmacSHA1 encryption.
                            For information about how to create the signature, go to the Amazon FPS Basic Quick Start Developer Guide.
        signatureVersion    A value that specifies the signature format.
        signatureMethod     A value that specifies the signing method.
                            For information on signing your request, see Working with Signatures in the Amazon Flexible Payments Service Basic Quick Start Developer Guide.
        transactionAmount   The total purchase price in USD, including tax and shipping.
        version             The version of the Co-Branded service API to use. This should always be set to 2009-01-09.
        */

        pipeline.setMandatoryParameters(
                "callerReferenceSingleUse",
                MaezelServlet.getCoBrandedServiceEndPointURL(),
                total.toString()
        );

        // Optional parameters
        pipeline.addParameter("callerReference", transactionReference);
        pipeline.addParameter("currencyCode", currency);
        pipeline.addParameter("paymentReason", description);
        pipeline.addParameter("paymentMethod", "ABT,ACH,CC");

        // SingleUse url
        return pipeline.getUrl();
    }

    public static boolean verifyCoBrandedServiceResponse(Map<String, Object> requestParameters) throws SignatureException {
        /*
         * Sample:
         *
        http://twetailer.appspot.com/API/maezel/cbuiEndPoint?
            signature=fQMzWnWiWatTIP%2F4E6iRN2Ee5vlk2JXQDbUYVbfuuzcqNhObfBQt86pf4YryWma%2FRFNm%2BtxmK%2F7x%0AVdU%2BZIhSD88SYXP53L5lj70BFwdcx3v30M5ycSdHct5%2FyiQyQTcY4mCiKbcn4ZlH7jJHOwrEyPMb%0A9M31sifq4IYm5kCcMPWl9puBihCd%2BXUvL%2BHquEEVaeaLRmoTyNHenS6lj024SzZUQ4YHsUC3CCuJ%0AtzgTg2aaxF5BOtyyQ%2BweGz6AVyD3cb8HBuEmvDkHy%2B4d9fRYYv4Eexpa1AY7wd9D3j0%2BOtnhZokq%0Aycd6%2BLHmF58YAVgLZQRd8A7kbgvQhMnAi1LFNw%3D%3D&
            signatureVersion=2&
            signatureMethod=RSA-SHA1&
            certificateUrl=https%3A%2F%2Ffps.sandbox.amazonaws.com%2Fcerts%2F090909%2FPKICert.pem&
            errorMessage=The+following+input%28s%29+are+not+well+formed%3A+%5BcallerReference%5D&
            status=CE&
            callerReference=70064-108017-109048
         */
        // Note: Signature verification does not require the secret key

        Map<String, String> controlledParameters = new HashMap<String, String>(requestParameters.size());

        for(String key: requestParameters.keySet()) {
            Object value = requestParameters.get(key);
            if (value == null) {
                controlledParameters.put(key, null);
            }
            if (value instanceof String[]) {
                controlledParameters.put(key, ((String []) value)[0]);
            }
            else if (value instanceof String) {
                controlledParameters.put(key, (String) value);
            }
            else {
                controlledParameters.put(key, value.toString());
            }
        }

        return new SignatureUtilsForOutbound().validateRequest(
                controlledParameters,
                MaezelServlet.getCoBrandedServiceEndPointURL(),
                "GET" // HTTP GET method specified because that's the mechanism used by the co-branded service
        );
    }

    public static Payment makePayRequest(Payment payment) {
        PayRequest request = new PayRequest();

        // senderTokenID is obtained from the Co-Branded service's return URL
        request.setSenderTokenId(payment.getAuthorizationId());

        // the caller reference is a unique identifier you create for this
        // pay request
        request.setCallerReference(payment.getReference());

        // set the amount and type of currency
        Amount amount = new Amount();
        amount.setCurrencyCode(CurrencyCode.USD);
        amount.setValue("1"); //set the transaction amount here
        request.setTransactionAmount(amount);

        // Instantiate the AmazonFPS service, specifying your credentials
        String accessKey = PropertyBundle.getProperty(PropertyKeys.AWS_ACCESS_KEY);
        String secretKey = PropertyBundle.getProperty(PropertyKeys.AWS_SECRET_KEY);

        AmazonFPSClient service = new AmazonFPSClient(accessKey, secretKey);

        // make the pay call
        try {
            PayResponse response = service.pay(request);
            if (response.isSetPayResult()) {
                PayResult  payResult = response.getPayResult();
                if (payResult.isSetTransactionId()) {
                    payment.setTransactionId(payResult.getTransactionId());
                }
                if (payResult.isSetTransactionStatus()) {
                    payment.setStatus(payResult.getTransactionStatus());
                }
            }
            if (response.isSetResponseMetadata()) {
                ResponseMetadata metadata = response.getResponseMetadata();
                if (metadata.isSetRequestId()) {
                    payment.setRequestId(metadata.getRequestId());
                }
            }
        }
        catch (AmazonFPSException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return payment;
    }
}
