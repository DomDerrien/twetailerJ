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
                    rawCommand.getSource(),
                    rawCommand.getSubject(),
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
                    rawCommand.getSource(),
                    rawCommand.getSubject(),
                    consumer,
                    new String[] { message }
            );
            return;
        }

        communicateToConsumer(
                rawCommand.getSource(),
                rawCommand.getSubject(),
                consumer,
                new String[] { LabelExtractor.get("cp_command_delete_invalid_parameters", consumer.getLocale()) }
        );
    }
}
