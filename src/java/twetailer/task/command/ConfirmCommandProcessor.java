package twetailer.task.command;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;
import static twetailer.connector.BaseConnector.communicateToCCed;
import static twetailer.connector.BaseConnector.communicateToConsumer;
import static twetailer.connector.BaseConnector.communicateToSaleAssociate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.connector.BaseConnector.Source;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Proposal;
import twetailer.dto.RawCommand;
import twetailer.dto.SaleAssociate;
import twetailer.dto.Store;
import twetailer.task.CommandLineParser;
import twetailer.task.CommandProcessor;
import twetailer.task.RobotResponder;
import twetailer.validator.ApplicationSettings;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.Prefix;
import twetailer.validator.CommandSettings.State;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.JsonObject;

public class ConfirmCommandProcessor {
    private static Logger log = Logger.getLogger(ConfirmCommandProcessor.class.getName());

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
        List<String> messages = new ArrayList<String>();
        String messageCC = null;
        List<String> cc = null;

        try {
            // If there's no PROPOSAL_KEY attribute, it's going to generate an exception as the desired side-effect
            proposal = CommandProcessor.proposalOperations.getProposal(pm, command.getLong(Proposal.PROPOSAL_KEY), null, null);
            // If the proposal is not for a demand the consumer owns, it's going to generate an exception as the desired side-effect
            demand = CommandProcessor.demandOperations.getDemand(pm, proposal.getDemandKey(), consumer.getKey());
        }
        catch(Exception ex) {
            messages.add(LabelExtractor.get("cp_command_confirm_invalid_proposal_id", consumer.getLocale()));
        }
        if (demand != null) {
            Locale locale = consumer.getLocale();
            String demandRef = LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { demand.getKey() }, locale);
            String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposal.getKey() }, locale);
            if (!State.published.equals(demand.getState())) {
                String stateLabel = CommandLineParser.localizedStates.get(locale).getString(demand.getState().toString());
                stateLabel = LabelExtractor.get("cp_tweet_state_part", new Object[] { stateLabel }, locale);
                messages.add(LabelExtractor.get("cp_command_confirm_invalid_state_demand", new Object[] { proposalRef, demandRef, stateLabel }, locale));
            }
            else {
                Store store = CommandProcessor.storeOperations.getStore(pm, proposal.getStoreKey());
                String demandTags = demand.getCriteria().size() == 0 ? "" : LabelExtractor.get("cp_tweet_tags_part", new Object[] { demand.getSerializedCriteria() }, locale);
                String pickup = LabelExtractor.get("cp_tweet_store_part", new Object[] { store.getKey(), store.getName() }, locale);
                // Inform the consumer of the successful confirmation
                messages.add(LabelExtractor.get("cp_command_confirm_acknowledge_confirmation", new Object[] { proposalRef, demandRef, demandTags, pickup }, locale));
                // Prepare the message for the CC-ed
                if (0 < demand.getCC().size()) {
                    messageCC = LabelExtractor.get(
                            "cp_command_confirm_forward_confirmation_to_cc",
                            new Object[] { consumer.getName(), proposalRef, demandRef, demandTags, pickup },
                            locale
                    );
                    cc = demand.getCC();
                }
                Long robotKey = RobotResponder.getRobotSaleAssociateKey(pm);
                if (proposal.getOwnerKey().equals(robotKey)) {
                    // Inform the consumer about the next steps in the demo mode
                    messages.add(LabelExtractor.get("cp_command_confirm_inform_about_demo_mode", consumer.getLocale()));

                    // Prepare the message simulating the closing by the robot
                    RawCommand consequence = new RawCommand();
                    consequence.setCommand(Prefix.action + ":" + Action.close + " " + Prefix.proposal + ":" + proposal.getKey());
                    consequence.setSource(Source.robot);

                    // Persist message
                    consequence = CommandProcessor.rawCommandOperations.createRawCommand(pm, consequence);

                    // Create a task for that command
                    Queue queue = CommandProcessor._baseOperations.getQueue();
                    log.warning("Preparing the task: /maezel/processCommand?key=" + consequence.getKey().toString());
                    queue.add(
                            url(ApplicationSettings.get().getServletApiPath() + "/maezel/processCommand").
                                param(Command.KEY, consequence.getKey().toString()).
                                method(Method.GET)
                    );
                }
                else {
                    // Inform the sale associate of the successful confirmation
                    SaleAssociate saleAssociate = CommandProcessor.saleAssociateOperations.getSaleAssociate(pm, proposal.getOwnerKey());
                    locale = saleAssociate.getLocale();
                    demandRef = LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { demand.getKey() }, locale);
                    proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposal.getKey() }, locale);
                    String tags = LabelExtractor.get("cp_tweet_tags_part", new Object[] { proposal.getSerializedCriteria() }, locale);
                    communicateToSaleAssociate(
                            new RawCommand(saleAssociate.getPreferredConnection()),
                            saleAssociate,
                            new String[] { LabelExtractor.get("cp_command_confirm_inform_about_confirmation", new Object[] { proposalRef, tags, demandRef }, locale)}
                    );
                }

                // Update the proposal and the demand states
                proposal.setState(State.confirmed);
                proposal = CommandProcessor.proposalOperations.updateProposal(pm, proposal);
                demand.setState(State.confirmed);
                demand = CommandProcessor.demandOperations.updateDemand(pm, demand);
            }
        }

        // Inform the demand owner
        communicateToConsumer(
                rawCommand,
                consumer,
                messages.toArray(new String[0])
        );

        // Inform the cc-ed people
        if (cc != null) {
            for (String coordinate: cc) {
                communicateToCCed(coordinate, messageCC, consumer.getLocale());
            }
        }
    }
}
