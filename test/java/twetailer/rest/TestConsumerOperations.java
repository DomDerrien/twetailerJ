package twetailer.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.DataSourceException;
import twetailer.dto.Consumer;
import twitter4j.MockTwitterUser;
import twitter4j.TwitterException;

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.User;

public class TestConsumerOperations {

	class MockConsumerOperation extends ConsumerOperations {
		PersistenceManager mockPM;
		protected void setPersistenceManager(PersistenceManager pm) {
			mockPM = pm;
		}
	    public PersistenceManager getPersistenceManager() {
	    	return mockPM;
	    }
	}
	
	private MockConsumerOperation mockConsumerOperations;
    private MockAppEngineEnvironment mockEnvironment;
    
	@Before
	public void setUp() throws Exception {
		mockConsumerOperations = new MockConsumerOperation();
		
        mockEnvironment = new MockAppEngineEnvironment();
        mockEnvironment.setUp();
	}

	@After
	public void tearDown() throws Exception {
        mockEnvironment.tearDown();
	}

    @Test
	public void testGetConsumersI() throws DataSourceException {
		final String qA = "a";
		final String qV = "b";
		mockConsumerOperations.setPersistenceManager(new MockPersistenceManager() {
            @Override
            @SuppressWarnings({ "unchecked", "serial" })
            public javax.jdo.Query newQuery(Class clazz) {
                assertEquals(Consumer.class, clazz);
                return new MockQuery() {
                    @Override
                    public void setFilter(String pattern) {
                        assertEquals(qA + " == value", pattern);
                    }
                    @Override
                    public void setOrdering(String pattern) {
                        assertEquals("creationDate desc", pattern);
                    }
                    @Override
                    public void declareParameters(String pattern) {
                        assertEquals("String value", pattern);
                    }
                    @Override
                    public List<Object> execute(Object value) {
                        assertEquals(qV, value);
                        return new ArrayList<Object>();
                    }
                };
            }
		});
		List<Consumer> consumers = mockConsumerOperations.getConsumers(qA, qV, 0);
        assertNotNull(consumers);
        assertEquals(0, consumers.size());
		assertTrue(mockConsumerOperations.getPersistenceManager().isClosed());
	}

	@Test
	public void testGetConsumersII() throws DataSourceException {
		final String qA = "a";
		final String qV = "b";
		mockConsumerOperations.setPersistenceManager(new MockPersistenceManager() {
			@SuppressWarnings({ "serial", "unchecked" })
            public javax.jdo.Query newQuery(Class clazz) {
                assertEquals(Consumer.class, clazz);
				return new MockQuery(){
                    @Override
                    public List<Object> execute(Object value) {
						// Whatever the query is, no Consumer instance match
                        return new ArrayList<Object>();
					}
				};
			}
		});
		List<Consumer> consumers = mockConsumerOperations.getConsumers(qA, qV, 0);
        assertNotNull(consumers);
        assertEquals(0, consumers.size());
		assertTrue(mockConsumerOperations.getPersistenceManager().isClosed());
	}

	@Test
	public void testGetConsumersIII() throws DataSourceException {
		final String qA = "a";
		final String qV = "b";
		mockConsumerOperations.setPersistenceManager(new MockPersistenceManager() {
			@SuppressWarnings({ "serial", "unchecked" })
            public javax.jdo.Query newQuery(Class clazz) {
                assertEquals(Consumer.class, clazz);
				return new MockQuery(){
                    @Override
                    public List<Object> execute(Object value) {
						// Whatever the query is, no Consumer instance match
						return new ArrayList<Object>();
					}
				};
			}
		});
		List<Consumer> consumers = mockConsumerOperations.getConsumers(qA, qV, 0);
        assertNotNull(consumers);
		assertEquals(0, consumers.size());
		assertTrue(mockConsumerOperations.getPersistenceManager().isClosed());
	}

	@Test
	public void testGetConsumersIV() throws DataSourceException {
		final String qA = "a";
		final String qV = "b";
		final Consumer selected = new Consumer();
		mockConsumerOperations.setPersistenceManager(new MockPersistenceManager() {
			@SuppressWarnings({ "serial", "unchecked" })
            public javax.jdo.Query newQuery(Class clazz) {
                assertEquals(Consumer.class, clazz);
				return new MockQuery(){
                    @Override
                    public List<Object> execute(Object value) {
						// Whatever the query is, one Consumer instance match
						List<Object> selection = new ArrayList<Object>();
						selection.add(selected);
						return selection;
					}
				};
			}
		});
		List<Consumer> consumers = mockConsumerOperations.getConsumers(qA, qV, 0);
		assertNotNull(consumers);
		assertEquals(1, consumers.size());
		assertEquals(selected, consumers.get(0));
		assertTrue(mockConsumerOperations.getPersistenceManager().isClosed());
	}

	@Test
	public void testGetConsumersV() throws DataSourceException {
		final String qA = "a";
		final String qV = "b";
		final Consumer selected = new Consumer();
		final Consumer spare1 = new Consumer();
		final Consumer spare2 = new Consumer();
		final Consumer spare3 = new Consumer();
		final Consumer spare4 = new Consumer();
		mockConsumerOperations.setPersistenceManager(new MockPersistenceManager() {
			@SuppressWarnings({ "serial", "unchecked" })
            public javax.jdo.Query newQuery(Class clazz) {
                assertEquals(Consumer.class, clazz);
				return new MockQuery(){
                    @Override
                    public List<Object> execute(Object value) {
						// Whatever the query is, one Consumer instance match
						List<Object> selection = new ArrayList<Object>();
						selection.add(selected);
						selection.add(spare1);
						selection.add(spare2);
						selection.add(spare3);
						selection.add(spare4);
						return selection;
					}
				};
			}
		});
		List<Consumer> consumers = mockConsumerOperations.getConsumers(qA, qV, 0);
		assertNotNull(consumers);
		assertEquals(5, consumers.size());
		assertEquals(selected, consumers.get(0));
		assertEquals(spare1, consumers.get(1));
		assertEquals(spare2, consumers.get(2));
		assertEquals(spare3, consumers.get(3));
		assertEquals(spare4, consumers.get(4));
		assertTrue(mockConsumerOperations.getPersistenceManager().isClosed());
	}

	@Test(expected=DataSourceException.class)
	public void testGetConsumerI() throws DataSourceException {
		final long key = 1234L;
		mockConsumerOperations.setPersistenceManager(new MockPersistenceManager() {
            @Override
            public <T> T getObjectById(Class<T> arg0, Object arg1) {
                assertEquals(key, ((Long) arg1).longValue());
                return null;
            }
		});
		try {
		    mockConsumerOperations.getConsumer(key);
		}
		finally {
		    assertTrue(mockConsumerOperations.getPersistenceManager().isClosed());
		}
	}

	@Test
	public void testGetConsumerIV() throws DataSourceException {
        final long key = 1234L;
		final Consumer selected = new Consumer();
		mockConsumerOperations.setPersistenceManager(new MockPersistenceManager() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T getObjectById(Class<T> arg0, Object arg1) {
                assertEquals(key, ((Long) arg1).longValue());
                return (T) selected;
            }
		});
		Consumer consumer = mockConsumerOperations.getConsumer(key);
		assertNotNull(consumer);
		assertEquals(selected, consumer);
		assertTrue(mockConsumerOperations.getPersistenceManager().isClosed());
    }

    @Test(expected=InvalidParameterException.class)
    public void testGetConsumerV() throws DataSourceException {
        BaseOperations bOps = new BaseOperations();
        PersistenceManager pm = mockEnvironment.getPersistenceManager();
        bOps.getConsumerOperations().getConsumer(pm, null);
    }

    @Test(expected=InvalidParameterException.class)
    public void testGetConsumerVI() throws DataSourceException {
        BaseOperations bOps = new BaseOperations();
        PersistenceManager pm = mockEnvironment.getPersistenceManager();
        bOps.getConsumerOperations().getConsumer(pm, 0L);
    }

	@Test
	public void testCreateConsumerI() throws DataSourceException {
		mockConsumerOperations.setPersistenceManager(new MockPersistenceManager() {
			@SuppressWarnings({ "serial", "unchecked" })
            public javax.jdo.Query newQuery(Class clazz) {
                assertEquals(Consumer.class, clazz);
				return new MockQuery(){
                    @Override
                    public List<Object> execute(Object value) {
						// Whatever the query is, no Consumer instance match
						return new ArrayList<Object>();
					}
				};
			}
		});
		User systemUser = new User("email", "domain");
		Consumer createdConsumer = mockConsumerOperations.createConsumer(systemUser);
		assertNotNull(createdConsumer);
		assertEquals(createdConsumer, ((MockPersistenceManager) mockConsumerOperations.getPersistenceManager()).getPersistedObject());
		assertEquals("email", createdConsumer.getEmail());
		assertEquals("email", createdConsumer.getName());
		assertTrue(mockConsumerOperations.getPersistenceManager().isClosed());
	}
	
	@Test
	public void testCreateConsumerII() throws DataSourceException {
		final Consumer existingConsumer = new Consumer();
		User systemUser = new User("email", "domain");
		mockConsumerOperations.setPersistenceManager(new MockPersistenceManager() {
			@SuppressWarnings({ "serial", "unchecked" })
            public javax.jdo.Query newQuery(Class clazz) {
                assertEquals(Consumer.class, clazz);
				return new MockQuery(){
                    @Override
                    public List<Object> execute(Object value) {
						List<Object> selection = new ArrayList<Object>();
						selection.add(existingConsumer);
						return selection;
					}
				};
			}
		});
		Consumer createdConsumer = mockConsumerOperations.createConsumer(systemUser);
		assertNotNull(createdConsumer);
		assertNull(((MockPersistenceManager) mockConsumerOperations.getPersistenceManager()).getPersistedObject());
		assertEquals(existingConsumer, createdConsumer);
		assertTrue(mockConsumerOperations.getPersistenceManager().isClosed());
	}

    @Test
    public void testCreateConsumerIII() throws DataSourceException {
        User user = new User("test", "test.com");
        
        BaseOperations bOps = new BaseOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return mockEnvironment.getPersistenceManager();
            }
        };

        // Verify there's no instance
        Query query = new Query(Consumer.class.getSimpleName());
        assertEquals(0, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());

        // Create the user once
        bOps.getConsumerOperations().createConsumer(user);
        
        // Verify there's one instance
        query = new Query(Consumer.class.getSimpleName());
        assertEquals(1, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());

        // Tries to recreate it
        bOps.getConsumerOperations().createConsumer(user);
        
        // Verify there's still one instance
        query = new Query(Consumer.class.getSimpleName());
        assertEquals(1, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());
    }

    @Test
    @SuppressWarnings("serial")
    public void testCreateConsumerIV() throws DataSourceException, TwitterException {
        final String name = "test";
        MockTwitterUser user = new MockTwitterUser() {
            @Override
            public String getName() {
                return name;
            }
        };
        
        BaseOperations bOps = new BaseOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return mockEnvironment.getPersistenceManager();
            }
        };

        // Verify there's no instance
        Query query = new Query(Consumer.class.getSimpleName());
        assertEquals(0, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());

        // Create the user once
        bOps.getConsumerOperations().createConsumer(user);
        
        // Verify there's one instance
        query = new Query(Consumer.class.getSimpleName());
        assertEquals(1, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());

        // Tries to recreate it
        bOps.getConsumerOperations().createConsumer(user);
        
        // Verify there's still one instance
        query = new Query(Consumer.class.getSimpleName());
        assertEquals(1, DatastoreServiceFactory.getDatastoreService().prepare(query).countEntities());
    }

    @Test
    public void testUpdateConsumerI() throws DataSourceException {
        final Long twitterId = 2122312321L;
        User user = new User("test", "test.com");
        
        BaseOperations bOps = new BaseOperations();
        PersistenceManager pm = mockEnvironment.getPersistenceManager();
        try {
            // Create the user once
            Consumer consumer = bOps.getConsumerOperations().createConsumer(pm, user);
            
            // Update it
            consumer.setTwitterId(twitterId);
            
            // Persist the update
            consumer = bOps.getConsumerOperations().updateConsumer(pm, consumer);
            
            // Close the persistence manager and open a new one for a separate query
            pm.close();
            pm = mockEnvironment.getPersistenceManager();
            
            // Search for the update instance
            List<Consumer> consumers = bOps.getConsumerOperations().getConsumers("email", "test", 1);
            assertNotNull(consumers.size());
            assertEquals(1, consumers.size());
            assertEquals(twitterId, consumers.get(0).getTwitterId());
        }
        finally {
            pm.close();
        }
    }

    @Test
    public void testUpdateConsumerII() throws DataSourceException {
        
        final PersistenceManager pm = mockEnvironment.getPersistenceManager();
        BaseOperations bOps = new BaseOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm;
            }
        };
        try {
            // Create the user once
            Consumer consumer = bOps.getConsumerOperations().createConsumer(new User("test", "domain"));
            
            // Update it
            consumer.setTwitterId(12345L);
            
            // Persist the update
            consumer = bOps.getConsumerOperations().updateConsumer(consumer);
        }
        finally {
            pm.close();
        }
    }
}
