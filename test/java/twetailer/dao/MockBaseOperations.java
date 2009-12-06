package twetailer.dao;

import javax.jdo.MockPersistenceManager;
import javax.jdo.PersistenceManager;

import com.google.appengine.api.labs.taskqueue.MockQueue;
import com.google.appengine.api.labs.taskqueue.Queue;

public class MockBaseOperations extends BaseOperations {
    private static PersistenceManager pm = new MockPersistenceManager();

    @Override
    public PersistenceManager getPersistenceManager() {
        return pm;
    }

    private static Queue q = new MockQueue();

    @Override
    public Queue getQueue() {
        return q;
    }
};
