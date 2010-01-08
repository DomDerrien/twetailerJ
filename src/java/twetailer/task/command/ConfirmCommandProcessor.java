package twetailer.task.command;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;
import static twetailer.connector.BaseConnector.communicateToConsumer;
import static twetailer.connector.BaseConnector.communicateToSaleAssociate;

import javax.jdo.PersistenceManager;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.connector.BaseConnector.Source;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Proposal;
import twetailer.dto.RawCommand;
import twetailer.dto.SaleAssociate;
import twetailer.task.CommandProcessor;
import twetailer.task.RobotResponder;
import twetailer.validator.ApplicationSettings;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.Prefix;
import twetailer.validator.CommandSettings.State;
import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class ConfirmCommandProcessor {
    public static void processConfirmCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command) throws ClientException, DataSourceException {
        //
        // Used by the consumer to accept a proposal
        // Note that the proposal should refer to a demand owned by the consumer
        //
        // The consumer receives a notification with the store location
        // The sale associate receives a notification with the confirmation and the suggestion to put the product aside for the consumer
        //
        Proposal proposal = null;
        Demand demand = null;
        try {
            // If there's no PROPOSAL_KEY attribute, it's going to generate an exception as the desired side-effect
            proposal = CommandProcessor.proposalOperations.getProposal(pm, command.getLong(Proposal.PROPOSAL_KEY), null, null);
            // If the proposal is not for a demand the consumer owns, it's going to generate an exception as the desired side-effect
            demand = CommandProcessor.demandOperations.getDemand(pm, proposal.getDemandKey(), consumer.getKey());
        }
        catch(Exception ex) {
            communicateToConsumer(
                    rawCommand,
                    consumer,
                    LabelExtractor.get("cp_command_confirm_invalid_proposal_id", consumer.getLocale())
            );
        }
        if (demand != null) {
            if (!State.published.equals(demand.getState())) {
                communicateToConsumer(
                        rawCommand,
                        consumer,
                        LabelExtractor.get("cp_command_confirm_invalid_state_demand", new Object[] { proposal.getKey(), demand.getKey(), demand.getState().toString() }, consumer.getLocale())
                );
            }
            else {
                // Inform the consumer of the successful confirmation
                communicateToConsumer(
                        rawCommand,
                        consumer,
                        LabelExtractor.get(
                                "cp_command_confirm_acknowledge_confirmation",
                                new Object[] {
                                        proposal.getKey(),
                                        demand.getKey(),
                                        demand.getSerializedCriteria(),
                                        proposal.getStoreKey(),
                                        // TODO: Add the lookup to the store table to get the store name
                                        "\\[Not yet implemented\\]" // store.getName()
                                },
                                consumer.getLocale()
                        )
                );
                Long robotKey = RobotResponder.getRobotSaleAssociateKey(pm);
                if (proposal.getOwnerKey().equals(robotKey)) {
                    // Inform the consumer about the next steps in the demo mode
                    communicateToConsumer(
                            rawCommand,
                            consumer,
                            LabelExtractor.get("cp_command_confirm_inform_about_demo_mode", consumer.getLocale())
                    );

                    // Prepare the message simulating the closing by the robot
                    RawCommand consequence = new RawCommand();
                    consequence.setCommand(Prefix.action + ":" + Action.close + " " + Prefix.proposal + ":" + proposal.getKey());
                    consequence.setSource(Source.robot);

                    // Persist message
                    consequence = CommandProcessor.rawCommandOperations.createRawCommand(pm, consequence);

                    // Create a task for that command
                    Queue queue = CommandProcessor._baseOperations.getQueue();
                    queue.add(
                            url(ApplicationSettings.get().getServletApiPath() + "/maezel/processCommand").
                                param(Command.KEY, consequence.getKey().toString()).
                                method(Method.GET)
                    );
                }
                else {
                    // Inform the sale associate of the successful confirmation
                    SaleAssociate saleAssociate = CommandProcessor.saleAssociateOperations.getSaleAssociate(pm, proposal.getOwnerKey());
                    communicateToSaleAssociate(
                            new RawCommand(saleAssociate.getPreferredConnection()),
                            saleAssociate,
                            LabelExtractor.get(
                                    "cp_command_confirm_inform_about_confirmation",
                                    new Object[] {
                                            proposal.getKey(),
                                            proposal.getSerializedCriteria(),
                                            demand.getKey()
                                    },
                                    consumer.getLocale()
                            )
                    );
                }

                // Update the proposal and the demand states
                proposal.setState(State.confirmed);
                proposal = CommandProcessor.proposalOperations.updateProposal(pm, proposal);
                demand.setState(State.confirmed);
                demand = CommandProcessor.demandOperations.updateDemand(pm, demand);
            }
        }
    }
}
