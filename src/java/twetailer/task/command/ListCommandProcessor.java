package twetailer.task.command;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;
import static twetailer.connector.BaseConnector.communicateToConsumer;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.ReservedOperationException;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.Proposal;
import twetailer.dto.RawCommand;
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

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

import domderrien.i18n.LabelExtractor;
import domderrien.i18n.StringUtils;
import domderrien.jsontools.JsonObject;

public class ListCommandProcessor {

    public static void processListCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command, JsonObject prefixes, JsonObject actions) throws DataSourceException, ClientException {

        Locale locale = consumer.getLocale();

        // Get identified Demand
        if (command.containsKey(Demand.REFERENCE)) {
            Long entityKey = command.getLong(Demand.REFERENCE);
            if (!Long.valueOf(-1L).equals(entityKey)) {
                QueryPointOfView pointOfView = QueryPointOfView.fromJson(command, QueryPointOfView.CONSUMER);
                String message = null;
                try {
                    Long ownerKey = consumer.getKey();
                    Long saleAssociateKey = QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView) ? CommandProcessor.retrieveSaleAssociate(pm, consumer, Action.list, Demand.class.getName()).getKey() : null;
                    Demand demand = DemandSteps.getDemand(pm, entityKey, ownerKey, pointOfView, saleAssociateKey);
                    // Echo back the specified demand
                    Location location = LocationSteps.getLocation(pm, demand);
                    boolean anonymize = !QueryPointOfView.CONSUMER.equals(pointOfView);
                    message = CommandProcessor.generateTweet(demand, location, anonymize, locale);
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
                url("/_admin/maelzel/validateLocation").
                    param(Location.POSTAL_CODE, postalCode).
                    param(Location.COUNTRY_CODE, countryCode).
                    param(Consumer.CONSUMER_KEY, consumer.getKey().toString()).
                    param(Command.KEY, rawCommand.getKey().toString()).
                    method(Method.GET)
        );
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
                String truncatedTag = StringUtils.toUnicode((String) tag);
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
}
