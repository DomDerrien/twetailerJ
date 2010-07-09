package twetailer.dto;

import java.util.Arrays;
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

    public final static String DEMO = "demo";
    public final static String GOLF = "golf";
    public final static String TAXI = "taxi";

    private final static String[] VALID_HASHTAGS_ARRAY = new String[] {
        DEMO,
        GOLF,
        TAXI
    };

    private static List<String> VALID_HASHTAGS_LIST = Arrays.asList(VALID_HASHTAGS_ARRAY);

    public static String[] getHashTagsArray() {
        return VALID_HASHTAGS_ARRAY;
    }

    public static List<String> getHashTagsList() {
        return VALID_HASHTAGS_LIST;
    }

    public static String getVocabularySetIdentifier(Command command) {
        String identifier = "";
        if (command.getHashTags().contains(HashTag.DEMO)) {
            // Demo hash tag has no effect
        }
        if (command.getHashTags().contains(HashTag.GOLF)) {
            identifier = "golf_";
        }
        return identifier;
    }
}
