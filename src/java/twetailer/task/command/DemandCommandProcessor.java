package twetailer.task.command;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;
import static twetailer.connector.BaseConnector.communicateToConsumer;

import java.util.List;

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
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.State;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.JsonObject;

public class DemandCommandProcessor {
    public static void processDemandCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command, JsonObject prefixes, JsonObject actions) throws DataSourceException, ClientException {
        //
        // Used by a consumer to:
        //
        // 1. create a new demand
        // 2. update the identified demand
        //
        Long demandKey = 0L;
        Location newLocation = Location.hasAttributeForANewLocation(command) ? CommandProcessor.locationOperations.createLocation(pm, command) : null;
        if (command.containsKey(Demand.REFERENCE)) {
            // Extracts the new location
            if (newLocation != null) {
                command.put(Demand.LOCATION_KEY, newLocation.getKey());
            }
            // Update the demand attributes
            Demand demand = null;
            try {
                demand = CommandProcessor.demandOperations.getDemand(pm, command.getLong(Demand.REFERENCE), consumer.getKey());
            }
            catch(Exception ex) {
                communicateToConsumer(
                        rawCommand,
                        consumer,
                        LabelExtractor.get("cp_command_demand_invalid_demand_id", consumer.getLocale())
                );
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
                    communicateToConsumer(
                            rawCommand,
                            consumer,
                            CommandProcessor.generateTweet(demand, location, consumer.getLocale())
                    );
                    // Get the demandKey for the task scheduling
                    demandKey = demand.getKey();
                }
                else {
                    communicateToConsumer(
                            rawCommand,
                            consumer,
                            LabelExtractor.get("cp_command_demand_non_modifiable_state", new Object[] { demand.getKey(), state }, consumer.getLocale())
                    );
                }
            }
        }
        else {
            // Extracts the new location
            Long newLocationKey = consumer.getLocationKey();
            if (newLocation != null) {
                newLocationKey = newLocation.getKey();
            }
            // Get the latest demand or the default one
            List<Demand> demands = CommandProcessor.demandOperations.getDemands(pm, Command.OWNER_KEY, consumer.getKey(), 1);
            Demand latestDemand = null;
            if (0 < demands.size()) {
                latestDemand = demands.get(0);
                // Transfer the demand into a new object
                latestDemand = new Demand(latestDemand.toJson()); // To avoid attempts to persist the object
                // Reset sensitive fields
                latestDemand.resetKey();
                latestDemand.resetCoreDates();
                latestDemand.setAction(Action.demand);
                latestDemand.setHashTag(null);
                latestDemand.resetCriteria();
                latestDemand.setDefaultExpirationDate();
                latestDemand.setState(State.opened);
            }
            else {
                latestDemand = new Demand();
                // Set fields with default values
                latestDemand.setAction(Action.demand);
            }
            latestDemand.setSource(rawCommand.getSource());
            // Update of the latest command (can be the default one) with the just extracted parameters
            command = latestDemand.fromJson(command).toJson();
            if (newLocationKey != null && !newLocationKey.equals(command.getLong(Demand.LOCATION_KEY))) {
                command.put(Demand.LOCATION_KEY, newLocationKey);
            }
            // Persist the new demand
            Demand newDemand = CommandProcessor.demandOperations.createDemand(pm, command, consumer.getKey());
            communicateToConsumer(
                    rawCommand,
                    consumer,
                    LabelExtractor.get(
                            "cp_command_demand_acknowledge_creation",
                            new Object[] { newDemand.getKey() },
                            consumer.getLocale()
                    )
            );
            Location location = newDemand.getLocationKey() == null ? null : CommandProcessor.locationOperations.getLocation(pm, newDemand.getLocationKey());
            communicateToConsumer(
                    rawCommand,
                    consumer,
                    CommandProcessor.generateTweet(newDemand, location, consumer.getLocale())
            );
            // Get the demandKey for the task scheduling
            demandKey = newDemand.getKey();
        }

        // Temporary warning
        String hashTag = command.getString(Command.HASH_TAG);
        if (hashTag != null){
            communicateToConsumer(
                    rawCommand,
                    consumer,
                    LabelExtractor.get("cp_command_demand_hashtag_warning", new Object[] { demandKey, hashTag }, consumer.getLocale())
            );
        }

        // Create a task for that demand
        if (demandKey != 0L) {
            Queue queue = QueueFactory.getDefaultQueue();
            queue.add(
                    url(ApplicationSettings.get().getServletApiPath() + "/maezel/validateOpenDemand").
                        param(Demand.KEY, demandKey.toString()).
                        method(Method.GET)
            );
        }
    }
}
