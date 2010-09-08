package twetailer.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javamocks.util.logging.MockLogger;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.connector.BaseConnector.Source;
import twetailer.connector.MessageGenerator.MessageId;
import twetailer.dto.Command;
import twetailer.dto.Demand;
import twetailer.dto.Entity;
import twetailer.dto.Location;
import twetailer.dto.Proposal;
import twetailer.dto.Store;
import twetailer.dto.HashTag.RegisteredHashTag;
import domderrien.i18n.DateUtils;
import domderrien.i18n.LabelExtractor;
import domderrien.i18n.LabelExtractor.ResourceFileId;
import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.JsonArray;

public class TestMessageGenerator {

    @BeforeClass
    public static void setUpBeforeClass() {
        BaseConnector.setLogger(new MockLogger("test", null));
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testConstructor() {
        new MessageGenerator(Source.simulated, null, Locale.ENGLISH);
    }

    @Test
    public void testGetChannelPrefixI() {
        assertEquals(MessageGenerator.LONG_MESSAGE_PREFIX, MessageGenerator.getChannelPrefix(Source.mail));
    }

    @Test
    public void testGetChannelPrefixIIa() {
        assertEquals(MessageGenerator.SHORT_MESSAGE_PREFIX, MessageGenerator.getChannelPrefix(Source.jabber));
    }

    @Test
    public void testGetChannelPrefixIIb() {
        assertEquals(MessageGenerator.SHORT_MESSAGE_PREFIX, MessageGenerator.getChannelPrefix(Source.twitter));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetChannelPrefixIII() {
        MessageGenerator.getChannelPrefix(Source.api);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetChannelPrefixIV() {
        MessageGenerator.getChannelPrefix(Source.robot);
    }

    @Test
    public void testGetVerticalPrefixI() {
        assertEquals(MessageGenerator.DEFAULT_VERTICAL_PREFIX, MessageGenerator.getVerticalPrefix(null));
    }

    @Test
    public void testGetVerticalPrefixII() {
        assertEquals(MessageGenerator.DEFAULT_VERTICAL_PREFIX, MessageGenerator.getVerticalPrefix(new ArrayList<String>()));
    }

    @Test
    public void testGetVerticalPrefixIII() {
        List<String> hashTags = new ArrayList<String>();
        hashTags.add("zzz");
        assertEquals(MessageGenerator.DEFAULT_VERTICAL_PREFIX, MessageGenerator.getVerticalPrefix(hashTags));
    }

    @Test
    public void testGetVerticalPrefixIV() {
        List<String> hashTags = new ArrayList<String>();
        hashTags.add(RegisteredHashTag.golf.toString());
        assertEquals(MessageGenerator.GOLF_VERTICAL_PREFIX, MessageGenerator.getVerticalPrefix(hashTags));
    }

    @Test
    public void testStaticGetMessage() {
        assertEquals(
                LabelExtractor.get(ResourceFileId.fourth, "long_core_emptyListIndicator", Locale.ENGLISH),
                MessageGenerator.getMessage(Source.mail, null, MessageId.emptyListIndicator, null, Locale.ENGLISH)
        );
    }

    @Test
    public void testSerializeDate() {
        MessageGenerator msgGen = new MessageGenerator(Source.jabber, null, Locale.ENGLISH); // Jabber for the short date format
        Calendar date = DateUtils.getNowCalendar();

        date.set(Calendar.HOUR_OF_DAY, 1); date.set(Calendar.MINUTE, 2); date.set(Calendar.SECOND, 3);
        assertTrue(msgGen.serializeDate(date.getTime(), Locale.ENGLISH).contains("T01:02:03"));
        date.set(Calendar.SECOND, 59);     assertTrue(msgGen.serializeDate(date.getTime(), Locale.ENGLISH).contains("T01:02:59"));
        date.set(Calendar.MINUTE, 59);     assertTrue(msgGen.serializeDate(date.getTime(), Locale.ENGLISH).contains("T01:59:59"));
        date.set(Calendar.HOUR, 23);       assertFalse(msgGen.serializeDate(date.getTime(), Locale.ENGLISH).contains("T23:59:59"));

        date.set(Calendar.HOUR_OF_DAY, 1); date.set(Calendar.MINUTE, 2); date.set(Calendar.SECOND, 3);
        assertTrue(msgGen.serializeDate(date.getTime(), Locale.ENGLISH).contains("T01:02:03"));
        date.set(Calendar.MINUTE, 59);     assertTrue(msgGen.serializeDate(date.getTime(), Locale.ENGLISH).contains("T01:59:03"));
        date.set(Calendar.SECOND, 59);     assertTrue(msgGen.serializeDate(date.getTime(), Locale.ENGLISH).contains("T01:59:59"));
        date.set(Calendar.HOUR, 23);       assertFalse(msgGen.serializeDate(date.getTime(), Locale.ENGLISH).contains("T23:59:59"));

        date.set(Calendar.HOUR_OF_DAY, 1); date.set(Calendar.MINUTE, 2); date.set(Calendar.SECOND, 3);
        assertTrue(msgGen.serializeDate(date.getTime(), Locale.ENGLISH).contains("T01:02:03"));
        date.set(Calendar.MINUTE, 59);     assertTrue(msgGen.serializeDate(date.getTime(), Locale.ENGLISH).contains("T01:59:03"));
        date.set(Calendar.HOUR, 23);       assertTrue(msgGen.serializeDate(date.getTime(), Locale.ENGLISH).contains("T23:59:03"));
        date.set(Calendar.SECOND, 59);     assertFalse(msgGen.serializeDate(date.getTime(), Locale.ENGLISH).contains("T23:59:59"));

        date.set(Calendar.HOUR_OF_DAY, 1); date.set(Calendar.MINUTE, 2); date.set(Calendar.SECOND, 3);
        assertTrue(msgGen.serializeDate(date.getTime(), Locale.ENGLISH).contains("T01:02:03"));
        date.set(Calendar.SECOND, 59);     assertTrue(msgGen.serializeDate(date.getTime(), Locale.ENGLISH).contains("T01:02:59"));
        date.set(Calendar.HOUR, 23);       assertTrue(msgGen.serializeDate(date.getTime(), Locale.ENGLISH).contains("T23:02:59"));
        date.set(Calendar.MINUTE, 59);     assertFalse(msgGen.serializeDate(date.getTime(), Locale.ENGLISH).contains("T23:59:59"));
    }

    @Test
    public void testDynamicGetMessage() {
        MessageGenerator msgGen = new MessageGenerator(Source.mail, null, Locale.ENGLISH);
        assertEquals(
                LabelExtractor.get(ResourceFileId.fourth, "long_core_emptyListIndicator", Locale.ENGLISH),
                msgGen.getMessage(MessageId.emptyListIndicator)
        );
    }

    @Test
    public void testGetAlternateMessageI() {
        MessageGenerator msgGen = new MessageGenerator(Source.mail, null, Locale.ENGLISH);
        assertEquals(
                LabelExtractor.get(ResourceFileId.fourth, "long_core_emptyListIndicator", Locale.ENGLISH),
                msgGen.getAlternateMessage(MessageId.emptyListIndicator)
        );
    }

    @Test
    public void testGetAlternateMessageII() {
        MessageGenerator msgGen = new MessageGenerator(Source.mail, null, Locale.ENGLISH);
        assertEquals(
                LabelExtractor.get(ResourceFileId.fourth, "long_core_emptyListIndicator", Locale.ENGLISH),
                msgGen.getAlternateMessage(MessageId.emptyListIndicator, (Map<String, Object>) null)
        );
    }

    @Test
    public void testGetAlternateMessageIII() {
        MessageGenerator msgGen = new MessageGenerator(Source.mail, null, Locale.ENGLISH);
        assertEquals(
                LabelExtractor.get(ResourceFileId.fourth, "long_core_emptyListIndicator", Locale.ENGLISH),
                msgGen.getAlternateMessage(MessageId.emptyListIndicator, (Object[]) null)
        );
    }

    @Test
    public void testPutAndRemove() {
        MessageGenerator msgGen = new MessageGenerator(Source.mail, null, Locale.ENGLISH);
        assertEquals(0, msgGen.getParameters().size());
        assertEquals(1, msgGen.put("key", "value").getParameters().size());
        assertEquals(0, msgGen.remove("key").getParameters().size());
    }

    @Test
    public void testFetchDemandNull() {
        MessageGenerator msgGen = new MessageGenerator(Source.mail, null, Locale.ENGLISH);
        assertEquals(0, msgGen.fetch((Demand) null).getParameters().size());
        assertEquals(0, msgGen.fetch((Proposal) null).getParameters().size());
        assertEquals(0, msgGen.fetch((Store) null).getParameters().size());
        assertEquals(0, msgGen.fetch((Location) null, "prefix").getParameters().size());
    }

    @Test
    public void testFetchDemand() {
        MessageGenerator msgGen = new MessageGenerator(Source.mail, null, Locale.ENGLISH);
        Demand demand = new Demand();
        Map<String, Object> param = msgGen.fetch(demand).getParameters();
        assertFalse(param.containsKey(Demand.KEY));
    }

    @Test
    public void testFetchProposal() {
        MessageGenerator msgGen = new MessageGenerator(Source.mail, null, Locale.ENGLISH);
        Proposal proposal = new Proposal();
        Map<String, Object> param = msgGen.fetch(proposal).getParameters();
        assertFalse(param.containsKey(Proposal.KEY));
    }

    @Test
    public void testFetchStore() {
        MessageGenerator msgGen = new MessageGenerator(Source.mail, null, Locale.ENGLISH);
        Store store = new Store();
        Map<String, Object> param = msgGen.fetch(store).getParameters();
        assertFalse(param.containsKey(Store.KEY));
    }

    @Test
    public void testFetchLocation() {
        MessageGenerator msgGen = new MessageGenerator(Source.mail, null, Locale.ENGLISH);
        Location location = new Location();
        Map<String, Object> param = msgGen.fetch(location, "prefix").getParameters();
        assertFalse(param.containsKey("prefix" + MessageGenerator.FIELD_SEPARATOR + Location.KEY));
    }

    @Test
    public void testFetchMetadataI() {
        final Long key = 43435L;
        final String metadata = ""; // Empty record
        final String emptyListIndicator = LabelExtractor.get(ResourceFileId.fourth, "long_core_emptyListIndicator", Locale.ENGLISH);
        MessageGenerator msgGen = new MessageGenerator(Source.mail, null, Locale.ENGLISH);
        Demand demand = new Demand();
        demand.setKey(key);
        demand.setDueDate(DateUtils.getNowDate());
        demand.setMetadata(metadata);
        Map<String, Object> param = msgGen.fetch(demand).getParameters();
        assertEquals(key, param.get("demand" + MessageGenerator.FIELD_SEPARATOR + Entity.KEY));
        assertTrue(param.containsKey("demand" + MessageGenerator.FIELD_SEPARATOR + Command.DUE_DATE));
        assertEquals(emptyListIndicator, param.get("demand" + MessageGenerator.FIELD_SEPARATOR + Command.META_DATA));
    }

    @Test
    public void testFetchMetadataII() {
        final Long key = 43435L;
        final String metadata = "{}"; // Emty JsonObject
        final String emptyListIndicator = LabelExtractor.get(ResourceFileId.fourth, "long_core_emptyListIndicator", Locale.ENGLISH);
        MessageGenerator msgGen = new MessageGenerator(Source.mail, null, Locale.ENGLISH);
        Demand demand = new Demand();
        demand.setKey(key);
        demand.setDueDate(DateUtils.getNowDate());
        demand.setMetadata(metadata);
        Map<String, Object> param = msgGen.fetch(demand).getParameters();
        assertEquals(key, param.get("demand" + MessageGenerator.FIELD_SEPARATOR + Entity.KEY));
        assertTrue(param.containsKey("demand" + MessageGenerator.FIELD_SEPARATOR + Command.DUE_DATE));
        assertEquals(emptyListIndicator, param.get("demand" + MessageGenerator.FIELD_SEPARATOR + Command.META_DATA));
    }

    @Test
    public void testFetchMetadataIII() {
        final Long key = 43435L;
        final String metadata = "{"; // Mal-formed record
        final String emptyListIndicator = LabelExtractor.get(ResourceFileId.fourth, "long_core_emptyListIndicator", Locale.ENGLISH);
        MessageGenerator msgGen = new MessageGenerator(Source.mail, null, Locale.ENGLISH);
        Demand demand = new Demand();
        demand.setKey(key);
        demand.setDueDate(DateUtils.getNowDate());
        demand.setMetadata(metadata);
        Map<String, Object> param = msgGen.fetch(demand).getParameters();
        assertEquals(key, param.get("demand" + MessageGenerator.FIELD_SEPARATOR + Entity.KEY));
        assertTrue(param.containsKey("demand" + MessageGenerator.FIELD_SEPARATOR + Command.DUE_DATE));
        assertEquals(emptyListIndicator, param.get("demand" + MessageGenerator.FIELD_SEPARATOR + Command.META_DATA));
    }

    @Test
    public void testFetchMetadataIV() {
        final Long key = 43435L;
        final String pKey = "key";
        final Double pValue = 1.0D;
        final String metadata = "{'" + pKey + "':" + pValue + "}";
        MessageGenerator msgGen = new MessageGenerator(Source.mail, null, Locale.ENGLISH);
        Demand demand = new Demand();
        demand.setKey(key);
        demand.setDueDate(DateUtils.getNowDate());
        demand.setMetadata(metadata);
        Map<String, Object> param = msgGen.fetch(demand).getParameters();
        assertEquals(key, param.get("demand" + MessageGenerator.FIELD_SEPARATOR + Entity.KEY));
        assertTrue(param.containsKey("demand" + MessageGenerator.FIELD_SEPARATOR + Command.DUE_DATE));
        assertEquals(metadata, param.get("demand" + MessageGenerator.FIELD_SEPARATOR + Command.META_DATA));
        assertEquals(pValue.longValue(), param.get("demand" + MessageGenerator.FIELD_SEPARATOR + Command.META_DATA + MessageGenerator.FIELD_SEPARATOR + pKey));
    }
}
