package twetailer.j2ee;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twetailer.DataSourceException;
import twetailer.connector.MailConnector;
import twetailer.connector.TwitterConnector;
import twetailer.connector.BaseConnector.Source;
import twetailer.dao.BaseOperations;
import twetailer.dao.ConsumerOperations;
import twetailer.dto.Consumer;
import twetailer.dto.RawCommand;
import twitter4j.TwitterException;

@SuppressWarnings("serial")
public class TwitterMailNotificationHandlerServlet extends HttpServlet {
    private static Logger log = Logger.getLogger(TwitterMailNotificationHandlerServlet.class.getName());

    protected BaseOperations _baseOperations = new BaseOperations();
    protected ConsumerOperations consumerOperations = _baseOperations.getConsumerOperations();

    public static final String TWITTER_BASE_URL = "href=\"http://twitter.com/";
    public static final String TWITTER_NOTIFICATION_SUBJECT_SUFFIX = "is now following you on Twitter!";

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Prepare the message to persist
        RawCommand rawCommand = new RawCommand(Source.mail);

        try {
            log.warning("Path Info: " + request.getPathInfo());
            
            // Extract the incoming message
            MimeMessage mailMessage = MailConnector.getMailMessage(request);
            if (mailMessage.getFrom() == null) {
                throw new MessagingException("Incorrect message (no FROM!)");
            }

            // Check the message to persist
            boolean isAFollowingNotification = false;
            String subject = mailMessage.getSubject();
            String body = MailConnector.getText(mailMessage);
            String from = "unkown";
            if (subject != null) {
                int subjectPrefixPos = subject.indexOf(TWITTER_NOTIFICATION_SUBJECT_SUFFIX);
                if (subjectPrefixPos != -1) {
                    from = subject.substring(0, TWITTER_NOTIFICATION_SUBJECT_SUFFIX.length()).trim();
                    int urlPosition = body.indexOf(TWITTER_BASE_URL);
                    if (urlPosition != -1) {
                        int separatorSpace = body.indexOf("\"", urlPosition);
                        if (separatorSpace != -1) {
                            isAFollowingNotification = true;
                            String screenName = body.substring(urlPosition + TWITTER_BASE_URL.length(), separatorSpace);
                            PersistenceManager pm = _baseOperations.getPersistenceManager();
                            try {
                                List<Consumer> consumers = consumerOperations.getConsumers(pm, Consumer.TWITTER_ID, screenName, 1);
                                if (consumers.size() == 0) {
                                    // 1. Follow the user
                                    twitter4j.User user = TwitterConnector.getTwetailerAccount().enableNotification(screenName);
                                    // 2. Create his record
                                    Consumer consumer = new Consumer();
                                    consumer.setName(user.getName() == null || user.getName() == "" ? user.getScreenName() : user.getName());
                                    consumer.setTwitterId(user.getScreenName());
                                    consumer = consumerOperations.createConsumer(pm, consumer);
                                    log.warning("Consumer account created for the new Twitter follower: " + screenName);
                                }
                            }
                            catch(DataSourceException ex) {
                                subject += "[DataSourceException:" + (ex.getCause() == null ? ex.getMessage() : ex.getCause().getMessage()) + "]";
                                isAFollowingNotification = false;
                                ex.printStackTrace();
                            }
                            catch (TwitterException ex) {
                                subject += "[TwitterException:" + (ex.getCause() == null ? ex.getMessage() : ex.getCause().getMessage()) + "]";
                                isAFollowingNotification = false;
                                ex.printStackTrace();
                            }
                            finally {
                                pm.close();
                            }
                        }
                    }
                }
            }

            if (!isAFollowingNotification) {
                CatchAllMailHandlerServlet.composeAndPostMailMessage(from, subject, body);
            }
        }
        catch (MessagingException ex) {
            rawCommand.setErrorMessage("Error while parsing the mail message -- ex: " + ex.getMessage());
        }
    }
}
