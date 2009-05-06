package org.domderrien.i18n;

import static org.junit.Assert.*;

import java.util.ListResourceBundle;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import org.domderrien.MockHttpServletRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestLocaleController {

    class MockResourceBundle extends ListResourceBundle {
        public final String EN = "en"; //$NON-NLS-1$
        public final String FR = "fr"; //$NON-NLS-1$ 
        public final String FR_CA = "fr_CA"; //$NON-NLS-1$
        public final String FR_BE = "fr_BE"; //$NON-NLS-1$
        public final String PT_BR = "pt_BR"; //$NON-NLS-1$
        public final String ENGLISH = "English"; //$NON-NLS-1$
        public final String FRENCH = "Français"; //$NON-NLS-1$
        public final String FRENCH_CANADIAN = "Français canadien"; //$NON-NLS-1$
        private Object[][] contents = new Object[][]{
            {EN, ENGLISH},
            {FR, FRENCH},
            {FR_CA, FRENCH_CANADIAN}
        };
        protected Object[][] getContents() {
            return contents;
        }         
    }

	MockResourceBundle mock = new MockResourceBundle();
	
	@Before
	public void setUp() throws Exception {
		LocaleController.setLanguageListRB(null);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testNewLocaleController() {
		new LocaleController();
	}

	@Test
	public void testSetLanguageListRB() {
		LocaleController.setLanguageListRB(mock);
	}

	@Test
	public void testGetLanguageListRBI() {
		LocaleController.setLanguageListRB(mock);
		assertEquals(mock, LocaleController.getLanguageListRB());
	}

	@Test
	public void testGetLanguageListRBII() {
		ResourceBundle liveRB = LocaleController.getLanguageListRB();
		assertNotNull(liveRB);
		assertNotNull(liveRB.getString(mock.EN));
		assertEquals(mock.ENGLISH, liveRB.getString(mock.EN));
	}

	@Test
	public void testGetLocaleIdI() {
		LocaleController.setLanguageListRB(mock);
		assertEquals(LocaleController.DEFAULT_LANGUAGE_ID, LocaleController.getLocaleId(null, true));
	}

	@Test
	public void testGetLocaleIdII() {
		LocaleController.setLanguageListRB(mock);
		MockHttpServletRequest request = new MockHttpServletRequest(){
			public String getParameter(String arg0) {
				assertEquals(LocaleController.REQUEST_LOCALE_KEY, arg0);
				return mock.FR_CA;
			}
		};
		assertEquals(mock.FR_CA, LocaleController.getLocaleId(request, true));
	}
	
	@Test
	public void testGetLocaleIdIII() {
		LocaleController.setLanguageListRB(mock);
		MockHttpServletRequest request = new MockHttpServletRequest(){
			public String getParameter(String arg0) {
				assertEquals(LocaleController.REQUEST_LOCALE_KEY, arg0);
				return mock.FR_BE;
			}
		};
		assertEquals(mock.FR, LocaleController.getLocaleId(request, true));
	}
	
	@Test
	public void testGetLocaleIdIV() {
		LocaleController.setLanguageListRB(mock);
		MockHttpServletRequest request = new MockHttpServletRequest(){
			public String getParameter(String arg0) {
				assertEquals(LocaleController.REQUEST_LOCALE_KEY, arg0);
				return mock.PT_BR;
			}
		};
		assertEquals(LocaleController.DEFAULT_LANGUAGE_ID, LocaleController.getLocaleId(request, true));
	}

	/*
	@Test
	public void testDetectLocale() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetLocaleIdHttpServletRequestBoolean() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetLocaleHttpServletRequest() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetLocaleString() {
		fail("Not yet implemented");
	}
	*/

}
