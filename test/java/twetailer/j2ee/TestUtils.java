package twetailer.j2ee;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.MockHttpServletRequest;
import javax.servlet.http.MockHttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestUtils {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testConstructor() {
        new ServletUtils();
    }

    @Test
    public void testConfigureHttpParametersI() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public void setCharacterEncoding(String encoding) {
                assertEquals("UTF-8", encoding);
            }
        };
        MockHttpServletResponse mockResponse = new MockHttpServletResponse() {
            @Override
            public void setCharacterEncoding(String encoding) {
                assertEquals("UTF-8", encoding);
            }
            @Override
            public void setContentType(String type) {
                assertNotNull(type);
                assertTrue(type.contains("text/javascript"));
            }
        };
        ServletUtils.configureHttpParameters(mockRequest, mockResponse);
    }

    @Test
    public void testConfigureHttpParametersII() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {
            @Override
            public void setCharacterEncoding(String encoding) throws UnsupportedEncodingException {
                // Should be ignored, silently caught
                throw new UnsupportedEncodingException("done in purpose");
            }
        };
        ServletUtils.configureHttpParameters(mockRequest, new MockHttpServletResponse());
    }
}
