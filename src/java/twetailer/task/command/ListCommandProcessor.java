package twetailer.task.command;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;
import static twetailer.connector.BaseConnector.communicateToConsumer;

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.CommunicationException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.ReservedOperationException;
import twetailer.connector.BaseConnector;
import twetailer.connector.MailConnector;
import twetailer.connector.MessageGenerator;
import twetailer.connector.BaseConnector.Source;
import twetailer.connector.MessageGenerator.MessageId;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Influencer;
import twetailer.dto.Location;
import twetailer.dto.Proposal;
import twetailer.dto.RawCommand;
import twetailer.dto.Registrar;
import twetailer.dto.SaleAssociate;
import twetailer.dto.Store;
import twetailer.dto.Command.QueryPointOfView;
import twetailer.task.CommandProcessor;
import twetailer.task.step.BaseSteps;
import twetailer.task.step.DemandSteps;
import twetailer.task.step.LocationSteps;
import twetailer.task.step.ProposalSteps;
import twetailer.task.step.StoreSteps;
import twetailer.validator.LocaleValidator;
import twetailer.validator.CommandSettings.Action;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.TaskOptions.Method;

import domderrien.i18n.LabelExtractor;
import domderrien.i18n.StringUtils;
import domderrien.i18n.LabelExtractor.ResourceFileId;
import domderrien.jsontools.JsonObject;

public class ListCommandProcessor {

    @SuppressWarnings("deprecation")
    public static void processListCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command, JsonObject prefixes, JsonObject actions) throws DataSourceException, ClientException {

        Locale locale = consumer.getLocale();

        // Get identified Demand
        if (command.containsKey(Demand.REFERENCE) || command.containsKey(Demand.DEMAND_KEY)) {
            Long entityKey = command.containsKey(Demand.REFERENCE) ? command.getLong(Demand.REFERENCE) : command.getLong(Demand.DEMAND_KEY);
            if (!Long.valueOf(-1L).equals(entityKey)) {
                QueryPointOfView pointOfView = QueryPointOfView.fromJson(command, QueryPointOfView.CONSUMER);
                String message = null;
                try {
                    Long ownerKey = consumer.getKey();
                    if (QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView)) {
                        // FIXME: remove this lock! for now only demands' owner can list them
                        throw new ClientException("Not yet implemented for Query by Associates!");
                    }
                    Long saleAssociateKey = QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView) ? CommandProcessor.retrieveSaleAssociate(pm, consumer, Action.list, Demand.class.getName()).getKey() : null;
                    Demand demand = DemandSteps.getDemand(pm, entityKey, ownerKey, pointOfView, saleAssociateKey);
                    Location location = LocationSteps.getLocation(pm, demand);
                    Influencer influencer = BaseSteps.getInfluencerOperations().getInfluencer(pm, demand.getInfluencerKey());

                    Map<Long, Proposal> proposals = new HashMap<Long, Proposal>();
                    Map<Long, Store> stores = new HashMap<Long, Store>();
                    Map<Long, Registrar> registrars = new HashMap<Long, Registrar>();
                    List<Long> proposalKeys = demand.getProposalKeys();
                    if (proposalKeys != null && 0 < proposalKeys.size()) {
                        List<Long> storeKeys = new ArrayList<Long>();
                        for (Proposal proposal: BaseSteps.getProposalOperations().getProposals(pm, proposalKeys)) {
                            // FIXME: filter out proposals not owned by the associate if pointOfView == SALE_ASSOCIATE
                            proposals.put(proposal.getKey(), proposal);
                            if(!storeKeys.contains(proposal.getStoreKey())) {
                                storeKeys.add(proposal.getStoreKey());
                            }
                        }
                        List<Long> registrarKeys = new ArrayList<Long>();
                        for (Store store: BaseSteps.getStoreOperations().getStores(pm, storeKeys)) {
                            stores.put(store.getKey(), store);
                            if(!registrarKeys.contains(store.getRegistrarKey())) {
                                registrarKeys.add(store.getRegistrarKey());
                            }
                        }
                        for (Registrar registrar: BaseSteps.getRegistrarOperations().getRegistrars(pm, registrarKeys)) {
                            registrars.put(registrar.getKey(), registrar);
                        }
                    }

                    // Echo back the specified demand
                    publishDemandDetails(consumer, rawCommand, demand, location, influencer, proposals, stores, registrars);
                }
                catch(InvalidIdentifierException ex) {
                    message = LabelExtractor.get("cp_command_list_invalid_demand_id", locale);
                }
                catch(ReservedOperationException ex) {
                    message = LabelExtractor.get("cp_command_parser_reserved_action", new String[] { ex.getAction().toString() }, locale);
                }
                communicateToConsumer(
                        rawCommand.getSource(),
                        rawCommand.getSubject(),
                        consumer,
                        new String[] { message }
                );
                return;
            }
        }

        // Get identified Proposal
        if (command.containsKey(Proposal.PROPOSAL_KEY)) {
            Long entityKey = command.getLong(Proposal.PROPOSAL_KEY);
            if (!Long.valueOf(-1L).equals(entityKey)) {
                QueryPointOfView pointOfView = QueryPointOfView.fromJson(command, QueryPointOfView.SALE_ASSOCIATE);
                String message = null;
                try {
                    Long ownerKey = consumer.getKey();
                    Long saleAssociateKey = null;
                    Long storeKey = null;
                    if (QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView)) {
                        SaleAssociate saleAssociate = CommandProcessor.retrieveSaleAssociate(pm, consumer, Action.list, Proposal.class.getName());
                        saleAssociateKey = saleAssociate.getKey();
                        storeKey = saleAssociate.getStoreKey();
                    }
                    Proposal proposal = ProposalSteps.getProposal(pm, entityKey, ownerKey, pointOfView, saleAssociateKey, storeKey);
                    // Echo back the specified proposal
                    Store store = StoreSteps.getStore(pm, proposal.getStoreKey());
                    boolean anonymize = !QueryPointOfView.CONSUMER.equals(pointOfView);
                    message = CommandProcessor.generateTweet(proposal, store, anonymize, locale);
                }
                catch(InvalidIdentifierException ex) {
                    message = LabelExtractor.get("cp_command_list_invalid_proposal_id", locale);
                }
                catch(ReservedOperationException ex) {
                    message = LabelExtractor.get("cp_command_parser_reserved_action", new String[] { ex.getAction().toString() }, locale);
                }
                communicateToConsumer(
                        rawCommand.getSource(),
                        rawCommand.getSubject(),
                        consumer,
                        new String[] { message }
                );
                return;
            }
        }

        // Get identified Store
        if (command.containsKey(Store.STORE_KEY)) {
            Long entityKey = command.getLong(Store.STORE_KEY);
            if (!Long.valueOf(-1L).equals(entityKey)) {
                String message = null;
                try {
                    Store store = StoreSteps.getStore(pm, entityKey);
                    // Echo back the specified demand
                    Location location = LocationSteps.getLocation(pm, store);
                    message = CommandProcessor.generateTweet(store, location, locale);
                }
                catch(InvalidIdentifierException ex) {
                    message = LabelExtractor.get("cp_command_list_invalid_store_id", locale);
                }
                communicateToConsumer(
                        rawCommand.getSource(),
                        rawCommand.getSubject(),
                        consumer,
                        new String[] { message }
                );
                return;
            }
        }

        // Get the location and range values for the Anonymous access or a Store listing
        List<Location> locationsAround = null;
        String localePart = null, rangePart = null;
        QueryPointOfView pointOfView = QueryPointOfView.fromJson(command, QueryPointOfView.CONSUMER);
        boolean anonymousAccess = QueryPointOfView.ANONYMOUS.equals(pointOfView);
        if (anonymousAccess || command.containsKey(Store.STORE_KEY)) {
            // Extract the range value
            Double range = LocaleValidator.DEFAULT_RANGE;
            String rangeUnit = LocaleValidator.DEFAULT_RANGE_UNIT;
            if (command.containsKey(Demand.RANGE)) { range = command.getDouble(Demand.RANGE); }
            if (command.containsKey(Demand.RANGE_UNIT)) { rangeUnit = command.getString(Demand.RANGE_UNIT); }
            rangePart = LabelExtractor.get("cp_tweet_range_part", new Object[] { range, rangeUnit }, locale);

            // Get the center
            List<Location> candidates = LocationSteps.getLocations(pm, command, false);
            if (candidates != null && 0 < candidates.size()) {
                Location center = candidates.get(0);
                if (center.getLatitude() == Location.INVALID_COORDINATE) {
                    reportInvalidLocation(consumer, rawCommand, center);
                    return;
                }
                localePart = LabelExtractor.get("cp_tweet_locale_part", new Object[] { center.getPostalCode(), center.getCountryCode() }, locale);

                // Get the location around the center
                command.put(Demand.RANGE, range);
                command.put(Demand.RANGE_UNIT, rangeUnit);
                command.put(Location.LATITUDE, center.getLatitude());
                command.put(Location.LONGITUDE, center.getLongitude());
                command.put(Location.COUNTRY_CODE, center.getCountryCode());
                locationsAround = LocationSteps.getLocations(pm, command, true);
            }
        }

        // Get selected Stores
        if (command.containsKey(Store.STORE_KEY)) {
            if (locationsAround == null) {
                String messageForMissingLocation = LabelExtractor.get(
                        "cp_command_list_store_missing_location",
                        new Object[] { LabelExtractor.get( "cp_tweet_store_reference_part", new Object[] { "*" }, locale) },
                        locale
                );
                communicateToConsumer(
                        rawCommand.getSource(),
                        rawCommand.getSubject(),
                        consumer,
                        new String[] { messageForMissingLocation }
                );
                return;
            }

            // Get the stores
            List<Store> stores = StoreSteps.getStores(pm, command);

            // Prepare the messages
            List<String> messages = new ArrayList<String>();
            if (stores.size() == 0) {
                messages.add(LabelExtractor.get("cp_command_list_no_store_in_location", new Object[] { localePart, rangePart }, locale));
            }
            else {
                messages.add(LabelExtractor.get("cp_command_list_store_series_introduction", new Object[] { stores.size(), localePart, rangePart }, locale));
                for (Store store: stores) {
                    messages.add(CommandProcessor.generateTweet(store, LocationSteps.getLocation(pm, store), locale));
                }
            }
            communicateToConsumer(
                    rawCommand.getSource(),
                    rawCommand.getSubject(),
                    consumer,
                    messages.toArray(new String[0])
            );
            return;
        }

        // Get selected Proposals
        if (command.containsKey(Proposal.PROPOSAL_KEY)) {
            if (anonymousAccess && locationsAround == null) {
                String messageForMissingLocation = LabelExtractor.get(
                        "cp_command_list_proposal_missing_location",
                        new Object[] { LabelExtractor.get( "cp_tweet_proposal_reference_part", new Object[] { "*" }, locale) },
                        locale
                );
                communicateToConsumer(
                        rawCommand.getSource(),
                        rawCommand.getSubject(),
                        consumer,
                        new String[] { messageForMissingLocation }
                );
                return;
            }

            // Get the sale associate identifier if needed
            Long saleAssociateKey = null;
            if (QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView)) {
                SaleAssociate saleAssociate = CommandProcessor.retrieveSaleAssociate(pm, consumer, Action.list, Proposal.class.getName());
                saleAssociateKey = saleAssociate.getKey();
            }

            // Get the proposals
            List<Proposal> proposals = ProposalSteps.getProposals(pm, command, consumer.getKey(), pointOfView, saleAssociateKey);

            // Prepare the messages
            List<String> messages = new ArrayList<String>();
            if (proposals.size() == 0) {
                String message = locationsAround != null ? "cp_command_list_no_proposal_in_location" : "cp_command_list_no_active_proposal";
                messages.add(LabelExtractor.get(message, new Object[] { localePart, rangePart }, locale));
            }
            else {
                String message = locationsAround != null ? "cp_command_list_proposal_series_introduction" : "cp_command_list_personal_proposal_series_introduction";
                messages.add(LabelExtractor.get(message, new Object[] { proposals.size(), localePart, rangePart }, locale));
                boolean anonymize = !QueryPointOfView.CONSUMER.equals(pointOfView);
                for (Proposal proposal: proposals) {
                    messages.add(CommandProcessor.generateTweet(proposal, StoreSteps.getStore(pm, proposal.getStoreKey()), anonymize, locale));
                }
            }
            communicateToConsumer(
                    rawCommand.getSource(),
                    rawCommand.getSubject(),
                    consumer,
                    messages.toArray(new String[0])
            );
            return;
        }

        // Get selected Demands (default)
        if (anonymousAccess && locationsAround == null) {
            String messageForMissingLocation = LabelExtractor.get(
                    "cp_command_list_demand_missing_location",
                    new Object[] { LabelExtractor.get( "cp_tweet_demand_reference_part", new Object[] { "*" }, locale) },
                    locale
            );
            communicateToConsumer(
                    rawCommand.getSource(),
                    rawCommand.getSubject(),
                    consumer,
                    new String[] { messageForMissingLocation }
            );
            return;
        }

        // Get the sale associate identifier if needed
        Long saleAssociateKey = null;
        if (QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView)) {
            SaleAssociate saleAssociate = CommandProcessor.retrieveSaleAssociate(pm, consumer, Action.list, Proposal.class.getName());
            saleAssociateKey = saleAssociate.getKey();
        }

        // Get the demands
        List<Demand> demands = DemandSteps.getDemands(pm, command, consumer.getKey(), pointOfView, saleAssociateKey);

        // Prepare the messages
        List<String> messages = new ArrayList<String>();
        if (demands.size() == 0) {
            String message = locationsAround != null ? "cp_command_list_no_demand_in_location" : "cp_command_list_no_active_demand";
            messages.add(LabelExtractor.get(message, new Object[] { localePart, rangePart }, locale));
        }
        else {
            String message = locationsAround != null ? "cp_command_list_demand_series_introduction" : "cp_command_list_personal_demand_series_introduction";
            messages.add(LabelExtractor.get(message, new Object[] { demands.size(), localePart, rangePart }, locale));
            boolean anonymize = !QueryPointOfView.CONSUMER.equals(pointOfView);
            for (Demand demand: demands) {
                messages.add(CommandProcessor.generateTweet(demand, LocationSteps.getLocation(pm, demand), anonymize, locale));
            }
        }
        communicateToConsumer(
                rawCommand.getSource(),
                rawCommand.getSubject(),
                consumer,
                messages.toArray(new String[0])
        );
    }

    protected static void reportInvalidLocation(Consumer consumer, RawCommand rawCommand, Location location) throws DataSourceException, ClientException {
        String postalCode = location.getPostalCode();
        String countryCode = location.getCountryCode();

        Locale locale = consumer.getLocale();

        if (LocaleValidator.DEFAULT_POSTAL_CODE_CA.equals(location.getPostalCode()) ||
            LocaleValidator.DEFAULT_POSTAL_CODE_US.equals(location.getPostalCode()) ||
            LocaleValidator.DEFAULT_POSTAL_CODE_ALT_US.equals(location.getPostalCode())
        ) {
            // Inform the user about the invalid location
            String message = LabelExtractor.get("cp_command_list_invalid_location", new String[] { location.getPostalCode(), location.getCountryCode() }, locale);
            communicateToConsumer(
                    rawCommand.getSource(),
                    rawCommand.getSubject(),
                    consumer,
                    new String[] { message }
            );
        }
        else {
            // Inform the user about a possible delay
            String localePart = LabelExtractor.get("cp_tweet_locale_part", new Object[] { postalCode, countryCode }, locale);
            String message = LabelExtractor.get("cp_command_list_with_new_location", new String[] { localePart }, locale);
            communicateToConsumer(
                    rawCommand.getSource(),
                    rawCommand.getSubject(),
                    consumer,
                    new String[] { message }
            );

            // Schedule the validation task that will resolve the location geo-coordinates
            Queue queue = BaseSteps.getBaseOperations().getQueue();
            queue.add(
                    withUrl("/_tasks/validateLocation").
                        param(Location.POSTAL_CODE, postalCode).
                        param(Location.COUNTRY_CODE, countryCode).
                        param(Consumer.CONSUMER_KEY, consumer.getKey().toString()).
                        param(Command.KEY, rawCommand.getKey().toString()).
                        method(Method.GET)
            );
        }
    }

    /**
     * Return <code>true</code> if one of the given criterion matches one of the given tags.
     * If a tag ends with a star (*), each criterion is only compared to the corresponding version.
     *
     * @param collator Java object used for the locale dependent comparisons
     * @param tags List of filters (can end with a star) to compare to the criteria
     * @param criteria List of criteria to compare to the tags
     * @return <code>true</code> if one criterion matches one tag, <code>false</code> otherwise
     */
    protected static boolean checkIfIncluded(Collator collator, List<Object> tags, List<String> criteria) {
        for(Object tag: tags) {
            if (((String) tag).endsWith("*")) {
                // Compare the filter to the beginning of each demand criteria
                String truncatedTag = StringUtils.toUnicode(((String) tag).getBytes()); // TODO: I don't think it does make a difference to convert the tags here!
                int usefulLength = truncatedTag.length() - 1;
                truncatedTag = truncatedTag.substring(0, usefulLength);
                for (String criterion: criteria) {
                    if (truncatedTag.length() <= criterion.length() && collator.compare(criterion.substring(0, usefulLength), truncatedTag) == 0) {
                        return true;
                    }
                }
            }
            else {
                // Compare the filter to each demand criteria
                for (String criterion: criteria) {
                    if (collator.compare(criterion, (String) tag) == 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Helper composing a MessageGenerator instance to list the Demand & related objects' attributes, plus list all attached Proposals & related attributes
     *
     * @param interlocutor Demand owner who issued the !list command
     * @param rawCommand RawCommand used to get the mail subject of the initial message
     * @param demand Object to print
     * @param location Demand's location
     * @param influencer Demand's influencer
     * @param proposals Proposals to print
     * @param stores Proposals' associated stores
     * @param registrars Stores' associated registrars
     *
     * @throws CommunicationException If the sending of the command response fails.
     */
    protected static void publishDemandDetails(Consumer interlocutor, RawCommand rawCommand, Demand demand, Location location, Influencer influencer, Map<Long, Proposal> proposals, Map<Long, Store> stores, Map<Long, Registrar> registrars) throws CommunicationException {
        Locale locale = interlocutor.getLocale();
        MessageGenerator msgGen = new MessageGenerator(rawCommand.getSource(), demand.getHashTags(), locale);
        msgGen.
            put("demand>owner>name", interlocutor.getName()).
            fetch(demand).
            fetch(location, "demand").
            fetch(influencer).
            put("message>footer", msgGen.getAlternateMessage(MessageId.messageFooter)).
            put("command>footer", LabelExtractor.get(ResourceFileId.fourth, "command_message_footer", locale));

        String cancelDemand = LabelExtractor.get(ResourceFileId.fourth, "command_message_body_demand_cancel", msgGen.getParameters(), locale);
        String updateDemand = LabelExtractor.get(ResourceFileId.fourth, "command_message_body_demand_update", msgGen.getParameters(), locale);
        String subject = null;
        if (Source.mail.equals(msgGen.getCommunicationChannel())) {
            subject = rawCommand.getSubject();
        }
        if (subject == null) {
            subject = msgGen.getAlternateMessage(MessageId.messageSubject, msgGen.getParameters());
        }
        subject = MailConnector.prepareSubjectAsResponse(subject, locale);

        msgGen.
            put("command>threadSubject", BaseConnector.prepareMailToSubject(subject)).
            put("command>cancelDemand", BaseConnector.prepareMailToBody(cancelDemand)).
            put("command>updateDemand", BaseConnector.prepareMailToBody(updateDemand));

        MessageGenerator innerMsgGen = new MessageGenerator(rawCommand.getSource(), demand.getHashTags(), locale);
        ArrayList<String> innerMessages = new ArrayList<String> (demand.getProposalKeys().size());

        Long place = 1L;
        for(Long proposalKey: demand.getProposalKeys()) {
            innerMsgGen.
                put("proposal>orderInList", place).
                fetch(proposals.get(proposalKey)).
                fetch(stores.get(proposals.get(proposalKey).getStoreKey())).
                fetch(registrars.get(stores.get(proposals.get(proposalKey).getStoreKey()).getRegistrarKey()));

            String rateProposal1 = LabelExtractor.get(ResourceFileId.fourth, "command_message_body_proposal_rate1", innerMsgGen.getParameters(), locale);
            String rateProposal2 = LabelExtractor.get(ResourceFileId.fourth, "command_message_body_proposal_rate2", innerMsgGen.getParameters(), locale);
            String rateProposal3 = LabelExtractor.get(ResourceFileId.fourth, "command_message_body_proposal_rate3", innerMsgGen.getParameters(), locale);
            String confirmProposal = LabelExtractor.get(ResourceFileId.fourth, "command_message_body_proposal_confirm", innerMsgGen.getParameters(), locale);
            String declineProposal = LabelExtractor.get(ResourceFileId.fourth, "command_message_body_proposal_decline", innerMsgGen.getParameters(), locale);

            innerMsgGen.
                put("command>threadSubject", BaseConnector.prepareMailToSubject(subject)).
                put("command>rateProposal1", BaseConnector.prepareMailToBody(rateProposal1)).
                put("command>rateProposal2", BaseConnector.prepareMailToBody(rateProposal2)).
                put("command>rateProposal3", BaseConnector.prepareMailToBody(rateProposal3)).
                put("command>confirmProposal", BaseConnector.prepareMailToBody(confirmProposal)).
                put("command>declineProposal", BaseConnector.prepareMailToBody(declineProposal));

            innerMessages.add(innerMsgGen.getMessage(MessageId.LIST_PROPOSALS_OF_ONE_DEMAND_TO_OWNER));

            place ++;
        }
        msgGen.put("demand>proposalList", innerMessages);

        String message = msgGen.getMessage(MessageId.LIST_ONE_DEMAND_TO_OWNER);

        communicateToConsumer(
                msgGen.getCommunicationChannel(),
                subject,
                interlocutor,
                new String[] { message }
        );
    }
}
