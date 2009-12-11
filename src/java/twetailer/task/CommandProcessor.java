package twetailer.task;

import static twetailer.connector.BaseConnector.communicateToConsumer;

import java.io.PrintStream;
import java.text.Collator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javamocks.io.MockOutputStream;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.ReservedOperationException;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.DemandOperations;
import twetailer.dao.LocationOperations;
import twetailer.dao.ProposalOperations;
import twetailer.dao.RawCommandOperations;
import twetailer.dao.SaleAssociateOperations;
import twetailer.dao.SettingsOperations;
import twetailer.dao.StoreOperations;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.Location;
import twetailer.dto.Proposal;
import twetailer.dto.RawCommand;
import twetailer.dto.SaleAssociate;
import twetailer.dto.Store;
import twetailer.task.command.CancelCommandProcessor;
import twetailer.task.command.CloseCommandProcessor;
import twetailer.task.command.ConfirmCommandProcessor;
import twetailer.task.command.DeclineCommandProcessor;
import twetailer.task.command.DeleteCommandProcessor;
import twetailer.task.command.DemandCommandProcessor;
import twetailer.task.command.HelpCommandProcessor;
import twetailer.task.command.ListCommandProcessor;
import twetailer.task.command.ProposeCommandProcessor;
import twetailer.task.command.SupplyCommandProcessor;
import twetailer.task.command.WWWCommandProcessor;
import twetailer.task.command.WishCommandProcessor;
import twetailer.validator.CommandSettings;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.Prefix;
import domderrien.i18n.DateUtils;
import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.JsonObject;

public class CommandProcessor {
    private static Logger log = Logger.getLogger(CommandProcessor.class.getName());

    // References made public for the business logic located in package twetailer.task.command
    public static BaseOperations _baseOperations = new BaseOperations();
    public static ConsumerOperations consumerOperations = _baseOperations.getConsumerOperations();
    public static DemandOperations demandOperations = _baseOperations.getDemandOperations();
    public static LocationOperations locationOperations = _baseOperations.getLocationOperations();
    public static ProposalOperations proposalOperations = _baseOperations.getProposalOperations();
    public static RawCommandOperations rawCommandOperations = _baseOperations.getRawCommandOperations();
    public static SaleAssociateOperations saleAssociateOperations = _baseOperations.getSaleAssociateOperations();
    public static StoreOperations storeOperations = _baseOperations.getStoreOperations();
    public static SettingsOperations settingsOperations = _baseOperations.getSettingsOperations();

    // Setter for injection of a MockLogger at test time
    protected static void setLogger(Logger mock) {
        log = mock;
    }

    /**
     * Prepare a message to be submit a user
     *
     * @param demand Demand to process
     * @param location Place where the demand is attached to
     * @param locale Indicator for the localized resource bundle to use
     * @return Serialized command
     */
    public static String generateTweet(Demand demand, Location location, Locale locale) {
        final String space = " ";
        StringBuilder tweet = new StringBuilder();
        JsonObject prefixes = CommandLineParser.localizedPrefixes.get(locale);
        JsonObject actions = CommandLineParser.localizedActions.get(locale);
        tweet.append(prefixes.getJsonArray(Prefix.action.toString()).getString(0)).append(":").append(actions.getJsonArray(demand.getAction().toString()).getString(0)).append(space);
        if (demand.getKey() != null) {
            tweet.append(prefixes.getJsonArray(Prefix.reference.toString()).getString(0)).append(":").append(demand.getKey()).append(space);
        }
        JsonObject states = CommandLineParser.localizedStates.get(locale);
        tweet.append(prefixes.getJsonArray(Prefix.state.toString()).getString(0)).append(":").append(states.getString(demand.getState().toString())).append(space);
        tweet.append(prefixes.getJsonArray(Prefix.expiration.toString()).getString(0)).append(":").append(DateUtils.dateToYMD(demand.getExpirationDate())).append(space);
        if (location != null && location.getPostalCode() != null) { // && location.getCountryCode() != null) { // CountryCode always set!
            tweet.append(prefixes.getJsonArray(Prefix.locale.toString()).getString(0)).append(":").append(location.getPostalCode()).append(space).append(location.getCountryCode()).append(space);
        }
        tweet.append(prefixes.getJsonArray(Prefix.range.toString()).getString(0)).append(":").append(demand.getRange()).append(demand.getRangeUnit()).append(space);
        tweet.append(prefixes.getJsonArray(Prefix.quantity.toString()).getString(0)).append(":").append(demand.getQuantity()).append(space);
        if (0 < demand.getCriteria().size()) {
            tweet.append(prefixes.getJsonArray(Prefix.tags.toString()).getString(0)).append(":");
            for (String tag: demand.getCriteria()) {
                tweet.append(tag).append(space);
            }
        }
        return tweet.toString();
    }

    /**
     * Prepare a message to be submit a user
     *
     * @param proposal Proposal to process
     * @param locale Indicator for the localized resource bundle to use
     * @return Serialized command
     */
    public static String generateTweet(Proposal proposal, Locale locale) {
        //
        // TODO: use the storeKey to send the store information?
        // TODO: with the demand containing a list of proposal keys, use of the proposal key index in that table as a proposal key equivalent
        //
        final String space = " ";
        StringBuilder tweet = new StringBuilder();
        JsonObject prefixes = CommandLineParser.localizedPrefixes.get(locale);
        JsonObject actions = CommandLineParser.localizedActions.get(locale);
        tweet.append(prefixes.getJsonArray(Prefix.action.toString()).getString(0)).append(":").append(actions.getJsonArray(proposal.getAction().toString()).getString(0)).append(space);
        if (proposal.getKey() != null) {
            tweet.append(prefixes.getJsonArray(Prefix.proposal.toString()).getString(0)).append(":").append(proposal.getKey()).append(space);
        }
        if (proposal.getDemandKey() != null) {
            tweet.append(prefixes.getJsonArray(Prefix.reference.toString()).getString(0)).append(":").append(proposal.getDemandKey()).append(space);
        }
        JsonObject states = CommandLineParser.localizedStates.get(locale);
        tweet.append(prefixes.getJsonArray(Prefix.state.toString()).getString(0)).append(":").append(states.getString(proposal.getState().toString())).append(space);
        tweet.append(prefixes.getJsonArray(Prefix.store.toString()).getString(0)).append(":").append(proposal.getStoreKey()).append(space);
        tweet.append(prefixes.getJsonArray(Prefix.quantity.toString()).getString(0)).append(":").append(proposal.getQuantity()).append(space);
        if (0 < proposal.getCriteria().size()) {
            tweet.append(prefixes.getJsonArray(Prefix.tags.toString()).getString(0)).append(":");
            for (String tag: proposal.getCriteria()) {
                tweet.append(tag).append(space);
            }
        }
        tweet.append(prefixes.getJsonArray(Prefix.price.toString()).getString(0)).append(":").append(proposal.getPrice()).append(space);
        tweet.append(prefixes.getJsonArray(Prefix.total.toString()).getString(0)).append(":").append(proposal.getTotal()).append(space);
        return tweet.toString();
    }

    /**
     * Prepare a message to be submit a user
     *
     * @param store Store to process
     * @param location Place where the store is located
     * @param locale Indicator for the localized resource bundle to use
     * @return Serialized command
     */
    public static String generateTweet(Store store, Location location, Locale locale) {
        final String space = " ";
        StringBuilder tweet = new StringBuilder();
        JsonObject prefixes = CommandLineParser.localizedPrefixes.get(locale);
        tweet.append(prefixes.getJsonArray(Prefix.store.toString()).getString(0)).append(":").append(store.getKey().toString()).append(space);
        tweet.append(prefixes.getJsonArray(Prefix.name.toString()).getString(0)).append(":").append(store.getName()).append(space);
        tweet.append(prefixes.getJsonArray(Prefix.address.toString()).getString(0)).append(":").append(store.getAddress()).append(space);
        if (store.getPhoneNumber() != null) {
            tweet.append(prefixes.getJsonArray(Prefix.phoneNumber.toString()).getString(0)).append(":").append(store.getPhoneNumber()).append(space);
        }
        if (location != null && location.getPostalCode() != null) { // && location.getCountryCode() != null) { // CountryCode always set!
            tweet.append(prefixes.getJsonArray(Prefix.locale.toString()).getString(0)).append(":").append(location.getPostalCode()).append(space).append(location.getCountryCode()).append(space);
        }
        return tweet.toString();
    }

    /**
     * Extract commands from the tables of raw commands and acts accordingly
     *
     * @param rawCommandKey Identifier of the raw command to process
     *
     * @throws DataSourceException If the data manipulation fails
     * @throws ClientException If the communication back with the user fails
     */
    public static void processRawCommands(Long rawCommandKey) throws DataSourceException, ClientException {
        PersistenceManager pm = _baseOperations.getPersistenceManager();
        try {
            processRawCommands(pm, rawCommandKey);
        }
        finally {
            pm.close();
        }
    }

    public final static String DEBUG_INFO_SWITCH = "-lusanga-debug-info";

    /**
     * Extract commands from the tables of raw commands and acts accordingly
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param rawCommandKey Identifier of the raw command to process
     *
     * @throws DataSourceException If the data manipulation fails
     * @throws ClientException If the communication back with the user fails
     */
    protected static void processRawCommands(PersistenceManager pm, Long rawCommandKey) throws DataSourceException, ClientException {
        // Get the identified raw command
        RawCommand rawCommand = rawCommandOperations.getRawCommand(pm, rawCommandKey);

        // Get the record of the command emitter
        Consumer consumer = retrieveConsumer(pm, rawCommand);
        Locale senderLocale = consumer.getLocale();

        // Load the definitions for the sender locale
        CommandLineParser.loadLocalizedSettings(senderLocale);
        Map<String, Pattern> patterns = CommandLineParser.localizedPatterns.get(senderLocale);

        try {
            // Extract information from the short message and process the information
            JsonObject command = CommandLineParser.parseCommand(patterns, rawCommand.getCommand(), consumer.getLocale());
            command.put(Command.SOURCE, rawCommand.getSource().toString());
            processCommand(pm, consumer, rawCommand, command);
        }
        catch(Exception ex) {
            String additionalInfo = getDebugInfo(ex);
            boolean exposeInfo = rawCommand.getCommand() != null && rawCommand.getCommand().contains(DEBUG_INFO_SWITCH);
            log.info("Error reported while processing rawCommand: " + rawCommand.getKey() + ", with the info: " + additionalInfo);
            if (exposeInfo) {
                MockOutputStream out = new MockOutputStream();
                ex.printStackTrace(new PrintStream(out));
                log.warning(out.toString());
            }
            // Report the error to the raw command emitter
            communicateToConsumer(
                    rawCommand,
                    consumer,
                    LabelExtractor.get("cp_unexpected_error", new Object[] { rawCommand.getKey(), exposeInfo ? additionalInfo : "" }, Locale.ENGLISH)
            );
            // Save the error information for further debugging
            rawCommand.setErrorMessage(additionalInfo);
            rawCommand = rawCommandOperations.updateRawCommand(pm, rawCommand);
        }
    }

    protected static String getDebugInfo(Exception ex) {
        return ex.getClass().getName() + " / " + ex.getMessage();
    }

    /**
     * Helper querying the data store to retrieve the Consumer who matches the command emitter's information.
     * Because the raw command exists, it's assumed the emitter's record exists.
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param rawCommand command as received by the system
     * @return The emitter Consumer record

     * @throws DataSourceException If no Consumer record match the raw command emitter
     */
    protected static Consumer retrieveConsumer(PersistenceManager pm, RawCommand rawCommand) throws DataSourceException {
        if (Source.simulated.equals(rawCommand.getSource())) {
            Consumer consumer = new Consumer();
            consumer.setName(rawCommand.getEmitterId());
            return consumer;
        }
        List<Consumer> consumers = null;
        if (Source.twitter.equals(rawCommand.getSource())) {
            consumers = consumerOperations.getConsumers(pm, Consumer.TWITTER_ID, rawCommand.getEmitterId(), 1);
        }
        else if (Source.jabber.equals(rawCommand.getSource())) {
            consumers = consumerOperations.getConsumers(pm, Consumer.JABBER_ID, rawCommand.getEmitterId(), 1);
        }
        else if (Source.mail.equals(rawCommand.getSource())) {
            consumers = consumerOperations.getConsumers(pm, Consumer.EMAIL, rawCommand.getEmitterId(), 1);
        }
        else {
            throw new DataSourceException("Provider " + rawCommand.getSource() + " not yet supported");
        }
        if (consumers.size() == 0) {
            throw new DataSourceException("Record for " + rawCommand.getSource().toString() + ": '" + rawCommand.getEmitterId() + "' not found!");
        }
        return consumers.get(0);
    }

    /**
     * Helper querying the data store to retrieve the SaleAssociate account attached to the given Consumer one.
     * If no SaleAssociate account is found, the error is reported that the corresponding action which required a SaleAssociate account failed.
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param consumer Consumer account to consider
     * @param action Action involved for that lookup
     * @return The SaleAssociate account
     *
     * @throws DataSourceException If the data retrieval fails
     * @throws ReservedOperationException If no account is returned
     */
    public static SaleAssociate retrieveSaleAssociate(PersistenceManager pm, Consumer consumer, Action action) throws DataSourceException, ReservedOperationException {
        List<SaleAssociate> saleAssociates = saleAssociateOperations.getSaleAssociates(pm, SaleAssociate.CONSUMER_KEY, consumer.getKey(), 1);
        if (saleAssociates.size() == 0) {
            throw new ReservedOperationException(action);
        }
        return saleAssociates.get(0);
    }

    /**
     * Dispatch the tweeted command according to the corresponding action
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param consumer originator of the raw command
     * @param rawCommand raw command with emitter coordinates
     * @param command parsed command
     *
     * @throws DataSourceException If the communication with the back-end fails
     * @throws ClientException If information extracted from the tweet are incorrect
     */
    public static void processCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command) throws DataSourceException, ClientException {
        Locale locale = consumer.getLocale();
        JsonObject prefixes = CommandLineParser.localizedPrefixes.get(locale);
        JsonObject actions = CommandLineParser.localizedActions.get(locale);
        // Prepare for locale dependent comparisons
        Collator collator = Collator.getInstance(locale);
        collator.setStrength(Collator.PRIMARY);
        // Clear case of help being requested at the prefix level
        if (command.containsKey(Command.NEED_HELP)) {
            HelpCommandProcessor.processHelpCommand(
                    rawCommand,
                    command.getString(Command.NEED_HELP),
                    locale,
                    collator
            );
            return;
        }
        String action = guessAction(command);
        try {
            // Alternate case of the help being asked as an action...
            if (CommandSettings.isEquivalentTo(prefixes, Prefix.help.toString(), action, collator)) {
                HelpCommandProcessor.processHelpCommand(
                        rawCommand,
                        command.containsKey(Demand.CRITERIA_ADD) ? command.getJsonArray(Demand.CRITERIA_ADD).getString(0) : "",
                        locale,
                        collator
                );
            }
            else if (CommandSettings.isEquivalentTo(actions, Action.cancel.toString(), action, collator)) {
                CancelCommandProcessor.processCancelCommand(pm, consumer, rawCommand, command);
            }
            else if (CommandSettings.isEquivalentTo(actions, Action.close.toString(), action, collator)) {
                CloseCommandProcessor.processCloseCommand(pm, consumer, rawCommand, command);
            }
            else if (CommandSettings.isEquivalentTo(actions, Action.confirm.toString(), action, collator)) {
                ConfirmCommandProcessor.processConfirmCommand(pm, consumer, rawCommand, command);
            }
            else if (CommandSettings.isEquivalentTo(actions, Action.decline.toString(), action, collator)) {
                DeclineCommandProcessor.processDeclineCommand(pm, consumer, rawCommand, command);
            }
            else if (CommandSettings.isEquivalentTo(actions, Action.delete.toString(), action, collator)) {
                DeleteCommandProcessor.processDeleteCommand(pm, consumer, rawCommand, command);
            }
            else if (CommandSettings.isEquivalentTo(actions, Action.demand.toString(), action, collator)) {
                DemandCommandProcessor.processDemandCommand(pm, consumer, rawCommand, command, prefixes, actions);
            }
            else if (CommandSettings.isEquivalentTo(actions, Action.list.toString(), action, collator)) {
                ListCommandProcessor.processListCommand(pm, consumer, rawCommand, command, prefixes, actions);
            }
            else if (CommandSettings.isEquivalentTo(actions, Action.propose.toString(), action, collator)) {
                ProposeCommandProcessor.processProposeCommand(pm, consumer, rawCommand, command);
            }
            else if (CommandSettings.isEquivalentTo(actions, Action.supply.toString(), action, collator)) {
                SupplyCommandProcessor.processSupplyCommand(pm, consumer, rawCommand, command);
            }
            else if (CommandSettings.isEquivalentTo(actions, Action.wish.toString(), action, collator)) {
                WishCommandProcessor.processWishCommand(pm, consumer, rawCommand, command);
            }
            else if (CommandSettings.isEquivalentTo(actions, Action.www.toString(), action, collator)) {
                WWWCommandProcessor.processWWWCommand(pm, consumer, rawCommand, command);
            }
            else {
                communicateToConsumer(
                        rawCommand,
                        consumer,
                        LabelExtractor.get("cp_command_parser_unsupported_action", new Object[] { action }, locale)
                );
            }
        }
        catch(ReservedOperationException ex) {
            communicateToConsumer(
                    rawCommand,
                    consumer,
                    LabelExtractor.get("cp_command_parser_reserved_action", new Object[] { action }, locale)
            );
        }
    }

    /**
     * Utility function extracting the action, even if the attribute is not present, by looking at all given parameters
     * @param command Set of command attributes
     * @return Specified or guessed action
     */
    protected static String guessAction(JsonObject command) {
        String action = command.getString(Command.ACTION);
        if (action == null) {
            if (command.containsKey(Demand.REFERENCE)) {
                action = command.size() == 1 ? Action.list.toString() : Action.demand.toString();
            }
            else if (command.containsKey(Store.STORE_KEY)) {
                action = command.size() == 1 ? Action.list.toString() : null; // No possibility to create/update/delete Store instance from Twitter
            }
            /* TODO: implement other listing variations
            else if (command.containsKey(Product.PRODUCT_KEY)) {
                action = command.size() == 1 ? Action.list.toString() : null; // No possibility to create/update/delete Store instance from Twitter
            }
            */
            else {
                action = Action.demand.toString();
            }
        }
        return action;
    }
}
