package twetailer.j2ee;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.validator.ApplicationSettings;

public class TestWaveProfileServlet {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testConstructor() {
        new WaveProfileServlet();
    }

    @Test
    public void testGetProductName() {
        assertEquals(ApplicationSettings.get().getProductName(), new WaveProfileServlet().getRobotName());
    }

    @Test
    public void testGetProductWebsite() {
        assertEquals(ApplicationSettings.get().getProductWebsite(), new WaveProfileServlet().getRobotProfilePageUrl());
    }

    @Test
    public void testGetProductLogo() {
        assertEquals(ApplicationSettings.get().getLogoURL(), new WaveProfileServlet().getRobotAvatarUrl());
    }
}
