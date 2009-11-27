package twetailer.task;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;
import static twetailer.connector.BaseConnector.communicateToConsumer;

import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.RawCommandOperations;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Location;
import twetailer.dto.RawCommand;
import twetailer.validator.ApplicationSettings;
import twetailer.validator.LocaleValidator;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

import domderrien.i18n.LabelExtractor;

public class LocationValidator {

    private static Logger log = Logger.getLogger(DemandValidator.class.getName());

    protected static BaseOperations _baseOperations = new BaseOperations();
    protected static ConsumerOperations consumerOperations = _baseOperations.getConsumerOperations();
    protected static LocationOperations locationOperations = _baseOperations.getLocationOperations();
    protected static RawCommandOperations rawCommandOperations = _baseOperations.getRawCommandOperations();

    // Setter for injection of a MockLogger at test time
    protected static void setLogger(Logger mock) {
        log = mock;
    }

    /**
     * Check the validity of the identified demand
     *
     * @param postalCode Postal code to consider
     * @param countryCode Country code to consider
     * @param consumerKey Identifier of the consumer who issued the command with the new location
     * @param commandKey Identifier of the raw command to re-process if the location is correct
     *
     * @throws DataSourceException If the data manipulation fails
     */
    public static void process(String postalCode, String countryCode, Long consumerKey, Long commandKey) throws DataSourceException {
        PersistenceManager pm = _baseOperations.getPersistenceManager();
        try {
            process(pm, postalCode, countryCode, consumerKey, commandKey);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Check the validity of the identified demand
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param postalCode Postal code to consider
     * @param countryCode Country code to consider
     * @param consumerKey Identifier of the consumer who issued the command with the new location
     * @param commandKey Identifier of the raw command to re-process if the location is correct
     *
     * @throws DataSourceException If the data manipulation fails
     */
    public static void process(PersistenceManager pm, String postalCode, String countryCode, Long consumerKey, Long commandKey) throws DataSourceException {
        List<Location> locations = locationOperations.getLocations(pm, postalCode, countryCode);
        Location location = null;
        if (locations.size() == 0) {
            location = new Location();
            location.setPostalCode(postalCode);
            location.setCountryCode(countryCode);
        }
        else {
            location = locations.get(0);
        }
        if (Location.INVALID_COORDINATE.equals(location.getLongitude())) {
            location = LocaleValidator.getGeoCoordinates(location);
            if (Location.INVALID_COORDINATE.equals(location.getLongitude())) {
                log.warning("Invalid location for the command: " + commandKey + " -- [" + postalCode + " " + countryCode + "]");
                RawCommand rawCommand = rawCommandOperations.getRawCommand(pm, commandKey);
                Consumer consumer = consumerOperations.getConsumer(pm, consumerKey);
                Locale locale = consumer.getLocale();
                try {
                    communicateToConsumer(
                            rawCommand,
                            consumer,
                            LabelExtractor.get(
                                    "lv_report_invalid_locale",
                                    new Object[] { rawCommand.getCommand() },
                                    locale
                            )
                        );
                    }
                    catch (ClientException ex) {
                        log.warning("Cannot communicate with consumer " + consumerKey + " -- ex: " + ex.getMessage());
                    }
                return;
            }
            if (locations.size() == 0) {
                location = locationOperations.createLocation(pm, location);
            }
            else {
                location = locationOperations.updateLocation(pm, location);
            }
        }
        // Create a task to re-process the raw command
        Queue queue = QueueFactory.getDefaultQueue();
        queue.add(
                url(ApplicationSettings.get().getServletApiPath() + "/maezel/processCommand").
                    param(Command.KEY, commandKey.toString()).
                    method(Method.GET)
        );
    }
}
