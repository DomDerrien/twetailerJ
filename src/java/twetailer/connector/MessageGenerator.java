package twetailer.connector;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import twetailer.connector.BaseConnector.Source;
import twetailer.dto.Command;
import twetailer.dto.Demand;
import twetailer.dto.Entity;
import twetailer.dto.Location;
import twetailer.dto.Proposal;
import twetailer.dto.Store;
import twetailer.dto.HashTag.RegisteredHashTag;
import twetailer.task.CommandLineParser;
import twetailer.validator.CommandSettings;
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
        messageFooter("footer"),
        emptyListIndicator("emptyListIndicator"),
        dateFormat("dateFormat"),
        dateTimeFormat("dateTimeFormat"),

        /** For message sent to the demand owner to confirm the demand creation */
        demandCreationAck("demand_creation_ackToOwner"),
        /** For message sent to CC'ed users to inform about the demand creation */
        demandCreationCpy("demand_creation_ackToCCed"),
        /** For message sent to sale associates located in the demand zone to inform about the new demand */
        demandCreationNot("demand_creation_associateNotif"),
        /** For message sent to the demand owner to confirm the demand update */
        demandUpdateAck("demand_update_ackToOwner"),
        /** For message sent to CC'ed users to inform about the demand update */
        demandUpdateCpy("demand_update_ackToCCed"),
        /** For message sent to the demand owner to confirm the demand cancellation */
        demandCancellationAck("demand_cancellation_ackToOwner"),
        /** For message sent to CC'ed users to inform about the demand cancellation */
        demandCancellationCpy("demand_cancellation_ackToCCed"),
        /** For message sent to the demand owner to confirm the demand deletion */
        demandDeletionAck("demand_deletion_ackToOwner"),
        /** For message sent to the demand owner to confirm the proposal confirmation */
        proposalConfirmationAck("proposal_confirmation_ackToConsumer"),
        /** For message sent to CC'ed users to inform about the proposal confirmation */
        proposalConfirmationCpy("proposal_confirmation_ackToCCed"),
        /** For message sent to the proposal owner to inform about the proposal confirmation */
        proposalConfirmationNot("proposal_confirmation_associateNotif"),
        /** For message sent to the demand owner to confirm the proposal declination */
        proposalDeclinationAck("zzz"),
        /** For message sent to CC'ed users to inform about the proposal declination */
        proposalDeclinationCpy("zzz"),
        /** For message sent to the demand owner to confirm the demand closing */
        demandClosingAck("demand_closing_ackToOwner"),
        /** For message sent to the owner of the proposal attached to the demand, to invite him to close it too */
        demandClosingNot("demand_closing_associateNotif"),

        /** For message sent to the proposal owner to confirm the proposal creation */
        proposalCreationAck("proposal_creation_ackToOwner"),
        /** For message sent to the demand owner to inform about the proposal creation */
        proposalCreationNot("proposal_creation_consumerNotif"),
        /** For message sent to the CC'ed users to inform about the proposal creation */
        proposalCreationCpy("proposal_creation_ccedNotif"),
        /** For message sent to the proposal owner to confirm the proposal update */
        proposalUpdateAck("proposal_update_ackToOwner"),
        /** For message sent to the demand owner to inform about the proposal update */
        proposalUpdateNot("zzz"),
        /** For message sent to the CC'ed users to inform about the proposal update */
        proposalUpdateCpy("zzz"),
        /** For message sent to the proposal owner to confirm the proposal cancellation */
        proposalCancellationAck("zzz"),
        /** For message sent to the demand owner to inform about the proposal cancellation */
        proposalCancellationNot("zzz"),
        /** For message sent to the CC'ed users to inform about the proposal cancellation */
        proposalCancellationCpy("zzz"),
        /** For message sent to the proposal owner to confirm the proposal closing*/
        proposalClosingAck("proposal_closing_ackToOwner"),
        /** For message sent to the owner of the demand attached to the proposal, to invite him to close it too */
        proposalClosingNot("proposal_closing_consumerNotif");

        private String tmxSuffix;

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
        String prefix = getChannelPrefix(communicationChannel) + getVerticalPrefix(hashTags);
        String out = LabelExtractor.get(ResourceFileId.fourth, prefix + identifier.getTMXSuffix(), parameters, locale);
        return out;
    }

    private String channelPrefix;
    private String verticalPrefix;
    private Locale userLocale;
    private String dateFormat;
    private String dateTimeFormat;
    private Map<String, Object> parameters = new HashMap<String, Object>();

    protected Map<String, Object> getParameters() {
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
     * @param demand Object to scan
     * @return The object instance, ready to be chained to another <code>fetch()</code> call
     */
    public MessageGenerator fetch(Demand demand) {
        if (demand != null) {
            final String prefix = "demand" + FIELD_SEPARATOR;
            // Entity & Command
            fetchEntity(demand, prefix);
            fetchCommand(demand, prefix);
            // Demand
            parameters.put(prefix + Demand.EXPIRATION_DATE, serializeDate(demand.getExpirationDate(), userLocale));
            // TODO: Demand.PROPOSAL_KEYS;
            parameters.put(prefix + Demand.RANGE, demand.getRange());
            parameters.put(prefix + Demand.RANGE_UNIT, demand.getRangeUnit());
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
            // Entity & Command
            fetchEntity(proposal, prefix);
            fetchCommand(proposal, prefix);
            // Proposal
            parameters.put(prefix + Proposal.CURRENCY_CODE, LabelExtractor.get(ResourceFileId.fourth, "common_currencySymbol_" + proposal.getCurrencyCode(), userLocale));
            parameters.put(prefix + Proposal.DEMAND_REFERENCE, proposal.getDemandKey());
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
            // Location.HAS_STORE
            // Location.LATITUDE
            // Location.LONGITUDE
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
        // Command
        // Command.ACTION
        // TODO: Proposal.CC
        parameters.put(prefix + Command.CRITERIA, command.getSerializedCriteria(emptyListIndicator));
        if (command.getDueDate() != null) { parameters.put(prefix + Command.DUE_DATE, serializeDate(command.getDueDate(), userLocale)); }
        parameters.put(prefix + Command.HASH_TAGS, command.getSerializedHashTags(emptyListIndicator));
        String metadata = command.getMetaData();
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
            catch(JsonException ex) { } // Malformed metadata are just not echoed back
        }
        parameters.put(prefix + Command.QUANTITY, command.getQuantity());
        parameters.put(prefix + Command.STATE, CommandSettings.getStates(userLocale).getString(command.getState().toString()));
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
                (Object[]) null,
                userLocale
        );
    }
}
