package twetailer.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.DataSourceException;
import twetailer.adapter.TwitterUtils;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.MockPersistenceManager;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twitter4j.DirectMessage;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TestDemandValidator {

    private class MockBaseOperations extends BaseOperations {
        private PersistenceManager pm = new MockPersistenceManager();
        @Override
        public PersistenceManager getPersistenceManager() {
            return pm;
        }
    };

    @SuppressWarnings("serial")
    private class MockTwitter extends Twitter {
        private Long twitterId;
        public MockTwitter(Long twitterId) {
            this.twitterId = twitterId;
        }
        private String sentMessage;
        public String getSentMessage() {
            return sentMessage;
        }
        @Override
        public DirectMessage sendDirectMessage(String id, String text) {
            assertEquals(twitterId.toString(), id);
            assertNotSame(0, text.length());
            sentMessage = text;
            return null;
        }
    };

    final Long consumerKey = 12345L;
    MockTwitter twitterAccount;

    @Before
    public void setUp() throws Exception {
        // Inject the fake Twitter account
        twitterAccount = new MockTwitter(consumerKey);
        TwitterUtils.releaseTwetailerAccount(twitterAccount);

        // ConsumerOperations mock
        ConsumerOperations consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                assertEquals(consumerKey, key);
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                consumer.setTwitterId(consumerKey);
                consumer.setLanguage(LocaleValidator.DEFAULT_LANGUAGE);
                return consumer;
            }
        };

        // Install the mocks
        DemandValidator._baseOperations = new MockBaseOperations();
        DemandValidator.consumerOperations = consumerOperations;
    }

    @After
    public void tearDown() {
        // Remove the fake Twitter account
        TwitterUtils.getTwetailerAccount();
    }

    @Test
    public void testConstructor() {
        new DemandValidator();
    }

    @Test
    public void testProcessNoDemand() throws DataSourceException {
        // DemandOperations mock
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Demand.STATE, key);
                assertEquals(CommandSettings.State.open.toString(), (String) value);
                return new ArrayList<Demand>();
            }
        };

        DemandValidator.process();

        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessI() throws DataSourceException {
        //
        // Invalid criteria
        //

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Demand.STATE, key);
                assertEquals(CommandSettings.State.open.toString(), (String) value);
                Demand demand = new Demand() {
                    @Override
                    public List<String> getCriteria() {
                        return null;
                    }
                };
                demand.setKey(demandKey);
                demand.setConsumerKey(consumerKey);
                List<Demand> demands = new ArrayList<Demand>();
                demands.add(demand);
                return demands;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        };

        // Process the test case
        DemandValidator.process();

        assertNotNull(twitterAccount.getSentMessage());
        assertTrue(twitterAccount.getSentMessage().contains(demandKey.toString()));
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessII() throws DataSourceException {
        //
        // Invalid criteria
        //

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Demand.STATE, key);
                assertEquals(CommandSettings.State.open.toString(), (String) value);
                Demand demand = new Demand() {
                    @Override
                    public List<String> getCriteria() {
                        return new ArrayList<String>();
                    }
                };
                demand.setKey(demandKey);
                demand.setConsumerKey(consumerKey);
                List<Demand> demands = new ArrayList<Demand>();
                demands.add(demand);
                return demands;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        };

        // Process the test case
        DemandValidator.process();

        assertNotNull(twitterAccount.getSentMessage());
        assertTrue(twitterAccount.getSentMessage().contains(demandKey.toString()));
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessIII() throws DataSourceException {
        //
        // Valid criteria
        // Invalid expiration date
        //

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Demand.STATE, key);
                assertEquals(CommandSettings.State.open.toString(), (String) value);
                Demand demand = new Demand() {
                    @Override
                    public Date getExpirationDate() {
                        return null;
                    }
                };
                demand.setKey(demandKey);
                demand.setConsumerKey(consumerKey);
                demand.addCriterion("test");
                List<Demand> demands = new ArrayList<Demand>();
                demands.add(demand);
                return demands;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        };

        // Process the test case
        DemandValidator.process();

        assertNotNull(twitterAccount.getSentMessage());
        assertTrue(twitterAccount.getSentMessage().contains(demandKey.toString()));
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessIV() throws DataSourceException {
        //
        // Valid criteria
        // Invalid expiration date
        //

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Demand.STATE, key);
                assertEquals(CommandSettings.State.open.toString(), (String) value);
                Demand demand = new Demand() {
                    @Override
                    public Date getExpirationDate() {
                        return new Date(12345L);
                    }
                };
                demand.setKey(demandKey);
                demand.setConsumerKey(consumerKey);
                demand.addCriterion("test");
                List<Demand> demands = new ArrayList<Demand>();
                demands.add(demand);
                return demands;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        };

        // Process the test case
        DemandValidator.process();

        assertNotNull(twitterAccount.getSentMessage());
        assertTrue(twitterAccount.getSentMessage().contains(demandKey.toString()));
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessV() throws DataSourceException {
        //
        // Valid criteria
        // Valid expiration date
        // Invalid range
        //

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Demand.STATE, key);
                assertEquals(CommandSettings.State.open.toString(), (String) value);
                Demand demand = new Demand() {
                    @Override
                    public Double getRange() {
                        return null;
                    }
                };
                demand.setKey(demandKey);
                demand.setConsumerKey(consumerKey);
                demand.addCriterion("test");
                demand.setRangeUnit(LocaleValidator.KILOMETER_UNIT);
                List<Demand> demands = new ArrayList<Demand>();
                demands.add(demand);
                return demands;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        };

        // Process the test case
        DemandValidator.process();

        assertNotNull(twitterAccount.getSentMessage());
        assertTrue(twitterAccount.getSentMessage().contains(demandKey.toString()));
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessVI() throws DataSourceException {
        //
        // Valid criteria
        // Valid expiration date
        // Invalid range
        //

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Demand.STATE, key);
                assertEquals(CommandSettings.State.open.toString(), (String) value);
                Demand demand = new Demand() {
                    @Override
                    public Double getRange() {
                        return 0.0D;
                    }
                };
                demand.setKey(demandKey);
                demand.setConsumerKey(consumerKey);
                demand.addCriterion("test");
                demand.setRangeUnit(LocaleValidator.KILOMETER_UNIT);
                List<Demand> demands = new ArrayList<Demand>();
                demands.add(demand);
                return demands;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        };

        // Process the test case
        DemandValidator.process();

        assertNotNull(twitterAccount.getSentMessage());
        assertTrue(twitterAccount.getSentMessage().contains(demandKey.toString()));
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessVII() throws DataSourceException {
        //
        // Valid criteria
        // Valid expiration date
        // Invalid range
        //

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Demand.STATE, key);
                assertEquals(CommandSettings.State.open.toString(), (String) value);
                Demand demand = new Demand() {
                    @Override
                    public Double getRange() {
                        return null;
                    }
                };
                demand.setKey(demandKey);
                demand.setConsumerKey(consumerKey);
                demand.addCriterion("test");
                demand.setRangeUnit(LocaleValidator.MILE_UNIT);
                List<Demand> demands = new ArrayList<Demand>();
                demands.add(demand);
                return demands;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        };

        // Process the test case
        DemandValidator.process();

        assertNotNull(twitterAccount.getSentMessage());
        assertTrue(twitterAccount.getSentMessage().contains(demandKey.toString()));
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessVIII() throws DataSourceException {
        //
        // Valid criteria
        // Valid expiration date
        // Invalid range
        //

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Demand.STATE, key);
                assertEquals(CommandSettings.State.open.toString(), (String) value);
                Demand demand = new Demand() {
                    @Override
                    public Double getRange() {
                        return 0.0D;
                    }
                };
                demand.setKey(demandKey);
                demand.setConsumerKey(consumerKey);
                demand.addCriterion("test");
                demand.setRangeUnit(LocaleValidator.MILE_UNIT);
                List<Demand> demands = new ArrayList<Demand>();
                demands.add(demand);
                return demands;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        };

        // Process the test case
        DemandValidator.process();

        assertNotNull(twitterAccount.getSentMessage());
        assertTrue(twitterAccount.getSentMessage().contains(demandKey.toString()));
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessIX() throws DataSourceException {
        //
        // Valid criteria
        // Valid expiration date
        // Valid range
        // Invalid quantity
        //

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Demand.STATE, key);
                assertEquals(CommandSettings.State.open.toString(), (String) value);
                Demand demand = new Demand() {
                    @Override
                    public Long getQuantity() {
                        return null;
                    }
                };
                demand.setKey(demandKey);
                demand.setConsumerKey(consumerKey);
                demand.addCriterion("test");
                demand.setRangeUnit(LocaleValidator.KILOMETER_UNIT);
                List<Demand> demands = new ArrayList<Demand>();
                demands.add(demand);
                return demands;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        };

        // Process the test case
        DemandValidator.process();

        assertNotNull(twitterAccount.getSentMessage());
        assertTrue(twitterAccount.getSentMessage().contains(demandKey.toString()));
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessX() throws DataSourceException {
        //
        // Valid criteria
        // Valid expiration date
        // Valid range
        // Invalid quantity
        //

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Demand.STATE, key);
                assertEquals(CommandSettings.State.open.toString(), (String) value);
                Demand demand = new Demand() {
                    @Override
                    public Long getQuantity() {
                        return 0L;
                    }
                };
                demand.setKey(demandKey);
                demand.setConsumerKey(consumerKey);
                demand.addCriterion("test");
                demand.setRangeUnit(LocaleValidator.KILOMETER_UNIT);
                List<Demand> demands = new ArrayList<Demand>();
                demands.add(demand);
                return demands;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        };

        // Process the test case
        DemandValidator.process();

        assertNotNull(twitterAccount.getSentMessage());
        assertTrue(twitterAccount.getSentMessage().contains(demandKey.toString()));
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessXI() throws DataSourceException {
        //
        // Valid criteria
        // Valid expiration date
        // Valid range
        // Valid quantity
        // Invalid location key
        //

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Demand.STATE, key);
                assertEquals(CommandSettings.State.open.toString(), (String) value);
                Demand demand = new Demand() {
                    @Override
                    public Long getQuantity() {
                        return 10L;
                    }
                    @Override
                    public Long getLocationKey() {
                        return null;
                    }
                };
                demand.setKey(demandKey);
                demand.setConsumerKey(consumerKey);
                demand.addCriterion("test");
                demand.setRangeUnit(LocaleValidator.KILOMETER_UNIT);
                List<Demand> demands = new ArrayList<Demand>();
                demands.add(demand);
                return demands;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        };

        // Process the test case
        DemandValidator.process();

        assertNotNull(twitterAccount.getSentMessage());
        assertTrue(twitterAccount.getSentMessage().contains(demandKey.toString()));
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessXII() throws DataSourceException {
        //
        // Valid criteria
        // Valid expiration date
        // Valid range
        // Valid quantity
        // Invalid location key
        //

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Demand.STATE, key);
                assertEquals(CommandSettings.State.open.toString(), (String) value);
                Demand demand = new Demand() {
                    @Override
                    public Long getLocationKey() {
                        return 0L;
                    }
                };
                demand.setKey(demandKey);
                demand.setConsumerKey(consumerKey);
                demand.addCriterion("test");
                demand.setRangeUnit(LocaleValidator.KILOMETER_UNIT);
                List<Demand> demands = new ArrayList<Demand>();
                demands.add(demand);
                return demands;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        };

        // Process the test case
        DemandValidator.process();

        assertNotNull(twitterAccount.getSentMessage());
        assertTrue(twitterAccount.getSentMessage().contains(demandKey.toString()));
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessXIII() throws DataSourceException {
        //
        // Valid criteria
        // Valid expiration date
        // Valid range
        // Valid quantity
        // Valid location key
        // Invalid location coordinates and invalid resolution
        //

        // LocationOperations mock
        final Long locationKey = 54321L;
        DemandValidator.locationOperations = new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                assertEquals(locationKey, key);
                Location location = new Location() {
                    @Override
                    public String getCountryCode() {
                        return "zzz";
                    }
                };
                location.setLatitude(0.0D);
                location.setLongitude(Location.INVALID_COORDINATE);
                location.setPostalCode("zzz");
                return location;
            }
            @Override
            public Location updateLocation(PersistenceManager pm, Location location) {
                assertNotNull(location);
                assertEquals(90.0D, location.getLatitude(), 0.0D);
                assertEquals(0.0D, location.getLongitude(), 0.0D);
                return location;
            }
        };

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Demand.STATE, key);
                assertEquals(CommandSettings.State.open.toString(), (String) value);
                Demand demand = new Demand() {
                    @Override
                    public Long getLocationKey() {
                        return locationKey;
                    }
                };
                demand.setKey(demandKey);
                demand.setConsumerKey(consumerKey);
                demand.addCriterion("test");
                demand.setRangeUnit(LocaleValidator.KILOMETER_UNIT);
                demand.setLocationKey(locationKey);
                List<Demand> demands = new ArrayList<Demand>();
                demands.add(demand);
                return demands;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        };

        // Process the test case
        DemandValidator.process();

        assertNotNull(twitterAccount.getSentMessage());
        assertTrue(twitterAccount.getSentMessage().contains(demandKey.toString()));
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessXIV() throws DataSourceException {
        //
        // Valid criteria
        // Valid expiration date
        // Valid range
        // Valid quantity
        // Valid location key
        // Invalid location coordinates with valid resolution
        //

        // LocationOperations mock
        final Long locationKey = 54321L;
        DemandValidator.locationOperations = new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                assertEquals(locationKey, key);
                Location location = new Location();
                location.setLatitude(0.0D);
                location.setLongitude(Location.INVALID_COORDINATE);
                location.setPostalCode("H0H0H0");
                location.setCountryCode("CA");
                return location;
            }
        };

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Demand.STATE, key);
                assertEquals(CommandSettings.State.open.toString(), (String) value);
                Demand demand = new Demand() {
                    @Override
                    public Long getLocationKey() {
                        return locationKey;
                    }
                };
                demand.setKey(demandKey);
                demand.setConsumerKey(consumerKey);
                demand.addCriterion("test");
                demand.setRangeUnit(LocaleValidator.KILOMETER_UNIT);
                demand.setLocationKey(locationKey);
                List<Demand> demands = new ArrayList<Demand>();
                demands.add(demand);
                return demands;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.published, demand.getState());
                return demand;
            }
        };

        // Process the test case
        DemandValidator.process();

        assertNull(twitterAccount.getSentMessage());
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessXV() throws DataSourceException {
        //
        // Valid criteria
        // Valid expiration date
        // Valid range
        // Valid quantity
        // Valid location key
        // Valid location coordinates
        //

        // LocationOperations mock
        final Long locationKey = 54321L;
        DemandValidator.locationOperations = new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                assertEquals(locationKey, key);
                Location location = new Location();
                location.setLatitude(0.0D);
                location.setLongitude(0.0D);
                return location;
            }
        };

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Demand.STATE, key);
                assertEquals(CommandSettings.State.open.toString(), (String) value);
                Demand demand = new Demand() {
                    @Override
                    public Long getLocationKey() {
                        return locationKey;
                    }
                };
                demand.setKey(demandKey);
                demand.setConsumerKey(consumerKey);
                demand.addCriterion("test");
                demand.setRangeUnit(LocaleValidator.KILOMETER_UNIT);
                demand.setLocationKey(locationKey);
                List<Demand> demands = new ArrayList<Demand>();
                demands.add(demand);
                return demands;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.published, demand.getState());
                return demand;
            }
        };

        // Process the test case
        DemandValidator.process();

        assertNull(twitterAccount.getSentMessage());
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessXVI() throws DataSourceException {
        //
        // Valid criteria
        // Valid expiration date
        // Valid range
        // Valid quantity
        // Valid location key
        // Impossible to get the corresponding Location instance
        //

        // LocationOperations mock
        final Long locationKey = 54321L;
        DemandValidator.locationOperations = new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws DataSourceException {
                throw new DataSourceException("done in purpose");
            }
        };

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, String key, Object value, int limit) {
                assertEquals(Demand.STATE, key);
                assertEquals(CommandSettings.State.open.toString(), (String) value);
                Demand demand = new Demand() {
                    @Override
                    public Long getLocationKey() {
                        return locationKey;
                    }
                };
                demand.setKey(demandKey);
                demand.setConsumerKey(consumerKey);
                demand.addCriterion("test");
                demand.setRangeUnit(LocaleValidator.KILOMETER_UNIT);
                demand.setLocationKey(locationKey);
                List<Demand> demands = new ArrayList<Demand>();
                demands.add(demand);
                return demands;
            }
            @Override
            public Demand updateDemand(PersistenceManager pm, Demand demand) {
                assertNotNull(demand);
                assertEquals(CommandSettings.State.invalid, demand.getState());
                return demand;
            }
        };

        // Process the test case
        DemandValidator.process();

        assertNotNull(twitterAccount.getSentMessage());
        assertTrue(twitterAccount.getSentMessage().contains(demandKey.toString()));
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    public void testProcessXVII() throws DataSourceException {
        //
        // Impossible to get the Demand instance
        //

        // ConsumerOperations mock
        DemandValidator.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) throws DataSourceException {
                assertEquals(consumerKey, key);
                throw new DataSourceException("done in purpose");
            }
        };

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, String key, Object value, int limit) throws DataSourceException {
                assertEquals(Demand.STATE, key);
                assertEquals(CommandSettings.State.open.toString(), (String) value);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setConsumerKey(consumerKey);
                List<Demand> demands = new ArrayList<Demand>();
                demands.add(demand);
                return demands;
            }
        };

        // Process the test case
        DemandValidator.process();

        assertNull(twitterAccount.getSentMessage());
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }

    @Test
    @SuppressWarnings("serial")
    public void testProcessXVIII() throws DataSourceException {
        //
        // Impossible to tweet the warnings
        //

        TwitterUtils.getTwetailerAccount();
        TwitterUtils.releaseTwetailerAccount(new Twitter() {
            @Override
            public DirectMessage sendDirectMessage(String id, String text) throws TwitterException {
                throw new TwitterException("done in purpose");
            }
        });

        // DemandOperations mock
        final Long demandKey = 67890L;
        DemandValidator.demandOperations = new DemandOperations() {
            @Override
            public List<Demand> getDemands(PersistenceManager pm, String key, Object value, int limit) throws DataSourceException {
                assertEquals(Demand.STATE, key);
                assertEquals(CommandSettings.State.open.toString(), (String) value);
                Demand demand = new Demand();
                demand.setKey(demandKey);
                demand.setConsumerKey(consumerKey);
                List<Demand> demands = new ArrayList<Demand>();
                demands.add(demand);
                return demands;
            }
        };

        // Process the test case
        DemandValidator.process();

        assertNull(twitterAccount.getSentMessage());
        assertTrue(DemandValidator._baseOperations.getPersistenceManager().isClosed());
    }
}
