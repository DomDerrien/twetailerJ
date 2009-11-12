package twetailer.task.command;

import static twetailer.connector.BaseConnector.communicateToConsumer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.Proposal;
import twetailer.dto.RawCommand;
import twetailer.dto.SaleAssociate;
import twetailer.task.CommandProcessor;
import twetailer.validator.CommandSettings.Action;
import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.JsonObject;

public class ListCommandProcessor {
    public static void processListCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command, JsonObject prefixes, JsonObject actions) throws DataSourceException, ClientException {
        //
        // Used by actors to:
        //
        // 1. Get the details about the identified demand
        // 2. Get the details about the identified proposal
        // 3. Get the details about the identified product
        // 4. Get the details about the identified store
        //
        if (command.containsKey(Demand.REFERENCE)) {
            Demand demand = null;
            try {
                demand = CommandProcessor.demandOperations.getDemand(pm, command.getLong(Demand.REFERENCE), consumer.getKey());
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
        }
        else if (command.containsKey(Proposal.PROPOSAL_KEY)) {
            Proposal proposal = null;
            SaleAssociate saleAssociate = CommandProcessor.retrieveSaleAssociate(pm, consumer, Action.list);
            try {
                proposal = CommandProcessor.proposalOperations.getProposal(pm, command.getLong(Proposal.PROPOSAL_KEY), saleAssociate.getKey(), null);
            }
            catch(Exception ex) {
                communicateToConsumer(
                        rawCommand,
                        consumer,
                        LabelExtractor.get("cp_command_list_invalid_proposal_id", consumer.getLocale())
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
        }
        /* TODO: implement other listing variations
        else if (command.getString(Product.PRODUCT_KEY) != null) {
            throw new ClientException("Listing Stores - Not yet implemented");
        }
        else if (command.getString(Store.STORE_KEY) != null) {
            throw new ClientException("Listing Stores - Not yet implemented");
        }
        */
        else {
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
                                "cp_command_list_series_introduction",
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
}
