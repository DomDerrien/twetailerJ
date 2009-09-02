package twetailer;

import java.text.ParseException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestDataSourceException {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testConstructorI() {
        new DataSourceException("test");
    }

    @Test
    public void testConstructorII() {
        new DataSourceException("test", new IllegalArgumentException("test"));
    }

    @Test(expected=RuntimeException.class)
    public void testFromJson() throws ParseException {
        new DataSourceException("test").fromJson(null);
    }

    @Test(expected=RuntimeException.class)
    public void testToJson() throws ParseException {
        new DataSourceException("test").toJson();
    }
}