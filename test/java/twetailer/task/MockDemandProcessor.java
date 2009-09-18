package twetailer.task;

import twetailer.dao.BaseOperations;
import twetailer.dao.DemandOperations;

public class MockDemandProcessor extends DemandProcessor {

    private static BaseOperations originalBaseOperations;
    private static DemandOperations originalDemandOperations;

    public static BaseOperations injectMockBaseOperations(BaseOperations mockBaseOperations) {
        originalBaseOperations = _baseOperations;
        _baseOperations = mockBaseOperations;
        return originalBaseOperations;
    }

    public static DemandOperations injectMockDemandOperations(DemandOperations mockDemandOperations) {
        originalDemandOperations = demandOperations;
        demandOperations = mockDemandOperations;
        return originalDemandOperations;
    }

    public static void restoreDemandProcessor() {
        _baseOperations = originalBaseOperations;
        demandOperations = originalDemandOperations;
    }
}
