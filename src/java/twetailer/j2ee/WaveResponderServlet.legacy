package twetailer.j2ee;

import java.util.List;
import java.util.logging.Logger;

import com.google.wave.api.AbstractRobotServlet;
import com.google.wave.api.Blip;
import com.google.wave.api.Event;
import com.google.wave.api.RobotMessageBundle;
import com.google.wave.api.TextView;
import com.google.wave.api.Wavelet;

/**
 * Entry point processing Google Wave messages.
 * Received information are stored in a RawCommand instance
 * that the task "/maezel/processCommand" will process
 * asynchrounously.
 *
 * @see twetailer.dto.RawCommand
 * @see twetailer.j2ee.MaezelServlet
 *
 * @author Dom Derrien
 */

@SuppressWarnings("serial")
public class WaveResponderServlet extends AbstractRobotServlet {
    private static Logger log = Logger.getLogger(WaveResponderServlet.class.getName());

    // Setter for injection of a MockLogger at test time
    protected static void setLogger(Logger mock) {
        log = mock;
    }

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

        if (bundle.wasSelfAdded()) {
            log.info("Wave responder activated after its addition to the Wavelet!");
            Blip blip = wavelet.appendBlip();
            TextView textView = blip.getDocument();
            textView.append("Hi\nIf this is your first visit, don't hesitate to send the command '!help' for a brief introduction");
        }

        List<Event> submittedBlips = bundle.getBlipSubmittedEvents();
        if (submittedBlips != null) {
            for(Event e : submittedBlips) {
                Blip blip = wavelet.appendBlip();
                TextView textView = blip.getDocument();
                textView.append("Sorry, I cannot process your commands for now :( -- " + e.getBlip().getDocument().getText());
            }
        }
    }
}
