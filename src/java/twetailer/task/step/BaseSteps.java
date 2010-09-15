package twetailer.task.step;

import static twetailer.connector.BaseConnector.communicateToCCed;
import static twetailer.connector.BaseConnector.communicateToConsumer;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.CommunicationException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.connector.MessageGenerator;
import twetailer.connector.BaseConnector.Source;
import twetailer.connector.MessageGenerator.MessageId;
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
import twetailer.dto.Proposal;
import twetailer.dto.Store;
import twetailer.task.CommandProcessor;
import domderrien.i18n.DateUtils;
import domderrien.i18n.LabelExtractor;
import domderrien.i18n.LabelExtractor.ResourceFileId;

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

    public static String automatedResponseFooter = "%0A--%0AThis email will be sent to ezToff's automated mail reader.";

    public static void notifyCreationToCCed(PersistenceManager pm, Demand demand, Consumer owner) throws DataSourceException {
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
            notifyMessageToCCed(cc, "Copy of Twetailer Notification", messageCC, locale);
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
            notifyMessageToCCed(cc, "Copy of Twetailer Notification", messageCC, locale);
        }
    }

    /**
     * Sends a message to the CC-ed people with a copy of the given demand, just after a proposal confirmation
     *
     * @param demand Demand to echo to the CC-ed people
     * @param proposal Proposal to echo to the CC-ed people
     * @param store Place where a SaleAssociate created the Proposal
     * @param consumer Demand owner
     *
     * @throws DataSourceException If getting information about the demand location fails
     */
    public static void notifyConfirmationToCCed(PersistenceManager pm, Demand demand, Proposal proposal, Store store, Consumer owner) throws DataSourceException {
        // Prepare the message for the CC-ed
        List<String> cc = demand.getCC();
        if (cc != null && 0 < cc.size()) {
            //
            // TODO: get the #hashtag to decide which label set to use!
            //

            // Compose the message
            Locale locale = owner.getLocale();

            String demandTags = demand.getCriteria().size() == 0 ? "" : LabelExtractor.get("cp_tweet_tags_part", new Object[] { demand.getSerializedCriteria() }, locale);
            String pickup = LabelExtractor.get("cp_tweet_store_part", new Object[] { store.getKey(), store.getName() }, locale);
            String dueDate = DateUtils.dateToCustom(proposal.getDueDate(), LabelExtractor.get("cp_short_date_part", locale), locale);
            String dueTime = DateUtils.dateToCustom(proposal.getDueDate(), LabelExtractor.get("cp_short_time_part", locale), locale);

            String messageCC = LabelExtractor.get(
                    "cp_command_confirm_forward_confirmation_to_cc",
                    new Object[] {
                            owner.getName(),                // 0
                            proposal.getKey(),              // 1
                            demand.getKey(),                // 2
                            demandTags,                     // 3
                            pickup,                         // 4
                            dueDate,                        // 5
                            dueTime,                        // 6
                            proposal.getQuantity(),         // 7
                            proposal.getPrice(),            // 8
                            proposal.getTotal(),            // 9
                            "\\$",                          // 10
                            proposal.getSerializedCriteria()// 11
                    },
                    locale
            );

            // Send the message
            notifyMessageToCCed(cc, "Copy of Twetailer Notification", messageCC, locale);
        }
    }

    /**
     * Sends a message to the CC-ed people
     *
     * @param coordinates List of email addresses or Twitted identifier
     * @param e-mail subject, for thread-aware mail readers
     * @param message Information to convey to the CC-ed user
     * @param locale Identifier of the language to use
     */
    public static void notifyMessageToCCed(List<String> coordinates, String subject, String message, Locale locale) {
        for (String coordinate: coordinates) {
            try {
                communicateToCCed(Source.mail, coordinate, subject, message, locale);
            }
            catch (ClientException e) { } // Too bad, cannot contact the CC-ed person... Don't block the next sending!
        }
    }
}
