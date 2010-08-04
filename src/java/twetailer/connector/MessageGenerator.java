package twetailer.connector;

import java.util.List;
import java.util.Locale;

import twetailer.connector.BaseConnector.Source;
import twetailer.dto.HashTag.RegisteredHashTag;
import domderrien.i18n.LabelExtractor;
import domderrien.i18n.LabelExtractor.ResourceFileId;

public class MessageGenerator {

    public enum MessageId {
        /** Identifier of the message sent to the demand owner to confirm the demand creation */
        demandCreationAck,
        /** Identifier of the message sent to CC'ed users to inform about the demand creation */
        demandCreationCpy,
        /** Identifier of the message sent to sale associates located in the demand zone to inform about the new demand */
        demandCreationNot,
        /** Identifier of the message sent to the demand owner to confirm the demand update */
        demandUpdateAck,
        /** Identifier of the message sent to CC'ed users to inform about the demand update */
        demandUpdateCpy,
        /** Identifier of the message sent to the demand owner to confirm the demand cancellation */
        demandCancellationAck,
        /** Identifier of the message sent to CC'ed users to inform about the demand cancellation */
        demandCancellationCpy,
        /** Identifier of the message sent to the demand owner to confirm the demand deletion */
        demandDeletionAck,
        /** Identifier of the message sent to the demand owner to confirm the proposal confirmation */
        proposalConfirmationAck,
        /** Identifier of the message sent to CC'ed users to inform about the proposal confirmation */
        proposalConfirmationCpy,
        /** Identifier of the message sent to the proposal owner to inform about the proposal confirmation */
        proposalConfirmationNot,
        /** Identifier of the message sent to the demand owner to confirm the proposal declination */
        proposalDeclinationAck,
        /** Identifier of the message sent to CC'ed users to inform about the proposal declination */
        proposalDeclinationCpy,
        /** Identifier of the message sent to the demand owner to confirm the demand cancellation */
        demandClosingAck,

        /** Identifier of the message sent to the proposal owner to confirm the proposal creation */
        proposalCreationAck,
        /** Identifier of the message sent to the demand owner to inform about the proposal creation */
        proposalCreationNot,
        /** Identifier of the message sent to the CC'ed users to inform about the proposal creation */
        proposalCreationCpy,
        /** Identifier of the message sent to the proposal owner to confirm the proposal update */
        proposalUpdateAck,
        /** Identifier of the message sent to the demand owner to inform about the proposal update */
        proposalUpdateNot,
        /** Identifier of the message sent to the CC'ed users to inform about the proposal update */
        proposalUpdateCpy,
        /** Identifier of the message sent to the proposal owner to confirm the proposal cancellation */
        proposalCancellationAck,
        /** Identifier of the message sent to the demand owner to inform about the proposal cancellation */
        proposalCancellationNot,
        /** Identifier of the message sent to the CC'ed users to inform about the proposal cancellation */
        proposalCancellationCpy,
        /** Identifier of the message sent to the proposal owner to confirm the proposal deletion */
        proposalDeletionAck
    }

    public static boolean needShortMessage(Source source) {
        return Source.twitter.equals(source) || Source.jabber.equals(source);
    }

    private final static String SHORT_MESSAGE_PREFIX = "short_";
    private final static String LONG_MESSAGE_PREFIX = "long_";

    private final static String DEFAULT_VERTICAL_PREFIX = "core_";
    private final static String GOLF_VERTICAL_PREFIX = "golf_";


    public static String getVerticalPrefix(List<String> hashTags) {
        String identifier = DEFAULT_VERTICAL_PREFIX;
        if (hashTags.contains(RegisteredHashTag.golf.toString())) {
            identifier = GOLF_VERTICAL_PREFIX;
        }
        // Note: other hash tags don't rely on a prefix for now
        return identifier;
    }

    public static String translateMessageId(MessageId identifier) {
        switch(identifier) {
        case demandCreationAck:       return "demand_creation_ackToOwner";
        case demandCreationCpy:       return "demand_creation_ackToCCed";
        case demandCreationNot:       return "demand_creation_associateNotif";
        case demandUpdateAck:         return "demand_update_ackToOwner";
        case demandUpdateCpy:         return "demand_update_ackToCCed";
        case demandCancellationAck:   return "demand_cancellation_ackToOwner";
        case demandCancellationCpy:   return "demand_cancellation_ackToCCed";
        case demandDeletionAck:       return "demand_deletion_ackToOwner";
        case proposalCreationAck:     return "proposal_creation_ackToOwner";
        case proposalCreationNot:     return "proposal_creation_consumerNotif";
        case proposalCreationCpy:     return "proposal_creation_ccedNotif";
        case proposalUpdateAck:       return "proposal_update_ackToOwner";
        case proposalConfirmationAck: return "proposal_confirmation_ackToConsumer";
        case proposalConfirmationCpy: return "proposal_confirmation_ackToCCed";
        case proposalConfirmationNot: return "proposal_confirmation_associateNotif";
        default: throw new IllegalArgumentException("The case for the message identifier '" + identifier.toString() + "' is missing!");
        }
    }

    public static String getMessage(Source source, List<String> hashTags, MessageId identifier, String[] parameters, Locale locale) {
        String prefix = (needShortMessage(source) ? SHORT_MESSAGE_PREFIX : LONG_MESSAGE_PREFIX) + getVerticalPrefix(hashTags);
        String out = LabelExtractor.get(ResourceFileId.fourth, prefix + translateMessageId(identifier), parameters, locale);
        return out;
    }
}
