package twetailer.task.step;

import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.PaymentOperations;
import twetailer.dao.ProposalOperations;
import twetailer.dao.RawCommandOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dao.SettingsOperations;
import twetailer.dao.StoreOperations;

public class BaseSteps {

    private static BaseOperations baseOperations;
    private static ConsumerOperations consumerOperations;
    private static DemandOperations demandOperations;
    private static LocationOperations locationOperations;
    private static PaymentOperations paymentOperations;
    private static ProposalOperations proposalOperations;
    private static RawCommandOperations rawCommandOperations;
    private static SaleAssociateOperations saleAssociateOperations;
    private static SettingsOperations settingsOperations;
    private static StoreOperations storeOperations;

    public static void resetOperationControllers(boolean forUnitTests) {
        baseOperations = forUnitTests ? null : new BaseOperations();
        consumerOperations = forUnitTests ? null : new ConsumerOperations();
        demandOperations = forUnitTests ? null : new DemandOperations();
        locationOperations = forUnitTests ? null : new LocationOperations();
        paymentOperations = forUnitTests ? null : new PaymentOperations();
        proposalOperations = forUnitTests ? null : new ProposalOperations();
        rawCommandOperations = forUnitTests ? null : new RawCommandOperations();
        saleAssociateOperations = forUnitTests ? null : new SaleAssociateOperations();
        settingsOperations = forUnitTests ? null : new SettingsOperations();
        storeOperations = forUnitTests ? null : new StoreOperations();
    }

    static {
        resetOperationControllers(false);
    }

    public static BaseOperations getBaseOperations() { return baseOperations; }
    public static ConsumerOperations getConsumerOperations() { return consumerOperations; }
    public static DemandOperations getDemandOperations() { return demandOperations; }
    public static LocationOperations getLocationOperations() { return locationOperations; }
    public static PaymentOperations getPaymentOperations() { return paymentOperations; }
    public static ProposalOperations getProposalOperations() { return proposalOperations; }
    public static RawCommandOperations getRawCommandOperations() { return rawCommandOperations; }
    public static SaleAssociateOperations getSaleAssociateOperations() { return saleAssociateOperations; }
    public static SettingsOperations getSettingsOperations() { return settingsOperations; }
    public static StoreOperations getStoreOperations() { return storeOperations; }

    public static void setMockBaseOperations(BaseOperations baseOperations) { BaseSteps.baseOperations = baseOperations; }
    public static void setMockConsumerOperations(ConsumerOperations consumerOperations) { BaseSteps.consumerOperations = consumerOperations; }
    public static void setMockDemandOperations(DemandOperations demandOperations) { BaseSteps.demandOperations = demandOperations; }
    public static void setMockLocationOperations(LocationOperations locationOperations) { BaseSteps.locationOperations = locationOperations; }
    public static void setMockPaymentOperations(PaymentOperations paymentOperations) { BaseSteps.paymentOperations = paymentOperations; }
    public static void setMockProposalOperations(ProposalOperations proposalOperations) { BaseSteps.proposalOperations = proposalOperations; }
    public static void setMockRawCommandOperations(RawCommandOperations rawCommandOperations) { BaseSteps.rawCommandOperations = rawCommandOperations; }
    public static void setMockSaleAssociateOperations(SaleAssociateOperations saleAssociateOperations) { BaseSteps.saleAssociateOperations = saleAssociateOperations; }
    public static void setMockSettingsOperations(SettingsOperations settingsOperations) { BaseSteps.settingsOperations = settingsOperations; }
    public static void setMockStoreOperations(StoreOperations storeOperations) { BaseSteps.storeOperations = storeOperations; }
}
