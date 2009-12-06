package twetailer.task.command;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;
import static twetailer.connector.BaseConnector.communicateToSaleAssociate;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Proposal;
import twetailer.dto.RawCommand;
import twetailer.dto.SaleAssociate;
import twetailer.task.CommandProcessor;
import twetailer.validator.ApplicationSettings;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.State;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.JsonObject;

public class ProposeCommandProcessor {
    public static void processProposeCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command) throws ClientException, DataSourceException {
        //
        // Used by a sale associate to:
        //
        // 1. create a new proposal
        // 2. update the identified proposal
        //
        Long proposalKey = 0L;
        SaleAssociate saleAssociate = CommandProcessor.retrieveSaleAssociate(pm, consumer, Action.propose);
        if (command.containsKey(Proposal.PROPOSAL_KEY)) {
            // Update the proposal attributes
            Proposal proposal = null;
            try {
                proposal = CommandProcessor.proposalOperations.getProposal(pm, command.getLong(Proposal.PROPOSAL_KEY), null, saleAssociate.getStoreKey());
            }
            catch(Exception ex) {
                communicateToSaleAssociate(
                        rawCommand,
                        saleAssociate,
                        LabelExtractor.get("cp_command_proposal_invalid_proposal_id", consumer.getLocale())
                );
            }
            if (proposal != null) {
                State state = proposal.getState();
                if (state.equals(State.opened) || state.equals(State.published) || state.equals(State.invalid)) {
                    proposal.fromJson(command);
                    proposal.setState(State.opened); // Will force the re-validation of the entire proposal
                    proposal = CommandProcessor.proposalOperations.updateProposal(pm, proposal);
                    // Echo back the updated proposal
                    communicateToSaleAssociate(
                            rawCommand,
                            saleAssociate,
                            CommandProcessor.generateTweet(proposal, consumer.getLocale())
                    );
                    // Get the proposalKey for the task scheduling
                    proposalKey = proposal.getKey();
                }
                else {
                    communicateToSaleAssociate(
                            rawCommand,
                            saleAssociate,
                            LabelExtractor.get("cp_command_proposal_non_modifiable_state", new Object[] { proposal.getKey(), state }, consumer.getLocale())
                    );
                }
            }
        }
        else {
            // Get the proposal attributes
            command.put(Command.SOURCE, rawCommand.getSource().toString());
            command.put(Command.RAW_COMMAND_ID, rawCommand.getKey());
            // Persist the new proposal
            Proposal newProposal = CommandProcessor.proposalOperations.createProposal(pm, command, saleAssociate);
            communicateToSaleAssociate(
                    rawCommand,
                    saleAssociate,
                    LabelExtractor.get(
                            "cp_command_proposal_acknowledge_creation",
                            new Object[] { newProposal.getKey() },
                            consumer.getLocale()
                    )
            );
            communicateToSaleAssociate(
                    rawCommand,
                    saleAssociate,
                    CommandProcessor.generateTweet(newProposal, consumer.getLocale())
            );
            // Get the proposalKey for the task scheduling
            proposalKey = newProposal.getKey();
        }

        // Temporary warning
        String hashTag = command.getString(Command.HASH_TAG);
        if (hashTag != null){
            communicateToSaleAssociate(
                    rawCommand,
                    saleAssociate,
                    LabelExtractor.get("cp_command_proposal_hashtag_warning", new Object[] { proposalKey, hashTag }, consumer.getLocale())
            );
        }

        // Create a task for that proposal
        if (proposalKey != 0L) {
            Queue queue = CommandProcessor._baseOperations.getQueue();
            queue.add(
                    url(ApplicationSettings.get().getServletApiPath() + "/maezel/validateOpenProposal").
                        param(Proposal.KEY, proposalKey.toString()).
                        method(Method.GET)
            );
        }
    }
}
