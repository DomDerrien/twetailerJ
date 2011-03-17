package twetailer.task;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;
import static twetailer.connector.BaseConnector.communicateToConsumer;

import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Location;
import twetailer.dto.RawCommand;
import twetailer.task.step.BaseSteps;
import twetailer.validator.LocaleValidator;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.TaskOptions.Method;

import domderrien.i18n.LabelExtractor;

/**
 * Define the task that validate the given location coordinates with
 * a third party service. If the validation is successful, the command
 * that required it is scheduled for another processing with the
 * task "/_tasks/processCommand". If the location is invalid, a message
 * is sent to the Command initiator via the same communication channel
 * used to create the Command.
 *
 * @author Dom Derrien
 */
public class LocationValidator {

    private static Logger log = Logger.getLogger(LocationValidator.class.getName());

    /// Made available for test purposes
    public static void setMockLogger(Logger mockLogger) {
        log = mockLogger;
    }

    protected static Logger getLogger() {
        return log;
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
     * @throws InvalidIdentifierException If the retrieval of the identified resources fails
     */
    public static void process(String postalCode, String countryCode, Long consumerKey, Long commandKey) throws DataSourceException, InvalidIdentifierException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
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
     * @throws InvalidIdentifierException If the retrieval of the identified resources fails
     */
    public static void process(PersistenceManager pm, String postalCode, String countryCode, Long consumerKey, Long commandKey) throws DataSourceException, InvalidIdentifierException {
        List<Location> locations = BaseSteps.getLocationOperations().getLocations(pm, postalCode, countryCode);
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
            if (Location.INVALID_COORDINATE.equals(location.getLongitude()) && commandKey != null && commandKey != 0L) {
                getLogger().warning("Invalid location for the command: " + commandKey + " -- [" + postalCode + " " + countryCode + "]");
                RawCommand rawCommand = BaseSteps.getRawCommandOperations().getRawCommand(pm, commandKey);
                Consumer consumer = BaseSteps.getConsumerOperations().getConsumer(pm, consumerKey);
                Locale locale = consumer.getLocale();
                try {
                    communicateToConsumer(
                            rawCommand.getSource(),
                            rawCommand.getSubject(),
                            consumer,
                            new String[] {
                                    LabelExtractor.get(
                                        "lv_report_invalid_locale",
                                        new Object[] { rawCommand.getCommand() },
                                        locale
                                    )
                            }
                        );
                    }
                    catch (ClientException ex) {
                        getLogger().warning("Cannot communicate with consumer " + consumerKey + " -- ex: " + ex.getMessage());
                    }
                return;
            }
            if (locations.size() == 0) {
                location = BaseSteps.getLocationOperations().createLocation(pm, location);
            }
            else {
                location = BaseSteps.getLocationOperations().updateLocation(pm, location);
            }
        }

        // Create a task to re-process the raw command
        if (commandKey != null && commandKey != 0L) {
            Queue queue = BaseSteps.getBaseOperations().getQueue();
            getLogger().warning("Preparing the task: /_tasks/processCommand?key=" + commandKey.toString());
            queue.add(
                    withUrl("/_tasks/processCommand").
                        param(Command.KEY, commandKey.toString()).
                        method(Method.GET).
                        countdownMillis(2000)
            );
        }
    }
}
