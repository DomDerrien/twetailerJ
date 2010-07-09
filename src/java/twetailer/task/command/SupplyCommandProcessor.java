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
            saleAssociate = SaleAssociateSteps.updateSaleAssociate(pm, saleAssociate.getKey(), supplyParameters, saleAssociate, consumer);
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

        /******** ddd
        //
        // Used by a sale associate to add/remove tags to his supply list
        //

        // Process the command for the identifier sale associate
        boolean updateDetected = false;
        Consumer saConsumerRecord = BaseSteps.getConsumerOperations().getConsumer(pm, saleAssociate.getConsumerKey());
        if (command.containsKey(SaleAssociate.CRITERIA)) {
            saleAssociate.resetCriteria();
            List<Object> tags = command.getJsonArray(SaleAssociate.CRITERIA).getList();
            for (Object tag : tags) {
                saleAssociate.addCriterion((String) tag);
            }
            updateDetected = true;
        }
        if (command.containsKey(SaleAssociate.CRITERIA_ADD)) {
            int currentSize = saleAssociate.getCriteria().size();
            List<Object> tags = command.getJsonArray(SaleAssociate.CRITERIA_ADD).getList();
            for (Object tag : tags) {
                saleAssociate.addCriterion((String) tag);
            }
            updateDetected = updateDetected || currentSize < saleAssociate.getCriteria().size();
        }
        if (command.containsKey(SaleAssociate.CRITERIA_REMOVE)) {
            int currentSize = saleAssociate.getCriteria().size();
            List<Object> tags = command.getJsonArray(SaleAssociate.CRITERIA_REMOVE).getList();
            for (Object tag : tags) {
                saleAssociate.removeCriterion((String) tag);
            }
            updateDetected = updateDetected || saleAssociate.getCriteria().size() < currentSize;
        }

        if (command.containsKey(SaleAssociate.HASH_TAGS)) {
            saleAssociate.resetHashTags();
            List<Object> tags = command.getJsonArray(SaleAssociate.HASH_TAGS).getList();
            for (Object tag : tags) {
                if (HashTag.getHashTagsList().contains((String) tag)) {
                    saleAssociate.addHashTag((String) tag);
                }
            }
            updateDetected = updateDetected || true;
        }
        if (command.containsKey(SaleAssociate.HASH_TAGS_ADD)) {
            int currentSize = saleAssociate.getHashTags().size();
            List<Object> tags = command.getJsonArray(SaleAssociate.HASH_TAGS_ADD).getList();
            for (Object tag : tags) {
                if (HashTag.getHashTagsList().contains((String) tag)) {
                    saleAssociate.addHashTag((String) tag);
                }
            }
            updateDetected = updateDetected || currentSize < saleAssociate.getHashTags().size();
        }
        if (command.containsKey(SaleAssociate.HASH_TAGS_REMOVE)) {
            int currentSize = saleAssociate.getHashTags().size();
            List<Object> tags = command.getJsonArray(SaleAssociate.HASH_TAGS_REMOVE).getList();
            for (Object tag : tags) {
                if (HashTag.getHashTagsList().contains((String) tag)) {
                    saleAssociate.removeHashTag((String) tag);
                }
            }
            updateDetected = updateDetected || saleAssociate.getHashTags().size() < currentSize;
        }

        // Persist the update if any
        if (updateDetected) {
            BaseSteps.getSaleAssociateOperations().updateSaleAssociate(pm, saleAssociate);
        }

        // Echo back the updated supplied tag list
        Locale locale = saleAssociate.getLocale();
        int tagNb = saleAssociate.getCriteria() == null ? 0 : saleAssociate.getCriteria().size();
        int hashTagNb = saleAssociate.getCriteria() == null ? 0 : saleAssociate.getHashTags().size();
        List<String> messages = new ArrayList<String>();
        if (tagNb == 0 && hashTagNb == 0) {
            messages.add(LabelExtractor.get("cp_command_supply_empty_tag_list", locale));
        }
        else {
            if (tagNb == 1) {
                messages.add(LabelExtractor.get("cp_command_supply_updated_1_tag_list", new Object[] { saleAssociate.getCriteria().get(0) }, locale));
            }
            else {
                String tags = LabelExtractor.get("cp_tweet_tags_part", new Object[] { saleAssociate.getSerializedCriteria() }, locale);
                messages.add(LabelExtractor.get("cp_command_supply_updated_n_tag_list", new Object[] { tags, tagNb }, locale));
            }
            if (hashTagNb == 1) {
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
                saConsumerRecord,
                messages.toArray(new String[messages.size()])

        );
        ddd *********/
    }
}
