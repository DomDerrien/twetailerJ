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
import twetailer.dto.Store;
import twetailer.dto.Command.QueryPointOfView;
import twetailer.task.CommandLineParser;
import twetailer.task.step.BaseSteps;
import twetailer.task.step.ProposalSteps;
import twetailer.validator.CommandSettings.State;
import domderrien.i18n.LabelExtractor;
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
                String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { entityKey }, locale);
                String demandRef = LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { proposal.getDemandKey() }, locale);
                Demand demand = BaseSteps.getDemandOperations().getDemand(pm, proposal.getDemandKey(), consumer.getKey());
                Store store = BaseSteps.getStoreOperations().getStore(pm, proposal.getStoreKey());
                String demandTags = demand.getCriteria().size() == 0 ? "" : LabelExtractor.get("cp_tweet_tags_part", new Object[] { demand.getSerializedCriteria() }, locale);
                String pickup = LabelExtractor.get("cp_tweet_store_part", new Object[] { store.getKey(), store.getName() }, locale);
                message = LabelExtractor.get("cp_command_confirm_acknowledge_confirmation", new Object[] { proposalRef, demandRef, demandTags, pickup }, locale);
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

        /********** ddd
        if (locale != null) { return; }

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
            proposal = BaseSteps.getProposalOperations().getProposal(pm, command.getLong(Proposal.PROPOSAL_KEY), null, null);
            // If the proposal is not for a demand the consumer owns, it's going to generate an exception as the desired side-effect
            demand = BaseSteps.getDemandOperations().getDemand(pm, proposal.getDemandKey(), consumer.getKey());
        }
        catch(Exception ex) {
            messages.add(LabelExtractor.get("cp_command_confirm_invalid_proposal_id", consumer.getLocale()));
        }
        if (demand != null) {
            String demandRef = LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { demand.getKey() }, locale);
            String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposal.getKey() }, locale);
            if (!State.published.equals(demand.getState())) {
                String stateLabel = CommandLineParser.localizedStates.get(locale).getString(demand.getState().toString());
                stateLabel = LabelExtractor.get("cp_tweet_state_part", new Object[] { stateLabel }, locale);
                messages.add(LabelExtractor.get("cp_command_confirm_invalid_state_demand", new Object[] { proposalRef, demandRef, stateLabel }, locale));
            }
            else {
                Store store = BaseSteps.getStoreOperations().getStore(pm, proposal.getStoreKey());
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
                    prepareClosingResponseByTheRobot(pm, proposal.getKey());
                }
                else {
                    // Inform the sale associate of the successful confirmation
                    SaleAssociate saleAssociate = BaseSteps.getSaleAssociateOperations().getSaleAssociate(pm, proposal.getOwnerKey());
                    Consumer saConsumerRecord = BaseSteps.getConsumerOperations().getConsumer(pm, saleAssociate.getConsumerKey());
                    locale = saleAssociate.getLocale();
                    String tags = LabelExtractor.get("cp_tweet_tags_part", new Object[] { proposal.getSerializedCriteria() }, locale);
                    communicateToConsumer(
                            new RawCommand(saConsumerRecord.getPreferredConnection()),
                            saConsumerRecord,
                            new String[] { LabelExtractor.get("cp_command_confirm_inform_about_confirmation", new Object[] { proposalRef, tags, demandRef }, locale)}
                    );
                }

                // Update the proposal and the demand states
                proposal.setState(State.confirmed);
                proposal = BaseSteps.getProposalOperations().updateProposal(pm, proposal);
                demand.setState(State.confirmed);
                demand = BaseSteps.getDemandOperations().updateDemand(pm, demand);
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

    protected static void prepareClosingResponseByTheRobot(PersistenceManager pm, Long proposalKey) {
        // Simulated interaction
        RawCommand consequence = new RawCommand();
        consequence.setCommand(Prefix.action + CommandLineParser.PREFIX_SEPARATOR + Action.close + " " + Prefix.proposal + CommandLineParser.PREFIX_SEPARATOR + proposalKey);
        consequence.setSource(Source.robot);

        // Persist message
        consequence = BaseSteps.getRawCommandOperations().createRawCommand(pm, consequence);

        // Create a task for that command
        MaezelServlet.triggerCommandProcessorTask(consequence.getKey());
        ddd ***********/
    }
}
