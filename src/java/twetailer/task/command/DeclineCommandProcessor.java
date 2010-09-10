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
                new String[] { LabelExtractor.get("cp_command_decline_invalid_parameters", consumer.getLocale()) }
        );
    }
}
