package twetailer.j2ee;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Locale;

import javamocks.util.logging.MockLogger;

import javax.jdo.PersistenceManager;
import javax.mail.internet.InternetAddress;
import javax.servlet.MockServletInputStream;
import javax.servlet.ServletInputStream;
import javax.servlet.http.MockHttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import twetailer.connector.MailConnector;
import twetailer.connector.TestMailConnector;
import twetailer.dao.ConsumerOperations;
import twetailer.dao.MockBaseOperations;
import twetailer.dao.RawCommandOperations;
import twetailer.dto.Consumer;
import twetailer.dto.RawCommand;
import twetailer.task.step.BaseSteps;
import twetailer.validator.ApplicationSettings;

import com.google.appengine.api.datastore.DatastoreTimeoutException;
import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import domderrien.i18n.LabelExtractor;

public class TestMailResponderServlet {

    private static LocalServiceTestHelper  helper;
    private static String emailDomain;

    @BeforeClass
    public static void setUpBeforeClass() {
        MailResponderServlet.setLogger(new MockLogger("test", null));
        helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
        emailDomain = ApplicationSettings.get().getProductEmailDomain();
    }

    @Before
    public void setUp() throws Exception {
        BaseSteps.resetOperationControllers(true);
        BaseSteps.setMockBaseOperations(new MockBaseOperations());
        helper.setUp();
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();
    }

    @Test
    public void testConstructor() {
        new MailResponderServlet();
    }

    @Test
    public void testDoPostI() throws IOException {
        //
        // Normal process without trouble
        //
        final String from = "test-emitter@appspot.com";
        final String name = "Mr Emitter";
        final String subject = "Not important!";
        final String message = "wii console Mario Kart"; // FIXME: -- àéüôç";
        final MockServletInputStream stream = TestMailConnector.prepareTextStream(from, name, subject, message);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
        };

        final Long rawCommandKey = 12345L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand createRawCommand(PersistenceManager pm, RawCommand rawCommand) {
                assertEquals(from, rawCommand.getEmitterId());
                assertEquals(message + " cc:unit@test.net", rawCommand.getCommand());
                rawCommand.setKey(rawCommandKey);
                return rawCommand;
            }
            @Override
            public RawCommand updateRawCommand(RawCommand rawCommand) {
                fail("Call not expected");
                return null;
            }
        });

        final Long consumerKey = 56645L;
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer createConsumer(PersistenceManager pm, InternetAddress address) {
                assertEquals(from, address.getAddress());
                assertEquals(name, address.getPersonal());
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                consumer.setName(address.getPersonal());
                consumer.setEmail(address.getAddress());
                return consumer;
            }
        });

        new MailResponderServlet().doPost(request, null);
    }

    @Test
    public void testDoPostII() throws IOException {
        //
        // Normal process with an empty message
        //
        final String from = "test-emitter@appspot.com";
        final String name = "Mr Emitter";
        final MockServletInputStream stream = TestMailConnector.prepareEmptyStream(from, name);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
        };

        final Long rawCommandKey = 12345L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand createRawCommand(PersistenceManager pm, RawCommand rawCommand) {
                assertEquals(from, rawCommand.getEmitterId());
                assertEquals("cc:unit@test.net", rawCommand.getCommand());
                rawCommand.setKey(rawCommandKey);
                return rawCommand;
            }
            @Override
            public RawCommand updateRawCommand(RawCommand rawCommand) {
                fail("Call not expected");
                return null;
            }
        });

        final Long consumerKey = 56645L;
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer createConsumer(PersistenceManager pm, InternetAddress address) {
                assertEquals(from, address.getAddress());
                assertEquals(name, address.getPersonal());
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                consumer.setName(address.getPersonal());
                consumer.setEmail(address.getAddress());
                return consumer;
            }
        });

        new MailResponderServlet().doPost(request, null);
    }

    public static MockServletInputStream prepareStreamWithoutMessage(String from, String name) {
        MockServletInputStream stream = new MockServletInputStream();
        stream.setData(
                "MIME-Version: 1.0\n" +
                "Date: Fri, 06 Nov 2009 20:01:37 -0500\n" +
                "Content-Language: en\n" +
                "Accept-Language: en, fr\n" +
                "From: " + name + "<" + from + ">\n" +
                "To: Twetailer <assistant@" + emailDomain + ">\n" +
                "Cc: unit@test.net\n" +
                "Subject: Twetailer\n" +
                "Content-Type: multipart/alternative; boundary=BBBBBB\n" +
                "\n" +
                "--BBBBBB\n" +
                "\n" +
                "--BBBBBB--"
        );
        return stream;
    }

    @Test
    public void testDoPostIV() throws IOException {
        //
        // Normal process with an blank message
        //
        final String from = "test-emitter@appspot.com";
        final String name = "Mr Emitter";
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return prepareStreamWithoutMessage(from, name);
            }
        };

        final Long rawCommandKey = 12345L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand createRawCommand(PersistenceManager pm, RawCommand rawCommand) {
                assertNotNull(from, rawCommand.getEmitterId());
                assertNull(rawCommand.getCommand());
                assertNotNull(rawCommand.getErrorMessage());
                assertEquals(LabelExtractor.get("error_mail_messaging", Locale.ENGLISH), rawCommand.getErrorMessage());
                rawCommand.setKey(rawCommandKey);
                return rawCommand;
            }
            @Override
            public RawCommand updateRawCommand(RawCommand rawCommand) {
                fail("Call not expected");
                return null;
            }
        });

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer createConsumer(PersistenceManager pm, InternetAddress address) {
                fail("Call not expected");
                return null;
            }
        });

        new MailResponderServlet().doPost(request, null);
    }

    public static MockServletInputStream prepareStreamWithoutFrom(String from, String name) {
        MockServletInputStream stream = new MockServletInputStream();
        stream.setData(
                "MIME-Version: 1.0\n" +
                "Date: Fri, 06 Nov 2009 20:01:37 -0500\n" +
                "To: Twetailer <assistant@" + emailDomain + ">\n" +
                "Cc: unit@test.net\n" +
                "Subject: Twetailer\n" +
                "Content-Type: multipart/alternative; boundary=BBBBBB\n" +
                "\n" +
                "--BBBBBB\n" +
                "\n" +
                "--BBBBBB--"
        );
        return stream;
    }

    @Test
    public void testDoPostV() throws IOException {
        //
        // Normal process without "from:" information
        //
        final String from = "test-emitter@appspot.com";
        final String name = "Mr Emitter";
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return prepareStreamWithoutFrom(from, name);
            }
        };

        final Long rawCommandKey = 12345L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand createRawCommand(PersistenceManager pm, RawCommand rawCommand) {
                assertNull(rawCommand.getEmitterId());
                assertNull(rawCommand.getCommand());
                assertNotNull(rawCommand.getErrorMessage());
                assertEquals(LabelExtractor.get("error_mail_messaging", Locale.ENGLISH), rawCommand.getErrorMessage());
                rawCommand.setKey(rawCommandKey);
                return rawCommand;
            }
            @Override
            public RawCommand updateRawCommand(RawCommand rawCommand) {
                fail("Call not expected");
                return null;
            }
        });

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer createConsumer(PersistenceManager pm, InternetAddress address) {
                fail("Call not expected");
                return null;
            }
        });

        new MailResponderServlet().doPost(request, null);
    }

    @Test
    public void testExtractFirstLineIa() {
        String out = null;
        String in = out;

        assertEquals(out, MailResponderServlet.extractFirstLine(in));
    }

    @Test
    public void testExtractFirstLineIb() {
        String out = "";
        String in = out;

        assertEquals(out, MailResponderServlet.extractFirstLine(in));
    }

    @Test
    public void testExtractFirstLineIc() {
        String out = "blah-blah-blah";
        String in = out;

        assertEquals(out, MailResponderServlet.extractFirstLine(in));
    }

    @Test
    public void testExtractFirstLineId() {
        String out = "blah-blah-blah";
        String in = out + "\n\n\n";

        assertEquals(out, MailResponderServlet.extractFirstLine(in));
    }

    @Test
    public void testExtractFirstLineII() {
        String out = "blah-blah-blah";
        String in = " \r\n \t " + out + " \t \r\n \t ";

        assertEquals(out, MailResponderServlet.extractFirstLine(in));
    }

    @Test
    public void testExtractFirstLineIII() {
        String out = "blah-blah-blah";
        String in = " \r\n \t " + out + " \t \r\n\r\n subsequent message part being ignored";

        assertEquals(out, MailResponderServlet.extractFirstLine(in));
    }

    public static MockServletInputStream prepareTooBigStream(String from, String name) {
        MockServletInputStream stream = new MockServletInputStream();
        stream.setData(
                "Received: by 10.213.103.139 with SMTP id k11mr835270ebo.22.1264369051705;\n" +
                "Sun, 24 Jan 2010 13:37:31 -0800 (PST)\n" +
                "Return-Path: <steven.milstein@gmail.com>\n" +
                "Received: from ey-out-2122.google.com (ey-out-2122.google.com [74.125.78.26])\n" +
                "       by gmr-mx.google.com with ESMTP id 12si546282ewy.6.2010.01.24.13.37.30;\n" +
                "       Sun, 24 Jan 2010 13:37:30 -0800 (PST)\n" +
                "Received-SPF: pass (google.com: domain of steven.milstein@gmail.com designates 74.125.78.26 as permitted sender) client-ip=74.125.78.26;\n" +
                "Authentication-Results: gmr-mx.google.com; spf=pass (google.com: domain of steven.milstein@gmail.com designates 74.125.78.26 as permitted sender) smtp.mail=steven.milstein@gmail.com; dkim=pass (test mode) header.i=@gmail.com\n" +
                "Received: by ey-out-2122.google.com with SMTP id d26so774850eyd.19\n" +
                "       for <assistant@" + emailDomain + ">; Sun, 24 Jan 2010 13:37:30 -0800 (PST) DKIM-Signature: v=1; a=rsa-sha256; c=relaxed/relaxed;\n" +
                "       d=gmail.com; s=gamma;\n" +
                "       h=domainkey-signature:mime-version:received:in-reply-to:references\n" +
                "        :date:message-id:subject:from:to:content-type;\n" +
                "       bh=PJelFgZ9VD3XXr0+Zu+aTWU8t7PTt80Da+dWc3FZW0k=;\n" +
                "       b=EB6lahB2TkIgdxLXh100i3p7t4CR6PNDE7CUxkaPVRbwwOpZcMLV92xe0OxGDcfxNq\n" +
                "        9akDiCe+LhwuEM+wFddtJQ+bmuST+I/b+n6Y8+fn7vZOOl9VC69qViRi9oqFjxTOrE8O\n" +
                "        AnaGAkQO8n/Yoy4NojbbtsBhs+erbxThpo4VY=DomainKey-Signature: a=rsa-sha1; c=nofws;\n" +
                "       d=gmail.com; s=gamma;\n" +
                "       h=mime-version:in-reply-to:references:date:message-id:subject:from:to\n" +
                "        :content-type;\n" +
                "       b=enihP3aQyCuO94K9u1vr18z3U+ftNuMrX2m0hiOeDWgz9UJkev27GBnJR+N+28EV4w\n" +
                "        lmIycukXbONLEbKvLHE6VD5PHY2L7F03SsyzkvD5pP6Q7p09A9t1lIOhZGiEnEzwvFT2\n" +
                "        6Ltt0P6AxaRtDw0xeR1NCvJND0SBftp83KOno=\n" +
                "MIME-Version: 1.0\n" +
                "Received: by 10.213.41.82 with SMTP id n18mr1402796ebe.20.1264369050372; Sun,\n" +
                "       24 Jan 2010 13:37:30 -0800 (PST)\n" +
                "In-Reply-To: <001636c924e0aac57a047defd025@google.com>\n" +
                "References: <001636c924e0aac57a047defd025@google.com>\n" +
                "Date: Sun, 24 Jan 2010 16:37:30 -0500\n" +
                "Message-ID: <ea8a427d1001241337r191b2e7al24f8a3c37a09b03@mail.gmail.com>\n" +
                "Subject: Re: Delete existing Demands to work-around Expiration issue\n" +
                "From: Steven Milstein <steven.milstein@gmail.com>\n" +
                "To: Twetailer <assistant@" + emailDomain + ">\n" +
                "Content-Type: multipart/alternative; boundary=0016361374dc1ccd07047defdca2\n" +
                "\n" +
                "!cancel ref:60014\n" +
                "\n" +
                "On Sun, Jan 24, 2010 at 4:34 PM, Twetailer <assistant@" + emailDomain + "> wrote:\n" +
                "\n" +
                "> :-) Listing 6 active Demand(s)...\n" +
                "> action:demand reference:66037 state:confirmed expiration:2009-12-18 locale:33009 US range:25.0mi quantity:1 #demo tags:pizza delivery http_//myloc.me/2bf50\n" +
                "> action:demand reference:64002 state:published expiration:2010-01-13 locale:H9B1X9 CA range:100.0km quantity:1 tags:D wii console\n" +
                "> action:demand reference:60026 state:published expiration:2010-01-04 locale:H9B1X9 CA range:100.0km quantity:1 #demo tags:nintendo ds lite\n" +
                "> action:demand reference:60014 state:published expiration:2010-01-03 locale:H9B1X9 CA range:100.0km quantity:1 tags:Wii fit\n" +
                "> action:demand reference:50009 state:published expiration:2009-12-12 locale:H2X1Z5 CA range:100.0km quantity:1 tags:Audi S4 2008 2009 http_//myloc.me/1teHv\n" +
                "> action:demand reference:50006 state:published expiration:2009-12-12 locale:H2X1Z5 CA range:100.0km quantity:1 tags:used audi s4 2009\n" +
                ">"
        );
        return stream;
    }

    @Test
    public void testDoPostVIa() throws IOException {
        //
        // DatastoreTimeoutException while communicating with the back-end
        //
        final String from = "test-emitter@appspot.com";
        final String name = "Mr Emitter";
        final String subject = "Not important!";
        final String message = "wii console Mario Kart"; // FIXME: -- àéüôç";
        final MockServletInputStream stream = TestMailConnector.prepareTextStream(from, name, subject, message); // This stream does NOT contain language information
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
        };

        final Long rawCommandKey = 12345L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand createRawCommand(PersistenceManager pm, RawCommand rawCommand) {
                assertEquals(from, rawCommand.getEmitterId());
                assertEquals(message + " cc:unit@test.net", rawCommand.getCommand());
                assertNotNull(rawCommand.getErrorMessage());
                assertEquals(LabelExtractor.get("error_datastore_timeout", Locale.ENGLISH), rawCommand.getErrorMessage());
                rawCommand.setKey(rawCommandKey);
                return rawCommand;
            }
            @Override
            public RawCommand updateRawCommand(RawCommand rawCommand) {
                fail("Call not expected!");
                return null;
            }
        });

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer createConsumer(PersistenceManager pm, InternetAddress address) {
                assertEquals(from, address.getAddress());
                assertEquals(name, address.getPersonal());
                throw new DatastoreTimeoutException("Done in purpose");
            }
        });

        new MailResponderServlet().doPost(request, null);
    }

    @Test
    public void testDoPostVIb() throws IOException {
        //
        // DatastoreTimeoutException while communicating with the back-end
        //
        final String from = "test-emitter@appspot.com";
        final String name = "Mr Emitter";
        final MockServletInputStream stream = TestMailConnector.prepareEmptyStream(from, name); // This stream contains language information
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
        };

        final Long rawCommandKey = 12345L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand createRawCommand(PersistenceManager pm, RawCommand rawCommand) {
                assertEquals(from, rawCommand.getEmitterId());
                assertNotNull(rawCommand.getErrorMessage());
                assertEquals(LabelExtractor.get("error_datastore_timeout", Locale.ENGLISH), rawCommand.getErrorMessage());
                rawCommand.setKey(rawCommandKey);
                return rawCommand;
            }
            @Override
            public RawCommand updateRawCommand(RawCommand rawCommand) {
                fail("Call not expected!");
                return null;
            }
        });

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer createConsumer(PersistenceManager pm, InternetAddress address) {
                assertEquals(from, address.getAddress());
                assertEquals(name, address.getPersonal());
                throw new DatastoreTimeoutException("Done in purpose");
            }
        });

        new MailResponderServlet().doPost(request, null);
    }

    @Test
    public void testDoPostVII() throws IOException {
        //
        // Unexpected exception while communicating with the back-end
        //
        final String from = "test-emitter@appspot.com";
        final String name = "Mr Emitter";
        final String subject = "Not important!";
        final String message = "wii console Mario Kart"; // FIXME: -- àéüôç";
        final MockServletInputStream stream = TestMailConnector.prepareTextStream(from, name, subject, message);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
        };

        final Long rawCommandKey = 12345L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand createRawCommand(PersistenceManager pm, RawCommand rawCommand) {
                assertEquals(from, rawCommand.getEmitterId());
                assertEquals(message + " cc:unit@test.net", rawCommand.getCommand());
                assertNotNull(rawCommand.getErrorMessage());
                assertEquals(LabelExtractor.get("error_unexpected", new Object[] { 0L, "" }, Locale.ENGLISH), rawCommand.getErrorMessage());
                rawCommand.setKey(rawCommandKey);
                return rawCommand;
            }
            @Override
            public RawCommand updateRawCommand(RawCommand rawCommand) {
                fail("Call not expected!");
                assertEquals(rawCommandKey, rawCommand.getKey());
                return null;
            }
        });

        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer createConsumer(PersistenceManager pm, InternetAddress address) {
                assertEquals(from, address.getAddress());
                assertEquals(name, address.getPersonal());
                throw new IllegalArgumentException("Done in purpose");
            }
        });

        new MailResponderServlet().doPost(request, null);
    }

    @Test
    public void testDoPostVIII() throws IOException {
        //
        // Unexpected exception while preparing the task for the command processing
        //
        final String from = "test-emitter@appspot.com";
        final String name = "Mr Emitter";
        final String subject = "Not important!";
        final String message = "wii console Mario Kart"; // FIXME: -- àéüôç";
        final MockServletInputStream stream = TestMailConnector.prepareTextStream(from, name, subject, message);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
        };

        BaseSteps.setMockBaseOperations(new MockBaseOperations() {
            @Override
            public Queue getQueue() {
                throw new IllegalArgumentException("Done in purpose");
            }
        });

        final Long rawCommandKey = 12345L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand createRawCommand(PersistenceManager pm, RawCommand rawCommand) {
                assertEquals(from, rawCommand.getEmitterId());
                assertEquals(message + " cc:unit@test.net", rawCommand.getCommand());
                assertNull(rawCommand.getErrorMessage());
                rawCommand.setKey(rawCommandKey);
                return rawCommand;
            }
            @Override
            public RawCommand updateRawCommand(PersistenceManager pm, RawCommand rawCommand) {
                assertEquals(rawCommandKey, rawCommand.getKey());
                assertNotNull(rawCommand.getErrorMessage());
                assertEquals(LabelExtractor.get("error_unexpected", new Object[] { rawCommandKey, "" }, Locale.ENGLISH), rawCommand.getErrorMessage());
                return null;
            }
        });

        final Long consumerKey = 56645L;
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer createConsumer(PersistenceManager pm, InternetAddress address) {
                assertEquals(from, address.getAddress());
                assertEquals(name, address.getPersonal());
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                consumer.setName(address.getPersonal());
                consumer.setEmail(address.getAddress());
                return consumer;
            }
        });

        new MailResponderServlet().doPost(request, null);
    }

    @Test
    public void testDoPostIX() throws IOException {
        //
        // Unexpected exception while preparing the task for the command processing
        // And error while communicating to "catch-all" list
        //
        final String from = "test-emitter@appspot.com";
        final String name = "Mr Emitter";
        final String subject = "Not important!";
        final String message = "wii console Mario Kart"; // FIXME: -- àéüôç";
        final MockServletInputStream stream = TestMailConnector.prepareTextStream(from, name, subject, message);
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public ServletInputStream getInputStream() {
                return stream;
            }
        };

        BaseSteps.setMockBaseOperations(new MockBaseOperations() {
            @Override
            public Queue getQueue() {
                throw new IllegalArgumentException("Done in purpose");
            }
        });

        final Long rawCommandKey = 12345L;
        BaseSteps.setMockRawCommandOperations(new RawCommandOperations() {
            @Override
            public RawCommand createRawCommand(PersistenceManager pm, RawCommand rawCommand) {
                assertEquals(from, rawCommand.getEmitterId());
                assertEquals(message + " cc:unit@test.net", rawCommand.getCommand());
                assertNull(rawCommand.getErrorMessage());
                rawCommand.setKey(rawCommandKey);
                return rawCommand;
            }
            @Override
            public RawCommand updateRawCommand(PersistenceManager pm, RawCommand rawCommand) {
                assertEquals(rawCommandKey, rawCommand.getKey());
                assertNotNull(rawCommand.getErrorMessage());
                assertEquals(LabelExtractor.get("error_unexpected", new Object[] { rawCommandKey, "" }, Locale.ENGLISH), rawCommand.getErrorMessage());
                return null;
            }
        });

        final Long consumerKey = 56645L;
        BaseSteps.setMockConsumerOperations(new ConsumerOperations() {
            @Override
            public Consumer createConsumer(PersistenceManager pm, InternetAddress address) {
                assertEquals(from, address.getAddress());
                assertEquals(name, address.getPersonal());
                Consumer consumer = new Consumer();
                consumer.setKey(consumerKey);
                consumer.setName(address.getPersonal());
                consumer.setEmail(address.getAddress());
                return consumer;
            }
        });

        MailConnector.foolNextMessagePost();

        new MailResponderServlet().doPost(request, null);
    }

    @Test
    public void testExtractFirstLineIV() {
        // no soft line break, one empty line for the hard line break
        String test = "`Take some more tea,' the March Hare said to Alice, very earnestly.\r\n\r\n`I've had nothing yet,' Alice replied in an offended tone, `so \r\nI can't take more.'\r\n";
        assertEquals("`Take some more tea,' the March Hare said to Alice, very earnestly.", MailResponderServlet.extractFirstLine(test));
    }

    @Test
    public void testExtractFirstLineV() {
        // few soft line breaks, one empty line for the hard line break
        String test = "`Take some more tea,'\r\nthe March Hare said\r\nto Alice, very\r\nearnestly.\r\n\r\n`I've had nothing yet,' Alice replied in an offended tone, `so \r\nI can't take more.'\r\n";
        assertEquals("`Take some more tea,' the March Hare said to Alice, very earnestly.", MailResponderServlet.extractFirstLine(test));
    }

    @Test
    public void testExtractFirstLineVI() {
        // few soft line breaks, signature separator (short) for the hard line break
        String test = "`Take some more tea,'\r\nthe March Hare said\r\nto Alice, very\r\nearnestly.\r\n--\r\n`I've had nothing yet,' Alice replied in an offended tone, `so \r\nI can't take more.'\r\n";
        assertEquals("`Take some more tea,' the March Hare said to Alice, very earnestly.", MailResponderServlet.extractFirstLine(test));
    }

    @Test
    public void testExtractFirstLineVII() {
        // few soft line breaks, signature separator (standard) for the hard line break
        String test = "`Take some more tea,'\r\nthe March Hare said\r\nto Alice, very\r\nearnestly.\r\n-- \r\n`I've had nothing yet,' Alice replied in an offended tone, `so \r\nI can't take more.'\r\n";
        assertEquals("`Take some more tea,' the March Hare said to Alice, very earnestly.", MailResponderServlet.extractFirstLine(test));
    }
}
