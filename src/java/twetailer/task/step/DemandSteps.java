package twetailer.task.step;

import static twetailer.connector.BaseConnector.communicateToConsumer;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
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
import twetailer.connector.BaseConnector;
import twetailer.connector.MailConnector;
import twetailer.connector.MessageGenerator;
import twetailer.connector.BaseConnector.Source;
import twetailer.connector.MessageGenerator.MessageId;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Entity;
import twetailer.dto.Location;
import twetailer.dto.Proposal;
import twetailer.dto.RawCommand;
import twetailer.dto.SaleAssociate;
import twetailer.dto.Store;
import twetailer.dto.Command.QueryPointOfView;
import twetailer.j2ee.BaseRestlet;
import twetailer.j2ee.MaelzelServlet;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.State;
import domderrien.i18n.DateUtils;
import domderrien.i18n.LabelExtractor;
import domderrien.i18n.LabelExtractor.ResourceFileId;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

public class DemandSteps extends BaseSteps {

    /**
     * Utility method extracting the Demand information on behalf of the specified owner
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param demandKey Identifier of the demand to retrieve
     * @param ownerKey Identifier of the demands' owner
     * @param pointOfView Helps deciding which attributes can be exposed
     * @param saleAssociateKey  Identifier of the sale associate who create one of the proposal attached to the retrieved demand, used if <code>pointOfView == saleAssociate</code>
     * @return Identified demand
     *
     * @throws ReservedOperationException If <code>saleAssociatePointOfView</code> is <code>true</code> and if the <code>saleAssociateKey</code> is <code>null</code>,
     *                                    and if the sale associate has proposed nothing to this demand
     * @throws InvalidIdentifierException If the resource retrieval on the back-end fails
     */
    public static Demand getDemand(PersistenceManager pm, Long demandKey, Long ownerKey, QueryPointOfView pointOfView, Long saleAssociateKey) throws ReservedOperationException, InvalidIdentifierException {
        Demand output = null;

        if (QueryPointOfView.CONSUMER.equals(pointOfView)) {
            output = getDemandOperations().getDemand(pm, demandKey, ownerKey);
        }
        else if (QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView)) {
            if (saleAssociateKey == null) {
                throw new ReservedOperationException(Action.list, Demand.class.getName());
            }
            output = getDemandOperations().getDemand(pm, demandKey, ownerKey);
            if (!output.getSaleAssociateKeys().contains(saleAssociateKey)) {
                throw new ReservedOperationException(Action.list, Demand.class.getName());
            }
        }
        else { // if (QueryPointOfView.anonymous.equals(pointOfView)) {
            throw new ReservedOperationException(Action.list, Demand.class.getName());
        }

        return output;
    }

    /**
     * Utility method extracting the selected Demand information on behalf of the specified owner
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param parameters Filters of the selection
     * @param ownerKey Identifier of the demands' owner
     * @param pointOfView Helps deciding which attributes can be exposed
     * @param saleAssociateKey  Identifier of the sale associate who create one of the proposal attached to the selected demands, used if <code>pointOfView == saleAssociate</code>
     * @return List of matching demands, can be empty
     *
     * @throws ReservedOperationException If <code>saleAssociatePointOfView</code> is <code>true</code> and if the <code>saleAssociateKey</code> is <code>null</code>
     * @throws InvalidIdentifierException If the information about the locations for a search in an area is invalid
     * @throws DataSourceException If the resource selection on the back-end fails
     */
    public static List<Demand> getDemands(PersistenceManager pm, JsonObject parameters, Long ownerKey, QueryPointOfView pointOfView, Long saleAssociateKey) throws ReservedOperationException, InvalidIdentifierException, DataSourceException {

        Map<String, Object> queryParameters = prepareQueryForSelection(parameters);
        int maximumResults = parameters.containsKey(BaseRestlet.MAXIMUM_RESULTS_PARAMETER_KEY) ? (int) parameters.getLong(BaseRestlet.MAXIMUM_RESULTS_PARAMETER_KEY) : 0;

        List<Demand> output = null;

        if (QueryPointOfView.CONSUMER.equals(pointOfView)) {
            queryParameters.put(Command.OWNER_KEY, ownerKey);

            output = getDemandOperations().getDemands(pm, queryParameters, maximumResults);
        }
        else if (QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView)) {
            if (saleAssociateKey == null) {
                throw new ReservedOperationException(Action.list, Demand.class.getName());
            }
            queryParameters.put(Demand.SALE_ASSOCIATE_KEYS, saleAssociateKey);

            output = getDemandOperations().getDemands(pm, queryParameters, maximumResults);
        }
        else { // if (QueryPointOfView.anonymous.equals(pointOfView)) {
            List<Location> locations = LocationSteps.getLocations(pm, parameters, true);

            output = getDemandOperations().getDemands(pm, queryParameters, locations, maximumResults);
        }

        return output;
    }

    /**
     * Utility method extracting the identifiers of selected Demands on behalf of the specified owner
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param parameters Filters of the selection
     * @param ownerKey Identifier of the demands' owner
     * @param pointOfView Helps deciding which attributes can be exposed
     * @param saleAssociateKey  Identifier of the sale associate who create one of the proposal attached to the selected demands, used if <code>pointOfView == saleAssociate</code>
     * @return List of matching demand identifiers, can be empty
     *
     * @throws ReservedOperationException If <code>saleAssociatePointOfView</code> is <code>true</code> and if the <code>saleAssociateKey</code> is <code>null</code>
     * @throws DataSourceException If the resource selection on the back-end fails
     */
    public static List<Long> getDemandKeys(PersistenceManager pm, JsonObject parameters, Long ownerKey, QueryPointOfView pointOfView, Long saleAssociateKey) throws ReservedOperationException, DataSourceException {

        Map<String, Object> queryParameters = prepareQueryForSelection(parameters);
        int maximumResults = parameters.containsKey(BaseRestlet.MAXIMUM_RESULTS_PARAMETER_KEY) ? (int) parameters.getLong(BaseRestlet.MAXIMUM_RESULTS_PARAMETER_KEY) : 0;

        List<Long> output = null;

        if (QueryPointOfView.CONSUMER.equals(pointOfView)) {
            queryParameters.put(Command.OWNER_KEY, ownerKey);

            output = getDemandOperations().getDemandKeys(pm, queryParameters, maximumResults);
        }
        else if (QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView)) {
            if (saleAssociateKey == null) {
                throw new ReservedOperationException(Action.list, Demand.class.getName());
            }
            queryParameters.put(Demand.SALE_ASSOCIATE_KEYS, saleAssociateKey);

            output = getDemandOperations().getDemandKeys(pm, queryParameters, maximumResults);
        }
        else { // if (QueryPointOfView.anonymous.equals(pointOfView)) {
            throw new ReservedOperationException(Action.list, Demand.class.getName());
        }

        return output;
    }

    /**
     * Remove nominative information from the Demand record
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param pointOfView Identify the anonymization point of view
     * @param demand Entity to purge
     * @param saleAssociateKey Identifier of the concerned sale associate, used to get his list of proposal keys
     * @return Cleaned up Demand instance
     *
     * @throws DataSourceException If the retrieval of the proposal keys created by the sale associate fails
     */
    public static JsonObject anonymizeDemand(PersistenceManager pm, QueryPointOfView pointOfView, JsonObject demand, Long saleAssociateKey) throws DataSourceException {
        List<Long> saleAssociateProposalKeys = QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView) ? ProposalSteps.getProposalKeys(pm, saleAssociateKey) : null;
        return anonymizeDemand(pointOfView, demand, saleAssociateKey, saleAssociateProposalKeys);
    }

    /**
     * Remove nominative information from the Demand record
     *
     * @param pointOfView Identify the anonymization point of view
     * @param demand Entity to purge
     * @param saleAssociateKey Identifier of the concerned sale associate
     * @param saleAssociateProposalKeys List of proposal identifiers the sale associate has created
     * @return Cleaned up Demand instance
     */
    protected static JsonObject anonymizeDemand(QueryPointOfView pointOfView, JsonObject demand, Long saleAssociateKey,  List<Long> saleAssociateProposalKeys) {

        // Remove owner information
        if (!QueryPointOfView.CONSUMER.equals(pointOfView)) {
            demand.remove(Demand.OWNER_KEY);
        }

        // Remove the reference to the sale associates
        JsonArray saleAssociateKeys = demand.getJsonArray(Demand.SALE_ASSOCIATE_KEYS);
        int saleAssociateKeyNb = saleAssociateKeys == null ? 0 : saleAssociateKeys.size();
        demand.remove(Demand.SALE_ASSOCIATE_KEYS);

        // TODO: control the cancellerKey

        // Remove the references to proposals
        if (QueryPointOfView.CONSUMER.equals(pointOfView)) {
            // No alteration
        }
        else if (QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView)) {
            if (1 < saleAssociateKeyNb) {
                // Remove proposals from other sale associate
                int saleAssociateProposalKeyNb = saleAssociateProposalKeys == null ? 0 : saleAssociateProposalKeys.size();
                if (saleAssociateProposalKeyNb == 0) {
                    demand.remove(Demand.PROPOSAL_KEYS);
                }
                else if(demand.containsKey(Demand.PROPOSAL_KEYS)) {
                    JsonArray demandProposalKeys = demand.getJsonArray(Demand.PROPOSAL_KEYS);
                    if (0 < demandProposalKeys.size()) {
                        // Note: Direct operation contains() on the list returned by the Google API not implemented!
                        // => Need to clone the list first...
                        demandProposalKeys.getList().retainAll(Arrays.asList(saleAssociateProposalKeys.toArray()));
                    }
                }
            }
        }
        else { // if (QueryPointOfView.ANONYMOUS.equals(pointOfView)) {
            demand.remove(Demand.PROPOSAL_KEYS);
        }

        // Remove information about CC-ed users
        if (! QueryPointOfView.CONSUMER.equals(pointOfView)) {
            demand.remove(Demand.CC);
        }

        return demand;
    }

    /**
     * Remove nominative information from all listed Demand
     *
     * @param pointOfView Identify the anonymization point of view
     * @param demands List of entities to purge
     * @param saleAssociateKey Identifier of the concerned sale associate
     * @return List of cleaned up Demand instances
     *
     * @throws DataSourceException If the retrieval of the proposal keys created by the sale associate fails
     */
    public static JsonArray anonymizeDemands(QueryPointOfView pointOfView, JsonArray demands, Long saleAssociateKey) throws DataSourceException {
        List<Long> saleAssociateProposalKeys = QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView) ? ProposalSteps.getProposalKeys(saleAssociateKey) : null;
        int idx = demands.size();
        while (0 < idx) {
            -- idx;
            anonymizeDemand(pointOfView, demands.getJsonObject(idx), saleAssociateKey, saleAssociateProposalKeys);
        }
        return demands;
    }

    /**
     * Helper fetching a list of parameters for a query
     *
     * @param parameters bag of parameters proposed by a connector
     * @return Prefetch list of query parameters
     */
    protected static Map<String, Object> prepareQueryForSelection(JsonObject parameters) {
        Map<String, Object> queryFilters = new HashMap<String, Object>();

        if (!parameters.containsKey(BaseRestlet.ANY_STATE_PARAMETER_KEY)) {
            queryFilters.put(Demand.STATE_COMMAND_LIST, Boolean.TRUE);
        }

        Date lastModificationDate = null;
        if (parameters.containsKey(Entity.MODIFICATION_DATE)) {
            try {
                lastModificationDate = DateUtils.isoToDate(parameters.getString(Entity.MODIFICATION_DATE));
                queryFilters.put(">" + Entity.MODIFICATION_DATE, lastModificationDate);
            }
            catch (ParseException e) { } // Date not set, too bad.
        }

        return queryFilters;
    }

    private static JsonObject lastDemandQueryParameters = new GenericJsonObject();
    static {
        lastDemandQueryParameters.put(BaseRestlet.MAXIMUM_RESULTS_PARAMETER_KEY, 1);
        lastDemandQueryParameters.put(BaseRestlet.ANY_STATE_PARAMETER_KEY, Boolean.TRUE);
    }

    /**
     * Utility method create a demand with the given parameters and triggering the associated workflow steps
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param parameters Parameters produced by the Command line parser or transmitted via the REST API
     * @param owner Consumer who's going to own the created demand
     * @return Just created demand
     *
     * @throws DataSourceException if the retrieval of the last created demand or of the location information fail
     * @throws ClientException if there's an issue with the given parameters
     */
    public static Demand createDemand(PersistenceManager pm, JsonObject parameters, Consumer owner) throws DataSourceException, ClientException {

        // Detect given location
        if (!parameters.containsKey(Demand.LOCATION_KEY)) {
            List<Location> locations = LocationSteps.getLocations(pm, parameters, false);
            if (locations != null && 0 < locations.size()) {
                parameters.put(Demand.LOCATION_KEY, locations.get(0).getKey());
            }
        }

        if (!parameters.containsKey(Demand.LOCATION_KEY) || !parameters.containsKey(Demand.RANGE) || !parameters.containsKey(Demand.RANGE_UNIT)) {
            // Inherits some attributes from the last created demand
            List<Demand> lastDemands = getDemands(pm, lastDemandQueryParameters, owner.getKey(), QueryPointOfView.CONSUMER, null);
            if (0 < lastDemands.size()) {
                Demand lastDemand = lastDemands.get(0);
                if (!parameters.containsKey(Demand.LOCATION_KEY) && lastDemand.getLocationKey() != null) { parameters.put(Demand.LOCATION_KEY, lastDemand.getLocationKey()); }
                if (!parameters.containsKey(Demand.RANGE) && lastDemand.getRange() != null)              { parameters.put(Demand.RANGE, lastDemand.getRange()); }
                if (!parameters.containsKey(Demand.RANGE_UNIT) && lastDemand.getRangeUnit() != null)     { parameters.put(Demand.RANGE_UNIT, lastDemand.getRangeUnit()); }
            }

            // Fall back on the Consumer's location
            if (!parameters.containsKey(Demand.LOCATION_KEY) && owner.getLocationKey() != null) {
                parameters.put(Demand.LOCATION_KEY, owner.getLocationKey());
            }
        }

        Demand demand = getDemandOperations().createDemand(pm, parameters, owner.getKey());

        // Related workflow step
        MaelzelServlet.triggerValidationTask(demand);

        return demand;
    }

    /**
     * Utility method update a demand with the given parameters and triggering the associated workflow steps
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param rawCommand Reference of the command which initiated the process, is <code>null</code> if initiated by a REST API call
     * @param demandKey Resource identifier
     * @param parameters Parameters produced by the Command line parser or transmitted via the REST API
     * @param owner Consumer who owns the demand to be updated
     * @return Just updated demand
     *
     * @throws DataSourceException if the retrieval of the last created demand or of the location information fail
     * @throws InvalidIdentifierException if there's an issue with the Demand identifier is invalid
     * @throws InvalidStateException if the Demand is not update-able
     * @throws CommunicationException if the notification of a successful closing fails
     */
    public static Demand updateDemand(PersistenceManager pm, RawCommand rawCommand, Long demandKey, JsonObject parameters, Consumer owner) throws DataSourceException, InvalidIdentifierException, InvalidStateException, CommunicationException {

        Demand demand = getDemandOperations().getDemand(pm, demandKey, owner.getKey());
        State currentState = demand.getState();

        // Workflow state change
        if (parameters.size() == 1 && parameters.containsKey(Command.STATE)) {
            String proposedState = parameters.getString(Command.STATE);

            // Close
            if (State.confirmed.equals(currentState) && State.closed.toString().equals(proposedState)) {
                // Get the associated proposal
                Proposal proposal = getProposalOperations().getProposal(pm, demand.getProposalKeys().get(0), null, null);

                if (rawCommand != null) {
                    Locale locale = owner.getLocale();

                    MessageGenerator msgGen = new MessageGenerator(rawCommand.getSource(), demand.getHashTags(), locale);
                    msgGen.
                        put("demand>owner>name", owner.getName()).
                        fetch(demand).
                        fetch(proposal).
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
                            new String[] { msgGen.getMessage(MessageId.DEMAND_CLOSING_OK_TO_CONSUMER) }
                    );
                }

                if (!State.closed.equals(proposal.getState())) {
                    // Only one SaleAssociate is still associated with the Demand
                    SaleAssociate saleAssociate = getSaleAssociateOperations().getSaleAssociate(pm, demand.getSaleAssociateKeys().get(0));
                    Consumer saConsumerRecord = getConsumerOperations().getConsumer(pm, saleAssociate.getConsumerKey());
                    Store store = getStoreOperations().getStore(pm, proposal.getStoreKey());
                    Location location = getLocationOperations().getLocation(pm, store.getLocationKey());

                    // Inform Proposal owner about the closing
                    Locale locale = saConsumerRecord.getLocale();
                    MessageGenerator msgGen = new MessageGenerator(saConsumerRecord.getPreferredConnection(), demand.getHashTags(), locale);
                    msgGen.
                        put("proposal>owner>name", saConsumerRecord.getName()).
                        fetch(demand).
                        fetch(proposal).
                        fetch(store).
                        fetch(location, "store").
                        put("message>footer", msgGen.getAlternateMessage(MessageId.messageFooter)).
                        put("command>footer", LabelExtractor.get(ResourceFileId.fourth, "command_message_footer", locale));

                    String closeProposal = LabelExtractor.get(ResourceFileId.fourth, "command_message_body_proposal_close", msgGen.getParameters(), locale);
                    String subject = null;
                    if (Source.mail.equals(msgGen.getCommunicationChannel()) && Source.mail.equals(proposal.getSource())) {
                        subject = BaseSteps.getRawCommandOperations().getRawCommand(pm, proposal.getRawCommandId()).getSubject();
                    }
                    if (subject == null) {
                        subject = msgGen.getAlternateMessage(MessageId.messageSubject, msgGen.getParameters());
                    }
                    subject = MailConnector.prepareSubjectAsResponse(subject, locale);

                    msgGen.
                        put("command>threadSubject", MailConnector.prepareSubjectAsResponse(subject, locale).replaceAll(" ", "%20")).
                        put("command>closeProposal", closeProposal.replaceAll(" ", "%20").replaceAll(BaseConnector.ESCAPED_SUGGESTED_MESSAGE_SEPARATOR_STR, "%0A"));

                    try {
                        communicateToConsumer(
                                msgGen.getCommunicationChannel(),
                                subject,
                                saConsumerRecord,
                                new String[] { msgGen.getMessage(MessageId.DEMAND_CLOSING_OK_TO_ASSOCIATE) }
                        );
                    }
                    catch (CommunicationException ex) {} // Not a critical error, should not block the rest of the process
                }

                // No need to bother CC-ed
            }
            // Cancel
            else if (!State.closed.equals(currentState) && State.cancelled.toString().equals(proposedState)) {
                demand.setCancelerKey(owner.getKey());

                if (State.confirmed.equals(currentState)) {
                    // Only one SaleAssociate is still associated with the Demand
                    Long saleAssociateKey = demand.getSaleAssociateKeys().get(0);
                    SaleAssociate saleAssociate = getSaleAssociateOperations().getSaleAssociate(pm, saleAssociateKey);
                    Consumer saConsumerRecord = getConsumerOperations().getConsumer(pm, saleAssociate.getConsumerKey());
                    Proposal proposal = getProposalOperations().getProposal(pm, demand.getProposalKeys().get(0), saleAssociateKey, null);

                    // Confirm the demand canceling to the owner
                    if (rawCommand != null) {
                        Locale locale = owner.getLocale();

                        MessageGenerator msgGen = new MessageGenerator(rawCommand.getSource(), demand.getHashTags(), locale);
                        msgGen.
                            put("demand>owner>name", owner.getName()).
                            fetch(demand).
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

                    // Cancel the associated Proposal
                    proposal.setState(State.cancelled);
                    proposal.setCancelerKey(owner.getKey());
                    proposal = getProposalOperations().updateProposal(pm, proposal);

                    // Notify SaleAssociate about the confirmed Demand cancellation
                    Locale locale = saConsumerRecord.getLocale();
                    MessageGenerator msgGen = new MessageGenerator(saConsumerRecord.getPreferredConnection(), demand.getHashTags(), locale);
                    msgGen.
                        put("proposal>owner>name", saConsumerRecord.getName()).
                        fetch(demand).
                        fetch(proposal).
                        put("message>footer", msgGen.getAlternateMessage(MessageId.messageFooter)).
                        put("command>footer", LabelExtractor.get(ResourceFileId.fourth, "command_message_footer", locale));

                    String subject = null;
                    if (Source.mail.equals(msgGen.getCommunicationChannel()) && Source.mail.equals(proposal.getSource())) {
                        subject = getRawCommandOperations().getRawCommand(pm, proposal.getRawCommandId()).getSubject();
                    }
                    if (subject == null) {
                        subject = msgGen.getAlternateMessage(MessageId.messageSubject, msgGen.getParameters());
                    }
                    subject = MailConnector.prepareSubjectAsResponse(subject, locale);

                    try {
                        communicateToConsumer(
                                msgGen.getCommunicationChannel(),
                                subject,
                                saConsumerRecord,
                                new String[] { msgGen.getMessage(MessageId.DEMAND_CONFIRMED_CANCELLATION_OK_TO_ASSOCIATE) }
                        );
                    }
                    catch (CommunicationException ex) {} // Not a critical error, should not block the rest of the process

                    // Detach the SaleAssociate and the Proposal
                    demand.resetSaleAssociateKeys();
                    demand.resetProposalKeys();
                }
                else if (State.published.equals(currentState)) {
                    // Schedule the other proposal cancellation
                    MaelzelServlet.triggerProposalCancellationTask(demand.getProposalKeys(), owner.getKey(), null);

                    // Detach the SaleAssociates and the Proposals
                    demand.resetProposalKeys();
                    demand.resetSaleAssociateKeys();
                }
            }
            else {
                throw new InvalidStateException("Invalid state change attempt to: " + proposedState, currentState.toString(), proposedState);
            }

            demand.setState(proposedState);
            demand = getDemandOperations().updateDemand(pm, demand);
        }
        // Normal attribute update
        else if (State.opened.equals(currentState) || State.published.equals(currentState) || State.invalid.equals(currentState)) {
            // Detect new location
            try {
                List<Location> locations = LocationSteps.getLocations(pm, parameters, false);
                if (locations != null && 0 < locations.size()) {
                    Location location = locations.get(0);
                    if (!location.getKey().equals(demand.getLocationKey())) {
                        demand.setLocationKey(location.getKey());
                    }
                }
            }
            catch(InvalidIdentifierException ex) {
                // Reset location reference!
                demand.setLocationKey(null);
            }

            // Neutralize read-only parameters
            parameters.remove(Demand.SALE_ASSOCIATE_KEYS);
            parameters.remove(Demand.PROPOSAL_KEYS);

            // Integrate updates
            demand.fromJson(parameters);

            // Prepare as a new Demand
            demand.setState(State.opened); // Will force the re-validation of the entire demand
            demand.resetProposalKeys(); // All existing proposals are removed
            demand.resetSaleAssociateKeys(); // All existing sale associates need to be recontacted again

            // Persist updates
            demand = getDemandOperations().updateDemand(pm, demand);

            // Related workflow step
            MaelzelServlet.triggerValidationTask(demand);
        }
        else {
            throw new InvalidStateException("Entity not in modifiable state", currentState.toString(), null);
        }

        return demand;
    }

    /**
     * Utility method deleting the identified demand
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param demandKey Resource identifier
     * @param owner Consumer who owns the demand to delete
     *
     * @throws DataSourceException if the retrieval of the last created demand or of the location information fail
     * @throws InvalidIdentifierException if there's an issue with the Demand identifier is invalid
     * @throws InvalidStateException if the Demand is not already cancelled
     */
    public static void deleteDemand(PersistenceManager pm, Long demandKey, Consumer owner) throws DataSourceException, InvalidIdentifierException, InvalidStateException {

        Demand demand = getDemandOperations().getDemand(pm, demandKey, owner.getKey());

        State currentState = demand.getState();
        if (!State.cancelled.equals(currentState)) {
            throw new InvalidStateException("Invalid state change attempt to: " + State.markedForDeletion.toString(), currentState.toString(), State.markedForDeletion.toString());
        }

        demand.setState(State.markedForDeletion);
        demand.setMarkedForDeletion(Boolean.TRUE);
        getDemandOperations().updateDemand(pm, demand);

        List<Proposal> proposals = BaseSteps.getProposalOperations().getProposals(pm, Proposal.DEMAND_KEY, demandKey, 0);
        for (Proposal proposal: proposals) {
            proposal.setDemandKey(0L); // To cut the link
            BaseSteps.getProposalOperations().updateProposal(pm, proposal);
        }
    }
}
