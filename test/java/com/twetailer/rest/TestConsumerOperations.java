package com.twetailer.rest;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.google.appengine.api.users.User;
import com.twetailer.DataSourceException;
import com.twetailer.dto.Consumer;

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
	
	MockConsumerOperation mockConsumerOperations;

	@Before
	public void setUp() throws Exception {
		mockConsumerOperations = new MockConsumerOperation();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetConsumersI() throws DataSourceException {
		final String qA = "a";
		final String qV = "b";
		mockConsumerOperations.setPersistenceManager(new MockPersistenceManager() {
            @Override
            @SuppressWarnings({ "unchecked", "serial" })
            public Query newQuery(Class clazz) {
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
            public Query newQuery(Class clazz) {
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
            public Query newQuery(Class clazz) {
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
            public Query newQuery(Class clazz) {
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
            public Query newQuery(Class clazz) {
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

	@Test
	public void testCreateConsumerI() throws DataSourceException {
		mockConsumerOperations.setPersistenceManager(new MockPersistenceManager() {
			@SuppressWarnings({ "serial", "unchecked" })
            public Query newQuery(Class clazz) {
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
            public Query newQuery(Class clazz) {
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
}
