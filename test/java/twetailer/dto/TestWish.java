package twetailer.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.connector.BaseConnector.Source;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.State;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import domderrien.i18n.DateUtils;
import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonParser;

public class TestWish {

    private static LocalServiceTestHelper  helper;

    @BeforeClass
    public static void setUpBeforeClass() {
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());;
    }

    @Before
    public void setUp() throws Exception {
        helper.setUp();
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();
    }

    @Test
    public void testConstructorI() {
        Wish object = new Wish();
        assertNull(object.getKey());
        assertNotNull(object.getCreationDate());
    }

    @Test
    public void testConstructorII() throws JsonException {
        Wish object = new Wish(new JsonParser("{}").getJsonObject());
        assertNull(object.getKey());
        assertNotNull(object.getCreationDate());
    }

    Action action = Action.cancel;
    Long ownerKey = 12345L;
    State state = State.closed;
    Source source = Source.simulated;

    List<String> criteria = new ArrayList<String>(Arrays.asList(new String[] {"first", "second"}));
    Date expirationDate = new Date(new Date().getTime() + 65536L);
    Long quantity = 15L;

    @Test
    public void testAccessors() {
        Wish object = new Wish();

        // Command
        object.setAction(action);
        object.setAction(action.toString());
        object.setOwnerKey(ownerKey);
        object.setState(state);
        object.setState(state.toString());
        object.setSource(source);
        object.setSource(source.toString());

        // Wish
        object.setCriteria(criteria);
        object.setExpirationDate(expirationDate);
        object.setQuantity(quantity);

        // Command
        assertEquals(action, object.getAction());
        assertEquals(action, object.getAction());
        assertEquals(ownerKey, object.getOwnerKey());
        assertEquals(state, object.getState());
        assertEquals(source, object.getSource());

        // Wish
        assertEquals(criteria, object.getCriteria());
        assertEquals(expirationDate, object.getExpirationDate());
        assertEquals(quantity, object.getQuantity());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testResetCriteriaI() {
        Wish object = new Wish();

        object.addCriterion("first");
        assertEquals(1, object.getCriteria().size());

        object.addCriterion("first"); // Add it twice
        assertEquals(1, object.getCriteria().size());

        object.addCriterion("second");
        assertEquals(2, object.getCriteria().size());

        object.removeCriterion("first"); // Remove first
        assertEquals(1, object.getCriteria().size());

        object.resetCriteria(); // Reset all
        assertEquals(0, object.getCriteria().size());

        object.setCriteria(null); // Failure!
    }

    @Test
    public void testResetCriteriaII() {
        Wish object = new Wish();

        object.resetLists(); // To force the criteria list creation
        object.addCriterion("first");
        assertEquals(1, object.getCriteria().size());

        object.resetLists(); // To be sure there's no error
        object.removeCriterion("first"); // Remove first

        object.resetLists(); // To be sure there's no error
        object.resetCriteria(); // Reset all
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetAction() {
        Wish object = new Wish();

        object.setAction((Action) null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testResetExpirationDate() {
        Wish object = new Wish();

        object.setExpirationDate(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetExpirationDateInPast() {
        Wish object = new Wish();

        object.setExpirationDate(new Date(12345L));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetSource() {
        Wish object = new Wish();

        object.setSource((Source) null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetState() {
        Wish object = new Wish();

        object.setState((State) null);
    }

    @Test
    public void testJsonWishsI() {
        Wish object = new Wish();

        // Command
        object.setAction(action);
        object.setOwnerKey(ownerKey);
        object.setState(state);
        object.setSource(source);

        // Wish
        object.setCriteria(criteria);
        object.setExpirationDate(expirationDate);
        object.setQuantity(quantity);

        Wish clone = new Wish(object.toJson());

        // Command
        assertEquals(action, clone.getAction());
        assertEquals(ownerKey, clone.getOwnerKey());
        assertEquals(state, clone.getState());
        assertEquals(source, clone.getSource());

        // Wish
        assertEquals(criteria, clone.getCriteria());
        assertEquals(DateUtils.dateToISO(expirationDate), DateUtils.dateToISO(clone.getExpirationDate()));
        assertEquals(quantity, clone.getQuantity());
    }

    @Test
    public void testJsonWishsII() {
        Wish object = new Wish();
        object.setSource(source);

        // Command
        assertNull(object.getOwnerKey());

        // Wish
        assertEquals(0, object.getCriteria().size());

        Wish clone = new Wish(object.toJson());

        // Command
        assertNull(clone.getOwnerKey());

        // Wish
        assertEquals(0, clone.getCriteria().size());
    }

    @Test
    public void testJsonWishsIII() {
        Wish object = new Wish();
        object.setSource(source);

        object.resetLists();

        // Wish
        assertNull(object.getCriteria());

        Wish clone = new Wish(object.toJson());

        // Wish
        assertEquals(0, clone.getCriteria().size()); // Not null because the clone object creation creates empty List<String>
    }

    @Test
    public void testInvalidDateFormat() throws JsonException {
        Wish object = new Wish();
        Date date = object.getExpirationDate();

        object.fromJson(new JsonParser("{'" + Wish.EXPIRATION_DATE + "':'2009-01-01Tzzz'}").getJsonObject());

        assertEquals(DateUtils.dateToISO(date), DateUtils.dateToISO(object.getExpirationDate())); // Corrupted date did not alter the original date
    }
}
