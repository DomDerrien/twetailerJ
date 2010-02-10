package twetailer.task.command;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;
import static twetailer.connector.BaseConnector.communicateToConsumer;
import static twetailer.connector.BaseConnector.communicateToSaleAssociate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.ReservedOperationException;
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

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.JsonObject;

public class ListCommandProcessor {
    private static Logger log = Logger.getLogger(ListCommandProcessor.class.getName());

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
            if (Long.valueOf(-1L).equals(demandKey)) {
                // 1.1 List all demands in the specified area
                Double range = command.containsKey(Demand.RANGE) ? command.getDouble(Demand.RANGE) : LocaleValidator.DEFAULT_RANGE;
                String rangeUnit = command.containsKey(Demand.RANGE_UNIT) ? command.getString(Demand.RANGE_UNIT) : LocaleValidator.DEFAULT_RANGE_UNIT;
                Location consumerLocation = getLocation(pm, consumer, rawCommand, command, "cp_command_list_demand_missing_location");
                if (consumerLocation == null) {
                    return;
                }
                List<Location> locations = CommandProcessor.locationOperations.getLocations(pm, consumerLocation, range, rangeUnit, 0);
                List<Demand> demands = CommandProcessor.demandOperations.getDemands(pm, locations, 0);
                List<String> messages = new ArrayList<String>();
                if (demands.size() == 0) {
                    messages.add(
                            LabelExtractor.get(
                                    "cp_command_list_no_demand_in_location",
                                    new Object[] { consumerLocation.getPostalCode(), consumerLocation.getCountryCode(), range, rangeUnit },
                                    consumer.getLocale()
                            )
                    );
                }
                else {
                    messages.add(
                            LabelExtractor.get(
                                    "cp_command_list_demand_series_introduction",
                                    new Object[] { demands.size() },
                                    consumer.getLocale()
                            )
                    );
                    for (Demand demand: demands) {
                        Location location = CommandProcessor.locationOperations.getLocation(pm, demand.getLocationKey());
                        // Anonymized data listing!
                        messages.add(CommandProcessor.generateTweet(demand, location, true, consumer.getLocale()));
                    }
                }
                communicateToConsumer(
                        rawCommand,
                        consumer,
                        messages.toArray(new String[0])
                );
                return;
            }
            // 1.2 Get the details about the identified demand
            Demand demand = null;
            String message = null;
            try {
                demand = CommandProcessor.demandOperations.getDemand(pm, demandKey, consumer.getKey());
            }
            catch(Exception ex) {
                message = LabelExtractor.get("cp_command_list_invalid_demand_id", consumer.getLocale());
            }
            if (demand != null) {
                // Echo back the specified demand
                Location location = demand.getLocationKey() == null ? null : CommandProcessor.locationOperations.getLocation(pm, demand.getLocationKey());
                message = CommandProcessor.generateTweet(demand, location, false, consumer.getLocale());
            }
            communicateToConsumer(
                    rawCommand,
                    consumer,
                    new String[] { message }
            );
            return;
        }
        if (command.containsKey(Proposal.PROPOSAL_KEY)) {
            Long proposalKey = command.getLong(Proposal.PROPOSAL_KEY);
            if (Long.valueOf(-1L).equals(proposalKey)) {
                // 2.1 List all proposals in the specified area
                Double range = command.containsKey(Demand.RANGE) ? command.getDouble(Demand.RANGE) : LocaleValidator.DEFAULT_RANGE;
                String rangeUnit = command.containsKey(Demand.RANGE_UNIT) ? command.getString(Demand.RANGE_UNIT) : LocaleValidator.DEFAULT_RANGE_UNIT;
                Location consumerLocation = getLocation(pm, consumer, rawCommand, command, "cp_command_list_proposal_missing_location");
                if (consumerLocation == null) {
                    return;
                }
                List<Location> locations = CommandProcessor.locationOperations.getLocations(pm, consumerLocation, range, rangeUnit, 0);
                List<Proposal> proposals = CommandProcessor.proposalOperations.getProposals(pm, locations, 0);
                List<String> messages = new ArrayList<String>();
                if (proposals.size() == 0) {
                    messages.add(
                            LabelExtractor.get(
                                    "cp_command_list_no_proposal_in_location",
                                    new Object[] { consumerLocation.getPostalCode(), consumerLocation.getCountryCode(), range, rangeUnit },
                                    consumer.getLocale()
                            )
                    );
                }
                else {
                    messages.add(
                            LabelExtractor.get(
                                    "cp_command_list_proposal_series_introduction",
                                    new Object[] { proposals.size() },
                                    consumer.getLocale()
                            )
                    );
                    for (Proposal proposal: proposals) {
                        Store store = CommandProcessor.storeOperations.getStore(pm, proposal.getStoreKey());
                        // Anonymized data listing!
                        messages.add(CommandProcessor.generateTweet(proposal, store, true, consumer.getLocale()));
                    }
                }
                communicateToConsumer(
                        rawCommand,
                        consumer,
                        messages.toArray(new String[0])
                );
                return;
            }
            // 2.2 Get the details about the identified proposal
            Proposal proposal = null;
            SaleAssociate saleAssociate = null;
            String message = null;
            try {
                // Try to get the proposal
                proposal = CommandProcessor.proposalOperations.getProposal(pm, proposalKey, null, null);
            }
            catch(Exception ex) {
                message = LabelExtractor.get("cp_command_list_invalid_proposal_id", consumer.getLocale());
            }
            boolean checkIfConsumerOwnsDemand = false;
            try {
                // Try to get the proposal owner
                saleAssociate = CommandProcessor.retrieveSaleAssociate(pm, consumer, Action.list);
                if (proposal != null && !saleAssociate.getKey().equals(proposal.getOwnerKey())) {
                    checkIfConsumerOwnsDemand = true;
                }
            }
            catch(ReservedOperationException ex) {
                checkIfConsumerOwnsDemand = true;
            }
            if (checkIfConsumerOwnsDemand) {
                if (proposal != null) {
                    // Try to get the associated demand -- will fail if the querying consumer does own the demand
                    try {
                        CommandProcessor.demandOperations.getDemand(pm, proposal.getDemandKey(), consumer.getKey());
                    }
                    catch(Exception nestedEx) {
                        message = LabelExtractor.get("cp_command_list_invalid_proposal_id", consumer.getLocale());
                        proposal = null;
                    }
                }
            }
            if (proposal != null) {
                // Echo back the specified proposal
                Store store = CommandProcessor.storeOperations.getStore(pm, proposal.getStoreKey());
                message = CommandProcessor.generateTweet(proposal, store, false, saleAssociate == null ? consumer.getLocale() : saleAssociate.getLocale());
            }
            if (saleAssociate != null) {
                communicateToSaleAssociate(
                        rawCommand,
                        saleAssociate,
                        new String[] { message }
                );
            }
            else {
                communicateToConsumer(
                        rawCommand,
                        consumer,
                        new String[] { message }
                );
            }
            return;
        }
        if (command.containsKey(Store.STORE_KEY)) {
            Long storeKey = command.getLong(Store.STORE_KEY);
            if (Long.valueOf(-1L).equals(storeKey)) {
                // 3.1 List all stores in the specified area
                Double range = command.containsKey(Demand.RANGE) ? command.getDouble(Demand.RANGE) : LocaleValidator.DEFAULT_RANGE;
                String rangeUnit = command.containsKey(Demand.RANGE_UNIT) ? command.getString(Demand.RANGE_UNIT) : LocaleValidator.DEFAULT_RANGE_UNIT;
                Location consumerLocation = getLocation(pm, consumer, rawCommand, command, "cp_command_list_store_missing_location");
                if (consumerLocation == null) {
                    return;
                }
                List<Location> locations = CommandProcessor.locationOperations.getLocations(pm, consumerLocation, range, rangeUnit, 0);
                List<Store> stores = CommandProcessor.storeOperations.getStores(pm, locations, 0);
                List<String> messages = new ArrayList<String>();
                if (stores.size() == 0) {
                    messages.add(
                            LabelExtractor.get(
                                    "cp_command_list_no_store_in_location",
                                    new Object[] { consumerLocation.getPostalCode(), consumerLocation.getCountryCode(), range, rangeUnit },
                                    consumer.getLocale()
                            )
                    );
                }
                else {
                    messages.add(
                            LabelExtractor.get(
                                    "cp_command_list_store_series_introduction",
                                    new Object[] { stores.size() },
                                    consumer.getLocale()
                            )
                    );
                    for (Store store: stores) {
                        Location location = CommandProcessor.locationOperations.getLocation(pm, store.getLocationKey());
                        messages.add(CommandProcessor.generateTweet(store, location, consumer.getLocale()));
                    }
                }
                communicateToConsumer(
                        rawCommand,
                        consumer,
                        messages.toArray(new String[0])
                );
                return;
            }
            // 3.2 Get the details about the identified store
            Store store = null;
            String message = null;
            try {
                store = CommandProcessor.storeOperations.getStore(pm, storeKey);
            }
            catch(Exception ex) {
                message = LabelExtractor.get("cp_command_list_invalid_store_id", consumer.getLocale());
            }
            if (store != null) {
                // Echo back the specified proposal
                Location location = store.getLocationKey() == null ? null : CommandProcessor.locationOperations.getLocation(pm, store.getLocationKey());
                message = CommandProcessor.generateTweet(store, location, consumer.getLocale());
            }
            communicateToConsumer(
                    rawCommand,
                    consumer,
                    new String[] { message }
            );
            return;
        }
        // 4. Get information about all active demands owned by the consumer
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(Command.OWNER_KEY, consumer.getKey());
        parameters.put(Demand.STATE_COMMAND_LIST, Boolean.TRUE);
        List<Demand> demands = CommandProcessor.demandOperations.getDemands(pm, parameters, 0);
        List<String> messages = new ArrayList<String>();
        if (demands.size() == 0) {
            messages.add(
                    LabelExtractor.get(
                            "cp_command_list_no_active_demand",
                            consumer.getLocale()
                    )
            );
        }
        else {
            messages.add(
                    LabelExtractor.get(
                            "cp_command_list_demand_series_introduction",
                            new Object[] { demands.size() },
                            consumer.getLocale()
                    )
            );
            for (Demand demand: demands) {
                Location location = demand.getLocationKey() == null ? null : CommandProcessor.locationOperations.getLocation(pm, demand.getLocationKey());
                messages.add(CommandProcessor.generateTweet(demand, location, false, consumer.getLocale()));
            }
        }
        communicateToConsumer(
                rawCommand,
                consumer,
                messages.toArray(new String[0])
        );
    }

    protected static Location getLocation(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command, String messageKeyForMissingLocationInformation) throws DataSourceException, ClientException {
        // Report an error if the Locale information is missing
        if (!command.containsKey(Location.POSTAL_CODE)) {
            communicateToConsumer(
                    rawCommand,
                    consumer,
                    new String[] { LabelExtractor.get(
                            messageKeyForMissingLocationInformation,
                            consumer.getLocale()
                    )}
            );
            return null;
        }
        // Try to load the Locale information
        String postalCode = command.getString(Location.POSTAL_CODE);
        String countryCode = command.getString(Location.COUNTRY_CODE);
        List<Location> locations = CommandProcessor.locationOperations.getLocations(pm, postalCode, countryCode);
        if (locations.size() == 0) {
            communicateToConsumer(
                    rawCommand,
                    consumer,
                    new String[] { LabelExtractor.get(
                            "cp_command_list_with_new_location",
                            new Object[] { postalCode, countryCode },
                            consumer.getLocale()
                    )}
            );
            Queue queue = CommandProcessor._baseOperations.getQueue();
            log.warning("Preparing the task: /maezel/validateLocation?key=" + rawCommand.getKey().toString() +
                    "&postalCode=" + postalCode + "&countryCode=" + countryCode + "&consumerKey=" + consumer.getKey().toString());
            queue.add(
                    url(ApplicationSettings.get().getServletApiPath() + "/maezel/validateLocation").
                        param(Location.POSTAL_CODE, postalCode).
                        param(Location.COUNTRY_CODE, countryCode).
                        param(Consumer.CONSUMER_KEY, consumer.getKey().toString()).
                        param(Command.KEY, rawCommand.getKey().toString()).
                        method(Method.GET)
            );
            return null;
        }
        return locations.get(0);
    }
}
