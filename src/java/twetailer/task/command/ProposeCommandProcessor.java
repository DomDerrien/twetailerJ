package twetailer.task.command;

import static twetailer.connector.BaseConnector.communicateToConsumer;

import java.util.ArrayList;
import java.util.List;
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
        List<String> messages = new ArrayList<String>();

        if (!command.containsKey(Proposal.PROPOSAL_KEY)) {
            try {
                SaleAssociate saleAssociate = CommandProcessor.retrieveSaleAssociate(pm, consumer, Action.propose, Demand.class.getName());
                Store store = StoreSteps.getStore(pm, saleAssociate.getStoreKey());

                // Create new proposal
                command.put(Command.SOURCE, rawCommand.getSource().toString());
                command.put(Command.RAW_COMMAND_ID, rawCommand.getKey());
                command.put(Command.LOCATION_KEY, store.getLocationKey());
                if (command.containsKey(Proposal.DEMAND_REFERENCE)) {
                    command.put(Proposal.DEMAND_KEY, command.getLong(Proposal.DEMAND_REFERENCE));
                }

                Proposal proposal = ProposalSteps.createProposal(pm, command, saleAssociate, consumer);

                // Prepare the informative message successfully created demand
                //
                // TODO: get the #hashtag to decide which label set to use!
                //
                String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposal.getKey() }, locale);
                messages.add(LabelExtractor.get("cp_command_propose_acknowledge_creation", new Object[] { proposalRef }, locale));
                messages.add(CommandProcessor.generateTweet(proposal, store, false, locale));
            }
            catch(ReservedOperationException ex) {
                messages.add(LabelExtractor.get("cp_command_parser_reserved_action", new String[] { ex.getAction().toString() }, locale));
            }
        }
        else {
            Long proposalKey = command.getLong(Proposal.PROPOSAL_KEY);
            try {
                SaleAssociate saleAssociate = CommandProcessor.retrieveSaleAssociate(pm, consumer, Action.propose, Demand.class.getName());
                Store store = StoreSteps.getStore(pm, saleAssociate.getStoreKey());

                // Update specified proposal
                Proposal proposal = ProposalSteps.updateProposal(pm, proposalKey, command, saleAssociate, consumer);

                // Prepare the informative message successfully created proposal
                //
                // TODO: get the #hashtag to decide which label set to use!
                //
                messages.add(CommandProcessor.generateTweet(proposal, store, false, locale));
            }
            catch(InvalidIdentifierException ex) {
                messages.add(LabelExtractor.get("cp_command_propose_invalid_proposal_id", locale));
            }
            catch(InvalidStateException ex) {
                String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposalKey }, locale);
                String stateLabel = CommandLineParser.localizedStates.get(locale).getString(ex.getEntityState());
                stateLabel = LabelExtractor.get("cp_tweet_state_part", new Object[] { stateLabel }, locale);
                messages.add(LabelExtractor.get("cp_command_propose_non_modifiable_state", new Object[] { proposalRef, stateLabel }, locale));
            }
            catch(ReservedOperationException ex) {
                messages.add(LabelExtractor.get("cp_command_parser_reserved_action", new String[] { ex.getAction().toString() }, locale));
            }
        }

        // Inform the proposal owner
        communicateToConsumer(
                rawCommand,
                consumer,
                messages.toArray(new String[messages.size()])
        );

        /********** ddd
        //
        // Used by a sale associate to:
        //
        // 1. create a new proposal
        // 2. update the identified proposal
        //
        Long proposalKey = 0L;
        SaleAssociate saleAssociate = CommandProcessor.retrieveSaleAssociate(pm, consumer, Action.propose, Proposal.class.getName());
        Consumer saConsumerRecord = BaseSteps.getConsumerOperations().getConsumer(pm, saleAssociate.getConsumerKey());
        Store store = BaseSteps.getStoreOperations().getStore(pm, saleAssociate.getStoreKey());
        if (command.containsKey(Proposal.PROPOSAL_KEY)) {
            // Update the proposal attributes
            Proposal proposal = null;
            try {
                proposal = BaseSteps.getProposalOperations().getProposal(pm, command.getLong(Proposal.PROPOSAL_KEY), null, saleAssociate.getStoreKey());
            }
            catch(Exception ex) {
                messages.add(LabelExtractor.get("cp_command_propose_invalid_proposal_id", saleAssociate.getLocale()));
            }
            if (proposal != null) {
                State state = proposal.getState();
                if (state.equals(State.opened) || state.equals(State.published) || state.equals(State.invalid)) {
                    proposal.fromJson(command);
                    proposal.setState(State.opened); // Will force the re-validation of the entire proposal
                    proposal = BaseSteps.getProposalOperations().updateProposal(pm, proposal);
                    // Echo back the updated proposal
                    messages.add(CommandProcessor.generateTweet(proposal, store, false, saleAssociate.getLocale()));
                    // Get the proposalKey for the task scheduling
                    proposalKey = proposal.getKey();
                }
                else {
                    String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposal.getKey() }, locale);
                    String stateLabel = CommandLineParser.localizedStates.get(locale).getString(state.toString());
                    stateLabel = LabelExtractor.get("cp_tweet_state_part", new Object[] { stateLabel }, locale);
                    messages.add(LabelExtractor.get("cp_command_propose_non_modifiable_state", new Object[] { proposalRef, stateLabel }, locale));
                }
            }
        }
        else {
            // Get the proposal attributes
            command.put(Command.SOURCE, rawCommand.getSource().toString());
            command.put(Command.RAW_COMMAND_ID, rawCommand.getKey());
            command.put(Command.LOCATION_KEY, store.getLocationKey());
            // Persist the new proposal
            Proposal newProposal = BaseSteps.getProposalOperations().createProposal(pm, command, saleAssociate);
            String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { newProposal.getKey() }, locale);
            messages.add(
                    LabelExtractor.get(
                            "cp_command_propose_acknowledge_creation",
                            new Object[] { proposalRef },
                            saleAssociate.getLocale()
                    )
            );
            messages.add(CommandProcessor.generateTweet(newProposal, store, false, saleAssociate.getLocale()));
            // Get the proposalKey for the task scheduling
            proposalKey = newProposal.getKey();
        }

        // Communicate message
        communicateToConsumer(
                rawCommand,
                saConsumerRecord,
                messages.toArray(new String[0])
        );

        // Create a task for that proposal
        if (proposalKey != 0L) {
            Queue queue = BaseSteps.getBaseOperations().getQueue();
            log.warning("Preparing the task: /maezel/validateOpenProposal?key=" + proposalKey.toString());
            queue.add(
                    url(ApplicationSettings.get().getServletApiPath() + "/maezel/validateOpenProposal").
                        param(Proposal.KEY, proposalKey.toString()).
                        method(Method.GET)
            );
        }
        ddd *******/
    }
}
