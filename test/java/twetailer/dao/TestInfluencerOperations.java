package twetailer.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.cache.MockCacheFactory;
import javax.jdo.MockPersistenceManagerFactory;
import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.dto.Influencer;
import twetailer.task.step.BaseSteps;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class TestInfluencerOperations {

    private static LocalServiceTestHelper  helper;

    @BeforeClass
    public static void setUpBeforeClass() {
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    }

    @Before
    public void setUp() throws Exception {
        helper.setUp();
        BaseSteps.resetOperationControllers(false); // Use helper!
        CacheHandler.injectMockCacheFactory(new MockCacheFactory());
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();
        CacheHandler.injectMockCacheFactory(null);
        CacheHandler.injectMockCache(null);
    }

    @Test(expected=RuntimeException.class)
    public void testCreateWithFailureI() throws DataSourceException {
        new InfluencerOperations() {
            @Override
            public Influencer createInfluencer(PersistenceManager pm, Influencer influencer) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        }.createInfluencer(new Influencer());
    }

    @Test
    public void testCreateI() {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        InfluencerOperations ops = new InfluencerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        Influencer item = new Influencer();
        assertNull(item.getKey());

        item = ops.createInfluencer(item);
        assertNotNull(item.getKey());
        assertTrue(pm.isClosed());
    }

    @Test(expected=InvalidIdentifierException.class)
    public void testGetWithFailureI() throws InvalidIdentifierException {
        new InfluencerOperations().getInfluencer(543543L);
    }

    @Test(expected=RuntimeException.class)
    public void testGetWithFailureII() throws InvalidIdentifierException {
        new InfluencerOperations() {
            @Override
            public Influencer getInfluencer(PersistenceManager pm, Long key) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        }.getInfluencer(1234L);
    }

    @Test
    public void testGetI() throws InvalidIdentifierException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        InfluencerOperations ops = new InfluencerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };

        String name = "name";
        Influencer item = new Influencer();
        item.setName(name);
        item = ops.createInfluencer(pm, item);
        assertNotNull(item.getKey());

        Influencer retreived = ops.getInfluencer(pm, item.getKey());

        assertEquals(item.getKey(), retreived.getKey());
        assertEquals(item.getName(), retreived.getName());
    }

    @Test
    public void testGetII() throws InvalidIdentifierException {
        Influencer item = new InfluencerOperations().getInfluencer(null);
        assertEquals("AnotherSocialEconomy.com", item.getName());
    }

    @Test
    public void testGetIII() throws InvalidIdentifierException {
        Influencer item = new InfluencerOperations().getInfluencer(0L);
        assertEquals("AnotherSocialEconomy.com", item.getName());
    }

    @Test(expected=RuntimeException.class)
    public void testUpdateWithFailureI() throws DataSourceException {
        new InfluencerOperations(){
            @Override
            public Influencer updateInfluencer(PersistenceManager pm, Influencer influencer) {
                throw new RuntimeException("To exercise the 'finally { pm.close(); }' sentence.");
            }
        }.updateInfluencer(new Influencer());
    }

    @Test
    public void testUpdateI() throws InvalidIdentifierException {
        final PersistenceManager pm = new MockPersistenceManagerFactory().getPersistenceManager();
        InfluencerOperations ops = new InfluencerOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };

        String name = "name";
        Influencer item = new Influencer();
        item.setName(name);
        item = ops.createInfluencer(pm, item);
        assertNotNull(item.getKey());

        Influencer retreived = ops.getInfluencer(pm, item.getKey());

        assertEquals(item.getKey(), retreived.getKey());
        assertEquals(item.getName(), retreived.getName());

        item.setName("updated!");
        item = ops.updateInfluencer(pm, item);

        assertEquals("updated!", item.getName());
    }

    @Test
    public void testVerifyReferralIdValidityI() {
        final Long influencerKey = 12345L;
        final String referralId = influencerKey + "-" + "6749867986";

        BaseSteps.setMockInfluencerOperations(new InfluencerOperations() {
            @Override
            public Influencer getInfluencer(PersistenceManager pm, Long key) {
                Influencer influencer = new Influencer();
                influencer.setKey(influencerKey);
                influencer.setReferralId(referralId);
                return influencer;
            }
        });

        assertTrue(new InfluencerOperations().verifyReferralIdValidity(null, referralId + "-" + "00"));
    }

    @Test
    public void testVerifyReferralIdValidityII() {
        final Long influencerKey = 12345L;
        final String referralId = influencerKey + "-" + "6749867986";

        BaseSteps.setMockInfluencerOperations(new InfluencerOperations() {
            @Override
            public Influencer getInfluencer(PersistenceManager pm, Long key) {
                Influencer influencer = new Influencer();
                influencer.setKey(influencerKey);
                influencer.setReferralId(referralId + "54354");
                return influencer;
            }
        });

        assertFalse(new InfluencerOperations().verifyReferralIdValidity(null, referralId + "-" + "00"));
    }

    @Test
    public void testVerifyReferralIdValidityIII() {
        final Long influencerKey = 12345L;
        final String referralId = influencerKey + "6749867986"; // One missing separator

        BaseSteps.setMockInfluencerOperations(new InfluencerOperations() {
            @Override
            public Influencer getInfluencer(PersistenceManager pm, Long key) {
                fail("Unexpected call!");
                return null;
            }
        });

        assertFalse(new InfluencerOperations().verifyReferralIdValidity(null, referralId + "-" + "00"));
    }

    @Test
    public void testVerifyReferralIdValidityIV() {
        final Long influencerKey = 12345L;
        final String referralId = influencerKey + "-" + "6749867986" + "-"; // One extra separator

        BaseSteps.setMockInfluencerOperations(new InfluencerOperations() {
            @Override
            public Influencer getInfluencer(PersistenceManager pm, Long key) {
                fail("Unexpected call!");
                return null;
            }
        });

        assertFalse(new InfluencerOperations().verifyReferralIdValidity(null, referralId + "-" + "00"));
    }

    @Test
    public void testVerifyReferralIdValidityVa() {
        final Long influencerKey = 12345L;
        final String referralId = influencerKey + "-" + " " + "-"; // More than just digits (before '0')

        BaseSteps.setMockInfluencerOperations(new InfluencerOperations() {
            @Override
            public Influencer getInfluencer(PersistenceManager pm, Long key) {
                fail("Unexpected call!");
                return null;
            }
        });

        assertFalse(new InfluencerOperations().verifyReferralIdValidity(null, referralId + "-" + "00"));
    }

    @Test
    public void testVerifyReferralIdValidityVb() {
        final Long influencerKey = 12345L;
        final String referralId = influencerKey + "-" + "A" + "-"; // More than just digits (after '9')

        BaseSteps.setMockInfluencerOperations(new InfluencerOperations() {
            @Override
            public Influencer getInfluencer(PersistenceManager pm, Long key) {
                fail("Unexpected call!");
                return null;
            }
        });

        assertFalse(new InfluencerOperations().verifyReferralIdValidity(null, referralId + "-" + "00"));
    }

    @Test
    public void testVerifyReferralIdValidityVI() {
        final Long influencerKey = 12345L;
        final String referralId = influencerKey + "-" + "6749867986";

        BaseSteps.setMockInfluencerOperations(new InfluencerOperations() {
            @Override
            public Influencer getInfluencer(PersistenceManager pm, Long key) {
                fail("Unexpected call!");
                return null;
            }
        });

        assertFalse(new InfluencerOperations().verifyReferralIdValidity(null, referralId + "-" + "00000")); // Not exactly two significants digits
    }

    @Test
    public void testVerifyReferralIdValidityVII() {
        final Long influencerKey = 12345L;
        final String referralId = influencerKey + "-" + "6749867986";

        BaseSteps.setMockInfluencerOperations(new InfluencerOperations() {
            @Override
            public Influencer getInfluencer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                throw new InvalidIdentifierException("done in purpose!");
            }
        });

        assertFalse(new InfluencerOperations().verifyReferralIdValidity(null, referralId + "-" + "00"));
    }

    @Test
    public void testVerifyReferralIdValidityVIIIa() {
        assertFalse(new InfluencerOperations().verifyReferralIdValidity(null, null));
    }

    @Test
    public void testVerifyReferralIdValidityVIIIb() {
        assertFalse(new InfluencerOperations().verifyReferralIdValidity(null, "000")); // Definitively too short
    }

    @Test
    public void testGetInfluencerKeyI() {
        final Long influencerKey = 12345L;
        assertEquals(influencerKey, InfluencerOperations.getInfluencerKey(influencerKey + "-" + "000" + "-" + "00"));
    }

    @Test
    public void testGetInfluencerKeyII() {
        assertEquals(0L, InfluencerOperations.getInfluencerKey(null).longValue());
    }

    @Test
    public void testGetInfluencerKeyIII() {
        assertEquals(0L, InfluencerOperations.getInfluencerKey("").longValue());
    }

    @Test
    public void testGetReferralVariableIndexI() {
        final Long influencerKey = 12345L;
        final Long variablePart = 93L;
        assertEquals(variablePart, InfluencerOperations.getReferralVariableIndex(influencerKey + "-" + "000" + "-" + variablePart));
    }

    @Test
    public void testGetReferralVariableIndexII() {
        assertEquals(0L, InfluencerOperations.getReferralVariableIndex(null).longValue());
    }

    @Test
    public void testGetReferralVariableIndexIII() {
        assertEquals(0L, InfluencerOperations.getReferralVariableIndex("").longValue());
    }
}
