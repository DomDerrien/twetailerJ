package twetailer.task;

import twetailer.dao.BaseOperations;
import twetailer.dao.RawCommandOperations;
import twetailer.dao.SettingsOperations;

public class MockCommandProcessor extends CommandProcessor {

    private static BaseOperations originalBaseOperations;
    private static SettingsOperations originalSettingsOperations;
    private static RawCommandOperations originalRawCommandOperations;

    public static BaseOperations injectMocks(BaseOperations mockBaseOperations) {
        originalBaseOperations = _baseOperations;
        _baseOperations = mockBaseOperations;
        return originalBaseOperations;
    }

    public static SettingsOperations injectMocks(SettingsOperations mockSettingsOperations) {
        originalSettingsOperations = settingsOperations;
        settingsOperations = mockSettingsOperations;
        return originalSettingsOperations;
    }

    public static RawCommandOperations injectMocks(RawCommandOperations mockRawCommandOperations) {
        originalRawCommandOperations = rawCommandOperations;
        rawCommandOperations = mockRawCommandOperations;
        return originalRawCommandOperations;
    }

    public static void restoreOperations() {
        if (originalBaseOperations != null) {
            _baseOperations = originalBaseOperations;
        }
        if (originalSettingsOperations != null) {
            settingsOperations = originalSettingsOperations;
        }
        if (originalRawCommandOperations != null) {
            rawCommandOperations = originalRawCommandOperations;
        }
    }
}
