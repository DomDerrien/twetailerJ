package twetailer.dao;

import static org.junit.Assert.assertNotNull;
import javamocks.util.logging.MockLogger;

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

//    private static LocalServiceTestHelper  helper;

    @BeforeClass
    public static void setUpBeforeClass() {
//        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
//        BaseSteps.resetOperationControllers(true);
        CacheHandler.setLogger(new MockLogger(BaseOperations.class.getName(), null));
//        helper.setUp();
    }

    @After
    public void tearDown() throws Exception {
//        helper.tearDown();
        CacheHandler.injectCacheFactory(null);
        CacheHandler.injectCache(null);
    }

    @Test
    public void testConstructorI() {
        new CacheHandler<Consumer>(Consumer.class.getName(), "key");
    }

    @Test
    public void testConstructorII() {
        new CacheHandler<Consumer>(Consumer.class.getName(), new String[] { "key", "name" });
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
        CacheHandler.injectCacheFactory(new MockCacheFactory());
        assertNotNull(CacheHandler.getCache());
    }

    @Test
    public void testGetCacheIII() throws CacheException {
        CacheHandler.injectCache(new MockCache(null));
        assertNotNull(CacheHandler.getCache());
    }
}