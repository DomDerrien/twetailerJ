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
import domderrien.jsontools.JsonObject;

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
        if (State.published.equals(proposal.getState())) {
            try {
                Demand demand = demandOperations.getDemand(pm, proposal.getDemandKey(), null);
                if (State.published.equals(demand.getState())) {
                    Consumer consumer = consumerOperations.getConsumer(pm, demand.getOwnerKey());
                    Double totalCost = proposal.getTotal();
                    Double price = proposal.getPrice();
                    String message = null;
                    if (totalCost == null || totalCost.doubleValue() == 0.0D) {
                        message = LabelExtractor.get(
                                "pp_inform_consumer_about_proposal_with_price_only",
                                new Object[] {
                                        proposal.getKey(),                // {0}: proposal key
                                        proposal.getSerializedCriteria(), // {1}: proposal product
                                        demand.getKey(),                  // {2}: demand key
                                        demand.getSerializedCriteria(),   // {3}: demand tags
                                        demand.getExpirationDate(),       // {4}: demand expiration date
                                        proposal.getStoreKey(),           // {5}: store key, identifying the place where the retailer works.
                                        "[Not yet implemented]",          // {6}: store name 
                                        "$",                              // {7}: currency symbol
                                        price                             // {8}: price
                                },
                                consumer.getLocale()
                        );
                    }
                    else if (price == null || price.doubleValue() == 0.0D) {
                        message = LabelExtractor.get(
                                "pp_inform_consumer_about_proposal_with_total_cost_only",
                                new Object[] {
                                        proposal.getKey(),                // {0}: proposal key
                                        proposal.getSerializedCriteria(), // {1}: proposal product
                                        demand.getKey(),                  // {2}: demand key
                                        demand.getSerializedCriteria(),   // {3}: demand tags
                                        demand.getExpirationDate(),       // {4}: demand expiration date
                                        proposal.getStoreKey(),           // {5}: store key, identifying the place where the retailer works.
                                        "[Not yet implemented]",          // {6}: store name 
                                        "$",                              // {7}: currency symbol
                                        totalCost                         // {8}: total
                                },
                                consumer.getLocale()
                        );
                    }
                    else {
                        message = LabelExtractor.get(
                                "pp_inform_consumer_about_proposal_with_price_and_total_cost",
                                new Object[] {
                                        proposal.getKey(),                // {0}: proposal key
                                        proposal.getSerializedCriteria(), // {1}: proposal product
                                        demand.getKey(),                  // {2}: demand key
                                        demand.getSerializedCriteria(),   // {3}: demand tags
                                        demand.getExpirationDate(),       // {4}: demand expiration date
                                        proposal.getStoreKey(),           // {5}: store key, identifying the place where the retailer works.
                                        "[Not yet implemented]",          // {6}: store name 
                                        "$",                              // {7}: currency symbol
                                        price,                            // {8}: price
                                        totalCost                         // {9}: total
                                },
                                consumer.getLocale()
                        );
                    }
                    communicateToConsumer(
                            demand.getSource(),
                            consumer,
                            message
                    );
                    demand.addProposalKey(proposalKey);
                    demand = demandOperations.updateDemand(pm, demand);
                }
                else {
                    Retailer retailer = retailerOperations.getRetailer(pm, proposal.getOwnerKey());
                    JsonObject states = CommandSettings.getStates(retailer.getLocale());
                    String message = LabelExtractor.get(
                            "pp_inform_retailer_demand_not_published_state",
                            new Object[] {
                                    proposal.getKey(),
                                    demand.getKey(),
                                    states.getString(demand.getState().toString())
                            },
                            retailer.getLocale()
                    );
                    communicateToRetailer(retailer.getPreferredConnection(), retailer, message);
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
