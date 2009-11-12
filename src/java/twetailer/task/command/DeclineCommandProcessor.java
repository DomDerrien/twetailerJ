package twetailer.task.command;

import static twetailer.connector.BaseConnector.communicateToConsumer;
import static twetailer.connector.BaseConnector.communicateToSaleAssociate;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Proposal;
import twetailer.dto.RawCommand;
import twetailer.dto.SaleAssociate;
import twetailer.task.CommandProcessor;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.State;
import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.JsonObject;

public class DeclineCommandProcessor {
    public static void processDeclineCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command) throws ClientException, DataSourceException {
        //
        // Used by a consumer to refuse a proposal or by a sale associate to refuse a demand
        //
        if (command.containsKey(Demand.REFERENCE)) {
            SaleAssociate saleAssociate = CommandProcessor.retrieveSaleAssociate(pm, consumer, Action.decline);
            communicateToSaleAssociate(
                    rawCommand.getSource(),
                    saleAssociate,
                    "Not yet implemented!" // FIXME: decides what it means for sale associates
            );
        }
        else if (command.containsKey(Proposal.PROPOSAL_KEY)) {
            try {
                // Get the proposal
                Proposal proposal = CommandProcessor.proposalOperations.getProposal(pm, command.getLong(Proposal.PROPOSAL_KEY), null, null);
                // Get the corresponding demand -- No need for this demand, just the verification it exists
                CommandProcessor.demandOperations.getDemand(proposal.getDemandKey(), consumer.getKey());
                // Update the proposal state
                proposal.setState(State.declined);
                proposal = CommandProcessor.proposalOperations.updateProposal(pm, proposal);
                communicateToConsumer(
                        rawCommand.getSource(),
                        consumer,
                        LabelExtractor.get("cp_command_decline_acknowledge_proposal_closing", new Object[] { proposal.getKey() }, consumer.getLocale())
                );
            }
            catch(Exception ex) {
                communicateToConsumer(
                        rawCommand.getSource(),
                        consumer,
                        LabelExtractor.get("cp_command_decline_invalid_proposal_id", consumer.getLocale())
                );
            }
        }
        else {
            communicateToConsumer(
                    rawCommand.getSource(),
                    consumer,
                    LabelExtractor.get("cp_command_decline_invalid_parameters", consumer.getLocale())
            );
        }
    }
}
