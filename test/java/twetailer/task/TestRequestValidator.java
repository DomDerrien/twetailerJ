package twetailer.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.List;

import javamocks.util.logging.MockLogger;

import javax.jdo.MockPersistenceManager;
import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.InvalidIdentifierException;
import twetailer.connector.MockTwitterConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.BaseOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.LocationOperations;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.Request;
import twetailer.task.RequestValidator.ValidationStatus;
import twetailer.task.step.BaseSteps;
import twetailer.validator.LocaleValidator;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import domderrien.i18n.DateUtils;

public class TestRequestValidator {

    private static LocalServiceTestHelper  helper;
    private Long nowTime;

    @BeforeClass
    public static void setUpBeforeClass() {
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
        RequestValidator.setMockLogger(new MockLogger("test", null));
    }

    @Before
    public void setUp() throws Exception {
        helper.setUp();
        nowTime = DateUtils.getNowDate().getTime();
    }

    @After
    public void tearDown() {
        MockTwitterConnector.restoreTwitterConnector();
    }

    @Test
    public void testConstructor() {
        new TestRequestValidator();
    }

    @Test
    public void testGetLogger() {
        RequestValidator.getLogger();
    }

    @Test
    public void testCheckRequestFieldsIa() {
        Request request = new Request() {
            @Override
            public List<String> getCriteria() {
                return null;
            }
        };
        assertEquals(ValidationStatus.noTagNorHashTag, RequestValidator.checkRequestFields(new MockPersistenceManager(), new Consumer(), request));
    }

    @Test
    public void testCheckRequestFieldsIb() {
        Request request = new Request() {
            @Override
            public List<String> getHashTags() {
                return null;
            }
        };
        assertEquals(ValidationStatus.noTagNorHashTag, RequestValidator.checkRequestFields(new MockPersistenceManager(), new Consumer(), request));
    }

    @Test
    public void testCheckRequestFieldsIc() {
        Request request = new Request();
        assertEquals(ValidationStatus.noTagNorHashTag, RequestValidator.checkRequestFields(new MockPersistenceManager(), new Consumer(), request));
    }

    @Test
    public void testCheckRequestFieldsIIa() {
        Request request = new Request() {
            @Override
            public Date getDueDate() {
                return null;
            }
        };
        request.addCriterion("tag");
        assertEquals(ValidationStatus.noDueDate, RequestValidator.checkRequestFields(new MockPersistenceManager(), new Consumer(), request));
    }

    @Test
    public void testCheckRequestFieldsIIb() {
        Request request = new Request() {
            @Override
            public Date getDueDate() {
                return new Date(nowTime - (2 * 60 * 60) * 1000L); // 2 hours in the past
            }
        };
        request.addCriterion("tag");
        assertEquals(ValidationStatus.dueDateInPast, RequestValidator.checkRequestFields(new MockPersistenceManager(), new Consumer(), request));
    }

    @Test
    public void testCheckRequestFieldsIIc() {
        Request request = new Request() {
            @Override
            public Date getDueDate() {
                return new Date(nowTime + (2 * 365 * 24 * 60 * 60) * 1000L); // 2 years in the future
            }
        };
        request.addCriterion("tag");
        assertEquals(ValidationStatus.dueDateTooFarInFuture, RequestValidator.checkRequestFields(new MockPersistenceManager(), new Consumer(), request));
    }

    @Test
    public void testCheckRequestFieldsIId() {
        Request request = new Request() {
            @Override
            public Date getExpirationDate() {
                return null;
            }
        };
        request.addHashTag("hashtag");
        request.setDueDate(new Date(nowTime + (2 * 60 * 60) * 1000L)); // 2 hours in the future
        assertEquals(ValidationStatus.noExpirationDate, RequestValidator.checkRequestFields(new MockPersistenceManager(), new Consumer(), request));
    }

    @Test
    public void testCheckRequestFieldsIIe() {
        Request request = new Request() {
            @Override
            public Date getExpirationDate() {
                return new Date(nowTime - (2 * 60 * 60) * 1000L); // 2 hours in the past
            }
        };
        request.addHashTag("hashtag");
        request.setDueDate(new Date(nowTime + (2 * 60 * 60) * 1000L)); // 2 hours in the future
        assertEquals(ValidationStatus.expirationDateInPast, RequestValidator.checkRequestFields(new MockPersistenceManager(), new Consumer(), request));
    }

    @Test
    public void testCheckRequestFieldsIIf() {
        Request request = new Request() {
            @Override
            public Date getExpirationDate() {
                return new Date(nowTime + (2 * 365 * 24 * 60 * 60) * 1000L); // 2 years in the future
            }
        };
        request.addHashTag("hashtag");
        request.setDueDate(new Date(nowTime + (2 * 60 * 60) * 1000L)); // 2 hours in the future
        assertEquals(ValidationStatus.expirationDateTooFarInFuture, RequestValidator.checkRequestFields(new MockPersistenceManager(), new Consumer(), request));
    }

    @Test
    public void testCheckRequestFieldsIIg() {
        Request request = new Request();
        request.addHashTag("hashtag");
        request.setExpirationDate(new Date(nowTime + (2 * 60 * 60) * 1000L)); // 2 hours in the future
        request.setDueDate(new Date(nowTime + (1 * 60 * 60) * 1000L)); // 1 hour in the future
        assertEquals(ValidationStatus.dueDateBeforeExpiration, RequestValidator.checkRequestFields(new MockPersistenceManager(), new Consumer(), request));
    }

    @Test
    public void testCheckRequestFieldsIIIa() {
        Request request = new Request() {
            @Override
            public Double getRange() {
                return null;
            }
        };
        request.addCriterion("tag");
        request.setRange(0.001);
        request.setRangeUnit(LocaleValidator.KILOMETER_UNIT);
        assertEquals(ValidationStatus.rangeTooSmall, RequestValidator.checkRequestFields(new MockPersistenceManager(), new Consumer(), request));
    }

    @Test
    public void testCheckRequestFieldsIIIb() {
        Request request = new Request();
        request.addCriterion("tag");
        request.setRange(0.001);
        request.setRangeUnit(LocaleValidator.KILOMETER_UNIT);
        assertEquals(ValidationStatus.rangeTooSmall, RequestValidator.checkRequestFields(new MockPersistenceManager(), new Consumer(), request));
    }

    @Test
    public void testCheckRequestFieldsIIIc() {
        Request request = new Request();
        request.addCriterion("tag");
        request.setRange(999999999999999999999.99D);
        request.setRangeUnit(LocaleValidator.KILOMETER_UNIT);
        assertEquals(ValidationStatus.rangeTooLarge, RequestValidator.checkRequestFields(new MockPersistenceManager(), new Consumer(), request));
    }

    @Test
    public void testCheckRequestFieldsIIId() {
        Request request = new Request() {
            @Override
            public Double getRange() {
                return null;
            }
        };
        request.addCriterion("tag");
        request.setRange(0.001);
        request.setRangeUnit(LocaleValidator.MILE_UNIT);
        assertEquals(ValidationStatus.rangeTooSmall, RequestValidator.checkRequestFields(new MockPersistenceManager(), new Consumer(), request));
    }

    @Test
    public void testCheckRequestFieldsIIIe() {
        Request request = new Request();
        request.addCriterion("tag");
        request.setRange(0.001);
        request.setRangeUnit(LocaleValidator.MILE_UNIT);
        assertEquals(ValidationStatus.rangeTooSmall, RequestValidator.checkRequestFields(new MockPersistenceManager(), new Consumer(), request));
    }

    @Test
    public void testCheckRequestFieldsIIIf() {
        Request request = new Request();
        request.addCriterion("tag");
        request.setRange(999999999999999999999.99D);
        request.setRangeUnit(LocaleValidator.MILE_UNIT);
        assertEquals(ValidationStatus.rangeTooLarge, RequestValidator.checkRequestFields(new MockPersistenceManager(), new Consumer(), request));
    }

    @Test
    public void testCheckRequestFieldsIVa() {
        Request request = new Request() {
            @Override
            public Long getQuantity() {
                return null;
            }
        };
        request.addCriterion("tag");
        request.setRangeUnit(LocaleValidator.MILE_UNIT);
        assertEquals(ValidationStatus.noQuantity, RequestValidator.checkRequestFields(new MockPersistenceManager(), new Consumer(), request));
    }

    @Test
    public void testCheckRequestFieldsIVb() {
        Request request = new Request();
        request.addCriterion("tag");
        request.setQuantity(0L);
        assertEquals(ValidationStatus.noQuantity, RequestValidator.checkRequestFields(new MockPersistenceManager(), new Consumer(), request));
    }

    @Test
    public void testCheckRequestFieldsVa() {
        Request request = new Request() {
            @Override
            public Long getLocationKey() {
                return null;
            }
        };
        request.addCriterion("tag");
        assertEquals(ValidationStatus.noLocationKey, RequestValidator.checkRequestFields(new MockPersistenceManager(), new Consumer(), request));
    }

    @Test
    public void testCheckRequestFieldsVb() {
        Request request = new Request();
        request.addCriterion("tag");
        request.setLocationKey(0L);
        assertEquals(ValidationStatus.noLocationKey, RequestValidator.checkRequestFields(new MockPersistenceManager(), new Consumer(), request));
    }

    @Test
    public void testCheckRequestFieldsVc() {
        final Long locationKey = 5654645232L;

        Request request = new Request();
        request.addCriterion("tag");
        request.setLocationKey(locationKey);

        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) throws InvalidIdentifierException {
                assertEquals(locationKey, key);
                throw new InvalidIdentifierException("Done in purpose!");
            }
        });
        assertEquals(ValidationStatus.invalidLocationKey, RequestValidator.checkRequestFields(new MockPersistenceManager(), new Consumer(), request));
    }

    @Test
    public void testCheckRequestFieldsVd() {
        final Long locationKey = 5654645232L;

        Request request = new Request();
        request.addCriterion("tag");
        request.setLocationKey(locationKey);

        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                assertEquals(locationKey, key);
                Location resource = new Location();
                resource.setKey(locationKey);
                resource.setPostalCode(LocaleValidator.DEFAULT_POSTAL_CODE_CA);
                return resource;
            }
        });
        assertEquals(ValidationStatus.invalidLocation, RequestValidator.checkRequestFields(new MockPersistenceManager(), new Consumer(), request));
    }

    @Test
    public void testCheckRequestFieldsVe() {
        final Long locationKey = 5654645232L;

        Request request = new Request();
        request.addCriterion("tag");
        request.setLocationKey(locationKey);

        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                assertEquals(locationKey, key);
                Location resource = new Location();
                resource.setKey(locationKey);
                resource.setPostalCode(LocaleValidator.DEFAULT_POSTAL_CODE_US);
                return resource;
            }
        });
        assertEquals(ValidationStatus.invalidLocation, RequestValidator.checkRequestFields(new MockPersistenceManager(), new Consumer(), request));
    }

    @Test
    public void testCheckRequestFieldsVf() {
        final Long locationKey = 5654645232L;

        Request request = new Request();
        request.addCriterion("tag");
        request.setLocationKey(locationKey);

        BaseSteps.setMockLocationOperations(new LocationOperations() {
            @Override
            public Location getLocation(PersistenceManager pm, Long key) {
                assertEquals(locationKey, key);
                Location resource = new Location();
                resource.setKey(locationKey);
                resource.setPostalCode(LocaleValidator.DEFAULT_POSTAL_CODE_ALT_US);
                return resource;
            }
        });
        assertEquals(ValidationStatus.invalidLocation, RequestValidator.checkRequestFields(new MockPersistenceManager(), new Consumer(), request));
    }
}
