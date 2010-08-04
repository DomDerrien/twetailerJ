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
import twetailer.dto.Location;
import twetailer.dto.Proposal;
import twetailer.dto.RawCommand;
import twetailer.dto.Store;
import twetailer.dto.Command.QueryPointOfView;
import twetailer.task.CommandLineParser;
import twetailer.task.step.BaseSteps;
import twetailer.task.step.ProposalSteps;
import twetailer.validator.CommandSettings.State;
import domderrien.i18n.LabelExtractor;
import domderrien.i18n.LabelExtractor.ResourceFileId;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class ConfirmCommandProcessor {

    private static JsonObject confirmParameters = new GenericJsonObject();

    private static JsonObject getFreshConfirmParameters() {
        confirmParameters.removeAll();
        confirmParameters.put(Command.STATE, State.confirmed.toString());
        confirmParameters.put(Command.POINT_OF_VIEW, QueryPointOfView.CONSUMER.toString());
        return confirmParameters;
    }

    public static void processConfirmCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command) throws ClientException, DataSourceException {

        Locale locale = consumer.getLocale();

        // Confirm identified Proposal
        if (command.containsKey(Proposal.PROPOSAL_KEY)) {
            String message = null;
            Long entityKey = command.getLong(Proposal.PROPOSAL_KEY);
            try {
                Proposal proposal = ProposalSteps.updateProposal(pm, entityKey, getFreshConfirmParameters(), consumer);
                // Echo back the successful confirmation
                Store store = BaseSteps.getStoreOperations().getStore(pm, proposal.getStoreKey());
                Location location = BaseSteps.getLocationOperations().getLocation(pm, store.getLocationKey());

                String[] parameters = new String[] {
                        consumer.getName(), // 0
                        proposal.getKey().toString(), // 1
                        proposal.getDemandKey().toString(), // 2
                        proposal.getDueDate().toString(), // 3
                        proposal.getQuantity().toString(), // 4
                        proposal.getSerializedCriteria("none"), // 5
                        proposal.getSerializedHashTags("none"), // 6
                        "$", // 7
                        proposal.getPrice().toString(), // 8
                        proposal.getTotal().toString(), // 9
                        store.getKey().toString(), // 10
                        store.getName(), // 11
                        store.getAddress(), // 12
                        store.getUrl(), // 13
                        store.getPhoneNumber(), // 14
                        location.getPostalCode(), // 15
                        location.getCountryCode(), // 16
                        LabelExtractor.get(ResourceFileId.fourth, "long_golf_footer", locale), // 17
                        "0", // 18
                        "0" // 19
                };

                // Send the proposal details to the owner
                message = MessageGenerator.getMessage(
                        rawCommand.getSource(),
                        proposal.getHashTags(),
                        MessageId.proposalConfirmationAck,
                        parameters,
                        locale
                );
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
                    rawCommand,
                    consumer,
                    new String[] { message }
            );
            return;
        }

        communicateToConsumer(
                rawCommand,
                consumer,
                new String[] { LabelExtractor.get("cp_command_confirm_missing_proposal_id", consumer.getLocale()) }
        );
    }
}
