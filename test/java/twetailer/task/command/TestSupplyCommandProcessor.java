package twetailer.task.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.jdo.MockPersistenceManager;
import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.connector.BaseConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.MockBaseOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.RawCommand;
import twetailer.dto.SaleAssociate;
import twetailer.task.CommandProcessor;
import twetailer.task.TestCommandProcessor;
import twetailer.validator.CommandSettings.Action;
import twitter4j.TwitterException;
import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

public class TestSupplyCommandProcessor {

    @BeforeClass
    public static void setUpBeforeClass() {
        TestCommandProcessor.setUpBeforeClass();
    }

    @Before
    public void setUp() throws Exception {
        new TestCommandProcessor().setUp();
    }

    @After
    public void tearDown() throws Exception {
        new TestCommandProcessor().tearDown();
    }

    @Test
    public void testConstructor() {
        new SupplyCommandProcessor();
    }

    @Test
    public void testProcessCommandSupplyIa() throws TwitterException, DataSourceException, ClientException {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.supply.toString());

        final Long consumerKey = 111L;
        final Long saleAssociateKey = 222L;
        final Long storeKey = 333L;

        // SaleAssociateOperations mock
        final SaleAssociateOperations saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<Long> getSaleAssociateKeys(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                List<Long> saleAssociateKeys = new ArrayList<Long>();
                saleAssociateKeys.add(saleAssociateKey);
                return saleAssociateKeys;
            }
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                assertEquals(saleAssociateKey, key);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setStoreKey(storeKey);
                saleAssociate.setConsumerKey(consumerKey);
                return saleAssociate;
            }
        };

        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.saleAssociateOperations = saleAssociateOperations;

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_supply_empty_tag_list", Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandSupplyIb() throws TwitterException, DataSourceException, ClientException {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.supply.toString());

        final Long consumerKey = 111L;
        final Long saleAssociateKey = 222L;
        final Long storeKey = 333L;

        // SaleAssociateOperations mock
        final SaleAssociateOperations saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<Long> getSaleAssociateKeys(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                List<Long> saleAssociateKeys = new ArrayList<Long>();
                saleAssociateKeys.add(saleAssociateKey);
                return saleAssociateKeys;
            }
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                assertEquals(saleAssociateKey, key);
                // SaleAssociate saleAssociate = new SaleAssociate();
                SaleAssociate saleAssociate = new SaleAssociate() {
                    @Override
                    public List<String> getCriteria() {
                        return null;
                    }
                };
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setStoreKey(storeKey);
                saleAssociate.setConsumerKey(consumerKey);
                return saleAssociate;
            }
        };

        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.saleAssociateOperations = saleAssociateOperations;

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_supply_empty_tag_list", Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandSupplyII() throws TwitterException, DataSourceException, ClientException {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.supply.toString());

        final Long consumerKey = 111L;
        final Long saleAssociateKey = 222L;
        final Long storeKey = 333L;
        final String tag1 = "tag1";

        // SaleAssociateOperations mock
        final SaleAssociateOperations saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<Long> getSaleAssociateKeys(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                List<Long> saleAssociateKeys = new ArrayList<Long>();
                saleAssociateKeys.add(saleAssociateKey);
                return saleAssociateKeys;
            }
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                assertEquals(saleAssociateKey, key);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setStoreKey(storeKey);
                saleAssociate.setConsumerKey(consumerKey);
                saleAssociate.addCriterion(tag1);
                return saleAssociate;
            }
        };

        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.saleAssociateOperations = saleAssociateOperations;

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_supply_updated_1_tag_list", new Object[] { tag1 }, Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandSupplyIII() throws TwitterException, DataSourceException, ClientException {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.supply.toString());

        final Long consumerKey = 111L;
        final Long saleAssociateKey = 222L;
        final Long storeKey = 333L;
        final String tag1 = "tag1";
        final String tag2 = "tag2";
        final String tag3 = "tag3";

        // SaleAssociateOperations mock
        final SaleAssociateOperations saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<Long> getSaleAssociateKeys(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                List<Long> saleAssociateKeys = new ArrayList<Long>();
                saleAssociateKeys.add(saleAssociateKey);
                return saleAssociateKeys;
            }
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                assertEquals(saleAssociateKey, key);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setStoreKey(storeKey);
                saleAssociate.setConsumerKey(consumerKey);
                saleAssociate.addCriterion(tag1);
                saleAssociate.addCriterion(tag2);
                saleAssociate.addCriterion(tag3);
                return saleAssociate;
            }
        };

        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.saleAssociateOperations = saleAssociateOperations;

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        Locale locale = Locale.ENGLISH;
        String tags = LabelExtractor.get("cp_tweet_tags_part", new Object[] { tag1 + " " + tag2 + " " + tag3 }, locale);
        assertEquals(LabelExtractor.get("cp_command_supply_updated_n_tag_list", new Object[] { tags, 3 }, locale), sentText);
    }

    @Test
    public void testProcessCommandSupplyIV() throws TwitterException, DataSourceException, ClientException {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.supply.toString());
        final String newTag = "newTag";
        JsonArray tagList = new GenericJsonArray();
        tagList.add(newTag);
        command.put(SaleAssociate.CRITERIA, tagList);

        final Long consumerKey = 111L;
        final Long saleAssociateKey = 222L;
        final Long storeKey = 333L;
        final String tag1 = "tag1";
        final String tag2 = "tag2";
        final String tag3 = "tag3";

        // SaleAssociateOperations mock
        final SaleAssociateOperations saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<Long> getSaleAssociateKeys(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                List<Long> saleAssociateKeys = new ArrayList<Long>();
                saleAssociateKeys.add(saleAssociateKey);
                return saleAssociateKeys;
            }
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                assertEquals(saleAssociateKey, key);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setStoreKey(storeKey);
                saleAssociate.setConsumerKey(consumerKey);
                saleAssociate.addCriterion(tag1);
                saleAssociate.addCriterion(tag2);
                saleAssociate.addCriterion(tag3);
                return saleAssociate;
            }
        };

        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.saleAssociateOperations = saleAssociateOperations;

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_supply_updated_1_tag_list", new Object[] { newTag }, Locale.ENGLISH), sentText);
    }

    @Test
    public void testProcessCommandSupplyV() throws TwitterException, DataSourceException, ClientException {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.supply.toString());
        final String newTag = "newTag";
        JsonArray tagList = new GenericJsonArray();
        tagList.add(newTag);
        command.put(SaleAssociate.CRITERIA_ADD, tagList);

        final Long consumerKey = 111L;
        final Long saleAssociateKey = 222L;
        final Long storeKey = 333L;
        final String tag1 = "tag1";
        final String tag2 = "tag2";
        final String tag3 = "tag3";

        // SaleAssociateOperations mock
        final SaleAssociateOperations saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<Long> getSaleAssociateKeys(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                List<Long> saleAssociateKeys = new ArrayList<Long>();
                saleAssociateKeys.add(saleAssociateKey);
                return saleAssociateKeys;
            }
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                assertEquals(saleAssociateKey, key);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setStoreKey(storeKey);
                saleAssociate.setConsumerKey(consumerKey);
                saleAssociate.addCriterion(tag1);
                saleAssociate.addCriterion(tag2);
                saleAssociate.addCriterion(tag3);
                return saleAssociate;
            }
        };

        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.saleAssociateOperations = saleAssociateOperations;

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        Locale locale = Locale.ENGLISH;
        String tags = LabelExtractor.get("cp_tweet_tags_part", new Object[] { tag1 + " " + tag2 + " " + tag3 + " " + newTag }, locale);
        assertEquals(LabelExtractor.get("cp_command_supply_updated_n_tag_list", new Object[] { tags, 4 }, locale), sentText);
    }

    @Test
    public void testProcessCommandSupplyVI() throws TwitterException, DataSourceException, ClientException {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.supply.toString());
        final String newTag = "newTag";
        JsonArray tagList = new GenericJsonArray();
        tagList.add(newTag);
        command.put(SaleAssociate.CRITERIA_REMOVE, tagList);

        final Long consumerKey = 111L;
        final Long saleAssociateKey = 222L;
        final Long storeKey = 333L;
        final String tag1 = "tag1";
        final String tag2 = newTag;
        final String tag3 = "tag3";

        // SaleAssociateOperations mock
        final SaleAssociateOperations saleAssociateOperations = new SaleAssociateOperations() {
            @Override
            public List<Long> getSaleAssociateKeys(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(SaleAssociate.CONSUMER_KEY, key);
                assertEquals(consumerKey, (Long) value);
                List<Long> saleAssociateKeys = new ArrayList<Long>();
                saleAssociateKeys.add(saleAssociateKey);
                return saleAssociateKeys;
            }
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                assertEquals(saleAssociateKey, key);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setStoreKey(storeKey);
                saleAssociate.setConsumerKey(consumerKey);
                saleAssociate.addCriterion(tag1);
                saleAssociate.addCriterion(tag2);
                saleAssociate.addCriterion(tag3);
                return saleAssociate;
            }
        };

        // CommandProcessor mock
        CommandProcessor._baseOperations = new MockBaseOperations();
        CommandProcessor.saleAssociateOperations = saleAssociateOperations;

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        Locale locale = Locale.ENGLISH;
        String tags = LabelExtractor.get("cp_tweet_tags_part", new Object[] { tag1 + " " + tag3 }, locale);
        assertEquals(LabelExtractor.get("cp_command_supply_updated_n_tag_list", new Object[] { tags, 2 }, locale), sentText);
    }
}
