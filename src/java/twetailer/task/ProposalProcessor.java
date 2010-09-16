package twetailer.task;

import static twetailer.connector.BaseConnector.communicateToCCed;
import static twetailer.connector.BaseConnector.communicateToConsumer;
import static twetailer.connector.BaseConnector.getCCedCommunicationChannel;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.CommunicationException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.connector.BaseConnector;
import twetailer.connector.MailConnector;
import twetailer.connector.MessageGenerator;
import twetailer.connector.BaseConnector.Source;
import twetailer.connector.MessageGenerator.MessageId;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.Proposal;
import twetailer.dto.RawCommand;
import twetailer.dto.SaleAssociate;
import twetailer.dto.Store;
import twetailer.task.step.BaseSteps;
import twetailer.validator.CommandSettings;
import twetailer.validator.CommandSettings.State;
import domderrien.i18n.LabelExtractor;
import domderrien.i18n.LabelExtractor.ResourceFileId;

/**
 * Define the task with is invoked by the task "/maelzel/validateOpenProposal"
 * in order to broadcast the valid Proposal to the corresponding Demand
 * owner.
 *
 * When everything is OK, a message is sent to the Consumer with the same
 * medium she used to create the Demand (can be no message with created
 * from a console (Source==api)).
 *
 * @see twetailer.task.ProposalValidator
 *
 * @author Dom Derrien
 */
public class ProposalProcessor {

    private static Logger log = Logger.getLogger(ProposalProcessor.class.getName());

    // Setter for injection of a MockLogger at test time
    protected static void setLogger(Logger mock) {
        log = mock;
    }

    /**
     * Forward the identified proposal to listening sale associates
     *
     * @param proposalKey Identifier of the proposal to process
     *
     * @throws DataSourceException If the data manipulation fails
     * @throws InvalidIdentifierException If the retrieval of the identified resources fails
     */
    public static void process(Long proposalKey) throws DataSourceException, InvalidIdentifierException {
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            process(pm, proposalKey);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Forward the identified proposal to listening sale associates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param proposalKey Identifier of the proposal to process
     *
     * @throws DataSourceException If the data manipulation fails
     * @throws InvalidIdentifierException If the retrieval of the identified resources fails
     */
    public static void process(PersistenceManager pm, Long proposalKey) throws DataSourceException, InvalidIdentifierException {
        Proposal proposal = BaseSteps.getProposalOperations().getProposal(pm, proposalKey, null, null);
        if (State.published.equals(proposal.getState())) {
            try {
                Demand demand = BaseSteps.getDemandOperations().getDemand(pm, proposal.getDemandKey(), null);
                if (State.published.equals(demand.getState())) {

                    // Update the demand
                    demand.addProposalKey(proposalKey);
                    demand = BaseSteps.getDemandOperations().updateDemand(pm, demand);

                    // Prepare the notification only if worth it
                    if (!Source.api.equals(demand.getSource()) || 0 < demand.getCC().size()) {
                        Store store = BaseSteps.getStoreOperations().getStore(pm, proposal.getStoreKey());
                        Location location = BaseSteps.getLocationOperations().getLocation(pm, store.getLocationKey());
                        RawCommand rawCommand = Source.mail.equals(demand.getSource()) ? BaseSteps.getRawCommandOperations().getRawCommand(pm, demand.getRawCommandId()) : null;
                        Consumer consumer = BaseSteps.getConsumerOperations().getConsumer(pm, demand.getOwnerKey());
                        notifyAvailability(proposal, store, location, demand, rawCommand, consumer);
                    }
                }
                else {
                    SaleAssociate saleAssociate = BaseSteps.getSaleAssociateOperations().getSaleAssociate(pm, proposal.getOwnerKey());
                    Consumer saConsumerRecord = BaseSteps.getConsumerOperations().getConsumer(pm, saleAssociate.getConsumerKey());
                    Locale locale = saConsumerRecord.getLocale();
                    Source source = proposal.getSource();
                    if (Source.api.equals(source) || Source.widget.equals(source)) {
                        source = saConsumerRecord.getPreferredConnection();
                    }

                    String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposal.getKey() }, locale);
                    String demandRef = LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { demand.getKey() }, locale);
                    String stateLabel = CommandSettings.getStates(locale).getString(demand.getState().toString());
                    stateLabel = LabelExtractor.get("cp_tweet_state_part", new Object[] { stateLabel }, locale);
                    String message = LabelExtractor.get(
                            "pp_inform_saleAssociate_demand_not_published_state",
                            new Object[] {
                                    proposalRef,
                                    demandRef,
                                    stateLabel
                            },
                            locale
                    );
                    String subject = null;
                    if (Source.mail.equals(proposal.getSource())) {
                        RawCommand rawCommand = BaseSteps.getRawCommandOperations().getRawCommand(pm, proposal.getRawCommandId());
                        subject = rawCommand.getSubject();
                    }
                    if (subject == null ){
                        subject = MessageGenerator.getMessage(
                                source,
                                proposal.getHashTags(),
                                MessageId.messageSubject,
                                new String[] { demand.getKey().toString() },
                                locale
                        );
                    }
                    communicateToConsumer(
                            source,
                            MailConnector.prepareSubjectAsResponse(subject, locale),
                            saConsumerRecord,
                            new String[] { message }
                    );
                }
            }
            catch (InvalidIdentifierException ex) {
                log.warning("Cannot get information related to proposal: " + proposal.getKey() + " -- ex: " + ex.getMessage());
            }
            catch (ClientException ex) {
                log.warning("Cannot communicate with sale associate -- ex: " + ex.getMessage());
            }
        }
    }

    /**
     * Send a message to the identified consumer for him to review the proposal.
     * CC'ed users will be notified too.
     *
     * @param proposal New or updated proposal to be presented to the consumer
     * @param store Record of the store which produced the proposal
     * @param location Place where the store is located
     * @param demand Original demand for this proposal
     * @param rawCommand To be able to get the initial mail subject if it has been sent by e-mail
     * @param consumer Record of the demand owner
     *
     * @throws CommunicationException If the communication with the demand owner fails
     */
    public static void notifyAvailability(Proposal proposal, Store store, Location location, Demand demand, RawCommand rawCommand, Consumer consumer) throws DataSourceException, InvalidIdentifierException, CommunicationException {

        List<String> cc = demand.getCC();
        if (!Source.api.equals(demand.getSource()) || cc != null && 0 < cc.size()) {
            Locale locale = consumer.getLocale();

            // Send a message to the demand Owner
            if (!Source.api.equals(demand.getSource())) {
                MessageGenerator msgGen = new MessageGenerator(demand.getSource(), demand.getHashTags(), locale);
                msgGen.
                    put("demand>owner>name", consumer.getName()).
                    fetch(proposal).
                    fetch(store).
                    fetch(location, "store").
                    fetch(demand).
                    put("message>footer", msgGen.getAlternateMessage(MessageId.messageFooter));

                Map<String, Object> cmdPrm = new HashMap<String, Object>();
                cmdPrm.put("proposal>key", proposal.getKey());
                cmdPrm.put("demand>key", demand.getKey());
                cmdPrm.put("command>footer", LabelExtractor.get(ResourceFileId.fourth, "command_message_footer", locale));
                String confirmProposal = LabelExtractor.get(ResourceFileId.fourth, "command_message_body_proposal_confirm", cmdPrm, locale);
                String declineProposal = LabelExtractor.get(ResourceFileId.fourth, "command_message_body_proposal_decline", cmdPrm, locale);
                String cancelDemand = LabelExtractor.get(ResourceFileId.fourth, "command_message_body_demand_cancel", cmdPrm, locale);

                String subject = null;
                if (Source.mail.equals(demand.getSource()) && rawCommand.getSubject() != null) {
                    subject = rawCommand.getSubject();
                }
                else {
                    subject = msgGen.getAlternateMessage(MessageId.messageSubject, cmdPrm);
                }
                subject = MailConnector.prepareSubjectAsResponse(subject, locale);

                msgGen.
                    put("control>threadSubject", subject.replaceAll(" ", "%20")).
                    put("control>confirmProposal", confirmProposal.replaceAll(" ", "%20").replaceAll(BaseConnector.ESCAPED_SUGGESTED_MESSAGE_SEPARATOR_STR, "%0A")).
                    put("control>declineProposal", declineProposal.replaceAll(" ", "%20").replaceAll(BaseConnector.ESCAPED_SUGGESTED_MESSAGE_SEPARATOR_STR, "%0A")).
                    put("control>cancelDemand", cancelDemand.replaceAll(" ", "%20").replaceAll(BaseConnector.ESCAPED_SUGGESTED_MESSAGE_SEPARATOR_STR, "%0A"));

                String message = msgGen.getMessage(MessageId.proposalCreationNot);

                communicateToConsumer(
                        consumer.getPreferredConnection(),
                        subject,
                        consumer,
                        new String[] { message }
                );
            }

            // Send the proposal details to the CC'ed users
            if (cc != null && 0 < cc.size()) {
                MessageGenerator msgGen = null;
                String message = null;
                String subject = null;
                for (String coordinate: cc) {
                    try {
                        Source source = getCCedCommunicationChannel(coordinate);

                        if (msgGen == null || !source.equals(msgGen.getCommunicationChannel())) {
                            //
                            // TODO: cache the MessageGenerator instance per Source value to avoid unnecessary re-creation!
                            //
                            msgGen = new MessageGenerator(source, demand.getHashTags(), locale);
                            msgGen.
                                put("demand>owner>name", consumer.getName()).
                                fetch(proposal).
                                fetch(store).
                                fetch(location, "store").
                                fetch(demand).
                                put("message>footer", msgGen.getAlternateMessage(MessageId.messageFooter));

                            message = msgGen.getMessage(MessageId.proposalCreationCpy);
                        }

                        if (subject == null) {
                            Map<String, Object> cmdPrm = new HashMap<String, Object>();
                            cmdPrm.put("demand>key", demand.getKey());
                            subject = msgGen.getAlternateMessage(MessageId.messageSubject, cmdPrm).replaceAll(" ", "%20");
                            subject = MailConnector.prepareSubjectAsForward(subject, locale);
                        }

                        communicateToCCed(
                                source,
                                coordinate,
                                subject,
                                message,
                                locale
                        );
                    }
                    catch (ClientException e) { } // Too bad, cannot contact the CC-ed person... Don't block the next sending!
                }
            }
        }
    }
}
