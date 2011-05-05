package twetailer.task;

import static twetailer.connector.BaseConnector.communicateToConsumer;

import java.text.Collator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.InvalidIdentifierException;
import twetailer.ReservedOperationException;
import twetailer.connector.BaseConnector.Source;
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
import twetailer.task.command.LanguageCommandProcessor;
import twetailer.task.command.ListCommandProcessor;
import twetailer.task.command.ProposeCommandProcessor;
import twetailer.task.command.RateCommandProcessor;
import twetailer.task.command.SupplyCommandProcessor;
import twetailer.task.step.BaseSteps;
import twetailer.validator.CommandSettings;
import twetailer.validator.CommandSettings.Action;
import twetailer.validator.CommandSettings.Prefix;
import twetailer.validator.CommandSettings.State;
import domderrien.i18n.DateUtils;
import domderrien.i18n.LabelExtractor;
import domderrien.i18n.LabelExtractor.ResourceFileId;
import domderrien.jsontools.JsonObject;

/**
 * Define the logic of the main Twetailer task:<ul>
 * <li> Get the identified RawCommand instance which contains the text-based command; </li>
 * <li> Extract the command parameters (delegated to the CommandLineParser); </li>
 * <li> Dispatch the command to specialized task processor (!list, !propose, !confirm, etc.); </li>
 * <li> Offer the method to generate the text-based responses. </li></ul>
 *
 * @see twetailer.task.CommandLineParser
 * @see twetailer.task.connector.*
 *
 * @author Dom Derrien
 */

public class CommandProcessor {

    /**
     * Helper to print the short date if the time is set for the last second of the day
     *
     * @param date Date to process
     * @return String with the date attributes as: YYYY-MM-DD or YYYY-MM-DD'T'HH:MM:SS
     */
    @SuppressWarnings("deprecation")
    protected static String serializeDate(Date date) {
        if (date.getHours() == 23 && date.getMinutes() == 59 && date.getSeconds() == 59) {
            return DateUtils.dateToYMD(date);
        }
        return DateUtils.dateToISO(date);
    }

    /**
     * Prepare a message to be submit a user
     *
     * @param demand Demand to process
     * @param location Place where the demand is attached to
     * @param anonymized Should be <code>true</code> if specific identifiers should stay hidden
     * @param locale Indicator for the localized resource bundle to use
     * @return Serialized command
     *
     * @deprecated
     */
    public static String generateTweet(Demand demand, Location location, boolean anonymized, Locale locale) {
        String labelKeyPrefix = ""; // "golf";
        ResourceFileId resId = ResourceFileId.master; // labelKeyPrefix.length() == 0 ? ResourceFileId.master : ResourceFileId.fourth;

        final String space = Command.SPACE;
        // Get the labels for each demand attributes
        String action = LabelExtractor.get(resId, labelKeyPrefix + "cp_tweet_demand_action_part", locale) + space;
        String reference = anonymized || demand.getKey() == null ? "" : (LabelExtractor.get(resId, labelKeyPrefix + "cp_tweet_demand_reference_part", new Object[] { demand.getKey() }, locale) + space);
        String state = State.opened.equals(demand.getState()) || State.published.equals(demand.getState()) ? "" :
            LabelExtractor.get(
                    "cp_tweet_state_part",
                    new Object[] {
                            CommandSettings.getStates(locale).getString(demand.getState().toString())
                    },
                    locale
            ) +
            space;
        String dueDate = LabelExtractor.get(resId, labelKeyPrefix + "cp_tweet_dueDate_part", new Object[] { serializeDate(demand.getDueDate()) }, locale) + space;
        String expiration = demand.getExpirationDate().equals(demand.getDueDate()) ? "" : (LabelExtractor.get(resId, labelKeyPrefix + "cp_tweet_expiration_part", new Object[] { serializeDate(demand.getExpirationDate()) }, locale) + space);
        String coordinates = location == null || location.getPostalCode() == null ? "" : (LabelExtractor.get(resId, labelKeyPrefix + "cp_tweet_locale_part", new Object[] { location.getPostalCode(), location.getCountryCode() }, locale) + space);
        String range = LabelExtractor.get(resId, labelKeyPrefix + "cp_tweet_range_part", new Object[] { demand.getRange(), demand.getRangeUnit() }, locale) + space;
        String quantity = LabelExtractor.get(resId, labelKeyPrefix + "cp_tweet_quantity_part", new Object[] { demand.getQuantity() }, locale) + space;
        String tags = demand.getContent().length() == 0 ? "" : (LabelExtractor.get(resId, labelKeyPrefix + "cp_tweet_tags_part", new Object[] { demand.getContent() }, locale) + space);
        String hashtags = demand.getHashTags().size() == 0 ? "" : (LabelExtractor.get(resId, labelKeyPrefix + "cp_tweet_hashtags_part", new Object[] { demand.getSerializedHashTags() }, locale) + space);
        String proposals = anonymized || demand.getProposalKeys().size() == 0 ? "" : (LabelExtractor.get(resId, labelKeyPrefix + "cp_tweet_proposals_part", new Object[] { Command.getSerializedTags(null, Command.SEMICOLON + Command.SPACE, demand.getProposalKeys()) }, locale) + space);
        String CC = "";
        for (int i=0; i <demand.getCC().size(); i++) {
            CC += LabelExtractor.get(resId, labelKeyPrefix + "cp_tweet_cc_part", new Object[] { demand.getCC().get(i) }, locale) + space;
        }
        // Compose the final message
        return LabelExtractor.get(
                "cp_tweet_demand",
                new Object[] {
                        action,
                        reference,
                        state,
                        dueDate,
                        expiration,
                        coordinates,
                        range,
                        quantity,
                        tags,
                        hashtags,
                        proposals,
                        CC
                },
                locale
        ).trim();
    }

    /**
     * Prepare a message to be submit a user
     *
     * @param proposal Proposal to process
     * @param store Store where the Sale Associate who created the proposal works
     * @param anonymized Should be <code>true</code> if specific identifiers should stay hidden
     * @param locale Indicator for the localized resource bundle to use
     * @return Serialized command
     *
     * @deprecated
     */
    public static String generateTweet(Proposal proposal, Store store, boolean anonymized, Locale locale) {
        String labelKeyPrefix = ""; // "golf";
        ResourceFileId resId = ResourceFileId.master; // labelKeyPrefix.length() == 0 ? ResourceFileId.master : ResourceFileId.fourth;

        final String space = Command.SPACE;
        // Get the labels for each proposal attributes
        String action = LabelExtractor.get(resId, labelKeyPrefix + "cp_tweet_propose_action_part", locale) + space;
        String reference = anonymized || proposal.getKey() == null ? "" : (LabelExtractor.get(resId, labelKeyPrefix + "cp_tweet_proposal_reference_part", new Object[] { proposal.getKey() }, locale) + space);
        String demand = anonymized || proposal.getDemandKey() == null ? "" : (LabelExtractor.get(resId, labelKeyPrefix + "cp_tweet_demand_reference_part", new Object[] { proposal.getDemandKey() }, locale) + space);
        String state = State.opened.equals(proposal.getState()) || State.published.equals(proposal.getState()) ? "" :
            LabelExtractor.get(
                    "cp_tweet_state_part",
                    new Object[] {
                            CommandSettings.getStates(locale).getString(proposal.getState().toString())
                    },
                    locale
            ) +
            space;
        String quantity = LabelExtractor.get(resId, labelKeyPrefix + "cp_tweet_quantity_part", new Object[] { proposal.getQuantity() }, locale) + space;
        String dueDate = proposal.getDueDate() == null ? "" : (LabelExtractor.get(resId, labelKeyPrefix + "cp_tweet_dueDate_part", new Object[] { serializeDate(proposal.getDueDate()) }, locale) + space);
        String tags = proposal.getContent().length() == 0 ? "" : (LabelExtractor.get(resId, labelKeyPrefix + "cp_tweet_tags_part", new Object[] { proposal.getContent() }, locale) + space);
        String hashtags = proposal.getHashTags().size() == 0 ? "" : (LabelExtractor.get(resId, labelKeyPrefix + "cp_tweet_hashtags_part", new Object[] { proposal.getSerializedHashTags() }, locale) + space);
        String price = LabelExtractor.get(resId, labelKeyPrefix + "cp_tweet_price_part", new Object[] { proposal.getPrice(), "$" }, locale) + space;
        String total = LabelExtractor.get(resId, labelKeyPrefix + "cp_tweet_total_part", new Object[] { proposal.getTotal(), "$" }, locale) + space;
        String pickup = store == null ? "" : (LabelExtractor.get(resId, labelKeyPrefix + "cp_tweet_store_part", new Object[] { store.getKey(), store.getName() }, locale) + space);
        String CC = "";
        for (int i=0; i <proposal.getCC().size(); i++) {
            CC += LabelExtractor.get(resId, labelKeyPrefix + "cp_tweet_cc_part", new Object[] { proposal.getCC().get(i) }, locale) + space;
        }
        // Compose the final message
        return LabelExtractor.get(
                "cp_tweet_proposal",
                new Object[] {
                        action,
                        reference,
                        demand,
                        state,
                        quantity,
                        dueDate,
                        tags,
                        hashtags,
                        price,
                        total,
                        pickup,
                        CC
                },
                locale
        ).trim();
    }

    /**
     * Prepare a message to be submit a user
     *
     * @param store Store to process
     * @param location Place where the store is located
     * @param locale Indicator for the localized resource bundle to use
     * @return Serialized command
     *
     * @deprecated
     */
    public static String generateTweet(Store store, Location location, Locale locale) {
        final String space = Command.SPACE;
        // Get the labels for each store attributes
        String reference = store.getKey() == null ? "" : (LabelExtractor.get("cp_tweet_store_reference_part", new Object[] { store.getKey() }, locale) + space);
        String name = store.getName() == null ? "" : (LabelExtractor.get("cp_tweet_name_part", new Object[] { store.getName() }, locale) + space);
        String address = store.getAddress() == null ? "" : (LabelExtractor.get("cp_tweet_address_part", new Object[] { store.getAddress() }, locale) + space);
        String phone = store.getPhoneNumber() == null ? "" : (LabelExtractor.get("cp_tweet_phone_part", new Object[] { store.getPhoneNumber() }, locale) + space);
        String coordinates = location == null || location.getPostalCode() == null ? "" : (LabelExtractor.get("cp_tweet_locale_part", new Object[] { location.getPostalCode(), location.getCountryCode() }, locale) + space);
        // Compose the final message
        return LabelExtractor.get(
                "cp_tweet_store",
                new Object[] {
                        reference,
                        name,
                        address,
                        coordinates,
                        phone
                },
                locale
        ).trim();
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
        PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
        try {
            processRawCommands(pm, rawCommandKey);
        }
        finally {
            pm.close();
        }
    }

    public final static String DEBUG_MODE_PARAM = "debugMode";

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
        RawCommand rawCommand = BaseSteps.getRawCommandOperations().getRawCommand(pm, rawCommandKey);

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
            command.put(Command.RAW_COMMAND_ID, rawCommand.getKey());
            processCommand(pm, consumer, rawCommand, command);
        }
        catch(Exception ex) {
            String additionalInfo = getDebugInfo(ex);
            Logger.getLogger(CommandProcessor.class.getName()).severe("Unexpected error -- info: " + additionalInfo);
            boolean exposeInfo = rawCommand.getCommand() != null && rawCommand.getCommand().contains(DEBUG_MODE_PARAM);
            // Report the error to the raw command emitter
            communicateToConsumer(
                    rawCommand.getSource(),
                    rawCommand.getSubject(),
                    consumer,
                    new String[] { LabelExtractor.get("error_unexpected", new Object[] { rawCommand.getKey(), exposeInfo ? additionalInfo + ". " : "" }, senderLocale) }
            );
            // Save the error information for further debugging
            rawCommand.setErrorMessage(additionalInfo);
            rawCommand = BaseSteps.getRawCommandOperations().updateRawCommand(pm, rawCommand);
            // Rethrow the exception so the stack trace will be sent to the catch-all list
            throw new ClientException(LabelExtractor.get("error_unexpected", new Object[] { rawCommand.getKey(), additionalInfo + ". " }, Locale.ENGLISH), ex);
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
     * @throws InvalidIdentifierException If the retreival of the Robot Consumer instance fails
     */
    protected static Consumer retrieveConsumer(PersistenceManager pm, RawCommand rawCommand) throws InvalidIdentifierException, DataSourceException {
        if (Source.simulated.equals(rawCommand.getSource())) {
            Consumer consumer = new Consumer();
            consumer.setName(rawCommand.getEmitterId());
            return consumer;
        }
        if (Source.robot.equals(rawCommand.getSource())) {
            Long robotKey = RobotResponder.getRobotConsumerKey(pm);
            return BaseSteps.getConsumerOperations().getConsumer(pm, robotKey);
        }
        List<Consumer> consumers = null;
        if (Source.twitter.equals(rawCommand.getSource())) {
            consumers = BaseSteps.getConsumerOperations().getConsumers(pm, Consumer.TWITTER_ID, rawCommand.getEmitterId(), 1);
        }
        else if (Source.jabber.equals(rawCommand.getSource())) {
            consumers = BaseSteps.getConsumerOperations().getConsumers(pm, Consumer.JABBER_ID, rawCommand.getEmitterId(), 1);
        }
        else if (Source.mail.equals(rawCommand.getSource())) {
            consumers = BaseSteps.getConsumerOperations().getConsumers(pm, Consumer.EMAIL, rawCommand.getEmitterId(), 1);
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
     * @param entityClassName Name of the class concerned by the action
     * @return The SaleAssociate account
     *
     * @throws DataSourceException If the data retrieval fails
     * @throws ReservedOperationException If no account is returned
     */
    public static SaleAssociate retrieveSaleAssociate(PersistenceManager pm, Consumer consumer, Action action, String entityClassName) throws DataSourceException, ReservedOperationException {
        try {
            Long saleAssociateKey = consumer.getSaleAssociateKey();
            return BaseSteps.getSaleAssociateOperations().getSaleAssociate(pm, saleAssociateKey);
        }
        catch (Exception ex) {
            throw new ReservedOperationException(action, entityClassName);
        }
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
        String action = command.getString(Command.ACTION);
        try {
            // Alternate case of the help being asked as an action...
            if (CommandSettings.isEquivalentTo(prefixes, Prefix.help.toString(), action, collator)) {
                HelpCommandProcessor.processHelpCommand(
                        rawCommand,
                        command.containsKey(Command.CONTENT) ? command.getString(Command.CONTENT) : "",
                        locale,
                        collator
                );
            }
            else if (CommandSettings.isEquivalentTo(actions, Action.cancel.toString(), action, collator)) {
                command.put(Command.ACTION, Action.cancel.toString());
                CancelCommandProcessor.processCancelCommand(pm, consumer, rawCommand, command);
            }
            else if (CommandSettings.isEquivalentTo(actions, Action.close.toString(), action, collator)) {
                command.put(Command.ACTION, Action.close.toString());
                CloseCommandProcessor.processCloseCommand(pm, consumer, rawCommand, command);
            }
            else if (CommandSettings.isEquivalentTo(actions, Action.confirm.toString(), action, collator)) {
                command.put(Command.ACTION, Action.confirm.toString());
                ConfirmCommandProcessor.processConfirmCommand(pm, consumer, rawCommand, command);
            }
            else if (CommandSettings.isEquivalentTo(actions, Action.decline.toString(), action, collator)) {
                command.put(Command.ACTION, Action.decline.toString());
                DeclineCommandProcessor.processDeclineCommand(pm, consumer, rawCommand, command);
            }
            else if (CommandSettings.isEquivalentTo(actions, Action.delete.toString(), action, collator)) {
                command.put(Command.ACTION, Action.delete.toString());
                DeleteCommandProcessor.processDeleteCommand(pm, consumer, rawCommand, command);
            }
            else if (CommandSettings.isEquivalentTo(actions, Action.demand.toString(), action, collator)) {
                command.put(Command.ACTION, Action.demand.toString());
                DemandCommandProcessor.processDemandCommand(pm, consumer, rawCommand, command, prefixes, actions);
            }
            else if (CommandSettings.isEquivalentTo(actions, Action.language.toString(), action, collator)) {
                command.put(Command.ACTION, Action.language.toString());
                LanguageCommandProcessor.processLanguageCommand(pm, consumer, rawCommand, command);
            }
            else if (CommandSettings.isEquivalentTo(actions, Action.list.toString(), action, collator)) {
                command.put(Command.ACTION, Action.list.toString());
                ListCommandProcessor.processListCommand(pm, consumer, rawCommand, command, prefixes, actions);
            }
            else if (CommandSettings.isEquivalentTo(actions, Action.propose.toString(), action, collator)) {
                command.put(Command.ACTION, Action.propose.toString());
                ProposeCommandProcessor.processProposeCommand(pm, consumer, rawCommand, command);
            }
            else if (CommandSettings.isEquivalentTo(actions, Action.rate.toString(), action, collator)) {
                command.put(Command.ACTION, Action.rate.toString());
                RateCommandProcessor.processRateCommand(pm, consumer, rawCommand, command);
            }
            else if (CommandSettings.isEquivalentTo(actions, Action.supply.toString(), action, collator)) {
                command.put(Command.ACTION, Action.supply.toString());
                SupplyCommandProcessor.processSupplyCommand(pm, consumer, rawCommand, command);
            }
            else {
                communicateToConsumer(
                        rawCommand.getSource(),
                        rawCommand.getSubject(),
                        consumer,
                        new String[] { LabelExtractor.get("cp_command_parser_unsupported_action", new Object[] { action }, locale) }
                );
            }
        }
        catch(ReservedOperationException ex) {
            communicateToConsumer(
                    rawCommand.getSource(),
                    rawCommand.getSubject(),
                    consumer,
                    new String[] { LabelExtractor.get("cp_command_parser_reserved_action", new Object[] { action }, locale) }
            );
        }
    }
}
