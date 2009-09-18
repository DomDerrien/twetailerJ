package twetailer.adapter;

import twetailer.dao.BaseOperations;
import twetailer.dao.SettingsOperations;

public class MockTwitterAdapter extends TwitterAdapter {

    private static BaseOperations originalBaseOperations;
    private static SettingsOperations originalSettingsOperations;

    public static BaseOperations injectMockBaseOperations(BaseOperations mockBaseOperations) {
        originalBaseOperations = _baseOperations;
        _baseOperations = mockBaseOperations;
        return originalBaseOperations;
    }

    public static SettingsOperations injectMockSettingsOperations(SettingsOperations mockSettingsOperations) {
        originalSettingsOperations = settingsOperations;
        settingsOperations = mockSettingsOperations;
        return originalSettingsOperations;
    }

    public static void restoreTwitterAdapter() {
        _baseOperations = originalBaseOperations;
        settingsOperations = originalSettingsOperations;
    }
}
