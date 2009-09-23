package twetailer.task;

import twetailer.dao.BaseOperations;
import twetailer.dao.SettingsOperations;

public class MockTweetLoader extends TweetLoader {

    private static BaseOperations originalBaseOperations;
    private static SettingsOperations originalSettingsOperations;

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

    public static void restoreOperations() {
        _baseOperations = originalBaseOperations;
        settingsOperations = originalSettingsOperations;
    }
}
