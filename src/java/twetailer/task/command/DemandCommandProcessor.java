package twetailer.task.command;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;
import static twetailer.connector.BaseConnector.communicateToConsumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.RawCommand;
import twetailer.task.CommandProcessor;
import twetailer.validator.ApplicationSettings;
import twetailer.validator.CommandSettings.State;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.JsonObject;

public class DemandCommandProcessor {
    private static Logger log = Logger.getLogger(DemandCommandProcessor.class.getName());

    public static void processDemandCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command, JsonObject prefixes, JsonObject actions) throws DataSourceException, ClientException {
        //
        // Used by a consumer to:
        //
        // 1. create a new demand
        // 2. update the identified demand
        //
        List<String> messages = new ArrayList<String>();

        // Extracts and process the given location information
        if (Location.hasAttributeForANewLocation(command)) {
            Location newLocation = CommandProcessor.locationOperations.createLocation(pm, command);
            command.put(Demand.LOCATION_KEY, newLocation.getKey());
        }

        Long demandKey = 0L;
        if (command.containsKey(Demand.REFERENCE)) {
            // Update the demand attributes
            Demand demand = null;
            try {
                demand = CommandProcessor.demandOperations.getDemand(pm, command.getLong(Demand.REFERENCE), consumer.getKey());
            }
            catch(Exception ex) {
                messages.add(LabelExtractor.get("cp_command_demand_invalid_demand_id", consumer.getLocale()));
            }
            if (demand != null) {
                State state = demand.getState();
                if (state.equals(State.opened) || state.equals(State.published) || state.equals(State.invalid)) {
                    demand.fromJson(command);
                    demand.setState(State.opened); // Will force the re-validation of the entire demand
                    demand.resetProposalKeys(); // All existing proposals are removed
                    demand.resetSaleAssociateKeys(); // All existing sale associates need to be recontacted again
                    demand = CommandProcessor.demandOperations.updateDemand(pm, demand);
                    // Echo back the updated demand
                    Location location = demand.getLocationKey() == null ? null : CommandProcessor.locationOperations.getLocation(pm, demand.getLocationKey());
                    messages.add(CommandProcessor.generateTweet(demand, location, consumer.getLocale()));
                    // Get the demandKey for the task scheduling
                    demandKey = demand.getKey();
                }
                else {
                    messages.add(LabelExtractor.get("cp_command_demand_non_modifiable_state", new Object[] { demand.getKey(), state }, consumer.getLocale()));
                }
            }
        }
        else {
            // Inherit from the latest demand if any
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put(Command.OWNER_KEY, consumer.getKey());
            parameters.put(Demand.STATE_COMMAND_LIST, Boolean.TRUE);
            List<Demand> demands = CommandProcessor.demandOperations.getDemands(pm, parameters, 0);
            if (0 < demands.size()) {
                Demand previousDemand = demands.get(0);
                if (!command.containsKey(Demand.LOCATION_KEY)) { command.put(Demand.LOCATION_KEY, previousDemand.getLocationKey()); }
                if (!command.containsKey(Demand.RANGE))        { command.put(Demand.RANGE, previousDemand.getRange()); }
                if (!command.containsKey(Demand.RANGE_UNIT))   { command.put(Demand.RANGE_UNIT, previousDemand.getRangeUnit()); }
            }
            if (!command.containsKey(Demand.LOCATION_KEY) && consumer.getLocationKey() != null) {
                command.put(Demand.LOCATION_KEY, consumer.getLocationKey());
            }
            // Transmit rawCommand information
            command.put(Command.SOURCE, rawCommand.getSource().toString());
            command.put(Command.RAW_COMMAND_ID, rawCommand.getKey());
            // Persist the new demand
            Demand newDemand = CommandProcessor.demandOperations.createDemand(pm, command, consumer.getKey());
            messages.add(
                    LabelExtractor.get(
                            "cp_command_demand_acknowledge_creation",
                            new Object[] { newDemand.getKey() },
                            consumer.getLocale()
                    )
            );
            Location location = newDemand.getLocationKey() == null ? null : CommandProcessor.locationOperations.getLocation(pm, newDemand.getLocationKey());
            messages.add(CommandProcessor.generateTweet(newDemand, location, consumer.getLocale()));
            // Get the demandKey for the task scheduling
            demandKey = newDemand.getKey();
        }

        communicateToConsumer(
                rawCommand,
                consumer,
                messages.toArray(new String[messages.size()])
        );
        // Create a task for that demand
        if (demandKey != 0L) {
            Queue queue = CommandProcessor._baseOperations.getQueue();
            log.warning("Preparing the task: /maezel/validateOpenDemand?key=" + demandKey.toString());
            queue.add(
                    url(ApplicationSettings.get().getServletApiPath() + "/maezel/validateOpenDemand").
                        param(Demand.KEY, demandKey.toString()).
                        method(Method.GET)
            );
        }
    }
}
