package twetailer.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javamocks.util.logging.MockLogger;

import javax.cache.MockCacheFactory;
import javax.jdo.MockPersistenceManager;
import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.connector.BaseConnector;
import twetailer.connector.MailConnector;
import twetailer.connector.MockTwitterConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.CacheHandler;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.InfluencerOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.MockBaseOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dao.SettingsOperations;
import twetailer.dao.StoreOperations;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Influencer;
import twetailer.dto.Location;
import twetailer.dto.SaleAssociate;
import twetailer.dto.Settings;
import twetailer.dto.Store;
import twetailer.task.step.BaseSteps;
import twetailer.validator.CommandSettings.State;

import com.google.appengine.api.taskqueue.MockQueue;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class TestDemandProcessor {

    private static LocalServiceTestHelper  helper;

    final Long locationKey = 54375232L;
    final Long storeKey = 76532762L;
    final String tag = "just-one-tag";
    final Long robotKey = 6545987321L;
    final Long demandKey = 7654325489798754L;
    final Long consumerKey = 543543453L;
    final Long saleAssociateKey = 98776453221L;
    final Long influencerKey = 31415954L;

    @BeforeClass
    public static void setUpBeforeClass() {
        DemandProcessor.setMockLogger(new MockLogger("test", null));
        BaseConnector.setMockLogger(new MockLogger("test", null));
        MailConnector.setMockLogger(new MockLogger("test", null));
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
        CacheHandler.injectMockCacheFactory(new MockCacheFactory());

        // Map<Long, Long> activeChannels = (Map<Long, Long>) CacheHandler.getFromCache(ChannelConnector.MEMCACHE_IDENTIFIER);
        }

    @Before
    public void setUp() throws Exception {
        helper.setUp();

        BaseSteps.resetOperationControllers(true);
        BaseSteps.setMockBaseOperations(new MockBaseOperations());
        CacheHandler.injectMockCacheFactory(null);
        CacheHandler.injectMockCache(null);
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();

        RobotResponder.setRobotSaleAssociateKey(null);

        BaseConnector.resetLastCommunicationInSimulatedMode();

        MockTwitterConnector.restoreTwitterConnector();
    }

    @Test
    public void testConstructor() {
        new DemandProcessor();
    }

    @Test
    public void testGetLogger() {
        DemandProcessor.getLogger();
    }

    @Test
    public void testNotifyAvailabilityI() {
        //
        // No message sent
        //
        Consumer demandOwner = new Consumer();
        Consumer proposalOwner = new Consumer();
        proposalOwner.setPreferredConnection(Source.api);
        Demand demand = new Demand();

        DemandProcessor.notifyAvailability(demand, demandOwner, proposalOwner, new Influencer());
    }

    @Test
    public void testNotifyAvailabilityII() {
        //
        // Message sent by mail
        //
        Consumer demandOwner = new Consumer();
        Consumer proposalOwner = new Consumer();
        proposalOwner.setPreferredConnection(Source.mail);
        proposalOwner.setEmail("unit@test.org");
        Demand demand = new Demand();
        demand.setKey(12345L);

        DemandProcessor.notifyAvailability(demand, demandOwner, proposalOwner, new Influencer());
    }

    @Test
    public void testNotifyAvailabilityIII() {
        //
        // Message sent by mail
        //
        Consumer demandOwner = new Consumer();
        demandOwner.setPublishedDemandNb(256L);
        demandOwner.setClosedDemandNb(255L);
        Consumer proposalOwner = new Consumer();
        proposalOwner.setPreferredConnection(Source.mail);
        proposalOwner.setEmail("unit@test.org");
        Demand demand = new Demand();
        demand.setKey(12345L);

        DemandProcessor.notifyAvailability(demand, demandOwner, proposalOwner, new Influencer());
    }

    @Test
    public void testNotifyAvailabilityIV() {
        //
        // Message sending failing
        //
        Consumer demandOwner = new Consumer();
        Consumer proposalOwner = new Consumer();
        proposalOwner.setPreferredConnection(Source.mail);
        proposalOwner.setEmail("unit@test.org");
        Demand demand = new Demand();
        demand.setKey(12345L);

        MailConnector.foolNextMessagePost(); // For the initial message sending to the proposal owner

        DemandProcessor.notifyAvailability(demand, demandOwner, proposalOwner, new Influencer());
    }

    @Test
    public void testNotifyAvailabilityV() {
        //
        // Message sending failing
        //
        Consumer demandOwner = new Consumer();
        Consumer proposalOwner = new Consumer();
        proposalOwner.setPreferredConnection(Source.mail);
        proposalOwner.setEmail("unit@test.org");
        Demand demand = new Demand();
        demand.setKey(12345L);

        MailConnector.foolNextMessagePost(); // For the initial message sending to the proposal owner
        MailConnector.foolNextMessagePost(); // For the message sending to the admins

        DemandProcessor.notifyAvailability(demand, demandOwner, proposalOwner, new Influencer());
    }

    @Test
    public void testFilterSaleAssociatesI() {
        //
        // No sale associate in the area
        //
        Consumer demandOwner = new Consumer();
        Demand demand = new Demand();
        List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();

        assertEquals(0, DemandProcessor.filterSaleAssociates(saleAssociates, demand, demandOwner).size());
    }

    @Test
    public void testFilterSaleAssociatesII() {
        //
        // One sale associate already selected
        //
        Long saleAssociateKey = 12345L;
        Consumer demandOwner = new Consumer();
        Demand demand = new Demand();
        demand.addSaleAssociateKey(saleAssociateKey);
        List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
        SaleAssociate saleAssociate = new SaleAssociate();
        saleAssociate.setKey(saleAssociateKey);
        saleAssociates.add(saleAssociate);

        assertEquals(0, DemandProcessor.filterSaleAssociates(saleAssociates, demand, demandOwner).size());
    }

    @Test
    public void testFilterSaleAssociatesIII() {
        //
        // One sale associate in the area accepting anything
        //
        Consumer demandOwner = new Consumer();
        Demand demand = new Demand() {
            @Override
            public Demand resetLists() {
                return super.resetLists();
            }
        }.resetLists();
        List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
        SaleAssociate saleAssociate = new SaleAssociate();
        saleAssociate.setScore("1:0.0");
        saleAssociates.add(saleAssociate);

        assertEquals(1, DemandProcessor.filterSaleAssociates(saleAssociates, demand, demandOwner).size());
    }

    @Test
    public void testFilterSaleAssociatesIV() {
        //
        // One sale associate in the area, with an unsupported score version, so falling back on the default
        //
        Consumer demandOwner = new Consumer();
        Demand demand = new Demand();
        List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
        SaleAssociate saleAssociate = new SaleAssociate();
        saleAssociate.setScore("0:0.0");
        saleAssociates.add(saleAssociate);

        assertEquals(0, DemandProcessor.filterSaleAssociates(saleAssociates, demand, demandOwner).size());
    }

    @Test
    public void testFilterSaleAssociatesVa() {
        //
        // One sale associate in the area, with one common hash tag
        //
        Consumer demandOwner = new Consumer();
        Demand demand = new Demand();
        demand.addHashTag("hash");
        List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
        SaleAssociate saleAssociate = new SaleAssociate();
        saleAssociate.setScore("1:1.0");
        saleAssociate.addHashTag("hash");
        saleAssociates.add(saleAssociate);

        assertEquals(1, DemandProcessor.filterSaleAssociates(saleAssociates, demand, demandOwner).size());
    }

    @Test
    public void testFilterSaleAssociatesVb() {
        //
        // One sale associate in the area, with one common hash tag, but requires 2 hash tags
        //
        Consumer demandOwner = new Consumer();
        Demand demand = new Demand();
        demand.addHashTag("hash");
        List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
        SaleAssociate saleAssociate = new SaleAssociate();
        saleAssociate.setScore("1:2.0");
        saleAssociate.addHashTag("hash");
        saleAssociates.add(saleAssociate);

        assertEquals(0, DemandProcessor.filterSaleAssociates(saleAssociates, demand, demandOwner).size());
    }

    @Test
    public void testFilterSaleAssociatesVc() {
        //
        // One sale associate in the area, with no common hash tag
        //
        Consumer demandOwner = new Consumer();
        Demand demand = new Demand();
        demand.addHashTag("hash-given");
        List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
        SaleAssociate saleAssociate = new SaleAssociate();
        saleAssociate.setScore("1:1.0");
        saleAssociate.addHashTag("hash-asked");
        saleAssociates.add(saleAssociate);

        assertEquals(0, DemandProcessor.filterSaleAssociates(saleAssociates, demand, demandOwner).size());
    }

    @Test
    public void testFilterSaleAssociatesVd() {
        //
        // One sale associate in the area and a demand without hash tag list
        //
        Consumer demandOwner = new Consumer();
        Demand demand = new Demand() {
            @Override
            public Demand resetLists() {
                return super.resetLists();
            }
        }.resetLists();
        List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
        SaleAssociate saleAssociate = new SaleAssociate();
        saleAssociate.setScore("1:1.0");
        saleAssociate.addHashTag("hash-asked");
        saleAssociates.add(saleAssociate);

        assertEquals(0, DemandProcessor.filterSaleAssociates(saleAssociates, demand, demandOwner).size());
    }

    @Test
    public void testFilterSaleAssociatesVe() {
        //
        // One sale associate in the area without hash tag list
        //
        Consumer demandOwner = new Consumer();
        Demand demand = new Demand();
        List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
        SaleAssociate saleAssociate = new SaleAssociate() {
            @Override
            public SaleAssociate resetLists() {
                return super.resetLists();
            }
        }.resetLists();
        saleAssociate.setScore("1:1.0");
        saleAssociates.add(saleAssociate);

        assertEquals(0, DemandProcessor.filterSaleAssociates(saleAssociates, demand, demandOwner).size());
    }

    @Test
    public void testFilterSaleAssociatesVIa() {
        //
        // One sale associate in the area, with one common (normal) tag
        //
        Consumer demandOwner = new Consumer();
        Demand demand = new Demand();
        demand.setContent("tag");
        List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
        SaleAssociate saleAssociate = new SaleAssociate();
        saleAssociate.setScore("1:0.1");
        saleAssociate.addCriterion("tag", java.text.Collator.getInstance(Locale.ENGLISH));
        saleAssociates.add(saleAssociate);

        assertEquals(1, DemandProcessor.filterSaleAssociates(saleAssociates, demand, demandOwner).size());
    }

    @Test
    public void testFilterSaleAssociatesVIb() {
        //
        // One sale associate in the area, with one common (normal) tag, but requires 2 (normal) tags
        //
        Consumer demandOwner = new Consumer();
        Demand demand = new Demand();
        demand.setContent("tag");
        List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
        SaleAssociate saleAssociate = new SaleAssociate();
        saleAssociate.setScore("1:0.2");
        saleAssociate.addCriterion("tag", java.text.Collator.getInstance(Locale.ENGLISH));
        saleAssociates.add(saleAssociate);

        assertEquals(0, DemandProcessor.filterSaleAssociates(saleAssociates, demand, demandOwner).size());
    }

    @Test
    public void testFilterSaleAssociatesVIc() {
        //
        // One sale associate in the area, with no common (normal) tag
        //
        Consumer demandOwner = new Consumer();
        Demand demand = new Demand();
        demand.setContent("tag-given");
        List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
        SaleAssociate saleAssociate = new SaleAssociate();
        saleAssociate.setScore("1:0.1");
        saleAssociate.addCriterion("tag-asked", java.text.Collator.getInstance(Locale.ENGLISH));
        saleAssociates.add(saleAssociate);

        assertEquals(0, DemandProcessor.filterSaleAssociates(saleAssociates, demand, demandOwner).size());
    }

    @Test
    public void testFilterSaleAssociatesVId() {
        //
        // One sale associate in the area and a demand without (normal) tag list
        //
        Consumer demandOwner = new Consumer();
        Demand demand = new Demand() {
            @Override
            public Demand resetLists() {
                return super.resetLists();
            }
        }.resetLists();
        List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
        SaleAssociate saleAssociate = new SaleAssociate();
        saleAssociate.setScore("1:0.1");
        saleAssociate.addCriterion("tag-asked", java.text.Collator.getInstance(Locale.ENGLISH));
        saleAssociates.add(saleAssociate);

        assertEquals(0, DemandProcessor.filterSaleAssociates(saleAssociates, demand, demandOwner).size());
    }

    @Test
    public void testFilterSaleAssociatesVIe() {
        //
        // One sale associate in the area without (normal) tag list
        //
        Consumer demandOwner = new Consumer();
        Demand demand = new Demand();
        List<SaleAssociate> saleAssociates = new ArrayList<SaleAssociate>();
        SaleAssociate saleAssociate = new SaleAssociate() {
            @Override
            public SaleAssociate resetLists() {
                return super.resetLists();
            }
        }.resetLists();
        saleAssociate.setScore("1:0.1");
        saleAssociates.add(saleAssociate);

        assertEquals(0, DemandProcessor.filterSaleAssociates(saleAssociates, demand, demandOwner).size());
    }

    @Test
    public void testIdentifySaleAssociatesI() throws InvalidIdentifierException, DataSourceException {
        //
        // No location registered around the demand's one
        //
        Consumer owner = new Consumer();
        Demand demand = new Demand();
        demand.setLocationKey(locationKey);

        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(locationKey, key);
                Location resource = new Location();
                resource.setKey(locationKey);
                return resource;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) throws DataSourceException {
                assertEquals(locationKey, location.getKey());
                return new ArrayList<Location>();
            }
        });

        assertEquals(0, DemandProcessor.identifySaleAssociates(new MockPersistenceManager(), demand, owner).size());
    }

    @Test
    public void testIdentifySaleAssociatesII() throws InvalidIdentifierException, DataSourceException {
        //
        // One location with a store registered around the demand's one, but store not available anymore
        //
        Consumer owner = new Consumer();
        Demand demand = new Demand();
        demand.setLocationKey(locationKey);

        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(locationKey, key);
                Location resource = new Location();
                resource.setKey(locationKey);
                return resource;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) throws DataSourceException {
                assertEquals(locationKey, location.getKey());
                Location resource = new Location();
                resource.setKey(locationKey);
                List<Location> resources = new ArrayList<Location>();
                resources.add(resource);
                return resources;
            }
        });

        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, Map<String, Object> queryParameters, List<Location> locations, int limit) throws DataSourceException {
                assertEquals(locationKey, locations.get(0).getKey());
                return new ArrayList<Store>();
            }
        });

        assertEquals(0, DemandProcessor.identifySaleAssociates(new MockPersistenceManager(), demand, owner).size());
    }

    @Test
    public void testIdentifySaleAssociatesIII() throws InvalidIdentifierException, DataSourceException {
        //
        // One location and one store registered around the demand's one, but sale associate not available anymore
        //
        Consumer owner = new Consumer();
        Demand demand = new Demand();
        demand.setLocationKey(locationKey);

        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(locationKey, key);
                Location resource = new Location();
                resource.setKey(locationKey);
                return resource;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) throws DataSourceException {
                assertEquals(locationKey, location.getKey());
                Location resource = new Location();
                resource.setKey(locationKey);
                List<Location> resources = new ArrayList<Location>();
                resources.add(resource);
                return resources;
            }
        });

        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, Map<String, Object> queryParameters, List<Location> locations, int limit) throws DataSourceException {
                assertEquals(locationKey, locations.get(0).getKey());
                Store resource = new Store();
                resource.setKey(storeKey);
                List<Store> resources = new ArrayList<Store>();
                resources.add(resource);
                return resources;
            }
        });

        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String attribute, Object value, int limit) throws DataSourceException {
                assertEquals(SaleAssociate.STORE_KEY, attribute);
                assertEquals(storeKey, (Long) value);
                return new ArrayList<SaleAssociate>();
            }
        });

        assertEquals(0, DemandProcessor.identifySaleAssociates(new MockPersistenceManager(), demand, owner).size());
    }

    @Test
    public void testIdentifySaleAssociatesIV() throws InvalidIdentifierException, DataSourceException {
        //
        // One location, one store, and one sale associate registered around the demand's one -- sale associate can serve the demand
        // *** function called to setup the environment in testProcessIII() ***
        //
        Consumer owner = new Consumer();
        Demand demand = new Demand();
        demand.setKey(demandKey);
        demand.setLocationKey(locationKey);
        demand.setContent(tag);

        identifyTwoSaleAssociates(2);

        assertEquals(2, DemandProcessor.identifySaleAssociates(new MockPersistenceManager(), demand, owner).size());
    }

    public void identifyTwoSaleAssociates(final int saleAssociateNb) {
        //
        // Helper called by
        // - testIdentifySaleAssociatesIV()
        // - testProcessIV()
        // - testProcessV()
        //
        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(locationKey, key);
                Location resource = new Location();
                resource.setKey(locationKey);
                return resource;
            }
            @Override
            public List<Location> getLocations(PersistenceManager pm, Location location, Double range, String rangeUnit, boolean withStore, int limit) throws DataSourceException {
                assertEquals(locationKey, location.getKey());
                Location resource = new Location();
                resource.setKey(locationKey);
                List<Location> resources = new ArrayList<Location>();
                resources.add(resource);
                return resources;
            }
        });

        BaseSteps.setMockStoreOperations(new StoreOperations() {
            @Override
            public List<Store> getStores(PersistenceManager pm, Map<String, Object> queryParameters, List<Location> locations, int limit) throws DataSourceException {
                assertEquals(locationKey, locations.get(0).getKey());
                Store resource = new Store();
                resource.setKey(storeKey);
                List<Store> resources = new ArrayList<Store>();
                resources.add(resource);
                return resources;
            }
        });

        BaseSteps.setMockSaleAssociateOperations(new SaleAssociateOperations() {
            @Override
            public List<SaleAssociate> getSaleAssociates(PersistenceManager pm, String attribute, Object value, int limit) throws DataSourceException {
                assertEquals(SaleAssociate.STORE_KEY, attribute);
                assertEquals(storeKey, (Long) value);
                List<SaleAssociate> resources = new ArrayList<SaleAssociate>();
                // One associate
                SaleAssociate resource = new SaleAssociate();
                resource.setKey(2 * saleAssociateKey);
                resource.setStoreKey(storeKey); // Same store
                resource.setConsumerKey(2 * consumerKey);
                resource.addCriterion(tag, java.text.Collator.getInstance(Locale.ENGLISH));
                resources.add(resource);
                // Second associate
                if (1 < saleAssociateNb) {
                    resource = new SaleAssociate();
                    resource.setKey(saleAssociateKey);
                    resource.setStoreKey(storeKey); // Same store
                    resource.setConsumerKey(consumerKey);
                    resource.addCriterion("another-something", java.text.Collator.getInstance(Locale.ENGLISH));
                    resource.addCriterion(tag, java.text.Collator.getInstance(Locale.ENGLISH));
                    resources.add(resource);
                }
                // Third associate
                if (2 < saleAssociateNb) {
                    resource = new SaleAssociate();
                    resource.setKey(2 * saleAssociateKey);
                    resource.setStoreKey(storeKey); // Same store
                    resource.setConsumerKey(3 * consumerKey);
                    resource.addCriterion("another-something", java.text.Collator.getInstance(Locale.ENGLISH));
                    resource.addCriterion(tag, java.text.Collator.getInstance(Locale.ENGLISH));
                    resources.add(resource);
                }
                return resources;
            }
        });
    }

    @Test
    public void testHasRobotAlreadyContactedI() throws DataSourceException {
        BaseSteps.setMockSettingsOperations(new SettingsOperations() {
            @Override
            public Settings getSettings(PersistenceManager pm) throws DataSourceException {
                return new Settings();
            }
        });

        assertFalse(DemandProcessor.hasRobotAlreadyContacted(new MockPersistenceManager(), new Demand()));
    }

    @Test
    public void testHasRobotAlreadyContactedII() throws DataSourceException {
        BaseSteps.setMockSettingsOperations(new SettingsOperations() {
            @Override
            public Settings getSettings(PersistenceManager pm) throws DataSourceException {
                Settings resource = new Settings();
                resource.setRobotSaleAssociateKey(robotKey);
                return resource;
            }
        });

        assertFalse(DemandProcessor.hasRobotAlreadyContacted(new MockPersistenceManager(), new Demand()));
    }

    @Test
    public void testHasRobotAlreadyContactedIII() throws DataSourceException {
        Demand demand = new Demand();
        demand.addSaleAssociateKey(robotKey);

        BaseSteps.setMockSettingsOperations(new SettingsOperations() {
            @Override
            public Settings getSettings(PersistenceManager pm) throws DataSourceException {
                Settings resource = new Settings();
                resource.setRobotSaleAssociateKey(robotKey);
                return resource;
            }
        });

        assertTrue(DemandProcessor.hasRobotAlreadyContacted(new MockPersistenceManager(), demand));
    }

    @Test
    public void testProcessI() throws DataSourceException, InvalidIdentifierException {
        //
        // Try to process an already canceled demand
        //
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long ownerKey) {
                assertEquals(demandKey, key);
                Demand resource = new Demand() {
                    @Override
                    public Demand resetLists() {
                        return super.resetLists();
                    }
                }.resetLists();
                resource.setKey(demandKey);
                resource.setState(State.cancelled);
                return resource;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(demandKey, demand.getKey());
                return demand;
            }
        });

        DemandProcessor.process(new MockPersistenceManager(), demandKey, Boolean.FALSE);

        assertNull(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousQueue());
    }

    @Test
    public void testProcessII() throws DataSourceException, InvalidIdentifierException {
        //
        // Process the demand with #demo hash tag, but robot has already been contacted
        //
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long ownerKey) {
                assertEquals(demandKey, key);
                Demand resource = new Demand();
                resource.setKey(demandKey);
                resource.setState(State.published);
                resource.setOwnerKey(consumerKey);
                resource.addHashTag(RobotResponder.ROBOT_DEMO_HASH_TAG);
                resource.addSaleAssociateKey(robotKey);
                return resource;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(demandKey, demand.getKey());
                return demand;
            }
        });

        BaseSteps.setMockSettingsOperations(new SettingsOperations() {
            @Override
            public Settings getSettings(PersistenceManager pm) throws DataSourceException {
                Settings resource = new Settings();
                resource.setRobotSaleAssociateKey(robotKey);
                return resource;
            }
        });


        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer resource = new Consumer();
                resource.setKey(consumerKey);
                return resource;
            }
        });

        DemandProcessor.process(new MockPersistenceManager(), demandKey, Boolean.FALSE);

        assertNull(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousQueue());
    }

    @Test
    public void testProcessIII() throws DataSourceException, InvalidIdentifierException {
        //
        // Process the demand with #demo hash tag, and robot has not yet been contacted
        //
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long ownerKey) {
                assertEquals(demandKey, key);
                Demand resource = new Demand();
                resource.setKey(demandKey);
                resource.setState(State.published);
                resource.setOwnerKey(consumerKey);
                resource.addHashTag(RobotResponder.ROBOT_DEMO_HASH_TAG);
                return resource;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(demandKey, demand.getKey());
                assertEquals(robotKey, demand.getSaleAssociateKeys().get(0));
                return demand;
            }
        });

        BaseSteps.setMockSettingsOperations(new SettingsOperations() {
            @Override
            public Settings getSettings(PersistenceManager pm) throws DataSourceException {
                Settings resource = new Settings();
                resource.setRobotSaleAssociateKey(robotKey);
                return resource;
            }
        });


        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer resource = new Consumer();
                resource.setKey(consumerKey);
                return resource;
            }
        });

        DemandProcessor.process(new MockPersistenceManager(), demandKey, Boolean.FALSE);

        MockQueue queue = ((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousQueue();
        assertEquals(1, queue.getHistory().size());
    }

    @Test
    public void testProcessIV() throws DataSourceException, InvalidIdentifierException {
        //
        // Process a normal demand with the associate already contacted on two concerned
        //
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long ownerKey) {
                assertEquals(demandKey, key);
                Demand resource = new Demand();
                resource.setKey(demandKey);
                resource.setState(State.published);
                resource.setOwnerKey(consumerKey);
                resource.setLocationKey(locationKey);
                resource.setInfluencerKey(influencerKey);
                resource.addSaleAssociateKey(saleAssociateKey);
                resource.setContent("what-s-in-your-mind " + tag);
                return resource;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(demandKey, demand.getKey());
                assertEquals(2, demand.getSaleAssociateKeys().size());
                assertEquals(saleAssociateKey, demand.getSaleAssociateKeys().get(0)); // Was initially in the array
                assertEquals(2 * saleAssociateKey, demand.getSaleAssociateKeys().get(1).longValue()); // Has been added
                return demand;
            }
        });

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertTrue(consumerKey == key || 2 * consumerKey == key);
                Consumer resource = new Consumer();
                resource.setKey(key);
                resource.setPreferredConnection(Source.api);
                resource.setSaleAssociateKey(consumerKey == key ? saleAssociateKey : 2 * saleAssociateKey);
                return resource;
            }
        });

        BaseSteps.setMockInfluencerOperations(new InfluencerOperations() {
            @Override
            public Influencer getInfluencer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(influencerKey, key);
                Influencer resource = new Influencer();
                resource.setConsumerKey(consumerKey);
                return resource;
            }
        });

        identifyTwoSaleAssociates(2);

        DemandProcessor.process(new MockPersistenceManager(), demandKey, Boolean.FALSE);

        assertNull(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousQueue());
    }

    @Test
    public void testProcessV() throws DataSourceException, InvalidIdentifierException {
        //
        // Process a normal demand with the associate not yet contacted, plus the influencer
        //
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long ownerKey) {
                assertEquals(demandKey, key);
                Demand resource = new Demand();
                resource.setKey(demandKey);
                resource.setState(State.published);
                resource.setOwnerKey(consumerKey);
                resource.setLocationKey(locationKey);
                resource.setInfluencerKey(influencerKey);
                resource.setContent("what-s-in-your-mind " + tag);
                return resource;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(demandKey, demand.getKey());
                assertEquals(2, demand.getSaleAssociateKeys().size());
                assertEquals(2 * saleAssociateKey, demand.getSaleAssociateKeys().get(0).longValue()); // Has been added
                assertEquals(saleAssociateKey, demand.getSaleAssociateKeys().get(1)); // Has been added as the influencer
                return demand;
            }
        });

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertTrue(consumerKey == key || 2 * consumerKey == key);
                Consumer resource = new Consumer();
                resource.setKey(key);
                resource.setSaleAssociateKey(consumerKey == key ? saleAssociateKey : 2 * saleAssociateKey);
                resource.setPreferredConnection(Source.api);
                return resource;
            }
        });

        BaseSteps.setMockInfluencerOperations(new InfluencerOperations() {
            @Override
            public Influencer getInfluencer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(influencerKey, key);
                Influencer resource = new Influencer();
                resource.setKey(influencerKey);
                resource.setConsumerKey(consumerKey);
                return resource;
            }
        });

        identifyTwoSaleAssociates(1);

        DemandProcessor.process(new MockPersistenceManager(), demandKey, Boolean.FALSE);

        assertNull(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousQueue());
    }

    @Test
    public void testProcessVI() throws DataSourceException, InvalidIdentifierException {
        //
        // Process a normal demand with three associate not yet contacted, influencer being included
        //
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long ownerKey) {
                assertEquals(demandKey, key);
                Demand resource = new Demand();
                resource.setKey(demandKey);
                resource.setState(State.published);
                resource.setOwnerKey(consumerKey);
                resource.setLocationKey(locationKey);
                resource.setInfluencerKey(influencerKey);
                resource.setContent("what-s-in-your-mind " + tag);
                return resource;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertEquals(demandKey, demand.getKey());
                assertEquals(2, demand.getSaleAssociateKeys().size());
                assertEquals(2 * saleAssociateKey, demand.getSaleAssociateKeys().get(0).longValue()); // Has been added
                assertEquals(saleAssociateKey, demand.getSaleAssociateKeys().get(1)); // Has been added as the influencer
                return demand;
            }
        });

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertTrue(consumerKey == key || 2 * consumerKey == key || 3 * consumerKey == key);
                Consumer resource = new Consumer();
                resource.setKey(key);
                resource.setSaleAssociateKey(consumerKey == key ? saleAssociateKey : 2 * consumerKey == key ? 2 * saleAssociateKey : 3 * saleAssociateKey);
                resource.setPreferredConnection(Source.api);
                return resource;
            }
        });

        BaseSteps.setMockInfluencerOperations(new InfluencerOperations() {
            @Override
            public Influencer getInfluencer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(influencerKey, key);
                Influencer resource = new Influencer();
                resource.setConsumerKey(consumerKey);
                return resource;
            }
        });

        identifyTwoSaleAssociates(3);

        DemandProcessor.process(new MockPersistenceManager(), demandKey, Boolean.FALSE);

        assertNull(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousQueue());
    }

    @Test
    public void testProcessVII() throws DataSourceException, InvalidIdentifierException {
        //
        // Test entry point for MaelzelServlet with the key of a demand which has been canceled
        //
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public Demand getDemand(PersistenceManager pm, Long key, Long ownerKey) {
                assertEquals(demandKey, key);
                Demand resource = new Demand();
                resource.setKey(demandKey);
                resource.setState(State.cancelled);
                return resource;
            }
        });

        DemandProcessor.process(demandKey, Boolean.FALSE);

        assertNull(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousQueue());
    }

    @Test
    public void testBatchProcessI() throws DataSourceException, InvalidIdentifierException {
        //
        // Process a normal demand with three associate not yet contacted, influencer being included
        //
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Long> getDemandKeys(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
                return new ArrayList<Long>();
            }
        });

        DemandProcessor.batchProcess();

        assertNull(((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousQueue());
    }

    @Test
    public void testBatchProcessII() throws DataSourceException, InvalidIdentifierException {
        //
        // Process a normal demand with three associate not yet contacted, influencer being included
        //
        BaseSteps.setMockDemandOperations(new DemandOperations() {
            @Override
            public List<Long> getDemandKeys(PersistenceManager pm, Map<String, Object> parameters, int limit) throws DataSourceException {
                List<Long> resources = new ArrayList<Long>();
                resources.add(12345L);
                resources.add(23456L);
                resources.add(34567L);
                return resources;
            }
        });

        DemandProcessor.batchProcess();

        assertEquals(3, ((MockBaseOperations) BaseSteps.getBaseOperations()).getPreviousQueue().getHistory().size());
    }
}
