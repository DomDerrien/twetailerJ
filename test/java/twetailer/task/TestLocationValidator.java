package twetailer.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javamocks.io.MockInputStream;
import javamocks.util.logging.MockLogger;

import javax.jdo.MockPersistenceManager;
import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.DataSourceException;
import twetailer.connector.BaseConnector;
import twetailer.connector.MockTwitterConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.RawCommandOperations;
import twetailer.dto.Consumer;
import twetailer.dto.Location;
import twetailer.dto.RawCommand;
import twetailer.validator.LocaleValidator;
import twitter4j.DirectMessage;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import com.google.apphosting.api.MockAppEngineEnvironment;

import domderrien.i18n.LabelExtractor;

public class TestLocationValidator {

    MockAppEngineEnvironment appEnv = new MockAppEngineEnvironment();

    @Before
    public void setUp() throws Exception {
        LocationValidator.log = new MockLogger("unit-test", null);

        appEnv.setUp();
    }

    @After
    public void tearDown() throws Exception {
        LocationValidator.locationOperations = LocationValidator._baseOperations.getLocationOperations();
        LocationValidator.rawCommandOperations = LocationValidator._baseOperations.getRawCommandOperations();
        LocationValidator.consumerOperations = LocationValidator._baseOperations.getConsumerOperations();
        LocaleValidator.setValidatorStream(null);
        BaseConnector.resetLastCommunicationInSimulatedMode();

        appEnv.tearDown();
    }

    @Test
    public void testConstructor() {
        new LocationValidator();
    }

    @Test
    public void testProcessWithOneValidLocation() throws DataSourceException {
        final String postalCode = "H2N3C6";
        final String countryCode = "CA";
        final Long consumerKey = 111L;
        final Long rawCommandKey = 222L;
        final Double longitude = -73.3D;

        // LocationOperations mock
        LocationValidator.locationOperations = new LocationOperations() {
            @Override
            public List<Location> getLocations(PersistenceManager pm, String postalCode, String countryCode) {
                Location location = new Location();
                location.setLongitude(longitude);
                List<Location> locations = new ArrayList<Location>();
                locations.add(location);
                return locations;
            }
        };

        LocationValidator.process(postalCode, countryCode, consumerKey, rawCommandKey);
    }

    @Test
    public void testProcessWithOneIncompleteLocation() throws DataSourceException {
        final String postalCode = "H2N3C6";
        final String countryCode = "CA";
        final Long consumerKey = 111L;
        final Long rawCommandKey = 222L;
        final Double longitude = -73.3D;
        final Double latitude = 45.5D;

        // LocationOperations mock
        LocationValidator.locationOperations = new LocationOperations() {
            @Override
            public List<Location> getLocations(PersistenceManager pm, String postalCode, String countryCode) {
                Location location = new Location();
                location.setPostalCode(postalCode);
                location.setCountryCode(countryCode);
                List<Location> locations = new ArrayList<Location>();
                locations.add(location);
                return locations;
            }

            @Override
            public Location updateLocation(PersistenceManager pm, Location location) {
                assertEquals(postalCode, location.getPostalCode());
                assertEquals(countryCode, location.getCountryCode());
                assertEquals(longitude, location.getLongitude(), 0.0);
                assertEquals(latitude, location.getLatitude(), 0.0);
                return location;
            }
        };
        // RawCommandOperations mock
        LocationValidator.rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                RawCommand command = new RawCommand();
                command.setSource(Source.simulated);
                return command;
            }
        };
        // ConsumerOperations mock
        LocationValidator.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                Consumer consumer = new Consumer();
                return consumer;
            }
        };
        // LocaleValidator mock
        LocaleValidator.setValidatorStream(new MockInputStream("<geodata>\n\t<latt>" + latitude + "</latt>\n\t<longt>"
                + longitude + "</longt>\n</geodata>"));

        LocationValidator.process(new MockPersistenceManager(), postalCode, countryCode, consumerKey, rawCommandKey);
    }

    @Test
    public void testProcessWithOneNewLocation() throws DataSourceException {
        final String postalCode = "H2N3C6";
        final String countryCode = "CA";
        final Long consumerKey = 111L;
        final Long rawCommandKey = 222L;
        final Double longitude = -73.3D;
        final Double latitude = 45.5D;

        // LocationOperations mock
        LocationValidator.locationOperations = new LocationOperations() {
            @Override
            public List<Location> getLocations(PersistenceManager pm, String postalCode, String countryCode) {
                List<Location> locations = new ArrayList<Location>();
                return locations;
            }

            @Override
            public Location createLocation(PersistenceManager pm, Location location) {
                assertEquals(postalCode, location.getPostalCode());
                assertEquals(countryCode, location.getCountryCode());
                assertEquals(longitude, location.getLongitude(), 0.0);
                assertEquals(latitude, location.getLatitude(), 0.0);
                return location;
            }
        };
        // RawCommandOperations mock
        LocationValidator.rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                RawCommand command = new RawCommand();
                command.setSource(Source.simulated);
                return command;
            }
        };
        // ConsumerOperations mock
        LocationValidator.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                Consumer consumer = new Consumer();
                return consumer;
            }
        };
        // LocaleValidator mock
        LocaleValidator.setValidatorStream(new MockInputStream("<geodata>\n\t<latt>" + latitude + "</latt>\n\t<longt>"
                + longitude + "</longt>\n</geodata>"));

        LocationValidator.process(new MockPersistenceManager(), postalCode, countryCode, consumerKey, rawCommandKey);
    }

    @Test
    public void testProcessWithOneNewLocationButCannotResolve() throws DataSourceException {
        final String postalCode = "H2N3C6";
        final String countryCode = "CA";
        final Long consumerKey = 111L;
        final Long rawCommandKey = 222L;
        final String command = "!list store:* locale:" + postalCode + " " + countryCode;

        // LocationOperations mock
        LocationValidator.locationOperations = new LocationOperations() {
            @Override
            public List<Location> getLocations(PersistenceManager pm, String postalCode, String countryCode) {
                List<Location> locations = new ArrayList<Location>();
                return locations;
            }

            @Override
            public Location createLocation(PersistenceManager pm, Location location) {
                fail("Not expected because the resoltion failed");
                return location;
            }
        };
        // RawCommandOperations mock
        LocationValidator.rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                RawCommand rawCommand = new RawCommand();
                rawCommand.setSource(Source.simulated);
                rawCommand.setCommand(command);
                return rawCommand;
            }
        };
        // ConsumerOperations mock
        LocationValidator.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                Consumer consumer = new Consumer();
                return consumer;
            }
        };
        // LocaleValidator mock
        LocaleValidator.setValidatorStream(new MockInputStream(""));

        LocationValidator.process(new MockPersistenceManager(), postalCode, countryCode, consumerKey, rawCommandKey);

        String sentTweet = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNotNull(sentTweet);
        assertEquals(
                LabelExtractor.get(
                        "lv_report_invalid_locale",
                        new Object[] { command },
                        Locale.ENGLISH
                ),
                sentTweet
        );
    }

    @Test
    @SuppressWarnings("serial")
    public void testProcessWithOneNewLocationButCannotResolveAndCannotCommunicate() throws DataSourceException {
        final String postalCode = "H2N3C6";
        final String countryCode = "CA";
        final Long consumerKey = 111L;
        final Long rawCommandKey = 222L;
        final String command = "!list store:* locale:" + postalCode + " " + countryCode;

        // LocationOperations mock
        LocationValidator.locationOperations = new LocationOperations() {
            @Override
            public List<Location> getLocations(PersistenceManager pm, String postalCode, String countryCode) {
                List<Location> locations = new ArrayList<Location>();
                return locations;
            }

            @Override
            public Location createLocation(PersistenceManager pm, Location location) {
                fail("Not expected because the resoltion failed");
                return location;
            }
        };
        // RawCommandOperations mock
        LocationValidator.rawCommandOperations = new RawCommandOperations() {
            @Override
            public RawCommand getRawCommand(PersistenceManager pm, Long key) {
                RawCommand rawCommand = new RawCommand();
                rawCommand.setSource(Source.twitter);
                rawCommand.setCommand(command);
                return rawCommand;
            }
        };
        // ConsumerOperations mock
        LocationValidator.consumerOperations = new ConsumerOperations() {
            @Override
            public Consumer getConsumer(PersistenceManager pm, Long key) {
                Consumer consumer = new Consumer();
                consumer.setTwitterId("test");
                return consumer;
            }
        };
        // LocaleValidator mock
        LocaleValidator.setValidatorStream(new MockInputStream(""));
        // TwitterAccout mock
        final Twitter mockTwitterAccount = new Twitter() {
            @Override
            public DirectMessage sendDirectMessage(String id, String text) throws TwitterException {
                throw new TwitterException("done in purpose");
            }
        };
        MockTwitterConnector.injectMockTwitterAccount(mockTwitterAccount);

        LocationValidator.process(new MockPersistenceManager(), postalCode, countryCode, consumerKey, rawCommandKey);

        String sentTweet = BaseConnector.getLastCommunicationInSimulatedMode();
        assertNull(sentTweet);
    }
}
