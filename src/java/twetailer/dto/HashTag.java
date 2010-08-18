package twetailer.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static Map<String, RegisteredHashTag> equivalents;
    static {
        equivalents = new HashMap<String, RegisteredHashTag>();
        equivalents.put("eztoff", RegisteredHashTag.golf);
    }

    private static List<String> supportedHashTags;
    static {
        supportedHashTags = new ArrayList<String>(RegisteredHashTag.values().length);
        RegisteredHashTag[] registeredValues = RegisteredHashTag.values();
        for(int i=0; i<registeredValues.length; i++) {
            supportedHashTags.add(registeredValues[i].toString());
        }
    }

    public static List<String> getSupportedHashTags() {
        return supportedHashTags;
    }

    public static boolean isSupportedHashTag(String hashTag) {
        if (supportedHashTags.contains(hashTag.toLowerCase())) {
            return true;
        }
        if (equivalents.containsKey(hashTag.toLowerCase())) {
            return true;
        }
        return false;
    }

    public static String getSupportedHashTag(String hashTag) {
        if (supportedHashTags.contains(hashTag.toLowerCase())) {
            return hashTag;
        }
        if (equivalents.containsKey(hashTag.toLowerCase())) {
            return equivalents.get(hashTag).toString();
        }
        return null;
    }
}
