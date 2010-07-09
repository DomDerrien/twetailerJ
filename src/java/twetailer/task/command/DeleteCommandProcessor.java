package twetailer.task.command;

import static twetailer.connector.BaseConnector.communicateToConsumer;

import java.util.Locale;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.InvalidStateException;
import twetailer.ReservedOperationException;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Proposal;
import twetailer.dto.RawCommand;
import twetailer.dto.SaleAssociate;
import twetailer.task.CommandLineParser;
import twetailer.task.CommandProcessor;
import twetailer.task.step.DemandSteps;
import twetailer.task.step.ProposalSteps;
import twetailer.validator.CommandSettings.Action;
import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.JsonObject;

public class DeleteCommandProcessor {

    public static void processDeleteCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command) throws ClientException, DataSourceException {

        Locale locale = consumer.getLocale();

        // Delete identified Demand
        if (command.containsKey(Demand.REFERENCE)) {
            String message = null;
            Long entityKey = command.getLong(Demand.REFERENCE);
            try {
                DemandSteps.deleteDemand(pm, entityKey, consumer);
                // Echo back the specified demand
                String demandRef = LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { entityKey }, locale);
                message = LabelExtractor.get("cp_command_delete_acknowledge_demand_markedForDeletion", new Object[] { demandRef }, locale);
            }
            catch(InvalidIdentifierException ex) {
                message = LabelExtractor.get("cp_command_delete_invalid_demand_id", locale);
            }
            catch(InvalidStateException ex) {
                String demandRef = LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { entityKey }, locale);
                String stateLabel = CommandLineParser.localizedStates.get(locale).getString(ex.getEntityState().toString());
                stateLabel = LabelExtractor.get("cp_tweet_state_part", new Object[] { stateLabel }, locale);
                message = LabelExtractor.get("cp_command_delete_invalid_demand_state", new Object[] { demandRef, stateLabel },  locale);
            }
            communicateToConsumer(
                    rawCommand,
                    consumer,
                    new String[] { message }
            );
            return;
        }

        // Delete identified Proposal
        if (command.containsKey(Proposal.PROPOSAL_KEY)) {
            String message = null;
            Long entityKey = command.getLong(Proposal.PROPOSAL_KEY);
            try {
                SaleAssociate saleAssociate = CommandProcessor.retrieveSaleAssociate(pm, consumer, Action.delete, Demand.class.getName());
                ProposalSteps.deleteProposal(pm, entityKey, saleAssociate, consumer);
                // Echo back the specified proposal
                String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { entityKey }, locale);
                message = LabelExtractor.get("cp_command_delete_acknowledge_proposal_markedForDeletion", new Object[] { proposalRef }, locale);
            }
            catch(InvalidIdentifierException ex) {
                message = LabelExtractor.get("cp_command_delete_invalid_proposal_id", locale);
            }
            catch(InvalidStateException ex) {
                String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { entityKey }, locale);
                String stateLabel = CommandLineParser.localizedStates.get(locale).getString(ex.getEntityState().toString());
                stateLabel = LabelExtractor.get("cp_tweet_state_part", new Object[] { stateLabel }, locale);
                message = LabelExtractor.get("cp_command_delete_invalid_proposal_state", new Object[] { proposalRef, stateLabel },  locale);
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
                new String[] { LabelExtractor.get("cp_command_delete_invalid_parameters", consumer.getLocale()) }
        );

        /************** ddd
        //
        // Used by the resource owner to delete it
        //
        // 1. Delete the identified demand
        // 2. Delete the identified proposal
        if (command.containsKey(Demand.REFERENCE)) {
            Demand demand = null;
            String message = null;
            try {
                demand = BaseSteps.getDemandOperations().getDemand(pm, command.getLong(Demand.REFERENCE), consumer.getKey());
                String demandRef = LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { demand.getKey() }, locale);
                State state = demand.getState();
                if (!State.cancelled.equals(state)) {
                    String stateLabel = CommandLineParser.localizedStates.get(locale).getString(state.toString());
                    stateLabel = LabelExtractor.get("cp_tweet_state_part", new Object[] { stateLabel }, locale);
                    message = LabelExtractor.get("cp_command_delete_invalid_demand_state", new Object[] { demandRef, stateLabel },  locale);
                    demand = null; // To stop the process
                }
                else {
                    demand.setState(State.markedForDeletion);
                    demand = BaseSteps.getDemandOperations().updateDemand(pm, demand);
                    message = LabelExtractor.get("cp_command_delete_acknowledge_demand_markedForDeletion", new Object[] { demandRef }, locale);
                }
            }
            catch(Exception ex) {
                message = LabelExtractor.get("cp_command_delete_invalid_demand_id", consumer.getLocale());
            }
            communicateToConsumer(
                    rawCommand,
                    consumer,
                    new String[] { message }
            );
        }
        else if (command.containsKey(Proposal.PROPOSAL_KEY)) {
            Proposal proposal = null;
            String message = null;
            SaleAssociate saleAssociate = CommandProcessor.retrieveSaleAssociate(pm, consumer, Action.delete, Proposal.class.getName());
            Consumer saConsumerRecord = BaseSteps.getConsumerOperations().getConsumer(pm, saleAssociate.getConsumerKey());
            try {
                proposal = BaseSteps.getProposalOperations().getProposal(pm, command.getLong(Proposal.PROPOSAL_KEY), saleAssociate.getKey(), null);
                State state = proposal.getState();
                String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposal.getKey() }, locale);
                if (!State.cancelled.equals(state)) {
                    String stateLabel = CommandLineParser.localizedStates.get(locale).getString(state.toString());
                    stateLabel = LabelExtractor.get("cp_tweet_state_part", new Object[] { stateLabel }, locale);
                    message = LabelExtractor.get("cp_command_delete_invalid_proposal_state", new Object[] { proposalRef, stateLabel },  locale);
                    proposal = null; // To stop the process
                }
                else {
                    proposal.setState(State.markedForDeletion);
                    proposal = BaseSteps.getProposalOperations().updateProposal(pm, proposal);
                    message = LabelExtractor.get("cp_command_delete_acknowledge_proposal_closing", new Object[] { proposalRef }, locale);
                }
            }
            catch(Exception ex) {
                message = LabelExtractor.get("cp_command_delete_invalid_proposal_id", saleAssociate.getLocale());
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
                    new String[] { LabelExtractor.get("cp_command_delete_invalid_parameters", consumer.getLocale()) }
            );
        }
        ddd ************/
    }
}
