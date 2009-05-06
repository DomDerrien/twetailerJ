package org.domderrien.i18n;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestDateUtils {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testNewDateUtils() {
		new DateUtils();
	}

	@Test
	public void testGetNowCalendar() {
		DateUtils.getNowCalendar();
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testGetNowDate() {
		DateUtils.getNowDate();
	}

	@Test
	public void testDateToISO() {
        Date date = new GregorianCalendar(2008, 0, 23, 1, 23, 45).getTime();
        String computedDate = DateUtils.dateToISO(date);
        String[] dateParts = computedDate.split("T")[0].split("-");
        String[] timeParts = computedDate.split("T")[1].split(":");
        assertEquals("2008", dateParts[0]);
        assertEquals("01", dateParts[1]);
        assertEquals("23", dateParts[2]);
        assertEquals("01", timeParts[0]);
        assertEquals("23", timeParts[1]);
        assertEquals("45", timeParts[2]);
	}

	@Test
	public void testMillisecondsToISO() {
        long time = (new GregorianCalendar(2008, 0, 23, 1, 23, 45)).getTimeInMillis();
        String computedDate = DateUtils.millisecondsToISO(time);
        String[] dateParts = computedDate.split("T")[0].split("-");
        String[] timeParts = computedDate.split("T")[1].split(":");
        assertEquals("2008", dateParts[0]);
        assertEquals("01", dateParts[1]);
        assertEquals("23", dateParts[2]);
        assertEquals("01", timeParts[0]);
        assertEquals("23", timeParts[1]);
        assertEquals("45", timeParts[2]);
	}

	@Test
	public void testISOToDateI() {
        try {
        	String iso = "2008-01-23T01:23:45";
        	Date expectedDate = new GregorianCalendar(2008, 0, 23, 1, 23, 45).getTime();
			assertEquals(expectedDate, DateUtils.isoToDate(iso));
	        iso = "2008-01-23T01:23:45Z"; // Reference to UTC-0 is ignored
	        assertEquals(expectedDate, DateUtils.isoToDate(iso));
	        iso = "2008-01-23T01:23:45-0500"; // Reference to a time zone is ignored
	        assertEquals(expectedDate, DateUtils.isoToDate(iso));
		}
        catch (ParseException ex) {
			fail("No ParseException expected -- " + ex);
		}
	}

	@Test(expected=ParseException.class)
	public void testISOToDateII() throws ParseException {
		DateUtils.isoToDate(null);
	}

	@Test(expected=ParseException.class)
	public void testISOToDateIII() throws ParseException {
		DateUtils.isoToDate("");
	}

	@Test(expected=ParseException.class)
	public void testISOToDateIV() throws ParseException {
		DateUtils.isoToDate("garbage");
	}

	@Test
	public void testISOToMilliseconds() {
        try {
        	String iso = "2008-01-23T01:23:45";
        	long expectedTime = (new GregorianCalendar(2008, 0, 23, 1, 23, 45)).getTimeInMillis();
			assertEquals(expectedTime, DateUtils.isoToMilliseconds(iso));
			// No other test because DateUtils.isoToMilliseconds() relies on DateUtils.isoToDate()
		}
        catch (ParseException ex) {
			fail("No ParseException expected -- " + ex);
		}
	}

}
