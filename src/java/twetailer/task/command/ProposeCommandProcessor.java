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
import twetailer.dto.Store;
import twetailer.task.CommandLineParser;
import twetailer.task.CommandProcessor;
import twetailer.task.step.ProposalSteps;
import twetailer.task.step.StoreSteps;
import twetailer.validator.CommandSettings.Action;
import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.JsonObject;

public class ProposeCommandProcessor {

    public static void processProposeCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command) throws ClientException, DataSourceException {

        Locale locale = consumer.getLocale();
        String message = null;

        if (!command.containsKey(Proposal.PROPOSAL_KEY)) {
            try {
                SaleAssociate saleAssociate = CommandProcessor.retrieveSaleAssociate(pm, consumer, Action.propose, Demand.class.getName());
                Store store = StoreSteps.getStore(pm, saleAssociate.getStoreKey());

                // Create new proposal
                command.put(Command.LOCATION_KEY, store.getLocationKey());

                ProposalSteps.createProposal(pm, command, saleAssociate, consumer);

                // Creation confirmation message will be sent by the ProposalValidator being given it's a valid proposal
            }
            catch(ReservedOperationException ex) {
                message = LabelExtractor.get("cp_command_parser_reserved_action", new String[] { ex.getAction().toString() }, locale);
            }
        }
        else {
            Long proposalKey = command.getLong(Proposal.PROPOSAL_KEY);
            try {
                SaleAssociate saleAssociate = CommandProcessor.retrieveSaleAssociate(pm, consumer, Action.propose, Demand.class.getName());

                // Update specified proposal
                ProposalSteps.updateProposal(pm, rawCommand, proposalKey, command, saleAssociate, consumer, false);

                // Update confirmation message will be sent by the ProposalValidator being given it's a valid proposal
            }
            catch(InvalidIdentifierException ex) {
                message = LabelExtractor.get("cp_command_propose_invalid_proposal_id", locale);
            }
            catch(InvalidStateException ex) {
                String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposalKey }, locale);
                String stateLabel = CommandLineParser.localizedStates.get(locale).getString(ex.getEntityState());
                stateLabel = LabelExtractor.get("cp_tweet_state_part", new Object[] { stateLabel }, locale);
                message = LabelExtractor.get("cp_command_propose_non_modifiable_state", new Object[] { proposalRef, stateLabel }, locale);
            }
            catch(ReservedOperationException ex) {
                message = LabelExtractor.get("cp_command_parser_reserved_action", new String[] { ex.getAction().toString() }, locale);
            }
        }

        // Inform the proposal owner
        if (message != null) {
            communicateToConsumer(
                    rawCommand.getSource(),
                    rawCommand.getSubject(),
                    consumer,
                    new String[] { message }
            );
        }
    }
}
