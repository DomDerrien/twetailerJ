package twetailer.task;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;
import static twetailer.connector.BaseConnector.communicateToConsumer;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.connector.BaseConnector.Source;
import twetailer.dto.Consumer;
import twetailer.dto.HashTag;
import twetailer.dto.Proposal;
import twetailer.dto.RawCommand;
import twetailer.dto.SaleAssociate;
import twetailer.task.step.BaseSteps;
import twetailer.validator.ApplicationSettings;
import twetailer.validator.CommandSettings;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

import domderrien.i18n.DateUtils;
import domderrien.i18n.LabelExtractor;

/**
 * Define the task with is invoked by methods in ProposalSteps
 * every time a Proposal is updated significantly. If the Proposal
 * instance is valid, the task "/maezel/processPublishedProposal"
 * is scheduled to broadcast it to the corresponding Demand owner.
 *
 * @see twetailer.dto.Proposal
 * @see twetailer.task.step.ProposalSteps
 * @see twetailer.task.ProposalProcessor
 *
 * @author Dom Derrien
 */
public class ProposalValidator {

    private static Logger log = Logger.getLogger(ProposalValidator.class.getName());

    // Setter for injection of a MockLogger at test time
    protected static void setLogger(Logger mock) {
        log = mock;
    }

    /**
     * Check the validity of the identified proposal
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

    private static final Long oneYear = 365 * 24 * 60 * 60 * 1000L;

    /**
     * Check the validity of the identified proposal
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param proposalKey Identifier of the proposal to process
     *
     * @throws DataSourceException If the data manipulation fails
     * @throws InvalidIdentifierException If the retrieval of the identified resources fails
     */
    public static void process(PersistenceManager pm, Long proposalKey) throws DataSourceException, InvalidIdentifierException {
        Proposal proposal = BaseSteps.getProposalOperations().getProposal(pm, proposalKey, null, null);
        if (CommandSettings.State.opened.equals(proposal.getState())) {
            Date nowDate = DateUtils.getNowDate();
            Long nowTime = nowDate.getTime() - 60*1000; // Minus 1 minute
            try {
                SaleAssociate saleAssociate = BaseSteps.getSaleAssociateOperations().getSaleAssociate(pm, proposal.getOwnerKey());
                Consumer saConsumerRecord = BaseSteps.getConsumerOperations().getConsumer(pm, saleAssociate.getConsumerKey());
                Locale locale = saConsumerRecord.getLocale();
                String message = null;

                // Temporary filter
                filterHashTags(pm, saConsumerRecord, proposal);

                String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposal.getKey() }, locale);
                if (proposal.getCriteria() == null || proposal.getCriteria().size() == 0) {
                    message = LabelExtractor.get("pv_report_proposal_without_tag", new Object[] { proposalRef }, locale);
                }
                else if (proposal.getDueDate() == null || proposal.getDueDate().getTime() < nowTime) {
                    message = LabelExtractor.get("dv_report_due_in_past", new Object[] { proposalRef }, locale);
                }
                else if (nowTime + oneYear < proposal.getDueDate().getTime()) {
                    message = LabelExtractor.get("dv_report_due_too_far_in_future", new Object[] { proposalRef }, locale);
                }
                else if (proposal.getQuantity() == null || proposal.getQuantity() == 0L) {
                    message = LabelExtractor.get("pv_report_quantity_zero", new Object[] { proposalRef }, locale);
                }
                else if ((proposal.getPrice() == null || Double.valueOf(0.0D).equals(proposal.getPrice())) && (proposal.getTotal() == null || Double.valueOf(0.0D).equals(proposal.getTotal()))) {
                    message = LabelExtractor.get("pv_report_missing_price_and_total", new Object[] { proposalRef }, locale);
                }
                else {
                    Long demandKey = proposal.getDemandKey();
                    if (demandKey == null || demandKey == 0L) {
                        message = LabelExtractor.get("pv_report_missing_demand_reference", new Object[] { proposalRef }, locale);
                    }
                    else {
                        try {
                            BaseSteps.getDemandOperations().getDemand(pm, demandKey, null);
                        }
                        catch (InvalidIdentifierException ex) {
                            String demandRef = LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { demandKey }, locale);
                            message = LabelExtractor.get("pv_report_invalid_demand_reference", new Object[] { proposalRef, demandRef }, locale);
                        }
                   }
                }
                if (!Source.api.equals(proposal.getSource()) && message != null) {
                    log.warning("Invalid state for the proposal: " + proposal.getKey() + " -- message: " + message);
                    communicateToConsumer(
                            new RawCommand(proposal.getSource()),
                            saConsumerRecord,
                            new String[] { message }
                    );
                    proposal.setState(CommandSettings.State.invalid);
                }
                else {
                    proposal.setState(CommandSettings.State.published);

                    // Create a task for that proposal
                    Queue queue = BaseSteps.getBaseOperations().getQueue();
                    log.warning("Preparing the task: /maezel/processPublishedProposal?key=" + proposalKey.toString());
                    queue.add(
                            url(ApplicationSettings.get().getServletApiPath() + "/maezel/processPublishedProposal").
                                param(Proposal.KEY, proposalKey.toString()).
                                method(Method.GET)
                    );
                }
                proposal = BaseSteps.getProposalOperations().updateProposal(pm, proposal);
            }
            catch (DataSourceException ex) {
                log.warning("Cannot get information for sale associate: " + proposal.getOwnerKey() + " -- ex: " + ex.getMessage());
            }
            catch (ClientException ex) {
                log.warning("Cannot communicate with sale associate -- ex: " + ex.getMessage());
            }
        }
    }

    // Temporary method filtering out non #demo tags
    protected static void filterHashTags(PersistenceManager pm, Consumer saConsumerRecord, Proposal proposal) throws ClientException, DataSourceException {
        if (proposal.getHashTags() != null) {
            List<String> hashTags = proposal.getHashTags();
            if (hashTags.size() != 0) {
                String serializedHashTags = "";
                String hashTag = hashTags.get(0);
                if (hashTags.size() == 1 && !HashTag.isSupportedHashTag(hashTag)) {
                    serializedHashTags = hashTag;
                }
                else { // if (1 < hashTags.size()) {
                    for(int i = 0; i < hashTags.size(); ++i) {
                        hashTag = hashTags.get(i);
                        if (!HashTag.isSupportedHashTag(hashTag)) {
                            serializedHashTags += " " + hashTag;
                        }
                    }
                }
                if (0 < serializedHashTags.length()) {
                    Locale locale = saConsumerRecord.getLocale();
                    String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposal.getKey() }, locale);
                    String tags = LabelExtractor.get("cp_tweet_tags_part", new Object[] { serializedHashTags.trim() }, locale);
                    communicateToConsumer(
                            new RawCommand(proposal.getSource()),
                            saConsumerRecord,
                            new String[] { LabelExtractor.get("pv_report_hashtag_warning", new Object[] { proposalRef, tags }, locale) }
                    );
                    for (String tag: serializedHashTags.split(" ")) {
                        proposal.removeHashTag(tag);
                    }
                }
            }
        }
    }
}
