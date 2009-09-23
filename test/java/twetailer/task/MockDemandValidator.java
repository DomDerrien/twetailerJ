package twetailer.task;

import twetailer.dao.BaseOperations;
import twetailer.dao.DemandOperations;

public class MockDemandValidator extends DemandValidator {

    private static BaseOperations originalBaseOperations;
    private static DemandOperations originalDemandOperations;

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

    public static void restoreOperations() {
        if (originalBaseOperations != null) {
            _baseOperations = originalBaseOperations;
        }
        if (originalDemandOperations != null) {
            demandOperations = originalDemandOperations;
        }
    }
}
