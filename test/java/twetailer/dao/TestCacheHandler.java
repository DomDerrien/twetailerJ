package twetailer.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.util.Map;

import javamocks.util.logging.MockLogger;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.MockCache;
import javax.cache.MockCacheFactory;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.dto.Consumer;

public class TestCacheHandler {

    @BeforeClass
    public static void setUpBeforeClass() {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        CacheHandler.setMockLogger(new MockLogger(BaseOperations.class.getName(), null));
    }

    @After
    public void tearDown() throws Exception {
        CacheHandler.injectMockCacheFactory(null);
        CacheHandler.injectMockCache(null);
    }

    @Test
    public void testConstructorI() {
        new CacheHandler<Consumer>(Consumer.class.getName(), "key");
    }

    @Test
    public void testConstructorII() {
        new CacheHandler<Consumer>(Consumer.class.getName(), new String[] { "key", "name" });
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConstructorIII() {
        new CacheHandler<Consumer>(Consumer.class.getSimpleName(), new String[] { "key", "name" });
    }

    @Test
    public void testGetLogger() {
        assertNotNull(CacheHandler.getLogger());
    }

    @Test
    public void testGetCacheI() throws CacheException {
        assertNotNull(CacheHandler.getCache());
    }

    @Test
    public void testGetCacheII() throws CacheException {
        CacheHandler.injectMockCacheFactory(new MockCacheFactory());
        assertNotNull(CacheHandler.getCache());
    }

    @Test
    public void testGetCacheIII() throws CacheException {
        CacheHandler.injectMockCache(new MockCache(null));
        assertNotNull(CacheHandler.getCache());
    }

    @Test
    public void testClearCacheI() {
        CacheHandler.injectMockCache(new MockCache(null));
        CacheHandler.clearCache();
    }

    @Test
    public void testClearCacheII() {
        CacheHandler.injectMockCacheFactory(new MockCacheFactory() {
            @Override
            @SuppressWarnings("unchecked")
            public Cache createCache(Map configuration) throws CacheException {
                throw new CacheException();
            }
        });
        CacheHandler.clearCache();
    }

    @Test
    public void testGetFromCacheI() {
        CacheHandler.injectMockCache(new MockCache(null));
        assertNull(CacheHandler.getFromCache("1"));
    }

    @Test
    public void testGetFromCacheII() {
        CacheHandler.injectMockCacheFactory(new MockCacheFactory() {
            @Override
            @SuppressWarnings("unchecked")
            public Cache createCache(Map configuration) throws CacheException {
                throw new CacheException();
            }
        });
        CacheHandler.getFromCache("1");
    }

    @Test
    public void testSetInCacheI() {
        CacheHandler.injectMockCache(new MockCache(null));
        CacheHandler.setInCache("1", "2");
        assertEquals("2", CacheHandler.getFromCache("1"));
    }

    @Test
    public void testSetInCacheII() {
        CacheHandler.injectMockCache(new MockCache(null));
        CacheHandler.setInCache("1", "2");
        CacheHandler.setInCache("1", null);
        assertNull(CacheHandler.getFromCache("1"));
    }

    @Test
    public void testSetInCacheIII() {
        CacheHandler.injectMockCacheFactory(new MockCacheFactory() {
            @Override
            @SuppressWarnings("unchecked")
            public Cache createCache(Map configuration) throws CacheException {
                throw new CacheException();
            }
        });
        CacheHandler.setInCache("1", "2");
    }

    @Test
    public void testResetInCacheI() {
        CacheHandler.injectMockCache(new MockCache(null));
        CacheHandler.setInCache("1", "2");
        CacheHandler.resetInCache("1");
        assertNull(CacheHandler.getFromCache("1"));
    }

    @Test
    public void testCacheInstanceI() {
        CacheHandler.injectMockCache(new MockCache(null));
        CacheHandler<Consumer> cache = new CacheHandler<Consumer>(Consumer.class.getName(), new String[] { "key", "name", "unknown" });
        assertNull(cache.cacheInstance(null));
    }

    @Test
    public void testCacheInstanceII() {
        CacheHandler.injectMockCache(new MockCache(null));
        CacheHandler<Consumer> cache = new CacheHandler<Consumer>(Consumer.class.getName(), new String[] { "key", "name", "unknown" });
        Consumer consumer = new Consumer();
        consumer.setKey(12345L);
        assertEquals(consumer, cache.cacheInstance(consumer));
    }

    @Test
    public void testDecacheInstanceI() {
        CacheHandler.injectMockCache(new MockCache(null));
        CacheHandler<Consumer> cache = new CacheHandler<Consumer>(Consumer.class.getName(), new String[] { "key", "name", "unknown" });
        assertNull(cache.decacheInstance(null));
    }

    @Test
    public void testDecacheInstanceII() {
        CacheHandler.injectMockCache(new MockCache(null));
        CacheHandler<Consumer> cache = new CacheHandler<Consumer>(Consumer.class.getName(), new String[] { "key", "name", "unknown" });
        Consumer consumer = new Consumer();
        consumer.setKey(12345L);
        assertEquals(consumer, cache.decacheInstance(consumer));
    }

    @Test
    public void testGetInstanceI() {
        CacheHandler.injectMockCache(new MockCache(null));
        CacheHandler<Consumer> cache = new CacheHandler<Consumer>(Consumer.class.getName(), new String[] { "key", "name", "unknown" });
        Consumer consumer = new Consumer();
        consumer.setKey(12345L);
        assertEquals(consumer, cache.cacheInstance(consumer));
        assertNull(cache.getCachedInstance("key", null));
    }

    @Test
    public void testGetInstanceII() {
        CacheHandler.injectMockCache(new MockCache(null));
        CacheHandler<Consumer> cache = new CacheHandler<Consumer>(Consumer.class.getName(), new String[] { "key", "name", "unknown" });
        Consumer consumer = new Consumer();
        consumer.setKey(12345L);
        assertEquals(consumer, cache.cacheInstance(consumer));
        assertNull(cache.getCachedInstance("key", 0L));
        Consumer cached = cache.getCachedInstance("key", 12345L);
        assertNotNull(cached);
        assertNotSame(consumer, cached); // Not same as a new Consumer clone is generated by the CacheHandler
        assertEquals(consumer.getKey(), cached.getKey());
        assertEquals(consumer.getName(), cached.getName());
    }

    @Test
    public void testGetInstanceIII() {
        CacheHandler.injectMockCache(new MockCache(null));
        CacheHandler<Consumer> cache = new CacheHandler<Consumer>(Consumer.class.getName(), new String[] { "key", "name", "unknown" });
        Consumer consumer = new Consumer();
        consumer.setKey(12345L);
        assertEquals(consumer, cache.cacheInstance(consumer));
        CacheHandler.setInCache("twetailer.dto.Consumer_key_12345", "corruption" + CacheHandler.getFromCache("twetailer.dto.Consumer_key_12345"));
        assertNull(cache.getCachedInstance("key", 12345L));
    }
}

