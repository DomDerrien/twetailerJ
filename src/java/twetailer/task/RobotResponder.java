package twetailer.task;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;

import java.util.List;
import java.util.Locale;

import javax.jdo.PersistenceManager;

import twetailer.DataSourceException;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.ProposalOperations;
import twetailer.dao.RetailerOperations;
import twetailer.dao.StoreOperations;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.Proposal;
import twetailer.dto.Retailer;
import twetailer.dto.Store;
import twetailer.validator.CommandSettings;
import twetailer.validator.CommandSettings.State;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

import domderrien.i18n.LabelExtractor;

public class RobotResponder {

    protected static BaseOperations _baseOperations = new BaseOperations();
    protected static ConsumerOperations consumerOperations = _baseOperations.getConsumerOperations();
    protected static DemandOperations demandOperations = _baseOperations.getDemandOperations();
    protected static LocationOperations locationOperations = _baseOperations.getLocationOperations();
    protected static ProposalOperations proposalOperations = _baseOperations.getProposalOperations();
    protected static RetailerOperations retailerOperations = _baseOperations.getRetailerOperations();
    protected static StoreOperations storeOperations = _baseOperations.getStoreOperations();

    public static final String ROBOT_NAME = "Jack the Troll";
    public static final String ROBOT_POSTAL_CODE = "H0H0H0";
    public static final String ROBOT_COUNTRY_CODE = Locale.CANADA.getCountry();

    public static void processDemand(Long demandKey) throws DataSourceException {
        PersistenceManager pm = _baseOperations.getPersistenceManager();
        try {
            processDemand(pm, demandKey);
        }
        finally {
            pm.close();
        }
    }

    public static void processDemand(PersistenceManager pm, Long demandKey) throws DataSourceException {
        List<Retailer> retailers = retailerOperations.getRetailers(pm, Retailer.NAME, ROBOT_NAME, 1);
        if (0 < retailers.size()) {
            List<Location> locations = locationOperations.getLocations(pm, ROBOT_POSTAL_CODE, ROBOT_COUNTRY_CODE);
            List<Store> stores = storeOperations.getStores(pm, Store.LOCATION_KEY, locations.get(0).getKey(), 1);
            Demand demand = demandOperations.getDemand(pm, demandKey, null);
            Consumer consumer = consumerOperations.getConsumer(pm, demand.getOwnerKey());
            if (CommandSettings.State.published.equals(demand.getState())) {
                // Create a new and valid proposal
                Proposal proposal = new Proposal();
                proposal.setDemandKey(demandKey);
                proposal.setOwnerKey(retailers.get(0).getKey());
                proposal.setPrice(0.01D);
                proposal.setQuantity(100L);
                proposal.setSource(Source.simulated);
                proposal.setState(State.published);
                proposal.setStoreKey(stores.get(0).getKey());
                proposal.setTotal(1.15D);
                // Prepare the automated response
                String message = LabelExtractor.get("rr_automated_response", consumer.getLocale());
                String[] parts = message.split(" ");
                for (String part: parts) {
                    proposal.addCriterion(part);
                }
                // Persist the newly created proposal
                proposal = proposalOperations.createProposal(pm, proposal);
                // Schedule a task to transmit the proposal to the demand owner
                Queue queue = QueueFactory.getDefaultQueue();
                queue.add(url("/API/maezel/processPublishedProposal").param(Proposal.KEY, proposal.getKey().toString()).method(Method.GET));
            }
        }
    }
}
