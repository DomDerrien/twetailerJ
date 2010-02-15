package twetailer.j2ee;

import java.io.IOException;
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
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dto.Consumer;
import twitter4j.TwitterException;

@SuppressWarnings("serial")
public class TwitterMailNotificationHandlerServlet extends HttpServlet {
    private static Logger log = Logger.getLogger(TwitterMailNotificationHandlerServlet.class.getName());

    protected static BaseOperations _baseOperations = new BaseOperations();
    protected static ConsumerOperations consumerOperations = _baseOperations.getConsumerOperations();

    public static final String TWITTER_NOTIFICATION_SUBJECT_SUFFIX = " is now following you on Twitter!";
    public static final String TWITTER_INTRODUCTION_MESSAGE_RATTERN = "\\(([a-zA-Z0-9_]+)\\) is now following your tweets on Twitter";

    private static int lengthSubjectSuffix = TWITTER_NOTIFICATION_SUBJECT_SUFFIX.length();
    private static Pattern introductionPattern = Pattern.compile(TWITTER_INTRODUCTION_MESSAGE_RATTERN, Pattern.MULTILINE );

    /** Just made available for test purposes */
    protected static void setLogger(Logger mockLogger) {
        log = mockLogger;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.warning("Path Info: " + request.getPathInfo());

        processTwitterNotification(request, response);
    }

    protected static void processTwitterNotification(HttpServletRequest request, HttpServletResponse response) {
        try {
            log.warning("Path Info: " + request.getPathInfo());

            // Extract the incoming message
            MimeMessage mailMessage = MailConnector.getMailMessage(request);

            // Check the message to persist
            boolean isAFollowingNotification = false;
            String subject = mailMessage.getSubject();
            String body = MailConnector.getText(mailMessage);
            log.warning("Message from: " + mailMessage.getFrom()[0] + " -- subject: " + subject + " -- content: " + body);

            String followerName = "unknown";
            if (subject != null && subject.trim().endsWith(TWITTER_NOTIFICATION_SUBJECT_SUFFIX)) {
                followerName = subject.substring(0, subject.length() - lengthSubjectSuffix).trim();
                log.warning("Follower name: " + followerName);
                Matcher matcher = introductionPattern.matcher(body);
                if (matcher.find()) { // Runs the matcher once
                    isAFollowingNotification = true;
                    String followerScreenName = matcher.group(1).trim();
                    log.warning("Follower screen name: " + followerScreenName);
                    PersistenceManager pm = _baseOperations.getPersistenceManager();
                    try {
                        try {
                            // 1. Follow the user
                            TwitterConnector.getTwetailerAccount().enableNotification(followerScreenName);
                            // 2. Create his record
                            // TODO: call getConsumerKeys()
                            List<Consumer> consumers = consumerOperations.getConsumers(pm, Consumer.TWITTER_ID, followerScreenName, 1);
                            if (consumers.size() == 0) {
                                log.warning("Follower account to be created");
                                Consumer consumer = new Consumer();
                                consumer.setName(followerName);
                                consumer.setTwitterId(followerScreenName);
                                consumer = consumerOperations.createConsumer(pm, consumer);
                                log.warning("Consumer account created for the new Twitter follower: " + followerScreenName);
                            }
                            else {
                                log.warning("Follower account id: " + consumers.get(0).getKey());
                            }
                        }
                        finally {
                            pm.close();
                        }
                    }
                    catch (TwitterException ex) {
                        subject += "[TwitterException:" + ex.getMessage() + "]";
                        isAFollowingNotification = false;
                    }
                    catch(DataSourceException ex) {
                        subject += "[DataSourceException:" + ex.getMessage() + "]";
                        isAFollowingNotification = false;
                    }
                }
            }

            if (!isAFollowingNotification) {
                CatchAllMailHandlerServlet.composeAndPostMailMessage(followerName, subject, body);
            }
        }
        catch (MessagingException ex) {
            // Nothing to do with a corrupted message...
        }
        catch (IOException ex) {
            // Nothing to do with a corrupted message...
        }
    }
}
