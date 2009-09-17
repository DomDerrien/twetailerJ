package twetailer;

import java.text.ParseException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestSecurityException {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testConstructorI() {
        new SecurityException("test");
    }

    @Test
    public void testConstructorII() {
        new SecurityException("test", new IllegalArgumentException("test"));
    }

    @Test(expected=RuntimeException.class)
    public void testFromJson() throws ParseException {
        new SecurityException("test").fromJson(null);
    }

    @Test(expected=RuntimeException.class)
    public void testToJson() throws ParseException {
        new SecurityException("test").toJson();
    }
}
