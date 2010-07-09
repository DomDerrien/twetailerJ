package twetailer.payment;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;

import twetailer.DataSourceException;
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

/**
 * Wrapper for the Amazon FPS library
 *
 * @author Dom Derrien
 */
public class AmazonFPS {

    public static final String CALLER_REFERENCE = "callerReference";
    public static final String TOKEN_ID = "tokenID";

    /**
     * Cook the URL for the "Check Out" button for Amazon FPS
     *
     * @param transactionReference Identifier of the transaction, will be used to associate the return token with the transaction. Unicity of this identifier is really important!
     * @param description Message to be displayed to the Amazon FPS users and that should describe the transaction to be paid.
     * @param total Amount the Amazon FPS user is going to be charged for.
     * @param currency Currency of the transaction, a 3-letter ISO code.
     * @return a signed URL the !twetailer user can call once that will open the Amazon FPS login screen and give him access to the Aamzon payment platform
     *
     * @throws MalformedURLException If there's an issue while formatting the URL
     * @throws SignatureException If the signature cannot be generated adequately
     * @throws UnsupportedEncodingException If the encoding of the description or of the identifier fails
     */
    public String getCoBrandedServiceUrl(String transactionReference, String description, Double total, String currency) throws MalformedURLException, SignatureException, UnsupportedEncodingException {

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

        AmazonFPSSingleUsePipeline pipeline = getAmazonURLGenerator();

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

    /* Made available for unit test purposes */
    protected AmazonFPSSingleUsePipeline getAmazonURLGenerator() {
        String accessKey = PropertyBundle.getProperty(PropertyKeys.AWS_ACCESS_KEY);
        String secretKey = PropertyBundle.getProperty(PropertyKeys.AWS_SECRET_KEY);

        return new AmazonFPSSingleUsePipeline(accessKey, secretKey);
    }

    /**
     * Verify that the URL used to return to !twetailer has not been tampered with
     *
     * @param requestParameters Parameters extracted from the Http request (<code>request.getParameterMap()</code>)
     * @return <code>true</code> if the request is correct, <code>false</code> otherwise
     *
     * @throws SignatureException If the signature is incorrect
     */
    public boolean verifyCoBrandedServiceResponse(Map<String, ?> requestParameters) throws SignatureException {
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
            else if (value instanceof String[]) {
                controlledParameters.put(key, ((String []) value)[0]);
            }
            else if (value instanceof String) {
                controlledParameters.put(key, (String) value);
            }
            else {
                controlledParameters.put(key, value.toString());
            }
        }

        return getAmazonSignatureVerifier().validateRequest(
                controlledParameters,
                MaezelServlet.getCoBrandedServiceEndPointURL(),
                "GET" // HTTP GET method specified because that's the mechanism used by the co-branded service
        );
    }

    /* Made available for unit test purposes */
    protected SignatureUtilsForOutbound getAmazonSignatureVerifier() {
        return new SignatureUtilsForOutbound();
    }

    /**
     * Call the Amazon service via Http to process the given transaction token to effectively transfer money
     *
     * @param payment Entity containing information about the transaction, with the token received after the user accepted the transaction
     * @return Update Payment entity with the state of the payment
     *
     * @throws DataSourceException If there's an issue while sending the request to Amazon or while processing its response
     */
    public Payment makePayRequest(Payment payment) throws DataSourceException {
        PayRequest request = new PayRequest();

        // senderTokenID is obtained from the Co-Branded service's return URL
        request.setSenderTokenId(payment.getAuthorizationId());

        // the caller reference is a unique identifier you create for this pay request
        request.setCallerReference(payment.getReference());

        // set the amount and type of currency
        Amount amount = new Amount();
        // FIXME: need to be updated when the Payment data are loaded from the confirmed proposal!
        amount.setValue("1"); //set the transaction amount here
        amount.setCurrencyCode(CurrencyCode.USD);
        request.setTransactionAmount(amount);

        // FIXME: apply more tuning to the transaction
        /*
         * Read http://docs.amazonwebservices.com/AmazonFPS/2008-09-17/FPSMarketplaceGuide/
         *
        request.setCallerDescription("caller description");
        request.setChargeFeeTo(new ChargeFeeTo());
        request.setDescriptorPolicy(new DescriptorPolicy());
        request.setMarketplaceFixedFee(new Amount());
        request.setMarketplaceVariableFee(new BigDecimal());
        request.setRecipientTokenId("recipient token id");
        request.setSenderDescription("sender description");
        request.setTransactionTimeoutInMins(1000);
         */

        // make the pay call
        AmazonFPSClient service = getAmazonPaymentProcessor();
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
        catch (AmazonFPSException ex) {
            throw new DataSourceException("Unexpected exception while processing the payment with AmazonFPSClient.pay()", ex);
        }

        return payment;
    }

    /* Made available for unit test purposes */
    protected AmazonFPSClient getAmazonPaymentProcessor() {
        String accessKey = PropertyBundle.getProperty(PropertyKeys.AWS_ACCESS_KEY);
        String secretKey = PropertyBundle.getProperty(PropertyKeys.AWS_SECRET_KEY);

        return new AmazonFPSClient(accessKey, secretKey);
    }
}
