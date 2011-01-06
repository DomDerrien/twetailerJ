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
import twetailer.dto.Proposal;
import twetailer.dto.RawCommand;
import twetailer.dto.SaleAssociate;
import twetailer.task.CommandLineParser;
import twetailer.task.CommandProcessor;
import twetailer.task.step.DemandSteps;
import twetailer.task.step.ProposalSteps;
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
                DemandSteps.updateDemand(pm, rawCommand, entityKey, getFreshCancelParameters(), consumer, false);
                return;
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
                    rawCommand.getSource(),
                    rawCommand.getSubject(),
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
                ProposalSteps.updateProposal(pm, rawCommand, entityKey, getFreshCancelParameters(), saleAssociate, consumer, false);
                return;
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
                new String[] { LabelExtractor.get("cp_command_cancel_missing_demand_id", consumer.getLocale()) }
        );
    }
}
