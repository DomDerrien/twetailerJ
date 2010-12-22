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
import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class RateCommandProcessor {

    private static JsonObject ratingParameters = new GenericJsonObject();

    private static JsonObject getFreshRatingParameters(JsonObject command) {
        ratingParameters.removeAll();
        ratingParameters.put(Proposal.SCORE, command.getLong(Proposal.SCORE));
        if (command.containsKey(Proposal.COMMENT)) {
            ratingParameters.put(Proposal.COMMENT, command.getString(Proposal.COMMENT));
        }
        ratingParameters.put(Command.POINT_OF_VIEW, QueryPointOfView.CONSUMER.toString());
        return ratingParameters;
    }

    public static void processRateCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command) throws ClientException, DataSourceException {

        Locale locale = consumer.getLocale();

        if (command.containsKey(Proposal.PROPOSAL_KEY)) {
            String message = null;
            Long entityKey = command.getLong(Proposal.PROPOSAL_KEY);
            try {
                ProposalSteps.updateProposal(pm, rawCommand, entityKey, getFreshRatingParameters(command), consumer);
                return;
            }
            catch(InvalidIdentifierException ex) {
                message = LabelExtractor.get("cp_command_confirm_invalid_proposal_id", locale);
            }
            catch(InvalidStateException ex) {
                String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { entityKey }, locale);
                String stateLabel = CommandLineParser.localizedStates.get(locale).getString(ex.getEntityState().toString());
                stateLabel = LabelExtractor.get("cp_tweet_state_part", new Object[] { stateLabel }, locale);
                message = LabelExtractor.get("cp_command_confirm_invalid_state_demand", new Object[] { proposalRef, stateLabel },  locale);
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
                new String[] { LabelExtractor.get("cp_command_confirm_missing_proposal_id", consumer.getLocale()) }
        );
    }
}
