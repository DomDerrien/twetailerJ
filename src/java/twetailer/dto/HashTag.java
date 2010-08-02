package twetailer.dto;

import java.util.ArrayList;
import java.util.List;

//
// TODO: move this class in Settings and have the list as an attribute there
//

/**
 * Define the list of supported hash tags
 *
 * @see twetailer.dto.Commnad
 *
 * @author Dom Derrien
 */
public class HashTag {

    public enum RegisteredHashTag {
        demo,
        golf,
        taxi
    }

    private static List<String> supportedHashTags;

    public static List<String> getSupportedHashTags() {
        if (supportedHashTags == null) {
            supportedHashTags = new ArrayList<String>(RegisteredHashTag.values().length);
            RegisteredHashTag[] registeredValues = RegisteredHashTag.values();
            for(int i=0; i<registeredValues.length; i++) {
                supportedHashTags.add(registeredValues[i].toString());
            }
        }
        return supportedHashTags;
    }

    public static boolean isSupportedHashTag(String hashTag) {
        return getSupportedHashTags().contains(hashTag.toLowerCase());
    }
}
