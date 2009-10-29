package twetailer.j2ee;

import com.google.wave.api.ProfileServlet;

@SuppressWarnings("serial")
public class WaveProfileServlet extends ProfileServlet {

    @Override
    public String getRobotAvatarUrl() {
        return "http://twetailer.appspot.com/images/logo/logo-48x48.png";
    }

    @Override
    public String getRobotName() {
        return "Twetailer";
    }

    @Override
    public String getRobotProfilePageUrl() {
        return "http://www.twetailer.com/";
    }
}
