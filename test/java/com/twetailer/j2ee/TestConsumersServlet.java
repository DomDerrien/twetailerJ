package com.twetailer.j2ee;

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

public class TestConsumersServlet {

	@SuppressWarnings("serial")
	class MockConsumersServlet extends ConsumersServlet {
		PersistenceManager mockPM;
		protected void setPersistenceManager(PersistenceManager pm) {
			mockPM = pm;
		}
	    protected PersistenceManager getPersistenceManager() {
	    	return mockPM;
	    }
	}
	
	MockConsumersServlet mockConsumersServlet;

	@Before
	public void setUp() throws Exception {
		mockConsumersServlet = new MockConsumersServlet();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetConsumersI() throws DataSourceException {
		final String qA = "a";
		final String qV = "b";
		mockConsumersServlet.setPersistenceManager(new MockPersistenceManager() {
			public Query newQuery(String query) {
				assertNotSame(-1, query.indexOf(qA + " == '" + qV + "'"));
				return null;
			}
		});
		List<Consumer> consumers = mockConsumersServlet.getConsumers("a", "b");
		assertNull(consumers);
		assertTrue(mockConsumersServlet.getPersistenceManager().isClosed());
	}

	@Test
	public void testGetConsumersII() throws DataSourceException {
		final String qA = "a";
		final String qV = "b";
		mockConsumersServlet.setPersistenceManager(new MockPersistenceManager() {
			@SuppressWarnings("serial")
			public Query newQuery(String query) {
				assertNotSame(-1, query.indexOf(qA + " == '" + qV + "'"));
				return new MockQuery(){
					public Object execute() {
						// Whatever the query is, no Consumer instance match
						return null;
					}
				};
			}
		});
		List<Consumer> consumers = mockConsumersServlet.getConsumers("a", "b");
		assertNull(consumers);
		assertTrue(mockConsumersServlet.getPersistenceManager().isClosed());
	}

	@Test
	public void testGetConsumersIII() throws DataSourceException {
		final String qA = "a";
		final String qV = "b";
		mockConsumersServlet.setPersistenceManager(new MockPersistenceManager() {
			@SuppressWarnings("serial")
			public Query newQuery(String query) {
				assertNotSame(-1, query.indexOf(qA + " == '" + qV + "'"));
				return new MockQuery(){
					public Object execute() {
						// Whatever the query is, no Consumer instance match
						return new ArrayList<Consumer>();
					}
				};
			}
		});
		List<Consumer> consumers = mockConsumersServlet.getConsumers("a", "b");
		assertNotNull(consumers);
		assertEquals(0, consumers.size());
		assertTrue(mockConsumersServlet.getPersistenceManager().isClosed());
	}

	@Test
	public void testGetConsumersIV() throws DataSourceException {
		final String qA = "a";
		final String qV = "b";
		final Consumer selected = new Consumer();
		mockConsumersServlet.setPersistenceManager(new MockPersistenceManager() {
			@SuppressWarnings("serial")
			public Query newQuery(String query) {
				assertNotSame(-1, query.indexOf(qA + " == '" + qV + "'"));
				return new MockQuery(){
					public Object execute() {
						// Whatever the query is, one Consumer instance match
						List<Consumer> selection = new ArrayList<Consumer>();
						selection.add(selected);
						return selection;
					}
				};
			}
		});
		List<Consumer> consumers = mockConsumersServlet.getConsumers("a", "b");
		assertNotNull(consumers);
		assertEquals(1, consumers.size());
		assertEquals(selected, consumers.get(0));
		assertTrue(mockConsumersServlet.getPersistenceManager().isClosed());
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
		mockConsumersServlet.setPersistenceManager(new MockPersistenceManager() {
			@SuppressWarnings("serial")
			public Query newQuery(String query) {
				assertNotSame(-1, query.indexOf(qA + " == '" + qV + "'"));
				return new MockQuery(){
					public Object execute() {
						// Whatever the query is, one Consumer instance match
						List<Consumer> selection = new ArrayList<Consumer>();
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
		List<Consumer> consumers = mockConsumersServlet.getConsumers("a", "b");
		assertNotNull(consumers);
		assertEquals(5, consumers.size());
		assertEquals(selected, consumers.get(0));
		assertEquals(spare1, consumers.get(1));
		assertEquals(spare2, consumers.get(2));
		assertEquals(spare3, consumers.get(3));
		assertEquals(spare4, consumers.get(4));
		assertTrue(mockConsumersServlet.getPersistenceManager().isClosed());
	}

	@Test
	public void testGetConsumerI() throws DataSourceException {
		final String qA = "a";
		final String qV = "b";
		mockConsumersServlet.setPersistenceManager(new MockPersistenceManager() {
			public Query newQuery(String query) {
				assertNotSame(-1, query.indexOf(qA + " == '" + qV + "'"));
				return null;
			}
		});
		Consumer consumer = mockConsumersServlet.getConsumer("a", "b");
		assertNull(consumer);
		assertTrue(mockConsumersServlet.getPersistenceManager().isClosed());
	}

	@Test
	public void testGetConsumerII() throws DataSourceException {
		final String qA = "a";
		final String qV = "b";
		mockConsumersServlet.setPersistenceManager(new MockPersistenceManager() {
			@SuppressWarnings("serial")
			public Query newQuery(String query) {
				assertNotSame(-1, query.indexOf(qA + " == '" + qV + "'"));
				return new MockQuery(){
					public Object execute() {
						// Whatever the query is, no Consumer instance match
						return null;
					}
				};
			}
		});
		Consumer consumer = mockConsumersServlet.getConsumer("a", "b");
		assertNull(consumer);
		assertTrue(mockConsumersServlet.getPersistenceManager().isClosed());
	}

	@Test
	public void testGetConsumerIII() throws DataSourceException {
		final String qA = "a";
		final String qV = "b";
		mockConsumersServlet.setPersistenceManager(new MockPersistenceManager() {
			@SuppressWarnings("serial")
			public Query newQuery(String query) {
				assertNotSame(-1, query.indexOf(qA + " == '" + qV + "'"));
				return new MockQuery(){
					public Object execute() {
						// Whatever the query is, no Consumer instance match
						return new ArrayList<Consumer>();
					}
				};
			}
		});
		Consumer consumer = mockConsumersServlet.getConsumer("a", "b");
		assertNull(consumer);
		assertTrue(mockConsumersServlet.getPersistenceManager().isClosed());
	}

	@Test
	public void testGetConsumerIV() throws DataSourceException {
		final String qA = "a";
		final String qV = "b";
		final Consumer selected = new Consumer();
		mockConsumersServlet.setPersistenceManager(new MockPersistenceManager() {
			@SuppressWarnings("serial")
			public Query newQuery(String query) {
				assertNotSame(-1, query.indexOf(qA + " == '" + qV + "'"));
				return new MockQuery(){
					public Object execute() {
						// Whatever the query is, one Consumer instance match
						List<Consumer> selection = new ArrayList<Consumer>();
						selection.add(selected);
						return selection;
					}
				};
			}
		});
		Consumer consumer = mockConsumersServlet.getConsumer("a", "b");
		assertNotNull(consumer);
		assertEquals(selected, consumer);
		assertTrue(mockConsumersServlet.getPersistenceManager().isClosed());
	}

	@Test(expected=DataSourceException.class)
	public void testGetConsumerV() throws DataSourceException {
		final String qA = "a";
		final String qV = "b";
		final Consumer selected = new Consumer();
		final Consumer spare = new Consumer();
		mockConsumersServlet.setPersistenceManager(new MockPersistenceManager() {
			@SuppressWarnings("serial")
			public Query newQuery(String query) {
				assertNotSame(-1, query.indexOf(qA + " == '" + qV + "'"));
				return new MockQuery(){
					public Object execute() {
						// Whatever the query is, one Consumer instance match
						List<Consumer> selection = new ArrayList<Consumer>();
						selection.add(selected);
						selection.add(spare);
						return selection;
					}
				};
			}
		});
		mockConsumersServlet.getConsumer("a", "b");
		assertTrue(mockConsumersServlet.getPersistenceManager().isClosed());
	}
	
	@Test
	public void testCreateConsumerI() throws DataSourceException {
		mockConsumersServlet.setPersistenceManager(new MockPersistenceManager() {
			@SuppressWarnings("serial")
			public Query newQuery(String query) {
				return new MockQuery(){
					public Object execute() {
						// Whatever the query is, no Consumer instance match
						return null;
					}
				};
			}
		});
		User systemUser = new User("email", "domain");
		Consumer createdConsumer = mockConsumersServlet.createConsumer(systemUser);
		assertNotNull(createdConsumer);
		assertEquals(createdConsumer, ((MockPersistenceManager) mockConsumersServlet.getPersistenceManager()).getPersistedObject());
		assertEquals("email", createdConsumer.getEmail());
		assertEquals("email", createdConsumer.getName());
		assertEquals(systemUser, createdConsumer.getSystemUser());
		assertTrue(mockConsumersServlet.getPersistenceManager().isClosed());
	}
	
	@Test
	public void testCreateConsumerII() throws DataSourceException {
		final Consumer existingConsumer = new Consumer();
		User systemUser = new User("email", "domain");
		existingConsumer.setSystemUser(systemUser);
		mockConsumersServlet.setPersistenceManager(new MockPersistenceManager() {
			@SuppressWarnings("serial")
			public Query newQuery(String query) {
				return new MockQuery(){
					public Object execute() {
						List<Consumer> selection = new ArrayList<Consumer>();
						selection.add(existingConsumer);
						return selection;
					}
				};
			}
		});
		Consumer createdConsumer = mockConsumersServlet.createConsumer(systemUser);
		assertNotNull(createdConsumer);
		assertNull(((MockPersistenceManager) mockConsumersServlet.getPersistenceManager()).getPersistedObject());
		assertEquals(existingConsumer, createdConsumer);
		assertTrue(mockConsumersServlet.getPersistenceManager().isClosed());
	}
}
