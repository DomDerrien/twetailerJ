package twetailer.task.step;

import static twetailer.connector.BaseConnector.communicateToCCed;
import static twetailer.connector.BaseConnector.communicateToConsumer;
import static twetailer.connector.BaseConnector.getCCedCommunicationChannel;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

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
import twetailer.dto.Registrar;
import twetailer.dto.SaleAssociate;
import twetailer.dto.Store;
import twetailer.dto.Command.QueryPointOfView;
import twetailer.j2ee.BaseRestlet;
import twetailer.j2ee.MaelzelServlet;
import twetailer.task.CommandLineParser;
import twetailer.task.RobotResponder;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.Prefix;
import twetailer.validator.CommandSettings.State;
import domderrien.i18n.DateUtils;
import domderrien.i18n.LabelExtractor;
import domderrien.i18n.LabelExtractor.ResourceFileId;
import domderrien.jsontools.JsonArray;
import domderrien.jsontools.JsonObject;

public class ProposalSteps extends BaseSteps {

    private final static Logger log = Logger.getLogger(ProposalSteps.class.getName());

    /* legacy *
    protected static AmazonFPS amazonFPS = new AmazonFPS();
    */

    public static Proposal getProposal(PersistenceManager pm, Long proposalKey, Long ownerKey, QueryPointOfView pointOfView, Long saleAssociateKey, Long storeKey) throws ReservedOperationException, InvalidIdentifierException {
        Proposal output = null;

        if (QueryPointOfView.CONSUMER.equals(pointOfView)) {
            output = getProposalOperations().getProposal(pm, proposalKey, null, null);

            try {
                // Verify the Demand owner
                getDemandOperations().getDemand(pm, output.getDemandKey(), ownerKey);

                // Get payment related URL
                if (State.confirmed.equals(output.getState())) {
                    /* legacy *
                    //
                    // TODO: verify the store record to check if it accepts AWS FPS payment
                    // TODO: verify that the proposal has a total cost value
                    //
                    // Cook the Amazon FPS Co-Branded service URL
                    String description = LabelExtractor.get(
                            "payment_transaction_description",
                            new Object[] {
                                    output.getSerializedCriteria(),
                                    demand.getSerializedCriteria()
                            },
                            Locale.ENGLISH // FIXME: get logged user's locale
                    );
                    try {
                        String transactionReference = Payment.getReference(ownerKey, demand.getKey(), proposalKey);
                        output.setAWSCBUIURL(amazonFPS.getCoBrandedServiceUrl(transactionReference, description, output.getTotal(), "USD"));
                    }
                    catch (Exception ex) {
                        throw new DataSourceException("Cannot compute the AWS FPS Co-Branded Service URL", ex);
                    }
                    */
                }
            }
            catch (Exception ex) {
                throw new ReservedOperationException(Action.list, Proposal.class.getName());
            }
        }
        else if (QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView)) {
            if (saleAssociateKey == null && storeKey == null) {
                throw new ReservedOperationException(Action.list, Proposal.class.getName());
            }
            output = getProposalOperations().getProposal(pm, proposalKey, saleAssociateKey, storeKey);
        }
        else { // if (QueryPointOfView.anonymous.equals(pointOfView)) {
            throw new ReservedOperationException(Action.list, Proposal.class.getName());
        }

        return output;
    }

    public static List<Proposal> getProposals(PersistenceManager pm, JsonObject parameters, Long ownerKey, QueryPointOfView pointOfView, Long saleAssociateKey) throws ReservedOperationException, InvalidIdentifierException, DataSourceException {

        Map<String, Object> queryParameters = prepareQueryForSelection(parameters);
        int maximumResults = parameters.containsKey(BaseRestlet.MAXIMUM_RESULTS_PARAMETER_KEY) ? (int) parameters.getLong(BaseRestlet.MAXIMUM_RESULTS_PARAMETER_KEY) : 0;

        List<Proposal> output = null;

        if (QueryPointOfView.CONSUMER.equals(pointOfView)) {
            queryParameters.put(Proposal.CONSUMER_KEY, ownerKey);

            boolean withKeySet = parameters.containsKey(Entity.KEY);
            if (withKeySet) {
                JsonArray intialKeys = parameters.getJsonArray(Entity.KEY);
                List<Long> convertedKeys = new ArrayList<Long>();
                int limit = intialKeys.size();
                for (int idx=0; idx<limit; idx++) {
                    convertedKeys.add(Long.valueOf(intialKeys.getString(idx)));
                }
                output = getProposalOperations().getProposals(pm, convertedKeys);
            }
            else {
                output = getProposalOperations().getProposals(pm, queryParameters, maximumResults);
            }
        }
        else if (QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView)) {
            if (saleAssociateKey == null) {
                throw new ReservedOperationException(Action.list, Proposal.class.getName());
            }
            queryParameters.put(Proposal.OWNER_KEY, saleAssociateKey);

            output = getProposalOperations().getProposals(pm, queryParameters, maximumResults);
        }
        else { // if (QueryPointOfView.anonymous.equals(pointOfView)) {
            List<Location> locations = LocationSteps.getLocations(pm, parameters, true);

            output = getProposalOperations().getProposals(pm, queryParameters, locations, maximumResults);
        }

        return output;
    }

    public static List<Long> getProposalKeys(PersistenceManager pm, JsonObject parameters, Long ownerKey, QueryPointOfView pointOfView, Long saleAssociateKey) throws ReservedOperationException, DataSourceException {

        Map<String, Object> queryParameters = prepareQueryForSelection(parameters);
        int maximumResults = parameters.containsKey(BaseRestlet.MAXIMUM_RESULTS_PARAMETER_KEY) ? (int) parameters.getLong(BaseRestlet.MAXIMUM_RESULTS_PARAMETER_KEY) : 0;

        List<Long> output = null;

        if (QueryPointOfView.CONSUMER.equals(pointOfView)) {
            queryParameters.put(Proposal.CONSUMER_KEY, ownerKey);

            output = getProposalOperations().getProposalKeys(pm, queryParameters, maximumResults);

            throw new RuntimeException("Not yet implemented!");
        }
        else if (QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView)) {
            if (saleAssociateKey == null) {
                throw new ReservedOperationException(Action.list, Proposal.class.getName());
            }
            queryParameters.put(Proposal.OWNER_KEY, saleAssociateKey);

            output = getProposalOperations().getProposalKeys(pm, queryParameters, maximumResults);
        }
        else { // if (QueryPointOfView.anonymous.equals(pointOfView)) {
            throw new ReservedOperationException(Action.list, Proposal.class.getName());
        }

        return output;
    }

    /**
     * Retrieve the list of proposal identifier the specified sale associate has created
     *
     * @param saleAssociateKey Identifier of the concerned sale associate
     * @return List of proposal identifiers
     *
     * @throws DataSourceException If the proposal key retrieval fails
     */
    protected static List<Long> getProposalKeys(Long saleAssociateKey) throws DataSourceException {
        if (saleAssociateKey == null) {
            return null;
        }
        PersistenceManager pm = getBaseOperations().getPersistenceManager();
        try {
            return getProposalKeys(pm, saleAssociateKey);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Retrieve the list of proposal identifier the specified sale associate has created
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param saleAssociateKey Identifier of the concerned sale associate
     * @return List of proposal identifiers
     *
     * @throws DataSourceException If the proposal key retrieval fails
     */
    protected static List<Long> getProposalKeys(PersistenceManager pm, Long saleAssociateKey) throws DataSourceException {
        if (saleAssociateKey == null) {
            return null;
        }
        // Get the keys of the proposals owned by the requester
        Map<String, Object> queryProposals = new HashMap<String, Object>();
        queryProposals.put(Command.OWNER_KEY, saleAssociateKey);
        queryProposals.put(Command.STATE_COMMAND_LIST, Boolean.TRUE);
        return getProposalOperations().getProposalKeys(pm, queryProposals, 0);
    }

    /**
     * Remove nominative information from the Proposal record
     *
     * @param pointOfView Identify the anonymization point of view
     * @param proposal Entity to purge
     * @return Cleaned up Proposal instance
     */
    public static JsonObject anonymizeProposal(QueryPointOfView pointOfView, JsonObject proposal) {

        // Remove owner information
        if (!QueryPointOfView.SALE_ASSOCIATE.equals(pointOfView)) {
            proposal.remove(Proposal.OWNER_KEY);
        }

        // TODO: control the cancellerKey

        return proposal;
    }

    /**
     * Remove nominative information from the Proposal record
     *
     * @param pointOfView Identify the anonymization point of view
     * @param proposals Array of entities to purge
     * @return List of cleaned up Proposal instances
     */
    public static JsonArray anonymizeProposals(QueryPointOfView pointOfView, JsonArray proposals) {
        int idx = proposals.size();
        while (0 < idx) {
            -- idx;
            anonymizeProposal(pointOfView, proposals.getJsonObject(idx));
        }
        return proposals;
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
            queryFilters.put(Proposal.STATE_COMMAND_LIST, Boolean.TRUE);
        }

        Date lastModificationDate = null;
        if (parameters.containsKey(Entity.MODIFICATION_DATE)) {
            try {
                lastModificationDate = DateUtils.isoToDate(parameters.getString(Entity.MODIFICATION_DATE));
                queryFilters.put(">" + Entity.MODIFICATION_DATE, lastModificationDate);
            }
            catch (ParseException e) {
                // Date not set, too bad.
                log.warning("Date in an invalid format: " + parameters.getString(Entity.MODIFICATION_DATE));
            }
        }

        return queryFilters;
    }

    /**
     * Utility method create a proposal with the given parameters and triggering the associated work flow steps
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param parameters Parameters produced by the Command line parser or transmitted via the REST API
     * @param owner Sale associate who's going to own the created proposal
     * @param saConsumerRecord Consumer record attached to the sale associate
     * @return Just created proposal
     *
     * @throws DataSourceException if the retrieval of the last created proposal or of the location information fail
     * @throws ClientException if there's an issue with the given parameters
     */
    public static Proposal createProposal(PersistenceManager pm, JsonObject parameters, SaleAssociate owner, Consumer saConsumerRecord) throws DataSourceException, ClientException {

        // Data validation & propagation
        if (!parameters.containsKey(Proposal.DEMAND_KEY)) {
            throw new InvalidIdentifierException("Missing " + Proposal.DEMAND_KEY + " attribute!");
        }
        Long demandKey = parameters.getLong(Proposal.DEMAND_KEY);
        Demand demand = getDemandOperations().getDemand(pm, demandKey, null);
        if (!parameters.containsKey(Proposal.CONSUMER_KEY)) {
            parameters.put(Proposal.CONSUMER_KEY, demand.getOwnerKey());
        }
        else if (!demand.getOwnerKey().equals(parameters.getLong(Proposal.CONSUMER_KEY))) {
            throw new ClientException("Proposed " + Proposal.CONSUMER_KEY + " does not match the key of the demand owner!");
        }

        parameters.put(Proposal.LOCATION_KEY, owner.getLocationKey());

        // Entity creation
        Proposal proposal = getProposalOperations().createProposal(pm, parameters, owner);

        // Related workflow step
        MaelzelServlet.triggerValidationTask(proposal);

        return proposal;
    }

    /**
     * Utility method update a proposal with the given parameters and triggering the associated workflow steps
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param rawCommand Reference of the command which initiated the process, is <code>null</code> if initiated by a REST API call
     * @param proposalKey Resource identifier
     * @param parameters Parameters produced by the Command line parser or transmitted via the REST API
     * @param owner Sale associate who owns the proposal to be updated
     * @param saConsumerRecord Consumer record attached to the sale associate
     * @param isUserAdmin
     * @return Just updated proposal
     *
     * @throws DataSourceException if the retrieval of the last created proposal or of the location information fail
     * @throws InvalidIdentifierException if there's an issue with the Proposal identifier is invalid
     * @throws InvalidStateException if the Proposal is not update-able
     * @throws CommunicationException if the communication of the update confirmation fails
     */
    public static Proposal updateProposal(PersistenceManager pm, RawCommand rawCommand, Long proposalKey, JsonObject parameters, SaleAssociate owner, Consumer saConsumerRecord, boolean isUserAdmin) throws DataSourceException, InvalidIdentifierException, InvalidStateException, CommunicationException {

        Proposal proposal = getProposalOperations().getProposal(pm, proposalKey, isUserAdmin ? null : owner.getKey(), isUserAdmin ? null : owner.getStoreKey());
        State currentState = proposal.getState();

        // Workflow state change
        if (parameters.size() == 1 && parameters.containsKey(Command.STATE)) {
            String proposedState = parameters.getString(Command.STATE);

            // Close
            if (State.confirmed.equals(currentState) && State.closed.toString().equals(proposedState)) {
                // Update the user's statistics
                owner.setClosedProposalNb(owner.getClosedProposalNb() == null ? 1 : owner.getClosedProposalNb() + 1);
                owner = BaseSteps.getSaleAssociateOperations().updateSaleAssociate(pm, owner);

                // Update the store's statistics
                Store store = getStoreOperations().getStore(pm, proposal.getStoreKey());
                store.setClosedProposalNb(store.getClosedProposalNb() == null ? 1 : store.getClosedProposalNb() + 1);
                store = BaseSteps.getStoreOperations().updateStore(pm, store);

                // Get the associated demand
                Demand demand = getDemandOperations().getDemand(pm, proposal.getDemandKey(), null);

                if (rawCommand != null && !Source.robot.equals(rawCommand.getSource())) {
                    Locale locale = saConsumerRecord.getLocale();

                    MessageGenerator msgGen = new MessageGenerator(rawCommand.getSource(), demand.getHashTags(), locale);
                    msgGen.
                        put("proposal>owner>name", saConsumerRecord.getName()).
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
                            saConsumerRecord,
                            new String[] { msgGen.getMessage(MessageId.PROPOSAL_CLOSING_OK_TO_ASSOCIATE) }
                    );
                }

                if (!State.closed.equals(demand.getState())) {
                    // Get demand owner
                    Consumer demandOwner = getConsumerOperations().getConsumer(pm, demand.getOwnerKey());
                    Location location = getLocationOperations().getLocation(pm, store.getLocationKey());
                    Registrar registrar = getRegistrarOperations().getRegistrar(pm, store.getRegistrarKey());

                    // Inform Proposal owner about the closing
                    Locale locale = demandOwner.getLocale();
                    MessageGenerator msgGen = new MessageGenerator(demandOwner.getPreferredConnection(), demand.getHashTags(), locale);
                    msgGen.
                        put("demand>owner>name", demandOwner.getName()).
                        fetch(demand).
                        fetch(proposal).
                        fetch(store).
                        fetch(location, "store").
                        fetch(registrar).
                        put("message>footer", msgGen.getAlternateMessage(MessageId.messageFooter)).
                        put("command>footer", LabelExtractor.get(ResourceFileId.fourth, "command_message_footer", locale));

                    String closeDemand = LabelExtractor.get(ResourceFileId.fourth, "command_message_body_demand_close", msgGen.getParameters(), locale);
                    String subject = null;
                    if (Source.mail.equals(msgGen.getCommunicationChannel()) && Source.mail.equals(demand.getSource())) {
                        subject = BaseSteps.getRawCommandOperations().getRawCommand(pm, demand.getRawCommandId()).getSubject();
                    }
                    if (subject == null) {
                        subject = msgGen.getAlternateMessage(MessageId.messageSubject, msgGen.getParameters());
                    }
                    subject = MailConnector.prepareSubjectAsResponse(subject, locale);

                    msgGen.
                        put("command>threadSubject", BaseConnector.prepareMailToSubject(MailConnector.prepareSubjectAsResponse(subject, locale))).
                        put("command>closeDemand", BaseConnector.prepareMailToBody(closeDemand));

                    try {
                        communicateToConsumer(
                                msgGen.getCommunicationChannel(),
                                subject,
                                demandOwner,
                                new String[] { msgGen.getMessage(MessageId.PROPOSAL_CLOSING_OK_TO_CONSUMER) }
                        );
                    }
                    catch (CommunicationException e) {
                        // Not a critical error, should not block the rest of the process
                        log.warning("Cannot inform " + demand.getOwnerKey());
                    }
                }

                // No need to bother CC-ed
            }
            // Cancel
            else if (!State.closed.equals(currentState) && State.cancelled.toString().equals(proposedState)) {
                proposal.setCancelerKey(saConsumerRecord.getKey());

                // Get the associated demand
                Demand demand = getDemandOperations().getDemand(pm, proposal.getDemandKey(), null);
                demand.removeProposalKey(proposalKey);

                // Confirm the proposal canceling to the owner
                if (rawCommand != null) {
                    Locale locale = saConsumerRecord.getLocale();

                    MessageGenerator msgGen = new MessageGenerator(rawCommand.getSource(), demand.getHashTags(), locale);
                    msgGen.
                        put("proposal>owner>name", saConsumerRecord.getName()).
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
                            saConsumerRecord,
                            new String[] { msgGen.getMessage(MessageId.PROPOSAL_CANCELLATION_OK_TO_ASSOCIATE) }
                    );
                }

                if (State.confirmed.equals(currentState)) {
                    Consumer demandOwner = getConsumerOperations().getConsumer(pm, demand.getOwnerKey());
                    Location location = getLocationOperations().getLocation(pm, demand.getLocationKey());

                    // FIXME: Place the associated demand in the published state again if not expired
                    demand.setState(State.published);
                    demand.updateModificationDate(); // To be sure it's picked-up by the next cron job 'processPublishedDemands'

                    // Notify Consumer about the confirmed Proposal cancellation
                    Locale locale = demandOwner.getLocale();
                    MessageGenerator msgGen = new MessageGenerator(demandOwner.getPreferredConnection(), demand.getHashTags(), locale);
                    msgGen.
                        put("demand>owner>name", demandOwner.getName()).
                        fetch(demand).
                        fetch(location, "demand").
                        fetch(proposal).
                        put("message>footer", msgGen.getAlternateMessage(MessageId.messageFooter)).
                        put("command>footer", LabelExtractor.get(ResourceFileId.fourth, "command_message_footer", locale));

                    String cancelDemand = LabelExtractor.get(ResourceFileId.fourth, "command_message_body_demand_cancel", msgGen.getParameters(), locale);
                    String updateDemand = LabelExtractor.get(ResourceFileId.fourth, "command_message_body_demand_update", msgGen.getParameters(), locale);
                    String subject = null;
                    if (Source.mail.equals(msgGen.getCommunicationChannel()) && Source.mail.equals(demand.getSource())) {
                        subject = getRawCommandOperations().getRawCommand(pm, demand.getRawCommandId()).getSubject();
                    }
                    if (subject == null) {
                        subject = msgGen.getAlternateMessage(MessageId.messageSubject, msgGen.getParameters());
                    }
                    subject = MailConnector.prepareSubjectAsResponse(subject, locale);

                    msgGen.
                    put("command>threadSubject", BaseConnector.prepareMailToSubject(subject)).
                    put("command>cancelDemand", BaseConnector.prepareMailToBody(cancelDemand)).
                    put("command>updateDemand", BaseConnector.prepareMailToBody(updateDemand));

                    try {
                        communicateToConsumer(
                                msgGen.getCommunicationChannel(),
                                subject,
                                demandOwner,
                                new String[] { msgGen.getMessage(MessageId.PROPOSAL_CONFIRMED_CANCELLATION_OK_TO_CONSUMER) }
                        );
                    }
                    catch (CommunicationException e) {
                        // Not a critical error, should not block the rest of the process
                        log.warning("Cannot inform " + demand.getOwnerKey());
                    }
                }

                getDemandOperations().updateDemand(pm, demand);
            }
            else {
                throw new InvalidStateException("Invalid state change attempt to: " + proposedState, currentState.toString(), proposedState);
            }

            proposal.setState(proposedState);
            proposal = getProposalOperations().updateProposal(pm, proposal);
        }
        // Normal attribute update
        else if (State.opened.equals(currentState) || State.published.equals(currentState) || State.invalid.equals(currentState)) {
            // Integrate updates
            proposal.fromJson(parameters, isUserAdmin);

            // Prepare as a new Demand
            proposal.setState(State.opened); // Will force the re-validation of the entire demand

            // Persist updates
            proposal = getProposalOperations().updateProposal(pm, proposal);

            // Detach the proposal from the associated demand (just in case it's now invalid)
            Demand demand = getDemandOperations().getDemand(pm, proposal.getDemandKey(), null);
            demand.removeProposalKey(proposalKey);
            getDemandOperations().updateDemand(pm, demand);

            // Related workflow step
            MaelzelServlet.triggerValidationTask(proposal);
        }
        else {
            throw new InvalidStateException("Entity not in modifiable state", currentState.toString(), null);
        }

        return proposal;
    }

    /**
     * Utility method updating the proposal state if the specified user owns the attached demand
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param rawCommand Reference of the command which initiated the process, is <code>null</code> if initiated by a REST API call
     * @param proposalKey Resource identifier
     * @param parameters Parameters produced by the Command line parser or transmitted via the REST API
     * @param demandOwner Owner of the demand associated to the identified proposal
     * @return Just updated proposal
     *
     * @throws DataSourceException if the retrieval of the last created proposal or of the location information fail
     * @throws InvalidIdentifierException if there's an issue with the Proposal identifier is invalid
     * @throws InvalidStateException if the Proposal is not update-able
     * @throws ReservedOperationException if the Consumer does not own the Demand associated to the identified Proposal
     * @throws CommunicationException If the result of the operation cannot be communicated to the SaleAssociate owning the concerned Proposal
     */
    public static Proposal updateProposal(PersistenceManager pm, RawCommand rawCommand, Long proposalKey, JsonObject parameters, Consumer demandOwner) throws DataSourceException, InvalidIdentifierException, InvalidStateException, ReservedOperationException, CommunicationException {

        // Verify the correct parameter sequence
        if (parameters.size() != 2 && parameters.size() != 3 ||
            !parameters.containsKey(Command.POINT_OF_VIEW) ||
            !parameters.containsKey(Command.STATE) && !parameters.containsKey(Proposal.SCORE) ||
            parameters.containsKey(Command.STATE) && !State.confirmed.toString().equals(parameters.getString(Command.STATE)) && !State.declined.toString().equals(parameters.getString(Command.STATE))
        ) {
            throw new InvalidStateException("Invalid parameter sequence for the Proposal state update!");
        }

        // Get the proposal and verify its state
        Proposal proposal = getProposalOperations().getProposal(pm, proposalKey, null, null);

        // Verify the consumer owns the associated demand and its state
        Demand demand = getDemandOperations().getDemand(pm, proposal.getDemandKey(), demandOwner.getKey());

        if (parameters.containsKey(Command.STATE)) {
            boolean confirmProposal = State.confirmed.toString().equals(parameters.getString(Command.STATE));
            State newState = confirmProposal ? State.confirmed : State.declined;

            // Verify the proposal state
            State currentState = proposal.getState();
            if (!State.published.equals(currentState)) {
                throw new InvalidStateException("Invalid Proposal state for a " + (confirmProposal ? "confirmation" : "declination"), currentState.toString(), newState.toString());
            }

            // Verify the demand state
            if (!State.published.equals(demand.getState())) {
                throw new InvalidStateException("Invalid associated Demand state for a " + (confirmProposal ? "confirmation" : "declination"), demand.getState().toString(), newState.toString());
            }

            // Persist the proposal state change
            proposal.setState(newState);
            if (!confirmProposal) {
                proposal.setCancelerKey(demandOwner.getKey());
            }
            proposal = getProposalOperations().updateProposal(pm, proposal);

            if (confirmProposal) {
                // Cancel the other proposals associated to the demand
                List<Long> proposalKeys = demand.getProposalKeys();
                if (1 < proposalKeys.size()) {
                    // Schedule the other proposal cancellation
                    MaelzelServlet.triggerProposalCancellationTask(proposalKeys, demandOwner.getKey(), proposalKey);

                    // Clean-up the list of associated proposals
                    demand.resetProposalKeys();
                    demand.addProposalKey(proposalKey);
                }
                List<Long> saleAssociateKeys = demand.getSaleAssociateKeys();
                if (1 < saleAssociateKeys.size()) {
                    demand.resetSaleAssociateKeys();
                    demand.addSaleAssociateKey(proposal.getOwnerKey());
                }

                // Persist the demand state change
                demand.setState(State.confirmed);
                getDemandOperations().updateDemand(pm, demand);

                // Echo back the successful confirmation
                Store store = BaseSteps.getStoreOperations().getStore(pm, proposal.getStoreKey());
                Location location = BaseSteps.getLocationOperations().getLocation(pm, store.getLocationKey());
                Registrar registrar = getRegistrarOperations().getRegistrar(pm, store.getRegistrarKey());

                if (rawCommand != null) { // Means the Consumer did not use the console to !confirm the Proposal
                    Locale locale = demandOwner.getLocale();
                    MessageGenerator msgGen = new MessageGenerator(rawCommand.getSource(), demand.getHashTags(), locale);
                    msgGen.
                        put("demand>owner>name", demandOwner.getName()).
                        fetch(proposal).
                        fetch(store).
                        fetch(location, "store").
                        fetch(registrar).
                        fetch(demand).
                        put("message>footer", msgGen.getAlternateMessage(MessageId.messageFooter)).
                        put("command>footer", LabelExtractor.get(ResourceFileId.fourth, "command_message_footer", locale));

                    String cancelDemand = LabelExtractor.get(ResourceFileId.fourth, "command_message_body_demand_cancel", msgGen.getParameters(), locale);
                    String closeDemand = LabelExtractor.get(ResourceFileId.fourth, "command_message_body_demand_close", msgGen.getParameters(), locale);
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
                        put("command>closeDemand", BaseConnector.prepareMailToBody(closeDemand));

                    String message = msgGen.getMessage(MessageId.PROPOSAL_CONFIRMATION_OK_TO_CONSUMER);

                    communicateToConsumer(
                            msgGen.getCommunicationChannel(),
                            subject,
                            demandOwner,
                            new String[] { message }
                    );
                }

                Long robotKey = RobotResponder.getRobotSaleAssociateKey(pm);
                if (proposal.getOwnerKey().equals(robotKey)) {
                    // Schedule the automatic Proposal closing by the Robot
                    prepareProposalClosingByTheRobot(pm, proposalKey);
                }
                else {
                    // Inform the sale associate of the successful confirmation
                    SaleAssociate saleAssociate = getSaleAssociateOperations().getSaleAssociate(pm, proposal.getOwnerKey());
                    Consumer saConsumerRecord = getConsumerOperations().getConsumer(pm, saleAssociate.getConsumerKey());
                    Locale locale = saConsumerRecord.getLocale();

                    MessageGenerator msgGen = new MessageGenerator(saConsumerRecord.getPreferredConnection(), proposal.getHashTags(), locale);
                    msgGen.
                        put("proposal>owner>name", saConsumerRecord.getName()).
                        fetch(proposal).
                        fetch(demand).
                        put("message>footer", msgGen.getAlternateMessage(MessageId.messageFooter)).
                        put("command>footer", LabelExtractor.get(ResourceFileId.fourth, "command_message_footer", locale));

                    String cancelProposal = LabelExtractor.get(ResourceFileId.fourth, "command_message_body_proposal_cancel", msgGen.getParameters(), locale);
                    String closeProposal = LabelExtractor.get(ResourceFileId.fourth, "command_message_body_proposal_close", msgGen.getParameters(), locale);
                    String subject = null;
                    if (Source.mail.equals(msgGen.getCommunicationChannel()) && Source.mail.equals(proposal.getSource())) {
                        subject = getRawCommandOperations().getRawCommand(pm, proposal.getRawCommandId()).getSubject();
                    }
                    if (subject == null) {
                        subject = msgGen.getAlternateMessage(MessageId.messageSubject, msgGen.getParameters());
                    }
                    subject = MailConnector.prepareSubjectAsResponse(subject, locale);

                    msgGen.
                        put("command>threadSubject", BaseConnector.prepareMailToSubject(MailConnector.prepareSubjectAsResponse(subject, locale))).
                        put("command>cancelProposal", BaseConnector.prepareMailToBody(cancelProposal)).
                        put("command>closeProposal", BaseConnector.prepareMailToBody(closeProposal));

                    communicateToConsumer(
                            msgGen.getCommunicationChannel(),
                            subject,
                            saConsumerRecord,
                            new String[] { msgGen.getMessage(MessageId.PROPOSAL_CONFIRMATION_OK_TO_ASSOCIATE) }
                    );
                }

                // Notify CC-ed
                List<String> cc = demand.getCC();
                if (cc != null && 0 < cc.size()) {
                    Locale locale = demandOwner.getLocale();
                    MessageGenerator msgGen = null;
                    String message = null;
                    String subject = null;
                    for (String coordinate: cc) {
                        try {
                            Source source = getCCedCommunicationChannel(coordinate);

                            if (msgGen == null || !source.equals(msgGen.getCommunicationChannel())) {
                                //
                                // TODO: cache the MessageGenerator instance per Source value to avoid unnecessary re-creation!
                                //
                                msgGen = new MessageGenerator(source, demand.getHashTags(), locale);
                                msgGen.
                                    put("demand>owner>name", demandOwner.getName()).
                                    fetch(proposal).
                                    fetch(store).
                                    fetch(location, "store").
                                    fetch(registrar).
                                    fetch(demand).
                                    put("message>footer", msgGen.getAlternateMessage(MessageId.messageFooter));

                                message = msgGen.getMessage(MessageId.PROPOSAL_CONFIRMATION_OK_TO_CCED);
                            }

                            if (subject == null) {
                                Map<String, Object> cmdPrm = new HashMap<String, Object>();
                                cmdPrm.put("demand>key", demand.getKey());
                                subject = msgGen.getAlternateMessage(MessageId.messageSubject, cmdPrm);
                                subject = MailConnector.prepareSubjectAsForward(subject, locale);
                            }

                            communicateToCCed(
                                    msgGen.getCommunicationChannel(),
                                    coordinate,
                                    subject,
                                    message,
                                    locale
                            );
                        }
                        catch (CommunicationException e) {
                            // Too bad, cannot contact the CC-ed person... Don't block the next sending!
                            log.warning("Cannot inform cc'ed " + coordinate);
                        }
                    }
                }
            }
        }
        else { //  if (parameters.containsKey(Proposal.SCORE)) {
            // Persist the proposal attribute change
            proposal.setScore(Long.valueOf((int) parameters.getLong(Proposal.SCORE)));
            if (parameters.containsKey(Proposal.COMMENT)) {
                proposal.setComment(parameters.getString(Proposal.COMMENT));
            }
            proposal = getProposalOperations().updateProposal(pm, proposal);

            // Echo back the successful rating
            Store store = BaseSteps.getStoreOperations().getStore(pm, proposal.getStoreKey());
            Location location = BaseSteps.getLocationOperations().getLocation(pm, store.getLocationKey());
            Registrar registrar = getRegistrarOperations().getRegistrar(pm, store.getRegistrarKey());

            if (rawCommand != null) { // Means the Consumer did not use the console to !confirm the Proposal
                Locale locale = demandOwner.getLocale();
                MessageGenerator msgGen = new MessageGenerator(rawCommand.getSource(), demand.getHashTags(), locale);
                msgGen.
                    put("demand>owner>name", demandOwner.getName()).
                    fetch(proposal).
                    fetch(store).
                    fetch(location, "store").
                    fetch(registrar).
                    fetch(demand).
                    put("message>footer", msgGen.getAlternateMessage(MessageId.messageFooter)).
                    put("command>footer", LabelExtractor.get(ResourceFileId.fourth, "command_message_footer", locale));

                String confirmProposal = LabelExtractor.get(ResourceFileId.fourth, "command_message_body_proposal_confirm", msgGen.getParameters(), locale);
                String declineProposal = LabelExtractor.get(ResourceFileId.fourth, "command_message_body_proposal_decline", msgGen.getParameters(), locale);
                String cancelDemand = LabelExtractor.get(ResourceFileId.fourth, "command_message_body_demand_cancel", msgGen.getParameters(), locale);
                String listDemand = LabelExtractor.get(ResourceFileId.fourth, "command_message_body_demand_list", msgGen.getParameters(), locale);
                String subject = null;
                if (rawCommand != null) { // Can be only null if its source == api -- see caller context
                    subject = rawCommand.getSubject();
                }
                if (subject == null) {
                    subject = msgGen.getAlternateMessage(MessageId.messageSubject, msgGen.getParameters());
                }
                subject = MailConnector.prepareSubjectAsResponse(subject, locale);

                msgGen.
                    put("command>threadSubject", BaseConnector.prepareMailToSubject(subject)).
                    put("command>confirmProposal", BaseConnector.prepareMailToBody(confirmProposal)).
                    put("command>declineProposal", BaseConnector.prepareMailToBody(declineProposal)).
                    put("command>cancelDemand", BaseConnector.prepareMailToBody(cancelDemand)).
                    put("command>listDemand", BaseConnector.prepareMailToBody(listDemand));

                String message = msgGen.getMessage(MessageId.PROPOSAL_RATING_OK_TO_CONSUMER);

                communicateToConsumer(
                        msgGen.getCommunicationChannel(),
                        subject,
                        demandOwner,
                        new String[] { message }
                );
            }

            Long robotKey = RobotResponder.getRobotSaleAssociateKey(pm);
            if (proposal.getOwnerKey().equals(robotKey)) {
                // Stay silent
            }
            else {
                // Inform the sale associate of the successful rating
                SaleAssociate saleAssociate = getSaleAssociateOperations().getSaleAssociate(pm, proposal.getOwnerKey());
                Consumer saConsumerRecord = getConsumerOperations().getConsumer(pm, saleAssociate.getConsumerKey());
                Locale locale = saConsumerRecord.getLocale();

                MessageGenerator msgGen = new MessageGenerator(saConsumerRecord.getPreferredConnection(), proposal.getHashTags(), locale);
                msgGen.
                    put("proposal>owner>name", saConsumerRecord.getName()).
                    fetch(proposal).
                    fetch(demand).
                    put("message>footer", msgGen.getAlternateMessage(MessageId.messageFooter)).
                    put("command>footer", LabelExtractor.get(ResourceFileId.fourth, "command_message_footer", locale));

                String cancelProposal = LabelExtractor.get(ResourceFileId.fourth, "command_message_body_proposal_cancel", msgGen.getParameters(), locale);
                String closeProposal = LabelExtractor.get(ResourceFileId.fourth, "command_message_body_proposal_close", msgGen.getParameters(), locale);
                String subject = null;
                if (Source.mail.equals(msgGen.getCommunicationChannel()) && Source.mail.equals(proposal.getSource())) {
                    subject = getRawCommandOperations().getRawCommand(pm, proposal.getRawCommandId()).getSubject();
                }
                if (subject == null) {
                    subject = msgGen.getAlternateMessage(MessageId.messageSubject, msgGen.getParameters());
                }
                subject = MailConnector.prepareSubjectAsResponse(subject, locale);

                msgGen.
                    put("command>threadSubject", BaseConnector.prepareMailToSubject(MailConnector.prepareSubjectAsResponse(subject, locale))).
                    put("command>cancelProposal", BaseConnector.prepareMailToBody(cancelProposal)).
                    put("command>closeProposal", BaseConnector.prepareMailToBody(closeProposal));

                communicateToConsumer(
                        msgGen.getCommunicationChannel(),
                        subject,
                        saConsumerRecord,
                        new String[] { msgGen.getMessage(MessageId.PROPOSAL_RATING_OK_TO_ASSOCIATE) }
                );
            }
        }

        return proposal;
    }

    protected static void prepareProposalClosingByTheRobot(PersistenceManager pm, Long proposalKey) {
        // Simulated interaction
        RawCommand consequence = new RawCommand();
        consequence.setCommand(Prefix.action + CommandLineParser.PREFIX_SEPARATOR + Action.close + " " + Prefix.proposal + CommandLineParser.PREFIX_SEPARATOR + proposalKey);
        consequence.setSource(Source.robot);

        // Persist message
        consequence = getRawCommandOperations().createRawCommand(pm, consequence);

        // Create a task for that command
        MaelzelServlet.triggerCommandProcessorTask(consequence.getKey());
    }

    /**
     * Utility method deleting the identified proposal
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param proposalKey Resource identifier
     * @param owner Sale associate who owns the proposal to be deleted
     * @param saConsumerRecord Consumer record attached to the sale associate
     *
     * @throws DataSourceException if the retrieval of the last created proposal or of the location information fail
     * @throws InvalidIdentifierException if there's an issue with the proposal identifier is invalid
     * @throws InvalidStateException if the proposal is not already cancelled
     */
    public static void deleteProposal(PersistenceManager pm, Long proposalKey, SaleAssociate owner, Consumer saConsumerRecord) throws DataSourceException, InvalidIdentifierException, InvalidStateException {

        Proposal proposal = getProposalOperations().getProposal(pm, proposalKey, owner.getKey(), null);

        State currentState = proposal.getState();
        if (!State.cancelled.equals(currentState)) {
            throw new InvalidStateException("Invalid state change attempt to: " + State.markedForDeletion.toString(), currentState.toString(), State.markedForDeletion.toString());
        }

        proposal.setState(State.markedForDeletion);
        proposal.setMarkedForDeletion(Boolean.TRUE);
        getProposalOperations().updateProposal(pm, proposal);
    }
}
