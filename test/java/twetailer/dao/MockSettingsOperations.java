package twetailer.dao;

import javax.jdo.PersistenceManager;

import twetailer.dto.Settings;

public class MockSettingsOperations extends SettingsOperations {
    @Override
    public Settings getSettings() {
        return new Settings();
    }
    @Override
    public Settings getSettings(PersistenceManager pm) {
        return new Settings();
    }
    @Override
    public Settings updateSettings(Settings settings) {
        return settings;
    }
    @Override
    public Settings updateSettings(PersistenceManager pm, Settings settings) {
        return settings;
    }
};
