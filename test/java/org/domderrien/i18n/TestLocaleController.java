package org.domderrien.i18n;

import static org.junit.Assert.*;

import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.domderrien.MockHttpServletRequest;
import org.domderrien.MockHttpSession;
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
		public final String FRENCH = "Fran�ais"; //$NON-NLS-1$
		public final String FRENCH_CANADIAN = "Fran�ais canadien"; //$NON-NLS-1$
		private Object[][] contents = new Object[][] {
				{ EN, ENGLISH },
				{ FR, FRENCH },
				{ FR_CA, FRENCH_CANADIAN }
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
	public void testGetLocaleId0() {
		LocaleController.setLanguageListRB(mock);
		assertEquals(LocaleController.DEFAULT_LANGUAGE_ID, LocaleController.getLocaleId(null));
	}

	@Test
	public void testGetLocaleIdI() {
		LocaleController.setLanguageListRB(mock);
		assertEquals(LocaleController.DEFAULT_LANGUAGE_ID, LocaleController.getLocaleId(null, true));
	}

	@Test
	public void testGetLocaleIdII() {
		LocaleController.setLanguageListRB(mock);
		MockHttpServletRequest request = new MockHttpServletRequest() {
			public String getParameter(String arg0) {
				assertEquals(LocaleController.REQUEST_LOCALE_KEY, arg0);
				return "";
			}
		};
		assertEquals(LocaleController.DEFAULT_LANGUAGE_ID, LocaleController.getLocaleId(request, true));
	}

	@Test
	public void testGetLocaleIdIII() {
		LocaleController.setLanguageListRB(mock);
		MockHttpServletRequest request = new MockHttpServletRequest() {
			public String getParameter(String arg0) {
				assertEquals(LocaleController.REQUEST_LOCALE_KEY, arg0);
				return mock.FR_CA;
			}
		};
		assertEquals(mock.FR_CA, LocaleController.getLocaleId(request, true));
	}

	@Test
	public void testGetLocaleIdIV() {
		LocaleController.setLanguageListRB(mock);
		MockHttpServletRequest request = new MockHttpServletRequest() {
			public String getParameter(String arg0) {
				assertEquals(LocaleController.REQUEST_LOCALE_KEY, arg0);
				return mock.FR_BE;
			}
		};
		assertEquals(mock.FR, LocaleController.getLocaleId(request, true));
	}

	@Test
	public void testGetLocaleIdV() {
		LocaleController.setLanguageListRB(mock);
		MockHttpServletRequest request = new MockHttpServletRequest() {
			public String getParameter(String arg0) {
				assertEquals(LocaleController.REQUEST_LOCALE_KEY, arg0);
				return mock.PT_BR;
			}
		};
		assertEquals(LocaleController.DEFAULT_LANGUAGE_ID, LocaleController.getLocaleId(request, true));
	}

	@Test
	public void testGetLocaleIdVI() {
		LocaleController.setLanguageListRB(mock);
		MockHttpServletRequest request = new MockHttpServletRequest() {
			public String getParameter(String arg0) {
				assertEquals(LocaleController.REQUEST_LOCALE_KEY, arg0);
				return null;
			}
			public Locale getLocale() {
				// Even with FR (not FR_CA), the country is set (to FR in this case)
				return Locale.FRANCE;
			}
		};
		assertEquals(mock.FR, LocaleController.getLocaleId(request, true));
	}

	@Test
	public void testGetLocaleIdVII() {
		LocaleController.setLanguageListRB(mock);
		MockHttpServletRequest request = new MockHttpServletRequest() {
			public String getParameter(String arg0) {
				assertEquals(LocaleController.REQUEST_LOCALE_KEY, arg0);
				return null;
			}
			public Locale getLocale() {
				// Non recognized language will make return an empty country code
				return new Locale("dd");
			}
		};
		assertEquals(LocaleController.DEFAULT_LANGUAGE_ID, LocaleController.getLocaleId(request, true));
	}

	@Test
	public void testGetLocaleIdVIII() {
		LocaleController.setLanguageListRB(mock);
		MockHttpServletRequest request = new MockHttpServletRequest() {
			public Locale getLocale() {
				return null;
			}
		};
		assertEquals(LocaleController.DEFAULT_LANGUAGE_ID, LocaleController.getLocaleId(request, true));
	}
	
	@Test
	public void testGetLocaleIdIX() {
		LocaleController.setLanguageListRB(mock);
		MockHttpServletRequest request = new MockHttpServletRequest() {
			public HttpSession getSession(boolean create) {
				assertFalse(create);
				return null;
			}
		};
		assertEquals(LocaleController.DEFAULT_LANGUAGE_ID, LocaleController.getLocaleId(request, false));
	}
	
	@Test
	public void testGetLocaleIdX() {
		LocaleController.setLanguageListRB(mock);
		MockHttpServletRequest request = new MockHttpServletRequest() {
			public HttpSession getSession(boolean create) {
				assertFalse(create);
				return new MockHttpSession() {
					public Object getAttribute(String arg0) {
						assertEquals(LocaleController.SESSION_LOCALE_ID_KEY, arg0);
						return null;
					};
				};
			}
		};
		assertEquals(LocaleController.DEFAULT_LANGUAGE_ID, LocaleController.getLocaleId(request, false));
	}
	
	@Test
	public void testGetLocaleIdXI() {
		LocaleController.setLanguageListRB(mock);
		MockHttpServletRequest request = new MockHttpServletRequest() {
			public HttpSession getSession(boolean create) {
				assertFalse(create);
				return new MockHttpSession() {
					public Object getAttribute(String arg0) {
						assertEquals(LocaleController.SESSION_LOCALE_ID_KEY, arg0);
						return "";
					};
				};
			}
		};
		assertEquals(LocaleController.DEFAULT_LANGUAGE_ID, LocaleController.getLocaleId(request, false));
	}
	
	@Test
	public void testGetLocaleIdXII() {
		LocaleController.setLanguageListRB(mock);
		MockHttpServletRequest request = new MockHttpServletRequest() {
			public HttpSession getSession(boolean create) {
				assertFalse(create);
				return new MockHttpSession() {
					public Object getAttribute(String arg0) {
						assertEquals(LocaleController.SESSION_LOCALE_ID_KEY, arg0);
						return mock.FR_CA;
					};
				};
			}
		};
		assertEquals(mock.FR_CA, LocaleController.getLocaleId(request, false));
	}
	
	@Test
	public void testGetLocaleI() {
		assertEquals(LocaleController.DEFAULT_LOCALE, LocaleController.getLocale((String) null));
	}
	
	@Test
	public void testGetLocaleII() {
		assertEquals(LocaleController.DEFAULT_LOCALE, LocaleController.getLocale(""));
	}
	
	@Test
	public void testGetLocaleIII() {
		assertEquals(Locale.CANADA_FRENCH, LocaleController.getLocale(mock.FR_CA));
	}
	
	@Test
	public void testGetLocaleIV() {
		assertEquals(Locale.FRENCH, LocaleController.getLocale(mock.FR));
	}
	
	@Test
	public void testGetLocaleV() {
		assertEquals(LocaleController.DEFAULT_LOCALE, LocaleController.getLocale((HttpServletRequest) null));
	}

	@Test
	public void testDetectLocaleI() {
		assertEquals(LocaleController.DEFAULT_LOCALE, LocaleController.detectLocale(null));
	}
	
	@Test
	public void testDetectLocaleIdII() {
		LocaleController.setLanguageListRB(mock);
		MockHttpServletRequest request = new MockHttpServletRequest() {
			public String getParameter(String arg0) {
				assertEquals(LocaleController.REQUEST_LOCALE_KEY, arg0);
				return mock.FR_CA;
			}
			public HttpSession getSession(boolean create) {
				assertFalse(create);
				return new MockHttpSession() {
					public void setAttribute(String arg0, Object arg1) {
						assertEquals(LocaleController.SESSION_LOCALE_ID_KEY, arg0);
						assertEquals(mock.FR_CA, arg1);
					};
				};
			}
		};
		assertEquals(Locale.CANADA_FRENCH, LocaleController.detectLocale(request));
	}
}