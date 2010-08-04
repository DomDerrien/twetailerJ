package twetailer.task.step;

import static twetailer.connector.BaseConnector.communicateToCCed;
import static twetailer.connector.BaseConnector.communicateToConsumer;

import java.util.List;
import java.util.Locale;

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
import twetailer.dto.RawCommand;
import twetailer.dto.SaleAssociate;
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

    public static void confirmUpdate(PersistenceManager pm, RawCommand rawCommand, Demand demand, Consumer owner) throws DataSourceException, InvalidIdentifierException, CommunicationException {

        List<String> cc = demand.getCC();
        if (!Source.api.equals(demand.getSource()) || cc != null && 0 < cc.size()) {
            boolean isNewDemand = demand.getCreationDate().getTime() == demand.getModificationDate().getTime();
            Locale locale = Locale.ENGLISH; // owner.getLocale(); // TODO

            Location location = LocationSteps.getLocation(pm, demand);

            // Prepare the request parameters
            String[] parameters = new String[] {
                    owner.getName(), // 0
                    demand.getKey().toString(), //1
                    demand.getState().toString(), // 2
                    demand.getDueDate().toString(), // 3
                    demand.getModificationDate().toString(), // 4
                    location.getPostalCode() + " (" + location.getCountryCode() + ") within " + demand.getRange() + " " + demand.getRangeUnit(), // 5
                    demand.getQuantity().toString(), // 6
                    demand.getSerializedCriteria("none"), // 7
                    demand.getSerializedHashTags("none"), // 8
                    demand.getSerializedCC("none"), // 9
                    "<unknown>", // 10
                    Source.widget.equals(demand.getSource()) ? LabelExtractor.get("mc_mail_subject_response_default", locale) : rawCommand.getSubject(), // 11
                    "cancel demand:" + demand.getKey().toString(), // 12
                    LabelExtractor.get(ResourceFileId.fourth, "long_golf_footer", locale), // 13
                    "0", //14
                    "0" // 15
            };

            // Send the operation confirmation to the owner
            if (!Source.api.equals(demand.getSource())) {
                String message = MessageGenerator.getMessage(
                        demand.getSource(),
                        demand.getHashTags(),
                        isNewDemand ? MessageId.demandCreationAck: MessageId.demandUpdateAck,
                        parameters,
                        locale
                );
                communicateToConsumer(rawCommand, owner, new String[] { message });
            }

            // Send a notification to the CC'ed users
            if (cc != null && 0 < cc.size()) {
                String message = MessageGenerator.getMessage(
                        demand.getSource(),
                        demand.getHashTags(),
                        isNewDemand ? MessageId.demandCreationCpy : MessageId.demandUpdateCpy,
                        parameters,
                        locale
                );
                notifyMessageToCCed(cc, message, locale);
            }
        }
    }

    public static void notifyAvailability(PersistenceManager pm, Demand demand, Consumer associate) throws DataSourceException, InvalidIdentifierException, CommunicationException {
        Locale locale = associate.getLocale();

        String[] parameters = new String[] {
                associate.getName(), // 0
                demand.getKey().toString(), // 1
                demand.getDueDate().toString(), // 2
                demand.getExpirationDate().toString(), // 3
                demand.getQuantity().toString(), // 4
                demand.getSerializedCriteria("none"), // 5
                demand.getSerializedHashTags("none"), // 6
                LabelExtractor.get("mc_mail_subject_response_default", locale), // 7
                "propose demand:" + demand.getKey().toString() + " due:[update-date-time] total:$[amount] meta:{pull:[0],buggy:[0]} tags:[extra-info]", // 8
                "decline demand:" + demand.getKey().toString(), // 9
                LabelExtractor.get(ResourceFileId.fourth, "long_golf_footer", locale), // 10
                "0", //11
                "0" // 12
        };

        String message = MessageGenerator.getMessage(
                associate.getPreferredConnection(),
                demand.getHashTags(),
                MessageId.demandCreationNot,
                parameters,
                locale
        );

        communicateToConsumer(new RawCommand(associate.getPreferredConnection()), associate, new String[] { message });
    }

    public static void confirmUpdate(PersistenceManager pm, RawCommand rawCommand, Proposal proposal, SaleAssociate owner, Consumer associate) throws DataSourceException, InvalidIdentifierException, CommunicationException {

        if (!Source.api.equals(proposal.getSource())) {
            boolean isNewProposal = proposal.getCreationDate().getTime() == proposal.getModificationDate().getTime();
            Locale locale = Locale.ENGLISH; // owner.getLocale(); // TODO

            String[] parameters = new String[] {
                    associate.getName(), // 0
                    proposal.getKey().toString(), // 1
                    proposal.getDemandKey().toString(), // 2
                    proposal.getState().toString(), // 3
                    proposal.getDueDate().toString(), // 4
                    proposal.getQuantity().toString(), // 5
                    proposal.getSerializedCriteria("none"), // 6
                    proposal.getSerializedHashTags("none"), // 7
                    "$", // 8
                    proposal.getPrice().toString(), // 9
                    proposal.getTotal().toString(), // 10
                    Source.widget.equals(proposal.getSource()) ? LabelExtractor.get("mc_mail_subject_response_default", locale) : rawCommand.getSubject(), // 11
                    "cancel proposal:" + proposal.getKey().toString(), // 12
                    LabelExtractor.get(ResourceFileId.fourth, "long_golf_footer", locale), // 13
                    "0", //14
                    "0" // 15
            };

            String message = MessageGenerator.getMessage(
                    proposal.getSource(),
                    proposal.getHashTags(),
                    isNewProposal ? MessageId.proposalCreationAck: MessageId.proposalUpdateAck,
                    parameters,
                    locale
            );
            communicateToConsumer(rawCommand, associate, new String[] { message });
        }
    }

    public static void notifyAvailability(PersistenceManager pm, Proposal proposal, Demand demand, Consumer consumer) throws DataSourceException, InvalidIdentifierException, CommunicationException {

        List<String> cc = demand.getCC();
        if (!Source.api.equals(demand.getSource()) || cc != null && 0 < cc.size()) {
            Locale locale = consumer.getLocale();
            Store store = getStoreOperations().getStore(pm, proposal.getStoreKey());
            Location location = getLocationOperations().getLocation(pm, store.getLocationKey());

            String[] parameters = new String[] {
                    consumer.getName(), // 0
                    demand.getKey().toString(), // 1
                    demand.getDueDate().toString(), // 2
                    demand.getExpirationDate().toString(), // 3
                    demand.getQuantity().toString(), // 4
                    demand.getSerializedCriteria("none"), // 5
                    demand.getSerializedHashTags("none"), // 6
                    proposal.getKey().toString(), // 7
                    proposal.getDueDate().toString(), // 8
                    proposal.getQuantity().toString(), // 9
                    proposal.getSerializedCriteria("none"), // 10
                    proposal.getSerializedHashTags("none"), // 11
                    "$", // 12
                    proposal.getPrice().toString(), // 13
                    proposal.getTotal().toString(), // 14
                    store.getKey().toString(), // 15
                    store.getName(), // 16
                    store.getAddress(), // 17
                    store.getUrl(), // 18
                    store.getPhoneNumber(), // 19
                    location.getPostalCode(), // 20
                    location.getCountryCode(), // 21
                    LabelExtractor.get("mc_mail_subject_response_default", locale), // 22
                    "confirm proposal:" + proposal.getKey().toString(), // 23
                    "decline proposal:" + proposal.getKey().toString(), // 24
                    LabelExtractor.get(ResourceFileId.fourth, "long_golf_footer", locale), // 25
                    "0", // 26
                    "0" // 27
            };

            // Send the proposal details to the owner
            if (!Source.api.equals(demand.getSource())) {
                String message = MessageGenerator.getMessage(
                        consumer.getPreferredConnection(),
                        demand.getHashTags(),
                        MessageId.proposalCreationNot,
                        parameters,
                        locale
                );
                communicateToConsumer(new RawCommand(consumer.getPreferredConnection()), consumer, new String[] { message });
            }

            // Send the proposal details to the CC'ed users
            if (cc != null && 0 < cc.size()) {
                String message = MessageGenerator.getMessage(
                        consumer.getPreferredConnection(),
                        demand.getHashTags(),
                        MessageId.proposalCreationCpy,
                        parameters,
                        locale
                );
                notifyMessageToCCed(cc, message, locale);
            }
        }
    }

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
