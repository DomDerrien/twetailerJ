package twetailer.task.command;

import static twetailer.connector.BaseConnector.communicateToConsumer;

import java.util.Locale;

import javax.jdo.PersistenceManager;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dto.Consumer;
import twetailer.dto.Demand;
import twetailer.dto.RawCommand;
import twetailer.task.step.ConsumerSteps;
import twetailer.validator.LocaleValidator;
import domderrien.i18n.LabelExtractor;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;

public class LanguageCommandProcessor {

    private static JsonObject languageParameters = new GenericJsonObject();

    private static JsonObject getFreshLanguagelParameters(String newLanguage) {
        languageParameters.removeAll();
        languageParameters.put(Consumer.LANGUAGE, newLanguage);
        return languageParameters;
    }

    public static void processLanguageCommand(PersistenceManager pm, Consumer consumer, RawCommand rawCommand, JsonObject command) throws ClientException, DataSourceException {

        Locale locale = consumer.getLocale();
        String currentLanguage = consumer.getLanguage();
        String messageId;
        Object[] messageParams;

        if (command.containsKey(Demand.CRITERIA_ADD)) {
            String newLanguage = LocaleValidator.checkLanguage(command.getJsonArray(Demand.CRITERIA_ADD).getString(0));
            if (consumer.getLanguage().equals(newLanguage)) {
                messageId = "cp_command_language_given_value_as_current";
                messageParams = new Object[] {
                        currentLanguage,
                        domderrien.i18n.LocaleController.getLanguageListRB().getString(currentLanguage)
                };
            }
            else {
                ConsumerSteps.updateConsumer(pm, consumer.getKey(), getFreshLanguagelParameters(newLanguage), consumer);
                locale = consumer.getLocale();

                messageId = "cp_command_language_given_value_accepted";
                messageParams = new Object[] {
                        newLanguage,
                        domderrien.i18n.LocaleController.getLanguageListRB().getString(newLanguage),
                        currentLanguage
                };
            }
        }
        else {
            messageId = "cp_command_language_missing_language_code";
            messageParams = new Object[] {
                    currentLanguage,
                    domderrien.i18n.LocaleController.getLanguageListRB().getString(currentLanguage)
            };
        }

        communicateToConsumer(
                rawCommand.getSource(),
                rawCommand.getSubject(),
                consumer,
                new String[] { LabelExtractor.get( messageId, messageParams, locale) }
        );
    }
}
