package com.stretchcom.rskybox.server;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

public class GMT {
	private static final Logger log = Logger.getLogger(GMT.class.getName());
	
	public static Date convertToGmtDate(String theDate, Boolean theHasTime, TimeZone theTimeZone) {
		return convertToGmtDate(theDate, theHasTime, theTimeZone, null);
	}

	public static Date convertToGmtDate(String theDate, Boolean theHasTime, TimeZone theTimeZone, String theDateFormat) {
		Date date = null;
		
		try {
			if(theHasTime) {
				date = stringToDate(theDate, theTimeZone, theDateFormat);
			} else {
				date = stringWithoutTimeToDate(theDate, theTimeZone);
			}
		} catch(Exception e) {
			return null;
		}
		return date;
	}
	
	// If theTimeZone is null, returns date string in GMT time
	public static String convertToLocalDate(Date theGmtDate, TimeZone theTimeZone) {
		// defaults format to: "yyyy-MM-dd kk:mm"
		return convertToLocalDate(theGmtDate, theTimeZone, "yyyy-MM-dd kk:mm");
	}
	
	// If theTimeZone is null, returns date string in GMT time
	public static String convertToLocalDate(Date theGmtDate, TimeZone theTimeZone, String theDateFormat) {
		if(theGmtDate == null || theDateFormat == null) {
			return null;
		}

    	DateFormat df = new SimpleDateFormat(theDateFormat);
		if(theTimeZone != null) df.setTimeZone(theTimeZone);
		
		String timezoneStr = theTimeZone == null ? "<not_specified>" : theTimeZone.getDisplayName();
		log.info("convertToLocalDate(): timezone = " + timezoneStr + " local date = " + df.format(theGmtDate));
		return df.format(theGmtDate);
	}

	
	// returns null if the TimeZoneStr passed in is not recognized time zone name
	// only full time zone names are supported - not time zone abbreviations
	public static TimeZone getTimeZone(String theTimeZoneNameStr) {
		if(theTimeZoneNameStr == null) {return null;}
		
		String[] timeZoneNames = TimeZone.getAvailableIDs();
		boolean isValid = false;
		for(String s: timeZoneNames) {
			//::TODO should this comparison ignore case?
			if(s.equals(theTimeZoneNameStr)) {
				isValid = true;
				break;
			}
		}
		if(isValid) {
			return TimeZone.getTimeZone(theTimeZoneNameStr);
		} else {
			return null;
		}
	}

	// only supports the format: YYYY-MM-DD kk:mm
	// parses date using specified time zone -- don't want to use the default which depends on server configuration
	public static Date stringToDate(String theDateStr, TimeZone theTimeZone, String theDateFormat) {
		try {
			if(theDateFormat == null) {
				// set to the default format
				theDateFormat = "yyyy-MM-dd kk:mm";
			}
			DateFormat df = new SimpleDateFormat(theDateFormat);
			if(theTimeZone != null) df.setTimeZone(theTimeZone);
			return df.parse(theDateStr);
		} catch (ParseException e) {
			return null;
		}
	}
	
	// only supports the format: YYYY-MM-DD
	// parses date using specified time zone -- don't want to use the default which depends on server configuration
	public static Date stringWithoutTimeToDate(String theDateStr, TimeZone theTimeZone) throws ParseException {
		log.info("stringWithoutTimeToDate(): date input = " + theDateStr);
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		df.setTimeZone(theTimeZone);
		return df.parse(theDateStr);
	}

	// only supports the format: YYYY-MM-DD kk:mm 
	public static String dateToString(Date theDate) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd kk:mm");
		return df.format(theDate);
	}
	
	public static TimeZone getDefaultTimeZone() {
    	return GMT.getTimeZone(RskyboxApplication.DEFAULT_LOCAL_TIME_ZONE);
	}
	
	public static Boolean isDateBeforeNowPlusOffsetMinutes(Date theDate, int theNumberOfOffsetMinutes) {
		Calendar nowPlusOffset = Calendar.getInstance();
		nowPlusOffset.setTime(new Date());
		nowPlusOffset.add(Calendar.MINUTE, theNumberOfOffsetMinutes);
		
		// convert nowPlusOffset to a Date
		Date nowPlusOffsetDate = nowPlusOffset.getTime();
		
		if(theDate.before(nowPlusOffsetDate)) {
			return true;
		}
		return false;
	}

	
	public static Date addMinutesToDate(Date theDate, int theNumberOfMinutes) {
		Calendar newCal = Calendar.getInstance();
		newCal.setTime(theDate);
		newCal.add(Calendar.MINUTE, theNumberOfMinutes);
		
		// convert nowPlusOffset to a Date
		return newCal.getTime();
	}
}
