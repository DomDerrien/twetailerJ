package twitter4j;

import twitter4j.User;
import twitter4j.org.json.JSONException;
import twitter4j.org.json.JSONObject;

@SuppressWarnings("serial")
public class MockTwitterUser extends User {

    public static final int USER_ID = 256;
    public static final String TEST_STR = "test";

    private static JSONObject fakeData = new JSONObject();
    static {
        try {
            fakeData.put("id", USER_ID);
            fakeData.put("name", TEST_STR);
            fakeData.put("screen_name", TEST_STR);
            fakeData.put("location", TEST_STR);
            fakeData.put("description", TEST_STR);
            fakeData.put("profile_image_url", TEST_STR);
            fakeData.put("url", TEST_STR);
            fakeData.put("protected", true);
            fakeData.put("followers_count", 0);
            fakeData.put("profile_background_color", TEST_STR);
            fakeData.put("profile_text_color", TEST_STR);
            fakeData.put("profile_link_color", TEST_STR);
            fakeData.put("profile_sidebar_fill_color", TEST_STR);
            fakeData.put("profile_sidebar_border_color", TEST_STR);
            fakeData.put("friends_count", 0);
            fakeData.put("created_at", "Wed Jul 04 12:27:58 -0700 2001"); // "EEE MMM dd HH:mm:ss z yyyy";
            fakeData.put("favourites_count", 0);
            fakeData.put("utc_offset", 0);
            fakeData.put("time_zone", TEST_STR);
            fakeData.put("profile_background_image_url", TEST_STR);
            fakeData.put("profile_background_tile", TEST_STR);
            fakeData.put("following", true);
            fakeData.put("notifications", true);
            fakeData.put("statuses_count", 0);
        }
        catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public MockTwitterUser() throws TwitterException {
        super(fakeData);
    }
}
