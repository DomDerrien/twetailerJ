package twetailer.task.command;

import static twetailer.connector.BaseConnector.communicateToConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dto.Command;
import twetailer.dto.Consumer;
import twetailer.dto.Proposal;
import twetailer.dto.RawCommand;
import twetailer.dto.SaleAssociate;
import twetailer.task.CommandProcessor;
import twetailer.task.step.SaleAssociateSteps;
import twetailer.validator.CommandSettings.Action;
import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class SupplyCommandProcessor {

    private static JsonObject supplyParameters = new GenericJsonObject();

    private static JsonObject getFreshSupplyParameters(JsonObject command) {
        supplyParameters.removeAll();
        if (command.containsKey(Command.CRITERIA)) { supplyParameters.put(SaleAssociate.CRITERIA, command.getJsonArray(Command.CRITERIA)); }
        if (command.containsKey(Command.CRITERIA_ADD)) { supplyParameters.put(SaleAssociate.CRITERIA_ADD, command.getJsonArray(Command.CRITERIA_ADD)); }
        if (command.containsKey(Command.CRITERIA_REMOVE)) { supplyParameters.put(SaleAssociate.CRITERIA_REMOVE, command.getJsonArray(Command.CRITERIA_REMOVE)); }
        if (command.containsKey(Command.HASH_TAGS)) { supplyParameters.put(SaleAssociate.HASH_TAGS, command.getJsonArray(Command.HASH_TAGS)); }
        if (command.containsKey(Command.HASH_TAGS_ADD)) { supplyParameters.put(SaleAssociate.HASH_TAGS_ADD, command.getJsonArray(Command.HASH_TAGS_ADD)); }
        if (command.containsKey(Command.HASH_TAGS_REMOVE)) { supplyParameters.put(SaleAssociate.HASH_TAGS_REMOVE, command.getJsonArray(Command.HASH_TAGS_REMOVE)); }
        return supplyParameters;
    }

    public static void processSupplyCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command) throws ClientException, DataSourceException {

        SaleAssociate saleAssociate = CommandProcessor.retrieveSaleAssociate(pm, consumer, Action.supply, Proposal.class.getName());

        JsonObject supplyParameters = getFreshSupplyParameters(command);
        if (0 < supplyParameters.size()) {
            saleAssociate = SaleAssociateSteps.updateSaleAssociate(pm, saleAssociate.getKey(), supplyParameters, consumer, saleAssociate, false);
        }

        // Echo back the updated supplied tag list
        Locale locale = consumer.getLocale();
        int tagNb = saleAssociate.getCriteria() == null ? 0 : saleAssociate.getCriteria().size();
        int hashTagNb = saleAssociate.getCriteria() == null ? 0 : saleAssociate.getHashTags().size();
        List<String> messages = new ArrayList<String>();
        if (tagNb == 0 && hashTagNb == 0) {
            messages.add(LabelExtractor.get("cp_command_supply_empty_tag_hashtag_list", locale));
        }
        else {
            if (tagNb == 0) {
                messages.add(LabelExtractor.get("cp_command_supply_empty_tag_list", locale));
            }
            else if (tagNb == 1) {
                messages.add(LabelExtractor.get("cp_command_supply_updated_1_tag_list", new Object[] { saleAssociate.getCriteria().get(0) }, locale));
            }
            else {
                String tags = LabelExtractor.get("cp_tweet_tags_part", new Object[] { saleAssociate.getSerializedCriteria() }, locale);
                messages.add(LabelExtractor.get("cp_command_supply_updated_n_tag_list", new Object[] { tags, tagNb }, locale));
            }
            if (hashTagNb == 0) {
                messages.add(LabelExtractor.get("cp_command_supply_empty_hashtag_list", locale));
            }
            else if (hashTagNb == 1) {
                String hashTags = LabelExtractor.get("cp_tweet_hashtags_part", new Object[] { saleAssociate.getSerializedHashTags() }, locale);
                messages.add(LabelExtractor.get("cp_command_supply_updated_1_hashtag_list", new Object[] { hashTags }, locale));
            }
            else {
                String hashTags = LabelExtractor.get("cp_tweet_hashtags_part", new Object[] { saleAssociate.getSerializedHashTags() }, locale);
                messages.add(LabelExtractor.get("cp_command_supply_updated_n_hashtag_list", new Object[] { hashTags, hashTagNb }, locale));
            }
        }
        communicateToConsumer(
                rawCommand,
                consumer,
                messages.toArray(new String[messages.size()])

        );
    }
}
