package org.domderrien.i18n;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtils {

	/**
	 * Get the current date for the local time zone
	 * @return Current date/time
	 */
	public static Calendar getNowCalendar() {
		return new GregorianCalendar();
	}

	/**
	 * Get the current date for the local time zone
	 * @return Current date/time
	 * 
	 * @deprecated
	 */
	public static Date getNowDate() {
		return getNowCalendar().getTime();
	}
	
    // This is the ISO format for Dojo application
    private static final DateFormat isoFormatter = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss");
    
    /**
     * Transform the given date in an ISO formatted string
     * @param timeInMilliseconds date to transform
     * @return ISO representation of the given date
     */
    public static String millisecondsToISO(long timeInMilliseconds) {
        return dateToISO(new Date(timeInMilliseconds));
    }
    
    /**
     * Transform the given date in an ISO formatted string
     * @param date date to transform
     * @return ISO representation of the given date
     */
    public static String dateToISO(Date date) {
        return isoFormatter.format(date);
    }
    
    /**
     * Extract the date represented by the given ISO string
     * @param iso ISO representation of a date
     * @return Date in milliseconds
     * @throws ParseException if the given string does not have the expected ISO format
     */
    public static long isoToMilliseconds(String iso) throws ParseException {
        return isoToDate(iso).getTime();
    }
    
    /**
     * Extract the date represented by the given ISO string
     * @param iso ISO representation of a date
     * @return Date
     * @throws ParseException if the given string does not have the expected ISO format
     */
    public static Date isoToDate(String iso) throws ParseException {
        if (iso == null || iso.length() == 0) {
            throw new ParseException("Cannot unserialize an empty ISO string", 0);
        }
        isoFormatter.setCalendar(new GregorianCalendar());
        return isoFormatter.parse(iso);
    }
    
}
