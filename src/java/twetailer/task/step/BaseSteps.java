package twetailer.task.step;

import static twetailer.connector.BaseConnector.communicateToCCed;

import java.util.List;
import java.util.Locale;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.PaymentOperations;
import twetailer.dao.ProposalOperations;
import twetailer.dao.RawCommandOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dao.SeedOperations;
import twetailer.dao.SettingsOperations;
import twetailer.dao.StoreOperations;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.Store;
import twetailer.task.CommandProcessor;
import domderrien.i18n.LabelExtractor;

public class BaseSteps {

    private static BaseOperations baseOperations;
    private static ConsumerOperations consumerOperations;
    private static DemandOperations demandOperations;
    private static LocationOperations locationOperations;
    private static PaymentOperations paymentOperations;
    private static ProposalOperations proposalOperations;
    private static RawCommandOperations rawCommandOperations;
    private static SaleAssociateOperations saleAssociateOperations;
    private static SeedOperations seedOperations;
    private static SettingsOperations settingsOperations;
    private static StoreOperations storeOperations;

    public static void resetOperationControllers(boolean forUnitTests) {
        baseOperations = forUnitTests ? null : new BaseOperations();
        consumerOperations = forUnitTests ? null : new ConsumerOperations();
        demandOperations = forUnitTests ? null : new DemandOperations();
        locationOperations = forUnitTests ? null : new LocationOperations();
        paymentOperations = forUnitTests ? null : new PaymentOperations();
        proposalOperations = forUnitTests ? null : new ProposalOperations();
        rawCommandOperations = forUnitTests ? null : new RawCommandOperations();
        saleAssociateOperations = forUnitTests ? null : new SaleAssociateOperations();
        seedOperations = forUnitTests ? null : new SeedOperations();
        settingsOperations = forUnitTests ? null : new SettingsOperations();
        storeOperations = forUnitTests ? null : new StoreOperations();
    }

    static {
        resetOperationControllers(false);
    }

    public static BaseOperations getBaseOperations() { return baseOperations; }
    public static ConsumerOperations getConsumerOperations() { return consumerOperations; }
    public static DemandOperations getDemandOperations() { return demandOperations; }
    public static LocationOperations getLocationOperations() { return locationOperations; }
    public static PaymentOperations getPaymentOperations() { return paymentOperations; }
    public static ProposalOperations getProposalOperations() { return proposalOperations; }
    public static RawCommandOperations getRawCommandOperations() { return rawCommandOperations; }
    public static SaleAssociateOperations getSaleAssociateOperations() { return saleAssociateOperations; }
    public static SeedOperations getSeedOperations() { return seedOperations; }
    public static SettingsOperations getSettingsOperations() { return settingsOperations; }
    public static StoreOperations getStoreOperations() { return storeOperations; }

    public static void setMockBaseOperations(BaseOperations baseOperations) { BaseSteps.baseOperations = baseOperations; }
    public static void setMockConsumerOperations(ConsumerOperations consumerOperations) { BaseSteps.consumerOperations = consumerOperations; }
    public static void setMockDemandOperations(DemandOperations demandOperations) { BaseSteps.demandOperations = demandOperations; }
    public static void setMockLocationOperations(LocationOperations locationOperations) { BaseSteps.locationOperations = locationOperations; }
    public static void setMockPaymentOperations(PaymentOperations paymentOperations) { BaseSteps.paymentOperations = paymentOperations; }
    public static void setMockProposalOperations(ProposalOperations proposalOperations) { BaseSteps.proposalOperations = proposalOperations; }
    public static void setMockRawCommandOperations(RawCommandOperations rawCommandOperations) { BaseSteps.rawCommandOperations = rawCommandOperations; }
    public static void setMockSaleAssociateOperations(SaleAssociateOperations saleAssociateOperations) { BaseSteps.saleAssociateOperations = saleAssociateOperations; }
    public static void setMockSeedOperations(SeedOperations seedOperations) { BaseSteps.seedOperations = seedOperations; }
    public static void setMockSettingsOperations(SettingsOperations settingsOperations) { BaseSteps.settingsOperations = settingsOperations; }
    public static void setMockStoreOperations(StoreOperations storeOperations) { BaseSteps.storeOperations = storeOperations; }

    /**
     * Sends a message to the CC-ed people with a copy of the given demand, just after the creation
     *
     * @param demand Demand to echo to the CC-ed people
     * @param consumer Demand owner
     *
     * @throws DataSourceException If getting information about the demand location fails
     */
    public static void notifyCreationToCCed(PersistenceManager pm, Demand demand, Consumer owner) throws DataSourceException {
        // Prepare the message for the CC-ed
        List<String> cc = demand.getCC();
        if (cc != null && 0 < cc.size()) {
            //
            // TODO: get the #hashtag to decide which label set to use!
            //

            // Compose the message
            Locale locale = owner.getLocale();
            Location location;
            try {
                location = LocationSteps.getLocation(pm, demand);
            }
            catch (InvalidIdentifierException e) {
                location = null;
            }
            String tweet = CommandProcessor.generateTweet(demand, location, false, locale);
            String messageCC = LabelExtractor.get("cp_command_demand_forward_creation_to_cc", new Object[] { owner.getName(), tweet }, locale);

            // Send the message
            notifyMessageToCCed(cc, messageCC, locale);
        }
    }

    /**
     * Sends a message to the CC-ed people with a copy of the given demand, just after an update
     *
     * @param demand Demand to echo to the CC-ed people
     * @param consumer Demand owner
     *
     * @throws DataSourceException If getting information about the demand location fails
     */
    public static void notifyUpdateToCCed(PersistenceManager pm, Demand demand, Consumer owner) throws DataSourceException {
        // Prepare the message for the CC-ed
        List<String> cc = demand.getCC();
        if (cc != null && 0 < cc.size()) {
            //
            // TODO: get the #hashtag to decide which label set to use!
            //

            // Compose the message
            Locale locale = owner.getLocale();
            Location location;
            try {
                location = LocationSteps.getLocation(pm, demand);
            }
            catch (InvalidIdentifierException e) {
                location = null;
            }
            String tweet = CommandProcessor.generateTweet(demand, location, false, locale);
            String messageCC = LabelExtractor.get("cp_command_demand_forward_update_to_cc", new Object[] { owner.getName(), tweet }, locale);

            // Send the message
            notifyMessageToCCed(cc, messageCC, locale);
        }
    }

    /**
     * Sends a message to the CC-ed people with a copy of the given demand, just after a cancellation
     *
     * @param demand Demand to echo to the CC-ed people
     * @param consumer Demand owner
     *
     * @throws DataSourceException If getting information about the demand location fails
     */
    public static void notifyCancellationToCCed(PersistenceManager pm, Demand demand, Consumer owner) throws DataSourceException {
        // Prepare the message for the CC-ed
        List<String> cc = demand.getCC();
        if (cc != null && 0 < cc.size()) {
            //
            // TODO: get the #hashtag to decide which label set to use!
            //

            // Compose the message
            Locale locale = owner.getLocale();
            Location location;
            try {
                location = LocationSteps.getLocation(pm, demand);
            }
            catch (InvalidIdentifierException e) {
                location = null;
            }
            String tweet = CommandProcessor.generateTweet(demand, location, false, locale);
            String messageCC = LabelExtractor.get("cp_command_demand_forward_cancel_to_cc", new Object[] { owner.getName(), tweet }, locale);

            // Send the message
            notifyMessageToCCed(cc, messageCC, locale);
        }
    }

    /**
     * Sends a message to the CC-ed people with a copy of the given demand, just after a proposal confirmation
     *
     * @param demand Demand to echo to the CC-ed people
     * @param proposalKey Identifier of the confirmed Proposal
     * @param store Place where a SaleAssociate created the Proposal
     * @param consumer Demand owner
     *
     * @throws DataSourceException If getting information about the demand location fails
     */
    public static void notifyConfirmationToCCed(PersistenceManager pm, Demand demand, Long proposalKey, Store store, Consumer owner) throws DataSourceException {
        // Prepare the message for the CC-ed
        List<String> cc = demand.getCC();
        if (cc != null && 0 < cc.size()) {
            //
            // TODO: get the #hashtag to decide which label set to use!
            //

            // Compose the message
            Locale locale = owner.getLocale();
            String demandRef = LabelExtractor.get("cp_tweet_demand_reference_part", new Object[] { demand.getKey() }, locale);
            String proposalRef = LabelExtractor.get("cp_tweet_proposal_reference_part", new Object[] { proposalKey }, locale);
            String demandTags = demand.getCriteria().size() == 0 ? "" : LabelExtractor.get("cp_tweet_tags_part", new Object[] { demand.getSerializedCriteria() }, locale);
            String pickup = LabelExtractor.get("cp_tweet_store_part", new Object[] { store.getKey(), store.getName() }, locale);
            String messageCC = LabelExtractor.get("cp_command_confirm_forward_confirmation_to_cc", new Object[] { owner.getName(), proposalRef, demandRef, demandTags, pickup }, locale);

            // Send the message
            notifyMessageToCCed(cc, messageCC, locale);
        }
    }

    /**
     * Sends a message to the CC-ed people
     *
     * @param coordinates List of email addresses or Twitted identifier
     * @param message Information to convey to the CC-ed user
     * @param locale Identifier of the language to use
     */
    protected static void notifyMessageToCCed(List<String> coordinates, String message, Locale locale) {
        for (String coordinate: coordinates) {
            try {
                communicateToCCed(coordinate, message, locale);
            }
            catch (ClientException e) { } // Too bad, cannot contact the CC-ed person... Don't block the next sending!
        }
    }
}
