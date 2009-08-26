package twetailer.j2ee;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.appengine.api.users.User;

public class TestDemandServlet {

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

    @Test
    public void testGetLogger() {
        (new DemandsServlet()).getLogger();
        assertTrue(true);
        assertNull(null);
    }

    /*
    @Test
    public void testCreateResource() {
        fail("Not yet implemented");
    }

    @Test
    public void testDeleteResource() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetResource() {
        fail("Not yet implemented");
    }

    @Test
    public void testSelectResources() {
        fail("Not yet implemented");
    }

    @Test
    public void testUpdateResource() {
        fail("Not yet implemented");
    }

    @Test
    public void testCreateDemand() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetDemands() {
        fail("Not yet implemented");
    }
    */
}
