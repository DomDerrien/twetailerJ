package twetailer.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;

import javamocks.util.logging.MockLogger;

import javax.mail.MessagingException;
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

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import domderrien.i18n.LabelExtractor;

public class TestMailConnector {

    private static LocalServiceTestHelper  helper;

    @BeforeClass
    public static void setUpBeforeClass() {
        BaseConnector.setLogger(new MockLogger("test", null));
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());;
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

    public static MockServletInputStream prepareEmptyStream(String from, String name) {
        MockServletInputStream stream = new MockServletInputStream();
        stream.setData(
                "MIME-Version: 1.0\n" +
                "Date: Fri, 06 Nov 2009 20:01:37 -0500\n" +
                "From: " + name + "<" + from + ">\n" +
                "To: Twetailer <maezel@twetailer.appspotmail.com>\n" +
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
        final MockServletInputStream stream = prepareEmptyStream(from, name);
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

    public static MockServletInputStream prepareTextStream(String from, String name, String subject, String message) {
        MockServletInputStream stream = new MockServletInputStream();
        stream.setData(
                "MIME-Version: 1.0\n" +
                "Date: Fri, 06 Nov 2009 20:01:37 -0500\n" +
                (
                        from == null && name == null ?
                                ""  :
                                from == null ?
                                        "From:\n"  :
                                        "From: " + name + "<" + from + ">\n"
                ) +
                "To: Twetailer <maezel@twetailer.appspotmail.com>\n" +
                "Cc: unit@test.net\n" +
                (
                        subject == null ?
                                "" :
                                "Subject: " + subject + "\n"
                ) +
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
        final MockServletInputStream stream = prepareTextStream(from, name, subject, message);
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
                "To: Twetailer <maezel@twetailer.appspotmail.com>\n" +
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
                "To: Twetailer <maezel@twetailer.appspotmail.com>\n" +
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
                "To: Twetailer <maezel@twetailer.appspotmail.com>\n" +
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
                "To: Twetailer <maezel@twetailer.appspotmail.com>\n" +
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
                "To: Twetailer <maezel@twetailer.appspotmail.com>\n" +
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
                "To: Twetailer <maezel@twetailer.appspotmail.com>\n" +
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
                "To: Twetailer <maezel@twetailer.appspotmail.com>\n" +
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
        MailConnector.sendMailMessage(
                "testId",
                "testName",
                LabelExtractor.get("mc_mail_subject_response_prefix", Locale.ENGLISH) + "subject",
                "******************\n******************\ntest exhaustif pour voir où est \nla faute...\n******************\n******************",
                Locale.ENGLISH
        );
    }

    @Test
    public void testSendMailMessageII() throws UnsupportedEncodingException, MessagingException {
        MailConnector.sendMailMessage(
                "testId",
                "testName",
                "subject",
                "******************\n******************\ntest exhaustif pour voir où est \nla faute...\n******************\n******************",
                Locale.ENGLISH
        );
    }

    @Test
    public void testSendMailMessageIII() throws UnsupportedEncodingException, MessagingException {
        MailConnector.sendMailMessage(
                "testId",
                "testName",
                "",
                "******************\n******************\ntest exhaustif pour voir où est \nla faute...\n******************\n******************",
                Locale.ENGLISH
        );
    }

    @Test
    public void testSendMailMessageIV() throws UnsupportedEncodingException, MessagingException {
        MailConnector.sendMailMessage(
                "testId",
                "testName",
                null,
                "******************\n******************\ntest exhaustif pour voir où est \nla faute...\n******************\n******************",
                Locale.ENGLISH
        );
    }

    @Test
    public void testPrepareInternetAddressI() {
        String name = "name";
        String email = "unit@test.ca";

        InternetAddress test = MailConnector.prepareInternetAddress("UTF-8", name, email);

        assertEquals(name, test.getPersonal());
        assertEquals(email, test.getAddress());
    }

    @Test
    @Ignore
    public void testPrepareInternetAddressII() {
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
}
