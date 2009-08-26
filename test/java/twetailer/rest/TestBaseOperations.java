package twetailer.rest;

import java.io.IOException;
import java.util.logging.Logger;

import domderrien.mocks.MockLogger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.appengine.api.users.User;
import twetailer.j2ee.MockUserService;
import twetailer.j2ee.Utils;

public class TestBaseOperations {

	class MockBaseOperations extends BaseOperations {
        @Override
        protected Logger getLogger() {
            return new MockLogger(MockBaseOperations.class.getName(), null);
        }
	}

	static final User user = new User("test-email", "test-domain");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Utils.setUserService(new MockUserService(){
            @Override
            public User getCurrentUser() {
                return user;
            }
        });
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

    @Test(expected=java.lang.NoClassDefFoundError.class)
    public void testGetPersistenceManager() throws IOException {
        new MockBaseOperations().getPersistenceManager();
    }
}
