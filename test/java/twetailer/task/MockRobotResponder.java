package twetailer.task;

import twetailer.dao.BaseOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.SaleAssociateOperations;

public class MockRobotResponder extends RobotResponder {

    private static BaseOperations originalBaseOperations;
    private static DemandOperations originalDemandOperations;
    private static SaleAssociateOperations originalSaleAssociateOperations;

    public static BaseOperations injectMocks(BaseOperations mockBaseOperations) {
        originalBaseOperations = _baseOperations;
        _baseOperations = mockBaseOperations;
        return originalBaseOperations;
    }

    public static DemandOperations injectMocks(DemandOperations mockDemandOperations) {
        originalDemandOperations = demandOperations;
        demandOperations = mockDemandOperations;
        return originalDemandOperations;
    }

    public static SaleAssociateOperations injectMocks(SaleAssociateOperations mockSaleAssociateOperations) {
        originalSaleAssociateOperations = saleAssociateOperations;
        saleAssociateOperations = mockSaleAssociateOperations;
        return originalSaleAssociateOperations;
    }

    public static void restoreOperations() {
        if (originalBaseOperations != null) {
            _baseOperations = originalBaseOperations;
        }
        if (originalDemandOperations != null) {
            demandOperations = originalDemandOperations;
        }
        if (originalSaleAssociateOperations != null) {
            saleAssociateOperations = originalSaleAssociateOperations;
        }
    }
}
