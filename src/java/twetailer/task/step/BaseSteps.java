package twetailer.task.step;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.InfluencerOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.PaymentOperations;
import twetailer.dao.ProposalOperations;
import twetailer.dao.RawCommandOperations;
import twetailer.dao.RegistrarOperations;
import twetailer.dao.ReportOperations;
import twetailer.dao.ResellerOperations;
import twetailer.dao.ReviewSystemOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dao.SettingsOperations;
import twetailer.dao.StoreOperations;
import twetailer.dao.WishOperations;
import domderrien.i18n.DateUtils;
import domderrien.jsontools.JsonObject;

public class BaseSteps {

    private static BaseOperations baseOperations;
    private static ConsumerOperations consumerOperations;
    private static DemandOperations demandOperations;
    private static LocationOperations locationOperations;
    private static InfluencerOperations influencerOperations;
    private static PaymentOperations paymentOperations;
    private static ProposalOperations proposalOperations;
    private static RawCommandOperations rawCommandOperations;
    private static RegistrarOperations registrarOperations;
    private static ReportOperations reportOperations;
    private static ResellerOperations resellerOperations;
    private static ReviewSystemOperations reviewSystemOperations;
    private static SaleAssociateOperations saleAssociateOperations;
    private static SettingsOperations settingsOperations;
    private static StoreOperations storeOperations;
    private static WishOperations wishOperations;

    public static void resetOperationControllers(boolean forUnitTests) {
        baseOperations = forUnitTests ? null : new BaseOperations();
        consumerOperations = forUnitTests ? null : new ConsumerOperations();
        demandOperations = forUnitTests ? null : new DemandOperations();
        influencerOperations = forUnitTests ? null : new InfluencerOperations();
        locationOperations = forUnitTests ? null : new LocationOperations();
        paymentOperations = forUnitTests ? null : new PaymentOperations();
        proposalOperations = forUnitTests ? null : new ProposalOperations();
        rawCommandOperations = forUnitTests ? null : new RawCommandOperations();
        registrarOperations = forUnitTests ? null : new RegistrarOperations();
        reportOperations = forUnitTests ? null : new ReportOperations();
        resellerOperations = forUnitTests ? null : new ResellerOperations();
        reviewSystemOperations = forUnitTests ? null : new ReviewSystemOperations();
        saleAssociateOperations = forUnitTests ? null : new SaleAssociateOperations();
        settingsOperations = forUnitTests ? null : new SettingsOperations();
        storeOperations = forUnitTests ? null : new StoreOperations();
        wishOperations = forUnitTests ? null : new WishOperations();
    }

    static {
        resetOperationControllers(false);
    }

    public static BaseOperations getBaseOperations() { return baseOperations; }
    public static ConsumerOperations getConsumerOperations() { return consumerOperations; }
    public static DemandOperations getDemandOperations() { return demandOperations; }
    public static InfluencerOperations getInfluencerOperations() { return influencerOperations; }
    public static LocationOperations getLocationOperations() { return locationOperations; }
    public static PaymentOperations getPaymentOperations() { return paymentOperations; }
    public static ProposalOperations getProposalOperations() { return proposalOperations; }
    public static RawCommandOperations getRawCommandOperations() { return rawCommandOperations; }
    public static RegistrarOperations getRegistrarOperations() { return registrarOperations; }
    public static ReportOperations getReportOperations() { return reportOperations; }
    public static ResellerOperations getResellerOperations() { return resellerOperations; }
    public static ReviewSystemOperations getReviewSystemOperations() { return reviewSystemOperations; }
    public static SaleAssociateOperations getSaleAssociateOperations() { return saleAssociateOperations; }
    public static SettingsOperations getSettingsOperations() { return settingsOperations; }
    public static StoreOperations getStoreOperations() { return storeOperations; }
    public static WishOperations getWishOperations() { return wishOperations; }

    public static void setMockBaseOperations(BaseOperations baseOperations) { BaseSteps.baseOperations = baseOperations; }
    public static void setMockConsumerOperations(ConsumerOperations consumerOperations) { BaseSteps.consumerOperations = consumerOperations; }
    public static void setMockDemandOperations(DemandOperations demandOperations) { BaseSteps.demandOperations = demandOperations; }
    public static void setMockInfluencerOperations(InfluencerOperations influencerOperations) { BaseSteps.influencerOperations = influencerOperations; }
    public static void setMockLocationOperations(LocationOperations locationOperations) { BaseSteps.locationOperations = locationOperations; }
    public static void setMockPaymentOperations(PaymentOperations paymentOperations) { BaseSteps.paymentOperations = paymentOperations; }
    public static void setMockProposalOperations(ProposalOperations proposalOperations) { BaseSteps.proposalOperations = proposalOperations; }
    public static void setMockRawCommandOperations(RawCommandOperations rawCommandOperations) { BaseSteps.rawCommandOperations = rawCommandOperations; }
    public static void setMockRegistrarOperations(RegistrarOperations registrarOperations) { BaseSteps.registrarOperations = registrarOperations; }
    public static void setMockReportOperations(ReportOperations reportOperations) { BaseSteps.reportOperations = reportOperations; }
    public static void setMockResellerOperations(ResellerOperations resellerOperations) { BaseSteps.resellerOperations = resellerOperations; }
    public static void setMockReviewSystemOperations(ReviewSystemOperations reviewSystemOperations) { BaseSteps.reviewSystemOperations = reviewSystemOperations; }
    public static void setMockSaleAssociateOperations(SaleAssociateOperations saleAssociateOperations) { BaseSteps.saleAssociateOperations = saleAssociateOperations; }
    public static void setMockSettingsOperations(SettingsOperations settingsOperations) { BaseSteps.settingsOperations = settingsOperations; }
    public static void setMockStoreOperations(StoreOperations storeOperations) { BaseSteps.storeOperations = storeOperations; }
    public static void setMockWishOperations(WishOperations wishOperations) { BaseSteps.wishOperations = wishOperations; }

    /**
     * Import the value of the 'String' field and insert it in the filter list. If
     * the parameter has the 'startsWith' syntax, it generates the corresponding
     * sequence.
     *
     * @param fieldName Name of the attribute to process
     * @param parameters Bag of parameters built client-side
     * @param queryFilters Bag where the process attribute values are store
     * @return the filter bag for chaining purposes
     */
    protected static Map<String, Object> processStringFilter(String fieldName, JsonObject parameters, Map<String, Object> queryFilters) {

        if (parameters.containsKey(fieldName)) {
            String value = parameters.getString(fieldName);

            if (value.charAt(0) == BaseOperations.FILTER_STARTS_WITH) {
                if (value.length() < 3) {
                    throw new IllegalArgumentException("Filter must be at least 3 characters long for '" + fieldName + "'!");
                }
                queryFilters.put(BaseOperations.FILTER_STARTS_WITH + fieldName, value.substring(1));
            }
            else {
                queryFilters.put(fieldName, value);
            }
        }

        return queryFilters;
    }

    /**
     * Import the value of the 'Long' field.
     *
     * @param fieldName Name of the attribute to process
     * @param parameters Bag of parameters built client-side
     * @param queryFilters Bag where the process attribute values are store
     * @return the filter bag for chaining purposes
     */
    protected static Map<String, Object> processLongFilter(String fieldName, JsonObject parameters, Map<String, Object> queryFilters) {

        if (parameters.containsKey(fieldName)) {
            Long value = parameters.getLong(fieldName);

            queryFilters.put(fieldName, value);
        }

        return queryFilters;
    }

    /**
     * Import the value of the 'Boolean' field. The <code>true</code>
     * value is reported as soon as the parameter is detected in the
     * parameter list.
     *
     * @param fieldName Name of the attribute to process
     * @param parameters Bag of parameters built client-side
     * @param queryFilters Bag where the process attribute values are store
     * @return the filter bag for chaining purposes
     */
    protected static Map<String, Object> processBooleanFilter(String fieldName, JsonObject parameters, Map<String, Object> queryFilters) {

        if (!parameters.containsKey(fieldName)) {
            queryFilters.put(fieldName, Boolean.TRUE);
        }

        return queryFilters;
    }

    /**
     * Import the value of the 'Date' field and insert it in the filter list. As
     * no dates can be really equal, the prepared filter has the 'less than' syntax.
     *
     * @param fieldName Name of the attribute to process
     * @param parameters Bag of parameters built client-side
     * @param queryFilters Bag where the process attribute values are store
     * @return the filter bag for chaining purposes
     */
    protected static Map<String, Object> processDateFilter(String fieldName, JsonObject parameters, Map<String, Object> queryFilters) {

        if (parameters.containsKey(fieldName)) {
            Date lastModificationDate = null;
            try {
                lastModificationDate = DateUtils.isoToDate(parameters.getString(fieldName));
                queryFilters.put('>' + fieldName, lastModificationDate);
            }
            catch (ParseException e) { } // Date not set, too bad.
        }

        return queryFilters;
    }
}
