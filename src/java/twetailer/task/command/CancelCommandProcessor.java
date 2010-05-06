package twetailer.task.command;

import static twetailer.connector.BaseConnector.communicateToConsumer;
import static twetailer.connector.BaseConnector.communicateToSaleAssociate;

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
import twetailer.dto.Store;
import twetailer.task.CommandLineParser;
import twetailer.task.CommandProcessor;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.State;
import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.JsonObject;

public class CancelCommandProcessor {
    public static void processCancelCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command) throws ClientException, DataSourceException {
        //
        // Used by resource owner to stop the process of his resource
        //
        // 1. Cancel the identified demand
        // 2. Cancel the identified proposal
        // 3. Cancel the identified wish
        //
        if (command.containsKey(Demand.REFERENCE)) {
            // Update demand state
            Demand demand = null;
            String message = null;
            try {
                demand = CommandProcessor.demandOperations.getDemand(pm, command.getLong(Demand.REFERENCE), consumer.getKey());
                State state = demand.getState();
                if (State.closed.equals(state) || State.cancelled.equals(state) || State.markedForDeletion.equals(state)) {
                    String stateLabel = CommandLineParser.localizedStates.get(consumer.getLocale()).getString(state.toString());
                    message = LabelExtractor.get("cp_command_cancel_invalid_demand_state", new Object[] { demand.getKey(), stateLabel },  consumer.getLocale());
                    demand = null; // To stop the process
                }
            }
            catch(Exception ex) {
                message = LabelExtractor.get("cp_command_cancel_invalid_demand_id", consumer.getLocale());
            }
            if (demand != null) {
                // Update the demand and echo back the new state
                State previousState = demand.getState();
                demand.setState(State.cancelled);
                demand.setCancelerKey(demand.getOwnerKey());
                demand = CommandProcessor.demandOperations.updateDemand(pm, demand);
                Location location = demand.getLocationKey() == null ? null : CommandProcessor.locationOperations.getLocation(pm, demand.getLocationKey());
                message = CommandProcessor.generateTweet(demand, location, false, consumer.getLocale());
                if (State.published.equals(previousState)) {
                    demand.getState();
                    // FIXME: cancel also attached proposals
                    // FIXME: inform the sale associates who proposed articles about the cancellation
                }
                else if (State.confirmed.equals(previousState)) {
                    Map<String, Object> parameters = new HashMap<String, Object>();
                    parameters.put(Proposal.DEMAND_KEY, demand.getKey());
                    parameters.put(Command.STATE, State.confirmed.toString());
                    try {
                        List<Proposal> proposals = CommandProcessor.proposalOperations.getProposals(pm, parameters, 1);
                        if (0 < proposals.size()) {
                            // Update the proposal
                            Proposal proposal = proposals.get(0);
                            proposal.setState(State.cancelled);
                            proposal.setCancelerKey(consumer.getKey());
                            proposal = CommandProcessor.proposalOperations.updateProposal(pm, proposal);
                            // Inform the proposal owner
                            SaleAssociate saleAssociate = CommandProcessor.saleAssociateOperations.getSaleAssociate(pm, proposal.getOwnerKey());
                            RawCommand originalRawCommand = CommandProcessor.rawCommandOperations.getRawCommand(pm, proposal.getRawCommandId());
                            communicateToSaleAssociate(
                                    originalRawCommand,
                                    saleAssociate,
                                    new String[] { LabelExtractor.get("cp_command_cancel_demand_canceled_proposal_to_be_canceled", new Object[] { demand.getKey(), proposal.getKey(), proposal.getSerializedCriteria() }, consumer.getLocale()) }
                            );
                        }
                    }
                    catch(Exception ex) {
                        // Too bad, the proposal owner can be informed about the demand closing...
                        // He/she can still see the proposal has been canceled
                    }
                }
            }
            communicateToConsumer(
                    rawCommand,
                    consumer,
                    new String[] { message }
            );
        }
        else if (command.containsKey(Proposal.PROPOSAL_KEY)) {
            // Get the sale associate
            SaleAssociate saleAssociate = CommandProcessor.retrieveSaleAssociate(pm, consumer, Action.cancel, Proposal.class.getName());
            // Update proposal state
            Proposal proposal = null;
            String message = null;
            try {
                proposal = CommandProcessor.proposalOperations.getProposal(pm, command.getLong(Proposal.PROPOSAL_KEY), saleAssociate.getKey(), null);
                State state = proposal.getState();
                if (State.closed.equals(state) || State.cancelled.equals(state) || State.markedForDeletion.equals(state)) {
                    String stateLabel = CommandLineParser.localizedStates.get(consumer.getLocale()).getString(state.toString());
                    message = LabelExtractor.get("cp_command_cancel_invalid_proposal_state", new Object[] { proposal.getKey(), stateLabel },  consumer.getLocale());
                    proposal = null; // To stop the process
                }
            }
            catch(Exception ex) {
                message = LabelExtractor.get("cp_command_cancel_invalid_proposal_id", consumer.getLocale());
            }
            if (proposal != null) {
                // Update the proposal and echo back the new state
                State previousState = proposal.getState();
                proposal.setState(State.cancelled);
                proposal.setCancelerKey(proposal.getOwnerKey());
                proposal = CommandProcessor.proposalOperations.updateProposal(pm, proposal);
                Store store = CommandProcessor.storeOperations.getStore(pm, saleAssociate.getStoreKey());
                message = CommandProcessor.generateTweet(proposal, store, false, saleAssociate.getLocale());
                if (State.confirmed.equals(previousState)) {
                    // Restore the demand in the published state
                    Demand demand = CommandProcessor.demandOperations.getDemand(pm, proposal.getDemandKey(), null);
                    demand.setState(State.published);
                    demand.setCancelerKey(saleAssociate.getKey());
                    demand = CommandProcessor.demandOperations.updateDemand(pm, demand);
                    // Inform the consumer of the state change
                    Consumer demandOwner = CommandProcessor.consumerOperations.getConsumer(pm, demand.getOwnerKey());
                    RawCommand originalRawCommand = CommandProcessor.rawCommandOperations.getRawCommand(pm, demand.getRawCommandId());
                    communicateToConsumer(
                            originalRawCommand,
                            demandOwner,
                            new String[] { LabelExtractor.get("cp_command_cancel_proposal_canceled_demand_to_be_published", new Object[] { proposal.getKey(), demand.getKey(), demand.getSerializedCriteria() }, consumer.getLocale()) }
                    );
                }
                else if (!State.declined.equals(previousState)) {
                    proposal.getState();
                    // FIXME: inform the consumer who owns the attached demand about the cancellation
                }
            }
            communicateToSaleAssociate(
                    rawCommand,
                    saleAssociate,
                    new String[] { message }
            );
        }
        /* TODO: implement other variations
        else if (command.containsKey(Wish.REFERENCE)) {
            throw new ClientException("Canceling proposals - Not yet implemented");
        }
        */
        else {
            communicateToConsumer(
                    rawCommand,
                    consumer,
                    new String[] { LabelExtractor.get("cp_command_cancel_missing_demand_id", consumer.getLocale()) }
            );
        }
    }
}
