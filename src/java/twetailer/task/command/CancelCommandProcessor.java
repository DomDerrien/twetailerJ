package twetailer.task.command;

import static twetailer.connector.BaseConnector.communicateToConsumer;

import java.util.Locale;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.InvalidStateException;
import twetailer.ReservedOperationException;
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
import twetailer.task.step.DemandSteps;
import twetailer.task.step.LocationSteps;
import twetailer.task.step.ProposalSteps;
import twetailer.task.step.StoreSteps;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.State;
import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class CancelCommandProcessor {

    private static JsonObject cancelParameters = new GenericJsonObject();

    private static JsonObject getFreshCancelParameters() {
        cancelParameters.removeAll();
        cancelParameters.put(Command.STATE, State.cancelled.toString());
        return cancelParameters;
    }

    public static void processCancelCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command) throws ClientException, DataSourceException {

        Locale locale = consumer.getLocale();

        // Cancel identified Demand
        if (command.containsKey(Demand.REFERENCE)) {
            String message = null;
            Long entityKey = command.getLong(Demand.REFERENCE);
            try {
                Demand demand = DemandSteps.updateDemand(pm, entityKey, getFreshCancelParameters(), consumer);
                // Echo back the specified demand
                Location location = LocationSteps.getLocation(pm, demand);
                message = CommandProcessor.generateTweet(demand, location, false, locale);
            }
            catch(InvalidIdentifierException ex) {
                message = LabelExtractor.get("cp_command_cancel_invalid_demand_id", locale);
            }
            catch(InvalidStateException ex) {
                String demandRef = LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { entityKey }, locale);
                String stateLabel = CommandLineParser.localizedStates.get(locale).getString(ex.getEntityState().toString());
                stateLabel = LabelExtractor.get("cp_tweet_state_part", new Object[] { stateLabel }, locale);
                message = LabelExtractor.get("cp_command_cancel_invalid_demand_state", new Object[] { demandRef, stateLabel },  locale);
            }
            communicateToConsumer(
                    rawCommand,
                    consumer,
                    new String[] { message }
            );
            return;
        }

        // Cancel identified Proposal
        if (command.containsKey(Proposal.PROPOSAL_KEY)) {
            String message = null;
            Long entityKey = command.getLong(Proposal.PROPOSAL_KEY);
            try {
                SaleAssociate saleAssociate = CommandProcessor.retrieveSaleAssociate(pm, consumer, Action.cancel, Demand.class.getName());
                Proposal proposal = ProposalSteps.updateProposal(pm, entityKey, getFreshCancelParameters(), saleAssociate, consumer);
                // Echo back the specified proposal
                Store store = StoreSteps.getStore(pm, proposal.getStoreKey());
                message = CommandProcessor.generateTweet(proposal, store, false, locale);
            }
            catch(InvalidIdentifierException ex) {
                message = LabelExtractor.get("cp_command_cancel_invalid_proposal_id", locale);
            }
            catch(InvalidStateException ex) {
                String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { entityKey }, locale);
                String stateLabel = CommandLineParser.localizedStates.get(locale).getString(ex.getEntityState().toString());
                stateLabel = LabelExtractor.get("cp_tweet_state_part", new Object[] { stateLabel }, locale);
                message = LabelExtractor.get("cp_command_cancel_invalid_proposal_state", new Object[] { proposalRef, stateLabel },  locale);
            }
            catch(ReservedOperationException ex) {
                message = LabelExtractor.get("cp_command_parser_reserved_action", new String[] { ex.getAction().toString() }, locale);
            }
            communicateToConsumer(
                    rawCommand,
                    consumer,
                    new String[] { message }
            );
            return;
        }

        communicateToConsumer(
                rawCommand,
                consumer,
                new String[] { LabelExtractor.get("cp_command_cancel_missing_demand_id", consumer.getLocale()) }
        );

        /******* ddd
        if (locale != null) { return; }

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
                demand = BaseSteps.getDemandOperations().getDemand(pm, command.getLong(Demand.REFERENCE), consumer.getKey());
                State state = demand.getState();
                if (State.closed.equals(state) || State.cancelled.equals(state) || State.markedForDeletion.equals(state)) {
                    String demandRef = LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { demand.getKey() }, locale);
                    String stateLabel = CommandLineParser.localizedStates.get(locale).getString(state.toString());
                    stateLabel = LabelExtractor.get("cp_tweet_state_part", new Object[] { stateLabel }, locale);
                    message = LabelExtractor.get("cp_command_cancel_invalid_demand_state", new Object[] { demandRef, stateLabel },  locale);
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
                demand = BaseSteps.getDemandOperations().updateDemand(pm, demand);
                Location location = LocationSteps.getLocation(pm, demand);
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
                        List<Proposal> proposals = BaseSteps.getProposalOperations().getProposals(pm, parameters, 1);
                        if (0 < proposals.size()) {
                            // Update the proposal
                            Proposal proposal = proposals.get(0);
                            proposal.setState(State.cancelled);
                            proposal.setCancelerKey(consumer.getKey());
                            proposal = BaseSteps.getProposalOperations().updateProposal(pm, proposal);
                            // Inform the proposal owner
                            SaleAssociate saleAssociate = BaseSteps.getSaleAssociateOperations().getSaleAssociate(pm, proposal.getOwnerKey());
                            Consumer saConsumerRecord = BaseSteps.getConsumerOperations().getConsumer(pm, saleAssociate.getConsumerKey());
                            RawCommand originalRawCommand = BaseSteps.getRawCommandOperations().getRawCommand(pm, proposal.getRawCommandId());
                            String demandRef = LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { demand.getKey() }, locale);
                            String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposal.getKey() }, locale);
                            String tags = proposal.getCriteria().size() == 0 ? "" : LabelExtractor.get("cp_tweet_tags_part", new Object[] { proposal.getSerializedCriteria() }, locale);
                            communicateToConsumer(
                                    originalRawCommand,
                                    saConsumerRecord,
                                    new String[] { LabelExtractor.get("cp_command_cancel_demand_canceled_proposal_to_be_canceled", new Object[] { demandRef, proposalRef, tags }, locale) }
                            );
                        }
                    }
                    catch(Exception ex) {
                        // Too bad, the proposal owner can be informed about the demand closing...
                        // He/she can still see the proposal has been cancelled
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
            Consumer saConsumerRecord = BaseSteps.getConsumerOperations().getConsumer(pm, saleAssociate.getConsumerKey());
            // Update proposal state
            Proposal proposal = null;
            String message = null;
            try {
                proposal = BaseSteps.getProposalOperations().getProposal(pm, command.getLong(Proposal.PROPOSAL_KEY), saleAssociate.getKey(), null);
                State state = proposal.getState();
                if (State.closed.equals(state) || State.cancelled.equals(state) || State.markedForDeletion.equals(state)) {
                    String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposal.getKey() }, locale);
                    String stateLabel = CommandLineParser.localizedStates.get(locale).getString(state.toString());
                    stateLabel = LabelExtractor.get("cp_tweet_state_part", new Object[] { stateLabel }, locale);
                    message = LabelExtractor.get("cp_command_cancel_invalid_proposal_state", new Object[] { proposalRef, stateLabel },  locale);
                    proposal = null; // To stop the process
                }
            }
            catch(Exception ex) {
                message = LabelExtractor.get("cp_command_cancel_invalid_proposal_id", saleAssociate.getLocale());
            }
            if (proposal != null) {
                // Update the proposal and echo back the new state
                State previousState = proposal.getState();
                proposal.setState(State.cancelled);
                proposal.setCancelerKey(proposal.getOwnerKey());
                proposal = BaseSteps.getProposalOperations().updateProposal(pm, proposal);
                Store store = BaseSteps.getStoreOperations().getStore(pm, saleAssociate.getStoreKey());
                message = CommandProcessor.generateTweet(proposal, store, false, saleAssociate.getLocale());
                if (State.confirmed.equals(previousState)) {
                    // Restore the demand in the published state
                    Demand demand = BaseSteps.getDemandOperations().getDemand(pm, proposal.getDemandKey(), null);
                    demand.setState(State.published);
                    demand.setCancelerKey(saleAssociate.getKey());
                    demand = BaseSteps.getDemandOperations().updateDemand(pm, demand);
                    // Inform the consumer of the state change
                    Consumer demandOwner = BaseSteps.getConsumerOperations().getConsumer(pm, demand.getOwnerKey());
                    RawCommand originalRawCommand = BaseSteps.getRawCommandOperations().getRawCommand(pm, demand.getRawCommandId());
                    String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposal.getKey() }, locale);
                    String demandRef = LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { demand.getKey() }, locale);
                    String tags = demand.getCriteria().size() == 0 ? "" : LabelExtractor.get("cp_tweet_tags_part", new Object[] { demand.getSerializedCriteria() }, locale);
                    communicateToConsumer(
                            originalRawCommand,
                            demandOwner,
                            new String[] { LabelExtractor.get("cp_command_cancel_proposal_canceled_demand_to_be_published", new Object[] { proposalRef, demandRef, tags }, locale) }
                    );
                }
                else if (!State.declined.equals(previousState)) {
                    proposal.getState();
                    // FIXME: inform the consumer who owns the attached demand about the cancellation
                }
            }
            communicateToConsumer(
                    rawCommand,
                    saConsumerRecord,
                    new String[] { message }
            );
        }
        else {
            communicateToConsumer(
                    rawCommand,
                    consumer,
                    new String[] { LabelExtractor.get("cp_command_cancel_missing_demand_id", consumer.getLocale()) }
            );
        }
        ddd **********/
    }
}
