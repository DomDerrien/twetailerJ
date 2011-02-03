package twetailer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.validator.CommandSettings.Action;

public class TestReservedOperationException {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testConstructorI() {
        new ReservedOperationException("test");
    }

    @Test
    public void testConstructorII() {
        new ReservedOperationException("test", new IllegalArgumentException("test"));
    }

    @Test
    public void testConstructorIIIa() {
        Exception ex = new ReservedOperationException(Action.list, "test", new IllegalArgumentException("test"));
        assertTrue(ex.getMessage().contains(Action.list.toString()));
    }

    @Test
    public void testConstructorIIIb() {
        Exception ex = new ReservedOperationException(Action.list, null, new IllegalArgumentException("test"));
        assertTrue(ex.getMessage().contains(Action.list.toString()));
    }

    @Test
    public void testConstructorIVa() {
        new ReservedOperationException(Action.list, "test");
    }

    @Test
    public void testConstructorIVb() {
        new ReservedOperationException(Action.list, null);
    }

    @Test(expected=RuntimeException.class)
    public void testFromJson() throws ParseException {
        new ReservedOperationException("test").fromJson(null);
    }

    @Test(expected=RuntimeException.class)
    public void testToJson() throws ParseException {
        new ReservedOperationException("test").toJson();
    }

    @Test
    public void testAccessorI() {
        ReservedOperationException ex = new ReservedOperationException(Action.list, "test");
        assertEquals(Action.list, ex.getAction());
        assertEquals("test", ex.getEntityClassName());
    }
}
