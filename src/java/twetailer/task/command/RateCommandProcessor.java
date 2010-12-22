package twetailer.task.command;

import static twetailer.connector.BaseConnector.communicateToConsumer;

import java.util.Locale;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.InvalidStateException;
import twetailer.ReservedOperationException;
import twetailer.connector.MessageGenerator;
import twetailer.connector.MessageGenerator.MessageId;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Proposal;
import twetailer.dto.RawCommand;
import twetailer.dto.Command.QueryPointOfView;
import twetailer.task.CommandLineParser;
import twetailer.task.step.ProposalSteps;
import twetailer.validator.CommandSettings.Action;
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
            Proposal proposal = null;
            Long entityKey = command.getLong(Proposal.PROPOSAL_KEY);
            try {
                proposal = ProposalSteps.updateProposal(pm, rawCommand, entityKey, getFreshRatingParameters(command), consumer);
                return;
            }
            catch(InvalidIdentifierException ex) {
                String actionLabel = CommandLineParser.localizedActions.get(locale).getString(Action.rate.toString());
                MessageGenerator msgGen = new MessageGenerator(rawCommand.getSource(), null, locale);
                msgGen.
                    put("user>name", consumer.getName()).
                    put("user>action", actionLabel).
                    put("user>command", rawCommand.getCommand()).
                    put("message>footer", msgGen.getAlternateMessage(MessageId.messageFooter));
                message = msgGen.getMessage(MessageId.PROPOSAL_INVALID_ID_ERROR);
            }
            catch(InvalidStateException ex) {
                String actionLabel = CommandLineParser.localizedActions.get(locale).getString(Action.rate.toString());
                String stateLabel = CommandLineParser.localizedStates.get(locale).getString(ex.getEntityState().toString());
                MessageGenerator msgGen = new MessageGenerator(rawCommand.getSource(), null, locale);
                msgGen.
                    put("user>name", consumer.getName()).
                    put("user>action", actionLabel).
                    put("user>command", rawCommand.getCommand()).
                    put("proposal>key", proposal == null ? "0" : proposal.getKey()).
                    put("proposal>state", stateLabel).
                    put("message>footer", msgGen.getAlternateMessage(MessageId.messageFooter));
                message = msgGen.getMessage(MessageId.PROPOSAL_INVALID_STATE_ERROR);
            }
            catch(ReservedOperationException ex) {
                String actionLabel = CommandLineParser.localizedActions.get(locale).getString(Action.rate.toString());
                MessageGenerator msgGen = new MessageGenerator(rawCommand.getSource(), null, locale);
                msgGen.
                    put("user>name", consumer.getName()).
                    put("user>action", actionLabel).
                    put("user>command", rawCommand.getCommand()).
                    put("message>footer", msgGen.getAlternateMessage(MessageId.messageFooter));
                message = msgGen.getMessage(MessageId.RESERVED_OPERATION_ERROR);
            }
            communicateToConsumer(
                    rawCommand.getSource(),
                    rawCommand.getSubject(),
                    consumer,
                    new String[] { message }
            );
            return;
        }

        String actionLabel = CommandLineParser.localizedActions.get(locale).getString(Action.rate.toString());
        MessageGenerator msgGen = new MessageGenerator(rawCommand.getSource(), null, locale);
        msgGen.
            put("user>name", consumer.getName()).
            put("user>action", actionLabel).
            put("user>command", rawCommand.getCommand()).
            put("message>footer", msgGen.getAlternateMessage(MessageId.messageFooter));
        communicateToConsumer(
                rawCommand.getSource(),
                rawCommand.getSubject(),
                consumer,
                new String[] { msgGen.getMessage(MessageId.PROPOSAL_MISSING_ID_ERROR) }
        );
    }
}
