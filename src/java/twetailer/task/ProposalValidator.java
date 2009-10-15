// ATM 23333
package twetailer.task;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;
import static twetailer.connector.BaseConnector.communicateToRetailer;

import java.util.Locale;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dao.BaseOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.ProposalOperations;
import twetailer.dao.RetailerOperations;
import twetailer.dto.Proposal;
import twetailer.dto.Retailer;
import twetailer.validator.CommandSettings;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

import domderrien.i18n.LabelExtractor;

public class ProposalValidator {

    private static final Logger log = Logger.getLogger(ProposalValidator.class.getName());

    protected static BaseOperations _baseOperations = new BaseOperations();
    protected static RetailerOperations retailerOperations = _baseOperations.getRetailerOperations();
    protected static DemandOperations demandOperations = _baseOperations.getDemandOperations();
    protected static ProposalOperations proposalOperations = _baseOperations.getProposalOperations();

    /**
     * Check the validity of the identified proposal
     *
     * @param proposalKey Identifier of the proposal to process
     *
     * @throws DataSourceException If the data manipulation fails
     */
    public static void process(Long proposalKey) throws DataSourceException {
        PersistenceManager pm = _baseOperations.getPersistenceManager();
        try {
            process(pm, proposalKey);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Check the validity of the identified proposal
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param proposalKey Identifier of the proposal to process
     *
     * @throws DataSourceException If the data manipulation fails
     */
    public static void process(PersistenceManager pm, Long proposalKey) throws DataSourceException {
        Proposal proposal = proposalOperations.getProposal(pm, proposalKey, null, null);
        if (CommandSettings.State.open.equals(proposal.getState())) {
            try {
                Retailer retailer = retailerOperations.getRetailer(pm, proposal.getOwnerKey());
                Locale locale = retailer.getLocale();
                String message = null;

                if(proposal.getCriteria() == null || proposal.getCriteria().size() == 0) {
                    message = LabelExtractor.get("pv_proposalShouldHaveAtLeastOneTag", new Object[] { proposal.getKey() }, locale);
                }
                else if (proposal.getQuantity() == null || proposal.getQuantity() == 0L) {
                    message = LabelExtractor.get("pv_proposalShouldConcernAtLeastOneItem", new Object[] { proposal.getKey() }, locale);
                }
                else if ((proposal.getPrice() == null || Double.valueOf(0.0D).equals(proposal.getPrice())) && (proposal.getTotal() == null || Double.valueOf(0.0D).equals(proposal.getTotal()))) {
                    message = LabelExtractor.get("pv_proposalShouldHaveAUnitPriceOrTotalPrice", new Object[] { proposal.getKey() }, locale);
                }
                else {
                    Long demandKey = proposal.getDemandKey();
                    if (demandKey == null || demandKey == 0L) {
                        message = LabelExtractor.get("pv_proposalShouldHaveADemandReference", new Object[] { proposal.getKey() }, locale);
                    }
                    else {
                        try {
                            demandOperations.getDemand(pm, demandKey, null);
                        }
                        catch (DataSourceException ex) {
                            message = LabelExtractor.get("pv_proposalHaveInvalidDemandReference", new Object[] { proposal.getKey(), demandKey }, locale);
                        }
                   }
                }
                if (message != null) {
                    log.warning("Invalid state for the proposal: " + proposal.getKey() + " -- message: " + message);
                    communicateToRetailer(proposal.getSource(), retailer, message);
                    proposal.setState(CommandSettings.State.invalid);
                }
                else {
                    proposal.setState(CommandSettings.State.published);

                    // Create a task for that proposal
                    Queue queue = QueueFactory.getDefaultQueue();
                    queue.add(url("/API/maezel/processPublishedProposal").param(Proposal.KEY, proposalKey.toString()).method(Method.GET));
                }
                proposalOperations.updateProposal(pm, proposal);
            }
            catch (DataSourceException ex) {
                log.warning("Cannot get information for retailer: " + proposal.getOwnerKey() + " -- ex: " + ex.getMessage());
            }
            catch (ClientException ex) {
                log.warning("Cannot communicate with retailer -- ex: " + ex.getMessage());
            }
        }
    }
}
