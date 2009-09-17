package twetailer.j2ee;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.api.users.User;

public class TestMaezelRestlet {

    static final User user = new User("test-email", "test-domain");
    MaezelRestlet ops;

    @Before
    public void setUp() throws Exception {
        ops = new MaezelRestlet();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testConstructor() {
        new MaezelRestlet();
    }
}
