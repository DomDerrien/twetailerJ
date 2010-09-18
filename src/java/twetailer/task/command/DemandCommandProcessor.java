package twetailer.task.command;

import static twetailer.connector.BaseConnector.communicateToConsumer;

import java.util.Locale;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.InvalidStateException;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.RawCommand;
import twetailer.task.CommandLineParser;
import twetailer.task.step.DemandSteps;
import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.JsonObject;

public class DemandCommandProcessor {

    public static void processDemandCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command, JsonObject prefixes, JsonObject actions) throws DataSourceException, ClientException {

        Locale locale = consumer.getLocale();
        String message = null;

        if (!command.containsKey(Demand.REFERENCE)) {
            // Create new demand
            command.put(Command.SOURCE, rawCommand.getSource().toString());
            command.put(Command.RAW_COMMAND_ID, rawCommand.getKey());

            DemandSteps.createDemand(pm, command, consumer);

            // Creation confirmation message will be sent by the DemandValidator being given it's a valid demand
        }
        else {
            Long demandKey = command.getLong(Demand.REFERENCE);
            try {
                // Update specified demand
                DemandSteps.updateDemand(pm, rawCommand, demandKey, command, consumer);

                // Update confirmation message will be sent by the DemandValidator being given it's a valid demand
            }
            catch(InvalidIdentifierException ex) {
                message = LabelExtractor.get("cp_command_demand_invalid_demand_id", locale);
            }
            catch(InvalidStateException ex) {
                String demandRef = LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { demandKey }, locale);
                String stateLabel = CommandLineParser.localizedStates.get(locale).getString(ex.getEntityState());
                stateLabel = LabelExtractor.get("cp_tweet_state_part", new Object[] { stateLabel }, locale);
                message = LabelExtractor.get("cp_command_demand_non_modifiable_state", new Object[] { demandRef, stateLabel }, locale);
            }
        }

        // Inform the demand owner
        if (message != null) {
            communicateToConsumer(
                    rawCommand.getSource(),
                    rawCommand.getSubject(),
                    consumer,
                    new String[] { message }
            );
        }
    }
}
