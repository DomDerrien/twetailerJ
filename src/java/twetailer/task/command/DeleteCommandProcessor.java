package twetailer.task.command;

import static twetailer.connector.BaseConnector.communicateToConsumer;
import static twetailer.connector.BaseConnector.communicateToSaleAssociate;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Proposal;
import twetailer.dto.RawCommand;
import twetailer.dto.SaleAssociate;
import twetailer.task.CommandLineParser;
import twetailer.task.CommandProcessor;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.State;
import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.JsonObject;

public class DeleteCommandProcessor {
    public static void processDeleteCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command) throws ClientException, DataSourceException {
        //
        // Used by the resource owner to delete it
        //
        // 1. Delete the identified demand
        // 2. Delete the identified proposal
        if (command.containsKey(Demand.REFERENCE)) {
            Demand demand = null;
            try {
                demand = CommandProcessor.demandOperations.getDemand(pm, command.getLong(Demand.REFERENCE), consumer.getKey());
                State state = demand.getState();
                if (!State.cancelled.equals(state)) {
                    String stateLabel = CommandLineParser.localizedStates.get(consumer.getLocale()).getString(state.toString());
                    communicateToConsumer(
                            rawCommand,
                            consumer,
                            LabelExtractor.get("cp_command_delete_invalid_demand_state", new Object[] { demand.getKey(), stateLabel },  consumer.getLocale())
                    );
                    demand = null; // To stop the process
                }
                else {
                    demand.setState(State.markedForDeletion);
                    demand = CommandProcessor.demandOperations.updateDemand(pm, demand);
                    communicateToConsumer(
                            rawCommand,
                            consumer,
                            LabelExtractor.get("cp_command_delete_acknowledge_demand_markedForDeletion", new Object[] { demand.getKey() }, consumer.getLocale())
                    );
                }
            }
            catch(Exception ex) {
                communicateToConsumer(
                        rawCommand,
                        consumer,
                        LabelExtractor.get("cp_command_delete_invalid_demand_id", consumer.getLocale())
                );
            }
        }
        else if (command.containsKey(Proposal.PROPOSAL_KEY)) {
            Proposal proposal = null;
            SaleAssociate saleAssociate = CommandProcessor.retrieveSaleAssociate(pm, consumer, Action.delete);
            try {
                proposal = CommandProcessor.proposalOperations.getProposal(pm, command.getLong(Proposal.PROPOSAL_KEY), saleAssociate.getKey(), null);
                State state = proposal.getState();
                if (!State.cancelled.equals(state)) {
                    String stateLabel = CommandLineParser.localizedStates.get(consumer.getLocale()).getString(state.toString());
                    communicateToSaleAssociate(
                            rawCommand,
                            saleAssociate,
                            LabelExtractor.get("cp_command_delete_invalid_proposal_state", new Object[] { proposal.getKey(), stateLabel },  consumer.getLocale())
                    );
                    proposal = null; // To stop the process
                }
                else {
                    proposal.setState(State.markedForDeletion);
                    proposal = CommandProcessor.proposalOperations.updateProposal(pm, proposal);
                    communicateToSaleAssociate(
                            rawCommand,
                            saleAssociate,
                            LabelExtractor.get("cp_command_delete_acknowledge_proposal_closing", new Object[] { proposal.getKey() }, consumer.getLocale())
                    );
                }
            }
            catch(Exception ex) {
                communicateToSaleAssociate(
                        rawCommand,
                        saleAssociate,
                        LabelExtractor.get("cp_command_delete_invalid_proposal_id", consumer.getLocale())
                );
            }
        }
        else {
            communicateToConsumer(
                    rawCommand,
                    consumer,
                    LabelExtractor.get("cp_command_delete_invalid_parameters", consumer.getLocale())
            );
        }
    }
}
