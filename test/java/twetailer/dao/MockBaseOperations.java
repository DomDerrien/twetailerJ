package twetailer.dao;

import javax.jdo.PersistenceManager;

public class MockBaseOperations extends BaseOperations {
    @Override
    public PersistenceManager getPersistenceManager() {
        return new MockPersistenceManager();
    }
};