package twetailer.remote;

import java.io.IOException;

import com.google.appengine.tools.remoteapi.RemoteApiInstaller;
import com.google.appengine.tools.remoteapi.RemoteApiOptions;

public class ConnectionUtils {

    public enum SetupChoice {
        LOCALHOST,
        TWETAILER,
        ANOTHERSOCIALECONOMY
    };

    private final static String username = "twetailer@gmail.com";
    private final static String password = "St3v3nR1ckD0m@MTL";

    public static RemoteApiInstaller setup(SetupChoice hostChoice) throws IOException {
        RemoteApiInstaller installer = new RemoteApiInstaller();
        installer.install(new RemoteApiOptions()
            .server(
                SetupChoice.ANOTHERSOCIALECONOMY.equals(hostChoice) ? "anothersocialeconomy.appspot.com" : SetupChoice.TWETAILER.equals(hostChoice) ? "twetailer.appspot.com" : "localhost",
                SetupChoice.ANOTHERSOCIALECONOMY.equals(hostChoice) || SetupChoice.TWETAILER.equals(hostChoice) ? 443 : 9999
            )
            .remoteApiPath("/_remote_api")
            .credentials(username, password)
        );
        return installer;
    }
}
