package twetailer.j2ee;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jdo.PersistenceManager;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twetailer.DataSourceException;
import twetailer.connector.MailConnector;
import twetailer.connector.TwitterConnector;
import twetailer.dto.Consumer;
import twetailer.task.step.BaseSteps;
import twetailer.validator.ApplicationSettings;
import twitter4j.TwitterException;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.TaskOptions.Method;

/**
 * Entry point processing notifications sent by the
 * Twitter service. The main goals are:<ul>
 *   <li>detect the notifications about new followers
 *   and to follow them back;</li>
 *   <li>trigger the task loading DMs when one
 *   corresponding notification is received.</li></ul>
 *
 * Unresolved notifications are forwarded to the
 * "admins" list.
 *
 * @see twetailer.j2ee.CatchAllMailHandlerServlet
 *
 * @author Dom Derrien
 */
@SuppressWarnings("serial")
public class TwitterMailNotificationHandlerServlet extends HttpServlet {

    private static Logger log = Logger.getLogger(TwitterMailNotificationHandlerServlet.class.getName());

    /// Made available for test purposes
    public static void setMockLogger(Logger mockLogger) {
        log = mockLogger;
    }

    protected static Logger getLogger() {
        return log;
    }

    public static final String TWITTER_NOTIFICATION_SUBJECT_SUFFIX = " is now following you on Twitter!";
    public static final String TWITTER_INTRODUCTION_MESSAGE_RATTERN = "\\(([a-zA-Z0-9_]+)\\) is now following your tweets on Twitter";

    private static int lengthSubjectSuffix = TWITTER_NOTIFICATION_SUBJECT_SUFFIX.length();
    private static Pattern introductionPattern = Pattern.compile(TWITTER_INTRODUCTION_MESSAGE_RATTERN, Pattern.MULTILINE );

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        processTwitterNotification(request, response);
    }

    protected static List<String> responderEndpoints = new ArrayList<String>();

    public static List<String> getResponderEndpoints() {
        if (responderEndpoints.size() == 0) {
            String emailDomain = ApplicationSettings.get().getProductEmailDomain();

            responderEndpoints.add("twitter@" + emailDomain);
        }
        return responderEndpoints;
    }

    protected static void processTwitterNotification(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Extract the incoming message
            MimeMessage mailMessage = MailConnector.getMailMessage(request);

            // Check the message to persist
            boolean isAFollowingNotification = false;
            String subject = mailMessage.getSubject();
            String body = MailConnector.getText(mailMessage);
            getLogger().warning("Message from: " + mailMessage.getFrom()[0] + " -- subject: " + subject + " -- content: " + body);

            String followerName = "unknown";
            if (subject != null && subject.trim().endsWith(TWITTER_NOTIFICATION_SUBJECT_SUFFIX)) {
                followerName = subject.substring(0, subject.length() - lengthSubjectSuffix).trim();
                getLogger().warning("Follower name: " + followerName);
                Matcher matcher = introductionPattern.matcher(body);
                if (matcher.find()) { // Runs the matcher once
                    isAFollowingNotification = true;
                    String followerScreenName = matcher.group(1).trim();
                    getLogger().warning("Follower screen name: " + followerScreenName);
                    PersistenceManager pm = BaseSteps.getBaseOperations().getPersistenceManager();
                    try {
                        try {
                            // 1. Follow the user
                            // TwitterConnector.getTwetailerAccount().enableNotification(followerScreenName);
                            TwitterConnector.getAseHubAccount().createFriendship(followerScreenName, true);
                            // 2. Create his record
                            // TODO: call getConsumerKeys()
                            List<Consumer> consumers = BaseSteps.getConsumerOperations().getConsumers(pm, Consumer.TWITTER_ID, followerScreenName, 1);
                            if (consumers.size() == 0) {
                                getLogger().warning("Follower account to be created");
                                Consumer consumer = new Consumer();
                                consumer.setName(followerName);
                                consumer.setTwitterId(followerScreenName);
                                consumer = BaseSteps.getConsumerOperations().createConsumer(pm, consumer);
                                getLogger().warning("Consumer account created for the new Twitter follower: " + followerScreenName);
                            }
                            else {
                                getLogger().warning("Follower account id: " + consumers.get(0).getKey());
                            }
                        }
                        finally {
                            pm.close();
                        }
                    }
                    catch (TwitterException ex) {
                        subject += " [TwitterException: " + ex.getMessage() + "]";
                        isAFollowingNotification = false;
                    }
                    catch(DataSourceException ex) {
                        subject += " [DataSourceException: " + ex.getMessage() + "]";
                        isAFollowingNotification = false;
                    }
                }
            }

            if (!isAFollowingNotification) {
                // Safe assumption: trigger a task to load the possibly just notified & waiting DMs
                Queue queue = BaseSteps.getBaseOperations().getQueue();
                queue.add(
                        withUrl("/_tasks/loadTweets").
                            method(Method.GET)
                );
                // Forward the message to the "admins" list
                MailConnector.reportErrorToAdmins(followerName, subject, body);
            }
        }
        catch (MessagingException ex) {
            // Nothing to do with a corrupted message...
            Logger.getLogger(TwitterMailNotificationHandlerServlet.class.getName()).warning("Issue while composing the message -- message " + ex.getMessage());
        }
        catch (IOException ex) {
            // Nothing to do with a corrupted message...
            Logger.getLogger(TwitterMailNotificationHandlerServlet.class.getName()).warning("Issue while sending the message -- message " + ex.getMessage());
        }
    }
}
