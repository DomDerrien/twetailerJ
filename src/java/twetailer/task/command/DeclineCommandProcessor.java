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
import twetailer.dto.Proposal;
import twetailer.dto.RawCommand;
import twetailer.dto.Command.QueryPointOfView;
import twetailer.task.CommandLineParser;
import twetailer.task.step.ProposalSteps;
import twetailer.validator.CommandSettings.State;
import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class DeclineCommandProcessor {

    private static JsonObject declineParameters = new GenericJsonObject();

    private static JsonObject getFreshDeclineParameters() {
        declineParameters.removeAll();
        declineParameters.put(Command.STATE, State.declined.toString());
        declineParameters.put(Command.POINT_OF_VIEW, QueryPointOfView.CONSUMER.toString());
        return declineParameters;
    }

    public static void processDeclineCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command) throws ClientException, DataSourceException {

        Locale locale = consumer.getLocale();

        // Confirm identified Proposal
        if (command.containsKey(Proposal.PROPOSAL_KEY)) {
            String message = null;
            Long entityKey = command.getLong(Proposal.PROPOSAL_KEY);
            try {
                ProposalSteps.updateProposal(pm, entityKey, getFreshDeclineParameters(), consumer);
                // Echo back the successful confirmation
                String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { entityKey }, locale);
                message = LabelExtractor.get("cp_command_decline_acknowledge_proposal_declination", new Object[] { proposalRef }, locale);
            }
            catch(InvalidIdentifierException ex) {
                message = LabelExtractor.get("cp_command_decline_invalid_proposal_id", locale);
            }
            catch(InvalidStateException ex) {
                String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { entityKey }, locale);
                String stateLabel = CommandLineParser.localizedStates.get(locale).getString(ex.getEntityState().toString());
                stateLabel = LabelExtractor.get("cp_tweet_state_part", new Object[] { stateLabel }, locale);
                message = LabelExtractor.get("cp_command_decline_invalid_state_demand", new Object[] { proposalRef, stateLabel },  locale);
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
                new String[] { LabelExtractor.get("cp_command_decline_invalid_parameters", consumer.getLocale()) }
        );

        /*********** ddd
        //
        // Used by a consumer to refuse a proposal or by a sale associate to refuse a demand
        //
        String message = null;
        if (command.containsKey(Demand.REFERENCE)) {
            SaleAssociate saleAssociate = CommandProcessor.retrieveSaleAssociate(pm, consumer, Action.decline, Demand.class.getName());
            Consumer saConsumerRecord = BaseSteps.getConsumerOperations().getConsumer(pm, saleAssociate.getConsumerKey());
            communicateToConsumer(
                    rawCommand,
                    saConsumerRecord,
                    new String[] { "Not yet implemented!" } // FIXME: decides what it means for sale associates
            );
            return;
        }
        else if (command.containsKey(Proposal.PROPOSAL_KEY)) {
            try {
                // Get the proposal
                Proposal proposal = BaseSteps.getProposalOperations().getProposal(pm, command.getLong(Proposal.PROPOSAL_KEY), null, null);
                // Get the corresponding demand -- No need for this demand, just the verification it exists and it's owned by the logged in consumer
                BaseSteps.getDemandOperations().getDemand(pm, proposal.getDemandKey(), consumer.getKey());
                // Update the proposal state
                proposal.setState(State.declined);
                proposal = BaseSteps.getProposalOperations().updateProposal(pm, proposal);
                Locale locale = consumer.getLocale();
                String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposal.getKey() }, locale);
                message = LabelExtractor.get("cp_command_decline_acknowledge_proposal_closing", new Object[] { proposalRef }, locale);
            }
            catch(Exception ex) {
                message = LabelExtractor.get("cp_command_decline_invalid_proposal_id", consumer.getLocale());
            }
        }
        else {
            message = LabelExtractor.get("cp_command_decline_invalid_parameters", consumer.getLocale());
        }
        communicateToConsumer(
                rawCommand,
                consumer,
                new String[] { message }
        );
        ddd **********/
    }
}
