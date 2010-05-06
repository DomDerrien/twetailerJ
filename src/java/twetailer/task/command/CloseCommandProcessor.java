package twetailer.task.command;

import static twetailer.connector.BaseConnector.communicateToConsumer;
import static twetailer.connector.BaseConnector.communicateToSaleAssociate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dao.RawCommandOperations;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Proposal;
import twetailer.dto.RawCommand;
import twetailer.dto.SaleAssociate;
import twetailer.task.CommandLineParser;
import twetailer.task.CommandProcessor;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.State;
import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.JsonObject;

public class CloseCommandProcessor {
    public static void processCloseCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command) throws ClientException, DataSourceException {
        //
        // Used by the resource owner to report that the expected product has been delivered
        //
        // 1. Close the identified demand
        // 2. Close the identified proposal
        if (command.containsKey(Demand.REFERENCE)) {
            Demand demand = null;
            String message = null;
            try {
                demand = CommandProcessor.demandOperations.getDemand(pm, command.getLong(Demand.REFERENCE), consumer.getKey());
                State state = demand.getState();
                if (!State.confirmed.equals(state)) {
                    String stateLabel = CommandLineParser.localizedStates.get(consumer.getLocale()).getString(state.toString());
                    message = LabelExtractor.get("cp_command_close_invalid_demand_state", new Object[] { demand.getKey(), stateLabel },  consumer.getLocale());
                    demand = null; // To stop the process
                }
                else {
                    demand.setState(State.closed);
                    demand = CommandProcessor.demandOperations.updateDemand(pm, demand);
                    message = LabelExtractor.get("cp_command_close_acknowledge_demand_closing", new Object[] { demand.getKey() }, consumer.getLocale());
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
                    List<Proposal> proposals = CommandProcessor.proposalOperations.getProposals(pm, parameters, 1);
                    if (0 < proposals.size()) {
                        Proposal proposal = proposals.get(0);
                        SaleAssociate saleAssociate = CommandProcessor.saleAssociateOperations.getSaleAssociate(pm, proposal.getOwnerKey());
                        RawCommand originalRawCommand = CommandProcessor.rawCommandOperations.getRawCommand(pm, proposal.getRawCommandId());
                        communicateToSaleAssociate(
                                originalRawCommand,
                                saleAssociate,
                                new String[] { LabelExtractor.get("cp_command_close_demand_closed_proposal_to_close", new Object[] { demand.getKey(), proposal.getKey() }, consumer.getLocale()) }
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
            try {
                proposal = CommandProcessor.proposalOperations.getProposal(pm, command.getLong(Proposal.PROPOSAL_KEY), saleAssociate.getKey(), null);
                State state = proposal.getState();
                if (!State.confirmed.equals(state)) {
                    String stateLabel = CommandLineParser.localizedStates.get(consumer.getLocale()).getString(state.toString());
                    message = LabelExtractor.get("cp_command_close_invalid_proposal_state", new Object[] { proposal.getKey(), stateLabel },  consumer.getLocale());
                    proposal = null; // To stop the process
                }
                else {
                    proposal.setState(State.closed);
                    proposal = CommandProcessor.proposalOperations.updateProposal(pm, proposal);
                    message = LabelExtractor.get("cp_command_close_acknowledge_proposal_closing", new Object[] { proposal.getKey() }, consumer.getLocale());
                }
            }
            catch(Exception ex) {
                message = LabelExtractor.get("cp_command_close_invalid_proposal_id", consumer.getLocale());
            }
            communicateToSaleAssociate(
                    rawCommand,
                    saleAssociate,
                    new String[] { message }
            );
            if (proposal != null) {
                Map<String, Object> parameters = new HashMap<String, Object>();
                parameters.put(Demand.PROPOSAL_KEYS, proposal.getKey());
                parameters.put(Command.STATE, State.confirmed.toString());
                try {
                    Demand demand = CommandProcessor.demandOperations.getDemand(pm, proposal.getDemandKey(), null);
                    Consumer demandOwner = CommandProcessor.consumerOperations.getConsumer(pm, demand.getOwnerKey());
                    RawCommand originalRawCommand = CommandProcessor.rawCommandOperations.getRawCommand(pm, demand.getRawCommandId());
                    communicateToConsumer(
                            originalRawCommand,
                            demandOwner,
                            new String[] { LabelExtractor.get("cp_command_close_proposal_closed_demand_to_close", new Object[] { proposal.getKey(), demand.getKey() }, consumer.getLocale()) }
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
    }
}
