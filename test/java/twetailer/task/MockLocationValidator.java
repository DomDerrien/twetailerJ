package twetailer.task;

import twetailer.dao.BaseOperations;
import twetailer.dao.LocationOperations;

public class MockLocationValidator extends LocationValidator {

    private static BaseOperations originalBaseOperations;
    private static LocationOperations originalLocationOperations;

    public static BaseOperations injectMocks(BaseOperations mockBaseOperations) {
        originalBaseOperations = _baseOperations;
        _baseOperations = mockBaseOperations;
        return originalBaseOperations;
    }

    public static LocationOperations injectMocks(LocationOperations mockLocationOperations) {
        originalLocationOperations = locationOperations;
        locationOperations = mockLocationOperations;
        return originalLocationOperations;
    }

    public static void restoreOperations() {
        if (originalBaseOperations != null) {
            _baseOperations = originalBaseOperations;
        }
        if (originalLocationOperations != null) {
            locationOperations = originalLocationOperations;
        }
    }
}
