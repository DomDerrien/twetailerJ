package twetailer.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;

import javamocks.io.MockInputStream;
import javamocks.util.logging.MockLogger;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.MockServletInputStream;
import javax.servlet.ServletInputStream;
import javax.servlet.http.MockHttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import twetailer.CommunicationException;
import twetailer.connector.BaseConnector.Source;
import twetailer.dto.Consumer;
import twetailer.validator.ApplicationSettings;

import com.google.appengine.api.mail.dev.LocalMailService;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMailServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import domderrien.i18n.LabelExtractor;
import domderrien.i18n.LabelExtractor.ResourceFileId;

public class TestMailConnector {

    private static LocalServiceTestHelper  helper;
    private static String emailDomain;

    @BeforeClass
    public static void setUpBeforeClass() {
        BaseConnector.setMockLogger(new MockLogger("test", null));
        MailConnector.setMockLogger(new MockLogger("test", null));
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
        emailDomain = ApplicationSettings.get().getProductEmailDomain();
    }

    @Before
    public void setUp() throws Exception {
        helper.setUp();
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();
    }

    @Test
    public void testConstructor() {
        new MailConnector();
    }

    @Test
    public void testGetLogger() {
        MailConnector.getLogger();
    }

    public static MockServletInputStream prepareEmptyStream(String from, String name, String emailDomain) {
        MockServletInputStream stream = new MockServletInputStream();
        stream.setData(
                "MIME-Version: 1.0\n" +
                "Date: Fri, 06 Nov 2009 20:01:37 -0500\n" +
                "From: " + name + "<" + from + ">\n" +
                "To: Twetailer <assistant@" + emailDomain + ">\n" +
                "Cc: unit@test.net\n" +
                "Content-Language: en\n" +
                "Accept-Language: en, fr\n" +
                "Subject: Twetailer\n" +
                "Content-Type: text/plain; charset=UTF-8\n" +
                "\n"
        );
        return stream;
    }

    @Test
    public void testGetMailMessage0() throws IOException, MessagingException {
        final String from = "test-emitter@appspot.com";
        final String name = "Mr Emitter";
        final MockServletInputStream stream = prepareEmptyStream(from, name, emailDomain);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
        };

        MimeMessage mailMessage = MailConnector.getMailMessage(request);

        assertNotNull(mailMessage);
        assertEquals(from, ((InternetAddress) mailMessage.getFrom()[0]).getAddress());
        assertEquals(name, ((InternetAddress) mailMessage.getFrom()[0]).getPersonal());
        assertEquals("", MailConnector.getText(mailMessage));
        assertEquals(0, stream.getNotProcessedContents().length());
    }

    public static MockServletInputStream prepareTextStream(String from, String name, String subject, String message, String emailDomain) {
        MockServletInputStream stream = new MockServletInputStream();
        stream.setData(
                "MIME-Version: 1.0\n" +
                "Date: Fri, 06 Nov 2009 20:01:37 -0500\n" +
                ( from == null && name == null ? ""  : from == null ? "From:\n"  : "From: " + name + " <" + from + ">\n" ) +
                "To: Twetailer <assistant@" + emailDomain + ">\n" +
                "Cc: unit@test.net\n" +
                ( subject == null ? "" : "Subject: " + subject + "\n" ) +
                "Content-Type: text/plain; charset=UTF-8\n" +
                "\n" +
                message
        );
        return stream;
    }

    @Test
    public void testGetMailMessageI() throws IOException, MessagingException {
        final String from = "test-emitter@appspot.com";
        final String name = "Mr Emitter";
        final String subject = "Not important!";
        final String message = "wii console Mario Kart"; // FIXME: -- àéüôç";
        final MockServletInputStream stream = prepareTextStream(from, name, subject, message, emailDomain);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
        };

        MimeMessage mailMessage = MailConnector.getMailMessage(request);

        assertNotNull(mailMessage);
        assertEquals(from, ((InternetAddress) mailMessage.getFrom()[0]).getAddress());
        assertEquals(name, ((InternetAddress) mailMessage.getFrom()[0]).getPersonal());
        assertEquals(message, MailConnector.getText(mailMessage));
        assertEquals(0, stream.getNotProcessedContents().length());
    }

    public static MockServletInputStream prepareHtmlStream(String from, String name, String message) {
        MockServletInputStream stream = new MockServletInputStream();
        stream.setData(
                "MIME-Version: 1.0\n" +
                "Date: Fri, 06 Nov 2009 20:01:37 -0500\n" +
                "From: " + name + "<" + from + ">\n" +
                "To: Twetailer <assistant@" + emailDomain + ">\n" +
                "Cc: unit@test.net\n" +
                "Subject: Twetailer\n" +
                "Content-Type: text/html; charset=UTF-8\n" +
                "\n" +
                message
        );
        return stream;
    }

    @Test
    public void testGetMailMessageII() throws IOException, MessagingException {
        final String from = "test-emitter@appspot.com";
        final String name = "Mr Emitter";
        final String message = "wii console Mario Kart"; // FIXME: -- àéüôç";
        final String envelope = "<html><body style='background-color:black;color:white;'><center>" + message + "</center></body></html>";
        final MockServletInputStream stream = prepareHtmlStream(from, name, envelope);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
        };

        MimeMessage mailMessage = MailConnector.getMailMessage(request);

        assertNotNull(mailMessage);
        assertEquals(from, ((InternetAddress) mailMessage.getFrom()[0]).getAddress());
        assertEquals(name, ((InternetAddress) mailMessage.getFrom()[0]).getPersonal());
        // TODO: implement the logic cleaning up the HTML tags
        // assertEquals(message, MailConnector.getText(mailMessage));
        assertEquals(envelope, MailConnector.getText(mailMessage));
        assertEquals(0, stream.getNotProcessedContents().length());
    }

    public static MockServletInputStream prepareOctetStream(String from, String name, String message) {
        MockServletInputStream stream = new MockServletInputStream();
        stream.setData(
                "MIME-Version: 1.0\n" +
                "Date: Fri, 06 Nov 2009 20:01:37 -0500\n" +
                "From: " + name + "<" + from + ">\n" +
                "To: Twetailer <assistant@" + emailDomain + ">\n" +
                "Cc: unit@test.net\n" +
                "Subject: Twetailer\n" +
                "Content-Type: application/octet-stream\n" +
                "Content-Transfer-Encoding: base64\n" +
                "\n" +
                "PGh0bWw+CiAgPGhlYWQ+CiAgPC9oZWFkPgogIDxib2R5PgogICAgPHA+VGhpcyBpcyB0aGUg\n" +
                "Ym9keSBvZiB0aGUgbWVzc2FnZS48L3A+CiAgPC9ib2R5Pgo8L2h0bWw+Cg=="
        );
        return stream;
    }

    @Test
    public void testGetMailMessageIII() throws IOException, MessagingException {
        final String from = "test-emitter@appspot.com";
        final String name = "Mr Emitter";
        final String message = "wii console Mario Kart"; // FIXME: -- àéüôç";
        final MockServletInputStream stream = prepareOctetStream(from, name, message);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
        };

        MimeMessage mailMessage = MailConnector.getMailMessage(request);

        assertNotNull(mailMessage);
        assertEquals(from, ((InternetAddress) mailMessage.getFrom()[0]).getAddress());
        assertEquals(name, ((InternetAddress) mailMessage.getFrom()[0]).getPersonal());
        assertEquals("", MailConnector.getText(mailMessage)); // Empty message!
        assertEquals(0, stream.getNotProcessedContents().length());
    }

    public static MockServletInputStream prepareMultipartWithJustTextStream(String boundary, String from, String name, String message) {
        MockServletInputStream stream = new MockServletInputStream();
        stream.setData(
                "MIME-Version: 1.0\n" +
                "Date: Fri, 06 Nov 2009 20:01:37 -0500\n" +
                "From: " + name + "<" + from + ">\n" +
                "To: Twetailer <assistant@" + emailDomain + ">\n" +
                "Cc: unit@test.net\n" +
                "Subject: Twetailer\n" +
                "Content-Type: multipart/alternative; boundary=" + boundary + "\n" +
                "\n" +
                "--" + boundary + "\n" +
                "Content-Type: text/plain; charset=UTF-8\n" +
                "\n" +
                message + "\n" +
                "--" + boundary + "\n" +
                "Content-Type: text/html; charset=UTF-8\n" +
                "\n" +
                "<center>" + message + "</center>\n" +
                "--" + boundary + "--"
        );
        return stream;
    }

    @Test
    public void testGetMailMessageXI() throws IOException, MessagingException {
        final String from = "test-emitter@appspot.com";
        final String name = "Mr Emitter";
        final String message = "wii console Mario Kart"; // FIXME: -- àéüôç";
        final String boundary = "R1T1RTS-4878472";
        final MockServletInputStream stream = prepareMultipartWithJustTextStream(boundary, from, name, message);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
        };

        MimeMessage mailMessage = MailConnector.getMailMessage(request);

        assertNotNull(mailMessage);
        assertEquals(from, ((InternetAddress) mailMessage.getFrom()[0]).getAddress());
        assertEquals(name, ((InternetAddress) mailMessage.getFrom()[0]).getPersonal());
        assertEquals(message, MailConnector.getText(mailMessage));
        assertEquals(0, stream.getNotProcessedContents().length());
    }

    public static MockServletInputStream prepareMultipartWithAttachmentStream(String boundary, String from, String name, String message) {
        MockServletInputStream stream = new MockServletInputStream();
        stream.setData(
                "MIME-Version: 1.0\n" +
                "Date: Fri, 06 Nov 2009 20:01:37 -0500\n" +
                "From: " + name + "<" + from + ">\n" +
                "To: Twetailer <assistant@" + emailDomain + ">\n" +
                "Cc: unit@test.net\n" +
                "Subject: Twetailer\n" +
                "Content-Type: multipart/mixed; boundary=\"" + boundary + "\"\n" +
                "\n" +
                "--" + boundary + "\n" +
                "Content-Type: text/plain; charset=UTF-8\n" +
                "\n" +
                message + "\n" +
                "--" + boundary + "\n" +
                "Content-Type: text/plain; charset=UTF-8\n" +
                "Content-Disposition: attachment; filename=\"test.txt\"\n" +
                "\n" +
                "<center>" + message + "</center>\n" +
                "--" + boundary + "--"
        );
        return stream;
    }

    @Test
    public void testGetMailMessageXII() throws IOException, MessagingException {
        final String from = "test-emitter@appspot.com";
        final String name = "Mr Emitter";
        final String message = "wii console Mario Kart"; // FIXME: -- àéüôç";
        final String boundary = "R1T1RTS-4878472";
        final MockServletInputStream stream = prepareMultipartWithAttachmentStream(boundary, from, name, message);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
        };

        MimeMessage mailMessage = MailConnector.getMailMessage(request);

        assertNotNull(mailMessage);
        assertEquals(from, ((InternetAddress) mailMessage.getFrom()[0]).getAddress());
        assertEquals(name, ((InternetAddress) mailMessage.getFrom()[0]).getPersonal());
        assertEquals(message, MailConnector.getText(mailMessage));
        assertEquals(0, stream.getNotProcessedContents().length());
    }

    public static MockServletInputStream prepareMultipartWithOctetStream(String boundary, String from, String name, String message) {
        MockServletInputStream stream = new MockServletInputStream();
        stream.setData(
                "MIME-Version: 1.0\n" +
                "Date: Fri, 06 Nov 2009 20:01:37 -0500\n" +
                "From: " + name + "<" + from + ">\n" +
                "To: Twetailer <assistant@" + emailDomain + ">\n" +
                "Cc: unit@test.net\n" +
                "Subject: Twetailer\n" +
                "Content-Type: multipart/mixed; boundary=\"" + boundary + "\"\n" +
                "\n" +
                "--" + boundary + "\n" +
                "Content-Type: application/octet-stream\n" +
                "Content-Transfer-Encoding: base64\n" +
                "\n" +
                "PGh0bWw+CiAgPGhlYWQ+CiAgPC9oZWFkPgogIDxib2R5PgogICAgPHA+VGhpcyBpcyB0aGUg\n" +
                "Ym9keSBvZiB0aGUgbWVzc2FnZS48L3A+CiAgPC9ib2R5Pgo8L2h0bWw+Cg==\n" +
                "--" + boundary + "\n" +
                "Content-Type: text/html; charset=UTF-8\n" +
                "\n" +
                message + "\n" +
                "--" + boundary + "--"
        );
        return stream;
    }

    @Test
    public void testGetMailMessageXIII() throws IOException, MessagingException {
        final String from = "test-emitter@appspot.com";
        final String name = "Mr Emitter";
        final String message = "wii console Mario Kart"; // FIXME: -- àéüôç";
        final String envelope = "<html><body style='background-color:black;color:white;'><center>" + message + "</center></body></html>";
        final String boundary = "R1T1RTS-4878472";
        final MockServletInputStream stream = prepareMultipartWithOctetStream(boundary, from, name, envelope);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
        };

        MimeMessage mailMessage = MailConnector.getMailMessage(request);

        assertNotNull(mailMessage);
        assertEquals(from, ((InternetAddress) mailMessage.getFrom()[0]).getAddress());
        assertEquals(name, ((InternetAddress) mailMessage.getFrom()[0]).getPersonal());
        // TODO: implement the logic cleaning up the HTML tags
        // assertEquals(message, MailConnector.getText(mailMessage));
        assertEquals(envelope, MailConnector.getText(mailMessage));
        assertEquals(0, stream.getNotProcessedContents().length());
    }

    public static MockServletInputStream prepareMultipartWithoutTextStream(String boundary, String from, String name, String message) {
        MockServletInputStream stream = new MockServletInputStream();
        stream.setData(
                "MIME-Version: 1.0\n" +
                "Date: Fri, 06 Nov 2009 20:01:37 -0500\n" +
                "From: " + name + "<" + from + ">\n" +
                "To: Twetailer <assistant@" + emailDomain + ">\n" +
                "Cc: unit@test.net\n" +
                "Subject: Twetailer\n" +
                "Content-Type: multipart/mixed; boundary=\"" + boundary + "\"\n" +
                "\n" +
                "--" + boundary + "\n" +
                "Content-Type: application/octet-stream; file=\"test.bin\"\n" +
                "Content-Disposition: attachment; filename=test.bin;\n" +
                "Content-Transfer-Encoding: base64\n" +
                "\n" +
                "PGh0bWw+CiAgPGhlYWQ+CiAgPC9oZWFkPgogIDxib2R5PgogICAgPHA+VGhpcyBpcyB0aGUg\n" +
                "Ym9keSBvZiB0aGUgbWVzc2FnZS48L3A+CiAgPC9ib2R5Pgo8L2h0bWw+Cg==\n" +
                "--" + boundary + "\n" +
                "Content-Type: text/plain; charset=UTF-8; file=\"test.txt\";\n" +
                "Content-Disposition: attachment; filename=\"test.txt\";\n" +
                "\n" +
                "<center>" + message + "</center>\n" +
                "--" + boundary + "--"
        );
        return stream;
    }

    @Test(expected=MessagingException.class)
    public void testGetMailMessageXIV() throws IOException, MessagingException {
        final String from = "test-emitter@appspot.com";
        final String name = "Mr Emitter";
        final String message = "wii console Mario Kart"; // FIXME: -- àéüôç";
        final String envelope = "<html><body style='background-color:black;color:white;'><center>" + message + "</center></body></html>";
        final String boundary = "R1T1RTS-4878472";
        final MockServletInputStream stream = prepareMultipartWithoutTextStream(boundary, from, name, envelope);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
        };

        MimeMessage mailMessage = MailConnector.getMailMessage(request);

        assertNotNull(mailMessage);
        assertEquals(from, ((InternetAddress) mailMessage.getFrom()[0]).getAddress());
        assertEquals(name, ((InternetAddress) mailMessage.getFrom()[0]).getPersonal());
        // TODO: implement the logic cleaning up the HTML tags
        // assertEquals(message, MailConnector.getText(mailMessage));

        MailConnector.getText(mailMessage); // Just attachments have been sent => Exception MessagingException thrown!
    }

    public static MockServletInputStream prepareMultipartWithEmbeddedStream(String boundary, String from, String name, String message) {
        MockServletInputStream stream = new MockServletInputStream();
        stream.setData(
                "MIME-Version: 1.0\n" +
                "Date: Fri, 06 Nov 2009 20:01:37 -0500\n" +
                "From: " + name + "<" + from + ">\n" +
                "To: Twetailer <assistant@" + emailDomain + ">\n" +
                "Cc: unit@test.net\n" +
                "Subject: Twetailer\n" +
                "Content-Type: multipart/mixed; boundary=\"" + boundary + "-111\"\n" +
                "\n" +
                "--" + boundary + "-111\n" +
                "Content-Type: multipart/parallel; boundary=" + boundary + "-222\n" +
                "\n" +
                "--" + boundary + "-222\n" +
                "Content-Type: audio/basic\n" +
                "Content-Transfer-Encoding: base64\n" +
                "\n" +
                "PGh0bWw+CiAgPGhlYWQ+CiAgPC9oZWFkPgogIDxib2R5PgogICAgPHA+VGhpcyBpcyB0aGUg\n" +
                "Ym9keSBvZiB0aGUgbWVzc2FnZS48L3A+CiAgPC9ib2R5Pgo8L2h0bWw+Cg==\n" +
                "\n" +
                "--" + boundary + "-222\n" +
                "Content-Type: image/gif\n" +
                "Content-Transfer-Encoding: base64\n" +
                "\n" +
                "PGh0bWw+CiAgPGhlYWQ+CiAgPC9oZWFkPgogIDxib2R5PgogICAgPHA+VGhpcyBpcyB0aGUg\n" +
                "Ym9keSBvZiB0aGUgbWVzc2FnZS48L3A+CiAgPC9ib2R5Pgo8L2h0bWw+Cg==\n" +
                "--" + boundary + "-222--\n" +
                "\n" +
                "--" + boundary + "-111\n" +
                "\n" +
                // Text without header is considered "text/plain; charset=us-ascii"
                message +"\n" +
                "--" + boundary + "-111--"
        );
        return stream;
    }

    @Test
    public void testGetMailMessageXV() throws IOException, MessagingException {
        final String from = "test-emitter@appspot.com";
        final String name = "Mr Emitter";
        final String message = "wii console Mario Kart";
        final String boundary = "R1T1RTS-4878472";
        final MockServletInputStream stream = prepareMultipartWithEmbeddedStream(boundary, from, name, message);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
        };

        MimeMessage mailMessage = MailConnector.getMailMessage(request);

        assertNotNull(mailMessage);
        assertEquals(from, ((InternetAddress) mailMessage.getFrom()[0]).getAddress());
        assertEquals(name, ((InternetAddress) mailMessage.getFrom()[0]).getPersonal());
        // TODO: implement the logic cleaning up the HTML tags
        // assertEquals(message, MailConnector.getText(mailMessage));
        assertEquals(message, MailConnector.getText(mailMessage)); // Just attachments have been sent
        assertEquals(0, stream.getNotProcessedContents().length());
    }

    @Test
    public void testSendMailMessageI() throws UnsupportedEncodingException, MessagingException {
        String subject = LabelExtractor.get("mc_mail_subject_response_prefix", Locale.ENGLISH) + "subject";
        MailConnector.sendMailMessage(
                false,
                false,
                "testId",
                "testName",
                subject,
                "******************\n******************\ntest exhaustif pour voir où est \nla faute...\n******************\n******************",
                Locale.ENGLISH
        );

        LocalMailService service = LocalMailServiceTestConfig.getLocalMailService();
        assertEquals(1, service.getSentMessages().size());
        assertEquals(subject, service.getSentMessages().get(0).getSubject());
        assertTrue(service.getSentMessages().get(0).getTo(0).contains("testId"));
        assertTrue(service.getSentMessages().get(0).getTo(0).contains("testName"));
    }

    @Test
    public void testSendMailMessageII() throws UnsupportedEncodingException, MessagingException {
        MailConnector.sendMailMessage(
                false,
                false,
                "testId",
                "testName",
                "subject",
                "******************\n******************\ntest exhaustif pour voir où est \nla faute...\n******************\n******************",
                Locale.ENGLISH
        );

        LocalMailService service = LocalMailServiceTestConfig.getLocalMailService();
        assertEquals(1, service.getSentMessages().size());
        assertEquals("subject", service.getSentMessages().get(0).getSubject());
        assertTrue(service.getSentMessages().get(0).getTo(0).contains("testId"));
        assertTrue(service.getSentMessages().get(0).getTo(0).contains("testName"));
    }

    @Test
    public void testSendMailMessageIII() throws UnsupportedEncodingException, MessagingException {
        MailConnector.sendMailMessage(
                false,
                false,
                "testId",
                "testName",
                "",
                "******************\n******************\ntest exhaustif pour voir où est \nla faute...\n******************\n******************",
                Locale.ENGLISH
        );

        LocalMailService service = LocalMailServiceTestConfig.getLocalMailService();
        assertEquals(1, service.getSentMessages().size());
        assertEquals("", service.getSentMessages().get(0).getSubject());
        assertTrue(service.getSentMessages().get(0).getTo(0).contains("testId"));
        assertTrue(service.getSentMessages().get(0).getTo(0).contains("testName"));
    }

    @Test
    public void testSendMailMessageIV() throws UnsupportedEncodingException, MessagingException {
        MailConnector.sendMailMessage(
                false,
                false,
                "testId",
                "testName",
                null,
                "******************\n******************\ntest exhaustif pour voir où est \nla faute...\n******************\n******************",
                Locale.ENGLISH
        );
    }

    @Test
    public void testSendMailMessageV() throws UnsupportedEncodingException, MessagingException {
        MailConnector.sendMailMessage(
                true, // With the twetailer-cc address
                false,
                "testId",
                "testName",
                null,
                "******************\n******************\ntest exhaustif pour voir où est \nla faute...\n******************\n******************",
                Locale.ENGLISH
        );

        LocalMailService service = LocalMailServiceTestConfig.getLocalMailService();
        assertEquals(1, service.getSentMessages().size());
        assertEquals("", service.getSentMessages().get(0).getSubject());
        assertTrue(service.getSentMessages().get(0).getTo(0).contains("testId"));
        assertTrue(service.getSentMessages().get(0).getTo(0).contains("testName"));
    }

    @Test
    public void testSendMailMessageVI() throws UnsupportedEncodingException, MessagingException {
        MailConnector.sendMailMessage(
                false,
                false,
                "testId",
                "testName",
                null,
                null,
                Locale.ENGLISH
        );

        LocalMailService service = LocalMailServiceTestConfig.getLocalMailService();
        assertEquals(1, service.getSentMessages().size());
        assertEquals("", service.getSentMessages().get(0).getSubject());
        assertTrue(service.getSentMessages().get(0).getTo(0).contains("testId"));
        assertTrue(service.getSentMessages().get(0).getTo(0).contains("testName"));
    }

    @Test
    public void testSendMailMessageVII() throws UnsupportedEncodingException, MessagingException {
        MailConnector.sendMailMessage(
                false,
                false,
                "testId",
                "testName",
                null,
                "",
                Locale.ENGLISH
        );

        LocalMailService service = LocalMailServiceTestConfig.getLocalMailService();
        assertEquals(1, service.getSentMessages().size());
        assertEquals("", service.getSentMessages().get(0).getSubject());
        assertTrue(service.getSentMessages().get(0).getTo(0).contains("testId"));
        assertTrue(service.getSentMessages().get(0).getTo(0).contains("testName"));
    }

    @Test
    public void testSendMailMessageVIII() throws UnsupportedEncodingException, MessagingException {
        MailConnector.sendMailMessage(
                false,
                false,
                "testId",
                "testName",
                null,
                "<p style='border:2px solid red;'>test exhaustif pour voir où est<br/>la faute...</p>",
                Locale.ENGLISH
        );

        LocalMailService service = LocalMailServiceTestConfig.getLocalMailService();
        assertEquals(1, service.getSentMessages().size());
        assertEquals("", service.getSentMessages().get(0).getSubject());
        assertTrue(service.getSentMessages().get(0).getTo(0).contains("testId"));
        assertTrue(service.getSentMessages().get(0).getTo(0).contains("testName"));
    }

    @Test
    public void testPrepareInternetAddressI() throws AddressException {
        String email = "unit@test.ca";

        InternetAddress test = MailConnector.prepareInternetAddress("UTF-8", null, email);

        assertNull(test.getPersonal());
        assertEquals(email, test.getAddress());
    }

    @Test
    public void testPrepareInternetAddressII() throws AddressException {
        String email = "unit@test.ca";

        InternetAddress test = MailConnector.prepareInternetAddress("UTF-8", "", email);

        assertNull(test.getPersonal());
        assertEquals(email, test.getAddress());
    }

    @Test
    public void testPrepareInternetAddressIII() throws AddressException {
        String name = "test";
        String email = "unit@test.ca";

        InternetAddress test = MailConnector.prepareInternetAddress("UTF-8", name, email);

        assertEquals(name, test.getPersonal());
        assertEquals(email, test.getAddress());
    }

    @Test
    @Ignore
    public void testPrepareInternetAddressIV() throws AddressException {
        String name = "Last with a UTF-8 sequence: ễ";
        byte[] nameBytes = name.getBytes();
        byte[] corruptedNameBytes = Arrays.copyOf(nameBytes, nameBytes.length + 1);
        corruptedNameBytes[corruptedNameBytes.length - 1] = corruptedNameBytes[corruptedNameBytes.length - 2];
        String corruptedName = new String(corruptedNameBytes);
        Charset.forName("dddd");
        String email = "unit@test.ca";

        InternetAddress test = MailConnector.prepareInternetAddress("iso-latin-1", corruptedName, email);

        // Don't know how to generate a UnsupportedEncodingException by just
        // injecting a corrupted UTF-8 sequence and/or a wrong character set

        assertEquals("", test.getPersonal());
        assertEquals(email, test.getAddress());
    }

    @Test
    public void testPrepareSubjectAsResponseI() {
        assertEquals("Re: test", MailConnector.prepareSubjectAsResponse("test", Locale.ENGLISH));
        assertEquals("Re: test", MailConnector.prepareSubjectAsResponse("   test   ", Locale.ENGLISH));
    }

    @Test
    public void testPrepareSubjectAsResponseII() {
        assertEquals("Re:test", MailConnector.prepareSubjectAsResponse("Re:test", Locale.ENGLISH));
        assertEquals("Re:\ttest", MailConnector.prepareSubjectAsResponse("Re:\ttest", Locale.ENGLISH));
        assertEquals("Re:  test", MailConnector.prepareSubjectAsResponse("Re:  test", Locale.ENGLISH));
        assertEquals("Re:  test", MailConnector.prepareSubjectAsResponse("   Re:  test   ", Locale.ENGLISH));
    }

    @Test
    public void testPrepareSubjectAsResponseIII() {
        String defaultSubject = LabelExtractor.get(ResourceFileId.fourth, "common_message_subject_default", Locale.ENGLISH);
        assertEquals(defaultSubject, MailConnector.prepareSubjectAsResponse(null, Locale.ENGLISH));
        assertEquals(defaultSubject, MailConnector.prepareSubjectAsResponse("", Locale.ENGLISH));
        assertEquals(defaultSubject, MailConnector.prepareSubjectAsResponse("     ", Locale.ENGLISH));
    }

    @Test
    public void testPrepareSubjectAsForwardI() {
        assertEquals("Fw: test", MailConnector.prepareSubjectAsForward("test", Locale.ENGLISH));
        assertEquals("Fw: test", MailConnector.prepareSubjectAsForward("   test   ", Locale.ENGLISH));
    }

    @Test
    public void testPrepareSubjectAsForwardII() {
        assertEquals("Fw:test", MailConnector.prepareSubjectAsForward("Fw:test", Locale.ENGLISH));
        assertEquals("Fw:\ttest", MailConnector.prepareSubjectAsForward("Fw:\ttest", Locale.ENGLISH));
        assertEquals("Fw:  test", MailConnector.prepareSubjectAsForward("Fw:  test", Locale.ENGLISH));
        assertEquals("Fw:  test", MailConnector.prepareSubjectAsForward("   Fw:  test   ", Locale.ENGLISH));
    }

    @Test
    public void testPrepareSubjectAsForwardIII() {
        String defaultSubject = LabelExtractor.get(ResourceFileId.fourth, "common_message_subject_default", Locale.ENGLISH);
        assertEquals(defaultSubject, MailConnector.prepareSubjectAsForward(null, Locale.ENGLISH));
        assertEquals(defaultSubject, MailConnector.prepareSubjectAsForward("", Locale.ENGLISH));
        assertEquals(defaultSubject, MailConnector.prepareSubjectAsForward("     ", Locale.ENGLISH));
    }

    @Test(expected=java.io.IOException.class)
    public void defect_7845587_I() throws IOException, MessagingException {
        // https://www.pivotaltracker.com/story/show/7845587
        final MockServletInputStream stream = new MockServletInputStream();
        stream.setData(
            "Delivered-To: dominique.derrien@gmail.com\n" +
            "Received: by 10.229.80.81 with SMTP id s17cs100883qck;\n" +
            "        Mon, 20 Dec 2010 17:36:29 -0800 (PST)\n" +
            "Received: by 10.224.54.69 with SMTP id p5mr4751769qag.46.1292895389080;\n" +
            "        Mon, 20 Dec 2010 17:36:29 -0800 (PST)\n" +
            "Return-Path: <katelynconsumer@gmail.com>\n" +
            "Received: from mail-vw0-f47.google.com (mail-vw0-f47.google.com [209.85.212.47])\n" +
            "        by mx.google.com with ESMTP id m40si3472957vcr.55.2010.12.20.17.36.27;\n" +
            "        Mon, 20 Dec 2010 17:36:28 -0800 (PST)\n" +
            "Received-SPF: pass (google.com: domain of katelynconsumer@gmail.com designates 209.85.212.47 as permitted sender) client-ip=209.85.212.47;\n" +
            "Authentication-Results: mx.google.com; spf=pass (google.com: domain of katelynconsumer@gmail.com designates 209.85.212.47 as permitted sender) smtp.mail=katelynconsumer@gmail.com; dkim=pass (test mode) header.i=@gmail.com\n" +
            "Received: by mail-vw0-f47.google.com with SMTP id 6so1485106vws.34\n" +
            "        for <dominique.derrien@gmail.com>; Mon, 20 Dec 2010 17:36:27 -0800 (PST)\n" +
            "DKIM-Signature: v=1; a=rsa-sha256; c=relaxed/relaxed;\n" +
            "        d=gmail.com; s=gamma;\n" +
            "        h=domainkey-signature:received:received:from:content-type\n" +
            "         :content-transfer-encoding:subject:date:message-id:cc:to\n" +
            "         :mime-version:x-mailer;\n" +
            "        bh=t7KU0c8GXDI+1/XpOv7toqYcmjzsyWxApYvVMUrjkpY=;\n" +
            "        b=gChncTx1TnOu7RJwZxfzMJ89up455F5/QWAW0wJzBD+ExZD4WvkF8/k380pDrXWVcA\n" +
            "         58Iv6/sV0hMkKlEpfk2oMdGdLdgj7uXaqRQSA1iZyzDuOcJyupJm7+k4naIfiV0Tt9TT\n" +
            "         Pis/e7Ck2ylgYbWRcVBE+uunEnm4pPEZoonMo=\n" +
            "DomainKey-Signature: a=rsa-sha1; c=nofws;\n" +
            "        d=gmail.com; s=gamma;\n" +
            "        h=from:content-type:content-transfer-encoding:subject:date:message-id\n" +
            "         :cc:to:mime-version:x-mailer;\n" +
            "        b=XDAOy5ULXitTPhZSsc2mPTQ2tk9r0ZkaqQuUx9AYtVfrMOy9WlWCJpQdA/0MdPrC1N\n" +
            "         eB+2LylbZMrXtJ31o2UAi82XRAkgGi0UsJotlskE9QEGGFYd/kFv5rC333En76WoBy7f\n" +
            "         0wrCkobjemMu3R4ys0mYU006l/dotg/OTk2HQ=\n" +
            "Received: by 10.220.186.195 with SMTP id ct3mr1485248vcb.57.1292895387922;\n" +
            "        Mon, 20 Dec 2010 17:36:27 -0800 (PST)\n" +
            "Return-Path: <katelynconsumer@gmail.com>\n" +
            "Received: from [192.168.0.101] (modemcable127.8-58-74.mc.videotron.ca [74.58.8.127])\n" +
            "        by mx.google.com with ESMTPS id y4sm925779vch.11.2010.12.20.17.36.27\n" +
            "        (version=TLSv1/SSLv3 cipher=RC4-MD5);\n" +
            "        Mon, 20 Dec 2010 17:36:27 -0800 (PST)\n" +
            "From: Katelyn <katelynconsumer@gmail.com>\n" +
            "Content-Type: text/plain; charset=iso-8859-1\n" +
            "Content-Transfer-Encoding: quoted-printable\n" +
            "Subject: Re: Notification de AnotherSocialEconomy pour la Demande: 689001\n" +
            "Date: Mon, 20 Dec 2010 20:36:26 -0500\n" +
            "Message-Id: <D285AB1E-3B97-4075-99DB-0231ADA194B2@gmail.com>\n" +
            "Cc: dominique.derrien@gmail.com\n" +
            "To: assistant@" + emailDomain + "\n" +
            "Mime-Version: 1.0 (Apple Message framework v1082)\n" +
            "X-Mailer: Apple Mail (2.1082)\n" +
            "\n" +
            "confirmer proposition:690001=20\n" +
            "--\n" +
            "Cette commande va =EAtre trait=E9e automatiquement par le moteur de =\n" +
            "AnotherSocialEconomy.com.\n" +
            "\n" +
            "*** Dom ****\n" +
            "System responds with ... from this Mac Mail\n" +
            "\n" +
            ":-( The System encountered an unexpected error! You can resend your =\n" +
            "message, or, report incident identifier: 323-0.="
        );

        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
        };

        MimeMessage mailMessage = MailConnector.getMailMessage(request);

        assertNotNull(mailMessage);
        assertEquals("katelynconsumer@gmail.com", ((InternetAddress) mailMessage.getFrom()[0]).getAddress());
        assertEquals("Katelyn", ((InternetAddress) mailMessage.getFrom()[0]).getPersonal());
        assertEquals("confirmer proposition:690001", MailConnector.getText(mailMessage));
        assertEquals(0, stream.getNotProcessedContents().length());
    }

    @Test
    public void defect_7845587_II() throws IOException, MessagingException {
        // https://www.pivotaltracker.com/story/show/7845587
        final MockServletInputStream stream = new MockServletInputStream();
        stream.setData(
            "Delivered-To: dominique.derrien@gmail.com\n" +
            "Received: by 10.229.80.81 with SMTP id s17cs100883qck;\n" +
            "        Mon, 20 Dec 2010 17:36:29 -0800 (PST)\n" +
            "Received: by 10.224.54.69 with SMTP id p5mr4751769qag.46.1292895389080;\n" +
            "        Mon, 20 Dec 2010 17:36:29 -0800 (PST)\n" +
            "Return-Path: <katelynconsumer@gmail.com>\n" +
            "Received: from mail-vw0-f47.google.com (mail-vw0-f47.google.com [209.85.212.47])\n" +
            "        by mx.google.com with ESMTP id m40si3472957vcr.55.2010.12.20.17.36.27;\n" +
            "        Mon, 20 Dec 2010 17:36:28 -0800 (PST)\n" +
            "Received-SPF: pass (google.com: domain of katelynconsumer@gmail.com designates 209.85.212.47 as permitted sender) client-ip=209.85.212.47;\n" +
            "Authentication-Results: mx.google.com; spf=pass (google.com: domain of katelynconsumer@gmail.com designates 209.85.212.47 as permitted sender) smtp.mail=katelynconsumer@gmail.com; dkim=pass (test mode) header.i=@gmail.com\n" +
            "Received: by mail-vw0-f47.google.com with SMTP id 6so1485106vws.34\n" +
            "        for <dominique.derrien@gmail.com>; Mon, 20 Dec 2010 17:36:27 -0800 (PST)\n" +
            "DKIM-Signature: v=1; a=rsa-sha256; c=relaxed/relaxed;\n" +
            "        d=gmail.com; s=gamma;\n" +
            "        h=domainkey-signature:received:received:from:content-type\n" +
            "         :content-transfer-encoding:subject:date:message-id:cc:to\n" +
            "         :mime-version:x-mailer;\n" +
            "        bh=t7KU0c8GXDI+1/XpOv7toqYcmjzsyWxApYvVMUrjkpY=;\n" +
            "        b=gChncTx1TnOu7RJwZxfzMJ89up455F5/QWAW0wJzBD+ExZD4WvkF8/k380pDrXWVcA\n" +
            "         58Iv6/sV0hMkKlEpfk2oMdGdLdgj7uXaqRQSA1iZyzDuOcJyupJm7+k4naIfiV0Tt9TT\n" +
            "         Pis/e7Ck2ylgYbWRcVBE+uunEnm4pPEZoonMo=\n" +
            "DomainKey-Signature: a=rsa-sha1; c=nofws;\n" +
            "        d=gmail.com; s=gamma;\n" +
            "        h=from:content-type:content-transfer-encoding:subject:date:message-id\n" +
            "         :cc:to:mime-version:x-mailer;\n" +
            "        b=XDAOy5ULXitTPhZSsc2mPTQ2tk9r0ZkaqQuUx9AYtVfrMOy9WlWCJpQdA/0MdPrC1N\n" +
            "         eB+2LylbZMrXtJ31o2UAi82XRAkgGi0UsJotlskE9QEGGFYd/kFv5rC333En76WoBy7f\n" +
            "         0wrCkobjemMu3R4ys0mYU006l/dotg/OTk2HQ=\n" +
            "Received: by 10.220.186.195 with SMTP id ct3mr1485248vcb.57.1292895387922;\n" +
            "        Mon, 20 Dec 2010 17:36:27 -0800 (PST)\n" +
            "Return-Path: <katelynconsumer@gmail.com>\n" +
            "Received: from [192.168.0.101] (modemcable127.8-58-74.mc.videotron.ca [74.58.8.127])\n" +
            "        by mx.google.com with ESMTPS id y4sm925779vch.11.2010.12.20.17.36.27\n" +
            "        (version=TLSv1/SSLv3 cipher=RC4-MD5);\n" +
            "        Mon, 20 Dec 2010 17:36:27 -0800 (PST)\n" +
            "From: Katelyn <katelynconsumer@gmail.com>\n" +
            "Content-Type: text/plain; charset=iso-8859-1\n" +
            "Content-Transfer-Encoding: quoted-printable\n" +
            "Subject: Re: Notification de AnotherSocialEconomy pour la Demande: 689001\n" +
            "Date: Mon, 20 Dec 2010 20:36:26 -0500\n" +
            "Message-Id: <D285AB1E-3B97-4075-99DB-0231ADA194B2@gmail.com>\n" +
            "Cc: dominique.derrien@gmail.com\n" +
            "To: assistant@" + emailDomain + "\n" +
            "Mime-Version: 1.0 (Apple Message framework v1082)\n" +
            "X-Mailer: Apple Mail (2.1082)\n" +
            "\n" +
            "confirmer proposition:690001=20=0A=\r\n" +
            "--=0A=\r\n" +
            "Cette commande va =EAtre trait=E9e automatiquement par le moteur de=20=\r\n" +
            "AnotherSocialEconomy.com."
        );

//        Other valid alternative
//            "confirmer=20proposition:690001=20=0A--=0ACette=20commande=20va=20=EAtre=\r\n" +
//            "=20trait=E9e=20automatiquement=20par=20le=20moteur=20de=20=\r\n" +
//            "AnotherSocialEconomy.com."

        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
        };


        MimeMessage mailMessage = MailConnector.getMailMessage(request);

        assertNotNull(mailMessage);
        assertEquals("katelynconsumer@gmail.com", ((InternetAddress) mailMessage.getFrom()[0]).getAddress());
        assertEquals("Katelyn", ((InternetAddress) mailMessage.getFrom()[0]).getPersonal());
        assertEquals("confirmer proposition:690001 \n--\nCette commande va être traitée automatiquement par le moteur de AnotherSocialEconomy.com.", MailConnector.getText(mailMessage));
        assertEquals(0, stream.getNotProcessedContents().length());
    }

    @Test
    public void defect_7845587_III() throws IOException, MessagingException {
        // https://www.pivotaltracker.com/story/show/7845587
        final MockServletInputStream stream = new MockServletInputStream();
        stream.setData(
            "Delivered-To: dominique.derrien@gmail.com\n" +
            "Received: by 10.229.80.81 with SMTP id s17cs100883qck;\n" +
            "        Mon, 20 Dec 2010 17:36:29 -0800 (PST)\n" +
            "Received: by 10.224.54.69 with SMTP id p5mr4751769qag.46.1292895389080;\n" +
            "        Mon, 20 Dec 2010 17:36:29 -0800 (PST)\n" +
            "Return-Path: <katelynconsumer@gmail.com>\n" +
            "Received: from mail-vw0-f47.google.com (mail-vw0-f47.google.com [209.85.212.47])\n" +
            "        by mx.google.com with ESMTP id m40si3472957vcr.55.2010.12.20.17.36.27;\n" +
            "        Mon, 20 Dec 2010 17:36:28 -0800 (PST)\n" +
            "Received-SPF: pass (google.com: domain of katelynconsumer@gmail.com designates 209.85.212.47 as permitted sender) client-ip=209.85.212.47;\n" +
            "Authentication-Results: mx.google.com; spf=pass (google.com: domain of katelynconsumer@gmail.com designates 209.85.212.47 as permitted sender) smtp.mail=katelynconsumer@gmail.com; dkim=pass (test mode) header.i=@gmail.com\n" +
            "Received: by mail-vw0-f47.google.com with SMTP id 6so1485106vws.34\n" +
            "        for <dominique.derrien@gmail.com>; Mon, 20 Dec 2010 17:36:27 -0800 (PST)\n" +
            "DKIM-Signature: v=1; a=rsa-sha256; c=relaxed/relaxed;\n" +
            "        d=gmail.com; s=gamma;\n" +
            "        h=domainkey-signature:received:received:from:content-type\n" +
            "         :content-transfer-encoding:subject:date:message-id:cc:to\n" +
            "         :mime-version:x-mailer;\n" +
            "        bh=t7KU0c8GXDI+1/XpOv7toqYcmjzsyWxApYvVMUrjkpY=;\n" +
            "        b=gChncTx1TnOu7RJwZxfzMJ89up455F5/QWAW0wJzBD+ExZD4WvkF8/k380pDrXWVcA\n" +
            "         58Iv6/sV0hMkKlEpfk2oMdGdLdgj7uXaqRQSA1iZyzDuOcJyupJm7+k4naIfiV0Tt9TT\n" +
            "         Pis/e7Ck2ylgYbWRcVBE+uunEnm4pPEZoonMo=\n" +
            "DomainKey-Signature: a=rsa-sha1; c=nofws;\n" +
            "        d=gmail.com; s=gamma;\n" +
            "        h=from:content-type:content-transfer-encoding:subject:date:message-id\n" +
            "         :cc:to:mime-version:x-mailer;\n" +
            "        b=XDAOy5ULXitTPhZSsc2mPTQ2tk9r0ZkaqQuUx9AYtVfrMOy9WlWCJpQdA/0MdPrC1N\n" +
            "         eB+2LylbZMrXtJ31o2UAi82XRAkgGi0UsJotlskE9QEGGFYd/kFv5rC333En76WoBy7f\n" +
            "         0wrCkobjemMu3R4ys0mYU006l/dotg/OTk2HQ=\n" +
            "Received: by 10.220.186.195 with SMTP id ct3mr1485248vcb.57.1292895387922;\n" +
            "        Mon, 20 Dec 2010 17:36:27 -0800 (PST)\n" +
            "Return-Path: <katelynconsumer@gmail.com>\n" +
            "Received: from [192.168.0.101] (modemcable127.8-58-74.mc.videotron.ca [74.58.8.127])\n" +
            "        by mx.google.com with ESMTPS id y4sm925779vch.11.2010.12.20.17.36.27\n" +
            "        (version=TLSv1/SSLv3 cipher=RC4-MD5);\n" +
            "        Mon, 20 Dec 2010 17:36:27 -0800 (PST)\n" +
            "From: Katelyn <katelynconsumer@gmail.com>\n" +
            "Content-Type: text/plain; charset=iso-8859-1\n" +
            "Content-Transfer-Encoding: quoted-printable\n" +
            "Subject: Re: Notification de AnotherSocialEconomy pour la Demande: 689001\n" +
            "Date: Mon, 20 Dec 2010 20:36:26 -0500\n" +
            "Message-Id: <D285AB1E-3B97-4075-99DB-0231ADA194B2@gmail.com>\n" +
            "Cc: dominique.derrien@gmail.com\n" +
            "To: assistant@" + emailDomain + "\n" +
            "Mime-Version: 1.0 (Apple Message framework v1082)\n" +
            "X-Mailer: Apple Mail (2.1082)\n" +
            "\n" +
            "confirmer proposition:690001=20\n" +
            "--\n" +
            "Cette commande va =EAtre trait=E9e automatiquement par le moteur de =\n" +
            "AnotherSocialEconomy.com.\n" +
            "\n" +
            "*** Dom ****\n" +
            "System responds with ... from this Mac Mail\n" +
            "\n" +
            ":-( The System encountered an unexpected error! You can resend your =\n" +
            "message, or, report incident identifier: 323-0.="
        );

        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
        };

        MimeMessage mailMessage = MailConnector.getMailMessage(request);

        assertNotNull(mailMessage);
        assertEquals("katelynconsumer@gmail.com", ((InternetAddress) mailMessage.getFrom()[0]).getAddress());
        assertEquals("Katelyn", ((InternetAddress) mailMessage.getFrom()[0]).getPersonal());
        assertEquals("confirmer proposition:690001 \n--\nCette commande va être traitée automatiquement par le moteur de AnotherSocialEconomy.com.\n\n*** Dom ****\nSystem responds with ... from this Mac Mail\n\n:-( The System encountered an unexpected error! You can resend your message, or, report incident identifier: 323-0.", MailConnector.alternateGetText(mailMessage));
        assertEquals(0, stream.getNotProcessedContents().length());
    }

    @Test(expected=MessagingException.class)
    public void testReportErrorToAdminsI() throws MessagingException {
        MailConnector.foolNextMessagePost();
        MailConnector.reportErrorToAdmins("fooled attempt", "fooled attempt");
    }

    @Test
    public void testReportErrorToAdminsII() throws MessagingException {
        MailConnector.reportErrorToAdmins("subject", "body");
    }

    @Test
    public void testReportErrorToAdminsIII() throws MessagingException {
        MailConnector.reportErrorToAdmins("test@unit.org", "subject", "body");
    }

    @Test(expected=CommunicationException.class)
    public void testSendCopyToAdminsI() throws CommunicationException {
        MailConnector.foolNextMessagePost();
        Consumer resource = new Consumer();
        resource.setEmail("a@a.aa");
        MailConnector.sendCopyToAdmins(Source.mail, resource, "fooled message", null);
    }

    @Test
    public void testSendCopyToAdminsII() throws CommunicationException {
        MailConnector.sendCopyToAdmins(Source.mail, new Consumer(), "fooled message", null);
    }

    @Test
    public void testSendCopyToAdminsIII() throws CommunicationException {
        MailConnector.sendCopyToAdmins(Source.mail, new Consumer(), "fooled message", new String[0]);
    }

    @Test
    public void testSendCopyToAdminsIV() throws CommunicationException {
        MailConnector.sendCopyToAdmins(Source.mail, new Consumer(), "fooled message", new String[] { "one", "two", "three" });
    }

    @Test
    public void testAlternateGetTextI() throws MessagingException, IOException {
        Session session =  Session.getDefaultInstance(new Properties(), null);
        assertNull(MailConnector.alternateGetText(new MimeMessage(session) {
            @Override
            public InputStream getRawInputStream() {
                return null;
            }
        }));
    }

    private static String reference = "confirmer proposition:690001 \n--\nCette commande va être traitée automatiquement par le moteur de AnotherSocialEconomy.com.";

    @Test
    public void testAlternateGetTextII() throws MessagingException, IOException {
        Session session =  Session.getDefaultInstance(new Properties(), null);
        String content = MailConnector.alternateGetText(new MimeMessage(session) {
            @Override
            public InputStream getRawInputStream() {
                return new MockInputStream(
                    "confirmer proposition:690001=20=0A--=0A=\n" +
                    "Cette commande va =C3=AAtre trait=C3=A9e automatiquement par le moteur de =\n" +
                    "AnotherSocialEconomy.com.="
                );
            }
            @Override
            public String getContentType() {
                return "text/plain"; // charset will be defaulted on UTF-8
            }
        });
        assertNotNull(content);
        assertEquals(reference, content);
    }

    @Test
    public void testAlternateGetTextIII() throws MessagingException, IOException {
        Session session =  Session.getDefaultInstance(new Properties(), null);
        String content = MailConnector.alternateGetText(new MimeMessage(session) {
            @Override
            public InputStream getRawInputStream() {
                return new MockInputStream(
                    "confirmer proposition:690001=20=0A--=0A=\r\n" +
                    "Cette commande va =EAtre trait=E9e automatiquement par le moteur de =\r\n" +
                    "AnotherSocialEconomy.com.="
                );
            }
            @Override
            public String getContentType() {
                return "text/plain; charset=iso-8859-1";
            }
        });
        assertNotNull(content);
        assertEquals(reference, content);
    }

    @Test
    public void testAlternateGetTextIV() throws MessagingException, IOException {
        Session session =  Session.getDefaultInstance(new Properties(), null);
        String content = MailConnector.alternateGetText(new MimeMessage(session) {
            @Override
            public InputStream getRawInputStream() {
                return new MockInputStream(
                    "confirmer proposition:690001=20\n" +
                    "--\n" +
                    "Cette commande va =EAtre trait=E9e automatiquement par le moteur de =\n" +
                    "AnotherSocialEconomy.com.="
                );
            }
            @Override
            public String getContentType() {
                return "text/plain; charset=iso-8859-1";
            }
        });
        assertNotNull(content);
        assertEquals(reference, content);
    }
}
