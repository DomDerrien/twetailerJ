package twetailer;

import java.text.ParseException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestClientException {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testConstructorI() {
        new ClientException("test");
    }

    @Test
    public void testConstructorII() {
        new ClientException("test", new IllegalArgumentException("test"));
    }

    @Test(expected=RuntimeException.class)
    public void testFromJson() throws ParseException {
        new ClientException("test").fromJson(null);
    }

    @Test(expected=RuntimeException.class)
    public void testToJson() throws ParseException {
        new ClientException("test").toJson();
    }
}
