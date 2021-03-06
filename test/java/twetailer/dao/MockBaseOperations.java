package twetailer.dao;

import javax.jdo.MockPersistenceManager;
import javax.jdo.PersistenceManager;

import com.google.appengine.api.taskqueue.MockQueue;
import com.google.appengine.api.taskqueue.Queue;

public class MockBaseOperations extends BaseOperations {

    PersistenceManager lastPersistenceManager;

    @Override
    public PersistenceManager getPersistenceManager() {
        // lastPersistenceManager = new MockPersistenceManagerFactory().getPersistenceManager(); // Too much time consuming!
        lastPersistenceManager = new MockPersistenceManager();
        return lastPersistenceManager;
    }

    public PersistenceManager getPreviousPersistenceManager() {
        return lastPersistenceManager;
    }

    MockQueue lastQueue;

    @Override
    public Queue getQueue() {
        lastQueue = new MockQueue();
        return lastQueue;
    }

    public MockQueue getPreviousQueue() {
        return lastQueue;
    }
};
