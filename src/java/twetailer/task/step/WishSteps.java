package twetailer.task.step;

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
import twetailer.InvalidStateException;
import twetailer.ReservedOperationException;
import twetailer.connector.MailConnector;
import twetailer.connector.MessageGenerator;
import twetailer.connector.BaseConnector.Source;
import twetailer.connector.MessageGenerator.MessageId;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Entity;
import twetailer.dto.Location;
import twetailer.dto.RawCommand;
import twetailer.dto.Wish;
import twetailer.dto.Command.QueryPointOfView;
import twetailer.j2ee.BaseRestlet;
import twetailer.j2ee.MaelzelServlet;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.State;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

public class WishSteps extends BaseSteps {

    /**
     * Utility method extracting the Wish information on behalf of the specified owner
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param wishKey Identifier of the wish to retrieve
     * @param ownerKey Identifier of the wishes' owner
     * @param pointOfView Helps deciding which attributes can be exposed
     * @param saleAssociateKey  Identifier of the sale associate who create one of the proposal attached to the retrieved wish, used if <code>pointOfView == saleAssociate</code>
     * @return Identified wish
     *
     * @throws ReservedOperationException If <code>saleAssociatePointOfView</code> is <code>true</code> and if the <code>saleAssociateKey</code> is <code>null</code>,
     *                                    and if the sale associate has proposed nothing to this wish
     * @throws InvalidIdentifierException If the resource retrieval on the back-end fails
     */
    public static Wish getWish(PersistenceManager pm, Long wishKey, Long ownerKey, QueryPointOfView pointOfView, Long saleAssociateKey) throws ReservedOperationException, InvalidIdentifierException {
        Wish output = null;

        if (QueryPointOfView.CONSUMER.equals(pointOfView)) {
            output = getWishOperations().getWish(pm, wishKey, ownerKey);
        }
        else if (QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView)) {
            throw new ReservedOperationException(Action.list, Wish.class.getName());
        }
        else { // if (QueryPointOfView.anonymous.equals(pointOfView)) {
            throw new ReservedOperationException(Action.list, Wish.class.getName());
        }

        return output;
    }

    /**
     * Utility method extracting the selected Wish information on behalf of the specified owner
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param parameters Filters of the selection
     * @param ownerKey Identifier of the wishes' owner
     * @param pointOfView Helps deciding which attributes can be exposed
     * @param saleAssociateKey  Identifier of the sale associate who create one of the proposal attached to the selected wishes, used if <code>pointOfView == saleAssociate</code>
     * @return List of matching wishes, can be empty
     *
     * @throws ReservedOperationException If <code>saleAssociatePointOfView</code> is <code>true</code> and if the <code>saleAssociateKey</code> is <code>null</code>
     * @throws InvalidIdentifierException If the information about the locations for a search in an area is invalid
     * @throws DataSourceException If the resource selection on the back-end fails
     */
    public static List<Wish> getWishes(PersistenceManager pm, JsonObject parameters, Long ownerKey, QueryPointOfView pointOfView, Long saleAssociateKey) throws ReservedOperationException, InvalidIdentifierException, DataSourceException {

        Map<String, Object> queryParameters = prepareQueryForSelection(parameters);
        int maximumResults = parameters.containsKey(BaseRestlet.MAXIMUM_RESULTS_PARAMETER_KEY) ? (int) parameters.getLong(BaseRestlet.MAXIMUM_RESULTS_PARAMETER_KEY) : 0;

        List<Wish> output = null;

        if (QueryPointOfView.CONSUMER.equals(pointOfView)) {
            queryParameters.put(Command.OWNER_KEY, ownerKey);

            output = getWishOperations().getWishes(pm, queryParameters, maximumResults);
        }
        else if (QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView)) {
            throw new ReservedOperationException(Action.list, Wish.class.getName());
        }
        else { // if (QueryPointOfView.anonymous.equals(pointOfView)) {
            List<Location> locations = LocationSteps.getLocations(pm, parameters, true);

            output = getWishOperations().getWishes(pm, queryParameters, locations, maximumResults);
        }

        return output;
    }

    /**
     * Utility method extracting the identifiers of selected Wishes on behalf of the specified owner
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param parameters Filters of the selection
     * @param ownerKey Identifier of the wishes' owner
     * @param pointOfView Helps deciding which attributes can be exposed
     * @param saleAssociateKey  Identifier of the sale associate who create one of the proposal attached to the selected wishes, used if <code>pointOfView == saleAssociate</code>
     * @return List of matching wish identifiers, can be empty
     *
     * @throws ReservedOperationException If <code>saleAssociatePointOfView</code> is <code>true</code> and if the <code>saleAssociateKey</code> is <code>null</code>
     * @throws DataSourceException If the resource selection on the back-end fails
     */
    public static List<Long> getWishKeys(PersistenceManager pm, JsonObject parameters, Long ownerKey, QueryPointOfView pointOfView, Long saleAssociateKey) throws ReservedOperationException, DataSourceException {

        Map<String, Object> queryParameters = prepareQueryForSelection(parameters);
        int maximumResults = parameters.containsKey(BaseRestlet.MAXIMUM_RESULTS_PARAMETER_KEY) ? (int) parameters.getLong(BaseRestlet.MAXIMUM_RESULTS_PARAMETER_KEY) : 0;

        List<Long> output = null;

        if (QueryPointOfView.CONSUMER.equals(pointOfView)) {
            queryParameters.put(Command.OWNER_KEY, ownerKey);

            output = getWishOperations().getWishKeys(pm, queryParameters, maximumResults);
        }
        else if (QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView)) {
            throw new ReservedOperationException(Action.list, Wish.class.getName());
        }
        else { // if (QueryPointOfView.anonymous.equals(pointOfView)) {
            throw new ReservedOperationException(Action.list, Wish.class.getName());
        }

        return output;
    }

    /**
     * Remove nominative information from the Wish record
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param pointOfView Identify the anonymization point of view
     * @param wish Entity to purge
     * @param saleAssociateKey Identifier of the concerned sale associate, used to get his list of proposal keys
     * @return Cleaned up Wish instance
     *
     * @throws DataSourceException If the retrieval of the proposal keys created by the sale associate fails
     */
    public static JsonObject anonymizeWish(PersistenceManager pm, QueryPointOfView pointOfView, JsonObject wish, Long saleAssociateKey) throws DataSourceException {
        List<Long> saleAssociateProposalKeys = QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView) ? ProposalSteps.getProposalKeys(pm, saleAssociateKey) : null;
        return anonymizeWish(pointOfView, wish, saleAssociateKey, saleAssociateProposalKeys);
    }

    /**
     * Remove nominative information from the Wish record
     *
     * @param pointOfView Identify the anonymization point of view
     * @param wish Entity to purge
     * @param saleAssociateKey Identifier of the concerned sale associate
     * @param saleAssociateProposalKeys List of proposal identifiers the sale associate has created
     * @return Cleaned up Wish instance
     */
    protected static JsonObject anonymizeWish(QueryPointOfView pointOfView, JsonObject wish, Long saleAssociateKey,  List<Long> saleAssociateProposalKeys) {

        // Remove owner information
        if (!QueryPointOfView.CONSUMER.equals(pointOfView)) {
            wish.remove(Wish.OWNER_KEY);
        }

        // TODO: control the cancellerKey

        // Remove the references to proposals
        if (QueryPointOfView.CONSUMER.equals(pointOfView)) {
            // No alteration
        }
        else if (QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView)) {
            // No allowed, exception already thrown
        }
        else { // if (QueryPointOfView.ANONYMOUS.equals(pointOfView)) {
            // No alteration
        }

        // Remove information about CC-ed users
        if (! QueryPointOfView.CONSUMER.equals(pointOfView)) {
            wish.remove(Wish.CC);
        }

        return wish;
    }

    /**
     * Remove nominative information from all listed Wish
     *
     * @param pointOfView Identify the anonymization point of view
     * @param wishes List of entities to purge
     * @param saleAssociateKey Identifier of the concerned sale associate
     * @return List of cleaned up Wish instances
     *
     * @throws DataSourceException If the retrieval of the proposal keys created by the sale associate fails
     */
    public static JsonArray anonymizeWishes(QueryPointOfView pointOfView, JsonArray wishes, Long saleAssociateKey) throws DataSourceException {
        List<Long> saleAssociateProposalKeys = QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView) ? ProposalSteps.getProposalKeys(saleAssociateKey) : null;
        int idx = wishes.size();
        while (0 < idx) {
            -- idx;
            anonymizeWish(pointOfView, wishes.getJsonObject(idx), saleAssociateKey, saleAssociateProposalKeys);
        }
        return wishes;
    }

    /**
     * Helper fetching a list of parameters for a query
     *
     * @param parameters bag of parameters proposed by a connector
     * @return Prefetch list of query parameters
     */
    protected static Map<String, Object> prepareQueryForSelection(JsonObject parameters) {
        Map<String, Object> queryFilters = new HashMap<String, Object>();

        // Date fields
        processDateFilter(Entity.MODIFICATION_DATE, parameters, queryFilters);

        // String fields
        processStringFilter(Command.HASH_TAGS, parameters, queryFilters);

        // Special fields
        if (!parameters.containsKey(BaseRestlet.ANY_STATE_PARAMETER_KEY)) {
            queryFilters.put(Demand.STATE_COMMAND_LIST, Boolean.TRUE);
        }

        return queryFilters;
    }

    private static JsonObject lastWishQueryParameters = new GenericJsonObject();
    static {
        lastWishQueryParameters.put(BaseRestlet.MAXIMUM_RESULTS_PARAMETER_KEY, 1);
        lastWishQueryParameters.put(BaseRestlet.ANY_STATE_PARAMETER_KEY, Boolean.TRUE);
    }

    /**
     * Utility method create a wish with the given parameters and triggering the associated workflow steps
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param parameters Parameters produced by the Command line parser or transmitted via the REST API
     * @param owner Consumer who's going to own the created wish
     * @return Just created wish
     *
     * @throws DataSourceException if the retrieval of the last created wish or of the location information fail
     * @throws ClientException if there's an issue with the given parameters
     */
    public static Wish createWish(PersistenceManager pm, JsonObject parameters, Consumer owner) throws DataSourceException, ClientException {

        // Detect given location
        if (!parameters.containsKey(Wish.LOCATION_KEY)) {
            List<Location> locations = LocationSteps.getLocations(pm, parameters, false);
            if (locations != null && 0 < locations.size()) {
                parameters.put(Wish.LOCATION_KEY, locations.get(0).getKey());
            }
        }

        if (!parameters.containsKey(Wish.LOCATION_KEY) || !parameters.containsKey(Wish.RANGE) || !parameters.containsKey(Wish.RANGE_UNIT)) {
            // Inherits some attributes from the last created wish
            List<Wish> lastWishes = getWishes(pm, lastWishQueryParameters, owner.getKey(), QueryPointOfView.CONSUMER, null);
            if (0 < lastWishes.size()) {
                Wish lastWish = lastWishes.get(0);
                if (!parameters.containsKey(Wish.LOCATION_KEY) && lastWish.getLocationKey() != null) { parameters.put(Wish.LOCATION_KEY, lastWish.getLocationKey()); }
                if (!parameters.containsKey(Wish.RANGE) && lastWish.getRange() != null)              { parameters.put(Wish.RANGE, lastWish.getRange()); }
                if (!parameters.containsKey(Wish.RANGE_UNIT) && lastWish.getRangeUnit() != null)     { parameters.put(Wish.RANGE_UNIT, lastWish.getRangeUnit()); }
            }

            // Fall back on the Consumer's location
            if (!parameters.containsKey(Wish.LOCATION_KEY) && owner.getLocationKey() != null) {
                parameters.put(Wish.LOCATION_KEY, owner.getLocationKey());
            }
        }

        Wish wish = getWishOperations().createWish(pm, parameters, owner.getKey());

        // Related workflow step
        MaelzelServlet.triggerValidationTask(wish);

        return wish;
    }

    /**
     * Utility method update a wish with the given parameters and triggering the associated workflow steps
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param rawCommand Reference of the command which initiated the process, is <code>null</code> if initiated by a REST API call
     * @param wishKey Resource identifier
     * @param parameters Parameters produced by the Command line parser or transmitted via the REST API
     * @param owner Consumer who owns the wish to be updated
     * @return Just updated wish
     *
     * @throws DataSourceException if the retrieval of the last created wish or of the location information fail
     * @throws InvalidIdentifierException if there's an issue with the Wish identifier is invalid
     * @throws InvalidStateException if the Wish is not update-able
     * @throws CommunicationException if the notification of a successful closing fails
     */
    public static Wish updateWish(PersistenceManager pm, RawCommand rawCommand, Long wishKey, JsonObject parameters, Consumer owner) throws DataSourceException, InvalidIdentifierException, InvalidStateException, CommunicationException {

        Wish wish = getWishOperations().getWish(pm, wishKey, owner.getKey());
        State currentState = wish.getState();

        // Workflow state change
        if (parameters.size() == 1 && parameters.containsKey(Command.STATE)) {
            String proposedState = parameters.getString(Command.STATE);

            // Cancel
            if (!State.expired.equals(currentState) && State.cancelled.toString().equals(proposedState)) {
                wish.setCancelerKey(owner.getKey());

                // Confirm the wish canceling to the owner
                if (rawCommand != null) {
                    Locale locale = owner.getLocale();

                    MessageGenerator msgGen = new MessageGenerator(rawCommand.getSource(), wish.getHashTags(), locale);
                    msgGen.
                        put("wish>owner>name", owner.getName()).
                        fetch(wish).
                        put("message>footer", msgGen.getAlternateMessage(MessageId.messageFooter));

                    String subject = null;
                    if (Source.mail.equals(msgGen.getCommunicationChannel())) {
                        subject = rawCommand.getSubject();
                    }
                    if (subject == null) {
                        subject = msgGen.getAlternateMessage(MessageId.messageSubject, msgGen.getParameters());
                    }
                    subject = MailConnector.prepareSubjectAsResponse(subject, locale);

                    communicateToConsumer(
                            msgGen.getCommunicationChannel(),
                            subject,
                            owner,
                            new String[] { msgGen.getMessage(MessageId.DEMAND_CANCELLATION_OK_TO_CONSUMER) }
                    );
                }
            }
            else {
                throw new InvalidStateException("Invalid state change attempt to: " + proposedState, currentState.toString(), proposedState);
            }

            wish.setState(proposedState);
            wish = getWishOperations().updateWish(pm, wish);
        }
        // Normal attribute update
        else if (State.opened.equals(currentState) || State.published.equals(currentState) || State.invalid.equals(currentState)) {
            // Detect new location
            try {
                List<Location> locations = LocationSteps.getLocations(pm, parameters, false);
                if (locations != null && 0 < locations.size()) {
                    Location location = locations.get(0);
                    if (!location.getKey().equals(wish.getLocationKey())) {
                        wish.setLocationKey(location.getKey());
                    }
                }
            }
            catch(InvalidIdentifierException ex) {
                // Reset location reference!
                wish.setLocationKey(null);
            }

            // Integrate updates
            wish.fromJson(parameters);

            // Prepare as a new Wish
            wish.setState(State.opened); // Will force the re-validation of the entire wish

            // Persist updates
            wish = getWishOperations().updateWish(pm, wish);

            // Related workflow step
            MaelzelServlet.triggerValidationTask(wish);
        }
        else {
            throw new InvalidStateException("Entity not in modifiable state", currentState.toString(), null);
        }

        return wish;
    }

    /**
     * Utility method deleting the identified wish
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param wishKey Resource identifier
     * @param owner Consumer who owns the wish to delete
     *
     * @throws DataSourceException if the retrieval of the last created wish or of the location information fail
     * @throws InvalidIdentifierException if there's an issue with the Wish identifier is invalid
     * @throws InvalidStateException if the Wish is not already cancelled
     */
    public static void deleteWish(PersistenceManager pm, Long wishKey, Consumer owner) throws DataSourceException, InvalidIdentifierException, InvalidStateException {

        Wish wish = getWishOperations().getWish(pm, wishKey, owner.getKey());

        State currentState = wish.getState();
        if (!State.cancelled.equals(currentState)) {
            throw new InvalidStateException("Invalid state change attempt to: " + State.markedForDeletion.toString(), currentState.toString(), State.markedForDeletion.toString());
        }

        wish.setState(State.markedForDeletion);
        wish.setMarkedForDeletion(Boolean.TRUE);
        getWishOperations().updateWish(pm, wish);
    }
}
