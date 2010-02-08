package twetailer.task.command;

import static twetailer.connector.BaseConnector.communicateToConsumer;
import static twetailer.connector.BaseConnector.communicateToSaleAssociate;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
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
                // FIXME: keep the cancellation code (can be: owner, direct interlocutor, associate, deal closed by me, deal closed by someone else
                demand = CommandProcessor.demandOperations.updateDemand(pm, demand);
                Location location = demand.getLocationKey() == null ? null : CommandProcessor.locationOperations.getLocation(pm, demand.getLocationKey());
                message = CommandProcessor.generateTweet(demand, location, consumer.getLocale());
                if (State.published.equals(previousState)) {
                    demand.getState();
                    // FIXME: cancel also attached proposals
                    // FIXME: inform the sale associates who proposed articles about the cancellation
                }
                else if (State.confirmed.equals(previousState)) {
                    demand.getState();
                    // FIXME: inform the sale associate if the demand was in the confirmed state
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
            SaleAssociate saleAssociate = CommandProcessor.retrieveSaleAssociate(pm, consumer, Action.cancel);
            // FIXME: allow also attached demand owner to cancel the proposal
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
                // FIXME: keep the cancellation code (can be: owner, direct interlocutor, associate, deal closed by me, deal closed by someone else
                proposal = CommandProcessor.proposalOperations.updateProposal(pm, proposal);
                Store store = CommandProcessor.storeOperations.getStore(pm, saleAssociate.getStoreKey());
                message = CommandProcessor.generateTweet(proposal, store, saleAssociate.getLocale());
                if (!State.declined.equals(previousState)) {
                    proposal.getState();
                    // FIXME: inform the consumer who owns the attached demand about the cancellation
                    // FIXME: put the demand in the published state if the proposal was in the confirmed state
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
