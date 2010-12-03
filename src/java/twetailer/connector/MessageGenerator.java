package twetailer.connector;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import twetailer.connector.BaseConnector.Source;
import twetailer.dto.Command;
import twetailer.dto.Demand;
import twetailer.dto.Entity;
import twetailer.dto.Influencer;
import twetailer.dto.Location;
import twetailer.dto.Proposal;
import twetailer.dto.Registrar;
import twetailer.dto.Request;
import twetailer.dto.ReviewSystem;
import twetailer.dto.Store;
import twetailer.dto.Wish;
import twetailer.dto.HashTag.RegisteredHashTag;
import twetailer.validator.CommandSettings;
import twetailer.validator.LocaleValidator;
import domderrien.i18n.DateUtils;
import domderrien.i18n.LabelExtractor;
import domderrien.i18n.LabelExtractor.ResourceFileId;
import domderrien.jsontools.JsonException;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.JsonParser;

/**
 * Messages Twetailer can produce vary according to:<ul>
 * <li>The connector's type: <code>long</code> when message can be verbose, and <code>short</code>;</li>
 * <li>The current vertical in [<code>none</code>, <code>golf</code>, <code>taxi</code>, etc.].</li></ul>
 * This utility class provides a wrapper that load the right message and fetch it with the given parameters.
 *
 * Note that the language selection is done by the called <code>LabelExtractor.get()</code> method.
 *
 * @see domderrien.i18n.LabelExtractor#get(int, Object[], Locale)
 * @see domderrien.i18n.LabelExtractor#get(int, Map, Locale)
 *
 * @author Dom Derrien
 */
public class MessageGenerator {

    /**
     * Identifiers of the message the class can handle transparently
     */
    public enum MessageId {
        dateFormat,
        dateTimeFormat,
        emptyListIndicator,
        noCCIndicator,

        messageSubject ("message_subject"),
        messageFooter ("message_footer"),
        robotAutomatedResponse ("robot_automatedResponse"),

        /// W1. Consumer creates a wish -- CC'ed and associates are notified
        /** For message sent to the wish owner to confirm the wish creation */
        WISH_CREATION_OK_TO_CONSUMER ("wish_creation_ackToConsumer"),
        /** For message sent to CC'ed users to inform about the wish creation */
        WISH_CREATION_OK_TO_CCED ("wish_creation_ackToCCed"),

        /// W2. Consumer updates his wish -- CC'ed are notified
        /** For message sent to the wish owner to confirm the wish update */
        WISH_UPDATE_OK_TO_CONSUMER ("wish_update_ackToConsumer"),
        /** For message sent to CC'ed users to inform about the wish update */
        WISH_UPDATE_OK_TO_CCED ("wish_update_ackToCCed"),

        /// W3. Consumer cancels his wish
        /** For message sent to the wish owner to confirm the wish cancellation */
        WISH_CANCELLATION_OK_TO_CONSUMER ("wish_cancellation_ackToConsumer"),

        /// W4. Someone transforms the wish in a demand
        /** For message sent to the wish owner to confirm the wish cancellation */
        WISH_TRANSFORMATION_OK_TO_INITIATOR ("wish_transformation_ackToInitiator"),
        /** For message sent to the wish owner to confirm the wish cancellation */
        WISH_TRANSFORMATION_OK_TO_CONSUMER ("wish_transformation_ackToConsumer"),
        /** For message sent to the wish owner to confirm the wish cancellation */
        WISH_TRANSFORMATION_OK_TO_CCED ("wish_transformation_ackToCCed"),

        /// C1. Consumer creates a demand -- CC'ed and associates are notified
        /** For message sent to the demand owner to confirm the demand creation */
        DEMAND_CREATION_OK_TO_CONSUMER ("demand_creation_ackToConsumer"),
        /** For message sent to CC'ed users to inform about the demand creation */
        DEMAND_CREATION_OK_TO_CCED ("demand_creation_ackToCCed"),
        /** For message sent to sale associates located in the demand zone to inform about the new demand */
        DEMAND_CREATION_OK_TO_ASSOCIATE ("demand_creation_associateNotif"),

        /// C2. Consumer updates his demand -- CC'ed are notified
        /** For message sent to the demand owner to confirm the demand update */
        DEMAND_UPDATE_OK_TO_CONSUMER ("demand_update_ackToConsumer"),
        /** For message sent to CC'ed users to inform about the demand update */
        DEMAND_UPDATE_OK_TO_CCED ("demand_update_ackToCCed"),
        // An update is presented as a notification of a new demand to sale associates

        /// C3. Consumer cancels his demand -- Only the associate of the confirmed proposal (if any) is notified
        /** For message sent to the demand owner to confirm the demand cancellation */
        DEMAND_CANCELLATION_OK_TO_CONSUMER ("demand_cancellation_ackToConsumer"),
        /** For message sent to the owner of the proposal attached to the demand, to inform him the demand has been canceled with the side-effect of canceling the proposal too */
        DEMAND_CONFIRMED_CANCELLATION_OK_TO_ASSOCIATE ("demand_confirmed_cancellation_associateNotif"),

        /// C4. Consumer confirms a proposal -- CC'ed and associate are notified
        /** For message sent to the demand owner to confirm the proposal confirmation */
        PROPOSAL_CONFIRMATION_OK_TO_CONSUMER ("proposal_confirmation_ackToConsumer"),
        /** For message sent to CC'ed users to inform about the proposal confirmation */
        PROPOSAL_CONFIRMATION_OK_TO_CCED ("proposal_confirmation_ackToCCed"),
        /** For message sent to the proposal owner to inform about the proposal confirmation */
        PROPOSAL_CONFIRMATION_OK_TO_ASSOCIATE ("proposal_confirmation_associateNotif"),

        /// C5. Consumer closes his demand -- Only associate who has not closed his proposal is notified
        /** For message sent to the demand owner to confirm the demand closing */
        DEMAND_CLOSING_OK_TO_CONSUMER ("demand_closing_ackToConsumer"),
        /** For message sent to the owner of the proposal attached to the demand, to invite him to close it too */
        DEMAND_CLOSING_OK_TO_ASSOCIATE ("demand_closing_associateNotif"),

        /// A1. Associate creates a proposal -- Consumer and CC'ed are notified
        /** For message sent to the proposal owner to confirm the proposal creation */
        PROPOSAL_CREATION_OK_TO_ASSOCIATE ("proposal_creation_ackToAssociate"),
        /** For message sent to the demand owner to inform about the proposal creation */
        PROPOSAL_CREATION_OK_TO_CONSUMER ("proposal_creation_consumerNotif"),
        /** For message sent to the CC'ed users to inform about the proposal creation */
        PROPOSAL_CREATION_OK_TO_CCED("proposal_creation_ccedNotif"),

        /// A2. Associate updates a proposal -- Consumer and CC'ed are notified
        /** For message sent to the proposal owner to confirm the proposal update */
        PROPOSAL_UPDATE_OK_TO_ASSOCIATE ("proposal_update_ackToAssociate"),
        /** For message sent to the demand owner to inform about the proposal update */
        PROPOSAL_UPDATE_OK_TO_CONSUMER ("proposal_update_consumerNotif"),
        /** For message sent to the CC'ed users to inform about the proposal update */
        PROPOSAL_UPDATE_OK_TO_CCED ("proposal_update_ccedNotif"),

        /// A3. Associate cancels a proposal -- Only the consumer of the confirmed demand is notified (demand placed back in 'published' state)
        /** For message sent to the proposal owner to confirm the proposal cancellation */
        PROPOSAL_CANCELLATION_OK_TO_ASSOCIATE ("proposal_cancellation_ackToAssociate"),
        /** For message sent to the owner of the proposal attached to the demand, to inform him the demand has been canceled with the side-effect of canceling the proposal too */
        PROPOSAL_CONFIRMED_CANCELLATION_OK_TO_CONSUMER ("proposal_confirmed_cancellation_consumerNotif"),

        /// A4. Associate closes his demand -- Only consumer who has not closed his demand is notified
        /** For message sent to the proposal owner to confirm the proposal closing*/
        PROPOSAL_CLOSING_OK_TO_ASSOCIATE ("proposal_closing_ackToAssociate"),
        /** For message sent to the owner of the demand attached to the proposal, to invite him to close it too */
        PROPOSAL_CLOSING_OK_TO_CONSUMER ("proposal_closing_consumerNotif"),

        nop; // Just to simplify the manipulation of the list entries :-)

        private String tmxSuffix;

        MessageId () {
            this.tmxSuffix = name();
        }
        MessageId (String tmxSuffix) {
            this.tmxSuffix = tmxSuffix;
        }

        String getTMXSuffix() {
            return tmxSuffix;
        }
    }

    protected final static String SHORT_MESSAGE_PREFIX = "short_";
    protected final static String LONG_MESSAGE_PREFIX = "long_";

    /**
     * Local helper deciding what type of messages should be loaded for the given source. If the
     * source is <code>api</code> or <code>robot</code>, it's expected that this function won't be
     * called (the caller should triage up-front).
     *
     * @param source Qualifier of the communication channel
     * @return <code>SHORT_MESSAGE_PREFIX</code> if the communication channel is <code>Twitter</code>
     * or <code>Jabber</code>, <code>LONG_MESSAGE_PREFIX</code> otherwise.
     *
     * @throws IllegalArgumentException if source is <code>api</code> or <code>robot</code>
     */
    protected static String getChannelPrefix(Source source) {
        if (Source.api.equals(source) || Source.robot.equals(source)) {
            throw new IllegalArgumentException("Caller should triage source in [api, robot] to avoid loading messages for nothing");
        }
        return Source.twitter.equals(source) || Source.jabber.equals(source) ? SHORT_MESSAGE_PREFIX : LONG_MESSAGE_PREFIX;
    }

    protected final static String DEFAULT_VERTICAL_PREFIX = "core_";
    protected final static String GOLF_VERTICAL_PREFIX = "golf_";

    /**
     * Get the accurate prefix for the TMX entries from the list of hash tags
     *
     * @param hashTags List of hash tags associated to a demand or proposal
     * @return Valid TMX entry prefix
     */
    protected static String getVerticalPrefix(List<String> hashTags) {
        String identifier = DEFAULT_VERTICAL_PREFIX;
        if (hashTags == null || hashTags.size() == 0) {
            // Default to 'core'
        }
        else if (hashTags.contains(RegisteredHashTag.golf.toString())) {
            identifier = GOLF_VERTICAL_PREFIX;
        }
        // Note: other hash tags don't rely on a prefix for now
        return identifier;
    }

    /**
     * Helper returning a localized message <code>long</code> or <code>short</code> and defined for the
     * corresponding vertical (loaded from the given hash tag list). Given parameters are injected into
     * the message if its TMX entry hosted the corresponding place holders.
     *
     * @param communicationChannel Used to select among <code>long</code> or <code>short</code> message
     * @param hashTags Used to detect the vertical prefix
     * @param identifier Identifier of the message to load
     * @param parameters Array of parameters to inject in sequence in place of the message place holders ({0}, {1}, {2}, etc.)
     * @param locale Used to load a localized message
     * @return String ready to be sent to the end users
     */
    public static String getMessage(Source communicationChannel, List<String> hashTags, MessageId identifier, String[] parameters, Locale locale) {
        String out = LabelExtractor.get(
                ResourceFileId.fourth,
                getChannelPrefix(communicationChannel) + getVerticalPrefix(hashTags) + identifier.getTMXSuffix(),
                getChannelPrefix(communicationChannel) + DEFAULT_VERTICAL_PREFIX + identifier.getTMXSuffix(),
                parameters,
                locale
        );
        return out;
    }

    private Source communicationChannel;
    private String channelPrefix;
    private String verticalPrefix;
    private Locale userLocale;
    private String dateFormat;
    private String dateTimeFormat;
    private Map<String, Object> parameters = new HashMap<String, Object>();

    /** Accessor */
    public Source getCommunicationChannel() {
        return communicationChannel;
    }

    /** Accessor */
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * Default constructor for a <code>MessageGenerator</code> instance
     * ready to be chained with multiple <code>fetch()</code> calls.
     *
     * @param communicationChannel Used to select among <code>long</code> or <code>short</code> message
     * @param hashTags Used to detect the vertical prefix
     * @param locale End-user's locale
     */
    public MessageGenerator(Source communicationChannel, List<String> hashTags, Locale locale) {
        this.communicationChannel = communicationChannel;
        channelPrefix = getChannelPrefix(communicationChannel);
        verticalPrefix = getVerticalPrefix(hashTags);
        userLocale = locale;

        dateFormat = getAlternateMessage(MessageId.dateFormat);
        dateTimeFormat = getAlternateMessage(MessageId.dateTimeFormat);
    }

    @SuppressWarnings("deprecation")
    protected String serializeDate(Date date, Locale locale) {
        if (date.getHours() == 23) {
            if (date.getMinutes() == 59) {
                if (date.getSeconds() == 59) {
                    return DateUtils.dateToCustom(date, dateFormat, userLocale);
                }
            }
        }
        return DateUtils.dateToCustom(date, dateTimeFormat, userLocale);
    }

    protected final static String FIELD_SEPARATOR = ">";

    /**
     * Extracts non null attributes and keeps them into the local parameter map
     *
     * @param wish Object to scan
     * @return The object instance, ready to be chained to another <code>fetch()</code> call
     */
    public MessageGenerator fetch(Wish wish) {
        if (wish != null) {
            final String prefix = "wish" + FIELD_SEPARATOR;
            // Wish
            fetchRequest(wish, prefix);
        }
        return this;
    }

    /**
     * Extracts non null attributes and keeps them into the local parameter map
     *
     * @param demand Object to scan
     * @return The object instance, ready to be chained to another <code>fetch()</code> call
     */
    public MessageGenerator fetch(Demand demand) {
        if (demand != null) {
            final String emptyListIndicator = getAlternateMessage(MessageId.emptyListIndicator);
            final String prefix = "demand" + FIELD_SEPARATOR;
            // Wish
            fetchRequest(demand, prefix);
            // Demand
            parameters.put(prefix + Demand.PROPOSAL_KEYS, demand.getSerializedProposalKeys(emptyListIndicator));
        }
        return this;
    }

    /**
     * Extracts non null attributes and keeps them into the local parameter map
     *
     * @param request Object to scan
     * @param prefix Identifier of the object
     * @return The object instance, ready to be chained to another <code>fetch()</code> call
     */
    public MessageGenerator fetchRequest(Request request, String prefix) {
        if (request != null) {
            // Command
            fetchCommand(request, prefix);
            // Demand
            parameters.put(prefix + Demand.EXPIRATION_DATE, serializeDate(request.getExpirationDate(), userLocale));
            parameters.put(prefix + Demand.RANGE, request.getRange());
            parameters.put(prefix + Demand.RANGE_UNIT, request.getRangeUnit());
        }
        return this;
    }

    /**
     * Extracts non null attributes and keeps them into the local parameter map
     *
     * @param proposal Object to scan
     * @return The object instance, ready to be chained to another <code>fetch()</code> call
     */
    public MessageGenerator fetch(Proposal proposal) {
        if (proposal != null) {
            final String prefix = "proposal" + FIELD_SEPARATOR;
            // Command
            fetchCommand(proposal, prefix);
            // Proposal
            parameters.put(prefix + Proposal.CURRENCY_CODE, LabelExtractor.get(ResourceFileId.master, "currencySymbol_" + proposal.getCurrencyCode(), (String) null, userLocale));
            parameters.put(prefix + Proposal.DEMAND_KEY, proposal.getDemandKey());
            parameters.put(prefix + Proposal.PRICE, proposal.getPrice());
            parameters.put(prefix + Proposal.TOTAL, proposal.getTotal());
        }
        return this;
    }

    /**
     * Extracts non null attributes and keeps them into the local parameter map
     *
     * @param store Object to scan
     * @return The object instance, ready to be chained to another <code>fetch()</code> call
     */
    public MessageGenerator fetch(Store store) {
        if (store != null) {
            final String prefix = "store" + FIELD_SEPARATOR;
            // Entity
            fetchEntity(store, prefix);
            // Store
            parameters.put(prefix + Store.ADDRESS, store.getAddress());
            double closedNb = store.getClosedProposalNb() == null ? 0 : store.getClosedProposalNb();
            parameters.put(prefix + Store.CLOSED_PROPOSAL_NB, closedNb);
            parameters.put(prefix + Store.EMAIL, store.getEmail());
            parameters.put(prefix + Store.NAME, store.getName());
            parameters.put(prefix + Store.PHONE_NUMBER, store.getPhoneNumber());
            double publishedNb = store.getPublishedProposalNb() == null ? 1 : store.getPublishedProposalNb(); // Can't be null with new demands, but can still be null with the old ones without this field
            parameters.put(prefix + Store.PUBLISHED_PROPOSAL_NB, publishedNb);
            parameters.put(prefix + Store.URL, store.getUrl());

            parameters.put(
                    prefix + "closedProposalPercentage",
                    LocaleValidator.formatFloatWith2Digits(100.0D * closedNb / publishedNb, userLocale)
            );

        }
        return this;
    }

    /**
     * Extracts non null attributes and keeps them into the local parameter map
     *
     * @param location Object to scan
     * @param parentPrefix Identifier of the parent's type
     * @return The object instance, ready to be chained to another <code>fetch()</code> call
     */
    public MessageGenerator fetch(Location location, String parentPrefix) {
        if (location != null) {
            final String prefix = parentPrefix + FIELD_SEPARATOR + "location" + FIELD_SEPARATOR;
            // Entity
            fetchEntity(location, prefix);
            // Location
            parameters.put(prefix + Location.COUNTRY_CODE, location.getCountryCode());
            parameters.put(prefix + Location.HAS_STORE, location.getHasStore());
            parameters.put(prefix + Location.LATITUDE, location.getLatitude());
            parameters.put(prefix + Location.LONGITUDE, location.getLongitude());
            parameters.put(prefix + Location.POSTAL_CODE, location.getPostalCode());
        }
        return this;
    }

    /**
     * Extracts non null attributes and keeps them into the local parameter map
     *
     * @param entity Object to scan
     * @param prefix Identifier to prepend to each entry in the parameter map
     * @return The object instance, ready to be chained to another <code>fetch()</code> call
     */
    public MessageGenerator fetchCommand(Command command, String prefix) {
        final String emptyListIndicator = getAlternateMessage(MessageId.emptyListIndicator);
        final String noCCIndicator = getAlternateMessage(MessageId.noCCIndicator);
        // Entity
        fetchEntity(command, prefix);
        // Command
        // Command.ACTION
        parameters.put(prefix + Command.CC, command.getSerializedCC(noCCIndicator)); // Default setting
        parameters.put(prefix + Command.CRITERIA, command.getSerializedCriteria(emptyListIndicator));
        if (command.getDueDate() != null) { parameters.put(prefix + Command.DUE_DATE, serializeDate(command.getDueDate(), userLocale)); }
        parameters.put(prefix + Command.HASH_TAGS, command.getSerializedHashTags(emptyListIndicator));
        String metadata = command.getMetadata();
        parameters.put(prefix + Command.META_DATA, emptyListIndicator); // Default setting
        if (metadata != null && 0 < metadata.length()) {
            try {
                JsonObject data = new JsonParser(metadata).getJsonObject();
                if (0 < data.size()) {
                    parameters.put(prefix + Command.META_DATA, metadata);
                    for(String key: data.getMap().keySet()) {
                        // TODO: use the metadata descriptor to decide the type of the data to extract
                        parameters.put(prefix + Command.META_DATA + FIELD_SEPARATOR + key, data.getLong(key));
                    }
                }
            }
            catch(JsonException ex) {
                // Malformed metadata are just not echoed back
                Logger.getLogger(MessageGenerator.class.getName()).info("Malformed metadata in " + prefix + ".key=" + command.getKey() + ": " + metadata + " -- message: " + ex.getMessage());
            }
        }
        parameters.put(prefix + Command.QUANTITY, command.getQuantity());
        parameters.put(prefix + Command.STATE, CommandSettings.getStates(userLocale).getString(command.getState().toString()));
        return this;
    }

    /**
     * Extracts non null attributes and keeps them into the local parameter map
     *
     * @param influencer Object to scan
     * @return The object instance, ready to be chained to another <code>fetch()</code> call
     */
    public MessageGenerator fetch(Influencer influencer) {
        if (influencer != null) {
            final String prefix = "influencer" + FIELD_SEPARATOR;
            // Entity
            fetchEntity(influencer, prefix);
            // Influencer
            parameters.put(prefix + Influencer.EMAIL, influencer.getEmail());
            parameters.put(prefix + Influencer.NAME, influencer.getName());
            parameters.put(prefix + Influencer.REFERRAL_ID, influencer.getReferralId());
            parameters.put(prefix + Influencer.URL, influencer.getUrl());
        }
        return this;
    }

    /**
     * Extracts non null attributes and keeps them into the local parameter map
     *
     * @param registrar Object to scan
     * @return The object instance, ready to be chained to another <code>fetch()</code> call
     */
    public MessageGenerator fetch(Registrar registrar) {
        if (registrar != null) {
            final String prefix = "registrar" + FIELD_SEPARATOR;
            // Entity
            fetchEntity(registrar, prefix);
            // Reseller
            parameters.put(prefix + Registrar.EMAIL, registrar.getEmail());
            parameters.put(prefix + Registrar.NAME, registrar.getName());
            parameters.put(prefix + Registrar.URL, registrar.getUrl());
        }
        return this;
    }

    /**
     * Extracts non null attributes and keeps them into the local parameter map
     *
     * @param reviewSystem Object to scan
     * @return The object instance, ready to be chained to another <code>fetch()</code> call
     */
    public MessageGenerator fetch(ReviewSystem reviewSystem) {
        if (reviewSystem != null) {
            final String prefix = "reviewSystem" + FIELD_SEPARATOR;
            // Entity
            fetchEntity(reviewSystem, prefix);
            // ReviewSystem
            parameters.put(prefix + ReviewSystem.EMAIL, reviewSystem.getEmail());
            parameters.put(prefix + ReviewSystem.NAME, reviewSystem.getName());
            parameters.put(prefix + ReviewSystem.URL, reviewSystem.getUrl());
        }
        return this;
    }

    /**
     * Extracts non null attributes and keeps them into the local parameter map
     *
     * @param entity Object to scan
     * @param prefix Identifier to prepend to each entry in the parameter map
     * @return The object instance, ready to be chained to another <code>fetch()</code> call
     */
    public MessageGenerator fetchEntity(Entity entity, String prefix) {
        // Entity
        if (entity.getKey() != null) { parameters.put(prefix + Entity.KEY, entity.getKey()); }
        parameters.put(prefix + Entity.CREATION_DATE, serializeDate(entity.getCreationDate(), userLocale));
        parameters.put(prefix + Entity.MODIFICATION_DATE, serializeDate(entity.getModificationDate(), userLocale));
        return this;
    }

    /**
     * Helper to add individual parameters
     *
     * @param key Identifier of the message to add or override
     * @param value Value to associate with the parameter key
     * @return The object instance, ready to be chained to another <code>fetch()</code> call
     */
    public MessageGenerator put(String key, Object value) {
        parameters.put(key, value);
        return this;
    }

    /**
     * Helper to retrieve individual parameters
     *
     * @param key Identifier of the message to extract
     * @return The identified data
     */
    public Object get(String key) {
        return parameters.remove(key);
    }

    /**
     * Helper to remove individual parameters
     *
     * @param key Identifier of the message to add or override
     * @return The object instance, ready to be chained to another <code>fetch()</code> call
     */
    public MessageGenerator remove(String key) {
        parameters.remove(key);
        return this;
    }

    /**
     * Helper returning a localized message <code>long</code> or <code>short</code> and defined for the
     * corresponding vertical (loaded from the given hash tag list). Given parameters are injected into
     * the message if its TMX entry hosted the corresponding place holders.
     *
     * Note the previously fetched Map <code>parameters</code> is used to replace each place holder
     * found in the loaded localized message and matching a Map key.
     *
     * @param identifier Identifier of the message to load
     * @return String ready to be sent to the end users
     */
    public String getMessage(MessageId identifier) {
        return LabelExtractor.get(
                ResourceFileId.fourth,
                channelPrefix + verticalPrefix + identifier.getTMXSuffix(),
                channelPrefix + DEFAULT_VERTICAL_PREFIX + identifier.getTMXSuffix(),
                parameters,
                userLocale
        );
    }

    /**
     * Use the already setup MessageGenerator instance to get a message loaded with information
     * given from the proposed map of parameters.
     *
     * @param identifier Identifier of the message to load
     * @param parameters Alternate source of information to be injected in the TMX entry
     * @return String ready to be sent to the end users
     */
    public String getAlternateMessage(MessageId identifier, Map<String, Object> parameters) {
        return LabelExtractor.get(
                ResourceFileId.fourth,
                channelPrefix + verticalPrefix + identifier.getTMXSuffix(),
                channelPrefix + DEFAULT_VERTICAL_PREFIX + identifier.getTMXSuffix(),
                parameters,
                userLocale
        );
    }

    /**
     * Use the already setup MessageGenerator instance to get a message loaded with information
     * given from the proposed map of parameters.
     *
     * @param identifier Identifier of the message to load
     * @param parameters Alternate source of information to be injected in the TMX entry
     * @return String ready to be sent to the end users
     */
    public String getAlternateMessage(MessageId identifier, Object[] parameters) {
        return LabelExtractor.get(
                ResourceFileId.fourth,
                channelPrefix + verticalPrefix + identifier.getTMXSuffix(),
                channelPrefix + DEFAULT_VERTICAL_PREFIX + identifier.getTMXSuffix(),
                parameters,
                userLocale
        );
    }

    /**
     * Use the already setup MessageGenerator instance to get a message.
     *
     * @param identifier Identifier of the message to load
     * @return String ready to be sent to the end users
     */
    public String getAlternateMessage(MessageId identifier) {
        return LabelExtractor.get(
                ResourceFileId.fourth,
                channelPrefix + verticalPrefix + identifier.getTMXSuffix(),
                channelPrefix + DEFAULT_VERTICAL_PREFIX + identifier.getTMXSuffix(),
                (Object[]) null,
                userLocale
        );
    }
}
