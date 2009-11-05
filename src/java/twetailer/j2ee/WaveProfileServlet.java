package twetailer.j2ee;

import twetailer.validator.ApplicationSettings;

import com.google.wave.api.ProfileServlet;

@SuppressWarnings("serial")
public class WaveProfileServlet extends ProfileServlet {

    @Override
    public String getRobotAvatarUrl() {
        return ApplicationSettings.get().getLogoURL();
    }

    @Override
    public String getRobotName() {
        return ApplicationSettings.get().getProductName();
    }

    @Override
    public String getRobotProfilePageUrl() {
        return ApplicationSettings.get().getProductWebsite();
    }
}
