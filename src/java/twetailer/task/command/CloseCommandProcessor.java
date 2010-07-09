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

public class CloseCommandProcessor {

    private static JsonObject closeParameters = new GenericJsonObject();

    private static JsonObject getFreshCloseParameters() {
        closeParameters.removeAll();
        closeParameters.put(Command.STATE, State.closed.toString());
        return closeParameters;
    }

    public static void processCloseCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command) throws ClientException, DataSourceException {

        Locale locale = consumer.getLocale();

        // Close identified Demand
        if (command.containsKey(Demand.REFERENCE)) {
            String message = null;
            Long entityKey = command.getLong(Demand.REFERENCE);
            try {
                DemandSteps.updateDemand(pm, entityKey, getFreshCloseParameters(), consumer);
                // Echo back the specified demand
                String demandRef = LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { entityKey }, locale);
                message = LabelExtractor.get("cp_command_close_acknowledge_demand_closing", new Object[] { demandRef }, locale);
            }
            catch(InvalidIdentifierException ex) {
                message = LabelExtractor.get("cp_command_close_invalid_demand_id", locale);
            }
            catch(InvalidStateException ex) {
                String demandRef = LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { entityKey }, locale);
                String stateLabel = CommandLineParser.localizedStates.get(locale).getString(ex.getEntityState().toString());
                stateLabel = LabelExtractor.get("cp_tweet_state_part", new Object[] { stateLabel }, locale);
                message = LabelExtractor.get("cp_command_close_invalid_demand_state", new Object[] { demandRef, stateLabel },  locale);
            }
            communicateToConsumer(
                    rawCommand,
                    consumer,
                    new String[] { message }
            );
            return;
        }

        // Close identified Proposal
        if (command.containsKey(Proposal.PROPOSAL_KEY)) {
            String message = null;
            Long entityKey = command.getLong(Proposal.PROPOSAL_KEY);
            try {
                SaleAssociate saleAssociate = CommandProcessor.retrieveSaleAssociate(pm, consumer, Action.close, Demand.class.getName());
                ProposalSteps.updateProposal(pm, entityKey, getFreshCloseParameters(), saleAssociate, consumer);
                // Echo back the specified proposal
                String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { entityKey }, locale);
                message = LabelExtractor.get("cp_command_close_acknowledge_proposal_closing", new Object[] { proposalRef }, locale);
            }
            catch(InvalidIdentifierException ex) {
                message = LabelExtractor.get("cp_command_close_invalid_proposal_id", locale);
            }
            catch(InvalidStateException ex) {
                String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { entityKey }, locale);
                String stateLabel = CommandLineParser.localizedStates.get(locale).getString(ex.getEntityState().toString());
                stateLabel = LabelExtractor.get("cp_tweet_state_part", new Object[] { stateLabel }, locale);
                message = LabelExtractor.get("cp_command_close_invalid_proposal_state", new Object[] { proposalRef, stateLabel },  locale);
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
                new String[] { LabelExtractor.get("cp_command_close_invalid_parameters", consumer.getLocale()) }
        );

        /*********** ddd
        //
        // Used by the resource owner to report that the expected product has been delivered
        //
        // 1. Close the identified demand
        // 2. Close the identified proposal
        if (command.containsKey(Demand.REFERENCE)) {
            Demand demand = null;
            String message = null;
            try {
                demand = BaseSteps.getDemandOperations().getDemand(pm, command.getLong(Demand.REFERENCE), consumer.getKey());
                String demandRef = LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { demand.getKey() }, locale);
                State state = demand.getState();
                if (!State.confirmed.equals(state)) {
                    String stateLabel = CommandLineParser.localizedStates.get(locale).getString(state.toString());
                    stateLabel = LabelExtractor.get("cp_tweet_state_part", new Object[] { stateLabel }, locale);
                    message = LabelExtractor.get("cp_command_close_invalid_demand_state", new Object[] { demandRef, stateLabel },  locale);
                    demand = null; // To stop the process
                }
                else {
                    demand.setState(State.closed);
                    demand = BaseSteps.getDemandOperations().updateDemand(pm, demand);
                    message = LabelExtractor.get("cp_command_close_acknowledge_demand_closing", new Object[] { demandRef }, locale);
                }
            }
            catch(Exception ex) {
                message = LabelExtractor.get("cp_command_close_invalid_demand_id", consumer.getLocale());
            }
            communicateToConsumer(
                    rawCommand,
                    consumer,
                    new String[] { message }
            );
            if (demand !=  null) {
                Map<String, Object> parameters = new HashMap<String, Object>();
                parameters.put(Proposal.DEMAND_KEY, demand.getKey());
                parameters.put(Command.STATE, State.confirmed.toString());
                try {
                    List<Proposal> proposals = BaseSteps.getProposalOperations().getProposals(pm, parameters, 1);
                    if (0 < proposals.size()) {
                        Proposal proposal = proposals.get(0);
                        SaleAssociate saleAssociate = BaseSteps.getSaleAssociateOperations().getSaleAssociate(pm, proposal.getOwnerKey());
                        Consumer saConsumerRecord = BaseSteps.getConsumerOperations().getConsumer(pm, saleAssociate.getConsumerKey());
                        RawCommand originalRawCommand = BaseSteps.getRawCommandOperations().getRawCommand(pm, proposal.getRawCommandId());
                        Locale locale = saleAssociate.getLocale();
                        String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposal.getKey() }, locale);
                        String demandRef = LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { demand.getKey() }, locale);
                        communicateToConsumer(
                                originalRawCommand,
                                saConsumerRecord,
                                new String[] { LabelExtractor.get("cp_command_close_demand_closed_proposal_to_close", new Object[] { demandRef, proposalRef }, locale) }
                        );
                    }
                }
                catch(Exception ex) {
                    // Too bad, the proposal owner can be informed about the demand closing...
                    // He/she can still do it without notification
                }
            }
        }
        else if (command.containsKey(Proposal.PROPOSAL_KEY)) {
            Proposal proposal = null;
            String message = null;
            SaleAssociate saleAssociate = CommandProcessor.retrieveSaleAssociate(pm, consumer, Action.close, Proposal.class.getName());
            Consumer saConsumerRecord = BaseSteps.getConsumerOperations().getConsumer(pm, saleAssociate.getConsumerKey());
            try {
                proposal = BaseSteps.getProposalOperations().getProposal(pm, command.getLong(Proposal.PROPOSAL_KEY), saleAssociate.getKey(), null);
                String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposal.getKey() }, locale);
                State state = proposal.getState();
                if (!State.confirmed.equals(state)) {
                    String stateLabel = CommandLineParser.localizedStates.get(locale).getString(state.toString());
                    stateLabel = LabelExtractor.get("cp_tweet_state_part", new Object[] { stateLabel }, locale);
                    message = LabelExtractor.get("cp_command_close_invalid_proposal_state", new Object[] { proposalRef, stateLabel },  locale);
                    proposal = null; // To stop the process
                }
                else {
                    proposal.setState(State.closed);
                    proposal = BaseSteps.getProposalOperations().updateProposal(pm, proposal);
                    message = LabelExtractor.get("cp_command_close_acknowledge_proposal_closing", new Object[] { proposalRef }, locale);
                }
            }
            catch(Exception ex) {
                message = LabelExtractor.get("cp_command_close_invalid_proposal_id", saleAssociate.getLocale());
            }
            communicateToConsumer(
                    rawCommand,
                    saConsumerRecord,
                    new String[] { message }
            );
            if (proposal != null) {
                Map<String, Object> parameters = new HashMap<String, Object>();
                parameters.put(Demand.PROPOSAL_KEYS, proposal.getKey());
                parameters.put(Command.STATE, State.confirmed.toString());
                try {
                    Demand demand = BaseSteps.getDemandOperations().getDemand(pm, proposal.getDemandKey(), null);
                    Consumer demandOwner = BaseSteps.getConsumerOperations().getConsumer(pm, demand.getOwnerKey());
                    RawCommand originalRawCommand = BaseSteps.getRawCommandOperations().getRawCommand(pm, demand.getRawCommandId());
                    Locale locale = demandOwner.getLocale();
                    String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposal.getKey() }, locale);
                    String demandRef = LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { demand.getKey() }, locale);
                    communicateToConsumer(
                            originalRawCommand,
                            demandOwner,
                            new String[] { LabelExtractor.get("cp_command_close_proposal_closed_demand_to_close", new Object[] { proposalRef, demandRef }, locale) }
                    );
                }
                catch(Exception ex) {
                    // Too bad, the demand owner can be informed about the proposal closing...
                    // He/she can still do it without notification
                }
            }
        }
        else {
            communicateToConsumer(
                    rawCommand,
                    consumer,
                    new String[] { LabelExtractor.get("cp_command_close_invalid_parameters", consumer.getLocale()) }
            );
        }
        ddd ***********/
    }
}
