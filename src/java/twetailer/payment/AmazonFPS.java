package twetailer.payment;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.security.SignatureException;

import com.amazonaws.cbui.AmazonFPSSingleUsePipeline;
import com.amazonaws.utils.PropertyBundle;
import com.amazonaws.utils.PropertyKeys;

public class AmazonFPS {

    public static String getCoBrandedServiceUrl(Long consumerKey, Long demandKey, Long proposalKey, String description, Double total, String currency) throws MalformedURLException, SignatureException, UnsupportedEncodingException {

        String accessKey = PropertyBundle.getProperty(PropertyKeys.AWS_ACCESS_KEY);
        String secretKey = PropertyBundle.getProperty(PropertyKeys.AWS_SECRET_KEY);

        AmazonFPSSingleUsePipeline pipeline = new AmazonFPSSingleUsePipeline(accessKey, secretKey);

        pipeline.setMandatoryParameters("callerReferenceSingleUse",
                "http://www.mysite.com/call_back.jsp", "5");

        /*
         * Source: http://docs.amazonwebservices.com/AmazonFPS/latest/FPSGettingStartedGuide/gsMakingCoBrandedUIRequests.html
         *
        Parameter           Definition
        ==========================================================================
        callerKey           DigitalDownload's AWS Access Key ID.
        callerReference     A unique value DigitalDownload generated to identify the sender token for future references.
        currencyCode        Indicates the token's currency.
        paymentMethod       Payment methods that DigitalDownload supports. In this case the payment methods include an Amazon Payments account balance, bank transfers, and credit cards.
        paymentReason       Description of this transaction. John can see this on the Payment Authorization page.
        pipelineName        Name of the particular CBUI authorization pipeline DigitalDownload requested.
                            The value SingleUse refers to the pipeline that creates a payment token that is to be used only once. For information about payment tokens that can be used more than once (e.g., for recurring payments), go to the Amazon FPS Advanced Quick Start Developer Guide.
        returnURL           The destination web site John is redirected to after completing the payment authorization. This is a location on DigitalDownload's site.
        signature           A value Digital Download calculated using an HmacSHA256 or HmacSHA1 encryption.
                            For information about how to create the signature, go to the Amazon FPS Basic Quick Start Developer Guide.
        signatureVersion    A value that specifies the signature format.
        signatureMethod     A value that specifies the signing method.
                            For information on signing your request, see Working with Signatures in the Amazon Flexible Payments Service Basic Quick Start Developer Guide.
        transactionAmount   The total purchase price in USD, including tax and shipping.
        version             The version of the Co-Branded service API to use. This should always be set to 2009-01-09.
         */

        //optional parameters
        pipeline.addParameter("transactionAmount", total.toString());
        pipeline.addParameter("currencyCode", currency);
        pipeline.addParameter("paymentReason", description);
        pipeline.addParameter("paymentMethod", "ABT,ACH,CC");
        pipeline.addParameter("callerReference", consumerKey + "-" + demandKey + "-" + proposalKey);

        //SingleUse url
        System.err.println("Sample CBUI url for SingleUse pipeline :" + pipeline.getUrl());
        return pipeline.getUrl();
    }
}
