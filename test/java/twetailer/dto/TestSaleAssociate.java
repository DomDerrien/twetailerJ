package twetailer.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.validator.LocaleValidator;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonParser;

public class TestSaleAssociate {

    private static LocalServiceTestHelper helper;
    private static Collator collator = LocaleValidator.getCollator(Locale.ENGLISH);

    @BeforeClass
    public static void setUpBeforeClass() {
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
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
        SaleAssociate object = new SaleAssociate();
        assertNull(object.getKey());
        assertNotNull(object.getCreationDate());
    }

    @Test
    public void testConstructorII() throws JsonException {
        SaleAssociate object = new SaleAssociate(new JsonParser("{}").getJsonObject());
        assertNull(object.getKey());
        assertNotNull(object.getCreationDate());
    }

    Long key = 756423765L;
    Long closedProposalNb = 6453222L;
    Long consumerKey = 67890L;
    Long creatorKey = 12345L;
    List<String> criteria = Arrays.asList(new String[] {"first", "second"});
    List<String> hashTags = Arrays.asList(new String[] {"top", "bottom"});
    Boolean isStoreAdmin = Boolean.TRUE;
    Long publishedProposalNb = 645645L;
    Long storeKey = 54321L;
    String score = "1:2.3";

    @Test
    public void testAccessors() {
        SaleAssociate object = new SaleAssociate();

        object.setKey(key);

        object.setClosedProposalNb(closedProposalNb);
        object.setConsumerKey(consumerKey);
        object.setCreatorKey(creatorKey);
        object.setCriteria(criteria, collator);
        object.setHashTags(hashTags);
        object.setIsStoreAdmin(isStoreAdmin);
        object.setPublishedProposalNb(publishedProposalNb);
        object.setStoreKey(storeKey);
        object.setScore(score);

        assertEquals(key, object.getKey());

        assertEquals(closedProposalNb, object.getClosedProposalNb());
        assertEquals(consumerKey, object.getConsumerKey());
        assertEquals(creatorKey, object.getCreatorKey());
        assertEquals(criteria, object.getCriteria());
        assertEquals(hashTags, object.getHashTags());
        assertEquals(isStoreAdmin, object.getIsStoreAdmin());
        assertEquals(publishedProposalNb, object.getPublishedProposalNb());
        assertEquals(storeKey, object.getStoreKey());
        assertEquals(score, object.getScore());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testResetCriteriaI() {
        SaleAssociate object = new SaleAssociate();

        object.addCriterion(null, collator);
        assertEquals(0, object.getCriteria().size());

        object.addCriterion("", collator);
        assertEquals(0, object.getCriteria().size());

        object.addCriterion("first", collator);
        assertEquals(1, object.getCriteria().size());

        object.addCriterion("first", collator); // Add it twice
        assertEquals(1, object.getCriteria().size());

        object.addCriterion("FiRsT", collator); // Add it twice, mixed case
        assertEquals(1, object.getCriteria().size());

        object.addCriterion("second", collator);
        assertEquals(2, object.getCriteria().size());

        object.removeCriterion("first", collator); // Remove first
        assertEquals(1, object.getCriteria().size());

        object.addCriterion("Troisième", collator);
        assertEquals(2, object.getCriteria().size());

        object.addCriterion("TROISIÈME", collator);
        assertEquals(2, object.getCriteria().size());

        object.removeCriterion("TROISIÈME", collator); // Remove mixed case and disparate accents
        assertEquals(1, object.getCriteria().size());

        object.removeCriterion(null, collator);
        assertEquals(1, object.getCriteria().size());

        object.removeCriterion("", collator);
        assertEquals(1, object.getCriteria().size());

        object.resetCriteria(); // Reset all
        assertEquals(0, object.getCriteria().size());

        object.setCriteria(null, collator); // Failure!
    }

    @Test
    public void testResetCriteriaII() {
        SaleAssociate object = new SaleAssociate();

        object.resetLists(); // To force the criteria list creation
        object.addCriterion("first", collator);
        assertEquals(1, object.getCriteria().size());

        object.resetLists(); // To be sure there's no error
        object.removeCriterion("first", collator); // Remove first

        object.resetLists(); // To be sure there's no error
        object.resetCriteria(); // Reset all
    }

    @Test
    public void testJsonCommandsI() {
        //
        // Cache related copy (highest)
        //
        SaleAssociate object = new SaleAssociate();

        object.setKey(key);

        object.setClosedProposalNb(closedProposalNb);
        object.setConsumerKey(consumerKey);
        object.setCreatorKey(creatorKey);
        object.setCriteria(criteria, collator);
        object.setHashTags(hashTags);
        object.setIsStoreAdmin(isStoreAdmin);
        object.setPublishedProposalNb(publishedProposalNb);
        object.setStoreKey(storeKey);
        object.setScore(score);

        SaleAssociate clone = new SaleAssociate();
        clone.fromJson(object.toJson(), true, true);

        assertEquals(key, clone.getKey());

        assertEquals(closedProposalNb, clone.getClosedProposalNb());
        assertEquals(consumerKey, clone.getConsumerKey());
        assertEquals(creatorKey, clone.getCreatorKey());
        assertEquals(object.getCriteria().size(), clone.getCriteria().size());
        for (int idx = 0; idx < object.getCriteria().size(); idx++) {
            assertEquals(object.getCriteria().get(idx), clone.getCriteria().get(idx));
        }
        assertEquals(object.getHashTags().size(), clone.getHashTags().size());
        for (int idx = 0; idx < object.getHashTags().size(); idx++) {
            assertEquals(object.getHashTags().get(idx), clone.getHashTags().get(idx));
        }
        assertEquals(isStoreAdmin, clone.getIsStoreAdmin());
        assertEquals(publishedProposalNb, clone.getPublishedProposalNb());
        assertEquals(storeKey, clone.getStoreKey());
        assertEquals(score, clone.getScore());
    }

    @Test
    public void testJsonCommandsII() {
        //
        // Cache related copy (highest) but with no data transfered
        //
        SaleAssociate object = new SaleAssociate();

        SaleAssociate clone = new SaleAssociate();
        clone.fromJson(object.toJson(), true, true);

        assertEquals(0L, clone.getClosedProposalNb().longValue());
        assertNull(clone.getConsumerKey());
        assertNull(clone.getCreatorKey());
        assertEquals(0, clone.getCriteria().size());
        assertEquals(0, clone.getHashTags().size());
        assertFalse(clone.getIsStoreAdmin());
        assertEquals(0L, clone.getPublishedProposalNb().longValue());
        assertNull(clone.getStoreKey());
        assertEquals(SaleAssociate.DEFAULT_SCORE, clone.getScore());
    }

    @Test
    public void testJsonCommandsIIIa() {
        //
        // Admin update (middle)
        //
        SaleAssociate object = new SaleAssociate();

        object.setKey(key);

        object.setClosedProposalNb(closedProposalNb);
        object.setConsumerKey(consumerKey);
        object.setCreatorKey(creatorKey);
        // object.setCriteria(criteria, collator);
        object.setHashTags(hashTags);
        object.setIsStoreAdmin(isStoreAdmin);
        object.setPublishedProposalNb(publishedProposalNb);
        object.setStoreKey(storeKey);
        object.setScore(score);

        SaleAssociate clone = new SaleAssociate();
        clone.fromJson(object.toJson(), true, false);

        assertEquals(key, clone.getKey());

        assertEquals(closedProposalNb, clone.getClosedProposalNb());
        assertEquals(consumerKey, clone.getConsumerKey());
        assertEquals(creatorKey, clone.getCreatorKey());
        assertEquals(0, clone.getCriteria().size());
        // assertEquals(object.getCriteria().size(), clone.getCriteria().size());
        // for (int idx = 0; idx < object.getCriteria().size(); idx++) {
        //     assertEquals(object.getCriteria().get(idx), clone.getCriteria().get(idx));
        // }
        assertEquals(object.getHashTags().size(), clone.getHashTags().size());
        for (int idx = 0; idx < object.getHashTags().size(); idx++) {
            assertEquals(object.getHashTags().get(idx), clone.getHashTags().get(idx));
        }
        assertEquals(isStoreAdmin, clone.getIsStoreAdmin());
        assertEquals(publishedProposalNb, clone.getPublishedProposalNb());
        assertEquals(storeKey, clone.getStoreKey());
        assertEquals(score, clone.getScore());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testJsonCommandsIIIb() {
        //
        // Admin update (middle)
        //
        SaleAssociate object = new SaleAssociate();

        object.setCriteria(criteria, collator);

        SaleAssociate clone = new SaleAssociate();
        clone.fromJson(object.toJson(), true, false);
    }

    @Test
    public void testJsonCommandsIV() {
        //
        // User update for a new object (lower)
        //
        SaleAssociate object = new SaleAssociate();

        // object.setKey(key);

        object.setClosedProposalNb(closedProposalNb);
        object.setConsumerKey(consumerKey);
        object.setCreatorKey(creatorKey);
        // object.setCriteria(criteria, collator);
        object.setHashTags(hashTags);
        object.setIsStoreAdmin(isStoreAdmin);
        object.setPublishedProposalNb(publishedProposalNb);
        object.setStoreKey(storeKey);
        object.setScore(score);

        SaleAssociate clone = new SaleAssociate();
        clone.fromJson(object.toJson());

        assertNull(clone.getClosedProposalNb());
        assertEquals(consumerKey, clone.getConsumerKey());
        assertEquals(creatorKey, clone.getCreatorKey());
        assertEquals(0, clone.getCriteria().size());
        // assertEquals(object.getCriteria().size(), clone.getCriteria().size());
        // for (int idx = 0; idx < object.getCriteria().size(); idx++) {
        //     assertEquals(object.getCriteria().get(idx), clone.getCriteria().get(idx));
        // }
        assertEquals(object.getHashTags().size(), clone.getHashTags().size());
        for (int idx = 0; idx < object.getHashTags().size(); idx++) {
            assertEquals(object.getHashTags().get(idx), clone.getHashTags().get(idx));
        }
        assertFalse(clone.getIsStoreAdmin());
        assertNull(clone.getPublishedProposalNb());
        assertNull(clone.getStoreKey());
        assertEquals(score, clone.getScore());
    }

    @Test
    public void testJsonCommandsV() {
        //
        // User update for an existing object (lowest)
        //
        SaleAssociate object = new SaleAssociate();

        object.setKey(key);

        object.setClosedProposalNb(closedProposalNb);
        object.setConsumerKey(consumerKey);
        object.setCreatorKey(creatorKey);
        // object.setCriteria(criteria, collator);
        object.setHashTags(hashTags);
        object.setIsStoreAdmin(isStoreAdmin);
        object.setPublishedProposalNb(publishedProposalNb);
        object.setStoreKey(storeKey);
        object.setScore(score);

        SaleAssociate clone = new SaleAssociate();
        clone.fromJson(object.toJson());

        assertNull(clone.getClosedProposalNb());
        assertNull(clone.getConsumerKey());
        assertNull(clone.getCreatorKey());
        assertEquals(0, clone.getCriteria().size());
        // assertEquals(object.getCriteria().size(), clone.getCriteria().size());
        // for (int idx = 0; idx < object.getCriteria().size(); idx++) {
        //     assertEquals(object.getCriteria().get(idx), clone.getCriteria().get(idx));
        // }
        assertEquals(object.getHashTags().size(), clone.getHashTags().size());
        for (int idx = 0; idx < object.getHashTags().size(); idx++) {
            assertEquals(object.getHashTags().get(idx), clone.getHashTags().get(idx));
        }
        assertFalse(clone.getIsStoreAdmin());
        assertNull(clone.getPublishedProposalNb());
        assertNull(clone.getStoreKey());
        assertEquals(score, clone.getScore());
    }

    @Test
    public void testJsonCommandsVI() {
        JsonObject json = new GenericJsonObject();

        SaleAssociate object = new SaleAssociate();
        object.fromJson(json, true, false);
    }

    /**** ddd
    @Test
    public void testJsonCommandsII() {
        SaleAssociate object = new SaleAssociate();

        object.resetLists();

        assertNull(object.getCriteria());
        assertNull(object.getLocationKey());

        SaleAssociate clone = new SaleAssociate(object.toJson());

        assertEquals(0, clone.getCriteria().size()); // Not null because the clone object creation creates empty List<String>
        assertNull(clone.getLocationKey());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testJsonCommandsIII() {
        SaleAssociate object = new SaleAssociate();

        object.setCriteria(criteria, collator); // Supplied keywords must be transfered manually

        new SaleAssociate(object.toJson());
    }
    **** ddd ****/

    @Test
    public void testShortcut() {
        Long key = 12345L;
        JsonObject parameters = new GenericJsonObject();
        parameters.put(SaleAssociate.SALEASSOCIATE_KEY, key);

        assertEquals(key, new SaleAssociate(parameters).getKey());
    }

    @Test
    public void testGetSerialized() {
        SaleAssociate saleAssociate = new SaleAssociate();
        saleAssociate.addCriterion("one", collator);
        saleAssociate.addCriterion("two", collator);
        saleAssociate.addCriterion("three", collator);

        assertEquals("one two three", saleAssociate.getSerializedCriteria());
    }

    @Test
    public void testGetIsStoreAdmin() {
        SaleAssociate saleAssociate = new SaleAssociate();
        assertFalse(saleAssociate.getIsStoreAdmin());
        saleAssociate.setIsStoreAdmin(null);
        assertFalse(saleAssociate.getIsStoreAdmin());
        saleAssociate.setIsStoreAdmin(false);
        assertFalse(saleAssociate.getIsStoreAdmin());
        saleAssociate.setIsStoreAdmin(true);
        assertTrue(saleAssociate.getIsStoreAdmin());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetConsumerKey() {
        new SaleAssociate().setConsumerKey(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetCreatorKey() {
        new SaleAssociate().setCreatorKey(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetHashTags() {
        new SaleAssociate().setHashTags(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetStoreKey() {
        new SaleAssociate().setStoreKey(null);
    }

    @Test
    public void testSetScore() {
        SaleAssociate sa = new SaleAssociate();
        sa.setScore(score);
        assertEquals(score, sa.getScore());
        sa.setScore(null);
        assertEquals(SaleAssociate.DEFAULT_SCORE, sa.getScore());
    }

    @Test
    public void testGetDefaultSerializedCriteriaI() {
        String defaultValue = "default";
        assertEquals(defaultValue, new SaleAssociate().getSerializedCriteria(defaultValue));
    }

    @Test
    public void testGetDefaultSerializedCriteriaII() {
        String defaultValue = "default";
        assertEquals(defaultValue, new SaleAssociate().resetLists().getSerializedCriteria(defaultValue));
    }

    @Test
    public void testManageCriteria() {
        String defaultValue = "default";

        SaleAssociate saleAssociate1 = new SaleAssociate().resetLists();
        saleAssociate1.addCriterion("a", collator);
        saleAssociate1.addCriterion(null, collator);
        saleAssociate1.addCriterion("", collator);
        saleAssociate1.addCriterion("b", collator);
        saleAssociate1.addCriterion("a", collator);
        assertEquals("a b", saleAssociate1.getSerializedCriteria(defaultValue));

        SaleAssociate saleAssociate2 = new SaleAssociate();
        saleAssociate2.setCriteria(saleAssociate1.getCriteria(), collator); // Real cloning
        assertEquals(2, saleAssociate2.getCriteria().size());

        saleAssociate1.removeCriterion("a", collator);
        saleAssociate1.removeCriterion(null, collator);
        saleAssociate1.removeCriterion("", collator);
        saleAssociate1.removeCriterion("c", collator);

        assertEquals(2, saleAssociate2.getCriteria().size()); // Size preserved because the list has been cloned
        assertEquals(1, saleAssociate1.getCriteria().size());
        assertEquals("b", saleAssociate1.getSerializedCriteria(defaultValue));

        new SaleAssociate().resetLists().removeCriterion("z", collator); // No issue reported
    }

    @Test
    public void testGetDefaultSerializedHashTagsI() {
        String defaultValue = "default";
        assertEquals(defaultValue, new SaleAssociate().getSerializedHashTags(defaultValue));
    }

    @Test
    public void testGetDefaultSerializedHashTagsII() {
        String defaultValue = "default";
        assertEquals(defaultValue, new SaleAssociate().resetLists().getSerializedHashTags(defaultValue));
    }

    @Test
    public void testGetDefaultSerializedHashTagsIII() {
        String defaultValue = "default";
        SaleAssociate saleAssociate = new SaleAssociate();
        saleAssociate.addHashTag("a");
        assertEquals("#a", saleAssociate.getSerializedHashTags(defaultValue));
    }

    @Test
    public void testManageHashTags() {
        String defaultValue = "default";

        SaleAssociate saleAssociate1 = new SaleAssociate().resetLists();
        saleAssociate1.addHashTag("a");
        saleAssociate1.addHashTag(null);
        saleAssociate1.addHashTag("");
        saleAssociate1.addHashTag("b");
        saleAssociate1.addHashTag("a");
        assertEquals("#a #b", saleAssociate1.getSerializedHashTags(defaultValue));

        SaleAssociate saleAssociate2 = new SaleAssociate();
        saleAssociate2.setHashTags(saleAssociate1.getHashTags()); // Copying reference
        assertEquals(2, saleAssociate2.getHashTags().size());

        saleAssociate1.removeHashTag("a");
        saleAssociate1.removeHashTag(null);
        saleAssociate1.removeHashTag("");
        saleAssociate1.removeHashTag("c");

        assertEquals(1, saleAssociate2.getHashTags().size());
        assertEquals("#b", saleAssociate2.getSerializedHashTags(defaultValue));

        new SaleAssociate().resetLists().removeHashTag("z"); // No issue reported
    }

    @Test
    public void testResetHashTags() {
        assertEquals(0, new SaleAssociate().resetHashTags().getCriteria().size());
        assertNull(new SaleAssociate().resetLists().resetHashTags().getCriteria());
    }

    @Test
    public void testToJson() {
        SaleAssociate associate = new SaleAssociate();
        associate.setConsumerKey(543543L);
        associate.setCreatorKey(548762L);
        associate.setStoreKey(765453543L);

        JsonObject json = associate.toJson();
        assertFalse(json.containsKey(SaleAssociate.CRITERIA));
        assertFalse(json.containsKey(SaleAssociate.HASH_TAGS));

        json = associate.resetLists().toJson();
        assertFalse(json.containsKey(SaleAssociate.CRITERIA));
        assertFalse(json.containsKey(SaleAssociate.HASH_TAGS));
    }

    @Test
    public void testFromJsonI() {
        JsonObject json = new GenericJsonObject();

        json.put(Entity.KEY, 4354L);
        json.put(SaleAssociate.CONSUMER_KEY, 6546L);
        json.put(SaleAssociate.CREATOR_KEY, 6546L);

        SaleAssociate associate = new SaleAssociate(json);
        assertNull(associate.getConsumerKey());
        assertNull(associate.getCreatorKey());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testFromJsonII() {
        JsonObject json = new GenericJsonObject();

        json.put(SaleAssociate.CRITERIA, "not important");

        new SaleAssociate(json);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testFromJsonIII() {
        JsonObject json = new GenericJsonObject();

        json.put(SaleAssociate.CRITERIA_ADD, "not important");

        new SaleAssociate(json);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testFromJsonIV() {
        JsonObject json = new GenericJsonObject();

        json.put(SaleAssociate.CRITERIA_REMOVE, "not important");

        new SaleAssociate(json);
    }

    @Test
    public void testHashTagJsonII() {
        SaleAssociate associate = new SaleAssociate();
        associate.addHashTag("one");
        associate.addHashTag("two");
        associate.addHashTag("three");

        JsonObject json = new GenericJsonObject();
        json.put(SaleAssociate.HASH_TAGS_ADD, new GenericJsonArray());
        json.getJsonArray(SaleAssociate.HASH_TAGS_ADD).add("four");
        json.getJsonArray(SaleAssociate.HASH_TAGS_ADD).add("five");

        json.put(SaleAssociate.HASH_TAGS_REMOVE, new GenericJsonArray());
        json.getJsonArray(SaleAssociate.HASH_TAGS_REMOVE).add("two");
        json.getJsonArray(SaleAssociate.HASH_TAGS_REMOVE).add("four");

        associate.fromJson(json);

        assertEquals(3, associate.getHashTags().size());
    }
}
