package twetailer;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.connector.BaseConnector.Source;

public class TestCommunicationException {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testConstructorI() {
        new CommunicationException("test");
    }

    @Test
    public void testConstructorII() {
        new CommunicationException("test", new IllegalArgumentException("test"));
    }

    @Test
    public void testConstructorIII() {
        CommunicationException ex = new CommunicationException("test", Source.api);
        assertEquals(Source.api, ex.getSource());
    }

    @Test
    public void testConstructorIV() {
        CommunicationException ex = new CommunicationException("test", Source.simulated, new IllegalArgumentException("test"));
        assertEquals(Source.simulated, ex.getSource());
    }

    @Test(expected=RuntimeException.class)
    public void testFromJson() throws ParseException {
        new CommunicationException("test").fromJson(null);
    }

    @Test(expected=RuntimeException.class)
    public void testToJson() throws ParseException {
        new CommunicationException("test").toJson();
    }
}
