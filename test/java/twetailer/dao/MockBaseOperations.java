package twetailer.dao;

import javax.jdo.MockPersistenceManager;
import javax.jdo.PersistenceManager;

import com.google.appengine.api.labs.taskqueue.MockQueue;
import com.google.appengine.api.labs.taskqueue.Queue;

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

    Queue lastQueue;

    @Override
    public Queue getQueue() {
        lastQueue = new MockQueue();
        return lastQueue;
    }

    public Queue getPreviousQueue() {
        return lastQueue;
    }
};
