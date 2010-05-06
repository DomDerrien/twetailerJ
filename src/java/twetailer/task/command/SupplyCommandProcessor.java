package twetailer.task.command;

import static twetailer.connector.BaseConnector.communicateToSaleAssociate;

import java.util.List;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dto.Consumer;
import twetailer.dto.Proposal;
import twetailer.dto.RawCommand;
import twetailer.dto.SaleAssociate;
import twetailer.task.CommandProcessor;
import twetailer.validator.CommandSettings.Action;
import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.JsonObject;

public class SupplyCommandProcessor {
    public static void processSupplyCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command) throws ClientException, DataSourceException {
        //
        // Used by a sale associate to add/remove tags to his supply list
        //

        // Process the command for the identifier sale associate
        boolean updateDetected = false;
        SaleAssociate saleAssociate = CommandProcessor.retrieveSaleAssociate(pm, consumer, Action.supply, Proposal.class.getName());
        if (command.containsKey(SaleAssociate.CRITERIA)) {
            updateDetected = true;
            saleAssociate.resetCriteria();
            List<Object> tags = command.getJsonArray(SaleAssociate.CRITERIA).getList();
            for (Object tag : tags) {
                saleAssociate.addCriterion((String) tag);
            }
        }
        if (command.containsKey(SaleAssociate.CRITERIA_ADD)) {
            updateDetected = true;
            List<Object> tags = command.getJsonArray(SaleAssociate.CRITERIA_ADD).getList();
            for (Object tag : tags) {
                saleAssociate.addCriterion((String) tag);
            }
        }
        if (command.containsKey(SaleAssociate.CRITERIA_REMOVE)) {
            updateDetected = true;
            List<Object> tags = command.getJsonArray(SaleAssociate.CRITERIA_REMOVE).getList();
            for (Object tag : tags) {
                saleAssociate.removeCriterion((String) tag);
            }
        }

        // Persist the update if any
        if (updateDetected) {
            CommandProcessor.saleAssociateOperations.updateSaleAssociate(pm, saleAssociate);
        }

        // Echo back the updated supplied tag list
        int tagNb = saleAssociate.getCriteria() == null ? 0 : saleAssociate.getCriteria().size();
        String message = null;
        if (tagNb == 0) {
            message = LabelExtractor.get("cp_command_supply_empty_tag_list", saleAssociate.getLocale());
        }
        else if (tagNb == 1) {
            message = LabelExtractor.get("cp_command_supply_updated_1_tag_list", new Object[] { saleAssociate.getCriteria().get(0) }, saleAssociate.getLocale());
        }
        else {
            message = LabelExtractor.get("cp_command_supply_updated_n_tag_list", new Object[] { saleAssociate.getSerializedCriteria(), tagNb }, saleAssociate.getLocale());
        }
        communicateToSaleAssociate(
                rawCommand,
                saleAssociate,
                new String[] { message }

        );
    }
}
