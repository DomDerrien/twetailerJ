package twetailer.task.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.Collator;
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
import twetailer.InvalidIdentifierException;
import twetailer.connector.BaseConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.RawCommand;
import twetailer.dto.SaleAssociate;
import twetailer.task.CommandProcessor;
import twetailer.task.TestCommandProcessor;
import twetailer.task.step.BaseSteps;
import twetailer.validator.LocaleValidator;
import twetailer.validator.CommandSettings.Action;
import twitter4j.TwitterException;
import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.GenericJsonArray;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

public class TestSupplyCommandProcessor {

    private static Collator collator = LocaleValidator.getCollator(Locale.ENGLISH);

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
        final Long saConsumerRecordKey = 76325L;
        final Long saleAssociateKey = 222L;
        final Long storeKey = 333L;

        // SaleAssociateOperations mock
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                assertEquals(saleAssociateKey, key);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setStoreKey(storeKey);
                saleAssociate.setConsumerKey(saConsumerRecordKey);
                return saleAssociate;
            }
        });
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertTrue(consumerKey == key || saConsumerRecordKey == key);
                final Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                consumer.setPreferredConnection(Source.simulated);
                return consumer;
            }
        });

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setSaleAssociateKey(saleAssociateKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        Locale locale = Locale.ENGLISH;
        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_supply_empty_tag_hashtag_list", locale), sentText);
    }

    @Test
    public void testProcessCommandSupplyIb() throws TwitterException, DataSourceException, ClientException {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.supply.toString());

        final Long consumerKey = 111L;
        final Long saConsumerRecordKey = 76325L;
        final Long saleAssociateKey = 222L;
        final Long storeKey = 333L;

        // SaleAssociateOperations mock
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
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
                saleAssociate.setConsumerKey(saConsumerRecordKey);
                return saleAssociate;
            }
        });
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertTrue(consumerKey == key || saConsumerRecordKey == key);
                final Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                consumer.setPreferredConnection(Source.simulated);
                return consumer;
            }
        });

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setSaleAssociateKey(saleAssociateKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        Locale locale = Locale.ENGLISH;
        String sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_supply_empty_tag_hashtag_list", locale), sentText);
    }

    @Test
    public void testProcessCommandSupplyII() throws TwitterException, DataSourceException, ClientException {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.supply.toString());

        final Long consumerKey = 111L;
        final Long saConsumerRecordKey = 76325L;
        final Long saleAssociateKey = 222L;
        final Long storeKey = 333L;
        final String tag1 = "tag1";

        // SaleAssociateOperations mock
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                assertEquals(saleAssociateKey, key);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setStoreKey(storeKey);
                saleAssociate.setConsumerKey(saConsumerRecordKey);
                saleAssociate.addCriterion(tag1, collator);
                return saleAssociate;
            }
        });
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertTrue(consumerKey == key || saConsumerRecordKey == key);
                final Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                consumer.setPreferredConnection(Source.simulated);
                return consumer;
            }
        });

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setSaleAssociateKey(saleAssociateKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        Locale locale = Locale.ENGLISH;
        String sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1);
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_supply_updated_1_tag_list", new Object[] { tag1 }, Locale.ENGLISH), sentText);

        sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_supply_empty_hashtag_list", locale), sentText);
    }

    @Test
    public void testProcessCommandSupplyIII() throws TwitterException, DataSourceException, ClientException {
        // Command mock
        JsonObject command = new GenericJsonObject();
        command.put(Command.ACTION, Action.supply.toString());

        final Long consumerKey = 111L;
        final Long saConsumerRecordKey = 76325L;
        final Long saleAssociateKey = 222L;
        final Long storeKey = 333L;
        final String tag1 = "tag1";
        final String tag2 = "tag2";
        final String tag3 = "tag3";

        // SaleAssociateOperations mock
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                assertEquals(saleAssociateKey, key);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setStoreKey(storeKey);
                saleAssociate.setConsumerKey(saConsumerRecordKey);
                saleAssociate.addCriterion(tag1, collator);
                saleAssociate.addCriterion(tag2, collator);
                saleAssociate.addCriterion(tag3, collator);
                return saleAssociate;
            }
        });
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertTrue(consumerKey == key || saConsumerRecordKey == key);
                final Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                consumer.setPreferredConnection(Source.simulated);
                return consumer;
            }
        });

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setSaleAssociateKey(saleAssociateKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        Locale locale = Locale.ENGLISH;
        String sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1);
        assertNotNull(sentText);
        String tags = LabelExtractor.get("cp_tweet_tags_part", new Object[] { tag1 + " " + tag2 + " " + tag3 }, locale);
        assertEquals(LabelExtractor.get("cp_command_supply_updated_n_tag_list", new Object[] { tags, 3 }, locale), sentText);

        sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_supply_empty_hashtag_list", locale), sentText);
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
        final Long saConsumerRecordKey = 76325L;
        final Long saleAssociateKey = 222L;
        final Long storeKey = 333L;
        final String tag1 = "tag1";
        final String tag2 = "tag2";
        final String tag3 = "tag3";

        // SaleAssociateOperations mock
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                assertEquals(saleAssociateKey, key);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setStoreKey(storeKey);
                saleAssociate.setConsumerKey(saConsumerRecordKey);
                saleAssociate.addCriterion(tag1, collator);
                saleAssociate.addCriterion(tag2, collator);
                saleAssociate.addCriterion(tag3, collator);
                return saleAssociate;
            }
        });
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertTrue(consumerKey == key || saConsumerRecordKey == key);
                final Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                consumer.setPreferredConnection(Source.simulated);
                return consumer;
            }
        });

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setSaleAssociateKey(saleAssociateKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        Locale locale = Locale.ENGLISH;
        String sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1);
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_supply_updated_1_tag_list", new Object[] { newTag.toLowerCase() }, Locale.ENGLISH), sentText);

        sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_supply_empty_hashtag_list", locale), sentText);
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
        final Long saConsumerRecordKey = 76325L;
        final Long saleAssociateKey = 222L;
        final Long storeKey = 333L;
        final String tag1 = "tag1";
        final String tag2 = "tag2";
        final String tag3 = "tag3";

        // SaleAssociateOperations mock
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                assertEquals(saleAssociateKey, key);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setStoreKey(storeKey);
                saleAssociate.setConsumerKey(saConsumerRecordKey);
                saleAssociate.addCriterion(tag1, collator);
                saleAssociate.addCriterion(tag2, collator);
                saleAssociate.addCriterion(tag3, collator);
                return saleAssociate;
            }
        });
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertTrue(consumerKey == key || saConsumerRecordKey == key);
                final Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                consumer.setPreferredConnection(Source.simulated);
                return consumer;
            }
        });

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setSaleAssociateKey(saleAssociateKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        Locale locale = Locale.ENGLISH;
        String sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1);
        assertNotNull(sentText);
        String tags = LabelExtractor.get("cp_tweet_tags_part", new Object[] { tag1 + " " + tag2 + " " + tag3 + " " + newTag.toLowerCase() }, locale);
        assertEquals(LabelExtractor.get("cp_command_supply_updated_n_tag_list", new Object[] { tags, 4 }, locale), sentText);

        sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_supply_empty_hashtag_list", locale), sentText);
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
        final Long saConsumerRecordKey = 76325L;
        final Long saleAssociateKey = 222L;
        final Long storeKey = 333L;
        final String tag1 = "tag1";
        final String tag2 = newTag;
        final String tag3 = "tag3";

        // SaleAssociateOperations mock
        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public SaleAssociate getSaleAssociate(PersistenceManager pm, Long key) {
                assertEquals(saleAssociateKey, key);
                SaleAssociate saleAssociate = new SaleAssociate();
                saleAssociate.setKey(saleAssociateKey);
                saleAssociate.setStoreKey(storeKey);
                saleAssociate.setConsumerKey(saConsumerRecordKey);
                saleAssociate.addCriterion(tag1, collator);
                saleAssociate.addCriterion(tag2, collator);
                saleAssociate.addCriterion(tag3, collator);
                return saleAssociate;
            }
        });

        // RawCommand mock
        RawCommand rawCommand = new RawCommand(Source.simulated);

        // Consumer mock
        Consumer consumer = new Consumer();
        consumer.setKey(consumerKey);
        consumer.setSaleAssociateKey(saleAssociateKey);

        CommandProcessor.processCommand(new MockPersistenceManager(), consumer, rawCommand, command);

        Locale locale = Locale.ENGLISH;
        String sentText = BaseConnector.getCommunicationForRetroIndexInSimulatedMode(1);
        assertNotNull(sentText);
        String tags = LabelExtractor.get("cp_tweet_tags_part", new Object[] { tag1 + " " + tag3 }, locale);
        assertEquals(LabelExtractor.get("cp_command_supply_updated_n_tag_list", new Object[] { tags, 2 }, locale), sentText);

        sentText = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentText);
        assertEquals(LabelExtractor.get("cp_command_supply_empty_hashtag_list", locale), sentText);
    }
}
