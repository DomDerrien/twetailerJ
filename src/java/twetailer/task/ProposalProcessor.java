package twetailer.task;

import static twetailer.connector.BaseConnector.communicateToConsumer;

import java.util.Locale;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.connector.BaseConnector.Source;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Proposal;
import twetailer.dto.RawCommand;
import twetailer.dto.SaleAssociate;
import twetailer.dto.Store;
import twetailer.task.step.BaseSteps;
import twetailer.validator.CommandSettings;
import twetailer.validator.CommandSettings.State;
import domderrien.i18n.LabelExtractor;

/**
 * Define the task with is invoked by the task "/maezel/validateOpenProposal"
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
                Store store = BaseSteps.getStoreOperations().getStore(pm, proposal.getStoreKey());
                if (State.published.equals(demand.getState())) {

                    // Update the demand
                    demand.addProposalKey(proposalKey);
                    demand = BaseSteps.getDemandOperations().updateDemand(pm, demand);

                    // Prepare the notification only if worth it
                    if (!Source.api.equals(demand.getSource()) || 0 < demand.getCC().size()) {
                        Consumer consumer = BaseSteps.getConsumerOperations().getConsumer(pm, demand.getOwnerKey());
                        BaseSteps.notifyAvailability(pm, proposal, demand, consumer);
                    }
                }
                else {
                    SaleAssociate saleAssociate = BaseSteps.getSaleAssociateOperations().getSaleAssociate(pm, proposal.getOwnerKey());
                    Consumer saConsumerRecord = BaseSteps.getConsumerOperations().getConsumer(pm, saleAssociate.getConsumerKey());
                    Locale locale = saConsumerRecord.getLocale();
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
                    RawCommand rawCommand = BaseSteps.getRawCommandOperations().getRawCommand(pm, proposal.getRawCommandId());
                    communicateToConsumer(rawCommand, saConsumerRecord, new String[] { message });
                }
            }
            catch (InvalidIdentifierException ex) {
                log.warning("Cannot get information retaled to proposal: " + proposal.getKey() + " -- ex: " + ex.getMessage());
            }
            catch (ClientException ex) {
                log.warning("Cannot communicate with sale associate -- ex: " + ex.getMessage());
            }
        }
    }
}
