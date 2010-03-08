package twetailer.task.command;

import static twetailer.connector.BaseConnector.communicateToConsumer;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.RawCommand;
import twetailer.task.CommandProcessor;
import twetailer.validator.LocaleValidator;
import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.JsonObject;

public class LanguageCommandProcessor {
    public static void processLanguageCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command) throws ClientException, DataSourceException {
        //
        // Used by the resource owner to change its current language
        //
        String message;
        String currentLanguage = consumer.getLanguage();
        if (!command.containsKey(Demand.CRITERIA_ADD)) {
            message = LabelExtractor.get(
                    "cp_command_language_missing_language_code",
                    new Object[] {
                            currentLanguage,
                            domderrien.i18n.LocaleController.getLanguageListRB().getString(currentLanguage)
                    },
                    consumer.getLocale()
            );
        }
        else {
            String newLanguage = LocaleValidator.checkLanguage(command.getJsonArray(Demand.CRITERIA_ADD).getString(0));
            if (consumer.getLanguage().equals(newLanguage)) {
                message = LabelExtractor.get(
                        "cp_command_language_given_value_as_current",
                        new Object[] {
                                currentLanguage,
                                domderrien.i18n.LocaleController.getLanguageListRB().getString(currentLanguage)
                        },
                        consumer.getLocale()
                );
            }
            else {
                consumer.setLanguage(newLanguage);
                CommandProcessor.consumerOperations.updateConsumer(pm, consumer);

                message = LabelExtractor.get(
                        "cp_command_language_given_value_accepted",
                        new Object[] {
                                newLanguage,
                                domderrien.i18n.LocaleController.getLanguageListRB().getString(newLanguage),
                                currentLanguage
                        },
                        consumer.getLocale()
                );
            }
        }
        communicateToConsumer(
                rawCommand,
                consumer,
                new String[] { message }
        );
    }
}
