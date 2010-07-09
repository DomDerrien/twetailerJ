package twetailer.task.command;

import static twetailer.connector.BaseConnector.communicateToConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.InvalidStateException;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.RawCommand;
import twetailer.task.CommandLineParser;
import twetailer.task.CommandProcessor;
import twetailer.task.step.DemandSteps;
import twetailer.task.step.LocationSteps;
import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.JsonObject;

public class DemandCommandProcessor {

    public static void processDemandCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command, JsonObject prefixes, JsonObject actions) throws DataSourceException, ClientException {

        Locale locale = consumer.getLocale();
        List<String> messages = new ArrayList<String>();

        if (!command.containsKey(Demand.REFERENCE)) {
            // Create new demand
            command.put(Command.SOURCE, rawCommand.getSource().toString());
            command.put(Command.RAW_COMMAND_ID, rawCommand.getKey());

            Demand demand = DemandSteps.createDemand(pm, command, consumer);

            // Prepare the informative message successfully created demand
            //
            // TODO: get the #hashtag to decide which label set to use!
            //
            Location location = LocationSteps.getLocation(pm, demand);
            String demandRef = LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { demand.getKey() }, locale);
            messages.add(LabelExtractor.get("cp_command_demand_acknowledge_creation", new Object[] { demandRef }, locale));
            messages.add(CommandProcessor.generateTweet(demand, location, false, locale));
        }
        else {
            Long demandKey = command.getLong(Demand.REFERENCE);
            try {
                // Update specified demand
                Demand demand = DemandSteps.updateDemand(pm, demandKey, command, consumer);

                // Prepare the informative message successfully created demand
                //
                // TODO: get the #hashtag to decide which label set to use!
                //
                Location location = LocationSteps.getLocation(pm, demand);
                messages.add(CommandProcessor.generateTweet(demand, location, false, locale));
            }
            catch(InvalidIdentifierException ex) {
                messages.add(LabelExtractor.get("cp_command_demand_invalid_demand_id", locale));
            }
            catch(InvalidStateException ex) {
                String demandRef = LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { demandKey }, locale);
                String stateLabel = CommandLineParser.localizedStates.get(locale).getString(ex.getEntityState());
                stateLabel = LabelExtractor.get("cp_tweet_state_part", new Object[] { stateLabel }, locale);
                messages.add(LabelExtractor.get("cp_command_demand_non_modifiable_state", new Object[] { demandRef, stateLabel }, locale));
            }
        }

        // Inform the demand owner
        communicateToConsumer(
                rawCommand,
                consumer,
                messages.toArray(new String[messages.size()])
        );
    }
}
