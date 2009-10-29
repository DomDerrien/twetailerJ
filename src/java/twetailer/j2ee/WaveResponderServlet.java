package twetailer.j2ee;

import java.util.logging.Logger;

import com.google.wave.api.AbstractRobotServlet;
import com.google.wave.api.Blip;
import com.google.wave.api.Event;
import com.google.wave.api.EventType;
import com.google.wave.api.RobotMessageBundle;
import com.google.wave.api.TextView;
import com.google.wave.api.Wavelet;

@SuppressWarnings("serial")
public class WaveResponderServlet extends AbstractRobotServlet {
    private static final Logger log = Logger.getLogger(WaveResponderServlet.class.getName());

    @Override
    public void processEvents(RobotMessageBundle bundle) {
        Wavelet wavelet = bundle.getWavelet();

        // WAVELET_BLIP_CREATED
        // WAVELET_BLIP_REMOVED
        // WAVELET_PARTICIPANTS_CHANGED: to get create new accounts
        // WAVELET_SELF_ADDED: for the addition of the robot itself
        // WAVELET_TIMESTAMP_CHANGED: ?
        // WAVELET_TITLE_CHANGED
        // WAVELET_VERSION_CHANGED: ?
        // BLIP_CONTRIBUTORS_CHANGED? how is it compared to WAVELET_PARTICIPANTS_CHANGED?
        // BLIP_DELETED
        // BLIP_SUBMITTED
        // BLIP_TIMESTAMP_CHANGED: ?
        // BLIP_VERSION_CHANGED: ?
        // DOCUMENT_CHANGED: ?
        // FORM_BUTTON_CLICKED: ?

        log.info("Wave responder activated!");

        if (bundle.wasSelfAdded()) {
            Blip blip = wavelet.appendBlip();
            TextView textView = blip.getDocument();
            textView.append("Hi\nIf this is your first visit, don't hesitate to send the command '!help' for a brief introduction");
        }

        for(Event e:bundle.getEvents()) {
            if(e.getType() == EventType.BLIP_SUBMITTED) {
                Blip blip = wavelet.appendBlip();
                TextView textView = blip.getDocument();
                textView.append("Sorry, I dunno how to process your commands for now :(");
            }
        }
    }
}
