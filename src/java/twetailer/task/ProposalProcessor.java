package twetailer.task;

import static twetailer.connector.BaseConnector.communicateToConsumer;
import static twetailer.connector.BaseConnector.communicateToRetailer;

import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.ProposalOperations;
import twetailer.dao.RetailerOperations;
import twetailer.dao.StoreOperations;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Proposal;
import twetailer.dto.Retailer;
import twetailer.validator.CommandSettings;
import twetailer.validator.CommandSettings.State;
import domderrien.i18n.LabelExtractor;

public class ProposalProcessor {

    private static final Logger log = Logger.getLogger(ProposalProcessor.class.getName());

    protected static BaseOperations _baseOperations = new BaseOperations();
    protected static ConsumerOperations consumerOperations = _baseOperations.getConsumerOperations();
    protected static DemandOperations demandOperations = _baseOperations.getDemandOperations();
    protected static LocationOperations locationOperations = _baseOperations.getLocationOperations();
    protected static ProposalOperations proposalOperations = _baseOperations.getProposalOperations();
    protected static RetailerOperations retailerOperations = _baseOperations.getRetailerOperations();
    protected static StoreOperations storeOperations = _baseOperations.getStoreOperations();

    /**
     * Forward the identified proposal to listening retailers
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
     * Forward the identified proposal to listening retailers
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param proposalKey Identifier of the proposal to process
     *
     * @throws DataSourceException If the data manipulation fails
     */
    public static void process(PersistenceManager pm, Long proposalKey) throws DataSourceException {
        Proposal proposal = proposalOperations.getProposal(pm, proposalKey, null, null);
        if (CommandSettings.State.published.equals(proposal.getState())) {
            try {
                Demand demand = demandOperations.getDemand(pm, proposal.getDemandKey(), null);
                if (State.published.equals(demand.getState())) {
                    Consumer consumer = consumerOperations.getConsumer(pm, demand.getOwnerKey());
                    StringBuilder tags = new StringBuilder();
                    for(String tag: proposal.getCriteria()) {
                        tags.append(tag).append(" ");
                    }
                    communicateToConsumer(
                            demand.getSource(),
                            consumer,
                            LabelExtractor.get(
                                    "pp_informNewProposal",
                                    new Object[] {
                                            proposal.getKey(),
                                            demand.getKey(),
                                            tags,
                                            proposal.getStoreKey()
                                    },
                                    consumer.getLocale()
                            )
                    );
                    demand.addProposalKey(proposalKey);
                    demandOperations.updateDemand(pm, demand);
                }
                else {
                    Retailer retailer = retailerOperations.getRetailer(pm, proposal.getOwnerKey());
                    communicateToRetailer(
                            retailer.getPreferredConnection(),
                            retailer,
                            LabelExtractor.get(
                                    "pp_informDemandNotPublished",
                                    new Object[] {
                                            proposal.getKey(),
                                            demand.getKey(),
                                            CommandSettings.getStates(retailer.getLocale()).getString(demand.getState().toString()),
                                            proposal.getStoreKey()
                                    },
                                    retailer.getLocale()
                            )
                    );
                }
            }
            catch (DataSourceException ex) {
                log.warning("Cannot get information retaled to proposal: " + proposal.getKey() + " -- ex: " + ex.getMessage());
            }
            catch (ClientException ex) {
                log.warning("Cannot communicate with retailer -- ex: " + ex.getMessage());
            }
        }
    }
}
