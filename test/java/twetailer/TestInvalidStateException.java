package twetailer;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.validator.CommandSettings.State;

public class TestInvalidStateException {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testConstructorI() {
        new InvalidStateException("test");
    }

    @Test
    public void testConstructorII() {
        new InvalidStateException("test", new IllegalArgumentException("test"));
    }

    @Test
    public void testConstructorIII() {
        InvalidStateException ex = new InvalidStateException("test", State.opened.toString(), State.invalid.toString());
        assertEquals(State.opened.toString(), ex.getEntityState());
        assertEquals(State.invalid.toString(), ex.getProposedState());
    }

    @Test(expected=RuntimeException.class)
    public void testFromJson() throws ParseException {
        new InvalidStateException("test").fromJson(null);
    }

    @Test(expected=RuntimeException.class)
    public void testToJson() throws ParseException {
        new InvalidStateException("test").toJson();
    }
}
