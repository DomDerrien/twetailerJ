package twetailer.task.command;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;
import static twetailer.connector.BaseConnector.communicateToConsumer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.Proposal;
import twetailer.dto.RawCommand;
import twetailer.dto.SaleAssociate;
import twetailer.dto.Store;
import twetailer.task.CommandProcessor;
import twetailer.validator.ApplicationSettings;
import twetailer.validator.LocaleValidator;
import twetailer.validator.CommandSettings.Action;
import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.JsonObject;

public class ListCommandProcessor {
    public static void processListCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command, JsonObject prefixes, JsonObject actions) throws DataSourceException, ClientException {
        //
        // Used by actors to:
        //
        // 1.1 // Not yet defined
        // 1.2 Get the details about the identified demand
        // 2.1 // Not yet defined
        // 2.2 Get the details about the identified proposal
        // 3.1 Get information about all store around the given location
        // 3.2 Get the details about the identified store
        // 4. Get information about all active demands owned by the consumer
        //
        if (command.containsKey(Demand.REFERENCE)) {
            Long demandKey = command.getLong(Demand.REFERENCE);
            // if (Long.valueOf(-1L).equals(demandKey)) {
            //     // 1.1 // Not yet defined
            //     List all demands?
            //     return;
            // }
            // 1.2 Get the details about the identified demand
            Demand demand = null;
            try {
                demand = CommandProcessor.demandOperations.getDemand(pm, demandKey, consumer.getKey());
            }
            catch(Exception ex) {
                communicateToConsumer(
                        rawCommand,
                        consumer,
                        LabelExtractor.get("cp_command_list_invalid_demand_id", consumer.getLocale())
                );
            }
            if (demand != null) {
                // Echo back the specified demand
                Location location = demand.getLocationKey() == null ? null : CommandProcessor.locationOperations.getLocation(pm, demand.getLocationKey());
                communicateToConsumer(
                        rawCommand,
                        consumer,
                        CommandProcessor.generateTweet(demand, location, consumer.getLocale())
                );
            }
            return;
        }
        if (command.containsKey(Proposal.PROPOSAL_KEY)) {
            Long proposalKey = command.getLong(Proposal.PROPOSAL_KEY);
            // if (Long.valueOf(-1L).equals(proposalKey)) {
            //     // 2.1 // Not yet defined
            //     List all proposals?
            //     return;
            // }
            // 2.2 Get the details about the identified proposal
            Proposal proposal = null;
            SaleAssociate saleAssociate = CommandProcessor.retrieveSaleAssociate(pm, consumer, Action.list);
            try {
                proposal = CommandProcessor.proposalOperations.getProposal(pm, proposalKey, saleAssociate.getKey(), null);
            }
            catch(Exception ex) {
                communicateToConsumer(
                        rawCommand,
                        consumer,
                        LabelExtractor.get("cp_command_list_invalid_proposal_id", saleAssociate.getLocale())
                );
            }
            if (proposal != null) {
                // Echo back the specified proposal
                communicateToConsumer(
                        rawCommand,
                        consumer,
                        CommandProcessor.generateTweet(proposal, saleAssociate.getLocale())
                );
            }
            return;
        }
        if (command.containsKey(Store.STORE_KEY)) {
            Long storeKey = command.getLong(Store.STORE_KEY);
            if (Long.valueOf(-1L).equals(storeKey)) {
                // 3.1 List all stores in the specified area
                String postalCode = null;
                String countryCode = null;
                Double range = command.containsKey(Demand.RANGE) ? command.getDouble(Demand.RANGE) : LocaleValidator.DEFAULT_RANGE;
                String rangeUnit = command.containsKey(Demand.RANGE_UNIT) ? command.getString(Demand.RANGE_UNIT) : LocaleValidator.DEFAULT_RANGE_UNIT;
                Location consumerLocation = null;
                if (command.containsKey(Location.POSTAL_CODE)) {
                    postalCode = command.getString(Location.POSTAL_CODE);
                    countryCode = command.getString(Location.COUNTRY_CODE);
                    List<Location> locations = CommandProcessor.locationOperations.getLocations(pm, postalCode, countryCode);
                    if (locations.size() == 0) {
                        communicateToConsumer(
                                rawCommand,
                                consumer,
                                LabelExtractor.get(
                                        "cp_command_list_store_with_new_location",
                                        new Object[] { postalCode, countryCode },
                                        consumer.getLocale()
                                )
                        );
                        Queue queue = QueueFactory.getDefaultQueue();
                        queue.add(
                                url(ApplicationSettings.get().getServletApiPath() + "/maezel/validateLocation").
                                    param(Location.POSTAL_CODE, postalCode).
                                    param(Location.COUNTRY_CODE, countryCode).
                                    param(Consumer.CONSUMER_KEY, consumer.getKey().toString()).
                                    param(Command.KEY, rawCommand.getKey().toString()).
                                    method(Method.GET)
                        );
                        return;
                    }
                    consumerLocation = locations.get(0);
                }
                else {
                    communicateToConsumer(
                            rawCommand,
                            consumer,
                            LabelExtractor.get(
                                    "cp_command_list_store_missing_location",
                                    consumer.getLocale()
                            )
                    );
                    return;
                }
                List<Location> locations = CommandProcessor.locationOperations.getLocations(pm, consumerLocation, range, rangeUnit, 0);
                List<Store> stores = CommandProcessor.storeOperations.getStores(pm, locations, 0);
                if (stores.size() == 0) {
                    communicateToConsumer(
                            rawCommand,
                            consumer,
                            LabelExtractor.get(
                                    "cp_command_list_no_store_in_location",
                                    new Object[] { postalCode, countryCode, range, rangeUnit },
                                    consumer.getLocale()
                            )
                    );
                    return;
                }
                communicateToConsumer(
                        rawCommand,
                        consumer,
                        LabelExtractor.get(
                                "cp_command_list_store_series_introduction",
                                new Object[] { stores.size() },
                                consumer.getLocale()
                        )
                );
                for (Store store: stores) {
                    Location location = CommandProcessor.locationOperations.getLocation(pm, store.getLocationKey());
                    communicateToConsumer(
                            rawCommand,
                            consumer,
                            CommandProcessor.generateTweet(store, location, consumer.getLocale())
                    );
                }
                return;
            }
            // 3.2 Get the details about the identified store
            Store store = null;
            try {
                store = CommandProcessor.storeOperations.getStore(pm, storeKey);
            }
            catch(Exception ex) {
                communicateToConsumer(
                        rawCommand,
                        consumer,
                        LabelExtractor.get("cp_command_list_invalid_store_id", consumer.getLocale())
                );
            }
            if (store != null) {
                // Echo back the specified proposal
                Location location = store.getLocationKey() == null ? null : CommandProcessor.locationOperations.getLocation(pm, store.getLocationKey());
                communicateToConsumer(
                        rawCommand,
                        consumer,
                        CommandProcessor.generateTweet(store, location, consumer.getLocale())
                );
            }
            return;
        }
        // 4. Get information about all active demands owned by the consumer
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(Command.OWNER_KEY, consumer.getKey());
        parameters.put(Demand.STATE_COMMAND_LIST, Boolean.TRUE);
        List<Demand> demands = CommandProcessor.demandOperations.getDemands(pm, parameters, 0);
        if (demands.size() == 0) {
            communicateToConsumer(
                    rawCommand,
                    consumer,
                    LabelExtractor.get(
                            "cp_command_list_no_active_demand",
                            consumer.getLocale()
                    )
            );
        }
        else {
            communicateToConsumer(
                    rawCommand,
                    consumer,
                    LabelExtractor.get(
                            "cp_command_list_demand_series_introduction",
                            new Object[] { demands.size() },
                            consumer.getLocale()
                    )
            );
            for (Demand demand: demands) {
                Location location = demand.getLocationKey() == null ? null : CommandProcessor.locationOperations.getLocation(pm, demand.getLocationKey());
                communicateToConsumer(
                        rawCommand,
                        consumer,
                        CommandProcessor.generateTweet(demand, location, consumer.getLocale())
                );
            }
        }
    }
}
